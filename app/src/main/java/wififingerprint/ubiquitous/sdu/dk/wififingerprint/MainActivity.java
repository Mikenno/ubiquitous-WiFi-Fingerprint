package wififingerprint.ubiquitous.sdu.dk.wififingerprint;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

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

			DataLogger fingerprintDataLogger = new DataLogger(this, "log-fingerprints");
			GPSManager gpsManager = new GPSManager(this, fingerprintDataLogger);
			setupButtons(gpsManager);

		} else {
			// test application ... presume it was an unintended mistake not to accept
			requestPermissions();
		}
	}

	private void setupButtons(final GPSManager gpsManager) {
		final Button btn_startCollectingWiFiFingerprints = findViewById(R.id.btn_start_collect_WiFi_fingerprints);
		final Button btn_stopCollectingWiFiFingerprints = findViewById(R.id.btn_stop_collect_WiFi_fingerprints);
		btn_stopCollectingWiFiFingerprints.setEnabled(false);

		btn_startCollectingWiFiFingerprints.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				gpsManager.startLocationRequest();
				btn_startCollectingWiFiFingerprints.setEnabled(false);
				btn_stopCollectingWiFiFingerprints.setEnabled(true);
			}
		});

		btn_stopCollectingWiFiFingerprints.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				gpsManager.stopLocationRequests();
				btn_startCollectingWiFiFingerprints.setEnabled(true);
				btn_stopCollectingWiFiFingerprints.setEnabled(false);
			}
		});
	}
}
