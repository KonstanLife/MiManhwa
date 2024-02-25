package com.example.mimanhwa.Activities.AddActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class AddBookActivity extends AppCompatActivity {


    //Views camps
    private TextView activityTitle;
    private EditText bookNameCamp;
    private EditText bookInfoCamp;

    private TextView selectGenre;
    private TextView addFrontPage;
    private EditText authorEditText;
    private Button addBookBtn;

    //Variable para guardar estado de la actividad edicion/añadir
    private String stateActivity;

    //Id del libro que recibimos desde adaptador
    private  String bookId;

    //Firebase auth
    private FirebaseAuth firebaseAuth;

    //Array para almacenar generos

    private ArrayList<String> genreTitleArrayList,genreIdArrayList;

    //progress dialog
    private ProgressDialog progressDialog;

    //ImgUri
    private Uri imgUri = null;

    //Constants
    private static final String TAG = "ADD_IMG_TAG";
    private static  final int IMG_PICK_CODE = 1000;

    //Variables para almacenamiento temporal de info
    private String bookname = "", bookInfo ="", author ="";

    //select category id and category title
    private String selectedGenreId,selectedGenreTitle;

    //user
    private FirebaseUser firebaseUser;
    private String userName;

    //Tool bar items
    private Toolbar toolbar;
    private ImageButton backBtn;

    //Elementos de cambio de tema
    private boolean nightMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //Direccion de la imagen en  firestore
    private String filePathAndName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        //Inicializacion de Firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // Cargar generos desde la base de datos
        loadBookGenres();

        //Inicialización  de tool bar y sus elementos
        toolbar = (Toolbar)findViewById(R.id.toolbar_back);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        backBtn = findViewById(R.id.toolbar_back_imbtn);


        //Inicializacion de view items
        bookNameCamp = findViewById(R.id.add_book_name_et);
        bookInfoCamp = findViewById(R.id.add_book_desc_et);
        selectGenre = findViewById(R.id.add_book_genre_tv);
        addFrontPage = findViewById(R.id.add_book_frontpage_click_tv);
        addBookBtn = findViewById(R.id.add_book_btn);
        activityTitle = findViewById(R.id.add_book_title_tv);
        authorEditText = findViewById(R.id.add_book_author_et);

        //Bundle
        Bundle takeDates = getIntent().getExtras();
        stateActivity = takeDates.getString("stateActivity");
        bookId = takeDates.getString("bookId");

        //Compruebo si activity se usa para editar o añadir
        //En caso de actualizar descargo datos de esta lecutra desde database
        if(stateActivity.equals("add")){
            activityTitle.setText("Añadir lectura");
            addBookBtn.setText("Añadir");
        }else if(stateActivity.equals("edit")){
            activityTitle.setText("Editar lectura");
            addBookBtn.setText("Actualizar");
            addFrontPage.setVisibility(View.GONE);
            loadBookInfo();
        }


        //Inicializar progress dialog
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

        // click listener add frontpage
        addFrontPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imgPickIntent();
            }
        });

        // click listener button genre
        selectGenre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                categoryPickDialog();
            }
        });

        //Click listener add/update button
        addBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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


    //Metodo para cargar informacion de base de datos y guardar en arraylist

    private void loadBookInfo() {

        DatabaseReference refBook = FirebaseDatabase.getInstance().getReference("Books");
        refBook.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get book info
                selectedGenreId = "" + snapshot.child("genreId").getValue();
                String description = "" + snapshot.child("description").getValue();
                String title = "" + snapshot.child("title").getValue();
                String author = "" + snapshot.child("author").getValue();

                // set to views
                bookNameCamp.setText(title);
                bookInfoCamp.setText(description);
                authorEditText.setText(author);

                DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Genres");
                refBookCategory.child(selectedGenreId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get category
                        String category = ""+snapshot.child("genre").getValue();
                        //set to category text view
                        selectGenre.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Validamos los campos y añadimos o actualizamos informacion
    private void validateData(){

        //geta data
        bookname = bookNameCamp.getText().toString().trim();
        bookInfo = bookInfoCamp.getText().toString().trim();
        author = authorEditText.getText().toString().trim();

        if(TextUtils.isEmpty(bookname)){

            Toast.makeText(this, "Introiduce titulo de la lectura", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(bookInfo)){
            Toast.makeText(this, "Introduce descripcion de la lectura", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(author)){
            Toast.makeText(this, "Introduce nombre de autor", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(selectedGenreTitle)){
            Toast.makeText(this, "Elige el genero", Toast.LENGTH_SHORT).show();
        }
        /* else if(imgUri == null){

            Toast.makeText(this, "Sube una foto de portada", Toast.LENGTH_SHORT).show();
        }else{
        }*/


        if(stateActivity.equals("add")){
            if(imgUri == null){

                Toast.makeText(this, "Sube una foto de portada", Toast.LENGTH_SHORT).show();
            }else{
                uploadFrontPageToStorage();
                //  finish();
            }
        }else if(stateActivity.equals("edit")){
            updateBookInfo();
            finish();
        }
    }

    //Subir imagen de protada a Firebase storage
    private void uploadFrontPageToStorage() {

        progressDialog.setMessage("Cargando portada...");
        progressDialog.show();


        long timestamp = System.currentTimeMillis();

         filePathAndName = "FrontPageImg/" + timestamp;

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

                uploadBookInfoInBd(uploadedImgUrl,timestamp);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"onFailure: surgio error al cargar la portada" + e.getMessage());
                Toast.makeText(AddBookActivity.this, "Surgio error al cargar la portada" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    //Metodo para subir informacion de lectura a base de datos
    private void uploadBookInfoInBd(String uploadedImgUrl, long timestamp) {

        Log.d(TAG,"uploadBookToStorage: uploading book info to firebase db...");
        progressDialog.setMessage("Subiendo información de la lectura");
        String uid = firebaseAuth.getUid();

        //setup data to upload
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+bookname);
        hashMap.put("description",""+bookInfo);
        hashMap.put("genreId",""+selectedGenreId);
        hashMap.put("url",""+uploadedImgUrl);
        hashMap.put("timestamp",timestamp);
        hashMap.put("author",author);
        hashMap.put("viewsCount",0);
        hashMap.put("publisherId","" + firebaseAuth.getCurrentUser().getUid());

        //db reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                progressDialog.dismiss();
                finish();
                Log.d(TAG,"onSucces: Se subio con exito");
                Toast.makeText(AddBookActivity.this, "Se subio con exito", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"onFailure: error al subir archivo a bd" + e.getMessage());
                Toast.makeText(AddBookActivity.this, "Error al subir archivo a bd" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    //Metodo para actualizar informacion de lectura en base de datos
    private void updateBookInfo(){
        progressDialog.setMessage("Actualizando información de lectura");
        progressDialog.show();



        //setup data to update to db
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("title","" + bookNameCamp.getText());
        hashMap.put("description", ""+bookInfoCamp.getText());
        hashMap.put("genreId",""+selectedGenreId);
        hashMap.put("author",""+authorEditText.getText());


        //start update
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Toast.makeText(AddBookActivity.this, "Se actualizo", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(AddBookActivity.this, "" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //Metodo para cargar generos desde la base de datos
    private void loadBookGenres() {

        Log.d(TAG,"loadImgGenres: cargando generos");
        genreTitleArrayList = new ArrayList<>();
        genreIdArrayList = new ArrayList<>();

        //db reference to load genres
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Genres");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                genreTitleArrayList.clear(); //clear before data
                genreIdArrayList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){

                    //get id and title of genre
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("genre").getValue();

                    //add to respective arrayList
                    genreTitleArrayList.add(categoryTitle);
                    genreIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    //Metodo que permite elegir una categiria
    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: mostrando dialigo con genero");

        //sacar cadena de generos desde array list
        String[] categoriesArray = new String[genreTitleArrayList.size()];
        for(int i = 0; i < genreTitleArrayList.size(); i++){

            categoriesArray[i] = genreTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Genres").setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //hande item click
                //get clicked item from list
                selectedGenreTitle = genreTitleArrayList.get(which);
                selectedGenreId = genreIdArrayList.get(which);
                selectGenre.setText(selectedGenreTitle);
            }
        }).show();

    }

    //Cargar portada
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