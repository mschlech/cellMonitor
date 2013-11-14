package com.entscheidungsbaum.mobile.cellmonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

/**
 * 
 * @author marcus@entscheidungsbaum.com seperated from the worker thread
 * 
 */
public class CellMonitorActivatorService extends IntentService implements
		LocationListener {

	/**
	 * members
	 */
	private String LOG_TAG = CellMonitorActivatorService.class.getName();
	private boolean isStarted = false;

	/**
	 * a first cell location
	 */
	GsmCellLocation gcl;

	/**
	 * the bundle for data transfer
	 */
	Bundle cellBundle = new Bundle();
	Bundle cellPhysicsBundle = new Bundle();

	Bundle subscriberBundle = new Bundle();
	// Intent cellMonitorIntent;

	/**
	 * location listener setup sequence
	 */
	private LocationManager lm;

	private String mProvider;

	private SignalStrength mSignalStrength;

	private boolean mDone = false;

	private int mPingFlag;

	private AsyncTask<String, Void, HashMap<String, Long>> httpDownloadTest;

	/*
	 * fields to set from the phoneStateListener
	 */
	private static final int INFO_SERVICE_STATE_INDEX = 0;
	private static final int INFO_CELL_LOCATION_INDEX = 1;
	private static final int INFO_CALL_STATE_INDEX = 2;
	private static final int INFO_CONNECTION_STATE_INDEX = 3;
	private static final int INFO_SIGNAL_LEVEL_INDEX = 4;
	private static final int INFO_SIGNAL_LEVEL_INFO_INDEX = 5;
	private static final int INFO_DATA_DIRECTION_INDEX = 6;
	private static final int INFO_DEVICE_INFO_INDEX = 7;

	private static final int STATUS_RUNNING = 0;
	private static final int STATUS_FINISHED = 1;
	private static final int STATUS_ERROR = 3;
	private static final int STATUS_SIGNAL_CHANGED = 4;
	private static final int STATUS_ONCALLFORWARDING_CHANGED = 5;

	protected static final int CELLPHYSICS = 0;
	protected static final int GENERICCELLINFO = 1;

	ArrayList<? extends Parcelable> results;

	private ResultReceiver receiver = null;
	private ResultReceiver mCellPhysicsResultReceiver = null;

	public CellMonitorActivatorService() {
		super("CellMonitorActivatorService");
		// Intent cellMonitorIntent = new
		// Intent(this,CellMonitorActivatorService.class);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LOG_TAG, "onStartCommand " + intent.getExtras().toString());

		Log.d(LOG_TAG,
				"onStartCommand " + intent.getParcelableExtra("receiver")
						+ " action= " + intent.getAction() + " startID = "
						+ startId);

		// old
		receiver = intent.getParcelableExtra("receiver");
		mCellPhysicsResultReceiver = intent
				.getParcelableExtra("cellphysicsreceiver");
		

		String command = intent.getStringExtra("command");
		Log.d(LOG_TAG, "Command " + command);

		Criteria criteria = new Criteria();
		LocationManager locationManager = (LocationManager) this
				.getSystemService(this.getApplication().LOCATION_SERVICE);

		mProvider = locationManager.getBestProvider(criteria, false);
		Log.d(LOG_TAG, "mProvider " + mProvider);

		Location location = locationManager.getLastKnownLocation(mProvider);

		if (location != null) {
			Log.d(LOG_TAG, "Location mProvider " + mProvider
					+ " has been selected.");
			onLocationChanged(location);
		} else {
			Log.d(LOG_TAG, "no location update available ");

		}

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * @TODO refactor a bit smarter
	 * @param resultCode
	 * @param aBundle
	 */
	private void sendToReceiver(int resultCode, Bundle aBundle) {

		Log.d(LOG_TAG, " send To Receiver -> " + aBundle.toString()
				+ " receiver -> " + receiver.toString()
				+ " aBundle from PackageName " + this.getPackageName());

		if (cellBundle != null && cellPhysicsBundle != null) {

			receiver.send(resultCode, cellBundle);
			mCellPhysicsResultReceiver.send(resultCode, cellPhysicsBundle);
			mPingFlag = mPingFlag + 1;
		} else {

			Log.d(LOG_TAG, " nothing to send to the receiver null");
			receiver.send(0, new Bundle());
			mCellPhysicsResultReceiver.send(0, new Bundle());
			mPingFlag = 0;
		}

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(LOG_TAG,
				" onHandleIntent getDataString -> " + intent.getDataString());

		Log.d(LOG_TAG, "onHandleIntent -> " + intent);

	}

	@Override
	public void onCreate() {
		Log.i(LOG_TAG, "Started doing stuff periodically "
				+ this.getApplicationContext().toString());

		startPhoneStateListnerV9();

	}

	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(LOG_TAG, "onStart intent " + intent + " startID ->  " + startid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContextWrapper#stopService(android.content.Intent)
	 */
	@Override
	public boolean stopService(Intent name) {
		Log.d(LOG_TAG, "stopping service");
		return super.stopService(name);
	}

	/**
	 * @TODO refactoring
	 * 
	 *       start the PhoneStateListener for Version9
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void startPhoneStateListnerV9() {
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Log.d(LOG_TAG, " Telephony Manager -> " + tm);
		int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
				| PhoneStateListener.LISTEN_DATA_ACTIVITY
				| PhoneStateListener.LISTEN_CELL_LOCATION
				| PhoneStateListener.LISTEN_CALL_STATE
				| PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
				| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
				| PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
				| PhoneStateListener.LISTEN_SERVICE_STATE;

		if (tm.getCellLocation() == null) {
			Log.d(LOG_TAG, " request new Location ");
			GsmCellLocation.requestLocationUpdate();

		} else {

			gcl = (GsmCellLocation) tm.getCellLocation();
			Log.d(LOG_TAG, " available location " + gcl);

		}

		int[] gclInteger = { gcl.getCid(), gcl.getLac(), gcl.getPsc() };
		Log.d(LOG_TAG, "startPhoneStateListnerV9 Neighbour Cell -> "
				+ gclInteger);

		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkingInfo = cm.getActiveNetworkInfo();

		DetailedState detailedState = activeNetworkingInfo.getDetailedState();
		// further info to put into the bundle follows here ...
		// @TODO

		List<NeighboringCellInfo> cellneighbor = tm.getNeighboringCellInfo();
		for (NeighboringCellInfo neighboringCellInfo : cellneighbor) {
			Log.d(LOG_TAG,
					" Neighboring CellInfo parts "
							+ neighboringCellInfo.getCid() + " "
							+ neighboringCellInfo.getLac() + "  "
							+ neighboringCellInfo.getNetworkType() + " "
							+ neighboringCellInfo.getPsc() + " "
							+ neighboringCellInfo);

			Log.d(LOG_TAG, " Neighboring CellInfo toString = "
					+ neighboringCellInfo.toString());
			cellPhysicsBundle.putString("neighboringCell",
					neighboringCellInfo.toString());

		}

		int networkType = tm.getNetworkType();
		cellBundle.putIntArray("gsmCellLocation", gclInteger);
		cellBundle.putString("imsi", tm.getSubscriberId());
		cellBundle.putString("operator", tm.getNetworkOperator());
		cellBundle.putInt("networkType", networkType);
		cellPhysicsBundle.putIntArray("gsmCellLocation", gclInteger);
		cellPhysicsBundle.putString("imsi", tm.getSubscriberId());
		cellPhysicsBundle.putString("operator", tm.getNetworkOperator());
		cellPhysicsBundle.putInt("networkType", networkType);

		tm.listen(phoneStateListener, events);
	}

	/**
	 * @TODO refactoring
	 * @return an instance of TelephonManager
	 */
	private TelephonyManager getPhoneManager() {
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Log.d(LOG_TAG, " " + tm);
		return tm;
	}

	/**
	 * telephony manager to get the network data
	 */
	private void getNetworkingActivities() {
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

		GsmCellLocation gcLocation = (GsmCellLocation) tm.getCellLocation();
		Log.i(LOG_TAG, "gcLocation is [ " + gcLocation.toString() + " ] ");

	}

	/**
	 * IBinder
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(LOG_TAG, " IBINDER  intent " + intent);
		return null;
		// TODO: Return the communication channel to the service.
	}

	/**
	 * helper
	 */
	private final PhoneStateListener phoneStateListener = new PhoneStateListener() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.telephony.PhoneStateListener#onDataConnectionStateChanged
		 * (int, int)
		 */
		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {
			// TODO Auto-generated method stub
			Log.d(LOG_TAG, "onDataConnectionChanged state " + state
					+ " networktype " + networkType);
			cellBundle.putInt("networkType", networkType);
			// sendToReceiver(state, cellBundle);
			super.onDataConnectionStateChanged(state, networkType);
		}

		@Override
		public void onCallForwardingIndicatorChanged(boolean cfi) {
			Log.i(LOG_TAG, "onCallForwardingIndicatorChanged " + cfi);
			cellBundle.putBoolean("forwardingIndicator", cfi);
			// sendToReceiver(STATUS_ONCALLFORWARDING_CHANGED, cellBundle);

			super.onCallForwardingIndicatorChanged(cfi);
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			String callState = "UNKNOWN";

			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				callState = "IDLE";
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				callState = "Ringing (" + incomingNumber + ")";
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				callState = "Offhook";
				break;
			}

			Log.i(LOG_TAG, "onCallStateChanged " + callState);
			cellBundle.putString("onCallStateChanged", callState);

			// sendToReceiver(state, cellBundle);

			super.onCallStateChanged(state, incomingNumber);
		}

		@Override
		public void onCellLocationChanged(CellLocation location) {
			String locationString = location.toString();

			Log.i(LOG_TAG, "onCellLocationChanged " + locationString);

			cellBundle.putString("locationString", locationString);
			cellPhysicsBundle.putString("locationString", locationString);

			// sendToReceiver(INFO_CALL_STATE_INDEX, cellBundle);
			super.onCellLocationChanged(location);
		}

		@Override
		public void onDataActivity(int direction) {
			String directionString = "none";

			switch (direction) {
			case TelephonyManager.DATA_ACTIVITY_IN:
				directionString = "IN";
				break;
			case TelephonyManager.DATA_ACTIVITY_OUT:
				directionString = "OUT";
				break;
			case TelephonyManager.DATA_ACTIVITY_INOUT:
				directionString = "INOUT";
				break;
			case TelephonyManager.DATA_ACTIVITY_NONE:
				directionString = "NONE";
				break;
			default:
				directionString = "UNKNOWN: " + direction;
				break;
			}

			// setDataDirection(jsonCellIds[INFO_DATA_DIRECTION_INDEX],direction);

			Log.i(LOG_TAG, "onDataActivity " + directionString);

			cellBundle.putString("dataActivity ", directionString);
			// sendToReceiver(INFO_CALL_STATE_INDEX, cellBundle);
			super.onDataActivity(direction);
		}

		@Override
		public void onDataConnectionStateChanged(int state) {
			String connectionState = "Unknown";

			switch (state) {
			case TelephonyManager.DATA_CONNECTED:
				connectionState = "Connected";
				Log.d(LOG_TAG,
						"data Connected in onDataConnectionStateChanged="
								+ connectionState);
				break;
			case TelephonyManager.DATA_CONNECTING:
				connectionState = "Connecting";
				break;
			case TelephonyManager.DATA_DISCONNECTED:
				connectionState = "Disconnected";
				break;
			case TelephonyManager.DATA_SUSPENDED:
				connectionState = "Suspended";
				break;
			default:
				connectionState = "Unknown: " + state;
				break;
			}

			Log.i(LOG_TAG, "onDataConnectionStateChanged " + connectionState);

			cellBundle.putString("onDataConnectionStateChanged ",
					connectionState);
			// sendToReceiver(INFO_CALL_STATE_INDEX, cellBundle);

			super.onDataConnectionStateChanged(state);
		}

		@Override
		public void onMessageWaitingIndicatorChanged(boolean mwi) {
			Log.i(LOG_TAG, "onMessageWaitingIndicatorChanged " + mwi);

			super.onMessageWaitingIndicatorChanged(mwi);
		}

		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			String serviceStateString = "UNKNOWN";

			switch (serviceState.getState()) {
			case ServiceState.STATE_IN_SERVICE:
				serviceStateString = "IN SERVICE";
				break;
			case ServiceState.STATE_EMERGENCY_ONLY:
				serviceStateString = "EMERGENCY ONLY";
				break;
			case ServiceState.STATE_OUT_OF_SERVICE:
				serviceStateString = "OUT OF SERVICE";
				break;
			case ServiceState.STATE_POWER_OFF:
				serviceStateString = "POWER OFF";
				break;
			default:
				serviceStateString = "UNKNOWN";
				break;
			}

			Log.d(LOG_TAG, "onServiceStateChanged " + serviceStateString);

			// cellBundle.putString("serviceStateString", serviceStateString);
			// sendToReceiver(INFO_CALL_STATE_INDEX, cellBundle);

			super.onServiceStateChanged(serviceState);
		}

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			Log.i(LOG_TAG,
					"onSignalStrengthChanged signalstrength method argument => "
							+ signalStrength);
			mSignalStrength = signalStrength;
			// Log.d(LOG_TAG,
			// " SIGNALSTRENGTH CDMAdbm = " + signalStrength.getCdmaDbm()
			// + " EVDOdbm = " + signalStrength.getEvdoDbm()
			// + " signalStrength = " + signalStrength);
			Map<String, Object> signalMap = ReflectionUtils
					.dumpClassAsStringFeed(SignalStrength.class,
							mSignalStrength);
			// Map<String, Object> signalMap = ReflectionUtils
			// .dumpStaticFields(SignalStrength.class, mSignalStrength);
			// Map<String, Object> signalMap = ReflectionUtils.dumpClass(
			// SignalStrength.class, mSignalStrength);

			for (Map.Entry<String, Object> entry : signalMap.entrySet()) {

				cellPhysicsBundle.putString(entry.getKey(), entry.getValue()
						.toString());

			}

			Log.d(LOG_TAG, "CELLBUNDLE in onSignalStrength" + cellPhysicsBundle
					+ " mPingFlag" + mPingFlag);
			if (mPingFlag > 6) {
				String pingString = NetworkingTools.ping().toString();
				Log.d(LOG_TAG, "CELLBUNDLE ping result" + pingString);
				cellPhysicsBundle.putString("ping", pingString);

				mPingFlag = 0;

				DownloadTest dtest = new DownloadTest(
						CellMonitorActivatorService.this);
				httpDownloadTest = dtest
						.execute("http://cellmonitor.entscheidungsbaum.com/img/common/globe.png");

				StringBuffer sb = new StringBuffer();

				try {
					long[] httpTestArray = {dtest.get().get("startHttp"),dtest.get().get("HttpDownloadFinshed")};
					sb.append(dtest);
					// sb.append("StartHttp : ");
					// sb.append(dtest.get().get("startHttp"));
					// sb.append(";");
					// sb.append("HttpDownloadFinished : ");
					// sb.append(dtest.get().get("HttpDownloadFinshed"));

					cellPhysicsBundle.putLongArray("HttpDownloadTest",httpTestArray);

				} catch (InterruptedException e) {
					Log.d(LOG_TAG, " DOWNLOADTEST -> " + e);
				} catch (ExecutionException e) {
					Log.d(LOG_TAG, " DOWNLOADTEST -> " + e);
				}
			
			}
			sendToReceiver(CELLPHYSICS, cellPhysicsBundle);
			super.onSignalStrengthsChanged(signalStrength);
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.location.LocationListener#onLocationChanged(android.location.
	 * Location)
	 */
	@Override
	public void onLocationChanged(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();

		double[] geoLoc = { lat, lng };
		Log.d(LOG_TAG, " lat [" + lat + "] lon [" + lng + "]" + geoLoc[0]);
		cellPhysicsBundle.putDoubleArray("geoLocation", geoLoc);
		cellBundle.putDoubleArray("geoLocation ", geoLoc);
		mPingFlag = +1;
		if (mPingFlag > 6) {
			String pingString = NetworkingTools.ping().toString();
			cellBundle.putString("ping", pingString);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String,
	 * int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(LOG_TAG, "Status changed mProvider " + provider + " status "
				+ status + " Bundle extras " + extras.toString());
	}

	/**
	 * for the network test on a http download base
	 * 
	 * @author marcus@entscheidungsbaum.com 24.10.2013
	 *         CellMonitorActivatorService CellMonitorActivatorService.java
	 */

	private static class DownloadTest extends
			AsyncTask<String, Void, HashMap<String, Long>> {

		String LOG_TAG = DownloadTest.class.getCanonicalName();
		private Context mContext;

		public DownloadTest(Context context) {
			this.mContext = context;
		}

		/*
		 * 
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

		}

		/*
		 * gets the
		 */
		@Override
		protected HashMap<String, Long> doInBackground(String... params) {
			Log.d(LOG_TAG, " URL to do the HTTP download " + params[0]);
			PowerManager powerManager = (PowerManager) this.mContext
					.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
					PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
			wakeLock.acquire();
			HashMap<String, Long> testHttp = new HashMap<String, Long>();
			try {
				Log.d(LOG_TAG, "starting doBackground for url " + params[0]);

				testHttp = NetworkingTools.testHttpConnection(params[0]);

				Log.d(LOG_TAG,
						"retrieving form testHttpConnection doBackground for url "
								+ testHttp);

				return testHttp;
			} catch (Exception ioe) {
				Log.e(LOG_TAG, " Exception " + ioe);
			} finally {
				wakeLock.release();
			}
			wakeLock.release();

			return testHttp;
		}

	}
}
