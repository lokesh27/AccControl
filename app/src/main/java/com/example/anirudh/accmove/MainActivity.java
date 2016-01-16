package com.example.anirudh.accmove;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    static Button connectButtonNorth, leftClickButtonWest, rightClickButtonEast, keyboardOnOffButton, startStopButton, arrowUp, arrowDown, arrowLeft, arrowRight;
    static ZanyEditText editTextTemporary;
    SensorManager mSensorManager;
    Sensor senGyroscope;

    // port for UDP = 12001
    // port for TCP = 12002
    static int port1 = 0;
    static int port2 = 0;
    static String globalIP="";
    static String temp;

    static DatagramSocket sock;
    static Socket sockTCP;

    static boolean send =false;
    static boolean sendKey=false;
    static boolean sendTCP=false;

    static DataOutputStream dos;

    static int clickSend=0;

    static NotificationManager notificationManager;
    static Notification notificationStarMouse,notificationKeyboard;
    /*
    1 = Left Click Down
    2 = Left Click Up
    3 = Right Click Down
    4 = Right Click Up
     */

    public static float gx = 0.0f;
    public static float gy = 0.0f;
    public static float gz = 0.0f;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try{

            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationStarMouse = new Notification.Builder(this).setContentTitle("AccControl Mouse").setContentText("Touch to Pause").setSmallIcon(R.drawable.abc_btn_rating_star_on_mtrl_alpha)
                    .setOngoing(true).build();
            Intent intent1 = new Intent(this, Main2Activity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent1, Intent.FILL_IN_ACTION);
            notificationStarMouse.
            notificationKeyboard = new Notification.Builder(this).setContentTitle("AccControl Keyboard").setContentText("Keyboard in use")
                    .setSmallIcon(R.drawable.abc_spinner_textfield_background_material).setOngoing(true).build();

            connectButtonNorth= (Button) findViewById(R.id.connectButtonNorth);
            leftClickButtonWest= (Button) findViewById(R.id.leftButtonClickWest);
            rightClickButtonEast= (Button) findViewById(R.id.rightButtonClickEast);

            leftClickButtonWest.setVisibility(View.INVISIBLE);
            rightClickButtonEast.setVisibility(View.INVISIBLE);

            editTextTemporary = (ZanyEditText) findViewById(R.id.editTextTemporary);
            editTextTemporary.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    new Asynch2(s).execute();
                    Asynch2.temp=true;

                    //MainActivity.editTextTemporary.setText("");
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            editTextTemporary.setSingleLine();
            editTextTemporary.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    Log.i("key_new", "kuch hua!!!");
                    new Asynch2("enter@99").execute();
                    Asynch2.temp=true;
                    Asynch2.temp2=true;
                    //MainActivity.editTextTemporary.requestFocus();
                    //MainActivity.editTextTemporary.setText("");
                    return false;
                }
            });
            /*editTextTemporary.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_DEL){
                        Log.i("key","delete");
                    }
                    return false;
                }
            });*/
            //editTextTemporary.setVisibility(View.INVISIBLE);



            leftClickButtonWest.setOnTouchListener(leftClickButtonWestListener);
            rightClickButtonEast.setOnTouchListener(rightClickButtonEastListener);

            connectButtonNorth.setOnClickListener(connectButtonNorthListener);


            startStopButton= (Button) findViewById(R.id.startStopButton);
            startStopButton.setVisibility(View.INVISIBLE);
            startStopButton.setOnClickListener(startStopButtonListener);

            keyboardOnOffButton = (Button) findViewById(R.id.keyboardOnOffButton);
            keyboardOnOffButton.setVisibility(View.INVISIBLE);
            keyboardOnOffButton.setOnClickListener(keyboardOnOffButtonListener);

            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            senGyroscope=mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mSensorManager.registerListener(this,senGyroscope,1);

            arrowUp = (Button) findViewById(R.id.buttonUP);
            arrowUp.setOnTouchListener(arrowUpTouchListener);

            arrowDown = (Button) findViewById(R.id.buttonDOWN);
            arrowDown.setOnTouchListener(arrowDownTouchListener);

            arrowLeft = (Button) findViewById(R.id.buttonLEFT);
            arrowLeft.setOnTouchListener(arrowLeftTouchListener);

            arrowRight = (Button) findViewById(R.id.buttonRIGHT);
            arrowRight.setOnTouchListener(arrowRightTouchListener);

            arrowUp.setVisibility(View.INVISIBLE);
            arrowDown.setVisibility(View.INVISIBLE);
            arrowLeft.setVisibility(View.INVISIBLE);
            arrowRight.setVisibility(View.INVISIBLE);

        }
        catch (Exception e){
            Log.i("hello",e.toString());
        }

    }

    View.OnTouchListener arrowUpTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                new Asynch2("up_press@99").execute();
                Asynch2.temp=true;
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                new Asynch2("up_unpress@99").execute();
                Asynch2.temp=true;
            }
            return false;
        }
    };

    View.OnTouchListener arrowDownTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                new Asynch2("down_press@99").execute();
                Asynch2.temp=true;
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                new Asynch2("down_unpress@99").execute();
                Asynch2.temp=true;
            }
            return false;
        }
    };

    View.OnTouchListener arrowLeftTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                new Asynch2("left_press@99").execute();
                Asynch2.temp=true;
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                new Asynch2("left_unpress@99").execute();
                Asynch2.temp=true;
            }
            return false;
        }
    };

    View.OnTouchListener arrowRightTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                new Asynch2("right_press@99").execute();
                Asynch2.temp=true;
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                new Asynch2("right_unpress@99").execute();
                Asynch2.temp=true;
            }
            return false;
        }
    };


    View.OnClickListener keyboardOnOffButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(sendKey==false){
                sendKey=true;
                editTextTemporary.requestFocus();
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                notificationManager.notify(234,notificationKeyboard);
            }
            else{
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                sendKey=false;
                notificationManager.cancel(234);
            }
        }
    };


    View.OnClickListener startStopButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(MainActivity.send==true){
                MainActivity.send=false;
                notificationManager.cancel(123);
            }
            else{
                MainActivity.send=true;
                notificationManager.notify(123,notificationStarMouse);
            }
        }
    };


    View.OnTouchListener leftClickButtonWestListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                MainActivity.clickSend = 1;
                new Asynch().execute();
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                MainActivity.clickSend = 2;
                new Asynch().execute();
            }
            return false;
        }
    };

    View.OnTouchListener rightClickButtonEastListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                MainActivity.clickSend = 3;
                new Asynch().execute();
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                MainActivity.clickSend = 4;
                new Asynch().execute();
            }
            return false;
        }
    };

    View.OnClickListener connectButtonNorthListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            IntentIntegrator it = new IntentIntegrator(MainActivity.this);
            it.initiateScan();
            Log.i("scan1", "got the value");
        }
    };


    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(),"16. onDestroy()", Toast.LENGTH_SHORT).show();
        notificationManager.cancelAll();
        super.onDestroy();
    }



    private void makeConnections(){
        if(MainActivity.globalIP.indexOf("192")==-1){
            Log.i("Hello","Invalid IP entered");
        }
        else {
            try{
                leftClickButtonWest.setVisibility(View.VISIBLE);
                rightClickButtonEast.setVisibility(View.VISIBLE);
                startStopButton.setVisibility(View.VISIBLE);
                keyboardOnOffButton.setVisibility(View.VISIBLE);

                arrowUp.setVisibility(View.VISIBLE);
                arrowDown.setVisibility(View.VISIBLE);
                arrowLeft.setVisibility(View.VISIBLE);
                arrowRight.setVisibility(View.VISIBLE);

                MainActivity.connectButtonNorth.setVisibility(View.INVISIBLE);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                MainActivity.sock = new DatagramSocket();
                MainActivity.send = true;


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MainActivity.sockTCP=new Socket(globalIP,MainActivity.port2);
                            MainActivity.dos=new DataOutputStream(MainActivity.sockTCP.getOutputStream());
                            MainActivity.sendTCP=true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


            } catch (SocketException e) {
                e.printStackTrace();
                Log.i("hello",e.toString()+"");
            }
        }
    }


    // for QR Scanner
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        String re="";
        if (scanResult != null) {
            try {
                re = scanResult.getContents();
                if(re.isEmpty()==false){
                    Log.i("code", re);
                    String str_temp[]=re.split(":");
                    MainActivity.globalIP = str_temp[0];
                    port1 = Integer.parseInt(str_temp[1]);
                    port2= Integer.parseInt(str_temp[2]);
                    makeConnections();
                    notificationManager.notify(123,notificationStarMouse);
                }
            }
            catch(Exception e){
                Log.i("code",e.toString());
            }
        }

        else{
            Log.i("scan2","else code");
            new Intent(this, MainActivity.class);
            editTextTemporary.setFocusable(true);
        }
        // else continue with any other code you need in the method


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            gx = event.values[0];
            gy = event.values[1];
            gz = event.values[2];

            try {

                new Asynch().execute();
            }
            catch (Exception e){
                Log.i("hello",e.toString());
                Log.i("hello","Server Closed");
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}







