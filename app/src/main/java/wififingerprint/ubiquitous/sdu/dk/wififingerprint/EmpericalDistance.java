package wififingerprint.ubiquitous.sdu.dk.wififingerprint;

import android.net.wifi.ScanResult;

import com.google.android.gms.location.LocationResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmpericalDistance {
	private EmpericalDistance() {}

	/**
	 *
	 * @param results New scanned wifi strengths
	 * @param wiFiFingerprints Previously scanned wifi fingerprints
	 * @return Sorted list of k nearest wifiFingerprints
	 */
	public static List<WiFiFingerprint> getkNearest(List<ScanResult> results, List<WiFiFingerprint> wiFiFingerprints, int k) {
		// TODO: implement distance measure and return actual result ...
		return wiFiFingerprints;
	}

	public static Map<String, Double> getLocationPrediction(List<WiFiFingerprint> wiFiFingerprints) {
		//TODO: return the average between all locations
		Map<String, Double> result = new HashMap<>();
		LocationResult locationResult = wiFiFingerprints.get(wiFiFingerprints.size()-1).getLocationResult();
		result.put("lati", locationResult.getLastLocation().getLatitude());
		result.put("long", locationResult.getLastLocation().getLongitude());
		result.put("alti", locationResult.getLastLocation().getAltitude());
		return result;
	}
}
