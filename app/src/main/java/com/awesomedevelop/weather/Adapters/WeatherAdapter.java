package com.awesomedevelop.weather.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.awesomedevelop.weather.R;
import com.awesomedevelop.weather.WeatherData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Taras on 07.04.2015.
 */
public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.MyViewHolder> {


    private ArrayList<WeatherData> weatherDataSet;
    public Context mContext;

    public static class MyViewHolder extends RecyclerView.ViewHolder  {
        TextView textDescription,textTemp,textMin,textMax,date;
        ImageView Icon;

        public MyViewHolder(View itemView){
            super (itemView);
            this.Icon = (ImageView)itemView.findViewById(R.id.icon);
            this.textDescription = (TextView)itemView.findViewById(R.id.description);
            this.textTemp = (TextView)itemView.findViewById(R.id.temp);
            this.textMax = (TextView)itemView.findViewById(R.id.temp_max);
            this.textMin = (TextView)itemView.findViewById(R.id.temp_min);
            this.date = (TextView)itemView.findViewById(R.id.date);

        }
    }


    public WeatherAdapter(Context context, ArrayList<WeatherData> weather_d){
        this.weatherDataSet= weather_d;
        mContext=context;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_card, parent, false);



        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final TextView textViewName = holder.textDescription;
        final ImageView icon = holder.Icon;
        final TextView textTemp = holder.textTemp;
        final TextView textMax = holder.textMax;
        final TextView textMin = holder.textMin;
        final TextView date = holder.date;
        String UpDescription = weatherDataSet.get(listPosition).getDescription().substring(0,1).toUpperCase()+weatherDataSet.get(listPosition).getDescription().substring(1);
        textViewName.setText(UpDescription);
        textTemp.setText(String.valueOf(weatherDataSet.get(listPosition).getTemp()));
        textMax.setText(String.valueOf(weatherDataSet.get(listPosition).getTemp_max()));
        textMin.setText(String.valueOf(weatherDataSet.get(listPosition).getTemp_min()));
        date.setText(weatherDataSet.get(listPosition).getDate());
        String src = "http://openweathermap.org/img/w/"+ weatherDataSet.get(listPosition).getIcon()+".png";
        Picasso.with(mContext)
                .load(src)
                .resize(150, 150)
                .into(icon);




    }





    @Override
    public int getItemCount() {
        return weatherDataSet.size();
    }





}
