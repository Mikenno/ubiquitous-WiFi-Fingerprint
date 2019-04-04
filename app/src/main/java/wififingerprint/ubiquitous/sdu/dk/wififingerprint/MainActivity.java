package wififingerprint.ubiquitous.sdu.dk.wififingerprint;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

	private PrintWriter logger;
	private final String TAG = "TAG_ubi";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		requestPermissions();
	}

	private void requestPermissions() {
		ActivityCompat.requestPermissions(
				this,
				new String[] {
						Manifest.permission.WRITE_EXTERNAL_STORAGE,
						Manifest.permission.ACCESS_FINE_LOCATION,
						Manifest.permission.ACCESS_COARSE_LOCATION,
						Manifest.permission.CHANGE_WIFI_STATE
				},
				0);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {

			startLocationRequest();
		} else {
			// test application ... presume it was an unintended mistake not to accept
			requestPermissions();
		}
	}

	private void startLocationRequest() {
		FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(200);
		locationRequest.setFastestInterval(100);
		locationRequest.setMaxWaitTime(1000);
		locationRequest.setSmallestDisplacement(0);

		LocationCallback locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {

				WifiManager wifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
				List<ScanResult> results =  wifiManager.getScanResults();

				StringBuilder sb = new StringBuilder();
				for (ScanResult result : results) {
					sb.append(getLoggableScanResult(result));
				}

				log(String.format(Locale.ENGLISH, "%d, %s, %s", System.currentTimeMillis(), getLoggableLocationResult(locationResult), sb.toString()));
			}
		};

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
		}
	}

	private String getLoggableLocationResult(LocationResult locationResult) {
		return String.format(Locale.ENGLISH, "%f, %f, %f, %f",
				locationResult.getLastLocation().getAccuracy(),
				locationResult.getLastLocation().getLatitude(),
				locationResult.getLastLocation().getLongitude(),
				locationResult.getLastLocation().getAltitude());
	}

	private String getLoggableScanResult(ScanResult scanResult) {
		return String.format(Locale.ENGLISH, "%s, %d", scanResult.SSID, scanResult.level);
	}

	private void log(String message) {
		PrintWriter logger = getLogger();
		Log.d(TAG, message);
		if (logger != null) {
			logger.println(message);
			logger.flush();
		}
	}

	private PrintWriter getLogger() {
		if (logger != null)
			return logger;
		else {
			String basename = String.format(Locale.ENGLISH, "log-ubi-%d.csv", System.currentTimeMillis());

			final String dirname;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				dirname = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
			else
				dirname = getApplicationContext().getFilesDir().getAbsolutePath();

			File file = new File(dirname + File.separator + basename);

			try {
				logger = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}

			return logger;
		}

	}
}
