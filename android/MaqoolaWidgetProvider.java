package widget.test.apps.android.testwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by elfiky on 24/10/15.
 */
public class MaqoolaWidgetProvider extends AppWidgetProvider {
    final static String TAG = "Quotes_widget";
    final static String URL = "http://www.maqoola.com/imager/160*160/uploads/authors/";
    private static DisplayImageOptions displayOptions;
    int widgetId;

    static {
        displayOptions = new DisplayImageOptions.Builder()
                .delayBeforeLoading(2)
                .cacheOnDisk(true).build();
    }


    @Override
    public void onUpdate(Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;
        init_image_loader(context);
        for (final int appWidgetId : appWidgetIds) {
            try {
                final RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                        R.layout.maqoola_widget);
                remoteViews.setViewVisibility(R.id.loadingIcon, View.VISIBLE);
                String photo = "", author = "", quote_body = "";
                widgetId = appWidgetId;

                Random r = new Random();
                JsonArray quotes = jsonToStringFromAssetFolder(context);
                JsonObject quote = quotes.get(r.nextInt(quotes.size() - 1))
                        .getAsJsonObject();
                author = quote.get("auther_name").getAsString();
                photo = quote.get("photo").getAsString();
                quote_body = quote.get("quote").getAsString();


                remoteViews.setTextViewText(R.id.txtAuthor, author);
                remoteViews.setTextViewText(R.id.txtQuote, quote_body);
                remoteViews.setImageViewResource(R.id.imgAuthor, R.drawable.unknown);

                ImageLoader.getInstance()
                        .loadImage(URL + photo, displayOptions, new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                remoteViews.setImageViewBitmap(R.id.imgAuthor, loadedImage);
                                appWidgetManager.updateAppWidget(widgetId, remoteViews);
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                super.onLoadingFailed(imageUri, view, failReason);

                            }
                        });

                Intent intent = new Intent(context, MaqoolaWidgetProvider.class);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.reloadButton, pendingIntent);
                remoteViews.setViewVisibility(R.id.loadingIcon, View.GONE);

                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    private void init_image_loader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

    public static JsonArray jsonToStringFromAssetFolder(Context context) throws IOException {
        InputStream file = context.getResources().openRawResource(R.raw.cashed_quotes);

        byte[] data = new byte[file.available()];
        file.read(data);
        file.close();

        JsonParser parser = new JsonParser();
        JsonElement tradeElement = parser.parse(new String(data));
        return tradeElement.getAsJsonArray();
    }
}