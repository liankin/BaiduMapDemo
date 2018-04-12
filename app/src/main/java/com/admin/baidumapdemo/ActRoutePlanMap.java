package com.admin.baidumapdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.admin.baidumapdemo.overlayutil.BikingRouteOverlay;
import com.admin.baidumapdemo.overlayutil.DrivingRouteOverlay;
import com.admin.baidumapdemo.overlayutil.TransitRouteOverlay;
import com.admin.baidumapdemo.overlayutil.WalkingRouteOverlay;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 路线规划：
 * 根据城市名、起始点、终点，规划骑行、驾车、公交、步行等线路
 */
public class ActRoutePlanMap extends AppCompatActivity
        implements   OnGetRoutePlanResultListener {

    @BindView(R.id.map_view)
    MapView mapView;

    @BindView(R.id.ed_city)
    EditText edCity;
    @BindView(R.id.ed_start_bus_station)
    EditText edStartBusStation;
    @BindView(R.id.ed_end_bus_station)
    EditText edEndBusStation;
    @BindView(R.id.tv_choose_biking)
    TextView tvChooseBiking;
    @BindView(R.id.tv_choose_car)
    TextView tvChooseCar;
    @BindView(R.id.tv_choose_bus)
    TextView tvChooseBus;
    @BindView(R.id.tv_choose_walk)
    TextView tvChooseWalk;

    private BaiduMap mBaiduMap;

    private RoutePlanSearch routePlanSearch;//路径规划搜索接口

    private BikingRouteOverlay bikingOverlay;
    private DrivingRouteOverlay drivingOverlay;
    private TransitRouteOverlay transOverlay;
    private WalkingRouteOverlay walkingOverlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_routeplanmap);
        ButterKnife.bind(this);
        mBaiduMap = mapView.getMap();

        init();
    }

    @OnClick({ R.id.tv_choose_biking,
            R.id.tv_choose_car, R.id.tv_choose_bus, R.id.tv_choose_walk})
    public void onViewClicked(View view) {
        //路径规划中的出行节点信息,出行节点包括：起点，终点，途经点
        PlanNode fromPlanNode = PlanNode.withCityNameAndPlaceName(edCity.getText().toString(), edStartBusStation.getText().toString());
        PlanNode toPlanNode = PlanNode.withCityNameAndPlaceName(edCity.getText().toString(), edEndBusStation.getText().toString());
        switch (view.getId()) {
            /**
             * 各种路线规划的查询
             */
            case R.id.tv_choose_biking:
                //骑行路线
                routePlanSearch.bikingSearch(new BikingRoutePlanOption().from(fromPlanNode).to(toPlanNode));
                break;
            case R.id.tv_choose_car:
                //驾车路线
                routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(fromPlanNode).to(toPlanNode));
                break;
            case R.id.tv_choose_bus:
                //换乘公共交通工具的路线
                routePlanSearch.transitSearch(new TransitRoutePlanOption().city(edCity.getText().toString()).from(fromPlanNode).to(toPlanNode));
                break;
            case R.id.tv_choose_walk:
                //步行路线
                routePlanSearch.walkingSearch(new WalkingRoutePlanOption().from(fromPlanNode).to(toPlanNode));
                break;
        }
    }

    public void init() {

        //路径规划搜索接口
        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener(this);

        //骑行覆盖物添加并监听
        bikingOverlay = new BikingRouteOverlay(mBaiduMap);
        //驾车覆盖物添加并监听
        drivingOverlay = new DrivingRouteOverlay(mBaiduMap);
        //换乘覆盖物添加并监听
        transOverlay = new TransitRouteOverlay(mBaiduMap);
        //步行覆盖物添加并监听
        walkingOverlay = new WalkingRouteOverlay(mBaiduMap);
        //TODO: 添加各种路线覆盖物的点击事件
        mBaiduMap.setOnMarkerClickListener(bikingOverlay);
        mBaiduMap.setOnMarkerClickListener(drivingOverlay);
        mBaiduMap.setOnMarkerClickListener(transOverlay);
        mBaiduMap.setOnMarkerClickListener(walkingOverlay);
    }

    /**
     * 以下四个方法是路线规划搜索监听后的回调
     * a.步行回调
     *
     * @param walkingRouteResult
     */

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
        if (walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            showToast( "抱歉，未找到结果");
            return;
        }
        showToast( "步行线路条数：" + walkingRouteResult.getRouteLines().size());
        List<WalkingRouteLine> routeLines = walkingRouteResult.getRouteLines();
        mBaiduMap.clear();
        walkingOverlay.removeFromMap();
        walkingOverlay.setData(routeLines.get(0));
        walkingOverlay.addToMap();
        walkingOverlay.zoomToSpan();
    }

    /**
     * b.换乘路线结果回调
     *
     * @param transitRouteResult
     */

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        if (transitRouteResult == null || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            showToast("抱歉，未找到结果" );
            return;
        }
        showToast( "公交线路条数：" + transitRouteResult.getRouteLines().size());
        List<TransitRouteLine> routeLines = transitRouteResult.getRouteLines();
        mBaiduMap.clear();
        transOverlay.removeFromMap();
        transOverlay.setData(routeLines.get(0));
        transOverlay.addToMap();
        transOverlay.zoomToSpan();
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    /**
     * c.驾车路线结果回调
     *
     * @param drivingRouteResult
     */

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
        if (drivingRouteResult == null || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            showToast( "抱歉，未找到结果" );
            return;
        }
        showToast( "驾车线路条数：" + drivingRouteResult.getRouteLines().size());
        List<DrivingRouteLine> routeLines = drivingRouteResult.getRouteLines();
        mBaiduMap.clear();
        drivingOverlay.removeFromMap();
        drivingOverlay.setData(routeLines.get(0));
        drivingOverlay.addToMap();
        drivingOverlay.zoomToSpan();
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    /**
     * d.骑行路线结果回调
     *
     * @param bikingRouteResult
     */

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
        if (bikingRouteResult == null || bikingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            showToast( "抱歉，未找到结果");
            return;
        }
        showToast( "骑行线路条数：" + bikingRouteResult.getRouteLines().size());
        List<BikingRouteLine> routeLines = bikingRouteResult.getRouteLines();
        mBaiduMap.clear();
        bikingOverlay.removeFromMap();
        bikingOverlay.setData(routeLines.get(0));
        bikingOverlay.addToMap();
        bikingOverlay.zoomToSpan();
    }

    private void showToast(String str){
        Toast.makeText(ActRoutePlanMap.this,str,Toast.LENGTH_SHORT).show();
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
        routePlanSearch.destroy();
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
