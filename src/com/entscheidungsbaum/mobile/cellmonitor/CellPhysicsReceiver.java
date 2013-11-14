package com.entscheidungsbaum.mobile.cellmonitor;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * @author marcus@entscheidungsbaum.com 28.08.2013
 * 
 *         CellPhysicsReceiver.java
 */
public class CellPhysicsReceiver extends ResultReceiver {

	private Receiver mCellPhysicsResultReceiver;

	private int uploadIntervall;

	boolean uploaded = true;

	private static final String LOG_TAG = CellPhysicsReceiver.class.getName();

	private Timestamp timestamp;

	File cellPhysicsFile = null;

	private boolean mDone;

	private boolean isPersisted;
	JSONObject json;

	/**
	 * @param handler
	 */
	public CellPhysicsReceiver(Handler handler) {

		super(handler);

		Log.d(LOG_TAG,
				" Constructor " + " handler  SETUP " + handler.toString());

		// TODO Auto-generated constructor stub
	}

	public void setReceiver(Receiver receiver) {
		Log.d(LOG_TAG, " setReceiver " + receiver.toString());

		mCellPhysicsResultReceiver = receiver;
	}

	/**
	 * 
	 * @author marcus@entscheidungsbaum.com 10.09.2013 CellPhysicsReceiver
	 *         CellPhysicsReceiver.java
	 */
	public interface Receiver {
		public void onReceiveResult(int resultCode, Bundle resultData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.ResultReceiver#onReceiveResult(int, android.os.Bundle)
	 */
	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		timestamp = new Timestamp(System.currentTimeMillis());

		SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");

		String timeFormatString = s.format(timestamp);

		Log.d(LOG_TAG, "onReceiveResult in " + LOG_TAG + " resultData "
				+ resultData + " resultCode " + resultData + " on date "
				+ timeFormatString);

		if (mCellPhysicsResultReceiver != null) {
			mCellPhysicsResultReceiver.onReceiveResult(resultCode, resultData);
			cellPhysicsFile = new File("CellPhysicsJsonFile-" + timeFormatString
					+ ".json");
			json = Tools.resultInfoToJson(resultData, timeFormatString);
			isPersisted = Tools.persistToFile(json, cellPhysicsFile);

			Log.d(LOG_TAG, " JSON before upload task interval = "
					+ uploadIntervall + " persisted = " + isPersisted);
			if (uploadIntervall == 5) {

				File physicsFile = new File(
						Environment.getExternalStorageDirectory()
								+ cellPhysicsFile.getAbsolutePath());
				Log.d(LOG_TAG, "uploading json now uploadinterval reached ="
						+ uploadIntervall + " CellPhysicsFile length"
						+ cellPhysicsFile.length());

				new UploadTask().execute(physicsFile);
				uploadIntervall = 0;
				// try {
				// if (isUploaded.get()) {
				// Log.d(LOG_TAG,
				// " JSON upload task finished ");
				// uploadIntervall = 0;
				// }
				// } catch (InterruptedException e) {
				// Log.e("InterruptedException ", e.getMessage());
				// } catch (ExecutionException e) {
				// Log.e("ExecutionException ", e.getMessage());
				// }
			} else {

				uploadIntervall = uploadIntervall + 1;
				Log.d(LOG_TAG, "File uploadInterval = " + uploadIntervall);
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.ResultReceiver#describeContents()
	 */
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return super.describeContents();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.ResultReceiver#send(int, android.os.Bundle)
	 */
	@Override
	public void send(int resultCode, Bundle resultData) {
		// TODO Auto-generated method stub
		super.send(resultCode, resultData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.ResultReceiver#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel out, int flags) {
		// TODO Auto-generated method stub
		super.writeToParcel(out, flags);
	}

	/*
	 * avoid ANR
	 */
	// private class UploadTask extends AsyncTask<JSONObject, Void, Void> {

	private class UploadTask extends AsyncTask<File, Void, Boolean> {
		// private class UploadTask extends AsyncTask<File, Void, Void> {
		protected Boolean doInBackground(File... mJsonObject) {
			// protected Void doInBackground(File... aFile) {
			// isPersisted = Tools.persistToFile(mJsonObject[0],
			// cellPhysicsFile);

			boolean isUploaded = false;
			if (isPersisted) {
				isUploaded = NetworkingTools.uploadJsonFtp(mJsonObject[0]);
				Log.d(LOG_TAG, " File do in Backround file upload "
						+ mJsonObject[0] + " uploaded = " + isUploaded);
				// if (isUploaded) {
				// uploadIntervall = 0;
				// }
			}
			return isUploaded;
		}

		protected void onProgressUpdate(Void... progress) {
			Log.d(LOG_TAG, " PROGRESS UPDATE " + progress);
		}

		protected void onPostExecute(Void result) {
			Log.d(LOG_TAG, " onPostExecute -> uploadIntervall "
					+ uploadIntervall);
			complete(uploadIntervall);
		}

	}

	private final void complete(int anUploadIntervall) {
		Log.d(LOG_TAG, "completed intervall = " + uploadIntervall);
		mDone = true;

	}

}
