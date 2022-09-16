package edu.niu.z1811457.weatherproject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

//A single day of weather information
public class Weather
{
    private String dayOfWeek,
            minTemp,
            maxTemp,
            humidity,
            description,
            iconURL;

    public Weather(long timeStamp, double minT, double maxT, double humid, String descript, String iconName)
    {
        //Format the double temperature and round to nearest integer
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);

        //Find the day of the week
        dayOfWeek = convertTimeStampToDay(timeStamp);

        //Format the minimum and maximum temperatures as integers
        //and add a degree symbol (\u00B0) and F (for Fahrenheit)
        minTemp = numberFormat.format(minT) + "\u00B0F";
        maxTemp = numberFormat.format(maxT) + "\u00B0F";

        //Format the humidity percentage
        humidity = NumberFormat.getPercentInstance().format(humid/100.0);

        //Set the description
        description = descript;

        //Set the url for where the image is located and image name
        iconURL = "http://openweathermap.org/img/w/" + iconName + ".png";
    }//end constructor

    //Method that converts a timestamp to a day of the week
    //
    //the timeStamp argument is the number of seconds since January 1, 1970
    private static String convertTimeStampToDay( long timeStamp )
    {
        //Get a calendar object to calculate the day of the week
        Calendar calendar = Calendar.getInstance();
        //set the time for the calendar object
        calendar.setTimeInMillis(timeStamp * 1000);

        //get the default timezone for the device
        TimeZone timeZone = TimeZone.getDefault();

        //adjust the time for the device's timezone
        calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.getTimeInMillis()));

        //Specify the format for a Date
        // EEEE specifies to use just the day name
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");

        //retrieve a date object from the calendar and return the corresponding day name
        return dateFormat.format(calendar.getTime());
    }//end convertTimeStampToDay

    //Getters
    public String getDayOfWeek()
    {
        return dayOfWeek;
    }

    public String getMinTemp()
    {
        return minTemp;
    }

    public String getMaxTemp()
    {
        return maxTemp;
    }

    public String getHumidity()
    {
        return humidity;
    }

    public String getDescription()
    {
        return description;
    }

    public String getIconURL()
    {
        return iconURL;
    }
}//end Weather class
