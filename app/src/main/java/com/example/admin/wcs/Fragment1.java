package com.example.admin.wcs;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Fragment1 extends Fragment {

    private Button buttonUp, buttonDown, buttonBack, buttonOk, buttonMenu;
    private Button buttonSend, buttonReset;
    private EditText editTextCommand, editTextData;
    private TextView textViewMsg;

    private final List<String> mStrMsgList = new ArrayList<>();
    private final String STRING_RESET = "*#*#1234#*#*";

    private final int LENGTH_COMMAND = 8;
    private final int LENGTH_DATA = 120;

    private Thread mRecvThread = null;
    private volatile boolean shutdownRequested;     //设置结束标志位

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment1_main,container,false);

        MyButtonListener_key MyButtonListener = new MyButtonListener_key();

        textViewMsg = (TextView) view.findViewById(R.id.textView_msg);
        editTextCommand = (EditText) view.findViewById(R.id.editText_command);
        editTextData = (EditText) view.findViewById(R.id.editText_data);
        buttonSend = (Button) view.findViewById(R.id.button_send);
        buttonReset = (Button) view.findViewById(R.id.button_reset);

        buttonUp = (Button) view.findViewById(R.id.button_up);
        buttonDown = (Button) view.findViewById(R.id.button_down);
        buttonBack = (Button) view.findViewById(R.id.button_back);
        buttonOk = (Button) view.findViewById(R.id.button_ok);
        buttonMenu = (Button) view.findViewById(R.id.button_menu);

        buttonUp.setOnClickListener(MyButtonListener);
        buttonDown.setOnClickListener(MyButtonListener);
        buttonBack.setOnClickListener(MyButtonListener);
        buttonOk.setOnClickListener(MyButtonListener);
        buttonMenu.setOnClickListener(MyButtonListener);

        buttonUp.setTag(0);
        buttonDown.setTag(1);
        buttonBack.setTag(2);
        buttonOk.setTag(3);
        buttonMenu.setTag(4);

        textViewMsg.setMovementMethod(ScrollingMovementMethod.getInstance());      //控制滚动

        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 取得编辑框中我们输入的内容
                byte byteCommand[] = editTextCommand.getText().toString().getBytes();
                String[] strData = editTextData.getText().toString().split("\\.");

                if ((byteCommand.length > 0) && (byteCommand.length <= LENGTH_COMMAND)
                        && (strData.length <= LENGTH_DATA)) {

                    if  ((strData.length == 0) || ((strData.length == 1) && (strData[0].isEmpty()))) {
                        int[] dataBuff = new int[LENGTH_COMMAND];

                        for (int i = 0; i < byteCommand.length; i++) {
                            dataBuff[i] = (int) byteCommand[i];
                        }

                        ((MainActivity) getActivity()).mMyApplication.MsgSend(dataBuff);     // 发送给服务器
                        DisplayMessage("log", "send command only");
                    } else {
                        try {
                            int[] dataBuff = new int[LENGTH_COMMAND + strData.length];

                            for (int i = 0; i < byteCommand.length; i++) {
                                dataBuff[i] = (int) byteCommand[i];
                            }
                            for (int i = 0; i < strData.length; i++) {
                                dataBuff[i + LENGTH_COMMAND] = Integer.parseInt(strData[i], 16);
                            }

                            ((MainActivity) getActivity()).mMyApplication.MsgSend(dataBuff);     // 发送给服务器
                            DisplayMessage("log", "send command and data");
                        } catch (NumberFormatException e) {
                            Toast.makeText(getActivity(), "Send data error", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "Send length error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                synchronized (mStrMsgList) {
                    mStrMsgList.add(STRING_RESET);              //复位消息
                }
                mHandler.sendMessage(mHandler.obtainMessage());     // 发送消息
                Toast.makeText(getActivity(), "Reset operation successful", Toast.LENGTH_SHORT).show();
            }
        });

        //开启线程接收服务器发来的消息
        mRecvThread = new Thread(mRunnable);
        mRecvThread.start();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        shutdownRequested = true;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {   //参数为true表示隐藏，false表示显示
        if (hidden) {
            shutdownRequested = true;
        } else {
            mRecvThread = new Thread(mRunnable);
            mRecvThread.start();
        }
    }

    class MyButtonListener_key implements View.OnClickListener {
        public void onClick(View v) {
            try {
                String strKey[] = {"UP", "DOWN", "BACK", "OK", "MENU"};
                byte byteKey[] = strKey[(Integer) v.getTag()].getBytes();
                int[] dataBuff = new int[LENGTH_COMMAND];

                for (int i = 0; i < byteKey.length; i++) {
                    dataBuff[i] = (int) byteKey[i];
                }

                ((MainActivity)getActivity()).mMyApplication.MsgSend(dataBuff);     // 发送给服务器
                DisplayMessage("log", "key " + strKey[(Integer) v.getTag()] + " control");
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("MyButtonListener_key", "key fail", e);
            }
        }
    }

    public void DisplayMessage(String strTag, String strMsg) {
        try {
            SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");    //"yyyy-MM-dd    hh:mm:ss"
            String date = sDateFormat.format(new java.util.Date());

            synchronized (mStrMsgList) {
                mStrMsgList.add(date + " " + strTag + ": " + strMsg + "\n");        //打印
            }
            mHandler.sendMessage(mHandler.obtainMessage());              // 发送消息
        } catch (Exception e) {
            Log.e("DisplayMessage", "display message fail", e);
        }
    }

    private Handler mHandler = new Handler() {   //更新界面的显示（不能直接在线程中更新视图，因为Android线程是安全的）
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            synchronized (mStrMsgList) {
                while (!mStrMsgList.isEmpty()) {
                    String tmp = mStrMsgList.remove(0);

                    if (tmp.equals(STRING_RESET)) {
                        textViewMsg.setText("");
                        textViewMsg.scrollTo(0, 0);

                        editTextCommand.setText("");
                        editTextData.setText("");
                    } else {
                        textViewMsg.append(tmp);       // 将记录添加进来

                        //控制滚动到最后一行
                        int offset = textViewMsg.getLineCount() * textViewMsg.getLineHeight();
                        if (offset > textViewMsg.getHeight()) {
                            textViewMsg.scrollTo(0, offset - textViewMsg.getHeight());
                        }
                    }
                }
            }
        }
    };

    //线程:监听服务器发来的消息
    private Runnable mRunnable = new Runnable() {
        public void run() {
            shutdownRequested = false;

            //MsgRecv & CheckConnectState
            int[] bufRecv;

            do {
                bufRecv = ((MainActivity) getActivity()).mMyApplication.MsgRecv();

                if (bufRecv != null) {
                    String strMsgTmp = "";

                    for (int i = 0; i < bufRecv.length; i++) {
                        strMsgTmp += Integer.toHexString(bufRecv[i]) + " ";
                    }
                    DisplayMessage("recv", strMsgTmp);
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }
            } while ((bufRecv != null || ((MainActivity) getActivity()).mMyApplication.CheckConnectState())
                    && (!shutdownRequested));
        }
    };
}
