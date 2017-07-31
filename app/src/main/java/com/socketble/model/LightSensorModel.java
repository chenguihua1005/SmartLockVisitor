package com.socketble.model;



public class LightSensorModel {

//    public static final String SENSOR_DEFAULT_ADDRESS = "A0:E6:F8:B3:32:08";
    public static final String SENSOR_DEFAULT_ADDRESS = "A0:E6:F8:58:FE:58";

    private LightSensorStatus mStatus;

    private String mMacAddress;


    public String getMacAddress() {
        return mMacAddress;
    }

    public void setMacAddress(String mMacAddress) {
        this.mMacAddress = mMacAddress;
    }

    public LightSensorStatus getStatus() {
        return mStatus;
    }

    public void setStatus(LightSensorStatus mStatus) {
        this.mStatus = mStatus;
    }

}
