package com.example.trackpoint;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

  private TextView trackingServiceStatus;
  private TextView ipInput;
  private Button serviceEnable;
  private Button serviceUnenable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    trackingServiceStatus = (TextView) findViewById(R.id.trackingServiceStatus);
    ipInput = (TextView) findViewById(R.id.ipInput);
    serviceEnable = (Button) findViewById(R.id.enableTrackingService);
    serviceUnenable = (Button) findViewById(R.id.unenableTrackingService);
    serviceUnenable.setBackgroundColor(Color.RED);
    serviceEnable.setBackgroundColor(Color.GREEN);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
      if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
        trackingServiceStatus.setText("Service is enabled");
        serviceEnable.setBackgroundColor(Color.GRAY);
        startLocationService();
      }

      else
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }
  }

  public void turnOffServiceBtn(View view){
    trackingServiceStatus.setText("Service is unenabled");
    trackingServiceStatus.setTextColor(Color.RED);
    serviceUnenable.setBackgroundColor(Color.GRAY);
    serviceEnable.setBackgroundColor(Color.GREEN);
    stopLocationService();
  }

  public void turnOnServiceBtn(View view){
    if(!isLocationServiceRunning()){
      trackingServiceStatus.setText("Service is enabled");
      trackingServiceStatus.setTextColor(Color.GREEN);
      serviceEnable.setBackgroundColor(Color.GRAY);
      serviceUnenable.setBackgroundColor(Color.RED);
      startLocationService();
    }
  }

  public void serverConnectClick(View view){
    String ipAddress = (String) ipInput.getText();
  }

  private boolean isLocationServiceRunning() {
    ActivityManager activityManager =
            (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    if (activityManager != null) {
      for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)){
        if (ForegroundLocationService.class.getName().equals(service.service.getClassName())){
          if (service.foreground) {
            return true;
          }
        }
      }
      return false;
    }
    return false;
  }

  private void startLocationService() {
    if(!isLocationServiceRunning()){
      Intent intent = new Intent(getApplicationContext(), ForegroundLocationService.class);
      intent.setAction(Constant.ACTION_START_LOCATION_SERVICE);
      startService(intent);
      Toast.makeText(this," Location service started" , Toast.LENGTH_SHORT).show();
    }
  }
  private void stopLocationService() {
    if(isLocationServiceRunning()){
      Intent intent = new Intent(getApplicationContext(), ForegroundLocationService.class);
      intent.setAction(Constant.ACTION_STOP_LOCATION_SERVICE);
      startService(intent);
      Toast.makeText(this, "Location service stopped", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 1)
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
        startLocationService();
      else
        Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
  }
}