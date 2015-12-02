package com.elfiky.cordova.plugin.quotesnotifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.trio.android.maqoola.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.apache.cordova.LOG;

/**
 * Created by elfiky on 24/10/15.
 */
public class MaqoolaWidgetProvider extends AppWidgetProvider {
    final static String TAG = "Quotes_widget";
    final static String URL = "http://www.maqoola.com/imager/160*160/uploads/authors/";
    private static DisplayImageOptions displayOptions;
    int widgetId;
    public static SharedPreferences prefs;
    static Context current_context;
    static {
        displayOptions = new DisplayImageOptions.Builder()
                .delayBeforeLoading(2).cacheOnDisk(true).build();
    }

    @Override
    public void onUpdate(Context context,
            final AppWidgetManager appWidgetManager,final int[] appWidgetIds) {
        final int count = appWidgetIds.length;
        current_context=context;
        init_image_loader(context);
        for (final int appWidgetId : appWidgetIds) {
            try {
                final RemoteViews remoteViews = new RemoteViews(
                        context.getPackageName(), R.layout.maqoola_widget);
                remoteViews.setViewVisibility(R.id.loadingIcon, View.VISIBLE);

                widgetId = appWidgetId;

                prefs = context.getSharedPreferences(
                        MaqoolaNotificationsPlugin.NOTIFICATION_KEY,
                        Context.MODE_PRIVATE);

                String temp_quotes = prefs.getString(Alarm.QUOTES_KEY, "");
                if (!temp_quotes.equals("")) {
                    JsonParser parser = new JsonParser();
                    JsonElement tradeElement = parser.parse(temp_quotes);
                    JsonArray quotes = tradeElement.getAsJsonArray();
                    setQuote(quotes, remoteViews, appWidgetIds, appWidgetManager,widgetId);
                } else {
                    Ion.with(context).load(URL).asJsonArray()
                            .setCallback(new FutureCallback<JsonArray>() {
                                @Override
                                public void onCompleted(Exception e,
                                        JsonArray result) {
                                    if (e == null) {
                                        prefs.edit()
                                                .putString(Alarm.QUOTES_KEY,
                                                        result.toString())
                                                .apply();
                                        setQuote(result, remoteViews, appWidgetIds, appWidgetManager,widgetId);
                                    } else {
                                        if (e.getMessage() != null)
                                            Log.e(TAG, e.getMessage());
                                    }
                                }
                            });
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    private void init_image_loader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(
                context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

    public static void setQuote(JsonArray quotes,
            final RemoteViews remoteViews, final int[] appWidgetIds,
            final AppWidgetManager appWidgetManager,final int widgetId) {
        try {

            String photo = "", author = "", quote_body = "";
            JsonObject quote = null;
            if (quotes.size() > 0) {
                Random r = new Random();
                quote = quotes.get(r.nextInt(quotes.size() - 1))
                        .getAsJsonObject();
                author = quote.get("auther_name").getAsString();
                photo = quote.get("photo").getAsString();
                quote_body = quote.get("quote").getAsString();

            }
            if (quote != null) {
                remoteViews.setTextViewText(R.id.txtAuthor, author);
                remoteViews.setTextViewText(R.id.txtQuote, quote_body);
                remoteViews.setImageViewResource(R.id.imgAuthor,
                        R.drawable.unknown);

                ImageLoader.getInstance().loadImage(URL + photo,
                        displayOptions, new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri,
                                    View view, Bitmap loadedImage) {
                                remoteViews.setImageViewBitmap(R.id.imgAuthor,
                                        loadedImage);
                                appWidgetManager.updateAppWidget(widgetId,
                                        remoteViews);
                            }

                            @Override
                            public void onLoadingFailed(String imageUri,
                                    View view, FailReason failReason) {
                                super.onLoadingFailed(imageUri, view,
                                        failReason);

                            }
                        });

                Intent intent = new Intent(current_context, MaqoolaWidgetProvider.class);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        appWidgetIds);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        current_context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.reloadButton,
                        pendingIntent);
                remoteViews.setViewVisibility(R.id.loadingIcon, View.GONE);

                prefs.edit()
                .putString(MaqoolaNotificationsPlugin.QUOTE_ID_KEY,
                        quote.toString()).apply();
                PendingIntent i = getShowQuoteIntent(intent);

                remoteViews.setOnClickPendingIntent(R.id.txtQuote, i);
                remoteViews.setOnClickPendingIntent(R.id.txtAuthor, i);
                remoteViews.setOnClickPendingIntent(R.id.imgAuthor, i);

                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

    }

	private static PendingIntent getShowQuoteIntent(Intent intent) {
		Intent _intent = new Intent(current_context,
		        ViewQuoteActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
		        | Intent.FLAG_ACTIVITY_CLEAR_TOP
		        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		PendingIntent i = PendingIntent.getActivity(current_context, 0,
				_intent, 0);
		return i;
	}

}
