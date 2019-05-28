package com.txwk.totalblesdk_android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.clj.fastble.BleManager;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //rxpermission申请危险权限
        requestRxPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH});

    }

    public void 血糖尿酸(View view) {
        startActivity(new Intent(this,BleBloodSugarActivity.class));
    }

    public void 血压(View view) {
        startActivity(new Intent(this,BleBloodPressureActivity.class));
    }

    public void 血氧心率(View view) {
        startActivity(new Intent(this,BleHeartrateOxygenActivity.class));
    }

    public void 体温(View view) {
        startActivity(new Intent(this,BleTemperatureActivity.class));
    }

    public void 体重(View view) {
        startActivity(new Intent(this,BleWeightActivity.class));
    }

    public void 尿常规(View view) {
        startActivity(new Intent(this,BleUrineRoutineActivity.class));
    }


    private void requestRxPermissions(String... permission ) {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .requestEach(permission)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) {
                        if (permission.granted) {
                            // 用户已经同意该权限
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,
                            // 那么下次再次启动时，还会提示请求权限的对话框
                            finish();
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            finish();
                        }
                    }
                });
    }


}
