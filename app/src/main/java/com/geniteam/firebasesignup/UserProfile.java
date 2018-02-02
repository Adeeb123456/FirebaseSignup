package com.geniteam.firebasesignup;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.geniteam.firebasesignup.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserProfile extends AppCompatActivity {


    DatabaseReference databaseReferenceUser;
     String userID;
    List<User> userList=new ArrayList<>();
    ActivityMainBinding  binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    binding= DataBindingUtil.setContentView(this,R.layout.activity_main);


        databaseReferenceUser= FirebaseDatabase.getInstance().getReference(FireBaseConstants.FIREBASE_DB_REFERENCE_USER);
        if(getIntent()!=null){
            if(getIntent().hasExtra(FireBaseConstants.FIREBASE_USER_ID_Key)){
                userID=getIntent().getStringExtra(FireBaseConstants.FIREBASE_USER_ID_Key);
            binding.progressBarsignup.setVisibility(View.VISIBLE);
             /**
                databaseReferenceUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try{
                       userList.clear();
                            for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                                User user = null;
                                Log.d("debug","snap key "+snapshot.getKey());
                                Log.d("debug","user key "+userID);
                                if (snapshot.getKey().equals(userID)) {
                                    user=snapshot.getValue(User.class);
                                    userList.add(user);
                                    Log.d("debug","name "+user.name);
                                    Log.d("debug","email "+user.email);
                                    Log.d("debug","password "+user.pass);
                                    Log.d("debug","url "+user.photoUrl);
                                          setUpUi(user);
                                    Toast.makeText(getApplicationContext(),user.name+" Login successfully ",Toast.LENGTH_LONG).show();

                                }else {
                                    binding.progressBarsignup.setVisibility(View.GONE);
                                }
                                


                            }
                       Log.d("debug","size users "+userList.size());

                        }catch (Exception e){
                       e.printStackTrace();
                            binding.progressBarsignup.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(),"fail to login ",Toast.LENGTH_LONG).show();
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
**/


                // more accurat
                databaseReferenceUser.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user=dataSnapshot.getValue(User.class);

                        Log.d("debug","name "+user.name);
                        Log.d("debug","email "+user.email);
                        Log.d("debug","password "+user.pass);
                        Log.d("debug","url "+user.photoUrl);
                        setUpUi(user);
                        Toast.makeText(getApplicationContext(),user.name+" Login successfully ",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }



    public void setUpUi(User user){
        try{

        }catch (Exception e){

        }
        binding.userName.setText(user.name);
        binding.emailTv.setText(user.email);
        binding.passwordTv.setText(user.pass);
        binding.address.setText(user.Address);
        binding.progressBarsignup.setVisibility(View.GONE);
        binding.progressBarImage.setVisibility(View.VISIBLE);
        Picasso.with(this).load(user.photoUrl).into(binding.imageViewProfile, new Callback() {
            @Override
            public void onSuccess() {
                binding.progressBarImage.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                binding.progressBarImage.setVisibility(View.GONE);

            }
        });

    }
}
