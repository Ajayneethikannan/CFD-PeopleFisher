package com.example.ajaynk.peoplefisher2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.*;
import android.os.*;
import android.support.v4.app.ActivityCompat;
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
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btnOnOff, btnDiscover,  btnSend, btnLoc;
    ListView listView;
    TextView read_msg_box, connectionStatus;
    EditText writeMsg;
    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    MainActivity that = this;
    BroadcastReceiver mReceiver;
    IntentFilter mintentFilter;
    Calendar cal  = Calendar.getInstance();
    static String message;
    static List<WifiP2pDevice> devList = new ArrayList<WifiP2pDevice>();

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    String []  deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;
    static int falg =1 ;
    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main) ;
        initialWork();
        exqListener();
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
    }


    private  class SendMessage extends AsyncTask<List<WifiP2pDevice>, WifiP2pConfig, Void>
    {
        @Override
        protected Void doInBackground(List<WifiP2pDevice>... lists) {
            falg = 1;
            devList.clear();
            for (WifiP2pDevice device : lists[0]) {
                devList.add(device);
            }
            for (WifiP2pDevice device : devList) {
                stopConnection();
                try {
                    Thread.sleep(8000);
                    Log.d("debug", "thread sleeping for 8000");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d("debug", "thread sleeping for 8000 failed ");

                }


                final WifiP2pConfig config1 = new WifiP2pConfig();
                config1.deviceAddress = device.deviceAddress;
                config1.groupOwnerIntent = 15;Log.d("debug", "m Gonna be publish progrss");
                publishProgress(config1);


                falg = 1;
                Log.d("debug","starting while loop");
                while (falg == 1) {
                    try{
                        Thread.sleep(1000);

                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    Log.d("debug", "i am in while loop");

                }



            }
            return null;
        }

        @Override
        protected void onProgressUpdate(WifiP2pConfig... values) {
            super.onProgressUpdate(values);
            Connect(values[0]);
            Log.d("debug", "trying to connect(values[0]) ");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("debug", "trying to onPostExecute ");
            super.onPostExecute(aVoid);

        }
    }



    Handler handler = new Handler (new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg )
        {
            Log.d("debug", "about to inside switch of handleMessage");
            switch(msg.what)
            {

                case MESSAGE_READ:
                    byte[] readbuff = (byte[]) msg.obj;
                    String tempMsg = new String(readbuff, 0, msg.arg1);
                    read_msg_box.setText(tempMsg);
                    break;
                case 2:
                     Bundle data = msg.getData();
                    read_msg_box.setText(data.getString("name"));
                case 3:
                    stopConnection();


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


       btnSend.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               message = writeMsg.getText().toString();
               SendMessage sendMessage = new SendMessage();
               Log.d("debug", "trying to run sendMessage.execute(peers)");
               sendMessage.execute(peers);
           }
       });

        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GpsTracker gt = new GpsTracker(getApplicationContext());
                Location l = gt.getLocation();
                if( l == null){
                    Toast.makeText(getApplicationContext(),"GPS unable to get Value",Toast.LENGTH_SHORT).show();
                }else {
                    double lat = l.getLatitude();
                    double lon = l.getLongitude();
                    Toast.makeText(getApplicationContext(),"GPS Lat = "+lat+"\n lon = "+lon,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void initialWork()
    {

        btnLoc = (Button) findViewById(R.id.btnGetLoc);
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
                Toast.makeText(that,  peerList.getDeviceList().size() + "devices found", Toast.LENGTH_SHORT).show();

                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;
                Log.d("debug", "trying to iterate over the for loop for (WifiP2pDevice device : peerList.getDeviceList()) ");
                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;

                }

                Log.d("debug", "iteration over the for loop for (WifiP2pDevice device : peerList.getDeviceList()) end ");
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
                //Toast.makeText( that, "server address " + groupOwnerAddress,  Toast.LENGTH_SHORT).show();

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
                sendReceive.write(message);
                stopConnection();


            }
            catch( IOException e)
            {
                e.printStackTrace();

                try {
                    if(socket != null)
                    {socket.close();}
                }
                catch (IOException ev)
                {
                    ev.printStackTrace();
                }
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
                socket.connect(new InetSocketAddress(hostAdd, 4000), 100000);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                try {
                    socket.close();
                }
                catch (IOException ev)
                {
                    ev.printStackTrace();
                }
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
                        handler.obtainMessage(3, bytes, -1,buffer).sendToTarget();


                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                    Log.i("message", "error reading");
                    stopConnection();
                }

            }
        }

        public void write(String str)
        {

            try{

                Log.i("message", "writing in output"+str);

                outputStream.write(str.getBytes());
                Message message  = new Message();
                message.what = 2;
                message.getData().putString("name", str);
                handler.sendMessage(message);

                Log.i("message", "writing in output");


            }
            catch(IOException e)
            {
                e.printStackTrace();
                Log.i("message", "error in writing");
            }

        }
    }
public void stopConnection()
{
    if(sendReceive != null)
    {
        if(sendReceive.inputStream != null)
        {
            try{
                sendReceive.inputStream.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        if(sendReceive.outputStream!= null)
        {
            try{
                sendReceive.outputStream.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        sendReceive.socket = null;
        sendReceive.interrupt();
        sendReceive = null;
    }

    if(serverClass != null)
    {

        try {
            serverClass.serverSocket.close();
        }
        catch(IOException e) {

            e.printStackTrace();
            Log.d("error ","here");
        }
        serverClass.interrupt();
        serverClass = null;

    }

    if(clientClass != null)
    {
        try {
            clientClass.socket.close();
        }
        catch(IOException e) {

            e.printStackTrace();
        }
        clientClass.interrupt();
        clientClass = null;
    }


  mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
      @Override
      public void onSuccess() {
          Toast.makeText(getApplicationContext(), "removed group", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onFailure(int reason) {
          Toast.makeText(getApplicationContext(), "remove group fail", Toast.LENGTH_SHORT).show();
      }

  });
    falg = 0;

}

public void Connect(final WifiP2pConfig config)
{
    mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            Toast.makeText(that, "connected to  " + config.deviceAddress, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(int reason) {
            Toast.makeText(that, "not connected!", Toast.LENGTH_SHORT).show();

        }


    });
}



}
