package com.example.rdwifip2p;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final IntentFilter intentFilter = new IntentFilter();
    public WifiP2pManager.Channel channel;
    public WifiP2pManager manager;
    private WifiBroadcastReceiver receiver;
    private ArrayList<String> listAddressDevices;
    private ArrayList<WifiP2pDevice> listP2pDevices;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private TextView textViewDeviceAddress;
    public final static int DISCOVERY_STOPPED = 1;
    public final static int DISCOVERY_STARTED = 2;
    private int currentDiscoveryState = DISCOVERY_STOPPED;
    private Button buttonStartStopDiscovery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            initializeDiscovery();
        }
    }

    private void initializeDiscovery() {
        Button b = (Button)findViewById(R.id.buttonDiscover);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reDiscovery();
            }
        });
        buttonStartStopDiscovery = (Button) findViewById(R.id.buttonStartStop);
        buttonStartStopDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopDiscovery();
            }
        });
        textViewDeviceAddress = (TextView)findViewById(R.id.textViewDeviceAddress);

        listAddressDevices = new ArrayList<String>();
        listP2pDevices = new ArrayList<WifiP2pDevice>();
        listView = (ListView)findViewById(R.id.listViewDiscovery);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listAddressDevices);
        listView.setAdapter(adapter);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    @SuppressLint("MissingPermission")
    public void discovery(){
        if(currentDiscoveryState == DISCOVERY_STOPPED)
            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    currentDiscoveryState = DISCOVERY_STARTED;
                    System.out.println("discoverPeers Success");
                    buttonStartStopDiscovery.setText("STOP");
                }

                @Override
                public void onFailure(int reasonCode) {
                    System.out.println("discoverPeers Failure, reason : " + reasonCode);
                }
            });

    }

    public void startStopDiscovery(){
        if(currentDiscoveryState == DISCOVERY_STOPPED) discovery();
        if(currentDiscoveryState == DISCOVERY_STARTED) stopDiscovery(false);
    }

    public void stopDiscovery(boolean reDiscovery){
        if(currentDiscoveryState == DISCOVERY_STARTED)
            manager.stopPeerDiscovery(channel,new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    currentDiscoveryState = DISCOVERY_STOPPED;
                    System.out.println("stopPeerDiscovery Success");
                    buttonStartStopDiscovery.setText("START");
                    if(reDiscovery)
                        discovery();
                }

                @Override
                public void onFailure(int reasonCode) {
                    System.out.println("stopPeerDiscovery Failure, reason : " + reasonCode);
                }
            });

    }

    public void reDiscovery(){
        stopDiscovery(true);
    }

    public void setIsWifiP2pEnabled(boolean state){
        System.out.println("setIsWifiP2pEnabled() "+state);
    }

    public void setThisWifiP2pDevice(WifiP2pDevice device){
        System.out.println("setThisWifiP2pDevice() "+device.deviceName);
        textViewDeviceAddress.setText(device.deviceName+"\n"+device.deviceAddress);
    }

    public void updateWifiP2pDeviceList(WifiP2pDeviceList newPeersList){
        System.out.println("updateWifiP2pDeviceList " + newPeersList.getDeviceList().size());
        listAddressDevices.clear();
        listP2pDevices.clear();
        listP2pDevices.addAll(newPeersList.getDeviceList());
        Iterator<WifiP2pDevice> iterator = listP2pDevices.iterator();
        while(iterator.hasNext()){
            WifiP2pDevice p2pDevice = iterator.next();
            String name = p2pDevice.deviceName;
            String address = p2pDevice.deviceAddress;
            listAddressDevices.add(name+"\n"+address);
        }
        adapter.notifyDataSetChanged();
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WifiBroadcastReceiver(this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}