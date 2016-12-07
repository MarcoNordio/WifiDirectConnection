package com.example.marco.wifidirectconnection;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import java.util.Collection;


/**
 * Created by Marco on 30/11/16.
 */

public class DeviceList implements WifiP2pManager.PeerListListener {

    private MainActivity _mainActivity;

    public DeviceList(MainActivity a){
        _mainActivity = a;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) { //listener che viene attivato quando ho dei peers disponibili
        if(peers!= null && !peers.getDeviceList().isEmpty()) {
            Collection<WifiP2pDevice> list = peers.getDeviceList();
            _mainActivity.DeviceList.clear();
            _mainActivity.DeviceList.addAll(list);

            _mainActivity.DeviceListString.clear();
            for (WifiP2pDevice elem : peers.getDeviceList()) {
                _mainActivity.DeviceListString.add(elem.deviceName);
            }

            _mainActivity.listView.setAdapter(_mainActivity.adapter);
            _mainActivity.adapter.notifyDataSetChanged();
        }else{
            Toast.makeText(_mainActivity, "NO DEVICE FOUND", Toast.LENGTH_SHORT).show();
        }
    }
}
