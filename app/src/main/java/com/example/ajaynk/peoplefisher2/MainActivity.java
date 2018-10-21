package com.example.ajaynk.peoplefisher2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.*;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btnOnOff, btnDiscover,  btnSend;
    ListView listView;
    TextView read_msg_box, connectionStatus;
    EditText writeMsg;
    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    MainActivity that = this;
    BroadcastReceiver mReceiver;
    IntentFilter mintentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String []  deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main) ;
        initialWork();
        exqListener();
    }



    Handler handler = new Handler (new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg )
        {
            switch(msg.what)
            {
                case MESSAGE_READ:
                    byte[] readbuff = (byte[]) msg.obj;
                    String tempMsg = new String(readbuff, 0, msg.arg1);
                    read_msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private void exqListener()  {
        btnOnOff.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(wifiManager.isWifiEnabled())
                {
                    wifiManager.setWifiEnabled(false);
                    btnOnOff.setText("ON");
                }
                else
                {
                    wifiManager.setWifiEnabled(true);
                    btnOnOff.setText("OFF");
                }
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {


                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                            connectionStatus.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int reason) {
                            connectionStatus.setText("Connection Failed"+ reason);
                    }
                });
            }

        });
       listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
       {
           @Override
           public void onItemClick(AdapterView<?>  adapterView, View view, int i, long l )
           {
               final WifiP2pDevice device = deviceArray[i];
               WifiP2pConfig config = new WifiP2pConfig();
               config.deviceAddress = device.deviceAddress;
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                       Toast.makeText(that, "tada "+device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                       Toast.makeText(that, "not connected!", Toast.LENGTH_SHORT).show();

                    }
                });
           }
       });

       btnSend.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
                 String msg = writeMsg.getText().toString();
               Toast.makeText(that, "msg = "+ msg, Toast.LENGTH_SHORT).show();
                 sendReceive.write(msg.getBytes());

           }
       });
    }
    private void initialWork()
    {
        btnOnOff = (Button)findViewById(R.id.onOff);
        btnDiscover = (Button)findViewById(R.id.discover);
        btnSend = (Button)findViewById(R.id.sendButton);
        listView = (ListView) findViewById(R.id.peerListView);
        read_msg_box = (TextView) findViewById(R.id.readMsg);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        writeMsg = (EditText) findViewById(R.id.writeMsg);

        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager = (WifiP2pManager)  getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel,this);
        mintentFilter = new IntentFilter();
        mintentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mintentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mintentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mintentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    WifiP2pManager.PeerListListener peerListListener =  new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList  peerList) {
            if(!peerList.getDeviceList().equals(peers)) {
                Toast.makeText(that, "Wifi 1!" + peerList.getDeviceList().size(), Toast.LENGTH_SHORT).show();

                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    Toast.makeText(that, device.deviceAddress, Toast.LENGTH_SHORT).show();
                    deviceArray[index] = device;
                    index++;

                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);
            }
            if(peers.size() == 0)
                {
                    Toast.makeText( that, "No peers found , please stay calm", Toast.LENGTH_SHORT).show();
                }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener(){
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo)
        {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
            {
                connectionStatus.setText("Host");
                serverClass = new ServerClass();
                serverClass.start();
                Toast.makeText( that, "server class started", Toast.LENGTH_SHORT).show();

            }
            else
            {
                connectionStatus.setText("Client");

                clientClass = new ClientClass(groupOwnerAddress);
                Toast.makeText( that, "server address " + groupOwnerAddress,  Toast.LENGTH_SHORT).show();

                clientClass.start();
                Toast.makeText( that, "client class started", Toast.LENGTH_SHORT).show();


            }
        }

    } ;



    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mReceiver, mintentFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;


        @Override
        public void run()
        {
            try{
                serverSocket = new ServerSocket(4000);
                socket = serverSocket.accept();

                sendReceive = new SendReceive(socket);
                sendReceive.start();
            }
            catch( IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;

        public ClientClass(InetAddress hostAddress)
        {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();


        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 4000), 5000);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }



    }


    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt)
        {
            socket = skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                Log.i("message", "got streams");

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            while( socket!=null)
            {
                try{
                    bytes = inputStream.read(buffer);

                    if(bytes>0)
                    {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1,buffer).sendToTarget();
                        Log.i("message", "writing message");

                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                    Log.i("message", "error reading");
                }

            }
        }

        public void write(byte[] bytes)
        {

            try{
                outputStream.write(bytes);
                Log.i("message", "writing in output");


            }
            catch(IOException e)
            {
                e.printStackTrace();
                Log.i("message", "error in writing");
            }

        }
    }






}
