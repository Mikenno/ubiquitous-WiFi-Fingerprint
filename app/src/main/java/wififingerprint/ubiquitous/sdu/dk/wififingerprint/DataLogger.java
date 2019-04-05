package wififingerprint.ubiquitous.sdu.dk.wififingerprint;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class DataLogger {
	private Context context;
	private String baseFilename;
	private PrintWriter logger;
	private final String TAG = "TAG_ubi";

	public DataLogger(Context context, String baseFilename) {
		this.context = context;
		this.baseFilename = baseFilename;
	}

	public void log(String message) {
		PrintWriter logger = getLogger();
		message = String.format(Locale.ENGLISH, "%d,%s", System.currentTimeMillis(), message);
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
			String basename = String.format(Locale.ENGLISH, "%s-%d.csv", baseFilename, System.currentTimeMillis());

			final String dirname;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				dirname = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
			else
				dirname = context.getFilesDir().getAbsolutePath();

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
