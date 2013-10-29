package com.entscheidungsbaum.mobile.cellmonitor;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * @author marcus 
 * 
 */
public class CellMonitorScheduler extends IntentService implements
		CellDataResultReceiver.Receiver,  CellPhysicsReceiver.Receiver{

	private static final String LOG_TAG = CellMonitorScheduler.class.getName();

	private CellDataResultReceiver mCellDataResultReceiver;
	private CellPhysicsReceiver mCellPhysicsResultReceiver ;

	public CellMonitorScheduler() {
		super("SCHEDULAR");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onStart(Intent intent, int startId) {

		Log.d(LOG_TAG, " intent " + intent.getDataString() + " startId "
				+ startId);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "GSM environment oncreate service");

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d(LOG_TAG, "onStartCommand " + intent.getAction() + " flags = "
				+ flags + " startID " + startId);

		mCellDataResultReceiver = new CellDataResultReceiver(new Handler());
		mCellDataResultReceiver.setRecveiver(this);
		mCellPhysicsResultReceiver = new CellPhysicsReceiver(new Handler());
		mCellPhysicsResultReceiver.setReceiver(this);
		// intent = new Intent(Intent.ACTION_SYNC, null, this,
		// CellMonitorActivatorService.class);
		Intent cellMonitorActivatorService = new Intent(this,
				CellMonitorActivatorService.class);
		
		cellMonitorActivatorService.putExtra("receiver",
				mCellDataResultReceiver);
		
		cellMonitorActivatorService.putExtra("cellphysicsreceiver",mCellPhysicsResultReceiver);
				
		/**
		 * @TODO to be refactored
		 */
		Log.d(LOG_TAG,
				"onStart getDataString extra = "
						+ intent.getExtras()
								.getString(
										"com.entscheidungsbaum.mobile.cellmonitor.networktype")
						+ " startID " + startId);
		if (intent
				.getExtras()
				.getString(
						"com.entscheidungsbaum.mobile.cellmonitor.networktype")
				.equals("gsm")) {
			Log.d(LOG_TAG, "GSM environment starting service");

		}

		startService(cellMonitorActivatorService);

		// return START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		// Bundle cellBundle = intent.getExtras();

		Log.d(LOG_TAG, " cellBundle to String -> "
				+ intent.getExtras().toString());
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {

		 Log.d(LOG_TAG, " resultData = { " + resultData.toString()
		 + " } resultCode " + resultCode);
		
	}

}
