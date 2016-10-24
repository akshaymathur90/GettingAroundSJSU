package edu.sjsu.gettingaroundsjsu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    ImageView king, engr, yoshihiro, studentunion, bbc, southparking;
    public  final static String PAR_KEY = "edu.sjsu.objectPass.par";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        king.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "You pressed 1", Toast.LENGTH_SHORT).show();
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
//                Toast.makeText(MainActivity.this, "You pressed 2", Toast.LENGTH_SHORT).show();
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
//                Toast.makeText(MainActivity.this, "You pressed 3", Toast.LENGTH_SHORT).show();
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
//                Toast.makeText(MainActivity.this, "You pressed 4", Toast.LENGTH_SHORT).show();
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
//                Toast.makeText(MainActivity.this, "You pressed 5", Toast.LENGTH_SHORT).show();
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
//                Toast.makeText(MainActivity.this, "You pressed 6", Toast.LENGTH_SHORT).show();
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
}
