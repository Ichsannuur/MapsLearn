package com.ics.mapslearn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;

public class PlaceAutoCompleteActivity extends AppCompatActivity {
    private TextView tvPickUpFrom, tvDestLocation;
    private TextView tvPickUpAddr, tvPickUpLatLng, tvPickUpName;
    private TextView tvDestLocAddr, tvDestLocLatLng, tvDestLocName;
    private static final int PICK_UP = 0;
    private static final int DEST_LOC = 1;
    private static int REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_auto_complete);
        wigetInit();

        tvPickUpFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAutoComplete(PICK_UP);
            }
        });

        tvDestLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAutoComplete(DEST_LOC);
            }
        });
    }

    // Method untuk Inisilisasi Widget agar lebih rapih
    private void wigetInit() {
        tvPickUpFrom = findViewById(R.id.tvPickUpFrom);
        tvDestLocation = findViewById(R.id.tvDestLocation);
        tvPickUpAddr = findViewById(R.id.tvPickUpAddr);
        tvPickUpLatLng = findViewById(R.id.tvPickUpLatLng);
        tvPickUpName = findViewById(R.id.tvPickUpName);
        tvDestLocAddr = findViewById(R.id.tvDestLocAddr);
        tvDestLocLatLng = findViewById(R.id.tvDestLocLatLng);
        tvDestLocName = findViewById(R.id.tvDestLocName);
    }

    private void showAutoComplete(int typeLocation){
        REQUEST_CODE = typeLocation;
        AutocompleteFilter typefilter = new AutocompleteFilter.Builder().setCountry("ID").build();
        try{
            Intent i = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typefilter).build(this);
            startActivityForResult(i, REQUEST_CODE);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Place placeData = PlaceAutocomplete.getPlace(this,data);
            if(placeData.isDataValid()){
                String placeAddress = placeData.getAddress().toString();
                LatLng placeLatLng = placeData.getLatLng();
                String placeName = placeData.getName().toString();

                switch (REQUEST_CODE){
                    case PICK_UP:
                        // Set ke widget lokasi asal
                        tvPickUpFrom.setText(placeAddress);
                        tvPickUpAddr.setText("Location Address : " + placeAddress);
                        tvPickUpLatLng.setText("LatLang : " + placeLatLng.toString());
                        tvPickUpName.setText("Place Name : " + placeName);
                        break;

                    case DEST_LOC:
                        // Set ke widget lokasi tujuan
                        tvDestLocation.setText(placeAddress);
                        tvDestLocAddr.setText("Destination Address : " + placeAddress);
                        tvDestLocLatLng.setText("LatLang : " + placeLatLng.toString());
                        tvDestLocName.setText("Place Name : " + placeName);
                        break;
                }
            }else{
                Toast.makeText(this, "Nama lokasi tidak ada", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
