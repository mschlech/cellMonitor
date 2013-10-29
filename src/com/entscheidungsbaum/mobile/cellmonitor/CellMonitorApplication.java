package com.entscheidungsbaum.mobile.cellmonitor;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * @author marcus@entscheidungsbaum.com
 * 
 */
public class CellMonitorApplication extends Activity {

	private final static String LOG_TAG = CellMonitorApplication.class
			.getCanonicalName();

	private TelephonyManager tm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(LOG_TAG, "Started cellMonitor");
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		String mTextStr = ("DEVICE INFO\n\n" + "SDK: `"
				+ Build.VERSION.SDK_INT
				+ "`\nCODENAME: `"
				+ Build.VERSION.CODENAME
				+ "`\nRELEASE: `"
				+ Build.VERSION.RELEASE
				+ "`\nDevice: `"
				+ Build.DEVICE
				+ "`\nHARDWARE: `"
				+ Build.HARDWARE
				+ "`\nMANUFACTURER: `"
				+ Build.MANUFACTURER
				+ "`\nMODEL: `"
				+ Build.MODEL
				+ "`\nPRODUCT: `"
				+ Build.PRODUCT
				+ ((getRadio() == null) ? "" : ("`\nRADIO: `" + getRadio()))
				+ "`\nBRAND: `"
				+ Build.BRAND
				+ ((Build.VERSION.SDK_INT >= 8) ? ("`\nBOOTLOADER: `" + Build.BOOTLOADER)
						: "") + "`\nBOARD: `" + Build.BOARD + "`\nID: `"
				+ Build.ID + "`\n\n"

		);

		IntentFilter i = new IntentFilter();
		i.addAction("com.entscheidungsbaum.mobile.cellmonitor");

		Intent cellMonitorIntent = new Intent(this, CellMonitorScheduler.class);
	//	Intent cellMonitorIntent = new Intent(this, CellMonitorActivatorService.class);
		
		cellMonitorIntent.setAction("com.entscheidungsbaum.mobile.cellmonitor");
		cellMonitorIntent.putExtra(
				"com.entscheidungsbaum.mobile.cellmonitor.networktype", "gsm");
		// getUpEnvironmentVar());

		Log.d(LOG_TAG, "starting Cell Service timer task for "
				+ cellMonitorIntent.getExtras());
		startService(cellMonitorIntent);

	}

	@Override
	protected void onResume() {
		Log.d(LOG_TAG, "onResume");

		super.onResume();
	}

	/**
	 * @TODO make a parcleable out of it, to be used on future purposes
	 * @return some object
	 */
	private Bundle getUpEnvironmentVar() {

		StringBuffer subInfo = new StringBuffer();
		ArrayList<String> gsmSetupArray = new ArrayList<String>();

		subInfo.append("{");
		subInfo.append("phoneType=");
		subInfo.append(tm.getPhoneType() == tm.PHONE_TYPE_GSM ? "gsm" : "cdma");
		subInfo.append(",");
		subInfo.append("deviceId=");
		// String networkType = "gsm";
		subInfo.append(tm.getDeviceId());
		subInfo.append(",");
		subInfo.append("deviceSoftwareVersion=");
		subInfo.append(tm.getDeviceSoftwareVersion());
		subInfo.append(",");
		subInfo.append("IMSI=");
		subInfo.append(tm.getSubscriberId());

		subInfo.append("}");

		Log.d(LOG_TAG, " Subscriber Info = " + subInfo);
		gsmSetupArray.add(subInfo.toString());

		Bundle initialSetupBundle = new Bundle();
		initialSetupBundle.putStringArrayList("gsmSetupArray", gsmSetupArray);
		return initialSetupBundle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.entscheidungsbaum.mobile.cellmonitor.CellDataResultReceiver.Receiver
	 * #onReceiveResult(int, android.os.Bundle)
	 */
//	@Override
//	public void onReceiveResult(int resultCode, Bundle resultData) {
//
//		Log.d(LOG_TAG, " resultData = { " + resultData + " } resultCode "
//				+ resultCode);
//
//	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static final String getRadio() {
		if (Build.VERSION.SDK_INT >= 8 && Build.VERSION.SDK_INT < 14)
			return Build.RADIO;
		else if (Build.VERSION.SDK_INT >= 14)
			return Build.getRadioVersion();
		else
			return null;
	}

}
