package com.awesomedevelop.weather;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.awesomedevelop.weather.Adapters.WeatherAdapter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity  {
    private static ArrayList<WeatherData> weather_data;
    private static RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView.Adapter adapter;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    public final static String TABLE="CacheWeather";
    public final static String ID="_id";
    public final static String DESCRIPTION="description";
    public final static String ICON="icon";
    public final static String TEMP="temp";
    public final static String TEMP_MIN="temp_min";
    public final static String TEMP_MAX="temp_max";
    public final static String DATE="date";
    protected LocationManager locationManager;
    IsInternetExist isExist;
     ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        isExist = new IsInternetExist();
        dialog = new ProgressDialog(MainActivity.this);
        new update_weather().execute("");




    }



private class update_weather extends AsyncTask <String,Void,Void> {

    @Override
    protected void onPreExecute() {
      dialog.setMessage("Обновление");
      dialog.show();
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... params) {





        if(params[0].equals("")){

            if(!check_date() && isExist.isNetworkConnected(getApplicationContext())) {

                get_weather_update(current_city());
                cache_weather();
                load_cahed_weather();
            }
            else {
                load_cahed_weather();
            }


        }
        else if(!params[0].equals("") && isExist.isNetworkConnected(getApplicationContext())) {
            get_weather_update(params[0]);
        }
        else {
            Toast.makeText(getApplicationContext(),"Отсуствует интернет соединение",Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        adapter = new WeatherAdapter(MainActivity.this,weather_data);
        recyclerView.setAdapter(adapter);
        super.onPostExecute(aVoid);
    }
}





private void get_weather_update(String s){
    String description="",icon="",date;
    Double temp,temp_min,temp_max;
    weather_data = new ArrayList<WeatherData>();

    try {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://api.openweathermap.org/data/2.5/forecast?q="+s+"&lang=ua&units=metric&mode=json");
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String resp = EntityUtils.toString(entity);

        JSONObject j = new JSONObject(resp);
        JSONArray arr = j.getJSONArray("list");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject json = arr.getJSONObject(i);
            date = json.getString("dt_txt");
            JSONArray weather = json.getJSONArray("weather");
            JSONObject Position = weather.getJSONObject(0);
            description = Position.getString("description");
            icon = Position.getString("icon");



            JSONObject main = json.getJSONObject("main");
            temp  = main.getDouble("temp");
            temp_min = main.getDouble("temp_min");
            temp_max = main.getDouble("temp_max");
            Log.i("DATE",date);





            weather_data.add(new WeatherData(description,icon,temp,temp_min,temp_max,date));
        }
        System.out.println("city list:" + j.toString());
    } catch (Exception e) {
        System.out.println(e.getMessage());
    }

}


private void cache_weather(){
    dbHelper = new DatabaseHelper(getApplicationContext());
    database = dbHelper.getWritableDatabase();
    dbHelper.onCreate(database);
    ContentValues values = new ContentValues();
    for(int i=0;i<weather_data.size();i++){
        values.put(DESCRIPTION,weather_data.get(i).description);
        values.put(ICON,weather_data.get(i).icon);
        values.put(TEMP,weather_data.get(i).temp);
        values.put(TEMP_MIN,weather_data.get(i).temp_min);
        values.put(TEMP_MAX,weather_data.get(i).temp_max);
        values.put(DATE,weather_data.get(i).date);
        database.insert(TABLE,null,values);

    }

    Calendar c = Calendar.getInstance();
    c.getTime();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
    String formattedDate = df.format(c.getTime());
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    SharedPreferences.Editor editor=prefs.edit();
    editor.putString("current_data", formattedDate);
    editor.commit();
}
private void load_cahed_weather () {
    String description="",icon="",date="";
    Double temp=null,temp_min=null,temp_max=null;
    dbHelper = new DatabaseHelper(getApplicationContext());
    database = dbHelper.getWritableDatabase();
    weather_data = new ArrayList<WeatherData>();


    String[] data = new String[] {DESCRIPTION, ICON};
    Cursor mCursor = database.query(TABLE,
            new String[] {DESCRIPTION, ICON, TEMP, TEMP_MIN, TEMP_MAX, DATE},
            null,
            null, null, null, null);
    mCursor.moveToFirst();
    if(!mCursor.isAfterLast()){
        do {
            description=mCursor.getString(0);
            icon=mCursor.getString(1);
            temp=Double.valueOf(mCursor.getString(2));
            temp_min=Double.valueOf(mCursor.getString(3));
            temp_max=Double.valueOf(mCursor.getString(4));
            date=mCursor.getString(5);
            weather_data.add(new WeatherData(description,icon,temp,temp_min,temp_max,date));

        }while (mCursor.moveToNext());
    }
}


private String current_city(){

    String city=null;
    double latitude,longitude;
    Location location;
    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



    location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
    latitude=location.getLatitude();
    longitude = location.getLongitude();
    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
    try
    {
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

        city = addresses.get(0).getLocality().toString();

    }
    catch (IOException e)
    {
        e.printStackTrace();
    }
    return city;
}


private boolean check_date(){
    String last_data;
    Calendar c = Calendar.getInstance();
    c.getTime();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
    String formattedDate = df.format(c.getTime());
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    last_data=prefs.getString("current_data", null);
    if (last_data!=null){
    if (last_data.equals(formattedDate)){
    return true;
    }
    else {
        return false;
    }
    }
    else return false;
}






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(isExist.isNetworkConnected(getApplicationContext())) {
                    new update_weather().execute(s);
                    return true;
                }
                else {
                    Toast.makeText(getApplicationContext(),"Отсуствует интернет соединение",Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(final String s) {

                return false;
            }
        });

        MenuItem refresh = menu.findItem(R.id.action_refresh);
        refresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(isExist.isNetworkConnected(getApplicationContext())){
                    new update_weather().execute("");
                    return true;
                }
                else{
                    Toast.makeText(getApplicationContext(),"Отсуствует интернет соединение",Toast.LENGTH_SHORT).show();

                }
                return true;

            }
        });

        return true;
    }








    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            new update_weather().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
