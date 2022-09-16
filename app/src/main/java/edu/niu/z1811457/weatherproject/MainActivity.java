package edu.niu.z1811457.weatherproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Weather> weatherList;
    private WeatherAdapter weatherAdapter;
    private RecyclerView weatherRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create the array list
        weatherList = new ArrayList<>();

        //create the adapter that will be used to populate the recyclerview
        weatherAdapter = new WeatherAdapter(this, weatherList);

        //connect the recyclerview to object on activity_mai.xml
        weatherRV = findViewById(R.id.weatherRecyclerView);

        //use a linear layout for the recyclerview
        weatherRV.setLayoutManager(new LinearLayoutManager(this));

        //set the adapter with the recyclerview
        weatherRV.setAdapter(weatherAdapter);
    }//end onCreate

    //Method to createa URL object from a city name
    private URL createURL(String city) {
        //create strings with the password and start of the web address
        String apiKey = getString(R.string.api_key),
                baseURL = getString(R.string.web_url);

        try {
            //build a url with web address, city name, imperial units (F temps), password

            //change units to metric - Celsius temperatires
                        //standard - Kelvin temperatures
            // cnt = number of days of temperatures, 16 is the max 7 is the default

            String urlString = baseURL + URLEncoder.encode(city, "UTF-8") +
                            "&units=imperial&cnt=16&APPID=" + apiKey;
            return new URL(urlString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }//end createURL

    //method to dismiss the keyboard after the button is clicked
    private void dismissKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        //dismiss the keyboard
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }//end dismissKeyboard

    //custom async task to download the weather information
    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject> {
        static final String LOG_TAG = "MAIN";

        @Override
        protected JSONObject doInBackground(URL... urls) {

            HttpURLConnection connection = null;

            try {
                Log.d(LOG_TAG, "URL is " + urls[0]);

                //try to open the connection using the specified url
                connection = (HttpURLConnection)urls[0].openConnection();

                //get the response for the connection
                int response = connection.getResponseCode();

                //if the connection was made
                if( response == HttpURLConnection.HTTP_OK) {
                    //build a string with the json data
                    StringBuilder builder = new StringBuilder();

                    //use try for json data
                    try{
                        //create an input stream to read the data
                        BufferedReader reader = new BufferedReader( new InputStreamReader(connection.getInputStream()));

                        //read the 1st string from input
                        String line;
                        line = reader.readLine();

                        //while there is input
                        while (line != null) {
                            //append the line to data
                            builder.append(line);

                            //read the next piece of data
                            line = reader.readLine();
                        }
                    }
                    catch (IOException ioException) {
                        ioException.printStackTrace();
                        Log.d(LOG_TAG, getString(R.string.read_error));
                    }

                    //use the string that was just built to create a JSON object and return it
                    return new JSONObject(builder.toString());
                }
                else {
                    Log.d(LOG_TAG, getString(R.string.connect_error));
                }
            }
            catch (Exception e) {
                Log.d(LOG_TAG, getString(R.string.connect_error));
                e.printStackTrace();
            }
            finally {
                //disonnect the connection
                connection.disconnect();
            }
            return null;
        }//end doInBackground

        //method that parses the JSON data and creates weather objects that are put into the arraylist
        private void convertJSONtoArrayList(JSONObject forecast) {
            //clear the existing data in the arraylist
            weatherList.clear();

            //parse the JSON object
            try {
                //get the JSON array that holders the data
                JSONArray list = forecast.getJSONArray("list");

                //divide the JSONarray into the individual weather objects
                for (int sub = 0; sub < list.length(); sub++) {
                    //retrieve the day of the week for the current array object
                    JSONObject day = list.getJSONObject(sub);

                    //retrieve the temperatures for the day of the week
                    JSONObject temperatures = day.getJSONObject("temp");

                    //retrieve the weather description for the day of the week
                    JSONObject description = day.getJSONArray("weather").getJSONObject(0);

                    //create the weather object for the day of the week
                    Weather weather = new Weather(day.getLong("dt"),
                                                  temperatures.getDouble("min"),
                                                  temperatures.getDouble("max"),
                                                  day.getDouble("humidity"),
                                                  description.getString("description"),
                                                  description.getString("icon"));

                    //add the weather object to the arraylist
                    weatherList.add(weather);
                }//end for loop
            }
            catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }//end convert method

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            //populate the arraylist with parased json data
            convertJSONtoArrayList(jsonObject);

            //update the contents of the recyclerview
            weatherAdapter.notifyDataSetChanged();

            weatherRV.smoothScrollToPosition(0);
        }//end onPostExecute
    }//end GetWeatherTask

    //method to handle the button click
    public void getWeather (View view) {
        //retrieve the cityname from the edittext and use it create a url
        EditText cityET = findViewById(R.id.cityEditText);

        URL url = createURL(cityET.getText().toString());

        //if the url was created
        if(url != null) {
            //dismiss the keyboard
            dismissKeyboard(cityET);

            //create asynctask to retrieve the weather information
            GetWeatherTask weatherTask = new GetWeatherTask();

            //execute the asynctask and download the information
            weatherTask.execute(url);
        }
        else { //url was not created
            //display an invalid url error
            Toast.makeText(MainActivity.this, R.string.invalid_url, Toast.LENGTH_LONG).show();
        }
    }//end getWeather
}