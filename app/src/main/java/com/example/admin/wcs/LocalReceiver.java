package com.example.admin.wcs;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.WindowManager;
import android.widget.Toast;

public class LocalReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        String mMsg = intent.getExtras().getString("msg");

        if (mMsg.equals("SOCKET_DISCONNECT")) {     //若是socket掉线，进入处理逻辑
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle(R.string.app_name);
            dialogBuilder.setMessage("You have been offline. Please try to login again.");
            dialogBuilder.setCancelable(false);
            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCollector.finishAll(); // 销毁所有活动

                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent); // 重新启动LoginActivity
                }
            });

            AlertDialog alertDialog = dialogBuilder.create();
            // 需要设置AlertDialog的类型，保证在广播接收器中可以正常弹出
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alertDialog.show();
        } else {        //若是接收错误，打印错误类型
            Toast.makeText(context, mMsg, Toast.LENGTH_SHORT).show();
        }
    }
}
