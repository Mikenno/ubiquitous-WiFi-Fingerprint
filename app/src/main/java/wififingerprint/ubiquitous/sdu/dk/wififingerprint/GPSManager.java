package wififingerprint.ubiquitous.sdu.dk.wififingerprint;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class GPSManager {
	private Context context;
	private DataLogger dataLogger;

	public GPSManager(Context context, DataLogger dataLogger) {
		this.context = context;
		this.dataLogger = dataLogger;
	}

	public void startLocationRequest() {
		FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

		LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(200);
		locationRequest.setFastestInterval(100);
		locationRequest.setMaxWaitTime(1000);
		locationRequest.setSmallestDisplacement(0);

		LocationCallback locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
				List<ScanResult> results =  wifiManager.getScanResults();

				WiFiFingerprint wiFiFingerprint = new WiFiFingerprint(locationResult, results);

				dataLogger.log(wiFiFingerprint.toString());
			}
		};
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
		}
	}
}
