package com.socketble;

import android.app.Application;

import com.socketble.manager.AppManager;


public class SmartLockApplication extends Application {
    /** Instance of the current application. */
    private static SmartLockApplication mLightSensorApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mLightSensorApplication = this;
        AppManager.getInstance().setApplication(mLightSensorApplication);
    }


}
