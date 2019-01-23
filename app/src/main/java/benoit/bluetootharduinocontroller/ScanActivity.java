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
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


/*
* ScanActivity
* Manage the research of Bluetooth devices
 */
public class ScanActivity extends AppCompatActivity {

    //identifier of the request to enable Bluetooth
    private static final int REQUEST_ENABLE_BT = 1;
    //identifier for the chosen device
    public static final String BLUETOOTH_DEVICE_SELECTED = "Device selection";

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> devices = new ArrayList<String>();
    private ArrayList<BluetoothDevice> list = new ArrayList();
    private ArrayAdapter<String> adapter;

    //UI components
    private ListView list_devices;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_scan);

        //if the user come back to the mainActivity without choosing a device
        setResult(Activity.RESULT_CANCELED);

        //get the UI components
        title = (TextView)findViewById(R.id.activity_scan_title);
        list_devices = (ListView)findViewById(R.id.activity_scan_device_list);
        list_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                //add the selected device to the result
                intent.putExtra(BLUETOOTH_DEVICE_SELECTED, list.get(position));
                for(BluetoothDevice d : list){
                    Log.d("DEBUG SCAN", d.getName() + " " + d.getAddress());
                }

                //send the data when the user choose a device
                setResult(ScanActivity.RESULT_OK, intent);
                finish();
            }
        });

        //setup the Bluetooth research
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_LONG);
            toast.show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                //Send a request box to enable Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {

                //Store the Bounded devices
                Log.d("Discover","Discover BOUNDED devices");
                Set<BluetoothDevice> bltPairedDevices = bluetoothAdapter.getBondedDevices();
                if (bltPairedDevices != null && bltPairedDevices.size() > 0) {
                    for (BluetoothDevice bltD : bltPairedDevices) {
                        list.add(bltD);
                        devices.add(bltD.getAddress() + " " + bltD.getName());;
                    }
                }
                discoverDevices();
            }
        }
    }


    /*
    * discoverDevices
    * Manage the research of the devices
     */
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
                if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d("Debug", "New device detected " + device.getName());
                    devices.add(device.getAddress() + " " + device.getName());
                    list.add(device);
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.d("Test", "Test");

                if(devices.size()==0){
                    Log.d("Debug", "Pas de devices");
                    title.setText("No devices available");
                }else{
                    title.setVisibility(View.INVISIBLE);
                }

                adapter = new ArrayAdapter<String>(ScanActivity.this, android.R.layout.simple_list_item_1, devices);
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
