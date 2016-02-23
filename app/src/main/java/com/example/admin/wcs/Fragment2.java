package com.example.admin.wcs;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Fragment2 extends Fragment {

//    private Paint paint = new Paint();   //记得要为paint设置颜色，否则 看不到效果
//    private ImageView imgSave;  // save方法以及restore

    private View view;
    private FG2ViewsUp customViewUp;
    private FG2ViewsDown customViewDown;
    private Button buttonScan, buttonReset, buttonChange;
    private boolean useSensorFlag = false;
    private ProgressDialog progressDialog;

    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;
    private SensorEventListener mListener = null;

    private MainActivity.MyTouchListener mTouchListener;
    Timer myTimer;

    private final int LENGTH_COMMAND = 8;
    private final int LENGTH_DATA = 120;

    private final int TIMER_DELAY = 1000;
    private final int TIMER_PERIOD = 1000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment2_main,container,false);

//        imgSave = (ImageView)view.findViewById(R.id.imgSave);
//        save_drawCanvas();  // save方法以及restore

        customViewUp = (FG2ViewsUp) view.findViewById(R.id.FG2ViewsUp);
        customViewDown = (FG2ViewsDown) view.findViewById(R.id.FG2ViewsDown);
        buttonScan = (Button) view.findViewById(R.id.button_scan);
        buttonReset = (Button) view.findViewById(R.id.button_reset);
        buttonChange = (Button) view.findViewById(R.id.button_change);

        //注册接收MainActivity的Touch回调的对象，重写其中的onTouchEvent函数，并进行逻辑处理
        mTouchListener = new MainActivity.MyTouchListener() {       //创建触摸操作监听器
            @Override
            public void onTouchEvent(MotionEvent event) {
                // TODO Auto-generated method stub
                final int action = event.getAction();
                final int actionMasked = action & MotionEvent.ACTION_MASK;

                Log.v("onTouchEvent", "x:" + event.getX() + "  y:" + event.getY());

                float pointX = event.getX();
                float pointY = event.getY() - getStatusBarHeight()             //去掉状态栏高度差值
                        - (view.getHeight() -  customViewDown.getHeight());      //去掉view高度差值

                if ((pointY >= 0) && (pointY <= customViewDown.getHeight())) {
                    if (actionMasked == MotionEvent.ACTION_DOWN                 //按下
                            || actionMasked == MotionEvent.ACTION_MOVE) {       //滑动
                        customViewDown.CircleUpdate(pointX, pointY, customViewUp, new CallbackFeedback() {
                            @Override
                            public void VibratorControl(long[] pattern, int repeat) {
                                Vibrator myVibrator;
                                myVibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                                myVibrator.vibrate(pattern, repeat);   //第二个参数：重复上面的pattern 如果只想震动一次，设为-1
                            }
                        });
                    } else if (actionMasked == MotionEvent.ACTION_UP) {          //抬起
                        customViewDown.CircleReset(customViewUp);
                    }
                }
            }
        };

        //取得重力感应器Sensor对象
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        //创建SENSOR监听器
        mListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                float offsetX = event.values[0];
                float offsetY = event.values[1];
                float offsetZ = event.values[2];

                Log.v("onSensorChanged", "x:" + offsetX + "  y:" + offsetY + "  z:" + offsetZ);

                customViewDown.CircleOffset(-offsetX, offsetY, customViewUp);
            }
        };

        buttonChange.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (useSensorFlag == false) {       //未使用sensor，现改为sensor操作
                    customViewDown.CircleReset(customViewUp);
                    ((MainActivity) getActivity()).unRegisterMyTouchListener(mTouchListener);    //取消触摸监听器
                    //SENSOR_DELAY_NORMAL 还可以是以下常量：
                    //SENSOR_DELAY_NOMAL (200000微秒)，SENSOR_DELAY_UI (60000微秒)
                    //SENSOR_DELAY_GAME (20000微秒)，SENSOR_DELAY_FASTEST (0微秒)
                    mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_UI); //注册SENSOR监听器
                    useSensorFlag = true;
                    Toast.makeText(getActivity(), "Use sensor operate", Toast.LENGTH_SHORT).show();
                } else {                                  //已使用sensor，现改为触摸操作
                    customViewDown.CircleOffset(0, 0, customViewUp);
                    mSensorManager.unregisterListener(mListener);                               //取消SENSOR监听器
                    ((MainActivity) getActivity()).registerMyTouchListener(mTouchListener);          //注册触摸监听器
                    useSensorFlag = false;
                    Toast.makeText(getActivity(), "Use touchscreen operate", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ((MainActivity)getActivity()).registerMyTouchListener(mTouchListener);          //注册触摸监听器，默认为触摸操作

        buttonScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                float[] data = new float[240];

                Toast.makeText(getActivity(), "Scanning...", Toast.LENGTH_SHORT).show();

//                showProgressDialog();
                Random myRandom = new Random();         //暂时模拟收到的监测数据，总点数240
                for (int i = 0; i < 240; i += 6) {
                    data[i] = 60 + myRandom.nextInt(50);        //最小值为60， 最大值110，边界为100
                    for (int j = 1; j < 6; j++) {
                        data[i + j] = data[i];
                    }
                }
//                closeProgressDialog();

                customViewUp.ScanUpdate(data);      //使用新的数据，重新绘图
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                // 1.清楚检测的绘图区域
                // 2.默认为触摸操作
                customViewUp.ScanAndMoveReset();

                if (useSensorFlag == true) {            //已使用sensor，现改为触摸操作
                    customViewDown.CircleOffset(0, 0, customViewUp);
                    mSensorManager.unregisterListener(mListener);                               //取消SENSOR监听器
                    ((MainActivity)getActivity()).registerMyTouchListener(mTouchListener);          //注册触摸监听器
                    useSensorFlag = false;
                }
                Toast.makeText(getActivity(), "Reset operation successful", Toast.LENGTH_SHORT).show();
            }
        });

        //开启定时器，控制检测move操作的频率，并更新检测图形
        try {
            myTimer = new Timer();
            myTimer.schedule(new MoveTimerTask(), TIMER_DELAY, TIMER_PERIOD);     //TIMER_DELAY后执行，每隔TIMER_PERIOD执行一次
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            myTimer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {   //参数为true表示隐藏，false表示显示
        if (hidden) {
            try {
                myTimer.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!useSensorFlag) {       //已使用触摸操作
                customViewDown.CircleReset(customViewUp);
                ((MainActivity)getActivity()).unRegisterMyTouchListener(mTouchListener);    //取消触摸监听器
            }
            else {                                  //已使用sensor操作
                customViewDown.CircleOffset(0, 0, customViewUp);
                mSensorManager.unregisterListener(mListener);                               //取消SENSOR监听器
            }
        } else {
            try {
                myTimer = new Timer();
                myTimer.schedule(new MoveTimerTask(), TIMER_DELAY, TIMER_PERIOD);     //TIMER_DELAY后执行，每隔TIMER_PERIOD执行一次
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!useSensorFlag) {       //初始化触摸操作
                ((MainActivity)getActivity()).registerMyTouchListener(mTouchListener);          //注册触摸监听器
            }
            else {                                  //初始化sensor操作
                //SENSOR_DELAY_NORMAL 还可以是以下常量：
                //SENSOR_DELAY_NOMAL (200000微秒)，SENSOR_DELAY_UI (60000微秒)
                //SENSOR_DELAY_GAME (20000微秒)，SENSOR_DELAY_FASTEST (0微秒)
                mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_UI); //注册SENSOR监听器
            }
        }
    }

    class MoveTimerTask extends TimerTask {
        @Override
        public void run() {

            int[] dataBuff = new int[LENGTH_COMMAND + 3];
            byte byteCommand[] = "MOVE".getBytes();

            for (int i = 0; i < byteCommand.length; i++) {
                dataBuff[i] = (int) byteCommand[i];
            }

            //对数据四舍五入
            dataBuff[LENGTH_COMMAND] = Math.round(customViewUp.MoveGetX());
            dataBuff[LENGTH_COMMAND + 1] = Math.round(customViewUp.MoveGetY());
            dataBuff[LENGTH_COMMAND + 2] = Math.round(customViewUp.MoveGetDegree());

            Log.d("MoveTimerTask", "dataBuff: " + dataBuff[LENGTH_COMMAND]
                    + ",  " + dataBuff[LENGTH_COMMAND + 1]
                    + ",  " + dataBuff[LENGTH_COMMAND + 2]);

            ((MainActivity)getActivity()).mMyApplication.MsgSend(dataBuff);

            // 通过runOnUiThread()方法回到主线程处理逻辑
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    customViewUp.MoveUpdate();      //更新检测视图
                }
            });

