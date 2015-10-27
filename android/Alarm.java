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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;

/**
 * Created by elfiky on 08/03/15.
 */
public class Alarm extends BroadcastReceiver {
    public static final int NOTIFY_ME_ID = 11041;
    public static String TAG = "notification_quotes_error";
    public static String URL = "http://www.maqoola.com/api/cashed_quotes";
    public static String QUOTES_KEY = "cashed_quotes";
    public static String CURRENT_DAY = "current_day";
    static Context current_object;
    public static SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        ShowNotification(context);
    }

    public void SetAlarm(Context context) {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_YEAR);
        Log.v(TAG, context.getClass().getPackage().getName());
        prefs = context.getSharedPreferences(
                MaqoolaNotificationsPlugin.NOTIFICATION_KEY,
                Context.MODE_PRIVATE);
        if (prefs.getInt(CURRENT_DAY, -1) == -1) {

            AlarmManager alarmMgr = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, Alarm.class);

            PendingIntent pi = PendingIntent.getBroadcast(context, 0,
                    alarmIntent, 0);
            // Set the alarm to start at approximately 1:00 p.m.
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

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
        calendar.add(Calendar.HOUR_OF_DAY, 12);
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

    public static void ShowNotification(Context context) {
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();

        current_object = context;
        prefs = context.getSharedPreferences(
                MaqoolaNotificationsPlugin.NOTIFICATION_KEY,
                Context.MODE_PRIVATE);

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

    public static void setQuote(JsonArray quotes) {
        try {

            String title = "", body = "", brief = "", quote_id = "";
            JsonObject quote = null;
            if (quotes.size() > 0) {
                Random r = new Random();
                quote = quotes.get(r.nextInt(quotes.size() - 1))
                        .getAsJsonObject();
                body = quote.get("quote").getAsString();
                title = quote.get("auther_name").getAsString();
                brief = quote.get("brief").getAsString().trim();
                quote_id = quote.get("id").getAsString();
            }
            if (quote != null) {
                prefs.edit()
                        .putString(MaqoolaNotificationsPlugin.QUOTE_ID_KEY,
                                quote.toString()).apply();
                final NotificationManager mgr = (NotificationManager) current_object
                        .getSystemService(Context.NOTIFICATION_SERVICE);

                Intent intent = new Intent(current_object,
                        ViewQuoteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                PendingIntent i = PendingIntent.getActivity(current_object, 0,
                        intent, 0);
                NotificationCompat.Builder normal = buildNormal(current_object,
                        title, body, i, brief);

                Random r = new Random();
                int i1 = r.nextInt(80 - 65) + 65;
                mgr.notify(NOTIFY_ME_ID + i1, normal.setContentText(body)
                        .build());
                LOG.v(TAG, quote_id);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

    }

    private static NotificationCompat.Builder buildNormal(Context context,
            String title, String quote, PendingIntent intent, String breif) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(context);
        b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(title + " (" + breif + ")")
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_stat_quotes_white_icon)
                .setStyle(new BigTextStyle().bigText(quote)).setTicker("مقولة")
                .setPriority(Notification.PRIORITY_HIGH);

        return (b);
    }

}