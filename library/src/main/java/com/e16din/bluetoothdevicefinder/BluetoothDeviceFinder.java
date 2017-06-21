package com.e16din.bluetoothdevicefinder;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BluetoothDeviceFinder {

    public static class HOLDER {
        public static final BluetoothDeviceFinder instance = new BluetoothDeviceFinder();
    }

    public static BluetoothDeviceFinder init(OnFoundListener listener, String... wantedNames) {
        if (listener == null) throw new NullPointerException("OnFoundListener must not be null");

        final BluetoothDeviceFinder instance = HOLDER.instance;

        instance.onFoundListener = listener;
        instance.wantedNames = wantedNames;

        return instance;
    }

    public static BluetoothDeviceFinder init(OnFoundListener listener) {
        return BluetoothDeviceFinder.init(listener, "");
    }

    public static BluetoothDeviceFinder instance() {
        return HOLDER.instance;
    }

    private OnFoundListener onFoundListener;
    private String[] wantedNames;

    private final BroadcastReceiver receiver = new DeviceFoundReceiver(new DeviceFoundReceiver.OnFoundListener() {
        @Override
        public void onFound(BluetoothDevice device) {
            if (device != null && checkName(device.getName())) {
                Log.i("debug", "found device: " + device.getName());
                if (onFoundListener != null) {
                    onFoundListener.onFound(device);
                }
            }
        }
    });

    private boolean checkName(String deviceName) {
        if (wantedNames == null || wantedNames.length == 0) return true;

        if (deviceName == null) return false;

        for (String wantedName : wantedNames) {
            if (wantedName == null) continue;

            if (deviceName.toLowerCase().startsWith(wantedName.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public void onResume(Context context) {
        context.registerReceiver(receiver, new IntentFilter() {{
            addAction(DeviceFoundReceiver.ACTION_FOUND_BONDED_DEVICE);
            addAction(BluetoothDevice.ACTION_FOUND);
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        }});
        context.startService(new Intent(context, FinderService.class));
    }

    public void onPause(Context context) {
        context.unregisterReceiver(receiver);
        context.stopService(new Intent(context, FinderService.class));
    }

    public interface OnFoundListener {
        void onFound(BluetoothDevice device);
    }
}
