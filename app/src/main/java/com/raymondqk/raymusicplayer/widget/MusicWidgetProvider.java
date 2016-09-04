package com.raymondqk.raymusicplayer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.raymondqk.raymusicplayer.R;
import com.raymondqk.raymusicplayer.service.MusicService;

/**
 * Created by 陈其康 raymondchan on 2016/8/5 0005.
 */
public class MusicWidgetProvider extends AppWidgetProvider {

    public static final String WIDGET_PLAY_ACTION = "widget.play.action";
    public static final String WIDGET_PLAY = "com.raymondcqk.musicplayer.widget_play_for_service";
    public static final String WIDGET_NEXT_ACTION = "widget.next.action";
    public static final String WIDGET_NEXT = "com.raymondcqk.musicplayer.widget_next_for_service";
    public static final String WIDGET_PREVIEW_ACTION = "widget.preview.action";
    public static final String WIDGET_PREVIEW = "com.raymondcqk.musicplayer.widget_preview_for_service";
    public static final String TEST = "TEST";

    private int play_state = MusicService.STATE_STOP;
    private boolean isFirst = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent != null) {


            Log.i(TEST, "widget-onReceive");
            String action = intent.getAction();
            if (TextUtils.equals(action, WIDGET_PLAY_ACTION)) {
                Intent i = new Intent();
                i.setAction(WIDGET_PLAY);
                context.sendBroadcast(i);
                Log.i(TEST, "widget-onReceive-sendPlay");
            }
            if (TextUtils.equals(action, WIDGET_NEXT_ACTION)) {
                Intent i = new Intent();
                i.setAction(WIDGET_NEXT);
                context.sendBroadcast(i);
                Log.i(TEST, "widget-onReceive-sendNext");
            }
            if (TextUtils.equals(action, WIDGET_PREVIEW_ACTION)) {
                Intent i = new Intent();
                i.setAction(WIDGET_PREVIEW);
                context.sendBroadcast(i);
                Log.i(TEST, "widget-onReceive-sendPreview");
            }
        }

        /**
         * 更新widget ui 的方式
         *
         if (isFirst) {
         return;
         } else {
         RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
         if (play_state == MusicService.STATE_PLAYING) {
         play_state = MusicService.STATE_STOP;
         remoteViews.setImageViewResource(R.id.widget_ib_play, R.drawable.play);

         } else if (play_state == MusicService.STATE_STOP) {
         play_state = MusicService.STATE_PLAYING;
         remoteViews.setImageViewResource(R.id.widget_ib_play, R.drawable.pause);

         }

         AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
         ComponentName componentName = new ComponentName(context, MusicWidgetProvider.class);
         appWidgetManager.updateAppWidget(componentName, remoteViews);

         isFirst = false;
         }

         }
         */

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);

        Intent intentNext = new Intent();
        intentNext.setAction(WIDGET_NEXT_ACTION);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_next, pendingIntentNext);

        Intent intent = new Intent();
        intent.setAction(WIDGET_PLAY_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_ib_play, pendingIntent);


        Intent intentPre = new Intent();
        intentPre.setAction(WIDGET_PREVIEW_ACTION);
        PendingIntent pendingIntentPre = PendingIntent.getBroadcast(context, 0, intentPre, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_preview, pendingIntentPre);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);


    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

    }
}
