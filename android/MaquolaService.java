package com.elfiky.cordova.plugin.quotesnotifications;

import java.util.Calendar;
import java.util.Random;

import org.apache.cordova.LOG;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.trio.android.maqoola.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by elfiky on 08/03/15.
 */
public class MaquolaService extends Service {
	Alarm alarm = new Alarm();
	protected final int NOTIFY_ME_ID = 11041;
	public static String TAG = "notification_quotes_error";
	public static String URL = "http://www.maqoola.com/api/cashed_quotes";
	public static String QUOTES_KEY = "cashed_quotes";
	public static String CURRENT_DAY = "current_day";
	static Context current_object;
	SharedPreferences prefs;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			current_object = this;
			prefs = getSharedPreferences(MaquolaService.class
					.getPackage().getName(), Context.MODE_PRIVATE);
		
			// start tracking location
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						Calendar c = Calendar.getInstance();
						int hour = c.get(Calendar.HOUR_OF_DAY);
						
						if (hour == 12 && (prefs.getInt(CURRENT_DAY, -1) == -1 || prefs.getInt(CURRENT_DAY, -1) != c.get(Calendar.DAY_OF_YEAR))) {
							prefs.edit().putInt(CURRENT_DAY, c.get(Calendar.DAY_OF_YEAR)).apply();
							String temp_quotes = prefs
									.getString(QUOTES_KEY, "");
							if (!temp_quotes.equals("")) {
								JsonParser parser = new JsonParser();
								JsonElement tradeElement = parser
										.parse(temp_quotes);
								JsonArray quotes = tradeElement
										.getAsJsonArray();
								setQuote(quotes);
							} else {
								Ion.with(current_object)
								.load(URL)
								.asJsonArray()
								.setCallback(
									new FutureCallback<JsonArray>() {
										@Override
										public void onCompleted(Exception e, JsonArray result) {
											if (e == null) {
												prefs.edit().putString(QUOTES_KEY,
																result.toString()).apply();
												setQuote(result);
											} else {
												if (e.getMessage() != null)
													Log.e(TAG,
															e.getMessage());
											}
										}
									});
							}
							Thread.sleep(1000*60*60);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};

			thread.start();

		} catch (Exception ex) {
			Log.v("esaver_error", ex.getMessage());
		}
		alarm.SetAlarm(MaquolaService.this);
		return START_STICKY;
	}

	private void setQuote(JsonArray quotes) {
		try {
			String title = "test", body = "body", quote_id = "";
			if (quotes.size() > 0) {
				Random r = new Random();
				JsonObject quote = quotes.get(r.nextInt(quotes.size() - 1))
						.getAsJsonObject();
				body = quote.get("quote").getAsString();
				title = quote.get("auther_name").getAsString();
				quote_id = quote.get("id").getAsString();
			}

			prefs.edit()
					.putString(MaqoolaNotificationsPlugin.QUOTE_ID_KEY,
							quote_id).apply();
			final NotificationManager mgr = (NotificationManager) current_object
					.getSystemService(Context.NOTIFICATION_SERVICE);

			Intent intent = new Intent(current_object, ViewQuoteActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

			PendingIntent i = PendingIntent.getActivity(current_object, 0,
					intent, 0);

			Notification noti = new Notification.Builder(current_object)
					.setDefaults(Notification.DEFAULT_ALL)
					.setContentTitle(title).setContentText(body)
					.setSmallIcon(R.drawable.ic_stat_quotes_white_icon).setOnlyAlertOnce(true)
					.setContentIntent(i).build();

			mgr.notify(NOTIFY_ME_ID, noti);
			LOG.v(TAG, quote_id);
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}

	}

	public void onStart(Context context, Intent intent, int startId) {
		alarm.SetAlarm(context);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
