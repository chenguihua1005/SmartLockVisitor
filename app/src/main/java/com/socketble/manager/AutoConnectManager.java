package com.socketble.manager;

import com.socketble.model.LightSensorModel;
import com.socketble.model.LightSensorStatus;
import com.socketble.model.SensorSharedPreference;
import com.socketble.util.LogUtil;
import com.socketble.util.Logger.Log;

import java.util.ArrayList;
import java.util.List;


public class AutoConnectManager {

    private static final String TAG = "AutoConnectManager";

    private static AutoConnectManager mAutoConnectManager;

    private static final int BLE_AUTO_CONNECT_POLLING_INTERVAL = 5000;

    private List<LightSensorModel> mNeedAutoConnectDevices = new ArrayList<>();


    private AutoConnectManager() {
    }

    public static AutoConnectManager getInstance() {
        if (mAutoConnectManager == null) {
            mAutoConnectManager = new AutoConnectManager();
        }
        return mAutoConnectManager;
    }

    public void startAutoConnectThread() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {

                    sleep(BLE_AUTO_CONNECT_POLLING_INTERVAL);

                    checkBluetooth();

                    if (hasAutoConnectDevices()) {

                        for (LightSensorModel device : mNeedAutoConnectDevices) {

                            LogUtil.log(LogUtil.LogLevel.DEBUG, TAG, "connecting sensor...");
                            Log.i(TAG, "connect " + device.getMacAddress());

                            BLEManager.getInstance().connectBle(device.getMacAddress());
                        }
                    } else {
                        LogUtil.log(LogUtil.LogLevel.DEBUG, TAG, "Sensor is connected!");
                    }
                }
            }
        }).start();

    }

    private boolean hasAutoConnectDevices() {

        mNeedAutoConnectDevices.clear();

        List<LightSensorModel> devices = SensorSharedPreference.getDeviceList();

        for (LightSensorModel device : devices) {
            if (device.getStatus() == LightSensorStatus.DISCONNECT)
                mNeedAutoConnectDevices.add(device);
        }

        return mNeedAutoConnectDevices.size() > 0;
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkBluetooth() {
        if (!BLEManager.getInstance().isBLEEnable()) {
            LogUtil.log(LogUtil.LogLevel.DEBUG, TAG, "Phone's bluetooth is disabled!");
        }
    }

}
