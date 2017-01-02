package com.digprom.nkt01.wifibooster;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.util.Random;

/**
 * Created by nkt01 on 02.01.2017.
 */

public class NewLevelUtil {

    public static int getNewLevel(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 100);
        return level + new Random().nextInt(100 - level);
    }

    public static int getRealLevel(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 100);
    }
}
