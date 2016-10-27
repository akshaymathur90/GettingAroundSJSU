package edu.sjsu.gettingaroundsjsu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BuildingDetail extends AppCompatActivity {

    private final String TAG = getClass().getName().toString();
    TextView textViewDistance,textViewTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewDistance = (TextView) findViewById(R.id.tv_distance);
        textViewTime =(TextView) findViewById(R.id.tv_time);

        double org_lat = 37.335285;
        double org_lng = -121.884487;
        double dest_lat = 37.334815;
        double dest_lng = -121.880894;


        DistanceAsyncTask distanceAsyncTask = new DistanceAsyncTask();
        distanceAsyncTask.execute("https://maps.googleapis.com/maps/api/distancematrix/json?origins=37.335285,-121.884487&destinations=37.334815,-121.880894&mode=walking&units=imperial&key=AIzaSyDm7GmsBE4SIJojzIizbZ7sSEn_yqA0LyU");



    }

    public void updateDistanceUI(String distance, String time){
        textViewDistance.setText(distance);
        textViewTime.setText(time);
    }


    public class DistanceAsyncTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {

            try{
                URL newURL = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection)newURL.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                StringBuffer json = new StringBuffer(1024);
                String tmp="";
                while((tmp=reader.readLine())!=null)
                    json.append(tmp).append("\n");
                reader.close();
                Log.d(TAG,"json to string "+json.toString());
                return json.toString();
            } catch (Exception e){
                Log.e(TAG,e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject distanceObject = new JSONObject(s);
                JSONObject jsonObj = (JSONObject) distanceObject.getJSONArray("rows").get(0);
                JSONObject jsonElements = (JSONObject) jsonObj.getJSONArray("elements").get(0);
                String d = jsonElements.getJSONObject("distance").getString("text");
                String t = jsonElements.getJSONObject("duration").getString("text");

                Log.d(TAG,"distance is --> "+d);
                Log.d(TAG,"duration is --> "+t);

                updateDistanceUI(d,t);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
