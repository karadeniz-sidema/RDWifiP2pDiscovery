package com.example.rdwifip2p;

import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

public class WifiPeerListListener implements WifiP2pManager.PeerListListener {
    private MainActivity activity;

    public WifiPeerListListener(MainActivity act){
        activity = act;
    }
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        activity.updateWifiP2pDeviceList(peers);
    }
}
