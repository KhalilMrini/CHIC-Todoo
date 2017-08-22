package chic.khalil.chic;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Khalil on 21/05/17.
 */
public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int  mViewResourceId;

    DeviceArrayAdapterInterface callback;

    public DeviceListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices){
        super(context, tvResourceId,devices);
        this.mDevices = devices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mViewResourceId, null);

        BluetoothDevice device = mDevices.get(position);
        final String deviceName = device.getName();
        final String deviceAddress = device.getAddress();

        if (device != null) {

            final TextView deviceNameView = (TextView) convertView.findViewById(R.id.deviceName);
            final TextView deviceAddressView = (TextView) convertView.findViewById(R.id.deviceAddress);
            final LinearLayout deviceLayout = (LinearLayout) convertView.findViewById(R.id.deviceLayout);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null){
                        callback.wasClicked(deviceName, deviceAddress, position);
                        deviceLayout.setBackgroundColor(v.getResources().getColor(R.color.Blue1));
                        deviceNameView.setTextColor(v.getResources().getColor(R.color.White));
                        deviceAddressView.setTextColor(v.getResources().getColor(R.color.White));
                    }
                }
            };

            deviceNameView.setOnClickListener(listener);
            deviceAddressView.setOnClickListener(listener);

            if (deviceNameView != null) {
                deviceNameView.setText(deviceName);
            }
            if (deviceAddressView != null) {
                deviceAddressView.setText(deviceAddress);
            }
        }

        return convertView;
    }

    public void setCallback(DeviceArrayAdapterInterface callback){
        this.callback = callback;
    }

    public interface DeviceArrayAdapterInterface {

        void wasClicked(String deviceName, String deviceAddress, int i);

    }

}