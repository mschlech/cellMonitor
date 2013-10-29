/*
 * @Author marcus@entscheidungsbaum.com
 * 14.05.2013 
 */
package com.entscheidungsbaum.mobile.cellmonitor;

import java.io.File;
import java.io.FileWriter;
import java.sql.Date;
import java.sql.Timestamp;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * @author marcus@entscheidungsbaum.com 14.05.2013
 * 
 *         CellDataResultReceiver.java
 * 
 *         generic PhoneStateListener , Wifi and location information
 */
public class CellDataResultReceiver extends ResultReceiver {

	private final static String LOG_TAG = CellDataResultReceiver.class
			.getName();

	private Receiver mCellDataResultReceiver = null;

	private boolean uploaded = true;

	private int uploadIntervall;

	File persistedFile;

	private boolean mDone = false;

	private boolean isPersisted;

	private Date date = new Date(System.currentTimeMillis());
	private Timestamp timestamp ;
	
	/**
	 * @param handler
	 */
	public CellDataResultReceiver(Handler handler) {
		super(handler);
		Log.d(LOG_TAG,
				" Constructor " + " handler  SETUP " + handler.toString());

	}

	public void setRecveiver(Receiver receiver) {
		Log.d(LOG_TAG, " setReceiver " + receiver.toString());
		mCellDataResultReceiver = receiver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.ResultReceiver#describeContents()
	 */
	@Override
	public int describeContents() {
		Log.d(LOG_TAG, " describecontent  " + super.describeContents());
		return super.describeContents();
	}

	/**
	 * 
	 * @author marcus@entscheidungsbaum.com 04.06.2013 CellDataResultReceiver
	 *         CellDataResultReceiver.java
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

		Log.d(LOG_TAG, "onReceiveResult in " + LOG_TAG + " resultData "
				+ resultData + " resultCode " + resultData + " on date "
				+ timestamp);

		if (mCellDataResultReceiver != null) {
			mCellDataResultReceiver.onReceiveResult(resultCode, resultData);
			persistedFile = new File("GenericCellDataReceiveResults-"
					+ timestamp + ".json");
			JSONObject json = Tools.resultInfoToJson(resultData, timestamp);
			isPersisted = Tools.persistToFile(json, persistedFile);
			uploadIntervall = uploadIntervall + 1;

			Log.d(LOG_TAG, " JSON before upload task interval = "
					+ uploadIntervall + " file to upload " + persistedFile);
			if (uploadIntervall == 5) {
				
				File genericeFile = new File( Environment.getExternalStorageDirectory()+persistedFile.getAbsolutePath());
				Log.d(LOG_TAG, "uploading json now uploadinterval reached ="
						+ uploadIntervall + " length " + persistedFile.length() + " path "+genericeFile.getAbsolutePath()  );
				
				new UploadTask().execute(genericeFile);
				uploadIntervall = 0;
			} else {

				uploadIntervall = uploadIntervall + 1;
				Log.d(LOG_TAG, "File uploadInterval = " + uploadIntervall);
			}
		}

	}

	/*
	 * avoid ANR
	 */
	private class UploadTask extends AsyncTask<File, Void, Void> {
		// private class UploadTask extends AsyncTask<File, Void, Void> {

		/*
		 * change return type
		 */

		protected Void doInBackground(File... mJsonObject) {
			// protected Void doInBackground(File... aFile) {
			// isPersisted = Tools.persistToFile(mJsonObject[0], persistedFile);
			boolean isUploaded = false;
			Log.d(LOG_TAG, "File in asyncTask " + mJsonObject[0]);
			if (isPersisted) {
				isUploaded = NetworkingTools.uploadJsonFtp(mJsonObject[0]);
				Log.d(LOG_TAG, " do in Backround file upload " + mJsonObject[0]
						+ " uploaded = " + isUploaded);
				if (isUploaded) {
					uploadIntervall = 0;
				}
			}

			return null;
		}

		protected void onProgressUpdate(Void... progress) {
			Log.d(LOG_TAG, " PROGRESS UPDATE " + progress);
		}

		protected void onPostExecute(Void result) {
			Log.d(LOG_TAG, " onPostExecute uploadIntervall-> "
					+ uploadIntervall);
			complete(uploadIntervall);
		}

	}

	private final void complete(int anUploadInterval) {
		Log.d(LOG_TAG, "completed intervall = " + uploadIntervall);
		mDone = true;

	}
}
