package wififingerprint.ubiquitous.sdu.dk.wififingerprint;

import android.net.wifi.ScanResult;

import com.google.android.gms.location.LocationResult;

import java.util.List;
import java.util.Locale;

public class WiFiFingerprint {
	private LocationResult locationResult;
	private List<ScanResult> scanResults;

	public WiFiFingerprint(LocationResult locationResult, List<ScanResult> scanResults) {
		this.locationResult = locationResult;
		this.scanResults = scanResults;
	}

	private String getLoggableLocationResult(LocationResult locationResult) {
		return String.format(Locale.ENGLISH, "%f,%f,%f,%f",
				locationResult.getLastLocation().getAccuracy(),
				locationResult.getLastLocation().getLatitude(),
				locationResult.getLastLocation().getLongitude(),
				locationResult.getLastLocation().getAltitude());
	}

	private String getLoggableScanResults(List<ScanResult> scanResults) {
		StringBuilder sb = new StringBuilder();
		for (ScanResult sr : scanResults) {
			sb.append(String.format(Locale.ENGLISH, "%s,%d,", sr.BSSID, sr.level));
		}
		return sb.toString().substring(0, sb.length()-2);
	}

	public List<ScanResult> getScanResults() {
		return scanResults;
	}

	public LocationResult getLocationResult() {
		return locationResult;
	}

	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, "%s,%s", getLoggableLocationResult(locationResult), getLoggableScanResults(scanResults));
	}
}
