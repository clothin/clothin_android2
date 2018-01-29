package si.cit.clothingorigin.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import si.cit.clothingorigin.CitApp;
import si.cit.clothingorigin.Interfaces.RecyclerAdapterItemClickListener;
import si.cit.clothingorigin.Objects.Product;
import si.cit.clothingorigin.ProductionChainAdapter;
import si.cit.clothingorigin.R;
import si.cit.clothingorigin.RecentScansAdapter;
import si.cit.clothingorigin.views.FontView;
import timber.log.Timber;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, RecyclerAdapterItemClickListener {

    @BindView(R.id.recent_scans_list)
    RecyclerView recentScansRecyclerView;

    @BindView(R.id.noProductsText)
    FontView noProductsText;

    private RecentScansAdapter recentScansAdapter = null;
    private List<Product> recentScans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        recentScans = getRecentScanList();
        if(recentScans.size()>0) {
            noProductsText.setVisibility(View.INVISIBLE);
            if (recentScansAdapter == null) {
                recentScansAdapter = new RecentScansAdapter(this, recentScans, "scanHistoryAdapter");
                recentScansRecyclerView.setAdapter(recentScansAdapter);
                recentScansAdapter.setOnItemClickListener(this);
            } else {
                recentScansAdapter.notifyDataSetChanged();
            }
        }else{
            noProductsText.setVisibility(View.VISIBLE);
        }
    }

    private void init(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab_scan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCodeScanner();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Timber.i("Initializing recent Product scan list");
        recentScansRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,
                true));
    }

    private void openCodeScanner(){
        if(CitApp.getInstance().hasCameraPermissions()) {
            Intent scanIntent = new Intent(getApplicationContext(), CodeScannerActivity.class);
            startActivityForResult(scanIntent, CodeScannerActivity.ACTIVITY_REQUEST_CODE_SCAN);
        }else{
            Timber.i("Missing camera permissions! Requesting...");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CodeScannerActivity.APP_CAMERA_PERMISSION_REQUEST_CODE
            );
        }
    }

    private List<Product> getRecentScanList(){
        SharedPreferences sp = getSharedPreferences(getString(R.string.preferences_value_store), MODE_PRIVATE);
        String historyJson = sp.getString(getString(R.string.value_scan_history), "");
        List<Product> history = new ArrayList<>();
        Gson gson = new Gson();
        if (historyJson.length() > 0){
            history = gson.fromJson(historyJson, new TypeToken<List<Product>>(){}.getType());
            if (history != null) {
                Timber.i("Scan history length: " + history.size());
            } else {
                Timber.e("Failed to retrieve scan history!");
                history = new ArrayList<>();
            }
        }
        return history;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
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

        if (id == R.id.nav_scan) {
            // Open scan activity
            openCodeScanner();
        } else if (id == R.id.nav_items) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for(int i = 0;i<permissions.length;i++){
            Timber.i("Permission: "+permissions[i]+" granted: "+grantResults[i]);
        }

        switch (requestCode) {
            case CodeScannerActivity.APP_CAMERA_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.i("Permissions granted!");
                    openCodeScanner();
                } else {
                    Timber.i("Permissions denied!");
                    //Permissions denied, alert user that storage permissions are required to use the scanner.
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CodeScannerActivity.ACTIVITY_REQUEST_CODE_SCAN:
                if(resultCode==RESULT_OK){
                    String scanData = data.getStringExtra("scan_data");
                    showAlertDialog("Scan result: "+scanData);
                    if(scanData.startsWith("CLO_")) {
                        //check for scanned data on the BC. If product id schema is correct, display product details page
                        Intent productIntent = new Intent(getApplicationContext(),ItemDetailsActivity.class);
                        productIntent.putExtra("product_id",Long.valueOf(scanData.substring(4)));
                        startActivityForResult(productIntent,ItemDetailsActivity.ACTIVITY_REQUEST_CODE_PRODUCT);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onListItemClick(String adapterTag, int position) {
        Intent productIntent = new Intent(getApplicationContext(),ItemDetailsActivity.class);
        productIntent.putExtra("product_id",Long.valueOf(recentScans.get(position).id));
        productIntent.putExtra("fromScan",false);
        startActivityForResult(productIntent,ItemDetailsActivity.ACTIVITY_REQUEST_CODE_PRODUCT);
    }
}
