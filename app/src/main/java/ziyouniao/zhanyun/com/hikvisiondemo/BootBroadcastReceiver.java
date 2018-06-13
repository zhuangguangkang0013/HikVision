package ziyouniao.zhanyun.com.hikvisiondemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.provider.ContactsContract.Intents.Insert.ACTION;

/**
 * Created by ZY004Engineer on 2018/6/11.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            intent = new Intent(context, MainActivity.class);
            //1.如果自启动APP，参数为需要自动启动的应用包名
            //Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            //下面这句话必须加上才能开机自动运行app的界面
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //2.如果自启动Activity
            //context.startActivity(intent);
            //3.如果自启动服务
            context.startService(intent);
        }
    }
}
