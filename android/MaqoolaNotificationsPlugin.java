package com.elfiky.cordova.plugin.quotesnotifications;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class MaqoolaNotificationsPlugin extends CordovaPlugin {

	public static final String ACTION_ADD_NOTIFICATION = "add_notification";
	private final String TAG = "notification_quotes_error";
	public final static String QUOTE_ID_KEY = "quote_id";
	public final static String NOTIFICATION_KEY = "maqoola_notifications_key";

	@Override
	public boolean execute(final String action, JSONArray data,
			CallbackContext callbackContext) throws JSONException {

		if (ACTION_ADD_NOTIFICATION.equals(action)) {
			executeAddBroadCastReciver(callbackContext);
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

}