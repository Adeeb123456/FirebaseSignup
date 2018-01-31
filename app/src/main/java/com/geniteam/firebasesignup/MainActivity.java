package com.geniteam.firebasesignup;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.geniteam.firebasesignup.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

ActivityMainBinding mainBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainBinding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("debug", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("debug", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        mainBinding.login.setOnClickListener(this);
        mainBinding.signUp.setOnClickListener(this);
        mainBinding.update.setOnClickListener(this);
        mainBinding.cretedb.setOnClickListener(this);
        mainBinding.readDb.setOnClickListener(this);
        mainBinding.getuser.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }






    public void createAccount(String email,String password){

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("debug", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "fail to create",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this, "user created",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }


    public void signin(String email,String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("debug", "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("debug", "signInWithEmail:failed", task.getException());
                            Toast.makeText(MainActivity.this, "authentication fail",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            Log.w("debug", "signIn success", task.getException());
                            Toast.makeText(MainActivity.this, "authentication success",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }


    public FirebaseUser getCurrentUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();
           Log.d("debug","name "+user.getDisplayName());
            Log.d("debug","email"+user.getEmail());
            Log.d("debug","photoUrl "+photoUrl);


            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();
            Toast.makeText(MainActivity.this,
                    "user name "+name+" email "+email+"photoUrl "+photoUrl
                    , Toast.LENGTH_SHORT).show();
        }
      return   user;
    }


    public void updateUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName("Jane Q. User")
                .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("debug", "User profile updated.");
                            Toast.makeText(MainActivity.this, " updated ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if(view==mainBinding.login){
            signin(mainBinding.emailTv.getText().toString(),mainBinding.passwordTv.getText().toString());
        }else if(view==mainBinding.signUp){

            createAccount(mainBinding.emailTv.getText().toString(),mainBinding.passwordTv.getText().toString());
        }else if(view==mainBinding.update){
            updateUser();
        }else if(view==mainBinding.cretedb){
            initFireBaseDBAndAddValue();
        }else if(view==mainBinding.readDb){
            readFromDd();
        }else if(view==mainBinding.getuser){
        getCurrentUser();
    }
    }


    DatabaseReference myRef;
    FirebaseDatabase database;
    public void initFireBaseDBAndAddValue(){
        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("db");

        myRef.setValue("Hello, w2122!");

    }


    private void readFromDd(){
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("debug", "Value is: " + value);
                Toast.makeText(MainActivity.this, "fireb db "+value,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("debug", "Failed to read value.", error.toException());
                Toast.makeText(MainActivity.this, "Failed to read value."+error.toException(),
                        Toast.LENGTH_SHORT).show();

            }
        });
    }
}
