package com.entscheidungsbaum.mobile.cellmonitor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.net.http.AndroidHttpClient;
import android.os.SystemClock;
import android.util.Log;

/**
 * @author marcus@entscheidungsbaum.com 10.06.2013
 * 
 *         NetworkingTools.java
 * @param <E>
 */
public class NetworkingTools {

	private final static String LOG_TAG = NetworkingTools.class
			.getCanonicalName();

	private Inet4Address ipTOPing;

	long downloadTimeCompleted = 0;

	static String testUrl = "173.194.113.15";

	static String testResult;

	public static ArrayList<String> ping() {
		ArrayList<String> avgPing = new ArrayList<String>();

		try {
			Process process = Runtime.getRuntime().exec(
					"/system/bin/ping -c 5 " + testUrl);
			Log.d(LOG_TAG, process.toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));

			int i;
			char[] buffer = new char[4096];
			StringBuffer output = new StringBuffer();

			while ((i = reader.read(buffer)) > 0) {
				output.append(buffer, 0, i);
				Log.d(LOG_TAG,
						"COMPLETE PING STRING => "
								+ (output.toString().split(" ")).toString());

				String[] pingT = (output.toString().split(" "));
				Log.d(LOG_TAG, " pingT of time = " + pingT[pingT.length - 2]);
				Log.d(LOG_TAG, " part of time = "
						+ (pingT[(pingT[pingT.length - 2]).toString()
								.split("=").length]));
				avgPing.add(pingT[pingT.length - 2]);

			}
			reader.close();

			Log.d(LOG_TAG, " avgPing array = " + avgPing);
			// body.append(output.toString()+"\n");
			testResult = output.toString();
		} catch (IOException e) {
			// body.append("Error\n");
			Log.e(LOG_TAG, e.toString());
		}
		return avgPing;
	}

	/**
	 * 
	 * @param cellInfoJsonFeed
	 * @param networkservice
	 *            1 for ftp 2 for http
	 * @return boolean successful or not
	 */
	public static boolean uploadJsonFeed(JSONObject cellInfoJsonFeed,
			int networkservice) {
		boolean uploaded = false;
		Log.d(LOG_TAG, " upload request received");
		if (networkservice == 2) {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost hp = new HttpPost(
					"http;//www.entscheidungsbaum.com/cellmonitor");
			try {
				StringEntity se = new StringEntity(cellInfoJsonFeed.toString());
				hp.setEntity(se);
				hp.setHeader("Accept", "application/json");
				hp.setHeader("Content-type", "application/json");

				ResponseHandler responseHandler = new BasicResponseHandler();
				HttpResponse response = httpClient.execute(hp, responseHandler);

				Log.d(LOG_TAG, " STATUS CODE = "
						+ response.getStatusLine().getStatusCode());
				if (response.getStatusLine().getStatusCode() == 400)
					uploaded = true;
			} catch (UnsupportedEncodingException e) {
				uploaded = false;
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				uploaded = false;
				e.printStackTrace();
			} catch (IOException e) {
				uploaded = false;
				e.printStackTrace();
			}

		} else if (networkservice == 1) {

		}

		return uploaded;
	}

	public static boolean uploadJsonFtp(File aFileToUpload) {
		Log.d(LOG_TAG, "File to upload  is file ? = " + aFileToUpload.isFile()
				+ " file name " + aFileToUpload.length());

		boolean uploaded = false;
		boolean peristed2FS = false;

		String user = "cellmonitor";
		String password = "cell123";
		String server = "cellmonitor.entscheidungsbaum.com";
		FTPClient ftpClient = new FTPClient();

		try {

			ftpClient.connect(InetAddress.getByName(server));
			ftpClient.login(user, password);
			// ftpClient.changeWorkingDirectory("/");
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.cwd("/cellmonitordata");

			Log.d(LOG_TAG, " starting ftp upload buffersize=" + aFileToUpload);

			BufferedInputStream buffIn = null;
			FileInputStream fileInputStream = new FileInputStream(aFileToUpload);
			buffIn = new BufferedInputStream(fileInputStream);
			ftpClient.enterLocalPassiveMode();
			Log.d(LOG_TAG, "FileInputStream has reached =  " + buffIn.read());
			String[] remoteFiles = ftpClient.listNames();
			// boolean exist = false;
			Log.d(LOG_TAG, "RemoteFiles=" + remoteFiles);
			for (String fileToOverwrite : remoteFiles) {
				Log.d(LOG_TAG, "Files" + fileToOverwrite + " = "
						+ aFileToUpload.getName());
				if (aFileToUpload.getName() == fileToOverwrite) {
					Log.d(LOG_TAG, "file exist append ");
					ftpClient.appendFile("/cellmonitordata/", fileInputStream);
					// exist=true;

				}
			}
			Log.d(LOG_TAG, "file new  " + fileInputStream.read() + " "
					+ ftpClient.getStatus());

			ftpClient
					.storeFile("/cellmonitor" + aFileToUpload, fileInputStream);
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				Log.d(LOG_TAG,
						" ftp session completed  " + ftpClient.getReplyCode());

				buffIn.close();
				ftpClient.logout();
				ftpClient.disconnect();

				fileInputStream.close();
				Log.d(LOG_TAG, "disconnecting ftp session ");
				uploaded = true;
			}

		} catch (SocketException e) {
			uploaded = false;
			Log.e(LOG_TAG, "Socket exception" + e.toString());
		} catch (UnknownHostException e) {
			uploaded = false;
			Log.e(LOG_TAG, "unknown host exception " + e.toString());
		} catch (IOException e) {
			uploaded = false;
			Log.e(LOG_TAG, " IO excpetion " + e.toString());
		} finally {
			Log.d(LOG_TAG, " uploaded = " + uploaded);
			// boolean peristed2FS = Tools.persistToFile(json,cellPhysicsFile);

		}

		return uploaded;
	}

	// test the download of a well defined
	public static HashMap<String,  Long> testHttpConnection(
			String testHttpUri) {
		Log.d(LOG_TAG, "TESTHTTPCONNECTION = " + testHttpUri);

		HashMap<String, Long> downloadTime = new HashMap<String,Long>();

		long fileLength = 0;

		byte[] transferedBytes = new byte[1024];

		InputStream inputStream = null;
		
		long getContentDuration=0;
		long statusTime=0;
		long startExecute =0;
		try {
			
			URL url = new URL(testHttpUri);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        // Starts the query
	        conn.connect();
	        int statusCode = conn.getResponseCode();
	        fileLength = conn.getContentLength();
			// start
			startExecute = System.currentTimeMillis();
			Log.d(LOG_TAG, "STATUS CODE HTTP GET - " + statusCode);
			//
		
			inputStream = conn.getInputStream();
			
			int count =0;
			long total = 0;
			Log.d(LOG_TAG, " CONTENTLENGTH => " + fileLength + " content type " + conn.getContentType() );
			while ((count = inputStream.read(transferedBytes)) != -1) {
				total += count;
				Log.d(LOG_TAG, " CONTENTLENGTH => total " + total + " count => "+ count);

				if (total ==  fileLength) {
					
					Log.d(LOG_TAG, " file = " + total
							+ " retrieved file content Length " + fileLength + " getContentDuration =" +getContentDuration);
//					downloadSteps
//							.put("contentDownloadDuration", getContentDuration);
				}
			}
			getContentDuration =System.currentTimeMillis() - startExecute;

			Log.d(LOG_TAG, "TIMELIST  duration startExecute = " + startExecute + " statusTime = " + statusTime + " contentdeliveryDuration =  " + getContentDuration);
			downloadTime.put("startHttp", startExecute );

			downloadTime.put("HttpDownloadFinshed", getContentDuration);


		} catch (Exception e) {
			Log.e(LOG_TAG, "Error in test Http connection " + e);

		} finally {
			//downloadSteps = null;
			//downloadTime = null;
			statusTime=0;
			startExecute=0;
			Log.d(LOG_TAG, "downloadtime  " + downloadTime + " ms" );

		}

		Log.d(LOG_TAG, "Test Http Connection -> " + downloadTime);
		return downloadTime;
	}
}
