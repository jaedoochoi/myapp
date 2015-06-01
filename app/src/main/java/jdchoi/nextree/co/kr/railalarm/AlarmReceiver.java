package jdchoi.nextree.co.kr.railalarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Administrator on 2015-05-29.
 */
public class AlarmReceiver extends BroadcastReceiver{
    private static final String TAG = "AlarmReceiver";
    private static final int NOTI_ID = 5840;

    @Override
    public void onReceive(Context context, Intent intent) {
        //도착 알림시 Notification실행.
        showNotification(context, intent);
    }

    private void showNotification(Context context, Intent intent) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle("알 림")
                        .setContentText("일어나세요!  이제 내리실 준비 하세요.");
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        //내장사운드 출력함.//
        mBuilder.setSound(Uri.parse(new StringBuilder()
                .append("android.resource://")
                .append(context.getPackageName()).append("/")
                .append(R.raw.subway_transfer).toString()));
        mBuilder.setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTI_ID, mBuilder.build());

    }
}
