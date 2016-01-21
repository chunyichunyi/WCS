package com.example.admin.wcs;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyFragment2 extends Fragment {
    private float current_x = 0;
    private float current_y = 0;
    private MainActivity.Mytouchlisener mytouchlisener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final CustomView customView = new CustomView(this.getActivity());
        mytouchlisener = new MainActivity.Mytouchlisener(){
            @Override
            public void onTouchEvent(MotionEvent event) {
                final int action = event.getAction();
                current_x = event.getX();
                current_y = event.getY();
                Log.d("MyFragment2","current_x is "+current_x);
                Log.d("MyFragment2", "current_y is " + current_y);
                //      customView.setCurrent_x(current_x);
                //      customView.setCurrent_y(current_y);
                final int actionMasked = action & MotionEvent.ACTION_MASK;
                current_y -= getStatusBarHeight();      //去掉状态栏高度差值
                if(customView.out_cirlce(current_x,current_y)){
                    customView.setNewPointXY(current_x,current_y);
                }
                if(actionMasked == MotionEvent.ACTION_UP) {
                    current_x = customView.getCanvas_x();
                    current_y = customView.getCanvas_y();
                    //     customView.setCurrent_x(current_x);
                    //     customView.setCurrent_y(current_y);
                    Log.d("onTouchEvent","ACTION_UP");
                }

                customView.invalidate();
                Log.d("onTouchEvent", "current_x and current_y is " + current_x +  current_y);
            }
        };
        ((MainActivity)this.getActivity()).registerMyTouchListener(mytouchlisener);
        return customView;
    }


    class CustomView extends View{
        //    private float current_x = 0;
        //    private float current_y = 0;
        private float current_length = 0;
        private float canvas_x = 0;
        private float canvas_y = 0;
        private float canvas_length = 0;
        private boolean first_view = false;
        Paint paint;

        public CustomView(Context context) {
            super(context);
            paint = new Paint(); //设置一个笔刷大小是3的浅灰色的画笔
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setAntiAlias(true);       //抗锯齿
            current_x = canvas_x;
            current_y = canvas_y;
        }
        public void setCurrent_x(float x) {
            current_x = x;
        }

        public void setCurrent_y(float y) {
            current_y = y;
        }
        public float getCanvas_x() {
            return canvas_x;
        }

        public float getCanvas_y() {
            return canvas_y;
        }
        public void setCanvas_x(){
            canvas_x = getWidth()/2;
            current_x = canvas_x;
            Log.d("setCanvas_x", "The current_x is " + current_x);
        }
        public void setCanvas_y(){
            canvas_y = 4*getHeight()/5;
            current_y = canvas_y;
        }
        public float getCanvas_length() {
            return canvas_length;
        }
        public float getCurrent_length(){
            return current_length;
        }
        public boolean out_cirlce(float x, float y){
            float length_point = (x-canvas_x)*(x-canvas_x) + (y-canvas_y)*(y-canvas_y);
            if(length_point >(canvas_length*canvas_length))
                return true;
            else
                return false;
        }
        public void setNewPointXY(float x,float y){
            float tmp_x = x-canvas_x;
            float tmp_y = y-canvas_y;
            float tmp_r = (float) (Math.pow(tmp_x,2)+Math.pow(tmp_y,2));
            float point_length = (float)Math.sqrt(tmp_r);
            float k = canvas_length/point_length;
            current_x = k*(x-canvas_x) + canvas_x;
            current_y = k*(y-canvas_y) + canvas_y;
        }

        //在这里我们将测试canvas提供的绘制图形方法
        @Override
        protected void onDraw(Canvas canvas) {

            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);     //空心

            //画出圆球
            canvas_x = getWidth()/2;
            canvas_y = 4*getHeight()/5;
            canvas_length = canvas_x/2;
            current_length = getWidth() / 10;
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);     //空心
            Log.d("onDraw","paint circle current_x is "+current_x);
            //画最大范围
            Paint PaintRange = new Paint(paint);
            PathEffect effects = new DashPathEffect(new float[] {10, 10}, 1);       //设置虚线
            PaintRange.setPathEffect(effects);
            //canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 3);    //移动画纸的坐标点位置
            canvas.drawColor(Color.RED);
            //    canvas.clipRect(getWidth() / 3,2 * getHeight() / 3,getWidth() / 3 + getWidth()/5,2 * getHeight() / 3 + getHeight()/5);
            Log.d("MyFragment2","onDraw current_x is "+current_x);
            Log.d("MyFragment2", "onDraw current_y is " + current_y);
            canvas.drawCircle(canvas_x, canvas_y, canvas_length, PaintRange);
            canvas.drawCircle(current_x, current_y, current_length, PaintRange); //画圆圈
            //画出圆球


            //画最大范围
            //  Paint PaintRange = new Paint(paint);
            //   PathEffect effects = new DashPathEffect(new float[] {10, 10}, 1);       //设置虚线
            //   PaintRange.setPathEffect(effects);
            canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 3);    //移动画纸的坐标点位置
            canvas.drawCircle(0, 0, getWidth() / 3, PaintRange); //画圆圈

            //画实际检测数据
            Paint PaintData = new Paint(paint); //画点的画笔对象
            PaintData.setColor(Color.BLUE);
            PaintData.setStrokeWidth(2);
            float y = getWidth() / 5;      //假设半径
            int count = 360;                //总点数

            for(int i=0 ; i <count ; i++){
                canvas.drawPoint(0f, (y + i % 60), PaintData);
                if (i % 60 == 0){
                    canvas.drawLine(0f, y, 0f, (y + 60) ,PaintData);
                }
                canvas.rotate(360 / count, 0f, 0f); //旋转画纸
            }

            //绘制圆心
            Paint PaintObj = new Paint(paint);      //画圆心的画笔对象
            PaintObj.setStrokeWidth(10);
            PaintObj.setColor(Color.GRAY);
            canvas.drawCircle(0, 0, 20, PaintObj);
            PaintObj.setStyle(Paint.Style.FILL);
            PaintObj.setColor(Color.YELLOW);
            canvas.drawCircle(0, 0, 15, PaintObj);

            //绘制朝向指针
            Paint PaintPoint = new Paint(paint);      //画朝向指针的画笔对象
            PaintPoint.setStrokeWidth(10);
            PaintPoint.setColor(Color.BLACK);
            canvas.drawLine(0, 10, 0, -60, PaintPoint);
            if(first_view == false){
                first_view = true;
                current_x = canvas_x;
                current_y = canvas_y;
                invalidate();
            }
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
}
