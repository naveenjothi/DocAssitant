package com.example.docassitant.driver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.docassitant.LoginActivity;
import com.example.docassitant.MainActivity;
import com.example.docassitant.R;
import com.example.docassitant.SplashActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_CHECK_SETTINGS =11 ;
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    Location mlocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE=101;
    LocationManager locationManager;
    private int count=0;
    private Toolbar mTopToolbar;
    private GoogleMap mMap1;
    private String user_id;
    private double cus_lat,cus_lng;
    private Button arrived_btn,saved_btn;
    private DatabaseReference dref,driver_ref,cus_ref,patientreference;
    private String driver_id;
    private RelativeLayout modallayout,arrived_layout,saved_layout;
    private int SPLASH_TIME_OUT=2000;

    @RequiresApi(api = Build.VERSION_CODES.P)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTopToolbar.setTitle("");
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        TextView mTitle = (TextView) mTopToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("On Board");
        mTopToolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        mTopToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(MapsActivity.this, driver_MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });
        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        arrived_btn=(Button)findViewById(R.id.arrived_btn);
        saved_btn=(Button)findViewById(R.id.saved_btn);
        modallayout=(RelativeLayout)findViewById(R.id.modal_success);
        arrived_layout=(RelativeLayout)findViewById(R.id.arrived_btn_layout);
        saved_layout=(RelativeLayout)findViewById(R.id.saved_btn_layout);
        //using fusedlocation provider to get the current location
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            displayLocationSettingsRequest(getApplicationContext());
        }else{
           getLocation();
        }
        getvalues();
        arrived_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driver_ref=FirebaseDatabase.getInstance().getReference().child("Drivers");
                driver_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for(DataSnapshot snap:dataSnapshot.getChildren()){
                                if(snap.child("Patient_id").getValue().toString().equals(user_id)){
                                    driver_id=snap.getKey();
                                    System.out.println("driver_id"+driver_id);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                if(driver_id!=null){
                    if(driver_id!=""){
                        driver_ref.child(driver_id).child("Accept_status").setValue(2).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    arrived_layout.setVisibility(View.GONE);
                                    saved_layout.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }
            }
        });
        saved_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dref=FirebaseDatabase.getInstance().getReference().child("Drivers");
                dref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            dref.child(driver_id).child("Accept_status").setValue(0);
                            dref.child(driver_id).child("Patient_id").setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    cus_ref=FirebaseDatabase.getInstance().getReference().child("Users");
                                    cus_ref.child(user_id).child("emergency_status").setValue(0).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            modallayout.setVisibility(View.VISIBLE);
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    modallayout.setVisibility(View.GONE);
                                                    Intent i= new Intent(MapsActivity.this,driver_MainActivity.class);
                                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(i);
                                                    finish();
                                                }
                                            },SPLASH_TIME_OUT);
                                        }
                                    });
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void getvalues() {
        Intent i= getIntent();
        user_id=i.getStringExtra("user_id");
        if(user_id!=null){
            if(user_id!=""){
                getpatientlocation();
            }
        }
    }

    private void getpatientlocation() {
        patientreference= FirebaseDatabase.getInstance().getReference().child("Users");
        patientreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(Integer.parseInt(dataSnapshot.child(user_id).child("emergency_status").getValue().toString())==2){
                         cus_lat= Double.parseDouble(dataSnapshot.child(user_id).child("Pat_Lat").getValue().toString());
                         cus_lng= Double.parseDouble(dataSnapshot.child(user_id).child("Pat_Lng").getValue().toString());
                         System.out.println(cus_lat+"<<<<<<<<<<<>>>>>>>>>>>>"+cus_lng);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED ||ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},REQUEST_CODE);
            return;
        }
        Task <Location> task1=fusedLocationProviderClient.getLastLocation();

        task1.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mlocation=location;
                System.out.println("~~~~~~~~mlocation~~~~~~~~"+mlocation);
                //adding my cuurent location into the maps fragment
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
                mapFragment.getMapAsync(MapsActivity.this);
            }
        });
    }

    private void displayLocationSettingsRequest(Context context) {
        //trun on gps without navigation to the settings page
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        System.out.println("~~~~~~~~");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.e(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                            getLocation();

                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap1=googleMap;
        mMap.setMyLocationEnabled(true);
       if(mlocation!=null) {
           //to check the null value
           LatLng latLng = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
           final MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Iam here");
           mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
           mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
           mMap.addMarker(markerOptions);
           if(cus_lat !=0 && cus_lng!=0){
               Marker m2 = mMap.addMarker(new MarkerOptions()
                       .position(new LatLng(cus_lat,cus_lng))
                       .anchor(0.5f, 0.5f)
                       .title("Save me")
                       .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

//               final MarkerOptions options=new MarkerOptions().position(new LatLng(cus_lat,cus_lng)).title("Save me");
//               mMap1.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//               mMap1.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
//               mMap1.addMarker(options);
               LatLng origin=new LatLng(mlocation.getLatitude(),mlocation.getLongitude());
               LatLng dest=new LatLng(cus_lat,cus_lng);
               String url=getUrl(origin,dest);
               FetchUrl FetchUrl = new FetchUrl();
               FetchUrl.execute(url);
           }
           /*mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
               @Override
               public void onMapLongClick(LatLng latLng) {
                   count=count+1;
                   MarkerOptions markerOptions1=new MarkerOptions().position(latLng);
                    //using geocoder to take the place name
                   Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                   try {
                       List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude, 1);
                       Address obj = addresses.get(0);
                       String add = obj.getAddressLine(0);
                       add = add + "\n" + obj.getAdminArea();
                       add = add + "\n" + obj.getPostalCode();
                       add = add + "\n" + obj.getSubAdminArea();
                       add = add + "\n" + obj.getLocality();
                       add = add + "\n" + obj.getSubThoroughfare();

                       Log.v("IGA", "Address" + add);
                       markerOptions1.title(add);
                   } catch (IOException e) {
                       // TODO Auto-generated catch block
                       e.printStackTrace();
                       //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
                    //doing some functionality to remove and add markers when long pressing
                   if(count==1) {
                       mMap1.addMarker(markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                       LatLng origin=new LatLng(mlocation.getLatitude(),mlocation.getLongitude());
                       LatLng dest=latLng;
                       String url=getUrl(origin,dest);
                       FetchUrl FetchUrl = new FetchUrl();
                       FetchUrl.execute(url);
                   }else{
                       mMap1.clear();
                       count=0;
                       mMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                   }

               }
           });*/
       }else{
           //if null get the location to avoid crashing
           getLocation();
       }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    //displaying location settings after the user permission
                    displayLocationSettingsRequest(getApplicationContext());
                    getLocation();
                }else{
                   getLocation();
                }
                break;
        }

    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DirectionsJSONParser parser = new DirectionsJSONParser();
                Log.d("ParserTask",toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            System.out.println("~~~~~~~~~~"+routes);
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
}

