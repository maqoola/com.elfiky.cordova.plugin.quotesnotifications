package com.elfiky.cordova.plugin.quotesnotifications;

import java.util.Random;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class MaqoolaNotificationsPlugin extends CordovaPlugin {

	public static final String ACTION_ADD_NOTIFICATION = "add_notification";
	public static final String ACTION_DOWNLOAD_FILE = "download_file";
	private final String TAG = "notification_quotes_error";
	public final static String QUOTE_ID_KEY = "quote_id";
	public final static String NOTIFICATION_KEY = "maqoola_notifications_key";

	@Override
	public boolean execute(final String action, JSONArray data,
			CallbackContext callbackContext) throws JSONException {

		if (ACTION_ADD_NOTIFICATION.equals(action)) {
			executeAddBroadCastReciver(callbackContext);
		}else if(ACTION_DOWNLOAD_FILE.equals(action)){
			executeDownloadFile(callbackContext, data.getString(0));
		}

		return true;
	}

	private PluginResult executeAddBroadCastReciver(
			final CallbackContext callbackContext) {

		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {

					Alarm alarm = new Alarm();
					alarm.SetAlarm(cordova.getActivity());

				} catch (Exception ex) {
					Log.e(TAG, "Error Adding Broadcast Reciver");
					Log.e(TAG, ex.getMessage());
				}

				callbackContext.success();
			}
		});

		return null;
	}

	private PluginResult executeDownloadFile(
			final CallbackContext callbackContext,final String url) {

		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					Log.v(TAG, "start Download File");
					Uri uri= Uri.parse(url);
					DownloadManager.Request req=new DownloadManager.Request(uri);
					Random randomGenerator = new Random();
					req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
					                               | DownloadManager.Request.NETWORK_MOBILE)
					   .setAllowedOverRoaming(false)
					   .setTitle("مقولة")
					   .setDescription("جارى التحميل ...")
					   .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
					                                      "maqoola_quotes_" + randomGenerator.nextInt(1000) + ".jpg");


					req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
					DownloadManager dm = (DownloadManager) cordova.getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
			        dm.enqueue(req);
				} catch (Exception ex) {
					Log.e(TAG, "Error Download File");
					Log.e(TAG, ex.getMessage());
				}

				callbackContext.success();
			}
		});

		return null;
	}

}
