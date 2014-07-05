package uk.co.doismellburning.marauder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.AbstractList;
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
		mainText.setText("Starting Scan");
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
			for(int i = 0; i < wifiList.size(); i++){
				sb.append(new Integer(i+1).toString() + ".");
				sb.append((wifiList.get(i)).toString());
				sb.append("\\n");
			}
			mainText.setText(sb);
		}
	}
}
