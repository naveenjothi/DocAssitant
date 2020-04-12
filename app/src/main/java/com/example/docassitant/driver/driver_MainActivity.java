package com.example.docassitant.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.docassitant.MainActivity;
import com.example.docassitant.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skyfishjy.library.RippleBackground;

import java.util.HashMap;

public class driver_MainActivity extends AppCompatActivity {
    private static final String TAG = "driver_MainActivity";
    private Switch mSwitch;
    private boolean isOnline;
    private DatabaseReference reference,dref,driver_online_ref;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private TextView online_txt,Pname_text,Pgender_text,Palter_no_text,Pbg_text;
    private Button logout_btn;
    private ImageButton accept_btn,decline_btn;
    private String user_email,user_id,driver_id;
    private RelativeLayout driver_request_layout,driver_main_layout;
    private Toolbar mTopToolbar;
    private Location mlocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationManager locationManager;
    private static final int REQUEST_CHECK_SETTINGS =11 ;
    private static final int REQUEST_CODE=101;
    private RecyclerView recyclerView;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver__main);
        mSwitch=(Switch)findViewById(R.id.toggleswitch);
        online_txt=(TextView)findViewById(R.id.online_txt);
        Pbg_text=(TextView)findViewById(R.id.Pbg_txt);
        Pgender_text=(TextView)findViewById(R.id.Pgender_txt);
        Palter_no_text=(TextView)findViewById(R.id.Palter_no_txt);
        Pname_text=(TextView)findViewById(R.id.Pname_txt);
        logout_btn=(Button)findViewById(R.id.btn_logout);
        accept_btn=(ImageButton)findViewById(R.id.accept_btn);
        decline_btn=(ImageButton) findViewById(R.id.decline_btn);
        driver_request_layout=(RelativeLayout) findViewById(R.id.driver_requestview);
        driver_main_layout=(RelativeLayout) findViewById(R.id.driver_mainview);

        auth=FirebaseAuth.getInstance();
        firebaseUser=auth.getCurrentUser();
        user_email=firebaseUser.getEmail();
        final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.ripple_content);
        mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTopToolbar.setTitle("");
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        TextView mTitle = (TextView) mTopToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("Request");
        mTopToolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        mTopToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driver_main_layout.setVisibility(View.VISIBLE);
                driver_request_layout.setVisibility(View.GONE);
            }
        });
        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //using fusedlocation provider to get the current location
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            displayLocationSettingsRequest(getApplicationContext());
        }
        //listening firebase continuously for emergency status
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                        if(Integer.parseInt(snap.child("emergency_status").getValue().toString())==1){
                            user_id=snap.getKey();
                            final String Pname=snap.child("name").getValue().toString();
                            final String Palter_no=snap.child("alternate_mobile_no").getValue().toString();
                            final String Psex=snap.child("gender").getValue().toString();
                            final String Pblood_grp=snap.child("bg").getValue().toString();
                            //used to identify the drivers online
                            driver_online_ref = FirebaseDatabase.getInstance().getReference().child("Drivers");
                            driver_online_ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot Snapshots) {
                                    if(Snapshots.exists()) {
                                        if (Integer.parseInt(Snapshots.child(driver_id).child("status").getValue().toString()) == 1) {
                                            if (Integer.parseInt(Snapshots.child(driver_id).child("Accept_status").getValue().toString()) == 1){
                                                driver_main_layout.setVisibility(View.VISIBLE);
                                                driver_request_layout.setVisibility(View.GONE);
                                            }else{
                                                if(Integer.parseInt(Snapshots.child(driver_id).child("Decline_status").getValue().toString())==0){
                                                    driver_main_layout.setVisibility(View.GONE);
                                                    driver_request_layout.setVisibility(View.VISIBLE);
                                                    Pname_text.setText(Pname);
                                                    Pgender_text.setText(Psex);
                                                    Palter_no_text.setText(Palter_no);
                                                    Pbg_text.setText(Pblood_grp+"+ive");
                                                    rippleBackground.startRippleAnimation();
                                                } else if(Integer.parseInt(Snapshots.child(driver_id).child("Decline_status").getValue().toString()) == 1){
                                                    if(Integer.parseInt(Snapshots.child(driver_id).child("Patient_id").getValue().toString()) != Integer.parseInt(user_id)){
                                                        driver_online_ref.child(driver_id).child("Decline_status").setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                driver_main_layout.setVisibility(View.GONE);
                                                                driver_request_layout.setVisibility(View.VISIBLE);
                                                                rippleBackground.startRippleAnimation();
                                                            }
                                                        });
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isOnline=b;
                changefirebasedata();
            }
        });
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            displayLocationSettingsRequest(getApplicationContext());
        }
        accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference = FirebaseDatabase.getInstance().getReference().child("Users");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for(final DataSnapshot snap:dataSnapshot.getChildren()){
                                reference.child(user_id).child("emergency_status").setValue(2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        dref = FirebaseDatabase.getInstance().getReference().child("Drivers");
                                        dref.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                dref.child(driver_id).child("Accept_status").setValue(1);
                                                dref.child(driver_id).child("Decline_status").setValue(0);
                                                dref.child(driver_id).child("Patient_id").setValue(Integer.parseInt(user_id));
                                                driver_main_layout.setVisibility(View.VISIBLE);
                                                driver_request_layout.setVisibility(View.GONE);
                                                Intent i=new Intent(driver_MainActivity.this, MapsActivity.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                i.putExtra("user_id",user_id);
                                                startActivity(i);
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            });


                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        decline_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dref=FirebaseDatabase.getInstance().getReference().child("Drivers");
                dref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            dref.child(driver_id).child("Decline_status").setValue(1);
                            dref.child(driver_id).child("Patient_id").setValue(user_id).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    driver_online_ref=FirebaseDatabase.getInstance().getReference().child("Drivers_online");
                                    /*driver_online_ref.child(driver_id).child("Decline_status").setValue(1);
                                    driver_online_ref.child(driver_id).child("Patient_id").setValue(user_id);*/
                                    driver_main_layout.setVisibility(View.VISIBLE);
                                    driver_request_layout.setVisibility(View.GONE);
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
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent i=new Intent(driver_MainActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.putExtra("from",TAG);
                startActivity(i);
                finish();
            }
        });
    }

    private void changefirebasedata() {
        dref = FirebaseDatabase.getInstance().getReference().child("Drivers");
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                        String email=snap.child("email_id").getValue().toString();
                        if (user_email.equals(email)){
                            driver_id=snap.getKey();
                            if(isOnline){
                                dref.child(driver_id).child("status").setValue(1);
                                online_txt.setText("Online");
                            }else {
                                online_txt.setText("Offline");
                                dref.child(driver_id).child("status").setValue(0);
                            }
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        driver_online_ref=FirebaseDatabase.getInstance().getReference().child("Drivers");
        driver_online_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot snap:dataSnapshot.getChildren()){
                        String email=snap.child("email_id").getValue().toString();
                        if (user_email.equals(email)) {
                            driver_id=snap.getKey();
                            if (Integer.parseInt(snap.child("status").getValue().toString()) == 1) {
                                mSwitch.setChecked(true);
                                online_txt.setText("Online");
                            } else {
                                online_txt.setText("Offline");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                            status.startResolutionForResult(driver_MainActivity.this, REQUEST_CHECK_SETTINGS);
                           // getLocation(driver_id);

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
    private void getLocation(final String id) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ||ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},REQUEST_CODE);
           // return;
        }
        Task <Location> task1=fusedLocationProviderClient.getLastLocation();

        task1.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mlocation=location;
                System.out.println(mlocation);

                //adding my cuurent location into the maps fragment
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    //displaying location settings after the user permission
                    displayLocationSettingsRequest(getApplicationContext());
                   // getLocation(driver_id);
                }else{
                   // getLocation(driver_id);
                }
                break;
        }

    }
}
