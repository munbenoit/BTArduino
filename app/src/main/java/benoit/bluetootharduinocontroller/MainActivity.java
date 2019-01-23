package benoit.bluetootharduinocontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //identifier of the ScanActivity
    public static final int SCAN_ACTIVITY_REQUEST_CODE = 1;
    //identifier of the App
    private UUID MY_UUID = UUID.fromString("f9ef9c53-fede-45fc-ade7-8900215c7342");
    private static final byte[] ON = "1".getBytes();
    private static final byte[] OFF = "0".getBytes();
    //UI components
    private Button onBtn;
    private Button offBtn;
    private Button scanBtn;


    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice selected_device;
    private BluetoothSocket socket = null;
    private ConnectionThread connection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup of the layout environment
        onBtn = (Button) findViewById(R.id.activity_main_on_button);
        onBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ON", "onClick: ON");
                if(bluetoothAdapter!=null && connection!=null){
                    Log.d("ON", "onClick: ON");
                    connection.write(ON);
                }
            }
        });
        offBtn = (Button) findViewById(R.id.activity_main_off_button);
        offBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("OFF", "onClick: OFF");
                if(bluetoothAdapter!=null && connection!=null){
                    Log.d("OFF", "onClick: OFF");
                    connection.write(OFF);
                }
            }
        });
        scanBtn = (Button) findViewById(R.id.activity_main_scan_button);

        //Add an event
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scanActivity = new Intent(MainActivity.this, ScanActivity.class);
                startActivityForResult(scanActivity, SCAN_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Manage the connection with ScanActivity
        if(SCAN_ACTIVITY_REQUEST_CODE == requestCode && RESULT_OK==resultCode){
            //Data received from ScanActivity
            Bundle b = data.getExtras();
            selected_device = b.getParcelable(ScanActivity.BLUETOOTH_DEVICE_SELECTED);
            Log.d("SELECTED DEVICE",selected_device.getName());
            //Start a new connection with a device
            connection = new ConnectionThread(selected_device);
            connection.run();

        }
    }



    /*
    * ConnectionThread
    * Manage the connection with the Bluetooth device
     */
    private class ConnectionThread extends Thread{
        private final BluetoothSocket Blt_socket;
        private final BluetoothDevice Blt_device;
        private OutputStream outputStream;

        public ConnectionThread(BluetoothDevice device){
            BluetoothSocket tmp = null;
            Blt_device = device;
            try{
                Method m = Blt_device.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
                tmp = (BluetoothSocket) m.invoke(Blt_device, 1);
            }catch (NoSuchMethodException e){
                Log.d("DEBUG","No method");
            }catch (IllegalAccessException e){
                Log.d("DEBUG", "Not allowed");
            }catch (InvocationTargetException e){
                Log.d("DEBUG", "Not allowed");
            }

            Blt_socket = tmp;
        }

        public void run(){
            OutputStream tmp_output = null;
            Log.d("Thread run", "Thread running");
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try{
                Blt_socket.connect();
                Toast notify = Toast.makeText(getApplicationContext(), "Connection to "+ Blt_device.getName() + "established", Toast.LENGTH_SHORT);
                notify.show();
                tmp_output = Blt_socket.getOutputStream();
            }catch (IOException connectException){
                try {
                    Blt_socket.close();
                }catch (IOException closeException){
                    Log.e("ERROR CLOSE CONNECTION", "Couldn't close socket", closeException);
                }
                Log.e("ERROR OPEN CONNECTION", connectException.getMessage(), connectException);
                return;
            }
            outputStream = tmp_output;
        }

        public void write(byte[] data){
            try{
                outputStream.write(data);
            }catch (IOException e){

            }
        }

        public void cancel(){
            Log.d("Socket cancel", "Socket cancelled");
            try {
                Blt_socket.close();
            }catch (IOException closeException){
                Log.e("ERROR CLOSE CONNECTION", "Couldn't close socket", closeException);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
