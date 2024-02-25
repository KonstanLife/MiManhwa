package com.example.mimanhwa.Activities.AddActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mimanhwa.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddGenreActivity extends AppCompatActivity {


    //View elements

    private TextView genreTitle;
    private TextView addFrontPageButton;
    private AppCompatButton addGenreButton;
    private EditText genreNameEditText;

    //Tool bar elementes
    Toolbar toolbar;
    ImageButton backBtn;

    //Elementos de cambio de tema
    boolean nightMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //Campo para almacenar nombre de genero
    private  String genre = "";

    //Constantes
    private static final String TAG = "ADD_IMG_TAG";
    private static  final int IMG_PICK_CODE = 1000;

    //ImgUri
    private Uri imgUri = null;

    //Direccion de la imagen en  firestore
    private String filePathAndName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_genre);

        //Inicializacion de view items

        addFrontPageButton = findViewById(R.id.add_genre_frontpage_click_tv);
        addGenreButton = findViewById(R.id.add_genre_btn);
        genreTitle = findViewById(R.id.add_genre_title_tv);
        genreNameEditText = findViewById(R.id.add_genre_name_et);


        //Inicialización  de tool bar y sus elementos
        toolbar = (Toolbar)findViewById(R.id.toolbar_back);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        backBtn = findViewById(R.id.toolbar_back_imbtn);

        //Control del tema nocturno/diurno
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode",false);
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        ///init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Porfavor esperen");
        progressDialog.setCanceledOnTouchOutside(false);


        //click listener back button

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        //click listener addFrontPageButton

        addFrontPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgPickIntent();
            }
        });

        //click listener addGenreButton

        addGenreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
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


    //Validacion de datos

    private void validateData() {

        //Se comprueba que campo no este vacio y se guarda en Firebase

        genre = genreNameEditText.getText().toString().trim();

        if(TextUtils.isEmpty(genre)){

            Toast.makeText(this, "Introduce la categoria", Toast.LENGTH_SHORT).show();
        }else if(imgUri == null){

            Toast.makeText(this, "Sube una foto de portada", Toast.LENGTH_SHORT).show();
        }else{
            uploadFrontPageToStorage();
        }
    }

    private void addCategoryFirebase(String uploadedImgUrl, long timestamp) {

        //show progress
        progressDialog.setMessage("Añadiendo categoria...");


        //setup info add in farebase db

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);              //Identificador de genero
        hashMap.put("genre",""+genre);               //Titulo de genero
        hashMap.put("timestamp",timestamp);          //Tiempo
        hashMap.put("uid",""+firebaseAuth.getUid()); //Identidicador de usuario
        hashMap.put("url",""+uploadedImgUrl);        //Url de descarga la imagen de portada
        hashMap.put("dirImg",""+filePathAndName);        //Direccion de la imagen de portada en firestore

        //Añadir a firebase db---- Database root > Genres > genreId > genre info

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Genres");
        ref.child(""+timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                progressDialog.dismiss();
                finish();
                Toast.makeText(AddGenreActivity.this, "Categoria se añadio correctamente", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(AddGenreActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Subir fortada al storeage
    private void uploadFrontPageToStorage() {

        progressDialog.setMessage("Cargando portada...");
        progressDialog.show();


        long timestamp = System.currentTimeMillis();

        filePathAndName = "GenreFrontPageImg/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Log.d(TAG,"onSuccess: imagen de la portada se añadio al Storage");
                Log.d(TAG,"onSuccess tomando url de la imagen");

                //get img url
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedImgUrl = ""+uriTask.getResult();

                //upload to firebase db

                addCategoryFirebase(uploadedImgUrl,timestamp);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"onFailure: surgio error al cargar la portada" + e.getMessage());
                Toast.makeText(AddGenreActivity.this, "Surgio error al cargar la portada" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    //Cargar imagen desde el dispsitivo
    private void imgPickIntent() {
        Log.d(TAG,"imgPickIntent: empezando pick intent");

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select img"),IMG_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == IMG_PICK_CODE){
                Log.d(TAG,"onActivityResult: imagen captada");

                imgUri = data.getData();

                Log.d(TAG,"onActivityResuly URI" + imgUri);
            }
        }else{

            Log.d(TAG,"onActivityResult se cancelo subida de imagen");
            Toast.makeText(this, "se cancelo subida de imagen", Toast.LENGTH_SHORT).show();

        }
    }
}