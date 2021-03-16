package com.d.commenplayer.activity;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.d.commenplayer.MainActivity;
import com.d.commenplayer.R;

import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity {

    private TextSwitcher tv_switcher;
    private TextSwitcher tv_switcher2;
    private TextSwitcher tv_switcher3;

    public LocationClient mLocationClient;

    private TextView positionText;

    private MapView mapView;

    private BaiduMap baiduMap;

    private boolean isFirstLocate = true;

    private Instrumentation getInst = new Instrumentation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);
        mapView = (MapView) findViewById(R.id.bmapView);
        initView_advertise();
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MapActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }

    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            Toast.makeText(this, "nav to " + location.getAddrStr(), Toast.LENGTH_SHORT).show();
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.
                Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    /**
     *模拟点击屏幕事件，实现地图的缩放功能
     */

    public  void tap(int x,int y){
        try {
            getInst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0));

            getInst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0));
        }catch (Exception e){
            Log.i("模拟按键", "tap: 点击出错");
        }
    }
    @Override

    public boolean dispatchKeyEvent(KeyEvent event){
        switch (event.getKeyCode()){
            case KeyEvent.KEYCODE_B:
                Toast.makeText(this,"按下按键M",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MapActivity.this,SimpleActivity.class);
                startActivityForResult(intent,1);
                break;
            case KeyEvent.KEYCODE_R:
                Toast.makeText(this,"按下按键R",Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(MapActivity.this,MainActivity.class);
                startActivityForResult(intent2,1);
                break;
            case KeyEvent.KEYCODE_Y :
                tap(1230,618);
                break;
            case KeyEvent.KEYCODE_F4:
                Toast.makeText(this,"按下按键F4",Toast.LENGTH_SHORT).show();
                tap(1230,690);
                break;
            default:break;
        }
        return super.dispatchKeyEvent(event);
    }




    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }



    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
//            StringBuilder currentPosition = new StringBuilder();
//            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
//            currentPosition.append("经线：").append(location.getLongitude()).append("\n");
//            currentPosition.append("国家：").append(location.getCountry()).append("\n");
//            currentPosition.append("省：").append(location.getProvince()).append("\n");
//            currentPosition.append("市：").append(location.getCity()).append("\n");
//            currentPosition.append("区：").append(location.getDistrict()).append("\n");
//            currentPosition.append("街道：").append(location.getStreet()).append("\n");
//            currentPosition.append("定位方式：");
//            if (location.getLocType() == BDLocation.TypeGpsLocation) {
//                currentPosition.append("GPS");
//            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
//                currentPosition.append("网络");
//            }
//            positionText.setText(currentPosition);
            if (location.getLocType() == BDLocation.TypeGpsLocation
                    || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
            }
        }

    }

    private void initView_advertise() {
        tv_switcher = findViewById(R.id.tv_switcher);
        tv_switcher2 = findViewById(R.id.tv_switcher2);
        tv_switcher3 = findViewById(R.id.tv_switcher3);
        tv_switcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return new TextView(MapActivity.this);
            }
        });
        tv_switcher2.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return new TextView(MapActivity.this);
            }
        });
        tv_switcher3.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return new TextView(MapActivity.this);
            }
        });

        ArrayList<String> alist = new ArrayList<>();
        alist.clear();
//        for (int i = 0; i < 10; i++) {
//            alist.add("我是"+i);
        alist.add("西安电子科技大学1");
        alist.add("西安电子科技大学2");
        alist.add("西安电子科技大学3");
        alist.add("西安电子科技大学4");



        ArrayList<String> alist2 = new ArrayList<>();
        alist2.clear();
        alist2.add("西安电子科技大学2");
        alist2.add("西安电子科技大学3");
        alist2.add("西安电子科技大学4");
        alist2.add("西安电子科技大学1");

        ArrayList<String> alist3 = new ArrayList<>();
        alist3.clear();
        alist3.add("西安电子科技大学3");
        alist3.add("西安电子科技大学4");
        alist3.add("西安电子科技大学1");
        alist3.add("西安电子科技大学2");

        ArrayList<String> alist4 = new ArrayList<>();
        alist4.clear();
        alist4.add("西安电子科技大学4");
        alist4.add("西安电子科技大学1");
        alist4.add("西安电子科技大学2");
        alist4.add("西安电子科技大学3");

        new TextSwitcherAnimation(tv_switcher,alist).create();
        new TextSwitcherAnimation(tv_switcher2,alist2).create();
        new TextSwitcherAnimation(tv_switcher3,alist3).create();

    }
}
