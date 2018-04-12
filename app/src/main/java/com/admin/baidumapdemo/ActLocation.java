package com.admin.baidumapdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 定位功能
 */
public class ActLocation extends AppCompatActivity {

    @BindView(R.id.map_view)
    MapView mapView;
    @BindView(R.id.btn_my_address)
    Button btnMyAddress;

    private BaiduMap mBaiduMap;
    public LocationClient mLocationClient = null;
    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口，
    // 原有BDLocationListener接口暂时同步保留。具体介绍请参考后文中的说明
    public BDAbstractLocationListener myListener = new MyLocationListener();

    private double coordinateX = 10.598359;
    private double coordinateY = 84.067147;
    private boolean isFirstLocation = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_location);
        ButterKnife.bind(this);

        mBaiduMap = mapView.getMap();
        //设置地图级别为18
        initMap(18);

        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数

        mLocationClient.start();
        initLocation();
    }

    private void initLocation() {

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认bd09ll表示百度经纬度坐标，设置返回的定位结果坐标系

        int span = 100;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

//        option.setIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

//        option.setWifiValidTime(5*60*1000);
        //可选，7.2版本新增能力，如果您设置了这个接口，首次启动定位时，会先判断当前WiFi是否超出有效期，超出有效期的话，会先重新扫描WiFi，然后再定位

        mLocationClient.setLocOption(option);
    }

    @OnClick({R.id.btn_my_address})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_my_address:
                if(isFirstLocation){
                    //设置“我的位置”为中心点
                    initLocation(coordinateX,coordinateY);
                }
                break;
        }
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            if (location != null) {
                if(!isFirstLocation){
                    isFirstLocation = true;
                }
                coordinateX = location.getLatitude();
                coordinateY = location.getLongitude();
                //设置一开始的中心点
                initLocation(location.getLatitude(), location.getLongitude());
                //显示标记图标
                drawMark(location.getLatitude(), location.getLongitude());
                //以此为中心绘制指定区域圆形
                drawCircle(location.getLatitude(), location.getLongitude());
            }

            //获取定位结果
            location.getTime();    //获取定位时间
            location.getLocationID();    //获取定位唯一ID，v7.2版本新增，用于排查定位问题
            location.getLocType();    //获取定位类型
            location.getLatitude();    //获取纬度信息
            location.getLongitude();    //获取经度信息
            location.getRadius();    //获取定位精准度
            location.getAddrStr();    //获取地址信息
            location.getCountry();    //获取国家信息
            location.getCountryCode();    //获取国家码
            location.getCity();    //获取城市信息
            location.getCityCode();    //获取城市码
            location.getDistrict();    //获取区县信息
            location.getStreet();    //获取街道信息
            location.getStreetNumber();    //获取街道码
            location.getLocationDescribe();    //获取当前位置描述信息
            location.getPoiList();    //获取当前位置周边POI信息

            location.getBuildingID();    //室内精准定位下，获取楼宇ID
            location.getBuildingName();    //室内精准定位下，获取楼宇名称
            location.getFloor();    //室内精准定位下，获取当前位置所处的楼层信息

            if (location.getLocType() == BDLocation.TypeGpsLocation) {

                //当前为GPS定位结果，可获取以下信息
                location.getSpeed();    //获取当前速度，单位：公里每小时
                location.getSatelliteNumber();    //获取当前卫星数
                location.getAltitude();    //获取海拔高度信息，单位米
                location.getDirection();    //获取方向信息，单位度

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {

                //当前为网络定位结果，可获取以下信息
                location.getOperators();    //获取运营商信息

            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {

                //当前为网络定位结果

            } else if (location.getLocType() == BDLocation.TypeServerError) {

                //当前网络定位失败
                //可将定位唯一ID、IMEI、定位失败时间反馈至loc-bugs@baidu.com

            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {

                //当前网络不通

            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {

                //当前缺少定位依据，可能是用户没有授权，建议弹出提示框让用户开启权限
                //可进一步参考onLocDiagnosticMessage中的错误返回码

            }
        }

        /**
         * 回调定位诊断信息，开发者可以根据相关信息解决定位遇到的一些问题
         * 自动回调，相同的diagnosticType只会回调一次
         *
         * @param locType           当前定位类型
         * @param diagnosticType    诊断类型（1~9）
         * @param diagnosticMessage 具体的诊断信息释义
         */
        public void onLocDiagnosticMessage(int locType, int diagnosticType, String diagnosticMessage) {

            if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_BETTER_OPEN_GPS) {

                //建议打开GPS

            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_BETTER_OPEN_WIFI) {

                //建议打开wifi，不必连接，这样有助于提高网络定位精度！

            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CHECK_LOC_PERMISSION) {

                //定位权限受限，建议提示用户授予APP定位权限！

            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CHECK_NET) {

                //网络异常造成定位失败，建议用户确认网络状态是否异常！

            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CLOSE_FLYMODE) {

                //手机飞行模式造成定位失败，建议用户关闭飞行模式后再重试定位！

            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_INSERT_SIMCARD_OR_OPEN_WIFI) {

                //无法获取任何定位依据，建议用户打开wifi或者插入sim卡重试！

            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_OPEN_PHONE_LOC_SWITCH) {

                //无法获取有效定位依据，建议用户打开手机设置里的定位开关后重试！

            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_SERVER_FAIL) {

                //百度定位服务端定位失败
                //建议反馈location.getLocationID()和大体定位时间到loc-bugs@baidu.com

            } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_FAIL_UNKNOWN) {

                //无法获取有效定位依据，但无法确定具体原因
                //建议检查是否有安全软件屏蔽相关定位权限
                //或调用LocationClient.restart()重新启动后重试！

            }
        }

    }

    /**
     * 设置地图级别为18
     *
     * @param grade
     */
    private void initMap(int grade) {
        //描述地图将要发生的变化，使用工厂类MapStatusUpdateFactory创建，
        //设置级别为18，进去就是18了，默认是12
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomTo(grade);
        mBaiduMap.setMapStatus(mapStatusUpdate);
        //是否显示缩放按钮
        //mMapView.showZoomControls(false);
    }

    /**
     * 绘制mark覆盖物
     */
    private void drawMark(double varX, double varY) {
        MarkerOptions markerOptions = new MarkerOptions();
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_mark); // 描述图片
        markerOptions.position(new LatLng(varX, varY)) // 设置位置
                .icon(bitmap) // 加载图片
                .draggable(true) // 支持拖拽
                .title("当前位置"); // 显示文本
        //把绘制的圆添加到百度地图上去
        mBaiduMap.addOverlay(markerOptions);
    }

    /**
     * 设置一开始中心点的经纬度坐标
     *
     * @param varX
     * @param varY
     */
    private void initLocation(double varX, double varY) {
        //经纬度(纬度，经度)
        LatLng latlng = new LatLng(varX, varY);
        MapStatusUpdate mapStatusUpdate_circle = MapStatusUpdateFactory.newLatLng(latlng);
        mBaiduMap.setMapStatus(mapStatusUpdate_circle);
    }

    /**
     * 绘制圆
     */
    private void drawCircle(double varX, double varY) {
        // 1.创建自己
        CircleOptions circleOptions = new CircleOptions();
        // 2.设置数据 以“此位置”为圆心，1000米为半径绘制
        circleOptions.center(new LatLng(varX, varY))//中心
                .radius(1000)  //半径
                .fillColor(0x00000000)//填充圆的颜色 0x60FF0000
                .stroke(new Stroke(10, 0x600FF000));  //边框的宽度和颜色
        //把绘制的圆添加到百度地图上去
        mBaiduMap.addOverlay(circleOptions);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

}
