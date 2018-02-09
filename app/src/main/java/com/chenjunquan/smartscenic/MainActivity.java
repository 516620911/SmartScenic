package com.chenjunquan.smartscenic;

import android.Manifest;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.chenjunquan.smartscenic.activity.Camera2Activity;
import com.chenjunquan.smartscenic.present.MainPresent;

import butterknife.BindView;
import butterknife.OnClick;
import cn.droidlover.xdroidmvp.log.XLog;
import cn.droidlover.xdroidmvp.mvp.XActivity;
import cn.droidlover.xdroidmvp.router.Router;
import io.reactivex.functions.Consumer;

public class MainActivity extends XActivity<MainPresent>
        implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.bmapView)
    MapView mMapView;
    @BindView(R.id.ib_take_photo)
    ImageButton mIbTakePhoto;
    @BindView(R.id.ib_location)
    ImageButton mIbLocation;
    @BindView(R.id.ib_refresh)
    ImageButton mIbRefresh;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private BaiduMap mBaiduMap;
    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();
    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口
    private Overlay overlay;
    private boolean isFristLoc = true;
    private BDLocation bdLocation;

    @Override
    public void initData(Bundle savedInstanceState) {
        initView();
        initBaiduMap();
        initLocation();
    }

    private void initLocation() {
        //第一次定位到景区中心
        forceLocation(113.957166, 22.594236);
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        //配置定位SDK参数
        initLocationClientOption();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //调用LocationClient的start()方法，便可发起定位请求
        getRxPermissions().request(Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        mLocationClient.start();
                    }
                });
    }

    //实现定位监听
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null) {
                XLog.d("location == null", location.getCity());
                return;
            }
            bdLocation=location;
            //XLog.d(location.getLongitude()+"");
            //XLog.d(location.getLatitude()+"");
        }

    }

    private void forceLocation(double longitude, double latitude) {
        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                .direction(100)
                .latitude(latitude)
                .longitude(longitude).build();
        // 设置定位数据
        mBaiduMap.setMyLocationData(locData);
        LatLng latLng = new LatLng(latitude, longitude);
        //定位标注
        //BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.marker_location_icon);
        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）L
        // MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS, true, bitmapDescriptor);
        //mBaiduMap.setMyLocationConfiguration(config);
        //构建MarkerOption，用于在地图上添加Marker
            /*OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmapDescriptor);
            //在地图上添加Marker，并显示
            overlay = mBaiduMap.addOverlay(option);*/
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(latLng);
        //刷新地图状态
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.zoom(18).build()));
    }

    //配置定位SDK参数
    private void initLocationClientOption() {
        LocationClientOption option = new LocationClientOption();
        //可选，设置定位模式，默认高精度
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置返回经纬度坐标类型,如果想直接在百度地图上标注，请选择坐标类型bd09ll。
        option.setCoorType("bd09ll");
        //可选，设置发起定位请求的间隔ms,0仅定位一次，默认为0
        option.setScanSpan(1000);
        //可选，设置是否使用gps，使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.setOpenGps(true);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        option.setLocationNotify(true);
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
        option.setIgnoreKillProcess(false);
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        mLocationClient.setLocOption(option);
    }

    private void initView() {
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initBaiduMap() {
        //百度地址设置
        mBaiduMap = mMapView.getMap();
        UiSettings bMapSetting = mBaiduMap.getUiSettings();
        mMapView.showScaleControl(false);
        mMapView.showZoomControls(false);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public MainPresent newP() {
        return new MainPresent();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @OnClick({R.id.ib_take_photo, R.id.ib_location, R.id.ib_refresh})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ib_take_photo:
                getRxPermissions()
                        .request(Manifest.permission.CAMERA)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean) {
                                    Router.newIntent(MainActivity.this).to(Camera2Activity.class).launch();

                                } else {

                                }
                            }

                        });
                break;
            case R.id.ib_location:
                forceLocation(bdLocation.getLongitude(),bdLocation.getLatitude());
                break;
            case R.id.ib_refresh:
                break;
        }
    }


}
