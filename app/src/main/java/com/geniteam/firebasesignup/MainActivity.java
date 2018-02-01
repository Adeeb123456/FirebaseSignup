package com.geniteam.firebasesignup;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.geniteam.firebasesignup.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

   ActivityMainBinding mainBinding;

    DatabaseReference databaseReferenceUser;
     Uri selectedImageUri;
    StorageReference storageReferencePicture,imageRef;
    UploadTask uploadTask;

    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainBinding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        storageReferencePicture= FirebaseStorage.getInstance().getReference();

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
        mainBinding.imageViewProfile.setOnClickListener(this);

        initrDataBaseRerences();
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


    public void initrDataBaseRerences(){
        databaseReferenceUser=FirebaseDatabase.getInstance().getReference("Users");

    }

    public void addUserTOFireBaseDB(User user,String uuid){
try{
    databaseReferenceUser.child(uuid).setValue(user);
    mainBinding.progressBarsignup.setVisibility(View.GONE);

}catch (Exception e){

}


    }




    public void createAccount(final User user){
mainBinding.progressBarsignup.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(user.email,user.pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("debug", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {

                            if(task.getException()instanceof FirebaseAuthUserCollisionException){
                                mainBinding.progressBarsignup.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "User Already exists ",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }else {

                       uploadPictureToFireBaseDB(user);


                            Toast.makeText(MainActivity.this, "user created", Toast.LENGTH_SHORT).show();
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


    public String getCurrentFireBaseUserID(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId=null;
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
           userId = user.getUid();

        }
      return   userId;
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

            createAccount(getUserFromInputFiledValues());
        }else if(view==mainBinding.update){
            updateUser();
        }else if(view==mainBinding.cretedb){
            initFireBaseDBAndAddValue();
        }else if(view==mainBinding.readDb){
            readFromDd();
        }else if(view==mainBinding.getuser){
       // getCurrentFireBaseUser();
    }else if(view==mainBinding.imageViewProfile){
choosePictureFromGallery();
        }
    }



    public void choosePictureFromGallery(){
        Intent intent =new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,FireBaseConstants.PICK_PROFILE_PIC);


    }

    public void uploadPictureToFireBaseDB(final User user){
imageRef=storageReferencePicture.child("/images"+selectedImageUri.getLastPathSegment());
        progressDialog=new ProgressDialog(this);
        progressDialog.setMax(100);
        progressDialog.setMessage("uploading.. ");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();

        //start uploading

        uploadTask=imageRef.putFile(selectedImageUri);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                //sets and increments value of progressbar
                progressDialog.incrementProgressBy((int) progress);
            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                // Handle unsuccessful uploads
                Toast.makeText(MainActivity.this,"Error in uploading!",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d("debug","url "+downloadUrl);
                Toast.makeText(MainActivity.this,"Upload successful",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
User userWithProfilePic=user;
                userWithProfilePic.photoUrl=downloadUrl.toString();
                String uuid= getCurrentFireBaseUserID();
                 addUserTOFireBaseDB(userWithProfilePic,uuid);

                //showing the uploaded image in ImageView using the download url
                Picasso.with(MainActivity.this).load(downloadUrl).into(mainBinding.imageViewProfile);
            }
        });



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){
            if(requestCode==FireBaseConstants.PICK_PROFILE_PIC){
                selectedImageUri=data.getData();

                try {

                    Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),selectedImageUri);
                 //   bitmap.compress(Bitmap.CompressFormat.JPEG,20,);

                    selectedImageUri= savePicture(this,bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            Toast.makeText(getApplicationContext(),"error ",Toast.LENGTH_LONG).show();
        }
    }
    public  Uri savePicture(Context context, Bitmap bitmap) throws IOException {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},122);
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        tempDir.mkdir();
        File tempFile = File.createTempFile("profile", ".jpg", tempDir);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bytes);
        mainBinding.imageViewProfile.setImageBitmap(bitmap);
        byte[] bitmapData = bytes.toByteArray();

        //write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(tempFile);
    }

    public User getUserFromInputFiledValues(){
    String email=mainBinding.emailTv.getText().toString();
    if(email.isEmpty()){
        mainBinding.emailTv.setError("Email field missing ");
        mainBinding.emailTv.requestFocus();
    }
    String password=mainBinding.passwordTv.getText().toString();
    String name=mainBinding.userName.getText().toString();
    String address=mainBinding.address.getText().toString();

    User user=new User(name,email,password,address,"iamge url");

    return user;
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
