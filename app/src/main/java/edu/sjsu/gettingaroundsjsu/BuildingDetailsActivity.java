package edu.sjsu.gettingaroundsjsu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

/**
 * Created by dmodh on 10/23/16.
 */

public class BuildingDetailsActivity extends Activity {

    TableLayout tl;
    TableRow tr;
    TextView buildingNameTV, addressTV, distanceTV;
    ImageView buildingImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_building);

        Building building = (Building) getIntent().getParcelableExtra(MainActivity.PAR_KEY);

        buildingNameTV = (TextView) findViewById(R.id.buildingNameTV);
        addressTV = (TextView) findViewById(R.id.addressTV);
        distanceTV = (TextView) findViewById(R.id.distanceTV);
        buildingImage = (ImageView) findViewById(R.id.buildingImage);

        byte[] decodedString = Base64.decode(building.getImgString(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        buildingNameTV.setText(building.getBuildingName());
        addressTV.setText(building.getAddress());
        distanceTV.setText(building.getDistance());
        buildingImage.setImageBitmap(decodedByte);
    }

}
