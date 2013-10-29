package com.entscheidungsbaum.mobile.cellmonitor;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @author marcus@entscheidungsbaum.com
 * @TODO find apropriate usage not in use so far
 */
public class CellMonitorTimerService extends BroadcastReceiver {

	private static final String LOG_TAG = CellMonitorTimerService.class
			.getName();
	
	
	public CellMonitorTimerService() {
		Log.d(LOG_TAG, "CellMonitorTimerService constructor");
		// super(name);
	}

	
	private boolean isStarted = false;

	/**
	 * an interval of 30 seconds
	 */
	private static final long SCHEDULE_INTERVALL = 1000 * 30;

	@Override
	public void onReceive(Context context, Intent intent) {

		Intent i = new Intent(context, CellMonitorActivatorService.class);

		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(i);

		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			isStarted = true;
			Log.i(LOG_TAG + " is started ", intent.toString());
			Log.i(LOG_TAG,
					" onReceive started from context  "
							+ context.getPackageName());

			AlarmManager service = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			if ("com.entscheidungsbaum.mobile.cellmonitor".equals(intent
					.getAction())) {
				Log.i(LOG_TAG,
						" onReceive started from context  "
								+ context.getPackageName());
				PendingIntent pending = PendingIntent.getBroadcast(context, 0,
						i, PendingIntent.FLAG_CANCEL_CURRENT);
				Calendar workerSchedule = Calendar.getInstance();
				workerSchedule.add(Calendar.SECOND, 30);
				Log.d(LOG_TAG, "next scheduled event");

				service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
						workerSchedule.getTimeInMillis(), SCHEDULE_INTERVALL,
						pending);
				Log.i(LOG_TAG, " workerschedule " + context.getPackageName()
						+ " " + workerSchedule.toString());
				context.startService(i);

			} else if (intent.getAction().equals(Intent.ACTION_DELETE)) {
				Log.i(LOG_TAG, "Action deleted [" + context + "] intent ["
						+ intent + "]");
				isStarted = false;

			}
		}

	}

}