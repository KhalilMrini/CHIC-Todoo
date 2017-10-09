package chic.khalil.chic;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.test.SystemDefaults;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Khalil on 12/05/17.
 */
public class SettingsActivity extends IntentActivity implements DeviceListAdapter.DeviceArrayAdapterInterface {

    private static final String TAG = "SettingsActivity";

    UserDatabaseHelper userDb;
    TaskDatabaseHelper taskDb;

    Switch bluetoothSwitch;
    Button syncToWatch;
    Button discoverDevices;

    int numberOfBytes = 1;

    Handler mHandler = new Handler();

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    public DeviceListAdapter mDeviceListAdapter;

    private BluetoothAdapter mBluetoothAdapter;

    ListView lvNewDevices;

    Switch visualCountdown;
    Switch numericCountdown;
    Switch iconSwitch;

    private BluetoothGattCharacteristic chosenCharacteristic = null;
    private BluetoothGattService chosenService = null;

    int sendIndex = 0;
    boolean isSending = false;
    byte[] msg;

    Runnable task = new Runnable() {
        @Override
        public void run() {
            if (sendIndex >= msg.length) {
                // Finished sending
                Log.d(TAG, "Sending accomplished.");
                isSending = false;
                sendIndex = 0;
            } else if (!(chosenCharacteristic.setValue(combine(msg, sendIndex, numberOfBytes)))) {
                Log.e(TAG, "Could not set the value locally for " + sendIndex);
                send(10);
            } else {
                if (!mBluetoothLeService.write(chosenCharacteristic)) {
                    Log.e(TAG, "Could set the value locally but not on BLE for " + sendIndex);
                    send(10);
                } else {
                    Toast.makeText(SettingsActivity.this, "Sync Completed!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Sent the following byte: " + chosenCharacteristic.getStringValue(0));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layout = R.layout.activity_settings;
        super.onCreate(savedInstanceState);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userDb = new UserDatabaseHelper(this);
        taskDb = new TaskDatabaseHelper(this);

        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        visualCountdown = (Switch) findViewById(R.id.visualCountdown);
        numericCountdown = (Switch) findViewById(R.id.numericCountdown);
        iconSwitch = (Switch) findViewById(R.id.textIconSwitch);

        visualCountdown.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userDb.updateSettings(email, userDb.COL_3, isChecked);
            }
        });

        numericCountdown.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userDb.updateSettings(email, userDb.COL_4, isChecked);
            }
        });

        iconSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userDb.updateSettings(email, userDb.COL_5, isChecked);
            }
        });

        Cursor cursor = userDb.query(email);
        cursor.moveToFirst();
        visualCountdown.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(userDb.COL_3)) == 1);
        numericCountdown.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(userDb.COL_4)) == 1);
        iconSwitch.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(userDb.COL_5)) == 1);

        bluetoothSwitch = (Switch) findViewById(R.id.bluetoothSwitch);
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !mBluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "Enabling Bluetooth");
                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBTIntent);
                    discoverDevices.setEnabled(true);
                }
                if (!isChecked && mBluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "Disabling Bluetooth");
                    mBluetoothAdapter.disable();
                    discoverDevices.setEnabled(false);
                }
            }
        });
        bluetoothSwitch.setChecked(mBluetoothAdapter.isEnabled());

        syncToWatch = (Button) findViewById(R.id.syncButton);
        syncToWatch.setEnabled(false);
        discoverDevices = (Button) findViewById(R.id.discoverDevices);
        discoverDevices.setEnabled(mBluetoothAdapter.isEnabled());

        mBTDevices = new ArrayList<>();

        lvNewDevices = (ListView) findViewById(R.id.deviceList);

        syncToWatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startConnection();
                /*
                // Parameters
                byte[] parameters = {(byte) ((numericCountdown.isChecked() ? 4 : 0) + (visualCountdown.isChecked() ? 2 : 0) + (iconSwitch.isChecked() ? 1 : 0))};

                // Current time
                Date now = new Date();
                byte[] currentTime = {(byte) now.getHours(), (byte) now.getMinutes(), (byte) now.getSeconds()};

                // Day Plans
                String[] days = getResources().getStringArray(R.array.days);
                ArrayList<Byte> dayPlans = new ArrayList<Byte>();
                for (int i = 0; i < days.length; i++){
                    dayPlans.add((byte) i);
                    Cursor cursor = taskDb.query(email, child, getResources().getString(R.string.init_day)+days[i]);
                    int count = cursor.getCount();
                    dayPlans.add((byte) count);
                    for (int j = 0; j < count; j++){
                        cursor.moveToNext();

                        String startTime = cursor.getString(cursor.getColumnIndex(taskDb.COL_6));
                        dayPlans.add((byte) Integer.parseInt(startTime.substring(0,2)));
                        dayPlans.add((byte) Integer.parseInt(startTime.substring(3,5)));

                        String endTime = cursor.getString(cursor.getColumnIndex(taskDb.COL_7));
                        dayPlans.add((byte) Integer.parseInt(endTime.substring(0,2)));
                        dayPlans.add((byte) Integer.parseInt(endTime.substring(3,5)));

                        if (iconSwitch.isChecked()){
                            // Text case
                            String text = cursor.getString(cursor.getColumnIndex(taskDb.COL_5));
                            // TODO: Check size for padding
                            for (byte b: ByteBuffer.allocate(50).put(text.getBytes()).array()){
                                dayPlans.add(b);
                            }
                        } else {
                            // Image case
                            String filePath = cursor.getString(cursor.getColumnIndex(taskDb.COL_8));
                            Bitmap image = BitmapFactory.decodeFile(filePath);
                            if (image != null){
                                Bitmap selectedImage = Bitmap.createScaledBitmap(image, IMAGE_SIZE, IMAGE_SIZE, false);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                byte[] imageBytes = stream.toByteArray();
                                dayPlans.add((byte) 1);
                                for (byte b: imageBytes){
                                    dayPlans.add(b);
                                }
                            } else {
                                dayPlans.add((byte) 0);
                            }
                        }
                    }
                }

                int size = dayPlans.size();
                byte[] dayPlansArray = new byte[size];
                for (int i = 0; i < size; i++){
                    dayPlansArray[i] = dayPlans.get(i).byteValue();
                }
                
                byte[] bytes = combine(parameters, currentTime, dayPlansArray);
                */
                /*String[] days = getResources().getStringArray(R.array.days);
                ArrayList<Byte> dayPlans = new ArrayList<Byte>();
                for (int i = 0; i < days.length; i++) {
                    dayPlans.add((byte) i);
                    Cursor cursor = taskDb.query(email, child, getResources().getString(R.string.init_day) + days[i]);
                    int count = cursor.getCount();
                    dayPlans.add((byte) count);
                    for (int j = 0; j < count; j++) {
                        cursor.moveToNext();
                        String filePath = cursor.getString(cursor.getColumnIndex(taskDb.COL_8));
                        Bitmap image = BitmapFactory.decodeFile(filePath);
                        if (image != null) {
                            Bitmap selectedImage = Bitmap.createScaledBitmap(image, IMAGE_SIZE, IMAGE_SIZE, false);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] imageBytes = stream.toByteArray();
                        }
                    }
                }*/
                Cursor cursor = taskDb.query(email, child, getResources().getString(R.string.init_day) + "MONDAY");
                cursor.moveToFirst();
                String filePath = cursor.getString(cursor.getColumnIndex(taskDb.COL_8));
                Bitmap image = BitmapFactory.decodeFile(filePath).copy(Bitmap.Config.RGB_565, false);
                int bytes = image.getByteCount();
                ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
                image.copyPixelsToBuffer(buffer);
                msg = buffer.array();

                isSending = true;
                send(0);
            }
        });

        discoverDevices.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scanLeDevice(true);
            }
        });

    }

    public byte[] combine(byte[]... values){
        byte[] result = {};
        for (byte[] b: values){
            result = (byte[]) ArrayUtils.addAll(result, b);
        }
        return result;
    }

    public byte[] combine(byte[] values, int sendIndex, int number){
        byte[] result = {};
        for (int  i = 0; i < number; i++){
            if (sendIndex + i < values.length)
                result = ArrayUtils.addAll(result, new byte[]{values[sendIndex+i]});
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(SettingsActivity.this, MenuActivity.class);
        i.putExtra("email", email);
        i.putExtra("child", child);
        startActivity(i);
        SettingsActivity.this.finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    String mDeviceName;
    String mDeviceAddress;

    @Override
    public void wasClicked(String deviceName, String deviceAddress, int i){
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        mDeviceName = deviceName;
        mDeviceAddress = deviceAddress;

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                            if (!mBTDevices.contains(device)) {
                                mBTDevices.add(device);
                                mDeviceListAdapter = new DeviceListAdapter(SettingsActivity.this, R.layout.device_item, mBTDevices);
                                mDeviceListAdapter.setCallback(SettingsActivity.this);
                                lvNewDevices.setAdapter(mDeviceListAdapter);
                            }
                        }
                    });
                }
            };

    // ================================== CONNECTING TO BLUETOOTH SERVICE ================================== //

    private BluetoothLeService mBluetoothLeService;

    private final UUID SERVICE_UUID = UUID.fromString("497e3f64-2806-11e7-93ae-92361f002671");
    private final UUID CHARACTERISTIC_UUID = UUID.fromString("497e41e4-2806-11e7-93ae-92361f002671");

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.e(TAG, "Service Disconnected");
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                // Connected
                Log.d(TAG, "Connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                // Disconnected
                Log.d(TAG, "Disonnected");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.d(TAG, "Found Services");
                List<BluetoothGattService> services = mBluetoothLeService.getSupportedGattServices();
                int index = 0;
                while (index < services.size() && !services.get(index).getUuid().equals(SERVICE_UUID)) {
                    index++;
                }
                if (index < services.size()){
                    Log.d(TAG, "Found the Right Service");
                    chosenService = services.get(index);
                    List<BluetoothGattCharacteristic> characteristics = chosenService.getCharacteristics();
                    int cIndex = 0;
                    while (cIndex < characteristics.size() && !characteristics.get(cIndex).getUuid().equals(CHARACTERISTIC_UUID)){
                        cIndex ++;
                    }
                    if (cIndex < characteristics.size()){
                        Log.d(TAG, "Found the Right Characteristic");
                        chosenCharacteristic = characteristics.get(cIndex);
                        syncToWatch.setEnabled(true);
                        Toast.makeText(SettingsActivity.this, "You are now connected to the watch "+ mDeviceName,
                                Toast.LENGTH_LONG).show();
                        if (isSending){
                            send(0);
                        }
                    } else {
                        Log.e(TAG, "Could not find the Right Characteristic");
                    }
                } else {
                    Log.e(TAG, "Could not find the Right Service");
                }
            } else if (BluetoothLeService.CHARACTERISTIC_WRITTEN.equals(action)) {
                sendNext(0);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.CHARACTERISTIC_WRITTEN);
        return intentFilter;
    }

    private void send(int delay){
        mHandler.postDelayed(task, delay);
    }

    private void sendNext(int delay){
        sendIndex += numberOfBytes;
        send(delay);
    }
}
