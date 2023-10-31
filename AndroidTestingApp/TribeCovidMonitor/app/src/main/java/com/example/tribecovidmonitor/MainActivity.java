package com.example.tribecovidmonitor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.tribecovidmonitor.databinding.ActivityMainBinding;
import com.example.tribecovidmonitor.ui.CollectData.CollectDataFragment;
import com.example.tribecovidmonitor.ui.dashboard.Dashboard;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {
    final String fnlStr_debug_tag = "Activity Main Debug";
    final int fnlInt_request_audio_permissions = 1013;
    LinearLayout ll;
    final Button_Model arr_btnModels[] = {
            new Button_Model(Button_Model.fnlStr_code_notification),
    };

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout mDrawer;
    NavigationView nvDrawer;
    TextView results1, results2, results3, results4;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setContentView(R.layout.activity_main);

        // override the onOptionsItemSelected()
        // function to implement
        // the item click listener callback
        // to open and close the navigation
        // drawer when the icon is clicked

       toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // This will display an Up icon (<-), we will replace it with hamburger later
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);
        ll = (LinearLayout) findViewById(R.id.ll);

  results1=(TextView)findViewById(R.id.healthy);
        results2=(TextView)findViewById(R.id.mild);
        results3=(TextView)findViewById(R.id.moderate);
        results4=(TextView)findViewById(R.id.recover);
        create_Interface();
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

//        register_Broadcast_Receiver ( );
//        NavController navController = Navigation.findNavController(this, R.id.content_main);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    boolean handlerStop = false;

    void handleHandler(){
        Handler handler =new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                handler.postDelayed(this, 300);
                if(!handlerStop) {
                    updateTextView(); //update your text with other thread like asyncronous thread
                }
            }
        };
        handler.postDelayed(r, 0000);
    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
//                || super.onSupportNavigateUp();
//    }


    @SuppressLint("ResourceType")
    public void
    create_Interface() {
//        LinearLayout ll = new LinearLayout (this );
//        ll .setOrientation (LinearLayout .VERTICAL);
        Click_Listener_Buttons click_listener_buttons = new Click_Listener_Buttons();

        Button btn;
        for (Button_Model btnModel : arr_btnModels) {
            btn = new Button(this);
            btn.setText(btnModel.btn_text);
            btn.setTag(btnModel.btn_text);
            btn.setOnClickListener(click_listener_buttons);


            ll.addView(btn,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }



    }

    public void
    register_Broadcast_Receiver() {
        CachePurged_Receiver cachePurged_receiver = new CachePurged_Receiver();
        IntentFilter intentFilter = new IntentFilter("Cache Purged");
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(cachePurged_receiver, intentFilter);
    }


    /*Cache Purged Receiver Class */
    class CachePurged_Receiver extends BroadcastReceiver {
        @Override
        public void
        onReceive(Context context, Intent intent) {
            Log.d(fnlStr_debug_tag, "Cache Purged Broadcast Received");
            Toast.makeText(context, intent.getStringExtra("msg"),
                    Toast.LENGTH_LONG).show();
        }
    }


    /* Buttons Click Listeners class */
    class Click_Listener_Buttons implements View.OnClickListener {
        Service_Demo.LocalBinder localBinder;
        Service_Connection service_connection;

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void
        onClick(View view) {
            Intent intent = new Intent(MainActivity.this, Service_Demo.class);
            ;
            switch ((String) view.getTag()) {

                case Button_Model.fnlStr_code_notification:
                    Log.d(fnlStr_debug_tag, "running some code showing a notification");
                    //check for permissions
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        // granted start audio recording service
                        intent.putExtra("do", "Record Sound");
                        startService(intent);
                    } else {
                        // not granted , request the permissions
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                                fnlInt_request_audio_permissions);
                    }
                    break;
            }
        }

        class Service_Connection implements ServiceConnection {

            @Override
            public void
            onServiceConnected(ComponentName name, IBinder service) {
                // Called when binding has been established .
                localBinder = (Service_Demo.LocalBinder) service;
                Log.d(fnlStr_debug_tag, "" + localBinder.add(1, 2, 3, 4));
                // call the add method in the bound service .

                /* Stop sound recording in the bound service */
            }

            @Override
            public void
            onServiceDisconnected(ComponentName name) {
                //Called when binding is lost .
                localBinder = null;
            }

            @Override
            public void
            onBindingDied(ComponentName name) {
                // Called when  binding is dead.
                localBinder = null;
            }

            @Override
            public void
            onNullBinding(ComponentName name) {
                // Called when onBind returns null
                localBinder = null;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.nav_collect:
//                fragmentClass = DataCollect.class;
                startActivity(new Intent(MainActivity.this, DataCollect.class));

                break;
            case R.id.nav_dashboard:
                fragmentClass = Dashboard.class;
                break;
            case R.id.nav_home:
                fragmentClass = MainActivity.class;
                startActivity(new Intent(MainActivity.this, MainActivity.class));

                break;
            default:
                fragmentClass = MainActivity.class;
        }

        try {
//            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }


    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case fnlInt_request_audio_permissions:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //audio record permission has been granted
                    Intent intent = new Intent(MainActivity.this, Service_Demo.class);
                    intent.putExtra("do", "Record Sound");
                    startService(intent);
                } else {
                    Log.d(fnlStr_debug_tag, "Permission to record sound not granted");
                }
        }
    }


    public void updateTextView() {

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        float latitude = intent.getFloatExtra(Service_Demo.healthy, 0);
                        results1.setText("Healthy " + latitude);
                    }
                }, new IntentFilter(Service_Demo.ACTION_LOCATION_BROADCAST1)
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        float latitude = intent.getFloatExtra(Service_Demo.mild, 0);
                        results2.setText("Mild " + String.valueOf(latitude));
                    }
                }, new IntentFilter(Service_Demo.ACTION_LOCATION_BROADCAST2)
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                       float latitude = intent.getFloatExtra(Service_Demo.moderate, 0);
                        Log.d("xyzabc", String.valueOf(latitude));
                        results3.setText("Moderate " + String.valueOf(latitude));
                    }
                }, new IntentFilter(Service_Demo.ACTION_LOCATION_BROADCAST3)
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        float latitude = intent.getFloatExtra(Service_Demo.recovered, 0);
                        results4.setText("Recovered " + latitude);
                    }
                }, new IntentFilter(Service_Demo.ACTION_LOCATION_BROADCAST4)
        );
    }


    @Override
    public void onResume() {
        super.onResume();
        handlerStop=false;
        handleHandler();
    }

    @Override
    public void onPause() {
        super.onPause();
        handlerStop=true;
        handleHandler();
    }

    @Override
    public void onStop() {
        super.onStop();
        handlerStop=true;
        handleHandler();
    }
}

class Button_Model{

    final static String fnlStr_code_notification = "Start Monitoring";

    String btn_text;

    public
    Button_Model (String btn_text ){
        this.btn_text = btn_text; } }






