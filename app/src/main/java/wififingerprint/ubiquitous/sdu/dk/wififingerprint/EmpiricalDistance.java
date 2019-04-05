package wififingerprint.ubiquitous.sdu.dk.wififingerprint;

import android.location.Location;
import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class EmpiricalDistance {
	private static EmpiricalDistance empiricalDistance = null;
	private DataLogger dataLogger;

	private EmpiricalDistance() {}

	public static EmpiricalDistance getEmpiricalDistance(DataLogger dataLogger) {
		if (empiricalDistance == null)
			empiricalDistance = new EmpiricalDistance();
		empiricalDistance.dataLogger = dataLogger;
		return empiricalDistance;
	}

	/**
	 *
	 * @param scanResults New scanned wifi strengths
	 * @param wiFiFingerprints Previously scanned wifi fingerprints
	 * @return Sorted list of k nearest wifiFingerprints
	 */
	public List<WiFiFingerprint> getKNearest(List<ScanResult> scanResults, List<WiFiFingerprint> wiFiFingerprints, int k) {
		Map<Double, WiFiFingerprint> resultErrors = new TreeMap<>();

		for (WiFiFingerprint wifiFingerprint : wiFiFingerprints) {
			double totalError = 0;
			int containedSSIDs = 0;
			List<ScanResult> printScanResults = wifiFingerprint.getScanResults();

			for (ScanResult sr : scanResults) {
				ScanResult wifiScanResult = containsSSID(sr.BSSID, printScanResults);
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

		StringBuilder sb = new StringBuilder();

		for (Double key : resultErrors.keySet()) {
			sb.append(key);
			sb.append(",");
			sb.append(resultErrors.get(key).toString());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.deleteCharAt(sb.length()-1);

		dataLogger.log(String.format(Locale.ENGLISH, "kNearest_sorted_map,%s", sb.toString()));

		String logResult = Arrays.deepToString(resultList.toArray());
		logResult = logResult.substring(1, logResult.length()-1);

		dataLogger.log(String.format(Locale.ENGLISH, "kNearest_result_list,%s", logResult));

		return resultList;
	}

	private ScanResult containsSSID(String SSID, List<ScanResult> scanResultsList) {
		for (ScanResult sr : scanResultsList) {
			if (sr.BSSID.equals(SSID))
				return sr;
		}
		return null;
	}

	public Map<String, Double> getLocationPrediction(List<WiFiFingerprint> wiFiFingerprints) {
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

		dataLogger.log(String.format(Locale.ENGLISH, "prediction_result,%s", getFormattedPredictionResult(result)));

		return result;
	}

	public String getFormattedPredictionResult(Map<String, Double> predictionResult) {
		return String.format(Locale.ENGLISH, "Latitude,%s,Longitude,%s,Altitude,%s", predictionResult.get("latitude"), predictionResult.get("longitude"), predictionResult.get("altitude"));
	}
}
