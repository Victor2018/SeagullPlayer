package com.victor.player.library.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by victgor on 2017/10/16.
 */

public class AppUtil {
    // 应用版本
    public static int getAppVersionCode(Context context) {
        String packageName = context.getPackageName();
        try {
            int versionCode = context.getPackageManager().getPackageInfo(
                    packageName, 0).versionCode;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("System fault!!!", e);
        }
    }
    public static String getAppVersionName(Context context) {
        String packageName = context.getPackageName();
        try {
            String versionName = context.getPackageManager().getPackageInfo(
                    packageName, 0).versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("System fault!!!", e);
        }
    }

    /**
     * 获取APP名称
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        String appName ="";
        try {
            PackageManager packageManager = context.getPackageManager();

            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            appName = packageManager.getApplicationLabel(applicationInfo).toString();
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("System fault!!!", e);
        }
    }

    public static boolean isAppExist(Context context,String packageName) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> applicationInfos = packageManager.getInstalledApplications(0);
        for (ApplicationInfo info : applicationInfos) {
            if (TextUtils.equals(info.packageName, packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 启动到应用商店app详情界面
     *
     * @param appPkg    目标App的包名
     * @param marketPkg 应用商店包名 ,如果为""则由系统弹出应用商店列表供用户选择,否则调转到目标市场的应用详情界面，某些应用商店可能会失败
     */
    public static void launchAppDetail(Activity activity, String appPkg, String marketPkg) {
        try {
            if (TextUtils.isEmpty(appPkg)) return;

            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断某一个Activity是否存在任务栈里面
     * @return
     */
    public static boolean isActivityInTask(Context context,Class<?> cls){
        Intent intent = new Intent(context, cls);
        ComponentName cmpName = intent.resolveActivity(context.getPackageManager());
        boolean flag = false;
        if (cmpName != null) { // 说明系统中存在这个activity
            ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                if (taskInfo.baseActivity.equals(cmpName)) { // 说明它已经启动了
                    flag = true;
                    break;  //跳出循环，优化效率
                }
            }
        }
        return flag;
    }

    public static Activity scanForActivity(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }


}
