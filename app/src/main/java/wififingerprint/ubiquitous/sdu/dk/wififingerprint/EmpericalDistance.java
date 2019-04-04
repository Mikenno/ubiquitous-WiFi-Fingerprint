package wififingerprint.ubiquitous.sdu.dk.wififingerprint;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EmpericalDistance {
	private EmpericalDistance() {}

	/**
	 *
	 * @param scanResults New scanned wifi strengths
	 * @param wiFiFingerprints Previously scanned wifi fingerprints
	 * @return Sorted list of k nearest wifiFingerprints
	 */
	public static List<WiFiFingerprint> getKNearest(List<ScanResult> scanResults, List<WiFiFingerprint> wiFiFingerprints, int k) {
		Map<Double, WiFiFingerprint> resultErrors = new TreeMap<>();

		for (WiFiFingerprint wifiFingerprint : wiFiFingerprints) {
			double totalError = 0;
			int containedSSIDs = 0;
			List<ScanResult> printScanResults = wifiFingerprint.getScanResults();

			for (ScanResult sr : scanResults) {
				ScanResult wifiScanResult = containsSSID(sr.SSID, printScanResults);
				if(wifiScanResult != null) {
					containedSSIDs++;
					double distance = Math.pow((sr.level - wifiScanResult.level), 2);
					totalError+= distance;
				}
			}

			if (containedSSIDs != 0) {
				totalError = totalError/containedSSIDs;
				resultErrors.put(totalError, wifiFingerprint);
			}
		}

		List<WiFiFingerprint> resultList = new ArrayList<>();
		int count = 0;
		for (double key : resultErrors.keySet()) {
			if (count < k) {
				resultList.add(resultErrors.get(key));
			} else {
				// TODO: some error or warning ?
			}
			count++;
		}

		Log.d("TAG_ubi", resultErrors.keySet().toString());

		return resultList;
	}

	private static ScanResult containsSSID(String SSID, List<ScanResult> scanResultsList) {
		for (ScanResult sr : scanResultsList) {
			if (sr.SSID.equals(SSID))
				return sr;
		}
		return null;
	}

	public static Map<String, Double> getLocationPrediction(List<WiFiFingerprint> wiFiFingerprints) {
		//TODO: handle empty list...
		Map<String, Double> result = new HashMap<>();

		double latitude = 0;
		double longitude = 0;
		double altitude = 0;

		for (WiFiFingerprint wiFiFingerprint : wiFiFingerprints) {
			Location location = wiFiFingerprint.getLocationResult().getLastLocation();
			latitude += location.getLatitude();
			longitude += location.getLongitude();
			altitude += location.getAltitude();
		}

		latitude = latitude / wiFiFingerprints.size();
		longitude = longitude / wiFiFingerprints.size();
		altitude = altitude / wiFiFingerprints.size();

		result.put("latitude", latitude);
		result.put("longitude", longitude);
		result.put("altitude", altitude);
		return result;
	}
}
