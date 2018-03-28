package com.ics.mapslearn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.ics.mapslearn.network.APIServices;
import com.ics.mapslearn.network.RetrofitService;
import com.ics.mapslearn.response.Distance;
import com.ics.mapslearn.response.Duration;
import com.ics.mapslearn.response.LegsItem;
import com.ics.mapslearn.response.Response;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private GoogleMap mMap;
    private String API_KEY = "AIzaSyAOz2HtSEYQHcjkqsuHKkuvi_vMAyx3KuA";
    private LatLng pickUpLatLng = null;
    private LatLng locationLatLng = null;
    private TextView tvStartAddress, tvEndAddress, tvDistance;
    private LinearLayout infoPanel;
    public static final int PICK_UP = 0;
    public static final int DROP = 1;
    private static int REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        widgetInit();

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        tvStartAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlaceAutoComplete(PICK_UP);
            }
        });

        tvEndAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlaceAutoComplete(DROP);
            }
        });

    }

    private void showPlaceAutoComplete(int typeLocation) {
        REQUEST_CODE = typeLocation;
        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder().setCountry("ID").build();
        try{
            Intent i = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(autocompleteFilter).build(this);
            startActivityForResult(i,REQUEST_CODE);
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
            Place place = PlaceAutocomplete.getPlace(this,data);
            if(place.isDataValid()){
                LatLng placeLatLng = place.getLatLng();
                String address = place.getAddress().toString();

                switch (REQUEST_CODE){
                    case PICK_UP:
                        tvStartAddress.setText(address);
                        pickUpLatLng = place.getLatLng();
                        break;
                    case DROP:
                        tvEndAddress.setText(address);
                        locationLatLng = place.getLatLng();
                        break;
                }
                if(pickUpLatLng != null && locationLatLng != null){
                    actionRoute(pickUpLatLng,locationLatLng);
                }
            }
        }
    }

    private void actionRoute(LatLng pick,LatLng location) {
        String lokasiAwal = pick.latitude + "," + pick.longitude;
        String lokasiAkhir = location.latitude + "," + location.longitude;
        // Clear dulu Map nya
        mMap.clear();
        APIServices api = RetrofitService.service().create(APIServices.class);
        Call<Response> routeRespone = api.request_route(lokasiAwal,lokasiAkhir,API_KEY);
        routeRespone.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if(response.isSuccessful()){
                    Response dataDirection = response.body();
                    LegsItem legsItem = dataDirection.getRoutes().get(0).getLegs().get(0);

                    //Garis polyline
                    String polylinePoint = dataDirection.getRoutes().get(0).getOverviewPolyline().getPoints();

                    //Decode
                    List<LatLng> decodePath = PolyUtil.decode(polylinePoint);

                    //Gambar Garis ke Google Maps
                    mMap.addPolyline(new PolylineOptions().addAll(decodePath).width(8f).color(Color.argb(255,56,167,252))).setGeodesic(true);

                    //Tambah Marker
                    mMap.addMarker(new MarkerOptions().position(pickUpLatLng).title("Lokasi Awal"));
                    mMap.addMarker(new MarkerOptions().position(locationLatLng).title("Lokasi Tujuan"));

                    //Dapatkan jarak dan waktu
                    Distance distance = legsItem.getDistance();
                    Duration duration = legsItem.getDuration();

                    tvDistance.setText(distance.getText().toString());

                    //UNTUK MEMBUAT LAYAR DI TENGAH KETIKA MAP JALAN
                    LatLngBounds.Builder latBuilder = new LatLngBounds.Builder();
                    latBuilder.include(pickUpLatLng);
                    latBuilder.include(locationLatLng);

                    //Bounds coordinate
                    LatLngBounds bounds = latBuilder.build();
                    int width = getResources().getDisplayMetrics().widthPixels;
                    int height = getResources().getDisplayMetrics().heightPixels;
                    int padding = (int) (width * 0.2); // jarak
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,width,height,padding);
                    mMap.animateCamera(cu);
                    //END
                    infoPanel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void widgetInit() {
        tvStartAddress = findViewById(R.id.tvPickUpFrom);
        tvEndAddress = findViewById(R.id.tvDestLocation);
        tvDistance = findViewById(R.id.tvDistance);
        infoPanel = findViewById(R.id.infoPanel);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(10, 180, 10, 10);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
