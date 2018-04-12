package com.admin.baidumapdemo;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 普通地图：
 * 1.显示地图(普通、卫星、空白、交通图、热力图)
 * 2.标记指定的位置点、不断更新标记图标
 * 3.在指定的位置点显示自定义文字
 * 4.点击指定的位置点的标记图标，显示自定义弹出框
 * 5.指定半径、中心点画出区域圆形图
 * 6.显示指南针
 * 7.设置地图级别、中心点
 */
public class ActCommonMap extends AppCompatActivity {

    @BindView(R.id.map_view)
    MapView mapView;
    @BindView(R.id.tv_custom_map)
    TextView tvCustomMap;
    @BindView(R.id.tv_satellite_map)
    TextView tvSatelliteMap;
    @BindView(R.id.tv_blank_map)
    TextView tvBlankMap;
    @BindView(R.id.tv_traffic_map)
    TextView tvTrafficMap;
    @BindView(R.id.tv_thermal_map)
    TextView tvThermalMap;

    private BaiduMap mBaiduMap;
    //要显示的pop
    private View pop;
    //pop中的文本信息
    private TextView popWindowTitle;

    private boolean isTrafficMap = false;
    private boolean isThermalMap = false;
    private boolean isInfoWindow = false;
    private double coordinateX = 30.598359;//22.5422870000;
    private double coordinateY = 104.067147;//113.9804440000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_commonmap);
        ButterKnife.bind(this);

        initView();
    }

    /**
     * 设置地图属性等等
     */
    private void initView(){
        mBaiduMap = mapView.getMap();
        //设置地图级别为18
        initMap(18);

        //设置地图中心点
        initLocation(coordinateX, coordinateY);

        //显示指南针
        mBaiduMap.getUiSettings().setCompassEnabled(true);

        //绘制区域圆形
        drawCircle();

        //显示自定义文字
        drawText();

        //显示标记图标
        drawMark(coordinateX, coordinateY);

        //初始化popwindow,通过点击mark来控制它的显示
        initPop();

        //mark的点击事件
        // 点击某一个mark在他上放显示泡泡
        // 加载pop 添加到mapview 把他设置为隐藏 当点击的时候更新pop的位置 设置为显示
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker result) {
                showPop(result);
                return true;
            }
        });

        //隐藏pop
        pop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.setVisibility(View.INVISIBLE);
            }
        });

        //不断切换mark图标（显示多个图片的来回切换，类似于帧动画）
