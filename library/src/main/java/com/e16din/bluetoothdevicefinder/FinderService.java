package com.e16din.bluetoothdevicefinder;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FinderService extends Service {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;

    private Timer updateTimer = new Timer();
    private List<String> devices = new ArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();
        if (Debug.isDebuggerConnected()) {
            Debug.waitForDebugger();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bluetoothManager = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
        bluetoothAdapter = bluetoothManager.getAdapter();

        findBondedDevices();
        findOtherDevices();

        return super.onStartCommand(intent, flags, startId);
    }

    private void findOtherDevices() {
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    private void findBondedDevices() {
        devices.clear();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (bluetoothAdapter.isEnabled()) {
                    BluetoothDevice device = findDevice();
                    if (device != null) {
                        sendBroadcast(
                                new Intent(DeviceFoundReceiver.ACTION_FOUND_BONDED_DEVICE)
                                        .putExtra(DeviceFoundReceiver.KEY_DEVICE, device)
                        );
                    } else {
                        cancel();
                    }

                } else {
                    startActivity(
                            new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    );
                }
            }
        }, 0L, 100L);
    }

    private BluetoothDevice findDevice() {
        List<BluetoothDevice> connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        connectedDevices.addAll(bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER));
        connectedDevices.addAll(bluetoothAdapter.getBondedDevices());

        for (BluetoothDevice device : connectedDevices) {
            if (device == null) continue;

            String address = device.getAddress();
            if (device.getName() != null
                    && (!device.getName().isEmpty())
                    && (BluetoothAdapter.checkBluetoothAddress(address))) {

                if (!devices.contains(address)) {
                    devices.add(address);
                    return device;
                }
            }
        }

        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}