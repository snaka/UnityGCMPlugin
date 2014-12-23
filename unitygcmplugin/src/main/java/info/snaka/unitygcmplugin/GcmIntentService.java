package info.snaka.unitygcmplugin;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * 受け取った通知内容をステータスバーの通知領域に出力するための IntentService
 */
public class GcmIntentService extends IntentService {
    private final String TAG = "GcmIntentService";

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager m_notificationManager;
    private NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }


    /**
     * 受け取った通知の内容を通知領域にメッセージとして表示
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        // メッセージを出力
        if (!extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            String received = extras.getString("message", "");
            Log.d(TAG, "***** message: " + received);
            setNotification(extras.getString("message", ""));
        }

        // 通知が完了したら終了する
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


    private void setNotification(String msg) {
        m_notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, CustomUnityPlayerActivity.class), 0);

        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(getResources().getIdentifier("notify", "drawable", getPackageName()))
                .setContentTitle("GCM Notification")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setTicker("msg:" + msg)
                .setAutoCancel(true);

        builder.setContentIntent(contentIntent);
        m_notificationManager.notify(NOTIFICATION_ID, builder.build());

    }
}
