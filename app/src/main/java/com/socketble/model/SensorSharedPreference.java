package com.socketble.model;


import com.google.gson.Gson;
import com.socketble.util.SharedPreferenceUtil;
import com.socketble.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SensorSharedPreference {

    private static final String PREFERENCE_FILE_NAME = "light_sensor_model_sp";
    private static final String PREFERENCE_SENSOR_KEY = "light_sensor_model_key";
    private static final String PREFERENCE_A1_KEY = "a1_key";
    private static final String PREFERENCE_A2_KEY = "a2_key";
    private static final String PREFERENCE_A3_KEY = "a3_key";
    private static final String PREFERENCE_N1_KEY = "n1_key";
    private static final String PREFERENCE_N2_KEY = "n2_key";
    private static final String PREFERENCE_N3_KEY = "n3_key";


    public static void addDevice(LightSensorModel newDevice) {

        List<LightSensorModel> devices = getDeviceList();

        if (devices.size() == 0) {
            devices.add(newDevice);

        } else {
            // do not add same device
            LightSensorModel oldDevice = getDevice(newDevice.getMacAddress(), devices);
            if (oldDevice == null)
                devices.add(newDevice);
        }

        saveDevices(devices);
    }

    public static void deleteDevice(String macAddress) {
        List<LightSensorModel> devices = getDeviceList();
        LightSensorModel device = getDevice(macAddress, devices);
        if (device != null) {
            devices.remove(device);
            saveDevices(devices);
        }
    }

    public static boolean hasDevice(String macAddress) {
        List<LightSensorModel> devices = SensorSharedPreference.getDeviceList();
        LightSensorModel device = getDevice(macAddress, devices);

        return device != null;
    }


    public static void saveStatus(String macAddress, LightSensorStatus status) {
        List<LightSensorModel> devices = getDeviceList();
        LightSensorModel device = getDevice(macAddress, devices);
        if (device != null) {
            device.setStatus(status);
            saveDevices(devices);
        }
    }

    public static List<LightSensorModel> getDeviceList() {
        JSONArray responseArray;
        List<LightSensorModel> results = new ArrayList<>();

        String record = SharedPreferenceUtil.getPrefString(PREFERENCE_FILE_NAME, PREFERENCE_SENSOR_KEY, null);

        if (StringUtil.isEmpty(record)) {
            return results;
        }

        try {
            responseArray = new JSONArray(record);

            for (int i = 0; i < responseArray.length(); i++) {
                JSONObject responseJSON = responseArray.getJSONObject(i);
                LightSensorModel model = new Gson().fromJson(responseJSON.toString(), LightSensorModel.class);
                results.add(model);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return results;
        }

        return results;
    }

    public static void clearData() {
        SharedPreferenceUtil.clearPreference(SharedPreferenceUtil
                .getSharedPreferenceInstanceByName(PREFERENCE_FILE_NAME));
    }

    public static LightSensorModel getDevice(String address) {
        List<LightSensorModel> devices = getDeviceList();

        for (LightSensorModel device : devices) {
            if (device.getMacAddress().equals(address))
                return device;
        }

        return null;
    }

    private static LightSensorModel getDevice(String address, List<LightSensorModel> devices) {

        if (devices != null) {
            for (LightSensorModel device : devices) {
                if (device.getMacAddress().equals(address))
                    return device;
            }
        }

        return null;
    }

    private static void saveDevices(List<LightSensorModel> devices) {
        SharedPreferenceUtil.setPrefString(PREFERENCE_FILE_NAME, PREFERENCE_SENSOR_KEY,
                new Gson().toJson(devices));
    }

}