//        drawMarks();
    }

    /**
     * 不断更新Mark图标
     * 显示多个图片的来回切换，类似于帧动画
     * 拿上面那个方法直接改的设置icon
     * 绘制mark覆盖物
     */
    private void drawMarks() {
        MarkerOptions markerOptions = new MarkerOptions();
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_mark); // 描述图片
        ArrayList<BitmapDescriptor> bitmaps = new ArrayList<BitmapDescriptor>();
        bitmaps.add(bitmap);  //显示多个图片的来回切换，类似于帧动画
        bitmaps.add(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round));
        markerOptions.position(new LatLng(22.5422870000, 113.9804440000)) // 设置位置
                //.icon(bitmap) // 加载图片
                //切换图片
                .icons(bitmaps)
                .period(10) //切换时间
                .draggable(true) // 支持拖拽
                .title("世界之窗旁边的草房"); // 显示文本
        mBaiduMap.addOverlay(markerOptions);
    }

    /**
     * 初始化pop
     */
    private void initPop() {
        pop = View.inflate(getApplicationContext(), R.layout.pop_window_map, null);
        //必须使用百度的params
        ViewGroup.LayoutParams params = new MapViewLayoutParams.Builder().layoutMode(MapViewLayoutParams.ELayoutMode.mapMode) //按照经纬度设置
                .position(new LatLng(coordinateX, coordinateY)) //这个坐标无所谓的，但是不能传null
                .width(MapViewLayoutParams.WRAP_CONTENT)  //宽度
                .height(MapViewLayoutParams.WRAP_CONTENT)  //高度
                .build();
        mapView.addView(pop, params);
        //先设置隐藏，点击的时候显示
        pop.setVisibility(View.INVISIBLE);
        popWindowTitle = (TextView) pop.findViewById(R.id.popWindowTitle);
    }

    /**
     * 显示加载pop
     */
    private void showPop( Marker result){
        if( !isInfoWindow){
            //处理点击 ,当点击的时候更新并且显示位置
            ViewGroup.LayoutParams params = new MapViewLayoutParams.Builder().
                    layoutMode(MapViewLayoutParams.ELayoutMode.mapMode) //按照经纬度设置位置
                    .position(result.getPosition()) //这个坐标无所谓的，但是不能传null
                    .width(MapViewLayoutParams.WRAP_CONTENT)  //宽度
                    .height(MapViewLayoutParams.WRAP_CONTENT)  //高度
                    .yOffset(-5)  //相距  正值往下  负值往上
                    .build();
            mapView.updateViewLayout(pop, params);
            //更新下title
            String title = result.getTitle();
            popWindowTitle.setText(title);
            pop.setVisibility(View.VISIBLE);
        }else{
            pop.setVisibility(View.INVISIBLE);
        }
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
                .title("世界之窗旁边的草房"); // 显示文本
        //把绘制的圆添加到百度地图上去
        mBaiduMap.addOverlay(markerOptions);
    }

    /**
     * 绘制文字
     */
    private void drawText() {
        TextOptions textOptions = new TextOptions();
        textOptions.fontColor(Color.RED) //设置字体颜色
                .text("自定义文字覆盖物")  //设置显示文本
                .position(new LatLng(22.5422870000, 113.9804440000))   //设置显示坐标
                .fontSize(20) //设置文本大小
                .typeface(Typeface.SERIF)  //设置字体 Android的字体就三种，对称的，不对称的，等宽的
                .rotate(30);  //设置旋转角度
        //把绘制的圆添加到百度地图上去
        mBaiduMap.addOverlay(textOptions);
    }

    /**
     * 绘制圆
     */
    private void drawCircle() {
        // 1.创建自己
        CircleOptions circleOptions = new CircleOptions();
        // 2.设置数据 以世界之窗为圆心，1000米为半径绘制
        circleOptions.center(new LatLng(22.5422870000, 113.9804440000))//中心
                .radius(1000)  //半径
                .fillColor(0x60FF0000)//填充圆的颜色
                .stroke(new Stroke(10, 0x600FF000));  //边框的宽度和颜色
        //把绘制的圆添加到百度地图上去
        mBaiduMap.addOverlay(circleOptions);
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

    @OnClick({R.id.tv_custom_map, R.id.tv_satellite_map, R.id.tv_blank_map, R.id.tv_traffic_map, R.id.tv_thermal_map})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_custom_map:
                //普通地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.tv_satellite_map:
                //卫星地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.tv_blank_map:
                //空白地图, 基础地图瓦片将不会被渲染。在地图类型中设置为NONE，
                // 将不会使用流量下载基础地图瓦片图层。使用场景：与瓦片图层一起使用，
                // 节省流量，提升自定义瓦片图下载速度。
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);
                break;
            case R.id.tv_traffic_map:
                if (isTrafficMap == false) {
                    //开启交通图
                    mBaiduMap.setTrafficEnabled(true);
                    tvTrafficMap.setText("关闭交通图");
                    isTrafficMap = true;
                } else {
                    //开启交通图
                    mBaiduMap.setTrafficEnabled(false);
                    tvTrafficMap.setText("开启交通图");
                    isTrafficMap = false;
                }
                break;
            case R.id.tv_thermal_map:
                if (isThermalMap == false) {
                    //城市热力图
                    mBaiduMap.setBaiduHeatMapEnabled(true);
                    tvThermalMap.setText("关闭热力图");
                    isThermalMap = true;
                } else {
                    //开启交通图
                    mBaiduMap.setBaiduHeatMapEnabled(false);
                    tvThermalMap.setText("开启热力图");
                    isThermalMap = false;
                }
                break;
        }
    }

}
