package uk.co.doismellburning.marauder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.Date;
import java.util.List;

public class Marauder extends Activity {

	private WifiManager wifiManager;
	private WifiReceiver wifiReceiver;
	private TextView mainText;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);
		wifiManager.startScan();
		wifiReceiver = new WifiReceiver();
		mainText = (TextView) findViewById(R.id.mainText);
		registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		wifiManager.startScan();
		mainText.setText("Starting Scan...");
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh");
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		wifiManager.startScan();
		mainText.append("\n\nRefreshing...");
		return super.onMenuItemSelected(featureId, item);
	}

	protected void onPause() {
		unregisterReceiver(wifiReceiver);
		super.onPause();
	}

	protected void onResume() {
		registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	class WifiReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			StringBuilder sb = new StringBuilder();
			List<ScanResult> wifiList = wifiManager.getScanResults();
			JSONArray data = new JSONArray();
			for(int i = 0; i < wifiList.size(); i++){
				sb.append(new Integer(i+1).toString() + ".");
				ScanResult scanResult = wifiList.get(i);
				sb.append(scanResult.SSID + ": " + scanResult.level);
				sb.append("\n");
				data.put(scanResultToJSON(scanResult));
			}
			mainText.setText(sb);
			writeData(data);
		}
	}

	private void writeData(JSONArray data) {
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		Date d = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String name = df.format(d);
		File output = new File(path, name + ".json");

		try {
			if (!path.mkdirs()) {
				Log.e("marauder", "Could not make " + path);
				return; // TODO Better return type so we can like, alert users that their data is GOING INTO THE AETHER
			}

			OutputStream os = new FileOutputStream(output);
			Writer w = new OutputStreamWriter(os);
			w.write(data.toString());
			w.close();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public static JSONObject scanResultToJSON(ScanResult scanResult) {
		JSONObject o = new JSONObject();
		try {
			o.put("BSSID", scanResult.BSSID);
			o.put("capabilities", scanResult.capabilities);
			o.put("frequency", scanResult.frequency);
			o.put("level", scanResult.level);
			o.put("SSID", scanResult.SSID);
			// o.put("timestamp", scanResult.timestamp); // TODO Requires SDK > myphone
		} catch (JSONException e) {
			e.printStackTrace(); // Sigh
		}
		return o;
	}
}