//Thread for UDP connections

class Asynch extends AsyncTask{
    protected Object doInBackground(Object [] var){
        try{
            if(MainActivity.send){
                byte [] tempBytes;
                InetAddress inet;
                DatagramPacket dataPack;
                switch (MainActivity.clickSend){
                    case 0:
                        StringBuilder string = new StringBuilder();
                        MainActivity.temp=string.append(MainActivity.gx).append(",").append(MainActivity.gy).append(",").append(MainActivity.gz).append(",").toString();
                        tempBytes=MainActivity.temp.getBytes();
                        inet=InetAddress.getByName(MainActivity.globalIP);
                        dataPack=new DatagramPacket(tempBytes,tempBytes.length,inet,MainActivity.port1);
                        MainActivity.sock.send(dataPack);
                        break;
                    case 1:
                    {
                        MainActivity.temp="LeftDown";
                        tempBytes=MainActivity.temp.getBytes();
                        inet=InetAddress.getByName(MainActivity.globalIP);
                        dataPack=new DatagramPacket(tempBytes,tempBytes.length,inet,MainActivity.port1);
                        MainActivity.sock.send(dataPack);
                        MainActivity.clickSend=0;
                        break;
                    }

                    case 2:{

                        MainActivity.temp="LeftUp";
                        tempBytes=MainActivity.temp.getBytes();
                        inet=InetAddress.getByName(MainActivity.globalIP);
                        dataPack=new DatagramPacket(tempBytes,tempBytes.length,inet,MainActivity.port1);
                        MainActivity.sock.send(dataPack);
                        MainActivity.clickSend=0;
                        break;
                    }
                    case 3:{

                        MainActivity.temp="RightDown";
                        tempBytes=MainActivity.temp.getBytes();
                        inet=InetAddress.getByName(MainActivity.globalIP);
                        dataPack=new DatagramPacket(tempBytes,tempBytes.length,inet,MainActivity.port1);
                        MainActivity.sock.send(dataPack);
                        MainActivity.clickSend=0;
                        break;
                    }
                    case 4:{
                        MainActivity.temp="RightUp";
                        tempBytes=MainActivity.temp.getBytes();
                        inet=InetAddress.getByName(MainActivity.globalIP);
                        dataPack=new DatagramPacket(tempBytes,tempBytes.length,inet,MainActivity.port1);
                        MainActivity.sock.send(dataPack);
                        MainActivity.clickSend=0;
                        break;
                    }


                    default:{
                        string = new StringBuilder();
                        MainActivity.temp=string.append(MainActivity.gx).append(",").append(MainActivity.gy).append(",").append(MainActivity.gz).append(",").toString();
                        tempBytes=MainActivity.temp.getBytes();
                        inet=InetAddress.getByName(MainActivity.globalIP);
                        dataPack=new DatagramPacket(tempBytes,tempBytes.length,inet,MainActivity.port1);
                        MainActivity.sock.send(dataPack);
                        break;
                    }
                }
            }
        }
        //catch (Exception e){}
        catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    return null;
    }
}









