package com.admin.baidumapdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.admin.baidumapdemo.officiallocationdemo.OfficialLocationHomeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.tv_common_map)
    TextView tvCommonMap;
    @BindView(R.id.tv_search_address)
    TextView tvSearchAddress;
    @BindView(R.id.tv_location)
    TextView tvLocation;
    @BindView(R.id.tv_bus_line)
    TextView tvBusLine;
    @BindView(R.id.tv_route_line)
    TextView tvRouteLine;
    @BindView(R.id.tv_official_location_demo)
    TextView tvOfficialLocationDemo;

    private boolean isGetPermission = false;
    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private final static int RESULT_SUCCESS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        getPermission();
    }

    /**
     * 申请权限
     */
    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i = ContextCompat.checkSelfPermission(HomeActivity.this, permissions[0]);
            if (i != PackageManager.PERMISSION_GRANTED) {
                startRequestPermission();
            } else {
                isGetPermission = true;
            }
        } else {
            isGetPermission = true;
        }
    }

    // 开始提交请求权限
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(HomeActivity.this, permissions, RESULT_SUCCESS);
    }

    // 用户权限 申请 的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RESULT_SUCCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isGetPermission = true;
                }
            }
        }
    }

    @OnClick({R.id.tv_common_map, R.id.tv_search_address, R.id.tv_location, R.id.tv_bus_line, R.id.tv_route_line,
            R.id.tv_official_location_demo})
    public void onViewClicked(View view) {
        if (!isGetPermission) {
            getPermission();
            return;
        }
        Intent intent = null;
        switch (view.getId()) {
            case R.id.tv_common_map:
                intent = new Intent(HomeActivity.this, ActCommonMap.class);
                startActivity(intent);
                break;
            case R.id.tv_search_address:
                intent = new Intent(HomeActivity.this, ActSearchAddress.class);
                startActivity(intent);
                break;
            case R.id.tv_location:
//                intent = new Intent(HomeActivity.this, ActLocation.class);
                intent = new Intent(HomeActivity.this, ActNewLocation.class);
                startActivity(intent);
                break;
            case R.id.tv_bus_line:
                intent = new Intent(HomeActivity.this, ActBusLineMap.class);
                startActivity(intent);
                break;
            case R.id.tv_route_line:
                intent = new Intent(HomeActivity.this, ActRoutePlanMap.class);
                startActivity(intent);
                break;
            case R.id.tv_official_location_demo:
                intent = new Intent(HomeActivity.this, OfficialLocationHomeActivity.class);
                startActivity(intent);
                break;
        }
    }
}
