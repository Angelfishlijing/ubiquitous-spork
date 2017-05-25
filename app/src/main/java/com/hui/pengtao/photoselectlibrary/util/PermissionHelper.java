package com.hui.pengtao.photoselectlibrary.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.hui.pengtao.photoselectlibrary.R;


/**
 * Created by lyh on 2016/9/7.
 */
public class PermissionHelper {
    private Context mContext;
    public static final String PACKAGE = "package:";
    private AlertDialog alertDialog;


    public PermissionHelper(Context context) {
        this.mContext = context;
    }

    /**
     * 判断权限集合
     *
     * @param permissions 检测权限的集合
     * @return 权限已全部获取返回true，未全部获取返回false
     */
    public boolean checkPermissions(String... permissions) {
        for (String permission : permissions) {
            if (!checkPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断权限是否获取
     *
     * @param permission 权限名称
     * @return 已授权返回true，未授权返回false
     */
    public boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 判断权限集合
     *
     * @param permissions 检测权限的集合
     * @return 权限已全部获取返回true，未全部获取返回false
     */
    private boolean shouldShowRequestPermissionRationales(String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)
                    mContext, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断权限集合
     *
     * @param permissions 检测权限的集合
     * @return 权限已全部获取返回true，未全部获取返回false
     */
    private boolean shouldShowRequestPermissionRationalesWithFragment(Fragment Fragment, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)
                    mContext, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取权限
     *
     * @param resultCode
     * @return
     */
    public void permissionsCheck(int resultCode, String... permissions) {
        // 注意这里要使用shouldShowRequestPermissionRationale而不要使用requestPermission方法
        // 因为requestPermissions方法会显示不在询问按钮
        if (shouldShowRequestPermissionRationales(permissions)) {
            //如果用户以前拒绝过改权限申请，则给用户提示 z
            showMissingPermissionDialog();
        } else {
            //进行权限请求
            ActivityCompat.requestPermissions((Activity) mContext,
                    permissions,
                    resultCode);
        }
//        ActivityCompat.requestPermissions((Activity) mContext, new String[]{permission},resultCode);
    }


    public void permissionsCheckWithFragment(Fragment fragment, int resultCode, String... permissions) {
        // 注意这里要使用shouldShowRequestPermissionRationale而不要使用requestPermission方法
        // 因为requestPermissions方法会显示不在询问按钮
        if (shouldShowRequestPermissionRationalesWithFragment(fragment, permissions)) {
            //如果用户以前拒绝过改权限申请，则给用户提示 z
            showMissingPermissionDialog();
        } else {
            //进行权限请求
            fragment.requestPermissions(
                    permissions,
                    resultCode);
        }
    }

    // 显示缺失权限提示
    private void showMissingPermissionDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        alertDialog = builder.create();

        builder.setMessage(mContext.getString(R.string.missing_permission_text));
        // 拒绝, 退出应用
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                ((Activity) mContext).finish();
            }
        });

        builder.setPositiveButton(R.string.setting_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                startAppSettings();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse(PACKAGE + mContext.getPackageName()));
                mContext.startActivity(intent);

            }
        });
        builder.setCancelable(false);
        builder.show();

    }

    public void dismissDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            ((Activity) mContext).finish();
        }
    }

    public boolean isPermissionGrant(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // 启动应用的设置
    public void startAppSettings() {

        showMissingPermissionDialog();
    }


    private String READ_PERMISSIONS = Manifest.permission.READ_EXTERNAL_STORAGE;
    private String[] PERMISSIONS_GROUP = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    public static int PERMISSION_REQUEST_CAMERA_CODE = 0x112;

    public boolean checkCameraAndStorageWithActivity() {
        //权限没有授权，进入授权界面
        if (!checkPermissions(PERMISSIONS_GROUP)) {
            permissionsCheck(PERMISSION_REQUEST_CAMERA_CODE, PERMISSIONS_GROUP);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkStorageWithActivity() {
        //权限没有授权，进入授权界面
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionsCheck(PERMISSION_REQUEST_CAMERA_CODE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkCameraAndStorageWithFragment(Fragment fragment) {
        //权限没有授权，进入授权界面
        if (!checkPermissions(PERMISSIONS_GROUP)) {
            permissionsCheckWithFragment(fragment, PERMISSION_REQUEST_CAMERA_CODE, PERMISSIONS_GROUP);
            return false;
        } else {
            return true;
        }
    }


}
