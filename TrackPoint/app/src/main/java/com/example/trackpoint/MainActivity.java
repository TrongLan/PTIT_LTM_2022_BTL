package com.example.trackpoint;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
      if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        startLocationService();
      else
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }
  }

  public void turnOffServiceBtn(View view){
    stopLocationService();
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