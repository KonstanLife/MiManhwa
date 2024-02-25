package com.example.mimanhwa.Activities.AddActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import android.widget.Button;
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

public class AddChapterActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton backBtn;

    //Elementos de cambio de tema
    private boolean nightMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //View items
    private EditText addChapterName;
    private TextView uploadChapterButton;
    private Button addChapterBtn;


    //firebath auth
    private FirebaseAuth firebaseAuth;

    //progress dialog declaration
    private ProgressDialog progressDialog;

    //ImgUri
    private Uri pdfUri = null;

    //Constants
    private static final String TAG = "ADD_PDF_TAG";
    private static  final int PDF_PICK_CODE = 1000;


    private String chapterName = "";
    private String bookId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chapter);

        //Inicializacion view items
        addChapterName = findViewById(R.id.add_chapter_name_et);
        uploadChapterButton = findViewById(R.id.add_chapter_frontpage_click_tv);
        addChapterBtn = findViewById(R.id.add_chapter_btn);


        //Inicialización  de tool bar y sus elementos
        toolbar = (Toolbar)findViewById(R.id.toolbar_back);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        backBtn = findViewById(R.id.toolbar_back_imbtn);

        //Bundle
        Bundle takeDates = getIntent().getExtras();
        bookId = takeDates.getString("bookId");

        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Porfavor esperen");
        progressDialog.setCanceledOnTouchOutside(false);

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


        //click listener de boton cargar capitulo

        uploadChapterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgPickIntent();
            }
        });

        //click listener de boton subir a base datos capitulo y su info

        addChapterBtn.setOnClickListener(new View.OnClickListener() {
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

    //metodo para validar datos introducidos en campos
    private void validateData(){

        //geta data
        chapterName = addChapterName.getText().toString().trim();

        if(TextUtils.isEmpty(chapterName)){

            Toast.makeText(this, "Introiduce titulo de la capitulo", Toast.LENGTH_SHORT).show();
        } else if(pdfUri == null){

            Toast.makeText(this, "Sube un capitulo en formato pdf", Toast.LENGTH_SHORT).show();
        }else{
            uploadPDFToStorage();
        }


    }

    //Metodo para cargar PDF a Storege
    private void uploadPDFToStorage() {

        progressDialog.setMessage("Cargando capitulo...");
        progressDialog.show();


        long timestamp = System.currentTimeMillis();

        String filePathAndName = "Chapters/" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Log.d(TAG,"onSuccess: imagen de la portada se añadio al Storage");
                Log.d(TAG,"onSuccess tomando url de la imagen");

                //get img url
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedPdfUrl = ""+uriTask.getResult();

                //upload to firebase db

                uploadChapterInfoInBd(uploadedPdfUrl,timestamp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"onFailure: surgio error al cargar la portada" + e.getMessage());
                Toast.makeText(AddChapterActivity.this, "Surgio error al cargar la portada" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Metodo para subir informacion de capitulo a base de datos
    private void uploadChapterInfoInBd(String uploadedImgUrl, long timestamp) {

        Log.d(TAG,"uploadBookToStorage: uploading chapter info to firebase db...");
        progressDialog.setMessage("Subiendo información del capitulo");
        String uid = firebaseAuth.getUid();

        //setup data to upload
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+chapterName);
        hashMap.put("bookId",bookId);
        hashMap.put("url",""+uploadedImgUrl);
        hashMap.put("timestamp",timestamp);

        //db reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chapters");
        ref.child(""+timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                progressDialog.dismiss();
                finish();
                Log.d(TAG,"onSucces: Se subio con exito");
                Toast.makeText(AddChapterActivity.this, "Se subio con exito", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"onFailure: error al subir archivo a bd" + e.getMessage());
                Toast.makeText(AddChapterActivity.this, "Error al subir archivo a bd" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }


    //Metodo para subir imagen de portada
    private void imgPickIntent() {
        Log.d(TAG,"pdfPickIntent: empezando pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select pdf"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == PDF_PICK_CODE){
                Log.d(TAG,"onActivityResult: pdf captado");

                pdfUri = data.getData();

                Log.d(TAG,"onActivityResuly URI" + pdfUri);
            }
        }else{

            Log.d(TAG,"onActivityResult se cancelo subida de imagen");
            Toast.makeText(this, "se cancelo subida de imagen", Toast.LENGTH_SHORT).show();

        }
    }
}