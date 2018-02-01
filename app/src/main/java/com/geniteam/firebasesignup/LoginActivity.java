package com.geniteam.firebasesignup;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v4.database.DatabaseUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.geniteam.firebasesignup.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
ActivityLoginBinding binding;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth=FirebaseAuth.getInstance();
        binding= DataBindingUtil.setContentView(this,R.layout.activity_login);
        binding.login.setOnClickListener(this);
        binding.createAccount.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view==binding.login){
binding.progressBarlogin.setVisibility(View.VISIBLE);
            String email=binding.editText.getText().toString();
            String pass=binding.password.getText().toString();
logUserIn(email,pass);
        }else if(view==binding.createAccount){
startActivity(new Intent(getApplicationContext(),MainActivity.class));

        }

    }


    public void logUserIn(String email,String pass){
if(firebaseAuth!=null){
    firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()){
            String fireBaseUserID=getFireBaseUserId();
                binding.progressBarlogin.setVisibility(View.GONE);
                if(fireBaseUserID!=null){
              Intent intent=      new Intent(getApplicationContext(),UserProfile.class);
                    intent.putExtra(FireBaseConstants.FIREBASE_USER_ID_Key,fireBaseUserID);
                  startActivity(intent);


                }


            }else if (null!=task.getException().getMessage()){
                binding.progressBarlogin.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"Fail to login "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    });
}
    }


    public String getFireBaseUserId(){
        FirebaseUser firebaseUser=null;
        String uid=null;
try{
     firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

  uid=  firebaseUser.getUid();

}catch (Exception e){
    e.printStackTrace();
}

        return uid;

    }


    public void fetchUserAllDataFromDb(){

    }
}
