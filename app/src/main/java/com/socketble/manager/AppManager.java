package com.socketble.manager;


import android.app.Application;

public class AppManager {

    private static AppManager mAppManager;

    private Application mHPlusApplication = null;

    public Application getApplication() {
        return mHPlusApplication;
    }

    public void setApplication(Application application) {
        mHPlusApplication = application;
    }

    private AppManager() {
    }

    public static AppManager getInstance() {
        if (mAppManager == null) {
            mAppManager = new AppManager();
        }
        return mAppManager;
    }

}
