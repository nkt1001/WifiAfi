package com.digprom.nkt01.wifibooster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WifiBoosterView wifiBoosterView;
//    private ProgressBar progressBar;

    private TextView boost3;

    private Button boostBtn;

    private WifiManager wifiManager;
    private int currentBoostedLevel;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra(WifiBoostService.KEY_LEVEL)) {
                int level = intent.getIntExtra(WifiBoostService.KEY_LEVEL, 0);
                boolean isWorking = intent.getBooleanExtra(WifiBoostService.KEY_WORKING, false);

                if (level <= 0) level = NewLevelUtil.getRealLevel(context);
                initActivity(level, isWorking);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        wifiBoosterView = (WifiBoosterView) findViewById(R.id.imageView);
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        boost1 = (TextView) findViewById(R.id.textView2);
        boost3 = (TextView) findViewById(R.id.textView5);

        boostBtn = (Button) findViewById(R.id.button);

        wifiBoosterView.setListener(new WifiBoosterView.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {

                int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 100);

                Intent intent = new Intent(MainActivity.this, WifiBoostService.class);
                intent.putExtra(WifiBoostService.KEY_WORKING, wifiBoosterView.isEnabled());
                intent.putExtra(WifiBoostService.KEY_LEVEL, currentBoostedLevel);
                startService(intent);

                Log.d("TAG", "onAnimationEnd: " + wifiBoosterView.isEnabled());

                if (wifiBoosterView.isEnabled()) {
                    String result = String.format(Locale.getDefault(), getString(R.string.boosted), Integer.toString(currentBoostedLevel));
                    String info = String.format(Locale.getDefault(), getString(R.string.info), Integer.toString(currentBoostedLevel));
                    Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                    boost3.setText(info);

                    boostBtn.setText(getString(R.string.disable_boost));
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.boost_diasbled), Toast.LENGTH_SHORT).show();
                    boost3.setText(getString(R.string.boost_diasbled));
                    boostBtn.setText(getString(R.string.enable_boost));
                }

//                wifiBoosterView.setIsEnabled(!wifiBoosterView.isEnabled());
            }
        });

//        Intent callIntent = getIntent();
//        if (callIntent != null && callIntent.hasExtra(WifiBoostService.KEY_WORKING)) {
//            int level = callIntent.getIntExtra(WifiBoostService.KEY_LEVEL, 0);
//            boolean isWorking = callIntent.getBooleanExtra(WifiBoostService.KEY_WORKING, false);
//            initActivity(level, isWorking);
//        } else {
//            int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 100);
//            wifiBoosterView.setSignal(level);
//        }
    }

    private void initActivity(int level, boolean isWorking) {

        wifiBoosterView.setSignal(level);
        wifiBoosterView.setIsEnabled(isWorking);

        if (isWorking) {

            String result = String.format(Locale.getDefault(), getString(R.string.boosted), Integer.toString(level));
            String info = String.format(Locale.getDefault(), getString(R.string.info), Integer.toString(level));
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            boost3.setText(info);

            boostBtn.setText(getString(R.string.disable_boost));
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.boost_diasbled), Toast.LENGTH_SHORT).show();
            boost3.setText(getString(R.string.boost_diasbled));
            boostBtn.setText(getString(R.string.enable_boost));
        }
    }

    public void boost(View view) {
        if (wifiManager.isWifiEnabled()) {

            wifiBoosterView.setIsEnabled(!wifiBoosterView.isEnabled());

            if (wifiBoosterView.isEnabled()) {

                currentBoostedLevel = NewLevelUtil.getNewLevel(getApplicationContext());

                wifiBoosterView.startAnimation(currentBoostedLevel);
            } else {
                int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 100);
                wifiBoosterView.startAnimation(level);
            }
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.wifi_enable), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(WifiBoostService.INTENT_FILTER);
        registerReceiver(receiver, filter);

        startService(new Intent(this, WifiBoostService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }
}