//            //test MsgRecv & CheckConnectState
//            int[] bufRecv;
//            do {
//                bufRecv = ((MainActivity)getActivity()).mMyApplication.MsgRecv();
//            } while (bufRecv != null || ((MainActivity)getActivity()).mMyApplication.CheckConnectState());
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    //显示进度对话框
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("scanning...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    //关闭进度对话框
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

//    private void save_drawCanvas(){
//        //将icon图像转换为Bitmap对象
//        Bitmap iconbit = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher) ;
//
//        //创建一个的Bitmap对象
//        Bitmap bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)  ;
//
//        Canvas canvas = new Canvas (bitmap) ;
//
//        paint.setColor(Color.GREEN);
//        paint.setTextSize(16);  //设置字体大小
//        canvas.drawRect(10, 10, 50, 8, paint);
//        canvas.drawText("我没有旋转",50, 10, paint);
//        //保存canvas之前的操作,在sava()和restore之间的操作不会对canvas之前的操作进行影响
//        canvas.save() ;
//
//        //顺时针旋转30度
//        canvas.rotate(30) ;
//        canvas.drawColor(Color.RED);
//        canvas.drawBitmap(iconbit, 20, 20, paint);
//        canvas.drawRect(50, 10, 80, 50, paint);
//        //canvas.translate(20,20);
//        canvas.drawText("我是旋转的", 115, 20, paint);
//
//        //复原之前save()之前的属性,并且将save()方法之后的roate(),translate()以及clipXXX()方法的操作清空
//        canvas.restore();
//
//        //平移(20,20)个像素
//        //canvas.translate(20,20);
//        canvas.drawRect(80, 10, 110, 30, paint);
//        canvas.drawText("我没",115,20, paint);
//
//        //将Bitmap对象转换为Drawable图像资
//        //为ImageView设置图像
//        //imgSave.setImageBitmap(bitmap);
//
//        Drawable drawable = new BitmapDrawable(bitmap) ;
//        imgSave.setBackgroundDrawable(drawable) ;
//
//    }



//    private final float SLANT_ANGLE = (float)3.0;
//    private int mIntervalTime = 0;
//    public void SensorDataHandle(float x, float y, float z) {
//        //Log.e("SensorEventListener", "x: " + x + " y: " + y + " z: " + z);
//        //DisplayMessage("S", "x: " + x + " y: " + y + " z: " + z);
//
//        //for test 1秒1次
//        if (mIntervalTime++ != 5) {
//            return;
//        }
//        else {
//            mIntervalTime = 0;
//        }
//
//        String strTmp = "";
//
//        if (z < 0) {                                //屏幕倒置
//            strTmp = "error command";
//        }
//        else {
//            if (y > SLANT_ANGLE) {                  //前面屏幕低于后面屏幕
//                if (x > SLANT_ANGLE) {                  //左边屏幕高于右边屏幕
//                    strTmp = "move backward and turn left";
//                }
//                else if (x < -SLANT_ANGLE) {            //左边屏幕低于右边屏幕
//                    strTmp = "move backward and turn right";
//                }
//                else {
//                    strTmp = "move backward";
//                }
//            }
//            else if (y < -SLANT_ANGLE) {            //前面屏幕高于后面屏幕
//                if (x > SLANT_ANGLE) {                  //左边屏幕高于右边屏幕
//                    strTmp = "move forward and turn left";
//                }
//                else if (x < -SLANT_ANGLE) {            //左边屏幕低于右边屏幕
//                    strTmp = "move forward and turn right";
//                }
//                else {
//                    strTmp = "move forward";
//                }
//            }
//            else {
//                if (x > SLANT_ANGLE) {                  //左边屏幕高于右边屏幕
//                    strTmp = "turn left";
//                }
//                else if (x < -SLANT_ANGLE) {            //左边屏幕低于右边屏幕
//                    strTmp = "turn right";
//                }
//                else {
//                    strTmp = "stop";
//                }
//            }
//        }
//
//        DisplayMessage("sensor", strTmp);
//
//        return;
//    }
}
