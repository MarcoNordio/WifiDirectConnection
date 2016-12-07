package com.example.marco.wifidirectconnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener {

    android.app.ProgressDialog ProgressDialog = null;
    Button button;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReceiver mReceiver;
    private final IntentFilter intentFilter = new IntentFilter();
    DeviceList DeviceListModel;
    ArrayAdapter<String> adapter;
    ArrayList<WifiP2pDevice> DeviceList = new ArrayList<>();
    ArrayList<String> DeviceListString = new ArrayList<>();
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);


        DeviceListModel= new DeviceList(this);

        listView= (ListView) findViewById(R.id.listView);
        adapter=new ArrayAdapter<>(getApplicationContext(),R.layout.row,R.id.textViewList,DeviceListString);

        listView.setAdapter(adapter);

        SetBtnSearch();
        SetDeviceListClick();
    }


    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentFilter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void SetDeviceListClick() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                final WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = DeviceList.get(position).deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (ProgressDialog != null && ProgressDialog.isShowing()) {
                    ProgressDialog.dismiss();
                }

                AlertDialog.Builder miaAlert = new AlertDialog.Builder(MainActivity.this);
                miaAlert.setMessage("collegarsi come server?");
                miaAlert.setTitle("Server");

                miaAlert.setCancelable(false);
                miaAlert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        config.groupOwnerIntent=15;
                        Connect(config);
                    }
                });

                miaAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        config.groupOwnerIntent=0;
                        Connect(config);
                    }
                });

                AlertDialog alert = miaAlert.create();
                alert.show();


                //Connect(config);
            }
        });
    }



    private void SetBtnSearch() {
        button = (Button) findViewById(R.id.button);
        DeviceList.clear();
        DeviceListString.clear();
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "scan started", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MainActivity.this, "scan failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info.groupFormed && info.isGroupOwner) {
            Toast.makeText(this, "SERVER", Toast.LENGTH_SHORT).show();
        } else if (info.groupFormed) {
            Toast.makeText(this, "CLIENT", Toast.LENGTH_SHORT).show();
        }
    }


    public void Connect(WifiP2pConfig config) {
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Connect failed. Retry.",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
