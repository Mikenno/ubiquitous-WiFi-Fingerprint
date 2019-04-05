package wififingerprint.ubiquitous.sdu.dk.wififingerprint;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public abstract class GPSManager {
	private Context context;

	private FusedLocationProviderClient fusedLocationClient;
	private LocationRequest locationRequest;
	private LocationCallback locationCallback;

	private boolean collecting;

	public GPSManager(Context context) {
		this.context = context;
		this.collecting = false;
		initialize();
	}
	private void initialize() {

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(200);
		locationRequest.setFastestInterval(100);
		locationRequest.setMaxWaitTime(1000);
		locationRequest.setSmallestDisplacement(0);

		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				locationCallbackFunctionality(locationResult);
			}
		};
	}

	public void collectWiFiFingerprint() {
		if (!collecting && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
			collecting = true;
		}
	}

	protected void stopLocationRequests() {
		fusedLocationClient.removeLocationUpdates(locationCallback);
		collecting = false;
	}

	protected abstract void locationCallbackFunctionality(LocationResult locationResult);
}
