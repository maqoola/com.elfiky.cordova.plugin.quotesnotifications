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

	@Override
	public boolean execute(final String action, JSONArray data,
			CallbackContext callbackContext) throws JSONException {

		if (ACTION_ADD_NOTIFICATION.equals(action)) {
			executeAddBroadCastReciver(callbackContext);
		}
		

		return true;

	}

	private PluginResult executeAddBroadCastReciver(final CallbackContext callbackContext) {

		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
				    AlarmManager alarmMgr0 = (AlarmManager) cordova.getActivity().getSystemService(Context.ALARM_SERVICE);
			        //Create pending intent & register it to your alarm notifier class
			        Intent intent0 = new Intent(cordova.getActivity(), NotificationReceiver.class);
			        PendingIntent pendingIntent0 = PendingIntent.getBroadcast(cordova.getActivity(), 0, intent0, 0);

			        //set timer you want alarm to work (here I have set it to 7.20pm)
			        Calendar timeOff9 = Calendar.getInstance();
			        timeOff9.set(Calendar.HOUR_OF_DAY, 19);
			        timeOff9.set(Calendar.MINUTE, 57);
			        timeOff9.set(Calendar.SECOND, 0);

			        //set that timer as a RTC Wakeup to alarm manager object
			        alarmMgr0.set(AlarmManager.RTC_WAKEUP, timeOff9.getTimeInMillis(), pendingIntent0);

					Log.v(TAG, "Add Broadcast Reciver For Notification");
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