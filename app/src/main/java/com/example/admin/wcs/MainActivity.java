package com.example.admin.wcs;

import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    public MyApplication mMyApplication;

    private RadioGroup radioGroupBar;
    private RadioButton radioButtonChannel1;

    //Fragment Object
    private Fragment1 FragmentPage1;
    private Fragment2 FragmentPage2;
    private Fragment3 FragmentPage3;
    private Fragment4 FragmentPage4;
    private FragmentManager fManager;

    //保存MyTouchListener接口的列表
    private ArrayList<MyTouchListener> myTouchListeners = new ArrayList<>();

    private long exitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        mMyApplication = ((MyApplication)getApplicationContext());

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
                        FragmentPage1 = new Fragment1();
                        fTransaction.add(R.id.ly_content,FragmentPage1);
                    }else{
                        fTransaction.show(FragmentPage1);
                    }
                    break;
                case R.id.radioButton_channel2:
                    if(FragmentPage2 == null){
                        FragmentPage2 = new Fragment2();
                        fTransaction.add(R.id.ly_content,FragmentPage2);
                    }else{
                        fTransaction.show(FragmentPage2);
                    }
                    break;
                case R.id.radioButton_channel3:
                    if(FragmentPage3 == null){
                        FragmentPage3 = new Fragment3();
                        fTransaction.add(R.id.ly_content,FragmentPage3);
                    }else{
                        fTransaction.show(FragmentPage3);
                    }
                    break;
                case R.id.radioButton_channel4:
                    if(FragmentPage4 == null){
                        FragmentPage4 = new Fragment4();
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

    //回调接口
    public interface MyTouchListener {
        void onTouchEvent(MotionEvent event);
    }

    //提供给Fragment通过getActivity()方法来注册自己的触摸事件的方法
    public void registerMyTouchListener(MyTouchListener listener) {
        myTouchListeners.add(listener);
    }

    //提供给Fragment通过getActivity()方法来取消注册自己的触摸事件的方法
    public void unRegisterMyTouchListener(MyTouchListener listener) {
        myTouchListeners.remove(listener);
    }

     //分发触摸事件给所有注册了MyTouchListener的接口
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        for (MyTouchListener listener : myTouchListeners) {
            listener.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if((System.currentTimeMillis()-exitTime) > 2000) {      //System.currentTimeMillis()无论何时调用，肯定大于2000
                Toast.makeText(getApplicationContext(), "Press again to exit the program", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }
            else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
