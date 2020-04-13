package com.example.docassitant.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.Validator;
import android.service.autofill.Validators;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.example.docassitant.Category;
import com.example.docassitant.LoginActivity;
import com.example.docassitant.ProfileActivity;
import com.example.docassitant.R;
import com.example.docassitant.Register;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Range;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class driver_register extends AppCompatActivity {

    private Toolbar mTopToolbar;
    private EditText name_txt, mob_no_txt, pass_txt, confirm_pass_txt, email_txt;
    private Button submit_btn;
    private String name, mobile_no, password, confirm_password, email;
    private DatabaseReference reference;
    private long driver_count = 0;
    private AwesomeValidation details;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);
        //Toolbar content
        mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTopToolbar.setTitle("");
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        TextView mTitle = (TextView) mTopToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("SignUp");
        mTopToolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        mTopToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(driver_register.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });

        name_txt = (EditText) findViewById(R.id.driver_register_name);
        mob_no_txt = (EditText) findViewById(R.id.driver_register_phone_no);
        pass_txt = (EditText) findViewById(R.id.driver_register_password);
        confirm_pass_txt = (EditText) findViewById(R.id.driver_register_confirm_pass);
        email_txt = (EditText) findViewById(R.id.driver_register_email);
        submit_btn = (Button) findViewById(R.id.driver_register_submit_btn);

        //get values from register activity
        auth = FirebaseAuth.getInstance();
        //getincommingintent();


        reference = FirebaseDatabase.getInstance().getReference().child("Drivers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    driver_count = (dataSnapshot.getChildrenCount());
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
                if (name != null || mobile_no != null || password != null || confirm_password != null) {
                    Toast.makeText(driver_register.this, "All fields are present", Toast.LENGTH_LONG).show();
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser firebaseUser = auth.getCurrentUser();
                                        assert firebaseUser != null;
                                        addvalues();
                                    }
                                }
                            });


                } else {

                    Toast.makeText(driver_register.this, "Please enter all the fields", Toast.LENGTH_LONG).show();
                }
            }
        });

//      Validations
        details = new AwesomeValidation(ValidationStyle.BASIC);

//      Setting the Validators
        details.addValidation(this, R.id.driver_register_name, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.nameerror);
        details.addValidation(this, R.id.driver_register_phone_no, "^[2-9]{2}[0-9]{8}$", R.string.mobileerror);
        details.addValidation(this, R.id.driver_register_email, Patterns.EMAIL_ADDRESS, R.string.emailerror);

    }

    private void getincommingintent() {
        Intent i = getIntent();
        mobile_no = i.getStringExtra("mobile");
        mob_no_txt.setText(mobile_no);
    }

    private void addvalues() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", name);
        hashMap.put("mobile_no", mobile_no);
        hashMap.put("password", password);
        hashMap.put("confirm_password", confirm_password);
        hashMap.put("email_id", email);
        hashMap.put("status", 0);
        hashMap.put("Accept_status", 0);
        hashMap.put("Decline_status", 0);
        hashMap.put("Patient_id", 0);
        reference.child(String.valueOf(driver_count + 1)).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent i = new Intent(driver_register.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Toast.makeText(driver_register.this, "Data Inserted", Toast.LENGTH_LONG).show();
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    private void getValues() {

        if (details.validate()) {


            if (pass_txt.getText().toString().equals(confirm_pass_txt.getText().toString())) {

                name = name_txt.getText().toString().trim();
                mobile_no = mob_no_txt.getText().toString().trim();
                password = pass_txt.getText().toString().trim();
                confirm_password = confirm_pass_txt.getText().toString().trim();
                email = email_txt.getText().toString().trim();

            } else {

                confirm_pass_txt.setError("Password and Confirm password should be same");
            }
        }

    }

}

