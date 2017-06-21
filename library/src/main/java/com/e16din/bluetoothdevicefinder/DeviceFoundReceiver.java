package com.e16din.bluetoothdevicefinder;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceFoundReceiver extends BroadcastReceiver {

    public static final String ACTION_FOUND_BONDED_DEVICE = BuildConfig.APPLICATION_ID + ".action.FOUND_BONDED_DEVICE";

    public static final String KEY_DEVICE = BuildConfig.APPLICATION_ID + ".extra.DEVICE";

    private OnFoundListener onFoundListener;


    public DeviceFoundReceiver(OnFoundListener listener) {
        super();
        this.onFoundListener = listener;
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        final BluetoothDevice device;
        switch (intent.getAction()) {
            case ACTION_FOUND_BONDED_DEVICE:
                device = intent.getParcelableExtra(KEY_DEVICE);
                onFoundListener.onFound(device);
                break;
            case BluetoothDevice.ACTION_FOUND:
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                onFoundListener.onFound(device);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                BluetoothAdapter.getDefaultAdapter().startDiscovery();
                break;
        }
    }

    interface OnFoundListener {
        void onFound(BluetoothDevice device);
    }
}
