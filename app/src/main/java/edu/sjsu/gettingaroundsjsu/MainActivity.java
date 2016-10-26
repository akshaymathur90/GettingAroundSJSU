package edu.sjsu.gettingaroundsjsu;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
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

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    private final String TAG =getClass().getName().toString();
    public  final static String PAR_KEY = "edu.sjsu.objectPass.par";
    ImageView campusmap;
    ImageView king, engr, yoshihiro, studentunion, bbc, southparking;
//    ImageView kingpinpoint, engrpinpoint, yoshihiropinpoint, studentunionpinpoint, bbcpinpoint, southparkingpinpoint;
    BuildingDatabase db;

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
        db = new BuildingDatabase(this);

        initializeAll();

        resetOnClickListners();
    }

    private void parcelableMethod(Building building) {
        Intent myIntent = new Intent(MainActivity.this, BuildingDetailsActivity.class);
        Bundle myBundle = new Bundle();
        myBundle.putParcelable(PAR_KEY, building);
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
            Log.d(TAG,"Val-->"+c.getString(0));
            Log.d(TAG,"Val-->"+c.getString(1));
            Rect rectf;
            switch(c.getString(0)) {
                case "King Library":
                    makeOthersInvisible();
                    king.setVisibility(View.VISIBLE);
                    rectf = new Rect();
                    king.getLocalVisibleRect(rectf);
                    king.setX(rectf.width() - rectf.width()/4);
                    king.setY(rectf.height() + rectf.height()/2);
                    king.setImageResource(R.drawable.pinpoint);
                    king.setFocusable(true);
                    break;
                case "Engineering Building":
                    makeOthersInvisible();
                    engr.setVisibility(View.VISIBLE);
                    rectf = new Rect();
                    engr.getLocalVisibleRect(rectf);
                    engr.setY(engr.getY() - engr.getY()/4);
                    engr.setImageResource(R.drawable.pinpoint);
                    engr.setFocusable(true);
                    break;
                case "Yoshihiro Uchida Hall":
                    makeOthersInvisible();
                    yoshihiro.setVisibility(View.VISIBLE);
                    rectf = new Rect();
                    yoshihiro.getLocalVisibleRect(rectf);
                    yoshihiro.setImageResource(R.drawable.pinpoint);
                    yoshihiro.setFocusable(true);
                    break;
                case "Student Union":
                    makeOthersInvisible();
                    studentunion.setVisibility(View.VISIBLE);
                    rectf = new Rect();
                    studentunion.getLocalVisibleRect(rectf);
                    studentunion.setY(studentunion.getY() - studentunion.getY()/16);
                    studentunion.setImageResource(R.drawable.pinpoint);
                    studentunion.setFocusable(true);
                    break;
                case "BBC":
                    makeOthersInvisible();
                    bbc.setVisibility(View.VISIBLE);
                    rectf = new Rect();
                    bbc.getLocalVisibleRect(rectf);
                    bbc.setX(bbc.getX() + bbc.getX()/16);
                    bbc.setY(bbc.getY() - bbc.getY()/16);
                    bbc.setImageResource(R.drawable.pinpoint);
                    bbc.setFocusable(true);
                    break;
                case "South Parking Garage":
                    makeOthersInvisible();
                    southparking.setVisibility(View.VISIBLE);
                    rectf = new Rect();
                    southparking.getLocalVisibleRect(rectf);
                    southparking.setY(southparking.getY() - southparking.getY()/16);
                    southparking.setImageResource(R.drawable.pinpoint);
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
                building.setImgString(imgString);

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
                building.setImgString(imgString);

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
                building.setImgString(imgString);

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
                building.setImgString(imgString);

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
                building.setImgString(imgString);

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
                building.setImgString(imgString);

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
        king.setVisibility(View.INVISIBLE);
        engr.setVisibility(View.INVISIBLE);
        studentunion.setVisibility(View.INVISIBLE);
        bbc.setVisibility(View.INVISIBLE);
        yoshihiro.setVisibility(View.INVISIBLE);
        southparking.setVisibility(View.INVISIBLE);
    }

}
