package edu.niu.z1811457.weatherproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.MyViewHolder> { //MyViewHolder = shows that it is custom
    //add in the constructor, onCreateViewHolder, onBindViewHolder, getItemCount, custom viewholder, custom asynctask

    //data membbers
    private ArrayList<Weather> weatherData;
    private LayoutInflater inflater;
    private Context context;
    private Map<String, Bitmap> bitmaps;

    //constructor
    public WeatherAdapter(Context newContext, ArrayList<Weather> newWeatherData) {
        context = newContext;
        weatherData = newWeatherData;

        inflater = LayoutInflater.from(context);
        bitmaps = new HashMap<>();
    }//end constructor

    //custom viewHolder
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView pictureIV;
        public TextView dayTV, lowTV, highTV, humidityTV;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //connect all the data members to the fields on the screen
            pictureIV = itemView.findViewById(R.id.conditionImageView);
            dayTV = itemView.findViewById(R.id.dayTextView);
            lowTV = itemView.findViewById(R.id.lowTextView);
            highTV = itemView.findViewById(R.id.highTextView);
            humidityTV = itemView.findViewById(R.id.humidityTextView);
        }
    }//end MyViewHolder

    //custom asynctask
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        public LoadImageTask(ImageView image) {
            imageView = image;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                //create a URL object from the passed in string
                URL url = new URL(strings[0]);

                //open a connection with the url
                connection = (HttpURLConnection)url.openConnection();

                try{
                    //Get an input stream to retrieve the bitmap
                    InputStream inputStream = connection.getInputStream();

                    //retrieve the bitmap
                    bitmap = BitmapFactory.decodeStream(inputStream);

                    //insert the bitmap into Map object using url as the key
                    bitmaps.put(strings[0], bitmap);
                }

                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            catch (Exception e) {
                e.printStackTrace(); //displays in log
            }
            finally { //executed after try or catch
                //disconnect the URL Connection
                connection.disconnect();
            }
            return bitmap;
        }//end doInBackground

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //populate the imageview data member with the bitmap
            imageView.setImageBitmap(bitmap);
        }
    }//end LoadImageTask

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //attack the custom layout file
        View view = inflater.inflate(R.layout.city_view, parent, false);

        return new MyViewHolder(view);
    }//end onCreateViewHolder

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //get the weather object at the current position
        Weather weather = weatherData.get(position);

        //populate the textviews with the data from the weather object
        holder.dayTV.setText(context.getString(R.string.day_description, weather.getDayOfWeek(), weather.getDescription()));

        holder.lowTV.setText(context.getString(R.string.low_temp, weather.getMinTemp()));
        holder.highTV.setText(context.getString(R.string.high_temp, weather.getMaxTemp()));

        holder.humidityTV.setText(context.getString(R.string.humidity, weather.getHumidity()));

        //populate the imageview
        //the image bitmaps are all stored in the Map object with the URL as the key for the pairing
        //if the key exists, the image in already in the map
        if(bitmaps.containsKey(weather.getIconURL())) {
            //populate the imageview from the Map
            holder.pictureIV.setImageBitmap( bitmaps.get(weather.getIconURL()) );
        }
        else { //the key does not exist, image must be downloaded
            //use the asynctask to download the image, add to the Map, and populate the imageview
            LoadImageTask loadImageTask = new LoadImageTask(holder.pictureIV);
            loadImageTask.execute(weather.getIconURL());
        }
    }//end onbindViewHolder

    @Override
    public int getItemCount() {
        return weatherData.size();
    }
} //end WeatherAdapter
