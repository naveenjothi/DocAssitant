package com.example.docassitant;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.docassitant.Shake.ShakeDetector;
import com.example.docassitant.Shake.ShakeService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.Color.rgb;

public class Category extends AppCompatActivity {
    private static final String TAG = "Category";
    private Toolbar mTopToolbar;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private ImageView btnclose;
    private CircleImageView btnsettings;
    private RelativeLayout settings_layout,category_main_layout;
    private Button btn_logout,btn_editprofile;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private String number;
    private String Pat_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        btnsettings=(CircleImageView) findViewById(R.id.settings_btn);
        btnclose=(ImageView)findViewById(R.id.close_btn);
        settings_layout=(RelativeLayout)findViewById(R.id.settings_layout);
        category_main_layout=(RelativeLayout)findViewById(R.id.catogory_main_layout);
        btn_logout=(Button)findViewById(R.id.buttonlogout);
        btn_editprofile=(Button)findViewById(R.id.buttoneditprofile);

        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        auth=FirebaseAuth.getInstance();
        firebaseUser=auth.getCurrentUser();
        number=firebaseUser.getPhoneNumber();
        //Calling shake service
        Intent intent = new Intent(this, ShakeService.class);
        //Start Service
        startService(intent);


        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                /*
                 * The following method, "handleShakeEvent(count):" is a stub //
                 * method you would use to setup whatever you want done once the
                 * device has been shook.
                 */
                if(count>=3){
                    Changefirebasestatus();
                }
                Toast.makeText(Category.this,""+count,Toast.LENGTH_LONG).show();
            }
        });
        //firebase Listening
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                        if(Integer.parseInt(snap.child("emergency_status").getValue().toString())==1){
                            Intent i=new Intent(Category.this,Emergency.class);
                            i.putExtra("id",Pat_id);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnsettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                category_main_layout.setAlpha((float) 0.4);
                //category_main_layout.setBackgroundColor(rgb(211,211,211));
                settings_layout.setVisibility(View.VISIBLE);
            }
        });
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                category_main_layout.setAlpha(1);
                settings_layout.setVisibility(View.GONE);
            }
        });
        btn_editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Category.this,ProfileActivity.class);
                startActivity(i);
            }
        });
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent i=new Intent(Category.this,MainActivity.class);
                i.putExtra("from", TAG);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });
    }

    private void Changefirebasestatus() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                        if(number.equals(snap.child("mobile_no").getValue().toString())){
                            reference.child(snap.getKey()).child("emergency_status").setValue(1);
                            Pat_id=snap.getKey();
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
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }
}
