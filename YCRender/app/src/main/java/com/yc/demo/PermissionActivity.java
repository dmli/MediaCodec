package com.yc.demo;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldm on 17/3/6.
 * 负责做权限处理
 */

public abstract class PermissionActivity extends Activity {

    /**
     * 申请权限时的requestCode ID
     */
    private static final int APP_PERMISSION_REQUEST_CODE = 2001;

    /**
     * APP权限状态存储文件名称
     */
    private static final String APP_PERMISSION_FILE_NAME = "app_permission_file";

    /**
     * TAG
     */
    private String _TAG = getClass().getSimpleName();

    /**
     * 需要的权限可以在这个方法中返回
     * {@link android.Manifest.permission}
     * {@link android.Manifest.permission_group}
     *
     * @return new String[]{
     * Manifest.permission.CAMERA,
     * Manifest.permission.READ_EXTERNAL_STORAGE,
     * Manifest.permission.WRITE_EXTERNAL_STORAGE,...}
     */
    protected abstract String[] permissions();

    private SharedPreferences sharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkPermission()) {
            onAuthorizeSuccess();
        }

    }


    @SuppressLint("ApplySharedPref")
    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        boolean onInit = true;
        String[] permissionList = permissions();
        if (permissionList != null && permissionList.length > 0) {
            List<String> ps = findDeniedPermissions(this, permissionList);
            sharedPreferences = getSharedPreferences(APP_PERMISSION_FILE_NAME, Activity.MODE_PRIVATE);
            int code = sharedPreferences.getInt(_TAG, 0);
            if (ps.size() > 0 && code == 0) {
                onInit = false;
                SharedPreferences.Editor sEdit = sharedPreferences.edit();
                sEdit.putInt(_TAG, 1);
                sEdit.commit();
                requestPermissions(ps.toArray(new String[ps.size()]), APP_PERMISSION_REQUEST_CODE);
            }
        }
        return onInit;
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    public static List<String> findDeniedPermissions(Activity activity, String... permission) {
        List<String> denyPermissions = new ArrayList<>();
        for (String value : permission) {
            if (activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(value);
            }
        }
        return denyPermissions;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
            }
        }
        if (deniedPermissions.size() > 0) {
            onAuthorizeFail();
        } else {
            onAuthorizeSuccess();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onDestroy() {
        if (sharedPreferences != null) {
            SharedPreferences.Editor sEdit = sharedPreferences.edit();
            sEdit.putInt(_TAG, 0);
            sEdit.commit();
            sharedPreferences = null;
        }
        super.onDestroy();
    }

    public abstract void onAuthorizeFail();

    public void onAuthorizeSuccess() {
    }

}
