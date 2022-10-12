package com.example.trackpoint;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.StrictMode;
import com.google.android.gms.location.LocationRequest;
import android.os.Build;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class MainActivity extends AppCompatActivity {

  private TextView AddressText;
  private LocationRequest locationRequest;
  String geocode;



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);

    AddressText = findViewById(R.id.addressText);

    locationRequest = LocationRequest.create();
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    locationRequest.setInterval(5000);
    locationRequest.setFastestInterval(2000);

    // Tạo luồng mới để thực hiện gửi nhận dữ liệu qua tcp socket
    new Thread(new Runnable() {
      @Override
      public void run() {
        getCurrentLocation();
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        while(true){
          String message = geocode;
          try {
            Socket socket = new Socket("192.168.6.38", 2810);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF(message);
            outputStream.close();
            outputStream.flush();
            socket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }

      }
    }).start();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == 1){
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

        if (isGPSEnabled()) {

          getCurrentLocation();

        }else {

          turnOnGPS();
        }
      }
    }


  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 2) {
      if (resultCode == Activity.RESULT_OK) {
        getCurrentLocation();
      }
    }
  }

  private void getCurrentLocation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        if (isGPSEnabled()) {

          LocationServices.getFusedLocationProviderClient(MainActivity.this)
                  .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                      super.onLocationResult(locationResult);

                      LocationServices.getFusedLocationProviderClient(MainActivity.this)
                              .removeLocationUpdates(this);

                      if (locationResult != null && locationResult.getLocations().size() >0){

                        int index = locationResult.getLocations().size() - 1;
                        double latitude = locationResult.getLocations().get(index).getLatitude();
                        double longitude = locationResult.getLocations().get(index).getLongitude();
                        geocode = String.format("(%s, %s)", latitude, longitude);
//                        AddressText.setText(geocode);
                      }
                    }
                  }, Looper.getMainLooper());

        } else {
          turnOnGPS();
        }

      } else {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
      }
    }
  }

  private void turnOnGPS() {



    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest);
    builder.setAlwaysShow(true);

    Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
            .checkLocationSettings(builder.build());

    result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
      @Override
      public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

        try {
          LocationSettingsResponse response = task.getResult(ApiException.class);
          Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

        } catch (ApiException e) {

          switch (e.getStatusCode()) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

              try {
                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
              } catch (IntentSender.SendIntentException ex) {
                ex.printStackTrace();
              }
              break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
              //Device does not have location
              break;
          }
        }
      }
    });

  }

  private boolean isGPSEnabled() {
    LocationManager locationManager = null;
    boolean isEnabled = false;

    if (locationManager == null) {
      locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    return isEnabled;

  }
}