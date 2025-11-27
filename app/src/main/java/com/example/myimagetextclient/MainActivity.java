package com.example.myimagetextclient;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // 禁用不可点击的三个按钮
        bottomNav.getMenu().findItem(R.id.navigation_friends).setEnabled(false);
        bottomNav.getMenu().findItem(R.id.navigation_camera).setEnabled(false);
        bottomNav.getMenu().findItem(R.id.navigation_messages).setEnabled(false);

        // 绑定导航栏点击事件
        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.navigation_home) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                    return true;

                } else if (id == R.id.navigation_profile) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .commit();
                    return true;
                }

                return false;
            }
        });

        // 默认选中首页并加载
        bottomNav.setSelectedItemId(R.id.navigation_home);
    }
}
