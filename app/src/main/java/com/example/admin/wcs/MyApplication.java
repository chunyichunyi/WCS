package com.example.admin.wcs;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MyApplication extends Application {

    private Socket mSocket = null;
    private Thread mThread = null;
    private final int CONNECT_TIMEOUT = 2000;

    private final int FLAG_START = 0x86;
    private final int FLAG_FINISH = 0x68;
    private int dataLength;
    private int[] dataBuff;

    private final int LENGTH_COMMAND = 8;
    private final int LENGTH_DATA = 120;

    private final int RECV_TIMEOUT = 5000;

    private String mServerIp;
    private int mServerPort;
    private Context mContext;
    private final List<int[]> mMsgList  = new ArrayList<>();
    private boolean mConnectState;

    private boolean mDebugFlag;
    private final List<int[]> mDebugList = new ArrayList<>();

    CountDownLatch mLatch;      //控制login操作的同步问题

    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mConnectState = false;
        mDebugFlag = false;

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.admin.wcs.LOCAL_BROADCAST");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);    // 注册本地广播监听器
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    public boolean UserLogin(String serverIp, int serverPort) {
        mServerIp = serverIp;
        mServerPort = serverPort;
        mLatch = new CountDownLatch(1);

        try {
            if (mSocket == null) {
                mThread = new Thread(mRunnable);
                mThread.start();

                mLatch.await();     //等待线程中socket的连接操作完成
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (mSocket != null);
    }

    public void UserLogout() {
        try {
            mSocket.close();
        } catch (Exception e) {
        } finally {
            mSocket = null;
        }
    }

    public boolean MsgSend(int[] msgBuf) {
        if (mDebugFlag) {
            synchronized (mDebugList) {
                mDebugList.add(msgBuf.clone());
            }
            return true;
        }

        byte bufTmp[] = new byte[msgBuf.length + 3];  //长度补 3 Byte：FlagStart DataLength FlagFinish
        bufTmp[0] = (byte)FLAG_START;
        bufTmp[1] = (byte)msgBuf.length;

        for (int i = 0; i < msgBuf.length; i++) {
            bufTmp[i + 2] = (byte)msgBuf[i];
        }

        bufTmp[2 + msgBuf.length] = (byte)FLAG_FINISH;

        try{
            mSocket.getOutputStream().write(bufTmp);
            mSocket.getOutputStream().flush();
        } catch (Exception e) {
            UserLogout();
            return false;
        }

        return true;
    }

    public int[] MsgRecv() {
        if (mDebugFlag) {
            int[] tmp = null;
            synchronized (mDebugList) {
                if (!mDebugList.isEmpty()) {
                    tmp = mDebugList.remove(0);
                }
            }
            return tmp;
        }

        int[] tmp = null;
        synchronized (mMsgList) {
            if (!mMsgList.isEmpty()) {
                tmp = mMsgList.remove(0);
            }
        }
        return tmp;
    }

    public boolean CheckConnectState() {
        if (mDebugFlag) {
            return true;
        }

        return mConnectState;
    }

    // 线程:监听服务器发来的消息
    private Runnable mRunnable = new Runnable() {
        public void run() {
            try {
                // Socket实例化，连接服务器
                mSocket = new Socket();
                mSocket.connect(new InetSocketAddress(mServerIp, mServerPort), CONNECT_TIMEOUT);
            } catch (Exception e) {
                UserLogout();
            } finally {
                mLatch.countDown();     //socket连接操作完成
                if (mSocket == null) {
                    return;
                }
            }

            try {
                mConnectState = true;

                // 获取Socket输入输出流进行读写操作
                while (true) {
                    int tmpRead = mSocket.getInputStream().read();

                    if (tmpRead == -1) {
                        mConnectState = false;
                        throw new Exception();
                    } else if (tmpRead == FLAG_START) {
                        //增加超时处理机制
                        final ExecutorService exec = Executors.newFixedThreadPool(1);

                        Callable<String> call = new Callable<String>() {
                            public String call() throws Exception {
                                //开始执行耗时操作，这里的返回值类型为String，可以为任意类型
                                dataLength = mSocket.getInputStream().read();
                                if (dataLength > 0 && dataLength < LENGTH_COMMAND + LENGTH_DATA) {
                                    dataBuff = new int[dataLength];

                                    for (int i = 0; i < dataBuff.length; i++) {
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
                                synchronized (mMsgList) {
                                    mMsgList.add(dataBuff);
                                }
                            } else {                                //收到错误消息帧
                                LocalBroadcastSend("MSG_RECV_ERROR_FRAMEOR_FRAME");   //发送本地广播
                            }
                        } catch (TimeoutException e) {              //超时后返回
                            LocalBroadcastSend("MSG_RECV_ERROR_TIMEOUT_TIMEOUT");   //发送本地广播
                        } catch (Exception e) {                     //处理失败
                            LocalBroadcastSend("MSG_RECV_ERROR_UNKNOWN_UNKNOWN");   //发送本地广播
                        } finally {
                            exec.shutdown();                        //关闭线程池
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                UserLogout();
                LocalBroadcastSend("SOCKET_DISCONNECT");        //socket掉线，发送本地广播
            }
        }
    };

    public void LocalBroadcastSend(String msgSend) {
        Intent intent = new Intent("com.example.admin.wcs.LOCAL_BROADCAST");
        intent.putExtra("msg", msgSend);
        localBroadcastManager.sendBroadcast(intent);
    }

    public void SetDebugFlag(boolean flag) {
        mDebugFlag = flag;
    }

    public boolean GetDebugFlag() {
        return mDebugFlag;
    }
}
