package com.example.docassitant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Emergency extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "Emergency";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mLocationDatabaseReference;
    String value_lat = null;
    String value_lng=null;
    private String Pat_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        getincommingintent();
        mLocationDatabaseReference= mFirebaseDatabase.getReference().child("Users").child(Pat_id);
        buildGoogleApiClient();
    }

    private void getincommingintent() {
        Intent i= getIntent();
        Pat_id=i.getStringExtra("id");
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            value_lat = String.valueOf(mLastLocation.getLatitude());
            value_lng = String.valueOf(mLastLocation.getLongitude());
            System.out.println(value_lat+"<><><>"+value_lng);
            mLocationDatabaseReference.child("Pat_Lat").setValue(value_lat).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        mLocationDatabaseReference.child("Pat_Lng").setValue(value_lng).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                System.out.println("Location Updated");
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
