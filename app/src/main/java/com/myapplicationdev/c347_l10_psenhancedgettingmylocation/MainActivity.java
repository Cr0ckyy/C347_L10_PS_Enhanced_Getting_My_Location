package com.myapplicationdev.c347_l10_psenhancedgettingmylocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {

    Button btnStartDetector, btnStopDetector, btnRecords;
    TextView tvLatitude, tvLongitude;
    GoogleMap map;
    FusedLocationProviderClient client;
    LocationCallback mLocationCallback;
    String folderLocation;
    ToggleButton tbMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartDetector = findViewById(R.id.btnStartDetector);
        btnStopDetector = findViewById(R.id.btnStopDetector);
        btnRecords = findViewById(R.id.btnRecords);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tbMusic = findViewById(R.id.tbMusic);

        checkPermission();

        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);

        client = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        folderLocation = getFilesDir().getAbsolutePath() + "/Folder";
        File targetFile = new File(folderLocation, "data.txt");

        mLocationCallback = new LocationCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location data = locationResult.getLastLocation();
                    double lat = data.getLatitude();
                    double lng = data.getLongitude();

                    LatLng poi_currLocation = new LatLng(lat, lng);

                    assert mapFragment != null;
                    mapFragment.getMapAsync(googleMap -> {


                        map = googleMap;
                        tvLatitude.setText("Latitude: " + lat);
                        tvLongitude.setText("Longitude: " + lng);


                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi_currLocation, 15));
                        map.clear();

                        Marker currLocation = map.addMarker(new
                                MarkerOptions()
                                .position(poi_currLocation)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                        UiSettings ui = map.getUiSettings();
                        ui.setCompassEnabled(true);
                        ui.setZoomControlsEnabled(true);
                        ui.setMyLocationButtonEnabled(true);

                        File folder = new File(folderLocation);
                        if (!folder.exists()) {
                            boolean result = folder.mkdir();
                            if (result) {
                                Log.d("File Read/Write", "Folder created");
                            }
                        }
                        try {
                            FileWriter writer = new FileWriter(targetFile, true);
                            writer.write(lat + ", " + lng + "\n");
                            writer.flush();
                            writer.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    Toast.makeText(MainActivity.this, "New Location Detected\n" + "Lat: " + lat + ", " + " Lat: " + lng, Toast.LENGTH_SHORT).show();
                }
            }
        };

        btnStartDetector.setOnClickListener(view -> {
            checkPermission();

            LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setSmallestDisplacement(100);
            client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

            Intent i = new Intent(MainActivity.this, DetectorService.class);
            startService(i);
            Toast.makeText(MainActivity.this, "The detector has been activated.", Toast.LENGTH_SHORT).show();

        });

        btnStopDetector.setOnClickListener(view -> {
            client.removeLocationUpdates(mLocationCallback);
            map.clear();

            Intent i = new Intent(MainActivity.this, DetectorService.class);
            stopService(i);
            Toast.makeText(MainActivity.this, "The detector has been turned off.", Toast.LENGTH_SHORT).show();

        });

        btnRecords.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, RecordsActivity.class);
            startActivity(i);
        });

        tbMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int permissionCheck = PermissionChecker.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                Log.i("permission", "Permission not granted");
                return;
            }
            if (isChecked) {
                tbMusic.setTextOn("MUSIC OFF");
                startService(new Intent(MainActivity.this, MusicService.class));
            } else {
                tbMusic.setTextOff("MUSIC ON");
                stopService(new Intent(MainActivity.this, MusicService.class));
            }
        });
    }

    private void checkPermission() {
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            System.out.println("ACCESS_COARSE_LOCATION & ACCESS_FINE_LOCATION have been granted permission.");
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btnStartDetector.performClick();
                Toast.makeText(MainActivity.this, "The location update has begun.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "No permission has been granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}