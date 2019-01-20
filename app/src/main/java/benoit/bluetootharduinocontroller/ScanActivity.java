package benoit.bluetootharduinocontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class ScanActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> devices = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private ListView list_devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_scan);

        setResult(Activity.RESULT_CANCELED);

        list_devices = (ListView)findViewById(R.id.activity_scan_device_list);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Log.d("Start of the App", "Start");
        if (bluetoothAdapter == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_LONG);
            toast.show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                //Store the Bounded devices
                Log.d("Discover","Discover BOUNDED devices");
                Set<BluetoothDevice> bltPairedDevices = bluetoothAdapter.getBondedDevices();
                if (bltPairedDevices != null && bltPairedDevices.size() > 0) {
                    for (BluetoothDevice bltD : bltPairedDevices) {
                        devices.add(bltD.getAddress() + " " + bltD.getName());
                    }
                }
                discoverDevices();
            }
        }
    }

    private void discoverDevices(){
        Log.d("Discover","Discover the devices");
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }else{
            bluetoothAdapter.startDiscovery();
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Discover","Start of the discovering");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState()!= BluetoothDevice.BOND_BONDED){
                    Log.d("Debug", "New device detected " + device.getName());
                    devices.add(device.getAddress() + " " + device.getName());
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.d("Test", "Test");
                adapter = new ArrayAdapter<String>(ScanActivity.this, R.layout.activity_scan,R.id.activity_scan_device_list, devices);
                list_devices.setAdapter(adapter);
                for(String s : devices){
                    Log.d("Print devices", s);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
