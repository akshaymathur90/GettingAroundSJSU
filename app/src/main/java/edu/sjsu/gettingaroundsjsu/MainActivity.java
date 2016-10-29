package edu.sjsu.gettingaroundsjsu;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback,LocationListener {

    private final String TAG = getClass().getName().toString();
    public  final static String PAR_KEY = "edu.sjsu.objectPass.par";
    ImageView campusmap;
    ImageView king, engr, yoshihiro, studentunion, bbc, southparking;
    private final int REQUEST_LOCATION = 0;
    private GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    private View baseLayout;
    BuildingDatabase db;
    String latitude, longitude;
    String destLatitude, destLongitude;
    private RelativeLayout rl_Main;
    View v;
    LocationRequest mLocationRequest;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.main_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(getApplicationContext().SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG,"Listener query submit-->"+query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG,"Listener query text-->"+newText);
                if(newText.length()==0){
                    makeOthersInvisible();
                }
                return false;
            }
        });

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        baseLayout = findViewById(R.id.activity_main);
        db = new BuildingDatabase(this);

        campusmap = (ImageView) findViewById(R.id.campusmap);

        initializeAll();

        resetOnClickListners();

        buildGoogleApiClient();

        v = new MyView(getApplicationContext(),0,0);

        rl_Main = (RelativeLayout) findViewById(R.id.rl_bottom);
        v.setLayoutParams(rl_Main.getLayoutParams());

        rl_Main.addView(v);
        /*ImageView iv = (ImageView) findViewById(R.id.campusmap);
        iv.setVisibility(View.GONE);*/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btn_getlocation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
                mGoogleApiClient.connect();


            }
        });
        createLocationRequest();
    }

    private void requestLocationPermission() {
        Log.i(TAG, "Location permission has NOT been granted. Requesting permission.");

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying location permission rationale to provide additional context.");
            Snackbar.make(baseLayout, "App need needs to access location for navigation",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            Log.i(TAG, "Received response for contact permissions request.");

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(baseLayout, "Permissions Granted",
                        Snackbar.LENGTH_SHORT)
                        .show();
                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
                mGoogleApiClient.connect();
            } else {
                Log.i(TAG, "Location permissions were NOT granted.");
                Snackbar.make(baseLayout, "Permissions not Granted",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            mGoogleApiClient.disconnect();

        }



    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            requestLocationPermission();

            return;
        }else {
            Log.i(TAG,
                    "Location permission has already been granted. Displaying location");


        }
        startLocationUpdates();
        /*mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d(TAG,String.format("%s: %f", "Latitude:",
                    mLastLocation.getLatitude()));
            Log.d(TAG,String.format("%s: %f", "Longitude",
                    mLastLocation.getLongitude()));
            latitude = Double.valueOf(mLastLocation.getLatitude()).toString();
            longitude = Double.valueOf(mLastLocation.getLongitude()).toString();


            transformCoordinates(mLastLocation.getLatitude(),mLastLocation.getLongitude());

        } else {
            Log.e(TAG,"No location detected");
            Toast.makeText(this, "No location detected", Toast.LENGTH_LONG).show();
        }*/

    }
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()){
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
       // handleNewLocation(location);
        transformCoordinates(location.getLatitude(),location.getLongitude());
        latitude = Double.valueOf(location.getLatitude()).toString();
        longitude = Double.valueOf(location.getLongitude()).toString();
        Log.d(TAG,String.format("%s: %f", "Latitude:",
                location.getLatitude()));
        Log.d(TAG,String.format("%s: %f", "Longitude",
                location.getLongitude()));
    }

    public void transformCoordinates(Double lat, Double lng){

        float y = campusmap.getHeight();
        float x = campusmap.getWidth();
        Log.d(TAG,"IV height and width" +y+"---"+x);


        double finalLat = 37.338831;
        double baseLat  = 37.331596;

        double baseLong = -121.885989;  //small
        double finalLong = -121.876539; // big

        double percent_y = (lat - baseLat)/(finalLat-baseLat);
        double percent_x = (lng - baseLong)/(finalLong-baseLong);

        percent_y = 1-percent_y;
        //percent_x = 1-percent_x;

        double pix_x = percent_x * x;
        double pix_y = percent_y * y;

        Log.d(TAG,"percent x:->" + percent_x+ "y:->"+percent_y);

        Log.d(TAG,"x:->" + pix_x+ "y:->"+pix_y);

        //addMarker((float)pix_x-330,(float)pix_y-78);
        addMarker((float)pix_x,(float)pix_y);






    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    public void addMarker(float x , float y){
        rl_Main.removeView(v);

        v = new MyView(getApplicationContext(),x ,y);

        rl_Main = (RelativeLayout) findViewById(R.id.rl_bottom);
        v.setLayoutParams(rl_Main.getLayoutParams());

        rl_Main.addView(v);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    private void parcelableMethod(Building building) {
        Intent myIntent = new Intent(MainActivity.this, BuildingDetailsActivity.class);
        Bundle myBundle = new Bundle();
        myBundle.putParcelable(PAR_KEY, building);
        myBundle.putString("latitude", latitude);
        myBundle.putString("longitude", longitude);
        myBundle.putString("destLatitude", destLatitude);
        myBundle.putString("destLongitude", destLongitude);
        myIntent.putExtras(myBundle);
        startActivity(myIntent);
    }

    private byte[] getBytesFromBitmap(int king) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), king);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    public void onNewIntent(Intent intent) {
        Log.d(TAG,"new Intent called");
        setIntent(intent);
        handleIntent(intent);
    }
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query =
                    intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
    }

    private void doSearch(String queryStr) {
        // get a Cursor, prepare the ListAdapter
        // and set it
        Log.d(TAG,"the search query is-->" + queryStr);
        Cursor c = db.getWordMatches(queryStr, null);
        campusmap = (ImageView) findViewById(R.id.campusmap);

        if(c!=null && c.getCount()>0){
            Log.d(TAG,"Name-->"+c.getString(0));
            Log.d(TAG,"Address-->"+c.getString(1));
            Log.d(TAG,"Latitude-->"+c.getString(2));
            Log.d(TAG,"Longitude-->"+c.getString(3));
            destLatitude = c.getString(2);
            destLongitude = c.getString(3);
            switch(c.getString(0)) {
                case "King Library":
                    makeOthersInvisible();
                    //king.setImageResource(R.drawable.redpoint);
                    king.setBackgroundColor(Color.BLUE);
                    king.setAlpha(0.4f);
                    king.setPadding(30,30,30,30);
                    king.setFocusable(true);
                    break;
                case "Engineering Building":
                    makeOthersInvisible();
                    //engr.setImageResource(R.drawable.redpoint);
                    engr.setBackgroundColor(Color.BLUE);
                    engr.setAlpha(0.4f);
                    engr.setPadding(30,30,30,30);
                    engr.setFocusable(true);
                    break;
                case "Yoshihiro Uchida Hall":
                    makeOthersInvisible();
                    //yoshihiro.setImageResource(R.drawable.redpoint);
                    yoshihiro.setBackgroundColor(Color.BLUE);
                    yoshihiro.setAlpha(0.4f);
                    yoshihiro.setPadding(30,30,30,30);
                    yoshihiro.setFocusable(true);
                    break;
                case "Student Union":
                    makeOthersInvisible();
                    studentunion.setBackgroundColor(Color.BLUE);
                    studentunion.setAlpha(0.4f);
                    //studentunion.setImageResource(R.drawable.redpoint);
                    studentunion.setPadding(30,30,30,30);
                    studentunion.setFocusable(true);
                    break;
                case "BBC":
                    makeOthersInvisible();
                    //bbc.setImageResource(R.drawable.redpoint);
                    bbc.setBackgroundColor(Color.BLUE);
                    bbc.setAlpha(0.4f);
                    bbc.setPadding(30,30,30,30);
                    bbc.setFocusable(true);
                    break;
                case "South Parking Garage":
                    makeOthersInvisible();
                    //southparking.setImageResource(R.drawable.redpoint);
                    southparking.setBackgroundColor(Color.BLUE);
                    southparking.setAlpha(0.4f);
                    southparking.setPadding(30,30,30,30);
                    southparking.setFocusable(true);
                    break;
            }
        }
        else{
            Log.d(TAG,"No results found");
            makeOthersInvisible();
            resetOnClickListners();
            Toast.makeText(getApplicationContext(), "No Record Found", Toast.LENGTH_LONG).show();
        }
    }

    private void resetOnClickListners() {
        king.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imgString = Base64.encodeToString(getBytesFromBitmap(R.raw.king), Base64.NO_WRAP);
                Building building = new Building();
                building.setBuildingName("King Library");
                building.setAddress("Dr. Martin Luther King, Jr. Library, 150 East San Fernando Street, San Jose, CA 95112");
                building.setDistance("Calculating Distance...");
                building.setTime("Calculating Time...");
                building.setImgString(imgString);
                destLatitude = "37.336014";
                destLongitude = "121.885648";
                parcelableMethod(building);
            }
        });

        engr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imgString = Base64.encodeToString(getBytesFromBitmap(R.raw.engineering), Base64.NO_WRAP);
                Building building = new Building();
                building.setBuildingName("Engineering Building");
                building.setAddress("San José State University Charles W. Davidson College of Engineering, 1 Washington Square, San Jose, CA 95112");
                building.setDistance("Calculating Distance...");
                building.setTime("Calculating Time...");
                building.setImgString(imgString);
                destLatitude = "37.337618";
                destLongitude = "121.882243";
                parcelableMethod(building);
            }
        });

        yoshihiro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imgString = Base64.encodeToString(getBytesFromBitmap(R.raw.yoshihiro), Base64.NO_WRAP);
                Building building = new Building();
                building.setBuildingName("Yoshihiro Uchida Hall");
                building.setAddress("Yoshihiro Uchida Hall, San Jose, CA 95112");
                building.setDistance("Calculating Distance...");
                building.setTime("Calculating Time...");
                building.setImgString(imgString);
                destLatitude = "37.333447";
                destLongitude = "121.884240";
                parcelableMethod(building);
            }
        });

        studentunion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imgString = Base64.encodeToString(getBytesFromBitmap(R.raw.studentunion), Base64.NO_WRAP);
                Building building = new Building();
                building.setBuildingName("Student Union");
                building.setAddress("Student Union Building, San Jose, CA 95112");
                building.setDistance("Calculating Distance...");
                building.setTime("Calculating Time...");
                building.setImgString(imgString);
                destLatitude = "37.3363275";
                destLongitude = "121.8812869";
                parcelableMethod(building);
            }
        });

        bbc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imgString = Base64.encodeToString(getBytesFromBitmap(R.raw.bbc), Base64.NO_WRAP);
                Building building = new Building();
                building.setBuildingName("BBC");
                building.setAddress("Boccardo Business Complex, San Jose, CA 95112");
                building.setDistance("Calculating Distance...");
                building.setTime("Calculating Time...");
                building.setImgString(imgString);
                destLatitude = "37.336804";
                destLongitude = "121.878178";
                parcelableMethod(building);
            }
        });

        southparking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imgString = Base64.encodeToString(getBytesFromBitmap(R.raw.south), Base64.NO_WRAP);
                Building building = new Building();
                building.setBuildingName("South Parking Garage");
                building.setAddress("San Jose State University South Garage, 330 South 7th Street, San Jose, CA 95112");
                building.setDistance("Calculating Distance...");
                building.setTime("Calculating Time...");
                building.setImgString(imgString);
                destLatitude = "37.332636";
                destLongitude = "121.880560";
                parcelableMethod(building);
            }
        });
    }

    private void initializeAll() {
        king = (ImageView) findViewById(R.id.king);
        engr = (ImageView) findViewById(R.id.engr);
        yoshihiro = (ImageView) findViewById(R.id.yoshihiro);
        studentunion = (ImageView) findViewById(R.id.studentunion);
        bbc = (ImageView) findViewById(R.id.bbc);
        southparking = (ImageView) findViewById(R.id.southparking);
        king.setClickable(true);
        engr.setClickable(true);
        yoshihiro.setClickable(true);
        studentunion.setClickable(true);
        bbc.setClickable(true);
        southparking.setClickable(true);
    }

    private void makeOthersInvisible() {
        //king.setImageResource(R.drawable.transparent);
        king.setBackgroundColor(Color.TRANSPARENT);
        //engr.setImageResource(R.drawable.transparent);
        engr.setBackgroundColor(Color.TRANSPARENT);
        //studentunion.setImageResource(R.drawable.transparent);
        studentunion.setBackgroundColor(Color.TRANSPARENT);
        //yoshihiro.setImageResource(R.drawable.transparent);
        yoshihiro.setBackgroundColor(Color.TRANSPARENT);
        //bbc.setImageResource(R.drawable.transparent);
        bbc.setBackgroundColor(Color.TRANSPARENT);
        //southparking.setImageResource(R.drawable.transparent);
        southparking.setBackgroundColor(Color.TRANSPARENT);
    }

    class MyView extends View{


        Paint paint = new Paint();
        Point point = new Point();
        public MyView(Context context, float x, float y) {
            super(context);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(15);
            paint.setStyle(Paint.Style.FILL);
            point.x=x;
            point.y=y;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Bitmap b= BitmapFactory.decodeResource(getResources(), R.drawable.transparent);
            canvas.drawBitmap(b, 0, 0, paint);
            canvas.drawCircle(point.x, point.y, 20, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    point.x = event.getX();
                    point.y = event.getY();
                    Log.d(TAG,"points-->"+point.x+"- "+point.y);

            }
            invalidate();
            return true;

        }

        public void addMarker(int x, int y){

            point.x = x;
            point.y = y;
            invalidate();

        }

    }
    class Point {
        float x, y;
    }

}