//Thread for TCP Connections

class Asynch2 extends AsyncTask<Void,Void,Long >{

    static boolean temp=false;
    static boolean temp2=false;
    static String str;
    Asynch2(CharSequence s){
        str=s+"";
        //MainActivity.editTextTemporary.setText("");
    }


    @Override
    protected Long doInBackground(Void... params) {
        try {
            if(MainActivity.sendTCP) {
                MainActivity.dos.writeUTF(str);
                Log.i("send",str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Long temp){
        //Log.i("hello","execute");
        if(Asynch2.temp){
            Log.i("hello","execute");
            MainActivity.editTextTemporary.setText("");
            Asynch2.temp=false;
        }
        if(Asynch2.temp2){
            try {
                //Thread.sleep(200);
                MainActivity.keyboardOnOffButton.performClick();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Asynch2.temp2=false;
        }
    }
}










class ZanyEditText extends EditText {

    public ZanyEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ZanyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZanyEditText(Context context) {
        super(context);
    }


    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new ZanyInputConnection(super.onCreateInputConnection(outAttrs), true);
    }

    private class ZanyInputConnection extends InputConnectionWrapper {

        public ZanyInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {

            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL && getText().toString().length()==0) {
                new Asynch2("backspace@99").execute();
                Asynch2.temp=true;
                //MainActivity.editTextTemporary.setText("");
                Log.i("key_new", event.getKeyCode() + "");
                // Un-comment if you wish to cancel the backspace:
                // return false;
            }
            return super.sendKeyEvent(event);
        }


        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
            if (beforeLength-afterLength==1) {
                // backspace
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }

    }
}