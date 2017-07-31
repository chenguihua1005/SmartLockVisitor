package com.socketble.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;

import com.socketble.model.LightSensorModel;
import com.socketble.model.SensorSharedPreference;
import com.socketble.service.BluetoothLeService;
import com.socketble.util.LogUtil;
import com.socketble.util.Logger.Log;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class BLEManager {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private static BLEManager mBleManager;

    /**
     * the default BLUETOOTH Adapter.
     */
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeService mBluetoothLeService;

    /**
     * single instance
     *
     * @return
     */
    public static BLEManager getInstance() {
        if (mBleManager == null) {
            mBleManager = new BLEManager();
        }
        return mBleManager;
    }

    private BLEManager() {
    }

    /**
     * get the device is support ble or not
     *
     * @return
     */
    public boolean isSupportBLE() {
        return AppManager.getInstance().getApplication().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }


    /**
     * get the ble is enabled or not
     *
     * @return
     */
    public boolean isBLEEnable() {
        BluetoothManager bluetoothManager = (BluetoothManager) AppManager.getInstance().getApplication()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }


    /**
     * Get the default BLUETOOTH Adapter for this device.
     *
     * @return
     */
    public BluetoothAdapter getBluetoothAdapter() {
        if (mBluetoothAdapter == null) {
            final BluetoothManager bluetoothManager = (BluetoothManager) AppManager.getInstance().getApplication()
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        return mBluetoothAdapter;
    }

    /**
     * Stops an ongoing Bluetooth LE device scan.
     */
    public void stopBLEScan(BluetoothAdapter.LeScanCallback mLeScanCallback) {
        getBluetoothAdapter().stopLeScan(mLeScanCallback);
    }

    /**
     * Starts a scan for Bluetooth LE devices.
     *
     * @param mLeScanCallback
     */
    public void startBLEScan(BluetoothAdapter.LeScanCallback mLeScanCallback) {
        getBluetoothAdapter().startLeScan(mLeScanCallback);
    }

    public boolean isEnableBluetoothAdapter() {
        return getBluetoothAdapter().isEnabled();
    }

    public boolean isNeedShowOpenBleActivity() {
        return !isEnableBluetoothAdapter();
    }


    public void startBLEService(Context context) {
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            setBleServiceFromServiceConnected(service);
            if (!initBluetoothLeService()) {
                LogUtil.log(LogUtil.LogLevel.ERROR, TAG, "Unable to initialize Bluetooth");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (mBluetoothLeService != null) {
                mBluetoothLeService.unRegisterServiceBroadcast();
            }
        }
    };


    /**
     * @param service
     */
    public void setBleServiceFromServiceConnected(IBinder service) {
        mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
    }

    /**
     * connect BLE
     *
     * @param address
     * @return
     */
    public void connectBle(String address) {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(address);
        }
    }


    public void bondBle(BluetoothDevice device) {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.bondDevice(device);
        }
    }


    /**
     * disconnect ble service
     */

    public void disconnectAllDevices() {
        for (LightSensorModel device : SensorSharedPreference.getDeviceList()) {
            disconnectBle(device.getMacAddress());
        }
    }

    public void disconnectBle(String address) {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect(address);
        }
    }


    private void removeBond(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        try {
            LogUtil.log(LogUtil.LogLevel.ERROR, "removeBond", "Unpairing BT device " + device.getName() + "...");
            Class<?> btDeviceInstance = Class.forName(BluetoothDevice.class.getCanonicalName());
            Method removeBondMethod = btDeviceInstance.getMethod("removeBond");
            removeBondMethod.invoke(device);
        } catch (Throwable th) {
            LogUtil.log(LogUtil.LogLevel.ERROR, "removeBond", "Exception thrown");
            th.printStackTrace();
            return;
        }
        LogUtil.log(LogUtil.LogLevel.DEBUG, "removeBond", "Device " + device.getName() + " unpaired.");
    }

    public BluetoothDevice getBondDevice(String address) {
        Set<BluetoothDevice> devices = getBluetoothAdapter().getBondedDevices();
        if (devices != null && devices.size() > 0) {
            for (BluetoothDevice bluetoothDevice : devices) {
                if (bluetoothDevice.getAddress().equals(address)) {
                    return bluetoothDevice;
                }
            }
        }

        return null;
    }

    /**
     * reset ble service to null
     */
    public void resetBleService() {
        mBluetoothLeService = null;
    }

    /**
     * init ble service
     *
     * @return
     */
    public boolean initBluetoothLeService() {
        if (mBluetoothLeService != null && mBluetoothLeService.initialize()) {
            return true;
        }

        return false;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        return mBluetoothLeService.getConnectedDevices();
    }

    public BluetoothLeService getBluetoothLeService() {
        return mBluetoothLeService;
    }

    public void setBluetoothLeService(BluetoothLeService mBluetoothLeService) {
        this.mBluetoothLeService = mBluetoothLeService;
    }

    public void setNotification(String address, String serviceUuid, String characterUuid,
                                String descriptorUuid, boolean enable) {
        BluetoothGattService bluetoothGattService
                = mBluetoothLeService.getService(address, UUID.fromString(serviceUuid));

        if (bluetoothGattService != null) {
            BluetoothGattCharacteristic characteristic
                    = bluetoothGattService.getCharacteristic(UUID.fromString(characterUuid));

            mBluetoothLeService.setCharacteristicNotification(characteristic, descriptorUuid, address, enable);
        } else {
            LogUtil.log(LogUtil.LogLevel.ERROR, TAG, "bluetoothGattService is null");
        }
    }

    public void writeCharacteristic(String address, byte[] value, String serviceUuid, String characterUuid) {
        BluetoothGattService bluetoothGattService
                = mBluetoothLeService.getService(address, UUID.fromString(serviceUuid));
        if (bluetoothGattService != null) {
            BluetoothGattCharacteristic characteristic
                    = bluetoothGattService.getCharacteristic(UUID.fromString(characterUuid));
            if (characteristic == null) {
                Log.i(TAG, "characteristic is null");
            }

            mBluetoothLeService.writeCharacteristic(characteristic, address, value);
            Log.i(TAG, "writeCharacteristic:" + Arrays.toString(value));

        } else {
            Log.i(TAG, "bluetoothGattService is null");
            LogUtil.log(LogUtil.LogLevel.ERROR, TAG, "bluetoothGattService is null");
        }
    }

    public void readCharacteristic(String address, String serviceUuid, String characterUuid) {
        BluetoothGattService bluetoothGattService
                = mBluetoothLeService.getService(address, UUID.fromString(serviceUuid));

        if (bluetoothGattService != null) {
            BluetoothGattCharacteristic characteristic
                    = bluetoothGattService.getCharacteristic(UUID.fromString(characterUuid));
            mBluetoothLeService.readCharacteristic(characteristic, address);
        } else {
            LogUtil.log(LogUtil.LogLevel.INFO, TAG, "bluetoothGattService is null");
        }
    }

    public void pollCharacteristic(String address) {
        List<BluetoothGattService> services = mBluetoothLeService.getSupportedGattServices(address);
        for (BluetoothGattService service : services) {
            LogUtil.log(LogUtil.LogLevel.INFO, TAG, "s:" + service.getUuid());
            Log.i(TAG, "设备的service:" + service.getUuid());

            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                LogUtil.log(LogUtil.LogLevel.INFO, TAG, "c:" + characteristic.getUuid());
                Log.i(TAG, "设备的characteristic:" + service.getUuid());
            }
        }
    }


    public void unregisterBleService(Context context) {
        context.unbindService(mServiceConnection);

        if (mBluetoothLeService != null) {
            mBluetoothLeService.unRegisterServiceBroadcast();
        }
    }

}
