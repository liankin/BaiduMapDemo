package com.admin.baidumapdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.admin.baidumapdemo.overlayutil.PoiOverlay;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 搜索功能：
 * 范围搜索、周边搜索
 */
public class ActSearchAddress extends AppCompatActivity {

    @BindView(R.id.ed_search)
    EditText edSearch;
    @BindView(R.id.btn_range_search)
    Button btnRangeSearch;
    @BindView(R.id.btn_around_search)
    Button btnAroundSearch;
    @BindView(R.id.map_view)
    MapView mapView;

    //初始化一个搜索广播
    private PoiSearch poiSearch;

    private BaiduMap mBaiduMap;
    private double coordinateX = 22.5422870000;
    private double coordinateY = 113.9804440000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_searchaddress);
        ButterKnife.bind(this);

        mBaiduMap = mapView.getMap();

        //设置地图级别为18
        initMap(18);

        //设置地图中心点
        initLocation(coordinateX, coordinateY);
    }

    @OnClick({R.id.btn_range_search, R.id.btn_around_search})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_range_search:
                //范围搜索
                //搜索：指定范围、关键字,周边搜索和范围搜索基本一致
                if (!TextUtils.isEmpty(edSearch.getText().toString().trim())) {
                    rangeSearch(edSearch.getText().toString().trim());
                } else {
                    Toast.makeText(getApplicationContext(), "请输入关键字", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_around_search:
                //周边圆形搜索
                //搜索：指定关键字、圆半径、中心点
                if (!TextUtils.isEmpty(edSearch.getText().toString().trim())) {
                    aroundSearch(edSearch.getText().toString().trim());
                } else {
                    Toast.makeText(getApplicationContext(), "请输入关键字", Toast.LENGTH_SHORT).show();
                }
                break;
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
     * 范围搜索:指定范围、关键字进行搜索
     * 标记处此范围内所有与关键字相关的地方
     *
     * @param keyWord
     */
    private void rangeSearch(String keyWord) {
        // 实例化搜索方法
        PoiSearch newInstance = PoiSearch.newInstance();
        newInstance.setOnGetPoiSearchResultListener(new MyRangeSearchListener());
        // 发出搜索的请求 范围检索
        PoiBoundSearchOption boundOption = new PoiBoundSearchOption();
        // 确定两点坐标(东北，西南),在此两点之间的范围内搜索
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                // 这里我们随机弄两个坐标 分别是深圳世界之窗附近
                .include(new LatLng(22.5441560000, 113.9828800000)) // 世界之窗右上角的美加广场
                .include(new LatLng(22.5413850000, 113.9777770000)) // 世界之窗左下角的一个不知道叫啥的街道
                .build();
        boundOption.bound(latLngBounds); // 设置搜索的范围
        boundOption.keyword(keyWord); // 搜索的关键字
        newInstance.searchInBound(boundOption);
    }

    /**
     * 周边搜索
     *
     * @param keyWord
     */
    private void aroundSearch(String keyWord) {
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(new MyAroudSearchListener());

        PoiNearbySearchOption nearbyOption = new PoiNearbySearchOption();
        LatLng hmPos = new LatLng(22.5441560000, 113.9828800000);//指定中心点
        nearbyOption.location(hmPos);// 设置中心点
        nearbyOption.radius(1000);// 设置半径 单位是米
        nearbyOption.keyword(keyWord);// 关键字
        poiSearch.searchNearby(nearbyOption);
    }

    /**
     * 范围搜索监听
     */
    class MyRangeSearchListener implements OnGetPoiSearchResultListener {

        @Override
        public void onGetPoiDetailResult(PoiDetailResult result) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }

        @Override
        public void onGetPoiResult(PoiResult result) {
            // 收到发送过来的搜索请求之后我们进行处理
            if (result == null || SearchResult.ERRORNO.RESULT_NOT_FOUND == result.error) {
                Toast.makeText(getApplicationContext(), "未搜索到结果", Toast.LENGTH_LONG).show();
                return;
            }
            //搜索类型的类
            PoiOverlay overlay = new PoiOverlay(mBaiduMap);  //处理搜索Poi的覆盖物
            mBaiduMap.setOnMarkerClickListener(overlay);// 把事件分发给overlay，overlay才能处理点击事件
            overlay.setData(result);  //设置结果
            overlay.addToMap();//把搜索的结果添加到地图中去
            overlay.zoomToSpan(); //自动缩放到所以的mark覆盖物都能看到
        }
    }

    /**
     * 周边搜索(圆形)
     */
    class MyAroudSearchListener implements OnGetPoiSearchResultListener {

        @Override
        public void onGetPoiDetailResult(PoiDetailResult result) {
            if (result == null || SearchResult.ERRORNO.RESULT_NOT_FOUND == result.error) {
                Toast.makeText(getApplicationContext(), "未搜索到结果", Toast.LENGTH_LONG).show();
                return;
            }

            String text = result.getAddress() + "::" + result.getCommentNum() + result.getEnvironmentRating();
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }

        @Override
        public void onGetPoiResult(PoiResult result) {
            if (result == null || SearchResult.ERRORNO.RESULT_NOT_FOUND == result.error) {
                Toast.makeText(getApplicationContext(), "未搜索到结果", Toast.LENGTH_LONG).show();
                return;
            }
            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);// 搜索poi的覆盖物
            mBaiduMap.setOnMarkerClickListener(overlay);// 把事件分发给overlay，overlay才能处理点击事件
            overlay.setData(result);// 设置结果
            overlay.addToMap();// 把搜索的结果添加到地图中
            overlay.zoomToSpan();// 缩放地图，使所有Overlay都在合适的视野内 注： 该方法只对Marker类型的overlay有效
        }

    }

    //自己实现点击事件
    class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap arg0) {
            super(arg0);
        }

        @Override
        public boolean onPoiClick(int index) {
            PoiResult poiResult = getPoiResult();
            PoiInfo poiInfo = poiResult.getAllPoi().get(index);// 得到点击的那个poi信息
            String text = poiInfo.name + "," + poiInfo.address;
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

            PoiDetailSearchOption detailOption = new PoiDetailSearchOption();
            detailOption.poiUid(poiInfo.uid);// 设置poi的uid
            poiSearch.searchPoiDetail(detailOption);

            return super.onPoiClick(index);
        }
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
