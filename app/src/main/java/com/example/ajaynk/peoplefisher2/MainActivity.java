package com.example.ajaynk.peoplefisher2;



import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.*;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import java.util.List;
import android.widget.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.text.SimpleDateFormat;

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
    static Collection<MSG1> messageList1= new ArrayList<MSG1>();
    static Collection<MSG2> messageList2 = new ArrayList<MSG2>();
    //static Collection<MSG3> messageList3 = new ArrayList<MSG3>();
    ListView MSG1ListView;
    ListView MSG2ListView;
    //ListView MSG3ListView;
    SendMessage sendMessage;
    String ownDeviceName = "survivor";
    Button Triangle;
    double lat1 = 20.5937, long1 = 78.9629, lat2 = 20.5937 ,long2 = 78.9629, lat3 =20.5937, long3 = 78.9629;//initializing with india value
    double distance1 = 100, distance2 = 100, distance3 = 100;
    boolean UsingTril = false;
    int counter  = 0;


    static boolean shouldSendMessage = false;



    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    String []  deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;
    static int falg =1 ;
    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;
    double LAT, LONG;

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
                shouldSendMessage = true;

                try {
                    Thread.sleep(10000);
                    Log.d("debug", "thread sleeping for 10000");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d("debug", "thread sleeping for 10000 failed ");

                }


                WifiP2pConfig config1 = new WifiP2pConfig();
                config1.deviceAddress = device.deviceAddress;
                config1.groupOwnerIntent = 15;Log.d("debug", "m Gonna be publish progress");
                publishProgress(config1);


                falg = 1;
                int count = 0;
                Log.d("debug","starting while loop");
                while (falg == 1 && count <= 20) {
                    try{

                        Thread.sleep(1000);
                        count++;

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
                case 4:
                    updateList1(messageList1);
                    break;
                case 5:
                    updateList2(messageList2);
                case 8:
                    updateLocation();
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
        Triangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(peers.size() >= 3)
                {
                      TrilMessage tril = new TrilMessage();
                      tril.execute(peers);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Not enough people, please stay calm", Toast.LENGTH_SHORT).show();

                }
            }
        }

        );
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
                shouldSendMessage = true;
                if(sendMessage != null)sendMessage.cancel(true);
                sendMessage = new SendMessage();
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
                    LAT = 20.5937;
                    LONG  = 78.9629;
                    Toast.makeText(getApplicationContext(),"GPS unable to get Value",Toast.LENGTH_SHORT).show();
                }else {
                    LAT = l.getLatitude();
                    LONG = l.getLongitude();
                    Toast.makeText(getApplicationContext(),"GPS Lat = "+LAT+"\n lon = "+LONG,Toast.LENGTH_SHORT).show();
                }
            }
        });
        MSG1ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int index = 0;
                MSG1 mob = null;
                for(MSG1 mobj : messageList1)
                {

                    if(index == position) mob = mobj;
                    index++;
                }
                if(mob != null) {
                    String msg = "1 " + mob.latitud + " " + mob.longitud;
                    writeMsg.setText(msg);
                }

            }
        });
        MSG2ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int index = 0;
                MSG2 mob = null;
                for(MSG2 mobj : messageList2)
                {

                    if(index == position) mob = mobj;
                    index++;
                }
                if(mob != null) {
                    String msg = "2 " + mob.message +" " + mob.date;
                    writeMsg.setText(msg);
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
        Triangle = (Button) findViewById(R.id.btnTril);
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager = (WifiP2pManager)  getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel,this);
        mintentFilter = new IntentFilter();
        mintentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mintentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mintentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mintentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        MSG1ListView = (ListView) findViewById(R.id.MSG1ListView);
        MSG2ListView = (ListView) findViewById(R.id.MSG2ListView);
 //       MSG3ListView = (ListView) findViewById(R.id.MSG3ListView);


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
                if(shouldSendMessage)
                {
                    sendReceive.write(message);
                    shouldSendMessage = false;
                }




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
                socket.connect(new InetSocketAddress(hostAdd, 4000), 5000);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
                if(shouldSendMessage)
                {
                    sendReceive.write(message);
                    shouldSendMessage = false;
                }
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

                        Log.i("message", "writing message");
                        float lon,lat;
                        String n= new String(buffer,"UTF-8");
                        int len = n.length();
                        int index;
                        int type = Character.getNumericValue(n.charAt(0));
                        char[] charr = new char[len];
                        charr = n.toCharArray();
                        for(int i=0;i< len;i++){
                            if(!Character.isDigit(n.charAt(i)) && !Character.isLetter(n.charAt(i)) && !Character.isWhitespace(n.charAt(i))){
                                len = i;
                                break;
                            }
                        }

                        //int  indexx =n.indexOf(null);
                        n= n.substring(0,len);
                        Log.d("checking len"," len is "+len+" ");
                        switch(type)
                        {
                            case 1:
                                Log.d("case 1", " 457");
                                int first = n.indexOf(' ');
                                int second = n.indexOf(' ',first+1);
                                int third = n.indexOf(' ', second + 1);
                                 double latitude = Double.parseDouble(n.substring(first+1, second));
                                double longitude= Double.parseDouble(n.substring(second+1, third));
                                int strength = Integer.parseInt(n.substring(third+1));
                                handler.obtainMessage(MESSAGE_READ, bytes, -1,buffer).sendToTarget();
                                this.write("4");
                                messageList1.add(new MSG1(latitude,longitude));
                                handler.obtainMessage(4, bytes, -1,buffer).sendToTarget();//update position list
                                if(UsingTril)
                                {
                                    switch(counter)
                                    {
                                        case 0:
                                            distance1 = 10 * Math.pow(strength, 0.5);
                                            lat1 = latitude;
                                            long1 = longitude;
                                            break;
                                        case 1:
                                            distance2 = 10 * Math.pow(strength, 0.5);
                                            lat2 = latitude;
                                            long2 = longitude;
                                            break;
                                        case 2:
                                            distance3 = 10*Math.pow(strength, 0.5);
                                            lat3 = latitude;
                                            long3 = longitude;
                                            break;
                                        default:
                                            break;
                                    }
                                }
                                try {
                                    Thread.sleep(2500);
                                }
                                catch(InterruptedException e)
                                {
                                    e.printStackTrace();
                                }

                                stopConnection();
                                break;



                            case 2:
                                Log.d("case 2", " 471");
                                handler.obtainMessage(MESSAGE_READ, bytes, -1,buffer).sendToTarget();
                                int firind = n.indexOf(' ');
                                //int secind = n.indexOf(' ',firind+1);
                                String mesg = n.substring(firind+1);
                                int ones= Integer.parseInt(n.substring(0,firind));
                                //int threes = Integer.parseInt(n.substring(secind+1,len));



                                messageList2.add(new MSG2(mesg,ones));
                                this.write("4");
                                Collections.sort( (ArrayList)messageList2 , new Comparator<MSG2>(){
                                    public int compare(MSG2 o1, MSG2 o2){
                                        return (int)(o1.date - o2.date);
                                    }
                                });
                                handler.obtainMessage(5, bytes, -1,buffer).sendToTarget();
                                try {
                                    Thread.sleep(2500);
                                }
                                catch(InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                                stopConnection();
                                break;

                            case 3:
                                Log.d("case 3", " 477");
                                handler.obtainMessage(MESSAGE_READ, bytes, -1,buffer).sendToTarget();
                                int STRENGTH = wifiManager.calculateSignalLevel(60, 10);
                                handler.obtainMessage(8, bytes, -1,buffer).sendToTarget();
                                this.write("1 "+ LAT + " "+ LONG+ " "+ STRENGTH);
                                try {
                                    Thread.sleep(1000);
                                }
                                catch(InterruptedException e)
                                {
                                    e.printStackTrace();
                                }

                                this.write("4");
                              //  messageList3.add(new MSG3(mesg1,ones1));
                                handler.obtainMessage(6, bytes, -1,buffer).sendToTarget();
                                if(serverClass != null)
                                {
                                    try {
                                        Thread.sleep(2500);
                                    }
                                    catch(InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                                stopConnection();
                                break;
                            case 4:
                                Log.d("case 4", "  482");
                                stopConnection();
                                break;
                            default:

                                Log.d("default ", " 487");
                                stopConnection();
                                break;
                        }










                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                    Log.i("messag" +
                            "e", "error reading");
                    stopConnection();
                }

            }
        }

        public void write(String str)
        {

            try{

                Log.i("message", "writing in output"+str);

                outputStream.write(str.getBytes());
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
                if(serverClass!= null)serverClass.serverSocket.close();
            }
            catch(IOException e) {

                e.printStackTrace();
                Log.d("error ","here");
            }
            if(serverClass!= null)serverClass.interrupt();
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
                Toast.makeText(that, "not connected! "+ reason, Toast.LENGTH_SHORT).show();
                falg = 0;
                stopConnection();

            }


        });
    }


    public class MSG2{
        public String message;
        public long date;

        public String name;
        public int type;

        public MSG2(String masg, int type)
        {
            this.message = masg;
            this.type = type;
            this.name=ownDeviceName;
            this.date = System.currentTimeMillis();;

        }
    }

    public class MSG1{
        public double longitud;
        public double latitud;
        public String name;
        public MSG1(double longitudee,double latitudee){
            this.longitud = longitudee;
            this.latitud= latitudee;
            this.name=ownDeviceName;
        }
    }

    /*public class MSG3{
        public String message3;
        public long date3;
        public float type3;
        public float type4;
        public int wifisig;
        public String name3;

        public MSG3(int type3)
        {
            this.message3 = masg3;
            this.type3 = type3;
            this.name3=ownDeviceName;
            this.

        }
    }*/

    public void updateList1(Collection<MSG1> MSG1List)
    {
        String[] messages = new String[MSG1List.size()];
        int index =0;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        for(MSG1 msg : MSG1List)
        {
            messages[index]="Device Name :  ";
            messages[index]+=msg.name;
            messages[index]+=" Time : ";
            messages[index]+= timeStamp;
            messages[index]+=" latitude :   ";
            messages[index]+= Double.toString(msg.latitud);
            messages[index]+= "  ";
            messages[index]+= "longitude : ";
            messages[index]+=Double.toString(msg.longitud);
            index++;

        }
        ArrayAdapter adapter =  new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,messages );
        MSG1ListView.setAdapter(adapter);
    }
    public void updateList2(Collection<MSG2> MSG2List)
    {


        String[] messy  =new String[MSG2List.size()];
        int index = 0;
        /*Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long passed = now - c.getTimeInMillis();*/
        long secondsPassed = System.currentTimeMillis();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
//Local time zone
        //SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        for (MSG2 msg:  MSG2List)
        {
            messy[index]="Device Name :  ";
            messy[index]+=msg.name+" ";
            messy[index]+=" Time : ";
            messy[index]+= timeStamp;
            messy[index]+="  Message :  ";
            messy[index] += msg.message;
            index++;
        }



        ArrayAdapter adapter =  new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,messy );
        MSG2ListView.setAdapter(adapter);
    }
    /*public void updateList3(Collection<MSG3> MSG3List)
    {
        String[] messy1  =new String[MSG3List.size()];
        int index = 0;
        long secondsPassed1 = System.currentTimeMillis();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        for (MSG3 msg:  MSG3List)
        {
            messy1[index]="Device Name :  ";
            messy1[index]+=msg.name3+" ";
            messy1[index]+=" Time : ";
            messy1[index]+= timeStamp;
            messy1[index]+="  Message :  ";
            messy1[index] += msg.message3;
            index++;
        }
        ArrayAdapter adapter =  new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,messy1 );
        MSG3ListView.setAdapter(adapter);


    }*/

    private  class TrilMessage extends AsyncTask<List<WifiP2pDevice>, WifiP2pConfig, Void>
    {
        @Override
        protected Void doInBackground(List<WifiP2pDevice>... lists) {
            falg = 1;
            UsingTril = true;
            counter = 0;
            devList.clear();
            for (WifiP2pDevice device : lists[0]) {
                devList.add(device);
            }
            for (WifiP2pDevice device : devList) {
                stopConnection();
                shouldSendMessage = true;
                message = "3";
                if(counter == 3) {
                    UsingTril = false;
                    counter = 0;
                    /*double xa,ya,za;
                    int R=6371;
                    xa = R*(Math.cos(Math.toRadians(lat1))*  Math.cos(Math.toRadians(long1)));
                    ya = R*(Math.cos(Math.toRadians(lat1))*  Math.sin(Math.toRadians(long1)));
                    za = R*(Math.sin(Math.toRadians(lat1));

                    double xb,yb,zb;
                    xb= R*(Math.cos(Math.toRadians(lat2))*  Math.cos(Math.toRadians(long2)));
                    yb=R*(Math.cos(Math.toRadians(lat2))*  Math.sin(Math.toRadians(long2)));
                    zb = R*(Math.sin(Math.toRadians(lat2));

                    double  xc,yc,zc;
                    xc = R*(Math.cos(Math.toRadians(lat3))*  Math.cos(Math.toRadians(long3)));
                    yc= R*(Math.cos(Math.toRadians(lat3))*  Math.sin(Math.toRadians(long3)));
                    zc = R*(Math.sin(Math.toRadians(lat3));*/

                     class TriangulationUtils
                    {
                        private static final double EARTH_RADIUS = 6371; // km

                        private static final int X = 0;
                        private static final int Y = 1;
                        private static final int Z = 2;

                        /**
                         * Calculate the position of the point by using the passed points position
                         * and strength of signal.
                         *
                         * The actual calculation is called trilateration:
                         * https://en.wikipedia.org/wiki/Trilateration
                         *
                         * Also few parts from:
                         * http://stackoverflow.com/questions/2813615/trilateration-using-3-latitude-and-longitude-points-and-3-distances
                         *
                         * @return the resulting point calculated.
                         */
                        public  double[] triangulation(double lat0, double lon0, double r0, double lat1, double lon1, double r1, double lat2, double lon2, double r2)
                        {
                            // Convert to cartesian
                            double[] p0 = latlon2cartesian(lat0, lon0);
                            double[] p1 = latlon2cartesian(lat1, lon1);
                            double[] p2 = latlon2cartesian(lat2, lon2);

                            // Convert so that p0 sits at (0,0)
                            double[] p0a = new double[]{0, 0, 0};
                            double[] p1a = new double[]{p1[X] - p0[X], p1[Y] - p0[Y], p1[Z] - p0[Z]};
                            double[] p2a = new double[]{p2[X] - p0[X], p2[Y] - p0[Y], p2[Z] - p0[Z]};

                            // All distances refers to p0, the origin
                            Double p1distance = distance(p0a, p1a);
                            if (p1distance == null)
                                return null;
                            Double p2distance = distance(p0a, p2a);
                            if (p2distance == null)
                                return null;

                            // unit vector of p1a
                            double[] p1a_ev = new double[]{p1a[X] / p1distance, p1a[Y] / p1distance, p1a[X] / p1distance};
                            // dot product of p1a_ev with p2a
                            double p2b_x = p1a_ev[X]*p2a[X] + p1a_ev[Y]*p2a[Y] + p1a_ev[Z]*p2a[Z];
                            // finding the y of p2b (for same distance of p2a from p0a)
                            double p2b_y = Math.sqrt(Math.abs(Math.pow(p2distance, 2) - Math.pow(p2b_x, 2)));

                            // Convert so that p1 stays on the x line (rotates the plane)
                            double[] p0b = new double[]{0, 0, 0};
                            double[] p1b = new double[]{p1distance, 0, 0};
                            double[] p2b = new double[]{p2b_x, p2b_y, 0};

                            double d = p1distance , i = p2b_x, j = p2b_y;

                            double x = (Math.pow(r0, 2) - Math.pow(r1, 2) + Math.pow(d, 2)) / (2*d);
                            double y = (Math.pow(r0, 2) - Math.pow(r2, 2) + Math.pow(i, 2) + Math.pow(j, 2)) / (2*j) - (i/j)*x;

                            double[] pb = new double[]{x, y, 0};
                            Double pbdistance = distance(p0b, pb);
                            if (pbdistance == null)
                                return null;

                            // Opposite operation done for converting points from coordinate system a to b
                            double pax = pb[X]/p1a_ev[X] + pb[Y]/p1a_ev[Y] + pb[Z]/p1a_ev[Z];
                            double[] pa = new double[]
                                    {
                                            pax,
                                            Math.sqrt(Math.abs(Math.pow(pbdistance, 2) - Math.pow(pax, 2))),
                                            0
                                    };

                            // Opposite operation done for converting points from coordinate system to a
                            double p[] = new double[]
                                    {
                                            pa[X] + p0[X],
                                            pa[Y] + p0[Y],
                                            pa[Z] + p0[Z]
                                    };

                            return cartesian2latlon(p[X], p[Y], p[Z]);
                        }

                        private double[ ]  latlon2cartesian(double lat, double lon)
                        {
                            return new double[ ]
                                    {
                                            Math.cos(lon) * Math.cos(lat) * EARTH_RADIUS,
                                            Math.sin(lon) * Math.cos(lat) * EARTH_RADIUS,
                                            Math.sin(lat) * EARTH_RADIUS
                                    };
                        }

                        private double[] cartesian2latlon(double x, double y, double z)
                        {
                            return new double[]
                                    {
                                            Math.atan(y/x),
                                            Math.acos(z/EARTH_RADIUS)
                                    };
                        }

                        private  Double distance(double[] p0, double[] p1)
                        {

                            if (p0.length != p1.length)
                                return null;

                            double val = 0;
                            for (int n = 0; n < p0.length; n++)
                                val += Math.pow(p1[n] - p0[n], 2);
                            return Math.sqrt(val);
                        }
                        public List<Object> bestOf(int num, Object... wifiSpots)
                        {
                            return null;
                        }
                    }

                    TriangulationUtils obj = new TriangulationUtils();
                     double [] db = new double[2];
                     db = obj.triangulation(lat1,long1,distance1,lat2,long2,distance2,lat3,long3,distance3);
                    LAT=db[1];
                    LONG = db[0];















                    break;
                }
                try {
                    Thread.sleep(10000);
                    Log.d("debug", "thread sleeping for 10000");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d("debug", "thread sleeping for 10000 failed ");

                }


                WifiP2pConfig config1 = new WifiP2pConfig();
                config1.deviceAddress = device.deviceAddress;
                config1.groupOwnerIntent = 15;
                Log.d("debug", "m Gonna be publish progress");
                publishProgress(config1);
                counter = (counter +1 )%4;

                falg = 1;
                int count = 0;
                Log.d("debug","starting while loop");
                while (falg == 1 && count <= 20) {
                    try{

                        Thread.sleep(1000);
                        count++;

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
    public void updateLocation()
    {

        GpsTracker gt = new GpsTracker(getApplicationContext());
        Location l = gt.getLocation();
        if( l == null){
            LAT = 20.5937;
            LONG  = 78.9629;
            Toast.makeText(getApplicationContext(),"GPS unable to get Value",Toast.LENGTH_SHORT).show();
        }else {
            LAT = l.getLatitude();
            LONG = l.getLongitude();
            Toast.makeText(getApplicationContext(),"GPS Lat = "+LAT+"\n lon = "+LONG,Toast.LENGTH_SHORT).show();
        }
    }


}
