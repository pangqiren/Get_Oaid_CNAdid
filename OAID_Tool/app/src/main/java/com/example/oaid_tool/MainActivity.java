package com.example.oaid_tool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oaid_tool.helpers.CNAdidHelper;
import com.example.oaid_tool.helpers.DevicesIDsHelper;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;


import java.util.List;

public class MainActivity extends AppCompatActivity implements DevicesIDsHelper.AppIdsUpdater {

  private String TAG = MainActivity.class.getSimpleName();
  private TextView mTvOaid;
  private TextView mTvAdid;
  private String mOAID;
  private DevicesIDsHelper mDevicesIDsHelper;

  private Conetxt mConetxt;

  private String[] PERMISSIONS_All_NEED = {
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
  };

  private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case 1:
          mTvOaid.setText(mOAID);
          break;
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
	mContext = this.getApplicationContext();
    checkAllPermissions(this);

    mTvOaid = findViewById(R.id.tv_oaid);
    mTvAdid = findViewById(R.id.tv_adid);
  }

  public void checkAllPermissions(final Context context) {
    AndPermission.with(context)
        .permission(PERMISSIONS_All_NEED)
        .onGranted(new Action() {
          @Override
          public void onAction(List<String> list) {
          }
        })

        .onDenied(new Action() {
          @Override
          public void onAction(List<String> list) {
            if (AndPermission.hasAlwaysDeniedPermission(context, PERMISSIONS_All_NEED)) {
              Toast.makeText(context, "Denied Permission", Toast.LENGTH_SHORT).show();
              Log.e(TAG, "Denied Permission");
              showNormalDialog(MainActivity.this);
            }
          }
        })
        .start();
  }


  /**
   * 获取设备当前 OAID
   *
   * @param view
   */
  public void getOAID(View view) {
    mDevicesIDsHelper = new DevicesIDsHelper(this);
    mDevicesIDsHelper.getOAID(this);
  }

  /**
   * OAID 回调事件
   *
   * @param ids
   */
  @Override public void OnIdsAvalid(@NonNull String ids) {
    mOAID = ids;
    Message msg = Message.obtain();
    msg.what = 1;
    handler.sendMessage(msg);
    Log.e(TAG, "OnIdsAvalid====>" + ids);
  }

  /**
   * 获取 CN Adid
   *
   * @param view
   */
  public void getCNAdid(View view) {
    String cnadid = CNAdidHelper.getInstance().readCNAdid(mContext);
  }


  private void showNormalDialog(final Context context) {
    final AlertDialog.Builder normalDialog =
        new AlertDialog.Builder(context);
    normalDialog.setTitle("去申请权限");
    normalDialog.setMessage("部分权限被你禁止了，可能误操作，可能会影响部分功能，是否去要去重新设置？");
    normalDialog.setPositiveButton("是",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            getAppDetailSettingIntent(context);
          }
        });
    normalDialog.setNegativeButton("否",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        });
    normalDialog.show();
  }

  static private void getAppDetailSettingIntent(Context context) {
    Intent localIntent = new Intent();
    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    if (Build.VERSION.SDK_INT >= 9) {
      localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
      localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
    }
    else if (Build.VERSION.SDK_INT <= 8) {
      localIntent.setAction(Intent.ACTION_VIEW);
      localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
      localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
    }
    context.startActivity(localIntent);
  }
}
