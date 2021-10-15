package com.example.mobileproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import com.example.mobileproject.ui.downloads.DownloadsFragment;
import com.example.mobileproject.ui.library.LibraryFragment;
import com.example.mobileproject.ui.navigate.NavigateFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        /*
        int[] lista = new int[]{R.layout.novel_grid_button,R.layout.novel_grid_button,R.layout.novel_grid_button,R.layout.novel_grid_button,R.layout.novel_grid_button};

        GridView gv = (GridView) findViewById(R.id.novelsGrid);
        gv.setAdapter(new Adaptador(this, lista));*/
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener(){

                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()){
                        case R.id.nav_library:
                            selectedFragment = new LibraryFragment();
                            break;
                        case R.id.nav_navigate:
                            selectedFragment = new NavigateFragment(ctx);
                            break;
                        case R.id.nav_download_list:
                            selectedFragment = new DownloadsFragment();
                            break;
                    }
                    if(selectedFragment == null){
                        return true;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,
                            selectedFragment).commit();

                    return true;
                }
            };


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}