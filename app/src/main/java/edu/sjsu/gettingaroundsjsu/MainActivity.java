package edu.sjsu.gettingaroundsjsu;

import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

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

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        baseLayout = findViewById(R.id.activity_main);
        db = new BuildingDatabase(this);

        initializeAll();

        resetOnClickListners();

        buildGoogleApiClient();
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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d(TAG,String.format("%s: %f", "Latitude:",
                    mLastLocation.getLatitude()));
            Log.d(TAG,String.format("%s: %f", "Longitude",
                    mLastLocation.getLongitude()));
            latitude = Double.valueOf(mLastLocation.getLatitude()).toString();
            longitude = Double.valueOf(mLastLocation.getLongitude()).toString();
        } else {
            Log.e(TAG,"No location detected");
            Toast.makeText(this, "No location detected", Toast.LENGTH_LONG).show();
        }

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
                    king.setImageResource(R.drawable.redpoint);
                    king.setPadding(30,30,30,30);
                    king.setFocusable(true);
                    break;
                case "Engineering Building":
                    makeOthersInvisible();
                    engr.setImageResource(R.drawable.redpoint);
                    engr.setPadding(30,30,30,30);
                    engr.setFocusable(true);
                    break;
                case "Yoshihiro Uchida Hall":
                    makeOthersInvisible();
                    yoshihiro.setImageResource(R.drawable.redpoint);
                    yoshihiro.setPadding(30,30,30,30);
                    yoshihiro.setFocusable(true);
                    break;
                case "Student Union":
                    makeOthersInvisible();
                    studentunion.setImageResource(R.drawable.redpoint);
                    studentunion.setPadding(30,30,30,30);
                    studentunion.setFocusable(true);
                    break;
                case "BBC":
                    makeOthersInvisible();
                    bbc.setImageResource(R.drawable.redpoint);
                    bbc.setPadding(30,30,30,30);
                    bbc.setFocusable(true);
                    break;
                case "South Parking Garage":
                    makeOthersInvisible();
                    southparking.setImageResource(R.drawable.redpoint);
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
                building.setDistance("XX Miles");
                building.setTime("YY Minutes");
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
                building.setDistance("XX Miles");
                building.setTime("YY Minutes");
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
                building.setDistance("XX Miles");
                building.setTime("YY Minutes");
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
                building.setDistance("XX Miles");
                building.setTime("YY Minutes");
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
                building.setDistance("XX Miles");
                building.setTime("YY Minutes");
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
                building.setDistance("XX Miles");
                building.setTime("YY Minutes");
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
        king.setImageResource(R.drawable.transparent);
        engr.setImageResource(R.drawable.transparent);
        studentunion.setImageResource(R.drawable.transparent);
        yoshihiro.setImageResource(R.drawable.transparent);
        bbc.setImageResource(R.drawable.transparent);
        southparking.setImageResource(R.drawable.transparent);
    }

}
