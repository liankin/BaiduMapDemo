package com.admin.baidumapdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.admin.baidumapdemo.overlayutil.BusLineOverlay;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 公交路线：
 * 根据城市名称、公交路线号，搜索公交路线
 */
public class ActBusLineMap extends AppCompatActivity
        implements OnGetBusLineSearchResultListener, OnGetPoiSearchResultListener {

    @BindView(R.id.map_view)
    MapView mapView;
    @BindView(R.id.ed_start_bus_station)
    EditText edStartBusStation;
    @BindView(R.id.ed_end_bus_station)
    EditText edEndBusStation;
    @BindView(R.id.tv_choose_bus)
    TextView tvChooseBus;
    @BindView(R.id.tv_previous)
    TextView tvPrevious;
    @BindView(R.id.tv_next)
    TextView tvNext;

    private BaiduMap mBaiduMap;

    private PoiSearch poiSearch;
    private BusLineSearch busLineSearch;
    private List<String> busLines = new ArrayList<>();
    private int uidPostion = 0;//当前选中第几条路线
    private BusLineResult mBusLineResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_buslinemap);
        ButterKnife.bind(this);

        mBaiduMap = mapView.getMap();

        init();
        onEvent();

    }

    /**
     * 点击事件
     * @param view
     */
    @OnClick({R.id.tv_choose_bus, R.id.tv_previous, R.id.tv_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_choose_bus:
                poiSearch.searchInCity(
                        new PoiCitySearchOption().city(
                                edStartBusStation.getText().toString()).keyword(edEndBusStation.getText().toString()));
                break;
            case R.id.tv_previous:
                if (uidPostion>0){
                    uidPostion--;
                    searchBusLine();
                }
                break;
            case R.id.tv_next:
                if (uidPostion<(busLines.size()-1)){
                    uidPostion++;
                    searchBusLine();
                }
                break;
        }
    }

    private void onEvent() {

    }

    public void init() {
        //检索查询初始化并监听
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(this);
        //路线查询初始化并监听
        busLineSearch = BusLineSearch.newInstance();
        busLineSearch.setOnGetBusLineSearchResultListener(this);
        //公交路线覆盖物，并设置路线的点击事件
        overlay = new BusLineOverlay(mBaiduMap){
            @Override
            public boolean onBusStationClick(int index) {
                if (mBusLineResult.getStations() != null
                        && mBusLineResult.getStations().get(index) != null) {
                    showToast( mBusLineResult.getStations().get(index).getTitle() );
                }
                return true;
            };
        };
        mBaiduMap.setOnMarkerClickListener(overlay);
    }


    /**
     * 【1】首先检索查询
     *
     * @param poiResult
     */
    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            showToast("抱歉，未找到结果" );
            return;
        }
        if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {

            busLines.clear();
            List<PoiInfo> allPoi = poiResult.getAllPoi();
            if (allPoi == null || allPoi.size() == 0) return;
            for (int i = 0; i < allPoi.size(); i++) {
                PoiInfo poiInfo = allPoi.get(i);
                //如果是公交类型的路线，就把它的UID添加到集合中
                if (poiInfo.type == PoiInfo.POITYPE.BUS_LINE) {
                    busLines.add(poiInfo.uid);
                }
            }
            mBusLineResult=null;
            if (busLines.size() == 0) return;
            uidPostion = 0;
            showToast( "发现"+busLines.size()+"条线路！");
            busLineSearch.searchBusLine(
                    new BusLineSearchOption().city(edStartBusStation.getText().toString()).uid(busLines.get(uidPostion)));
        }
    }

    /**
     * 【2】从检索的结果中筛选出公交路线
     * 并将其添加到地图上
     *
     * @param busLineResult
     */
    private BusLineResult route = null; // 保存驾车/步行路线数据的变量，供浏览节点时使用
    private int nodeIndex = -2; // 节点索引,供浏览节点时使用
    BusLineOverlay overlay; // 公交路线绘制对象
    @Override
    public void onGetBusLineResult(BusLineResult busLineResult) {
        if (busLineResult == null || busLineResult.error != SearchResult.ERRORNO.NO_ERROR) {
            showToast( "抱歉，未找到结果" );
            return;
        }

        mBaiduMap.clear();
        mBusLineResult = busLineResult;
        nodeIndex = -1;
        overlay.removeFromMap();
        overlay.setData(busLineResult);
        overlay.addToMap();
        overlay.zoomToSpan();
        tvPrevious.setVisibility(View.VISIBLE);
        tvNext.setVisibility(View.VISIBLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("路线详情").setMessage(busLineResult.getBusLineName()).setPositiveButton("朕知道了",null).show();
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
    }

    private void searchBusLine(){
        showToast( "当前查询的路线=="+uidPostion);
        busLineSearch.searchBusLine(
                new BusLineSearchOption().city(edStartBusStation.getText().toString()).uid(busLines.get(uidPostion)));

    }

    private void showToast(String str){
        Toast.makeText(ActBusLineMap.this,str,Toast.LENGTH_SHORT).show();
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
        busLineSearch.destroy();
        poiSearch.destroy();
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
