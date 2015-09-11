package com.elfiky.cordova.plugin.quotesnotifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.trio.android.maqoola.R;

import java.util.Random;

/**
 * Created by elfiky on 11/09/15.
 */
public class NotificationReceiver extends BroadcastReceiver {
    protected final int NOTIFY_ME_ID = 11041;
    public static String TAG = "notification_quotes_error";
    public static String URL = "http://192.168.43.90:8000/api/cashed_quotes";
    public static String QUOTES_KEY = "cashed_quotes";
    static Context current_object;
    SharedPreferences prefs;

    public NotificationReceiver() {
    }


    @Override
    public void onReceive(Context context, Intent _intent) {
        try {
            current_object = context;
            prefs = context.getSharedPreferences(
                    NotificationReceiver.class.getPackage().getName(), Context.MODE_PRIVATE);
            String temp_quotes = prefs.getString(QUOTES_KEY, "");
            if (!temp_quotes.equals("")) {
                JsonParser parser = new JsonParser();
                JsonElement tradeElement = parser.parse(temp_quotes);
                JsonArray quotes = tradeElement.getAsJsonArray();
                setQuote(quotes);
            } else {
                Ion.with(context)
                        .load(URL)
                        .asJsonArray()
                        .setCallback(new FutureCallback<JsonArray>() {
                            @Override
                            public void onCompleted(Exception e, JsonArray result) {
                                if (e == null) {
                                    prefs.edit().putString(QUOTES_KEY, result.toString()).apply();
                                    setQuote(result);
                                } else {
                                    if (e.getMessage() != null)
                                        Log.v(TAG, e.getMessage());
                                }
                            }
                        });
            }


        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void setQuote(JsonArray quotes) {
        String title = "test", body = "body";

        if (quotes.size() > 0) {
            Random r = new Random();
            JsonObject quote = quotes.get(r.nextInt(quotes.size()-1)).getAsJsonObject();
            body = quote.get("quote").getAsString();
            title = quote.get("author_name").getAsString();
        }
        final NotificationManager mgr = (NotificationManager) current_object
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Notification note = new Notification(R.drawable.icon, title, System.currentTimeMillis());
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(current_object.getApplicationContext(), notification);
        r.play();

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("com.whatsapp"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        // This pending intent will open
        // after notification click
        PendingIntent i = PendingIntent.getActivity(current_object, 0, intent, 0);
        note.setLatestEventInfo(current_object, title, body, i);
        mgr.notify(NOTIFY_ME_ID, note);

    }
}
