package com.example.admin.wcs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class FG2ViewsDown extends View{

    private float currentX = 0;
    private float currentY = 0;
    private float currentLength = 0;
    private float canvasX = 0;
    private float canvasY = 0;
    private float canvasLength = 0;
    private Paint mPaint;
    private boolean CIRCLE_INIT = true;

    private final int UPDATE_POINT_VALID = 0;
    private final int UPDATE_POINT_OUTSIDE = 1;
    private final int UPDATE_POINT_INVALID = 2;

    public FG2ViewsDown(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    public FG2ViewsDown(Context context , AttributeSet attrs){
        super(context, attrs);
    }

    //存在canvas对象，即存在默认的显示区域
    @Override
    public void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        mPaint = new Paint();;
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);     //空心
        mPaint.setAntiAlias(true);              //抗锯齿

        //移动范围（大圆）
        canvasX = getWidth() / 2;
        canvasY = getHeight() / 2;
        canvasLength = getHeight() * 4 / 9;
        mPaint.setColor(Color.GRAY);
        canvas.drawCircle(canvasX, canvasY, canvasLength, mPaint);

        //初始或移动当前位置（小圆）
        if (CIRCLE_INIT == true) {       //初始位置
            currentX = canvasX;
            currentY = canvasY;
            currentLength = getHeight() / 7;
        }
        mPaint.setColor(Color.LTGRAY);
        canvas.drawCircle(currentX, currentY, currentLength, mPaint);
    }

    public void CircleReset(final CallbackMove MoveFun) {
        if (MoveFun != null) {
            //MoveFun.MoveUpdate(0, 0, 0);
            //使用timer
            MoveFun.MoveStorage(0, 0, 0);
        }

        CIRCLE_INIT = true;
        invalidate();
    }

    public void CircleUpdate(float updateX, float updateY, final CallbackMove MoveFun, final CallbackFeedback FeedbackFun) {
        int MoveFlag;

        MoveFlag = SetCircleUpdate (updateX, updateY);

        if (MoveFun != null) {
            MoveControl(MoveFun);
        }

        if ((MoveFlag == UPDATE_POINT_OUTSIDE) && (FeedbackFun != null)) {
            long[] pattern = {0, 50};   // 停止 开启 停止 开启...
            FeedbackFun.VibratorControl(pattern, -1);   //第二个参数：重复上面的pattern 如果只想震动一次，设为-1
        }

        currentLength = getHeight() / 7;
        CIRCLE_INIT = false;
        invalidate();
    }

    public int SetCircleUpdate(float updateX, float updateY) {
        //根据点的位置，计算画圆的圆心
        float tmpX = updateX - canvasX;
        float tmpY = updateY - canvasY;
        float pointDistance = (float)Math.sqrt((Math.pow(tmpX, 2) + Math.pow(tmpY, 2)));

        if (pointDistance < canvasLength) {     //输入值有效，更新当前值
            currentX = updateX;
            currentY = updateY;
            return UPDATE_POINT_VALID;
        } else {
            if ((currentX != canvasX) || (currentY != canvasY)) {   //输入值出界，复位当前值
                currentX = canvasX;
                currentY = canvasY;
                return UPDATE_POINT_OUTSIDE;
            } else {    //输入值无效，当前值不变
                return UPDATE_POINT_INVALID;
            }
        }
    }

    public void CircleOffset(float offsetX, float offsetY, final CallbackMove MoveFun) {
        SetCircleOffset(offsetX, offsetY);

        if (MoveFun != null) {
            MoveControl(MoveFun);
        }

        currentLength = getHeight() / 7;
        CIRCLE_INIT = false;
        invalidate();
    }

    public void SetCircleOffset(float offsetX, float offsetY) {
        //根据偏移量，计算画圆的圆心
        currentX = canvasX + (float)(canvasLength * offsetX / 10.0);
        currentY = canvasY + (float)(canvasLength * offsetY / 10.0);
    }

    public void MoveControl(final CallbackMove MoveFun) {
        //数据在检测图中的反馈
        //确定不同的方向
        float offsetDegree;
        //此处暂设置角度范围[-10, 10]，即借用了X轴的值
        if (currentY - canvasY > 0) {
            offsetDegree = -(currentX - canvasX) * 10 / canvasLength;
        } else {
            offsetDegree = (currentX - canvasX) * 10 / canvasLength;
        }

        if (MoveFun != null) {
            //MoveFun.MoveUpdate(0, (currentY - canvasY) * 10 / canvasLength, offsetDegree);
            //使用timer
            MoveFun.MoveStorage(0, (currentY - canvasY) * 10 / canvasLength, offsetDegree);
        }
    }
}
