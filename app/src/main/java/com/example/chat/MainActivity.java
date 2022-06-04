package com.example.chat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // MAC de HC-05 00:21:13:01:33:F9
    private final String DEVICE_ADDRESS = "00:21:13:01:33:F9";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    InputStream inputStream;
    BluetoothSocket socket;
    boolean stopThread;
    byte[] buffer;
    private BluetoothDevice device;
    LineGraphSeries<DataPoint> series;
    GraphView graph;
    double  y, x = 0;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv=findViewById(R.id.textView);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        graph = findViewById(R.id.graph);
        series = new LineGraphSeries<>();
        graph.removeAllSeries();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.isDiscovering();
        }
        if (BTinit()) {
            if (BTconnect()) {
                beginListenForData();
            }
        }
    }

    public boolean BTinit() {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Appareil non compatible Bluetooth", Toast.LENGTH_SHORT).show();
        }
        assert bluetoothAdapter != null;
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "S'il vous plaît, connectez votre appareil Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                if (iterator.getAddress().equals(DEVICE_ADDRESS)) {
                    device = iterator;
                    Log.d("Device", device.getName());
                    Log.d("Device", device.getAddress());
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    public boolean BTconnect() {
        boolean connected = true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
        return connected;
    }

    void beginListenForData() {
        Log.d("Thread Rec", "beginListenForData");
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && !stopThread) {
                try {
                    int byteCount = inputStream.available();
                    if (byteCount > 0) {
                        byte[] rawBytes = new byte[byteCount];
                        inputStream.read(rawBytes);
                        Thread.sleep(98);
                        final String string = new String(rawBytes, StandardCharsets.UTF_8);
                        runOnUiThread(() -> {
                            //données reçues : String
                            if(isNumeric(string)) {
                                tv.setText("Récéption des données");
                                tv.setTextColor(Color.GREEN);
                            Log.d("Received", string);

                            y = Float.parseFloat(string.split("\n")[0]);
                                series.appendData(new DataPoint(x++, y), true, 70);
                                graph.addSeries(series);
                                Viewport viewport = graph.getViewport();
                                viewport.setYAxisBoundsManual(true);
                                viewport.setMinY(-50);
                                viewport.setMaxY(1050);
                                viewport.setScrollable(true);
                                try {
                                    FileWriter csvWriter = new FileWriter(getExternalFilesDir(null) + "/ECG " + java.time.LocalDate.now() + ".csv", true);
                                    csvWriter.append(String.valueOf(y).trim());
                                    csvWriter.append(";");
                                    csvWriter.append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                                    csvWriter.append("\n");
                                    csvWriter.flush();
                                    csvWriter.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {
                                tv.setText("Pas de communication ou Electrodes détachés");
                                tv.setTextColor(Color.RED);
                            }
                        });
                    }
                } catch (IOException | InterruptedException ex) {
                    stopThread = true;
                }
            }

        }).start();
    }
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}




