package com.example.marco.wifidirectconnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

    private static MainActivity _instance=null;

    android.app.ProgressDialog ProgressDialog = null;
    Button btnClient;
    Button btnServer;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReceiver mReceiver;
    ServerBroadcastReceiver serverBroadcastReceiver;
    ClientBroadcastReceiver clientBroadcastReceiver;

    private final IntentFilter intentFilter = new IntentFilter();
    DeviceList DeviceListModel;
    ArrayAdapter<String> adapter;
    ArrayList<WifiP2pDevice> DeviceList = new ArrayList<>();
    ArrayList<String> DeviceListString = new ArrayList<>();
    ListView listView;

    DeviceType thisDeviceType;

    public enum DeviceType {
        CLIENT,
        SERVER
    }


    public static MainActivity Instance(){
        return _instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _instance=this;// singleton

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

        SetBtnSearchAsClient();
        SetBtnSearchAsServer();

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

                Connect(config);
            }
        });
    }



    private void SetBtnSearchAsClient() {
        btnClient = (Button) findViewById(R.id.button);
        btnClient.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                thisDeviceType=DeviceType.CLIENT;
                clientBroadcastReceiver= new ClientBroadcastReceiver(mManager, mChannel, MainActivity.Instance());
                DeviceList.clear();
                DeviceListString.clear();
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

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


    private void SetBtnSearchAsServer() {
        //in realtà non devo cercare, devo aspettare che qualcuno cerchi di connetersi a me e poi creare un gruppo
        //perciò mi rendo disponibile come server


        btnServer = (Button) findViewById(R.id.button2);

        btnServer.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                thisDeviceType=DeviceType.SERVER;
                serverBroadcastReceiver= new ServerBroadcastReceiver(mManager, mChannel,MainActivity.Instance()); //classe con i listener per cambiamento di peers
            }
        });

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info.groupFormed && info.isGroupOwner) {
            Toast.makeText(this, "SERVER", Toast.LENGTH_SHORT).show();
        } else if (info.groupFormed) {
            Toast.makeText(this, "CLIENT", Toast.LENGTH_SHORT).show();

            //CHIAMATA PERICOLOSA, NON HO IL CONTROLLO PIENO DI QUESTO METODO onConnectionInfoAvailable
            new FileReceiveAsync().execute();

        }
    }


    public void Connect(WifiP2pConfig config) {

        mManager.connect(mChannel,config, new WifiP2pManager.ActionListener() {

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


    public void NavigateToServerActivity(){
        Intent serverIntent= new Intent(MainActivity.Instance(),ServerActivity.class);
        startActivity(serverIntent);
    }

    public void NavigateToClientActivity(){
        Intent clientIntent= new Intent(MainActivity.Instance(),ClientActivity.class);
        startActivity(clientIntent);
    }
}
