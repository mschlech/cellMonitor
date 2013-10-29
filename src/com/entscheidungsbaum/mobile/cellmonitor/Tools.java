package com.entscheidungsbaum.mobile.cellmonitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

/**
 * @author marcus@entscheidungsbaum.com 09.09.2013
 * 
 *         Tools.java
 */
public class Tools {

	private static final String LOG_TAG = Tools.class.getName();

	public static boolean persistToFile(JSONObject jo, File aFileToPersist) {
		boolean success = false;

		final FileWriter file;

		try {
			Log.d(LOG_TAG, " writing file to FS "
					+ Environment.getExternalStorageDirectory().getPath()
					+ "/" + aFileToPersist);

			file = new FileWriter(Environment.getExternalStorageDirectory()
					.getPath() + "/"+ aFileToPersist);
			Log.d(LOG_TAG, "FileToPersist length " + aFileToPersist.length());
			file.append(jo.toString());
			file.flush();
			file.close();
			success = true;
			// persistedFile = new
			// File(Environment.getExternalStorageDirectory()
			// .getPath() + aFileToPersist);

		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
		return success;

	}

	/**
	 * @param resultCode
	 * @param resultData
	 */
	public static JSONObject resultInfoToJson(Bundle resultData, Timestamp aDate) {
		Log.d(LOG_TAG, " resultInfoToJson invoked");

		JSONObject cellJson = new JSONObject();

		Set<String> keys = resultData.keySet();

		try {

			cellJson.put("date", aDate);

			for (String key : keys) {
				Object o = resultData.get(key);

				if (key.equals("getClass")) {
					// Log.d(LOG_TAG, " CREATOR FOUND");
				}
				cellJson.put((key.equals("getClass")) ? "ClassISnull" : key,
						o.toString());
				Log.d(LOG_TAG, " resultData resultInfoToJson key=" + key
						+ " value=" + o.toString());
				Log.d(LOG_TAG, " json Object to send -> " + cellJson.toString());
			}
		} catch (JSONException je) {
			Log.e(LOG_TAG, " could not create json feed ");
		} finally {
			Log.d(LOG_TAG, " about to do something special @TODO ");
		}
		return cellJson;

	}
	
	//public static 
}
