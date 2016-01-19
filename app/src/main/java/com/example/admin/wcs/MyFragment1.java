package com.example.admin.wcs;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MyFragment1 extends Fragment {

    private Button buttonUp, buttonDown;
    private Button buttonBack, buttonOk, buttonMenu;
    private Button buttonSend, buttonLogin, buttonSensor, buttonReset;
    private EditText editTextIp, editTextPort;
    private EditText editTextRecv, editTextSend;

    private Socket mSocket = null;
    private Thread mThread = null;
    private String mStrMSG = "";

    private final int FLAG_START = 0x86;
    private final int FLAG_FINISH = 0x68;
    private int dataLength;
    private int dataBuff[];
    private final int COMMAND_LENGTH = 8;
    private final int DATA_LENGTH_MAX = 128;
    private final String DEFAULT_IP = "192.168.8.1";
    private final String DEFAULT_PORT = "8088";
    private final String RESET_STRING = "*#*#1234#*#*";
    private final int RECV_TIMEOUT = 5000;

    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;
    private SensorEventListener mListener = null;
    private final float SLANT_ANGLE = (float)3.0;

    private int mIntervalTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_main,container,false);

        MyButtonListener_key MyButtonListener = new MyButtonListener_key();

        buttonSend = (Button) view.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new MyButtonListener_send());

        buttonLogin = (Button) view.findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new MyButtonListener_login());

        buttonSensor = (Button) view.findViewById(R.id.button_sensor);
        buttonSensor.setOnClickListener(new MyButtonListener_sensor());

        buttonReset = (Button) view.findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(new MyButtonListener_reset());

        buttonUp = (Button) view.findViewById(R.id.button_up);
        buttonUp.setOnClickListener(MyButtonListener);
        buttonUp.setTag(0);

        buttonDown = (Button) view.findViewById(R.id.button_down);
        buttonDown.setOnClickListener(MyButtonListener);
        buttonDown.setTag(1);

        buttonBack = (Button) view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(MyButtonListener);
        buttonBack.setTag(2);

        buttonOk = (Button) view.findViewById(R.id.button_ok);
        buttonOk.setOnClickListener(MyButtonListener);
        buttonOk.setTag(3);

        buttonMenu = (Button) view.findViewById(R.id.button_menu);
        buttonMenu.setOnClickListener(MyButtonListener);
        buttonMenu.setTag(4);

        editTextIp = (EditText) view.findViewById(R.id.editText_ip);
        editTextPort = (EditText) view.findViewById(R.id.editText_port);

        editTextRecv = (EditText) view.findViewById(R.id.editText_recv);
        editTextSend = (EditText) view.findViewById(R.id.editText_send);

        editTextIp.setText(DEFAULT_IP);
        editTextPort.setText(DEFAULT_PORT);

        return view;
    }

    //实现OnClickListener接口，作为login按钮的监听器类
    class MyButtonListener_login implements View.OnClickListener {
        public void onClick(View v) {
            // TODO Auto-generated method stub
            try {
                if (mSocket == null) {
                    mThread = new Thread(mRunnable);
                    mThread.start();
                }
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("tcp", "Thread create fail", e);
            }
        }
    }

    //实现OnClickListener接口，作为send按钮的监听器类
    class MyButtonListener_send implements View.OnClickListener {
        public void onClick(View v) {
            // TODO Auto-generated method stub
            try {
                // 取得编辑框中我们输入的内容
                byte byteText[] = editTextSend.getText().toString().getBytes();
                dataLength = COMMAND_LENGTH + byteText.length;

                if (dataLength > COMMAND_LENGTH && dataLength < DATA_LENGTH_MAX) {
                    //组装DataBuff
                    dataBuff = new int[dataLength];
                    byte byteDisplay[] = "DISPLAY".getBytes();

                    for (int i = 0; i < byteDisplay.length; i++) {
                        dataBuff[i] = (int) byteDisplay[i];
                    }

                    for (int i = COMMAND_LENGTH; i < dataLength; i++) {
                        dataBuff[i] = (int) byteText[i - COMMAND_LENGTH];
                    }

                    // 发送给服务器
/*                    mSocket.getOutputStream().write(FlagStart);
                    mSocket.getOutputStream().write(DataLength);

                    for (int i = 0; i < DataLength; i++) {
                        mSocket.getOutputStream().write(DataBuff[i]);
                    }

                    mSocket.getOutputStream().write(FlagFinish);
                    mSocket.getOutputStream().flush();
*/
                    SendByteMessage(dataBuff);
                    DisplayMessage("log", "message send");
                }
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("tcp", "send fail", e);
            }
        }
    }

    //实现OnClickListener接口，作为sensor按钮的监听器类
    class MyButtonListener_sensor implements View.OnClickListener {
        public void onClick(View v) {
            // TODO Auto-generated method stub
            //取得重力感应器Sensor对象
            mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }

            //创建SENSOR监听器
            mListener = new SensorEventListener() {
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }

                public void onSensorChanged(SensorEvent event) {
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];
                    SensorDataHandle(x, y, z);
                }
            };

            //注册SENSOR监听器, SENSOR_DELAY_NORMAL 还可以是以下常量：
            //SENSOR_DELAY_NOMAL (200000微秒)
            //SENSOR_DELAY_UI (60000微秒)
            //SENSOR_DELAY_GAME (20000微秒)
            //SENSOR_DELAY_FASTEST (0微秒)
            mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

            //取消SENSOR监听器
            //mSensorManager.unregisterListener(mListener);
        }
    }

    //实现OnClickListener接口，作为login按钮的监听器类
    class MyButtonListener_reset implements View.OnClickListener {
        public void onClick(View v) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("tcp", "reset connection fail", e);
            }

            mStrMSG = RESET_STRING;                             //复位消息
            mHandler.sendMessage(mHandler.obtainMessage());     // 发送消息

            //弹出对话框
            new AlertDialog.Builder(getActivity())
                    .setTitle("WC System")
                    .setMessage("Reset connection successful.")
                    .setPositiveButton("yes", null)
                    .show();
        }
    }

    // 线程:监听服务器发来的消息
    private Runnable mRunnable = new Runnable() {
        public void run() {
            try {
                // Socket实例化，连接服务器
                String serverIp = editTextIp.getText().toString();
                int serverPort = Integer.parseInt(editTextPort.getText().toString());

                mSocket = new Socket(serverIp, serverPort);
                DisplayMessage("log", "login success");

                // 获取Socket输入输出流进行读写操作
                while (true) {
                    if (mSocket.getInputStream().read() == FLAG_START) {
                        String strMsgTmp = "";

                        //增加超时处理机制
                        final ExecutorService exec = Executors.newFixedThreadPool(1);

                        Callable<String> call = new Callable<String>() {
                            public String call() throws Exception {
                                //开始执行耗时操作，这里的返回值类型为String，可以为任意类型
                                dataLength = mSocket.getInputStream().read();
                                if (dataLength > 0 && dataLength < DATA_LENGTH_MAX) {
                                    dataBuff = new int[dataLength];

                                    for (int i = 0; i < dataLength; i++) {
                                        dataBuff[i] = mSocket.getInputStream().read();
                                    }

                                    if (mSocket.getInputStream().read() == FLAG_FINISH) {
                                        return "success";
                                    }
                                }
                                return "fail";
                            }
                        };

                        try {
                            Future<String> future = exec.submit(call);
                            String result = future.get(RECV_TIMEOUT, TimeUnit.MILLISECONDS);    //任务处理超时时间设为 RECV_TIMEOUT，单位ms

                            //超时前返回
                            if(result.equals("success")) {          //已收到消息
                                for (int i = 0; i < dataLength; i++) {
                                    strMsgTmp += Integer.toHexString(dataBuff[i]) + " ";
                                }
                            }
                            else {                                  //收到错误消息帧
                                strMsgTmp = "frame error";
                            }
                        } catch (TimeoutException ex) {
                            //超时后返回
                            strMsgTmp = "frame timeout";
                        } catch (Exception e) {
                            //处理失败
                            strMsgTmp = "unknown error";
                        }
                        finally {
                            exec.shutdown();                        //关闭线程池
                            DisplayMessage("recv", strMsgTmp);     //打印
                        }

/*
                        //未包含超时，死等
                        dataLength = mSocket.getInputStream().read();
                        if (dataLength > 0 && dataLength < DATA_LENGTH_MAX) {
                            dataBuff = new int[dataLength];

                            for (int i = 0; i < dataLength; i++) {
                                dataBuff[i] = mSocket.getInputStream().read();
                            }

                            if (mSocket.getInputStream().read() == FLAG_FINISH) {
                                // 打印：已收到消息
                                String strMsgTmp = "";
                                for(int i = 0; i < dataLength; i++) {
                                    strMsgTmp += Integer.toHexString(dataBuff[i]) + " ";
                                }
                                DisplayMessage("recv", strMsgTmp);
                            }
                        }
 */
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                mSocket = null;
                Log.e("tcp", "login or recv fail", e);
            }
        }
    };

    Handler mHandler = new Handler()//更新界面的显示（不能直接在线程中更新视图，因为Android线程是安全的）
    {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 刷新
            try {
                if (mStrMSG.equals(RESET_STRING) == false) {
                    editTextRecv.append(mStrMSG);// 将记录添加进来
                } else {
                    editTextIp.setText(DEFAULT_IP);
                    editTextPort.setText(DEFAULT_PORT);
                    editTextSend.setText("");
                    editTextRecv.setText("");

                    if (mSensorManager != null) {
                        mSensorManager.unregisterListener(mListener);
                        mSensorManager = null;
                    }
                }
            } catch (Exception e) {
                Log.e("tcp", "recv display fail", e);
            }
        }
    };

    class MyButtonListener_key implements View.OnClickListener {
        public void onClick(View v) {
            try {
                String strKey[] = {"UP", "DOWN", "BACK", "OK", "MENU"};
                byte byteKey[] = strKey[(Integer) v.getTag()].getBytes();

                dataLength = COMMAND_LENGTH;
                dataBuff = new int[dataLength];
                for (int i = 0; i < byteKey.length; i++) {
                    dataBuff[i] = (int) byteKey[i];
                }
                // 发送给服务器
/*                mSocket.getOutputStream().write(FlagStart);
                mSocket.getOutputStream().write(DataLength);

                for (int i = 0; i < DataLength; i++) {
                    mSocket.getOutputStream().write(DataBuff[i]);
                }

                mSocket.getOutputStream().write(FlagFinish);
                mSocket.getOutputStream().flush();
*/
                SendByteMessage(dataBuff);
                DisplayMessage("log", "key " + strKey[(Integer) v.getTag()] + " control");
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("tcp", "key fail", e);
            }
        }
    }

    public void SendByteMessage(int msgBuf[]) {
        try{
            byte bufTmp[] = new byte[msgBuf.length + 3];  //长度补 3 Byte：FlagStart DataLength FlagFinish
            bufTmp[0] = (byte)FLAG_START;
            bufTmp[1] = (byte)dataLength;

            for (int i = 0; i < msgBuf.length; i++) {
                bufTmp[i + 2] = (byte)msgBuf[i];
            }

            bufTmp[2 + msgBuf.length] = (byte)FLAG_FINISH;
            mSocket.getOutputStream().write(bufTmp);
            mSocket.getOutputStream().flush();
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("tcp", "SendByteMessage fail", e);
            throw (NullPointerException)e;
        }
    }

    public void DisplayMessage(String strTag, String strMsg) {
        try{
            SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");    //"yyyy-MM-dd    hh:mm:ss"
            String date = sDateFormat.format(new java.util.Date());

            mStrMSG ="\n"  + date + " " + strTag + ": " + strMsg;       //打印
            mHandler.sendMessage(mHandler.obtainMessage());              // 发送消息
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("tcp", "DisplayMessage fail", e);
            throw (NullPointerException)e;
        }
    }

    public void SensorDataHandle(float x, float y, float z) {
        //Log.e("SensorEventListener", "x: " + x + " y: " + y + " z: " + z);
        //DisplayMessage("S", "x: " + x + " y: " + y + " z: " + z);

        //for test 1秒1次
        if (mIntervalTime++ != 5) {
            return;
        }
        else {
            mIntervalTime = 0;
        }

        String strTmp = "";

        if (z < 0) {                                //屏幕倒置
            strTmp = "error command";
        }
        else {
            if (y > SLANT_ANGLE) {                  //前面屏幕低于后面屏幕
                if (x > SLANT_ANGLE) {                  //左边屏幕高于右边屏幕
                    strTmp = "move backward and turn left";
                }
                else if (x < -SLANT_ANGLE) {            //左边屏幕低于右边屏幕
                    strTmp = "move backward and turn right";
                }
                else {
                    strTmp = "move backward";
                }
            }
            else if (y < -SLANT_ANGLE) {            //前面屏幕高于后面屏幕
                if (x > SLANT_ANGLE) {                  //左边屏幕高于右边屏幕
                    strTmp = "move forward and turn left";
                }
                else if (x < -SLANT_ANGLE) {            //左边屏幕低于右边屏幕
                    strTmp = "move forward and turn right";
                }
                else {
                    strTmp = "move forward";
                }
            }
            else {
                if (x > SLANT_ANGLE) {                  //左边屏幕高于右边屏幕
                    strTmp = "turn left";
                }
                else if (x < -SLANT_ANGLE) {            //左边屏幕低于右边屏幕
                    strTmp = "turn right";
                }
                else {
                    strTmp = "stop";
                }
            }
        }

        DisplayMessage("sensor", strTmp);

        return;
    }
}
