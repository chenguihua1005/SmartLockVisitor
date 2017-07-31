package com.socketble.controler;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.socketble.R;
import com.socketble.manager.AutoConnectManager;
import com.socketble.manager.BLEManager;
import com.socketble.manager.SensorBleDataManager;
import com.socketble.model.BleUuidKey;
import com.socketble.model.LightSensorModel;
import com.socketble.model.LightSensorStatus;
import com.socketble.model.SensorSharedPreference;
import com.socketble.view.QuitDialogFragment;
import com.websocketclient.OnWebSocketListener;
import com.websocketclient.WebSocketClient;
import com.websocketclient.WebSocketMessage;

import java.util.regex.Pattern;

/*
 * Created by JiangJie on 2017/2/13.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnWebSocketListener {

    private TextView tv;
    private Button btn3;
    private WebSocketClient client;
    private Gson gson = new Gson();

//    private Button mBreakDoorButton;
//    private Button mOpenDoorButton;
//    private Button mLockDoorButton;
    private Button mConfirmButton;
    private EditText mMacIdEditText;
    private TextView mStatusTextView;
    private ToggleButton mToggleButton;
    private int count = 0;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        client = new WebSocketClient(this, "ws://10.78.116.122:8080/phone/connect");
        client = new WebSocketClient(this, "ws://115.159.225.195:18080/phone/connect");

        handler.postDelayed(new Runnable(){
            public void run() {
                //execute the task
                client.connect();
            }
        }, 1000);


        if (SensorSharedPreference.getDeviceList().size() > 0) {
            SensorSharedPreference.saveStatus(SensorSharedPreference.getDeviceList().get(0).getMacAddress(),
                    LightSensorStatus.DISCONNECT);
        } else {
            SensorBleDataManager.getInstance().initSensor(LightSensorModel.SENSOR_DEFAULT_ADDRESS);
        }

        initViews();

        startBleService();


        AutoConnectManager.getInstance().startAutoConnectThread();
        SensorBleDataManager.getInstance().setDeviceStatusListener(new SensorBleDataManager.DeviceStatusListener() {
            @Override
            public void onChange(String macAddress, LightSensorStatus status) {
                if (status == LightSensorStatus.CONNECT) {
                    mStatusTextView.setText("connected");
                } else if (status == LightSensorStatus.DISCONNECT) {
                    mStatusTextView.setText("connecting...");
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        quitApp();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getRepeatCount() == 0)) {
            QuitDialogFragment dialog = new QuitDialogFragment();
            dialog.show(getFragmentManager(), "QuitDialogFragment");
            dialog.setListener(new QuitDialogFragment.NoticeDialogListener() {
                @Override
                public void onDialogPositiveClick() {
                    quitApp();

                }
            });
        }
        return false;
    }

    private void startBleService() {
        BLEManager.getInstance().startBLEService(MainActivity.this);
        SensorBleDataManager.getInstance().registerBleReceiver();
    }

    private void quitApp() {
        for (LightSensorModel device : SensorSharedPreference.getDeviceList()) {
            SensorSharedPreference.saveStatus(device.getMacAddress(), LightSensorStatus.DISCONNECT);
        }

        BLEManager.getInstance().disconnectAllDevices();
        BLEManager.getInstance().unregisterBleService(MainActivity.this);
        SensorBleDataManager.getInstance().unregisterBleReceiver();

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }


    private void initViews() {
        //tv = (TextView) findViewById(R.id.textView);
        btn3 = (Button) findViewById(R.id.button3);
        btn3.setOnClickListener(this);
        client.setOnWebSocketListener(this);
        mStatusTextView = (TextView) findViewById(R.id.status_tv);

        mToggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 当按钮第一次被点击时候响应的事件
                if (mToggleButton.isChecked()) {
                    //开门间隔，3s
                    mToggleButton.setEnabled(false);
                    handler.postDelayed(new Runnable(){
                        public void run() {
                            //execute the task
                            mToggleButton.setEnabled(true);
                        }
                    }, 3000);

                    count++;
                    Toast.makeText(MainActivity.this, "door opened", Toast.LENGTH_SHORT).show();
                    if (SensorSharedPreference.getDeviceList().size() > 0) {
                        BLEManager.getInstance().writeCharacteristic(SensorSharedPreference.getDeviceList().get(0).getMacAddress(),
                                new byte[]{0x02}, BleUuidKey.MAD_AIR_SERVICE, BleUuidKey.MAD_AIR_RW_CHARACTER);
                    }
                }
                // 当按钮再次被点击时候响应的事件
                else {
                    count++;
                    Toast.makeText(MainActivity.this, "door closed", Toast.LENGTH_SHORT).show();
                    if (SensorSharedPreference.getDeviceList().size() > 0) {
                        BLEManager.getInstance().writeCharacteristic(SensorSharedPreference.getDeviceList().get(0).getMacAddress(),
                                new byte[]{0x01}, BleUuidKey.MAD_AIR_SERVICE, BleUuidKey.MAD_AIR_RW_CHARACTER);
                    }
                }
                //开关一次，发通知给主人
                //申请一次，只能开关一次。再次开，需要在此申请
                if(2 == count) {
                    WebSocketMessage message = new WebSocketMessage("1100");
                    String jsonStr = gson.toJson(message);
                    client.sendMessage(jsonStr);
                    mToggleButton.setEnabled(false);
                    count = 0;
                }
            }
        });



//        mBreakDoorButton = (Button) findViewById(R.id.break_btn);
//        mBreakDoorButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (SensorSharedPreference.getDeviceList().size() > 0) {
//                    BLEManager.getInstance().writeCharacteristic(SensorSharedPreference.getDeviceList().get(0).getMacAddress(),
//                            new byte[]{0x00}, BleUuidKey.MAD_AIR_SERVICE, BleUuidKey.MAD_AIR_RW_CHARACTER);
//                }
//            }
//        });
//        mLockDoorButton = (Button) findViewById(R.id.lock_btn);
//        mLockDoorButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (SensorSharedPreference.getDeviceList().size() > 0) {
//                    BLEManager.getInstance().writeCharacteristic(SensorSharedPreference.getDeviceList().get(0).getMacAddress(),
//                            new byte[]{0x01}, BleUuidKey.MAD_AIR_SERVICE, BleUuidKey.MAD_AIR_RW_CHARACTER);
//                }
//            }
//        });
//        mOpenDoorButton = (Button) findViewById(R.id.open_btn);
//        mOpenDoorButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (SensorSharedPreference.getDeviceList().size() > 0) {
//                    BLEManager.getInstance().writeCharacteristic(SensorSharedPreference.getDeviceList().get(0).getMacAddress(),
//                            new byte[]{0x02}, BleUuidKey.MAD_AIR_SERVICE, BleUuidKey.MAD_AIR_RW_CHARACTER);
//                }
//            }
//        });
        //hide this button till authorized
//        mBreakDoorButton.setVisibility(View.INVISIBLE);
//        mLockDoorButton.setVisibility(View.INVISIBLE);
//        mOpenDoorButton.setVisibility(View.INVISIBLE);
        mToggleButton.setEnabled(false);
//        tv.setVisibility(View.INVISIBLE);

        mMacIdEditText = (EditText) findViewById(R.id.mac_et);
        if (SensorSharedPreference.getDeviceList().size() > 0) {
            mMacIdEditText.setText(SensorSharedPreference.getDeviceList().get(0).getMacAddress());
        }
        mConfirmButton = (Button) findViewById(R.id.confirm_bt);
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patternMac="^[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}$";
                if(Pattern.compile(patternMac).matcher(mMacIdEditText.getText().toString()).find()){
                    Toast.makeText(MainActivity.this, "MacId:" + mMacIdEditText.getText().toString(),
                            Toast.LENGTH_SHORT).show();
                    SensorBleDataManager.getInstance().initSensor(mMacIdEditText.getText().toString());
                } else {
                    Toast.makeText(MainActivity.this, "MacId不合法", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.button) {
//            client.connect();
//        }
//        if (v.getId() == R.id.button2) {
//            client.disconnect();
//        }
        if (v.getId() == R.id.button3) {
            WebSocketMessage message = new WebSocketMessage("1000");
            String jsonStr = gson.toJson(message);
            client.sendMessage(jsonStr);
        }
    }

    @Override
    public void onResponse(String response) {
//        tv.setText(response);
        if(response.contains("2000")) {
            showOneNormalDialog("Your request was approved");
            //display this button since authorized
//            mBreakDoorButton.setVisibility(View.VISIBLE);
//            mLockDoorButton.setVisibility(View.VISIBLE);
//            mOpenDoorButton.setVisibility(View.VISIBLE);
            mToggleButton.setEnabled(true);
        }else if(response.contains("3000")) {
            showOneNormalDialog("Your request was rejected");
            //hide this button till authorized
//            mBreakDoorButton.setVisibility(View.INVISIBLE);
//            mLockDoorButton.setVisibility(View.INVISIBLE);
//            mOpenDoorButton.setVisibility(View.INVISIBLE);
            mToggleButton.setEnabled(false);
        }
    }

    @Override
    public void onConnected() {
        Toast.makeText(this, "Websocket connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectError(String e) {
        Toast.makeText(this, "Websocket error occurred：" + e, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisConnected() {
        Toast.makeText(this, "Websocket disconnected", Toast.LENGTH_SHORT).show();
    }

    private void showOneNormalDialog(String message){

        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("消息");
        normalDialog.setMessage(message);
        normalDialog.setPositiveButton("确定", null);
        normalDialog.show();
    }
}
