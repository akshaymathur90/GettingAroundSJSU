package edu.sjsu.gettingaroundsjsu;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
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
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private final String TAG = getClass().getName().toString();
    private final int REQUEST_LOCATION = 0;

    private GoogleApiClient mGoogleApiClient;

    protected Location mLastLocation;

    private View baseLayout;
    private RelativeLayout rl_Main;

    BuildingDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        baseLayout = findViewById(R.id.activity_main);
        AppCompatButton streetView = (AppCompatButton) findViewById(R.id.btn_streetview);
        streetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startStreetView = new Intent(getApplicationContext(), StreetViewActivity.class);
                startActivity(startStreetView);
            }
        });

        AppCompatButton buildingDetails = (AppCompatButton) findViewById(R.id.btn_buildingdetail);
        buildingDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("pinonmap","starting pin point --**");
                Intent startBuildingDetails = new Intent(getApplicationContext(), PinPointTesting.class);
                startActivity(startBuildingDetails);

            }
        });


        db = new BuildingDatabase(this);
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
        } else {
            Log.e(TAG,"No location detected");
            Toast.makeText(this, "No location detected", Toast.LENGTH_LONG).show();
        }
    }

    public void currentLocationMarker(){
        rl_Main = (RelativeLayout) findViewById(R.id.relativelayout);
        View v = new MyView(getApplicationContext(),0,0);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(getApplicationContext().SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
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
        if(c!=null && c.getCount()>0){
            Log.d(TAG,"Val-->"+c.getString(0));
            Log.d(TAG,"Val-->"+c.getString(1));

        }
        else{
            Log.d(TAG,"No results found");
        }

    }


    class MyView extends View{


        Paint paint = new Paint();
        Point point = new Point();
        public MyView(Context context, int x, int y) {
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
            canvas.drawCircle(point.x, point.y, 50, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    point.x = event.getX();
                    point.y = event.getY();

            }
            invalidate();
            return true;

        }

    }
    class Point {
        float x, y;
    }


}
