package com.example.docassitant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private Toolbar mTopToolbar;
    private EditText name_txt,bg_txt,gender_txt,mob_no_txt,amob_no_txt;
    private Button submit_btn;
    private String name,bg,gender,mobile_no,alternate_mobile_no;
    private DatabaseReference reference;
    private long user_count=0;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //Toolbar content
        mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTopToolbar.setTitle("");
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        TextView mTitle = (TextView) mTopToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("Step 2/2");
        mTopToolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        mTopToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(ProfileActivity.this,Register.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });

        name_txt=(EditText)findViewById(R.id.register_name);
        bg_txt=(EditText)findViewById(R.id.register_blood_group);
        gender_txt=(EditText)findViewById(R.id.register_gender);
        mob_no_txt=(EditText)findViewById(R.id.register_phone_no);
        amob_no_txt=(EditText)findViewById(R.id.register_alternate_phone_no);

        submit_btn=(Button)findViewById(R.id.register_submit_btn);

        //get values from register activity
        getincommingintent();


        reference= FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                        if((snap.child("mobile_no").getValue().toString().equals("+91"+mobile_no))){
                            user_id=snap.getKey();
                        }
                    }
                    if(user_id!=null){
                        if(user_id!=""){
                            name=dataSnapshot.child(user_id).child("name").getValue().toString();
                            bg=dataSnapshot.child(user_id).child("bg").getValue().toString();
                            gender=dataSnapshot.child(user_id).child("gender").getValue().toString();
                            alternate_mobile_no=dataSnapshot.child(user_id).child("alternate_mobile_no").getValue().toString();
                            setvalues();
                        }else {
                            System.out.println("new user1");
                            user_count=(dataSnapshot.getChildrenCount());
                        }
                    }else {
                        System.out.println("new user");
                        user_count=(dataSnapshot.getChildrenCount());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getValues();
                if(name != null || bg != null  || gender != null  || mobile_no != null || alternate_mobile_no != null){
                    Toast.makeText(ProfileActivity.this,"All fields are present",Toast.LENGTH_LONG).show();
                    if(user_id!=null){
                        System.out.println("Comming");
                        if(user_id!=""){
                            System.out.println("HI");
                            updatevalues();
                        }
                        else {
                            System.out.println("hello1");
                            addvalues();
                        }
                    }else {
                        System.out.println("hello");
                        addvalues();
                    }
                }else {
                    Toast.makeText(ProfileActivity.this,"Please enter all the fields",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void setvalues() {
        name_txt.setText(name);
        bg_txt.setText(bg);
        gender_txt.setText(gender);
        mob_no_txt.setText(mobile_no);
        amob_no_txt.setText(alternate_mobile_no);
    }

    private void updatevalues() {
        reference=FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    reference.child(user_id).child("name").setValue(name);
                    reference.child(user_id).child("bg").setValue(bg);
                    reference.child(user_id).child("gender").setValue(gender);
                    reference.child(user_id).child("mobile_no").setValue(mobile_no);
                    reference.child(user_id).child("alternate_mobile_no").setValue(alternate_mobile_no);
                    Intent i=new Intent(ProfileActivity.this,Category.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Toast.makeText(ProfileActivity.this,"Data Updated",Toast.LENGTH_LONG).show();
                    startActivity(i);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getincommingintent() {
        Intent i=getIntent();
        mobile_no=i.getStringExtra("mobile");
        mob_no_txt.setText(mobile_no);
    }

    private void addvalues() {
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("name",name);
        hashMap.put("bg",bg);
        hashMap.put("gender",gender);
        hashMap.put("mobile_no",mobile_no);
        hashMap.put("alternate_mobile_no",alternate_mobile_no);
        hashMap.put("emergency_status",0);
        hashMap.put("Pat_Lat",0);
        hashMap.put("Pat_Lng",0);
        reference.child(String.valueOf(user_count+1)).setValue(hashMap);
        Intent i=new Intent(ProfileActivity.this,Category.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(this,"Data Inserted",Toast.LENGTH_LONG).show();
        startActivity(i);
        finish();
    }

    private void getValues() {
        name=name_txt.getText().toString();
        bg=bg_txt.getText().toString();
        gender=gender_txt.getText().toString();
        mobile_no="+91"+mob_no_txt.getText().toString().trim();
        alternate_mobile_no="+91"+amob_no_txt.getText().toString().trim();
    }
}