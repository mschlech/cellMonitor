package com.entscheidungsbaum.mobile.cellmonitor;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * 
 * @author marcus@entscheidungsbaum.com
 * 
 */
public class HttpHelper {
	public void getServerData() throws JSONException, ClientProtocolException,
			IOException {

		ArrayList<String> stringData = new ArrayList<String>();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		ResponseHandler<String> resonseHandler = new BasicResponseHandler();
		HttpPost postMethod = new HttpPost(
				"http://transfer.entscheidungsbaum.com");
		postMethod.setHeader("Content-Header", "application/json");
		JSONObject json = new JSONObject();
		json.put("AlertEmail", true);
		json.put("APIKey", "abc123456789");
		json.put("Id", 0);
		
		json.put("AlertPhone", false);
		postMethod.setEntity(new ByteArrayEntity(json.toString().getBytes(
				"UTF8")));
		String response = httpClient.execute(postMethod, resonseHandler);
		Log.e("response :", response);
	}
}
