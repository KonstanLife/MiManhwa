package com.example.mimanhwa.Activities.EditActivity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mimanhwa.Activities.HelpActivity.ContactActivity;
import com.example.mimanhwa.Activities.HelpActivity.FAQActivity;
import com.example.mimanhwa.Activities.HelpActivity.TermsActivity;
import com.example.mimanhwa.Activities.LoginActivity.LoginActivity;
import com.example.mimanhwa.Activities.LoginActivity.RegisterActivity;
import com.example.mimanhwa.Activities.MainActivity;
import com.example.mimanhwa.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {


    //Toolbar
    Toolbar toolbar;
    ImageButton backBtn;

    //Elementos de cambio de tema
    boolean nightMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //Activity views items

    private ImageView photoUserImageView;
    private TextView userNameTextView;
    private TextView userTypeTextView;
    private RelativeLayout addPhotoClickRL;
    private  TextView userMailTextView;
    private Switch switchNotifications;
    private RelativeLayout doQuestionRl;
    private RelativeLayout faqRl;
    private RelativeLayout privacityRl;
    private AppCompatButton saveChangesButton;

    //Fire base auth
    private FirebaseAuth firebaseAuth;

    //Progress dialog
    private ProgressDialog progressDialog;

    //Direccion imagen
    private Uri imageUri = null;

    //Constants
    private static  final String TAG = "PROFILE_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Inicializando views items

        photoUserImageView = findViewById(R.id.profile_profile_img);
        userNameTextView = findViewById(R.id.profile_name_tv);
        userTypeTextView = findViewById(R.id.profile_type_tv);
        addPhotoClickRL = findViewById(R.id.profile_add_photo_rl);
        userMailTextView = findViewById(R.id.prefile_mail_text);
        switchNotifications = findViewById(R.id.switch1);
        saveChangesButton = findViewById(R.id.save_profile_changes_btn);

        doQuestionRl = findViewById(R.id.do_question_rl);
        faqRl = findViewById(R.id.faq_rl);
        privacityRl = findViewById(R.id.privacity_rl);

        //Inicialización  de tool bar y sus elementos
        toolbar = (Toolbar)findViewById(R.id.toolbar_back);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        backBtn = findViewById(R.id.toolbar_back_imbtn);



        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Porfavor espera");
        progressDialog.setCanceledOnTouchOutside(false);

        //inicializar firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        //cargar informacion de usuario
        loadUserInfo();

        //Control del tema nocturno/diurno
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode",false);
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }


        //click listener back button

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


         //Add photo relative layout click cliestener
        addPhotoClickRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageGallery();
            }
        });

        //Save profile changes button click listener
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        //do question click listener
        doQuestionRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(EditProfileActivity.this, ContactActivity.class));
            }
        });

        //faq click listener
        faqRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditProfileActivity.this, FAQActivity.class));
            }
        });

        //privacity click listner
        privacityRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditProfileActivity.this, TermsActivity.class));
            }
        });

    }

    //Click listener de main menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.night ){

            Toast.makeText(this, "Boton de notificaion pulsado", Toast.LENGTH_SHORT).show();
            if (nightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor = sharedPreferences.edit();
                editor.putBoolean("nightMode", false);
                // Actualizar el estado de nightMode
                nightMode = !nightMode;
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor = sharedPreferences.edit();
                editor.putBoolean("nightMode", true);
                // Actualizar el estado de nightMode
                nightMode = !nightMode;
            }
            editor.apply();
        }

        return super.onOptionsItemSelected(item);
    }
    //menu Inflater
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.simple_menu,menu);


        return super.onCreateOptionsMenu(menu);
    }


    //Cargar informacion de usuario desde database y asignarla a campos de view
    private void loadUserInfo() {

        Log.d(TAG,"loadUserInfo: Añadiendo información de usuario" + firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //get data from bd

                String email = ""+snapshot.child("email").getValue();
                String name = ""+snapshot.child("name").getValue();
                String profileImage = ""+snapshot.child("profileImage").getValue();
                String timestamp = ""+snapshot.child("timestamp").getValue();
                String uid = ""+snapshot.child("uid").getValue();
                String userType = ""+snapshot.child("userType").getValue();

                //set data to ui
                userNameTextView.setText(name);
                userTypeTextView.setText("Tipo usuario: " +userType);
                userMailTextView.setText(email);


                //cargar imagen
                if(!(profileImage.equals(""))){
                    Picasso.get().load(profileImage).into(photoUserImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Validar que los datos sean correctos y actualizar
    private void validateData(){

        //validate data
            if (imageUri == null){
                //update without image
                updateProfile("");
            }else{
                //update with image
                uploadImage();
            }
    }

    //Subir imagen
    private void uploadImage(){
        progressDialog.setMessage("Actualizando imagen de perfil");
        progressDialog.show();

        //Image path and name
        String filePathAndName ="ProfileImages/" + firebaseAuth.getUid();

        //storage reference
        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedImageUrl = ""+uriTask.getResult();
                updateProfile(uploadedImageUrl);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "Error al cargar imagen" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Actualizar perfil
    private void updateProfile(String imageUrl){

        progressDialog.setMessage("Actualizando perfil de usuario");
        progressDialog.show();

        //setup data to update
        HashMap<String,Object> hashMap = new HashMap<>();

        if(imageUri != null){
            hashMap.put("profileImage",""+imageUrl);
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "Error al actualizar " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Coger imagen de galeria
    private void pickImageGallery(){
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    //get image uri
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG,"onActivityResult: " + imageUri);
                        Intent data = result.getData();

                        imageUri = data.getData();
                        Log.d(TAG,"onActivityResult: From gellery" + imageUri);
                        photoUserImageView.setImageURI(imageUri);
                    }else{

                        Toast.makeText(EditProfileActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
                    }
                }
            }

    );
}