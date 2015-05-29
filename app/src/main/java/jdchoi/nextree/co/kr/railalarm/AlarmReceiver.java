package jdchoi.nextree.co.kr.railalarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by Administrator on 2015-05-29.
 */
public class AlarmReceiver extends BroadcastReceiver{
    private static String TAG = "AlarmReceiver";
    private static MediaPlayer mp;

    @Override
    public void onReceive(Context context, Intent intent) {
        //도착 알림시 사운드 출력.
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            try {
                if (mp != null) {
                    mp.release();
                }

                mp = MediaPlayer.create(context, R.raw.subway_transfer);
                mp.seekTo(0);
                mp.start();

            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            showNotification(context, intent);
        }
    }

    private void showNotification(Context context, Intent intent) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("알 림")
                        .setContentText("일어나세요!  이제 내리실 준비 하세요.");
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

    }
}
