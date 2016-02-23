package com.example.admin.wcs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class FG2ViewsUp extends View implements CallbackMove{

    private Paint mPaint;
    private float[] mScanData;
    private float mScanRadius;
    private boolean SCAN_UPDATE = false;
    private float mMovePointX = 0;
    private float mMovePointY = 0;
    private float mMovePointDegree = 0;

    private float mMoveStorageX = 0;
    private float mMoveStorageY = 0;
    private float mMoveStorageDegree = 0;

    public FG2ViewsUp(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    public FG2ViewsUp(Context context , AttributeSet attrs){
        super(context,attrs);
    }
    //存在canvas对象，即存在默认的显示区域
    @Override
    public void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);    //移动画纸的坐标点位置

        mPaint = new Paint();
        mPaint.setAntiAlias(true);              //抗锯齿

        //绘制三角形
        Path path = new Path();
        float tmpLength = canvas.getWidth() / 60;
        path.moveTo(-tmpLength, tmpLength);     // 此点为多边形的起点
        path.lineTo(tmpLength, tmpLength);
        path.lineTo(0, -tmpLength * 2);
        path.close(); // 使这些点构成封闭的多边形
        mPaint.setColor(Color.BLACK);
        canvas.drawPath(path, mPaint);

//        canvas.save();      //保存canvas之前的操作,在sava()和restore之间的操作不会对canvas之前的操作进行影响
//        canvas.restore();  //复原之前save()之前的属性,并且将save()方法之后的roate(),translate()以及clipXXX()方法的操作清空

        //执行move操作的变换
        canvas.translate(-mMovePointX, -mMovePointY);
        canvas.rotate(-mMovePointDegree, mMovePointX, mMovePointY);                 //旋转坐标轴（第一个参数为正则顺时针旋转）

        mScanRadius = getWidth() / 3;       //检测的最大半径

        if (SCAN_UPDATE == true) {
            //画实际检测数据
            mPaint.setColor(Color.BLUE);
            mPaint.setStrokeWidth(2);
            float mPointRadian = (float)Math.toRadians(90 - (float)360 / mScanData.length);  //90 - (360 / 240)  计算两个点之间的弧度
            float mPointDegree = (float)360 / mScanData.length;   //数据为顺时针旋转

            for (int i = 0; i < mScanData.length; i++) {
                int j = (i + 1) % mScanData.length;
                if ((mScanData[i] <= mScanRadius) && (mScanData[j] <= mScanRadius)) {
                    canvas.drawLine(0f,                                 //第一个点x坐标
                                    -mScanData[i],                       //第一个点y坐标
                                    (float)Math.cos(mPointRadian) * mScanData[j],    //第二个点x坐标
                                    -(float)Math.sin(mPointRadian) * mScanData[j],    //第二个点y坐标
                                    mPaint);
                }

//                float xx = (float)Math.cos(mPointRadian) * mScanData[j];
//                float yy = (float)Math.sin(mPointRadian) * mScanData[j];
//                Log.v("onDraw", "i:" + mScanData[i] + "  xx:" + xx + "  yy:" + yy);

                canvas.rotate(mPointDegree, 0f, 0f); //旋转坐标轴（第一个参数为正则顺时针旋转）
            }

            //画最大检测范围
            PathEffect effects = new DashPathEffect(new float[]{10, 20}, 1);       //设置虚线
            mPaint.setPathEffect(effects);
            mPaint.setColor(Color.LTGRAY);
            mPaint.setStyle(Paint.Style.STROKE);     //空心
            canvas.drawCircle(0, 0, mScanRadius, mPaint); //画圆圈
        }
    }

    public void ScanUpdate(float[] data) {
        //根据传入数据（100为检测边界），重绘检测结果
        for (int i = 0; i < data.length; i++) {
            data[i] = data[i] * mScanRadius / 100;      //按绘图的范围，处理原始数据
        }
        mScanData = data.clone();   //数据为从当前坐标轴12点方向开始，顺时针一周

        SCAN_UPDATE = true;

        //清除move
        mMovePointX = 0;
        mMovePointY = 0;
        mMovePointDegree = 0;
        invalidate();
    }

    public void ScanAndMoveReset() {
        //清除scan
        mScanData = null;
        SCAN_UPDATE = false;

        //清除move
        mMovePointX = 0;
        mMovePointY = 0;
        mMovePointDegree = 0;
        invalidate();
    }

    @Override
    public void MoveUpdate(float x, float y, float degree) {
        //根据传入的位置偏移(x, y)，重绘范围, 角度degree为[0,360)，0度为当前坐标轴12点方向，顺时针递增
        //原始坐标：x轴从左至右增大，y轴从上至下增大

//        Log.d("MoveUpdate", "x:" + x + "  y:" + y + "  degree:" + degree);

        //计算位置偏移(x,y)在原始坐标方向上的新坐标值（只变换方向）
        float tmpRadian = (float)Math.toRadians(-mMovePointDegree);         //计算弧度
        float tmpX = x * (float) Math.cos(tmpRadian) + y * (float) Math.sin(tmpRadian);
        float tmpY = y * (float) Math.cos(tmpRadian) - x * (float) Math.sin(tmpRadian);

        //在原始坐标轴各方向上累加增量（变换位移）
        mMovePointX += tmpX;
        mMovePointY += tmpY;
        mMovePointDegree = (mMovePointDegree + degree) % 360;
        invalidate();
    }

    @Override
    public void MoveUpdate() {
        MoveUpdate(mMoveStorageX, mMoveStorageY, mMoveStorageDegree);
    }

    @Override
    public void MoveStorage(float x, float y, float degree) {
        mMoveStorageX = x;
        mMoveStorageY = y;
        mMoveStorageDegree = degree;
    }

    @Override
    public float MoveGetX() {
        return  mMoveStorageX;
    }

    @Override
    public float MoveGetY() {
        return  mMoveStorageY;
    }

    @Override
    public float MoveGetDegree() {
        return  mMoveStorageDegree;
    }
}
