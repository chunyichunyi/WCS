package com.example.admin.wcs;

import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.MotionEvent;
import android.view.Window;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RadioGroup radioGroupBar;
    private RadioButton radioButtonChannel1;

    //Fragment Object
    private MyFragment1 FragmentPage1;
    private MyFragment2 FragmentPage2;
    private MyFragment3 FragmentPage3;
    private MyFragment4 FragmentPage4;
    private FragmentManager fManager;
    private ArrayList<MainActivity.Mytouchlisener> mytouchliseners = new ArrayList<Mytouchlisener>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        fManager = getFragmentManager();

        radioGroupBar = (RadioGroup) findViewById(R.id.radioGroup_bar);
        radioGroupBar.setOnCheckedChangeListener(new MyRadioGroupListener());

        //获取第一个单选按钮，并设置其为选中状态
        radioButtonChannel1 = (RadioButton) findViewById(R.id.radioButton_channel1);
        radioButtonChannel1.setChecked(true);
    }

    //实现OnCheckedChangeListener接口，作为radioGroup的监听器类
    class MyRadioGroupListener implements RadioGroup.OnCheckedChangeListener {
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            FragmentTransaction fTransaction = fManager.beginTransaction();
            hideAllFragment(fTransaction);
            switch (checkedId){
                case R.id.radioButton_channel1:
                    if(FragmentPage1 == null){
                        FragmentPage1 = new MyFragment1();
                        fTransaction.add(R.id.ly_content,FragmentPage1);
                    }else{
                        fTransaction.show(FragmentPage1);
                    }
                    break;
                case R.id.radioButton_channel2:
                    if(FragmentPage2 == null){
                        FragmentPage2 = new MyFragment2();
                        fTransaction.add(R.id.ly_content,FragmentPage2);
                    }else{
                        fTransaction.show(FragmentPage2);
                    }
                    break;
                case R.id.radioButton_channel3:
                    if(FragmentPage3 == null){
                        FragmentPage3 = new MyFragment3();
                        fTransaction.add(R.id.ly_content,FragmentPage3);
                    }else{
                        fTransaction.show(FragmentPage3);
                    }
                    break;
                case R.id.radioButton_channel4:
                    if(FragmentPage4 == null){
                        FragmentPage4 = new MyFragment4();
                        fTransaction.add(R.id.ly_content,FragmentPage4);
                    }else{
                        fTransaction.show(FragmentPage4);
                    }
                    break;
            }
            fTransaction.commit();
        }
    }

    //隐藏所有Fragment
    private void hideAllFragment(FragmentTransaction fragmentTransaction){
        if(FragmentPage1 != null)fragmentTransaction.hide(FragmentPage1);
        if(FragmentPage2 != null)fragmentTransaction.hide(FragmentPage2);
        if(FragmentPage3 != null)fragmentTransaction.hide(FragmentPage3);
        if(FragmentPage4 != null)fragmentTransaction.hide(FragmentPage4);
    }
    public interface Mytouchlisener{
        public void onTouchEvent(MotionEvent event );
    }
    public void registerMyTouchListener(Mytouchlisener listener){
        mytouchliseners.add(listener);
    }
    public void unregisterMyTouchListener(Mytouchlisener listener){
        mytouchliseners.remove(listener);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for(Mytouchlisener listener:mytouchliseners){
            listener.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
    //    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.item_wifi_config) {
//            //调用wifi配置
//            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
//
//            return true;
//        }
//
//        if (id == R.id.item_bt_config) {
//            //弹出对话框
//            new AlertDialog.Builder(MainActivity.this)
//                    .setTitle("WCS")
//                    .setMessage("Coming soon...")
//                    .setPositiveButton("yes", null)
//                    .show();
//
//            return true;
//        }
//
//        if (id == R.id.item_reset_connection) {
//            try {
//                mSocket.close();
//                mSocket = null;
//            } catch (Exception e) {
//                // TODO: handle exception
//                Log.e("tcp", "reset connection fail", e);
//            }
//
//            mStrMSG = RESET_STRING;                             //复位消息
//            mHandler.sendMessage(mHandler.obtainMessage());     // 发送消息
//
//            //弹出对话框
//            new AlertDialog.Builder(MainActivity.this)
//                    .setTitle("WCS")
//                    .setMessage("Reset connection successful.")
//                    .setPositiveButton("yes", null)
//                    .show();
//
//            return true;
//        }
//
//        if (id == R.id.item_about) {
//            //弹出对话框
//            new AlertDialog.Builder(MainActivity.this)
//                    .setTitle("WCS")
//                    .setMessage("Wireless Control System 0.2.7\n" +
//                                "built on January 14, 2016")
//                    .setPositiveButton("yes", null)
//                    .show();
//
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
