package com.awesomedevelop.weather;

/**
 * Created by Taras on 07.04.2015.
 */
public class WeatherData {
    String description;
    Double temp;
    Double temp_min;
    Double temp_max;
    String icon;
    String date;



    public WeatherData(String description, String icon,Double temp,Double temp_min,Double temp_max, String date){
        this.description = description;
        this.temp = temp;
        this.temp_min = temp_min;
        this.temp_max = temp_max;
        this.icon = icon;
        this.date = date;

    }
public String getDescription (){return description;}
public Double getTemp(){return temp;}
public Double getTemp_min(){return temp_min;}
public Double getTemp_max(){return temp_max;}
public String getIcon() {return icon;}
public String getDate(){return date;}





}

//Tag.java

