package edu.sjsu.gettingaroundsjsu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dmodh on 10/23/16.
 */

public class BuildingDetailsActivity extends Activity {

    private final String TAG = getClass().getName().toString();

    TableLayout tl;
    TableRow tr;
    TextView buildingNameTV, addressTV, distanceTV, timeTV;
    ImageView buildingImage;
    Button closeActivity;
    ImageButton streetViewButton;
    String latitude, longitude, destLatitude, destLongitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_building);

        Bundle bundle = getIntent().getExtras();
        latitude = bundle.getString("latitude");
        longitude = bundle.getString("longitude");
        destLatitude = bundle.getString("destLatitude");
        destLongitude = bundle.getString("destLongitude");
        Log.d(TAG,"Latitude-->"+latitude);
        Log.d(TAG,"Longitude-->"+longitude);
        Log.d(TAG,"DestLatitude-->"+destLatitude);
        Log.d(TAG,"DestLongitude-->"+destLongitude);
        // destLongitude needs minus sign

        DistanceAsyncTask distanceAsyncTask = new DistanceAsyncTask();
        distanceAsyncTask.execute("https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + latitude +
                "," + longitude +
                "&destinations=" + destLatitude +
                ",-" + destLongitude +
                "&mode=walking&units=imperial&key=AIzaSyDm7GmsBE4SIJojzIizbZ7sSEn_yqA0LyU");

        findViewById(R.id.streetViewButton).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(), view.getContentDescription(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        closeActivity = (Button) findViewById(R.id.closeActivity);
        closeActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        closeActivity.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(), view.getContentDescription(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        streetViewButton = (ImageButton) findViewById(R.id.streetViewButton);
        streetViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(BuildingDetailsActivity.this, StreetViewActivity.class);
                Bundle myBundle = new Bundle();
                myBundle.putString("destLatitude", destLatitude);
                myBundle.putString("destLongitude", destLongitude);
                myIntent.putExtras(myBundle);
                startActivity(myIntent);
            }
        });
        streetViewButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), v.getContentDescription(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Building building = (Building) getIntent().getParcelableExtra(MainActivity.PAR_KEY);

        buildingNameTV = (TextView) findViewById(R.id.buildingNameTV);
        addressTV = (TextView) findViewById(R.id.addressTV);
        distanceTV = (TextView) findViewById(R.id.distanceTV);
        timeTV = (TextView) findViewById(R.id.timeTV);
        buildingImage = (ImageView) findViewById(R.id.buildingImage);

        byte[] decodedString = Base64.decode(building.getImgString(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        buildingNameTV.setText(building.getBuildingName());
        addressTV.setText(building.getAddress());
        distanceTV.setText(building.getDistance());
        timeTV.setText(building.getTime());
        buildingImage.setImageBitmap(decodedByte);
    }

    public void updateDistanceUI(String distance, String time){
        distanceTV.setText(distance);
        timeTV.setText(time);
    }

    public class DistanceAsyncTask extends AsyncTask<String,Void,String> {
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
