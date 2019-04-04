package wififingerprint.ubiquitous.sdu.dk.wififingerprint;

import android.Manifest;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
						Manifest.permission.ACCESS_COARSE_LOCATION
				},
				0);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		// test application, presume granted

		// do stuff...
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
