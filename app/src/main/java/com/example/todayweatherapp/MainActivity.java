package com.example.todayweatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.todayweatherapp.Adapter.ViewPagerAdapter;
import com.example.todayweatherapp.Common.Common;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private CoordinatorLayout coordinatorLayout;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private SearchView searchView;
    private List<String> cityList;
    private ArrayAdapter<String> arrayAdapter;
    private ListView search_list;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayout = findViewById(R.id.root_view);
        toolbar = findViewById(R.id.toolbar);
        search_list = findViewById(R.id.search_list);
        cityList = new ArrayList<>();
        //Set the action bar to be the custom toolbar that we created
        setSupportActionBar(toolbar);
        // This removes the back arrow on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //An android library that manages runtime permissions
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            buildLocationRequest();
                            buildLocationCallback();
                            fusedLocationProviderClient = LocationServices.
                                    getFusedLocationProviderClient(MainActivity.this);
                            fusedLocationProviderClient.requestLocationUpdates(
                                    locationRequest,
                                    locationCallback, Looper.myLooper());
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                            alertDialog.setTitle("Need Permissions");
                            alertDialog.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
                                    .setCancelable(false)
                                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                                            intent.setData(uri);
                                            startActivityForResult(intent, 101);
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            final AlertDialog alert = alertDialog.create();
                            alert.show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        Snackbar.make(coordinatorLayout, "Permission Denied", Snackbar.LENGTH_LONG).show();
                    }
                }).check();
        //Check if GPS is enabled on the device
        checkForLocation();
        //Executes the asynctask of loading the list of cities in the background
        new SearchSuggestionsList().execute();
        //When the user clicks on a city in the list, the city name will be set on the searchview
        search_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchView.setQuery((CharSequence) parent.getItemAtPosition(position), false);
            }
        });

    }

    private void checkForLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Your GPS is disabled. Please enable it in the settings")
                    .setCancelable(false)
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = alertDialog.create();
            alert.show();
        }
    }

    //Used for receiving notifications from the FusedLocationProviderApi
    // when the device location has changed or can no longer be determined.
    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Common.CURRENT_LOCATION = locationResult.getLastLocation();
                viewPager = findViewById(R.id.view_pager);
                setupViewPager(viewPager);
                tabLayout = findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(viewPager);
            }
        };

    }

    //Request a quality of service for location updates from the FusedLocationProviderAPI
    // The app requires high accuracy location
    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10.0f);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(TodayWeatherFragment.getInstance(), "Today");
        adapter.addFragment(ForecastFragment.getInstance(), "5 Days");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.forecast, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_search);
        //This makes sure the list disappears when the searchview is not expanded
        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (search_list.getVisibility() == View.GONE) {
                    search_list.setVisibility(View.VISIBLE);
                }
                searchView = (SearchView) menuItem.getActionView();
                searchView.setIconifiedByDefault(true);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        TodayWeatherFragment todayWeatherFragment = (TodayWeatherFragment) getSupportFragmentManager().findFragmentByTag(
                                "android:switcher:" + R.id.view_pager + ":" + 0);
                        ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(
                                "android:switcher:" + R.id.view_pager + ":" + 1);
                        todayWeatherFragment.getWeatherInformationByCity(query);
                        forecastFragment.getForecastWeatherInformationByCityName(query);
                        searchView.clearFocus();
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        arrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.list_item, cityList);
                        search_list.setAdapter(arrayAdapter);
                        arrayAdapter.getFilter().filter(newText);
                        return false;
                    }
                });
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                search_list.setVisibility(View.GONE);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_location) {
            checkForLocation();
            TodayWeatherFragment todayWeatherFragment = (TodayWeatherFragment) getSupportFragmentManager().findFragmentByTag(
                    "android:switcher:" + R.id.view_pager + ":" + 0);
            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(
                    "android:switcher:" + R.id.view_pager + ":" + 1);
            todayWeatherFragment.getWeatherInformationByLatLong();
            forecastFragment.getForecastWeatherInformationByLatLong();
        }
        return super.onOptionsItemSelected(item);
    }

    private class SearchSuggestionsList extends AsyncTask<Void, Void, List<String>> {

        //Reads the cityList.gzip file in the raw resource files and loads it into cityList as a List<String>
        @Override
        protected List<String> doInBackground(Void... voids) {
            cityList = new ArrayList<>();
            try {
                StringBuilder builder = new StringBuilder();
                InputStream is = getResources().openRawResource(R.raw.city_list);
                GZIPInputStream gzipInputStream = new GZIPInputStream(is);
                InputStreamReader reader = new InputStreamReader(gzipInputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String lineRead = bufferedReader.readLine();
                while (lineRead != null) {
                    builder.append(lineRead);
                    cityList = new Gson().fromJson(builder.toString(), new TypeToken<List<String>>() {
                    }.getType());
                    lineRead = bufferedReader.readLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return cityList;
        }
    }
}
