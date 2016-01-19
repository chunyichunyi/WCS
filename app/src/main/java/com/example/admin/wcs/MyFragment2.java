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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyFragment2 extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return new CustomView(this.getActivity());
    }

    class CustomView extends View{

        Paint paint;

        public CustomView(Context context) {
            super(context);
            paint = new Paint(); //设置一个笔刷大小是3的浅灰色的画笔
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setAntiAlias(true);       //抗锯齿
        }

        //在这里我们将测试canvas提供的绘制图形方法
        @Override
        protected void onDraw(Canvas canvas) {

            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);     //空心

            //画最大范围
            Paint PaintRange = new Paint(paint);
            PathEffect effects = new DashPathEffect(new float[] {10, 10}, 1);       //设置虚线
            PaintRange.setPathEffect(effects);
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
        }
    }
}
