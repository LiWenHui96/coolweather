package com.example.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AreaActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    private ListView areaList;
    private Button areaBackButton;
    private Button areaNavButton;

    // 已选城市
    private ArrayList<Map<String, Object>> areaLtv = new ArrayList<Map<String, Object>>();
    private String[] areaLtvKeys = new String[] { "city", "temperature" };
    private int[] areaLtvIds = new int[] { R.id.area_id, R.id.area_weather };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area);
        //初始化各控件
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        areaList = (ListView) findViewById(R.id.area_list);
        areaBackButton = (Button) findViewById(R.id.area_back_button);
        areaNavButton = (Button) findViewById(R.id.area_nav_button);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        String weatherId = getIntent().getStringExtra("weather_id");
        if (weatherId == null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showCityInfo(weather);
            initAddCity();
        } else {
            requestCity(weatherId);
            initAddCity();
        }

        initAddCity();

        areaNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        areaBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initAddCity() {
        SimpleAdapter sa = new SimpleAdapter(this, areaLtv, R.layout.list_area, areaLtvKeys, areaLtvIds);
        areaList.setAdapter(sa);
    }

    public void requestCity(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=52c07de7455c41d4a3968c8423003029";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AreaActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showCityInfo(weather);
                            Intent intent = new Intent(AreaActivity.this, WeatherActivity.class);
                            intent.putExtra("weather_id", weatherId);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AreaActivity.this, "获取城市信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AreaActivity.this, "获取城市信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showCityInfo(Weather weather) {
        Map<String, Object> line = new HashMap<String, Object>();
        String name = weather.basic.cityName;
        String temperature = weather.now.temperature;
        line.put("city", name);
        line.put("temperature", temperature);
        areaLtv.add(line);
        areaList.setVisibility(View.VISIBLE);
    }
}
