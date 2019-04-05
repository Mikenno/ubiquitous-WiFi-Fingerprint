package wififingerprint.ubiquitous.sdu.dk.wififingerprint;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.LocationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
	WifiManager wifiManager;

	DataLogger predictionResultsLogger;
	DataLogger fingerprintDataLogger;
	DataLogger empiricalDataLogger;

	List<WiFiFingerprint> wiFiFingerprints;

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

			final TextView receivedFingerPrint = findViewById(R.id.textView_collected_fingerprint);

			predictionResultsLogger = new DataLogger(this, "log-predictionResults");
			fingerprintDataLogger = new DataLogger(this, "log-fingerprints");
			empiricalDataLogger = new DataLogger(this, "log-empirical");
			wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			wiFiFingerprints = new ArrayList<>();

			GPSManager gpsManager = new GPSManager(this) {
				@Override
				public void locationCallbackFunctionality(LocationResult locationResult) {
					List<ScanResult> results =  wifiManager.getScanResults();

					WiFiFingerprint wiFiFingerprint = new WiFiFingerprint(locationResult, results);
					wiFiFingerprints.add(wiFiFingerprint);

					fingerprintDataLogger.log(wiFiFingerprint.toString());

					receivedFingerPrint.setText(String.format(Locale.ENGLISH, "New WiFiFingerprint - Total=%d", wiFiFingerprints.size()));

					stopLocationRequests();
				}
			};

			setupButtons(gpsManager);
		} else {
			// test application ... presume it was an unintended mistake not to accept
			requestPermissions();
		}
	}

	private void setupButtons(final GPSManager gpsManager) {
		final Button btn_startCollectingWiFiFingerprints = findViewById(R.id.btn_collect_WiFi_fingerprint);
		final Button btn_predictPosition = findViewById(R.id.btn_predict_position);
		final TextView textView_predictedLocation = findViewById(R.id.textView_predicted_location);
		final TextView receivedFingerPrint = findViewById(R.id.textView_collected_fingerprint);

		btn_startCollectingWiFiFingerprints.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				receivedFingerPrint.setText("Collecting new WiFiFingerprint...");
				gpsManager.collectWiFiFingerprint();
			}
		});

		btn_predictPosition.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WifiManager wifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

				List<ScanResult> results =  wifiManager.getScanResults();
				List<WiFiFingerprint> kNearest = EmpiricalDistance.getEmpiricalDistance(empiricalDataLogger).getKNearest(results, wiFiFingerprints, 3);
				Map<String, Double> predictedLocation = EmpiricalDistance.getEmpiricalDistance(empiricalDataLogger).getLocationPrediction(kNearest);
				String predictions = predictedLocation.values().toString();
				predictions = predictions.substring(1, predictions.length()-1);

				predictionResultsLogger.log(predictions);

				textView_predictedLocation.setText(EmpiricalDistance.getEmpiricalDistance(empiricalDataLogger).getFormattedPredictionResult(predictedLocation));
			}
		});
	}
}
