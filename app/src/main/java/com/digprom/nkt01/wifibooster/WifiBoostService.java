package com.digprom.nkt01.wifibooster;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.Locale;

public class WifiBoostService extends Service {

    public static final String INTENT_FILTER = "com.digprom.nkt01.wifibooster.WifiBoostService";

    public static final String KEY_WORKING = "KEY_WORKING";
    public static final String KEY_LEVEL = "KEY_LEVEL";
    private final int mId = 101;
    private boolean isWorking;
    private int level;

    public WifiBoostService() {
        isWorking = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.hasExtra(KEY_WORKING)) {
                Log.d("TAG", "onStartCommand: working");
                isWorking = intent.getBooleanExtra(KEY_WORKING, false);
                level = intent.getIntExtra(KEY_LEVEL, 0);
            } else {
                Log.d("TAG", "onStartCommand: not working");
                Intent resultIntent = new Intent(INTENT_FILTER);
                resultIntent.putExtra(KEY_WORKING, isWorking)
                        .putExtra(KEY_LEVEL, level);
                sendBroadcast(resultIntent);
            }

            showNotification(isWorking, level);

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification(boolean isWorking, int level) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(KEY_WORKING, isWorking)
                .putExtra(KEY_LEVEL, level);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent, 0);

        Intent serviceIntent = null;
        PendingIntent servicePending;

        if (isWorking) {
            serviceIntent = new Intent(getApplicationContext(), WifiBoostService.class);
            serviceIntent.putExtra(KEY_WORKING, false)
                    .putExtra(KEY_LEVEL, level);
            servicePending = PendingIntent.getService(this, 1, serviceIntent, 0);
        } else  {
            serviceIntent = new Intent(getApplicationContext(), MainActivity.class);
            serviceIntent.putExtra(KEY_WORKING, true)
                    .putExtra(KEY_LEVEL, level);
            servicePending = PendingIntent.getActivity(this, 1, serviceIntent, 0);
        }

        String actionString = isWorking ? getString(R.string.disable_boost) : getString(R.string.enable_boost);
        int iconRes = isWorking ? R.mipmap.ic_wifi_green : R.mipmap.ic_wifi_black;

        NotificationCompat.Action action = new NotificationCompat.Action(iconRes, actionString, servicePending);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String contentText = isWorking ?
                String.format(Locale.getDefault(), getString(R.string.info), Integer.toString(level)) :
                getString(R.string.boost_diasbled);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(contentText)
                        .setContentIntent(resultPendingIntent)
                        .addAction(action);


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(mId, mBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
