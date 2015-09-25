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

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by elfiky on 08/03/15.
 */
public class Alarm extends BroadcastReceiver {
    protected final int NOTIFY_ME_ID = 11041;
    public static String TAG = "notification_quotes_error";
    public static String URL = "http://www.maqoola.com/api/cashed_quotes";
    public static String QUOTES_KEY = "cashed_quotes";
    public static String CURRENT_DAY = "current_day";
    static Context current_object;
    SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();

        current_object = context;
        prefs = context.getSharedPreferences(intent.getClass().getPackage()
                .getName(), Context.MODE_PRIVATE);

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_YEAR);

        String temp_quotes = prefs.getString(QUOTES_KEY, "");
        if (!temp_quotes.equals("")) {
            JsonParser parser = new JsonParser();
            JsonElement tradeElement = parser.parse(temp_quotes);
            JsonArray quotes = tradeElement.getAsJsonArray();
            setQuote(quotes);
        } else {
            Ion.with(current_object).load(URL).asJsonArray()
                    .setCallback(new FutureCallback<JsonArray>() {
                        @Override
                        public void onCompleted(Exception e, JsonArray result) {
                            if (e == null) {
                                prefs.edit()
                                        .putString(QUOTES_KEY,
                                                result.toString()).apply();
                                setQuote(result);
                            } else {
                                if (e.getMessage() != null)
                                    Log.e(TAG, e.getMessage());
                            }
                        }
                    });
        }
        Alarm alarm = new Alarm();
        alarm.SetNextAlarm(current_object);
        wakeLock.release();
    }

    public void SetAlarm(Context context) {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_YEAR);

        prefs = context.getSharedPreferences(context.getClass().getPackage()
                .getName(), Context.MODE_PRIVATE);
        if (prefs.getInt(CURRENT_DAY, -1) == -1) {

            AlarmManager alarmMgr = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, Alarm.class);

            PendingIntent pi = PendingIntent.getBroadcast(context, 0,
                    alarmIntent, 0);
            // Set the alarm to start at approximately 1:00 p.m.
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 1);

            // With setInexactRepeating(), you have to use one of the
            // AlarmManager
            // interval
            // constants--in this case, AlarmManager.INTERVAL_DAY.
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

            prefs.edit().putInt(CURRENT_DAY, day).apply();
        }
    }

    public void SetNextAlarm(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, Alarm.class);

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, alarmIntent,
                0);
        // Set the alarm to start at approximately 1:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // With setInexactRepeating(), you have to use one of the AlarmManager
        // interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }

    public void CancelAlarm(Context context) {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent
                .getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
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
                    .setSmallIcon(R.drawable.ic_stat_quotes_white_icon)
                    .setOnlyAlertOnce(true).setContentIntent(i).build();
            Random r = new Random();
            int i1 = r.nextInt(80 - 65) + 65;
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
            mgr.notify(NOTIFY_ME_ID+i1, noti);
            LOG.v(TAG, quote_id);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

    }

}