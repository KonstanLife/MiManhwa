package com.example.mimanhwa.Activities.GeneralActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mimanhwa.Activities.EditActivity.EditProfileActivity;
import com.example.mimanhwa.Adapters.AdapterComment;
import com.example.mimanhwa.Extensions.MyApplication;
import com.example.mimanhwa.Extensions.TimestampConverter;
import com.example.mimanhwa.Models.ModelComment;
import com.example.mimanhwa.R;
import com.example.mimanhwa.databinding.DialogAddCommentBinding;
import com.example.mimanhwa.databinding.DialogDescriptionBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class BookInfoActivity extends AppCompatActivity {

    //View items details
    private ImageView bookFrontPage;
    private TextView titleBookTextView;
    private TextView descriptionBookTextView;
    private TextView genreBookTextView;
    private  TextView visitsTextView;
    private TextView dateTextView;
    private ImageButton addCommentsButton;
    private TextView authorTextView;

    private RecyclerView recyclerView;

    private Button startReadButton;

    private TextView readMoreBtn;

    //ToolBar
    Toolbar toolbar;
    ImageButton backBtn;

    //Elementos de cambio de tema
    boolean nightMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //book id
    private String bookId;

    boolean isInMyFavorite = false;

    private FirebaseAuth firebaseAuth;


    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;
    private String comment = "";

    // Crear una instancia de FirebaseTimestampConverter
    TimestampConverter timestampConverter = new TimestampConverter();

    //menu
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        titleBookTextView = findViewById(R.id.detailsTitleTV);
        descriptionBookTextView = findViewById(R.id.detailsDescriptionTv);
        genreBookTextView = findViewById(R.id.detailsCategoryTv);
        authorTextView = findViewById(R.id.detailsAuthorTv);
        visitsTextView = findViewById(R.id.detailsVisitsTv);
        dateTextView = findViewById(R.id.detailsDateTv);
        readMoreBtn = findViewById(R.id.readMoreTv);
        addCommentsButton = findViewById(R.id.addCommentBtn);
        recyclerView = findViewById(R.id.commentRV);

        startReadButton = findViewById(R.id.start_read_btn);
        bookFrontPage = findViewById(R.id.detailsFrontPageImgView);

        //Firebase auth
        firebaseAuth = FirebaseAuth.getInstance();


        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Espera");
        progressDialog.setCanceledOnTouchOutside(false);


        //Bundle
        Bundle takeDates = getIntent().getExtras();
        bookId = takeDates.getString("bookId");

        //Inicialización  de tool bar y sus elementos
        toolbar = (Toolbar)findViewById(R.id.toolbar_back);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        backBtn = findViewById(R.id.toolbar_back_imbtn);

        checkIsFavorite();

        loadBookDetails();
        loadComments();


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



        readMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                descriptionDialog();
            }
        });

        addCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addComentDialog();
            }
        });

        startReadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Bundle sendDates = new Bundle();
                sendDates.putString("bookId",bookId);

                Intent intent = new Intent(BookInfoActivity.this, ChaptersActivity.class);
                //send bundle
                intent.putExtras(sendDates);

                startActivity(intent);
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

        if(id == R.id.add_marker){
            if(isInMyFavorite){
                //in favorite, remove from favorite
                MyApplication.removeFromFavorite(BookInfoActivity.this,bookId);
                isInMyFavorite = false;
            }else{

                //not in favorite, add to favorite
                MyApplication.addToFavorite(BookInfoActivity.this, bookId);
                isInMyFavorite = true;
            }

            updateFavoriteIcon();
        }


        return super.onOptionsItemSelected(item);
    }

    //Metodo para actualizar icono de favoritos
    private void updateFavoriteIcon() {
        MenuItem item = mMenu.findItem(R.id.add_marker); // Obtener el ítem del menú
        if (isInMyFavorite) {
            item.setIcon(R.drawable.ic_added);
        } else {
            item.setIcon(R.drawable.ic_add_fav_white);
        }
    }

    //menu Inflater
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.book_indo_menu,menu);

        // Guardar una referencia al menú para su uso posterior
        mMenu = menu;

        MenuItem item = mMenu.findItem(R.id.add_marker); // Obtener el ítem del menú
        if (isInMyFavorite) {
            item.setIcon(R.drawable.ic_added);
        } else {
            item.setIcon(R.drawable.ic_add_fav_white);
        }

        return super.onCreateOptionsMenu(menu);
    }


    //Metodo para cargar comentarios desde BD
    private void loadComments() {
        //init array list
        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //clear array list
                commentArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelComment model = ds.getValue(ModelComment.class);
                    commentArrayList.add(model);
                }

                adapterComment = new AdapterComment(BookInfoActivity.this,commentArrayList);

                recyclerView.setAdapter(adapterComment);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Metodo para invocar dialogo para escribir comentario
    private void addComentDialog() {

        DialogAddCommentBinding commentAddBinding = DialogAddCommentBinding.inflate(LayoutInflater.from(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


        commentAddBinding.dialogAddCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data
                comment = commentAddBinding.dialogAddCommentTextEt.getText().toString().trim();
                //validate data
                if(TextUtils.isEmpty(comment)){
                    Toast.makeText(BookInfoActivity.this, "Introduce comentario", Toast.LENGTH_SHORT).show();
                }else{
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });
    }


    //Metodo para invocar dialogo para leer la  descripcion
    private void descriptionDialog() {

        DialogDescriptionBinding dialogDescriptionBinding = DialogDescriptionBinding.inflate(LayoutInflater.from(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog);
        builder.setView(dialogDescriptionBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //get data
                String title = ""+snapshot.child("title").getValue();
                String descriptions = ""+snapshot.child("description").getValue();

                dialogDescriptionBinding.dialogDescriptionTitleTv.setText(title);
                dialogDescriptionBinding.dialogDescriptionContentTv.setText(descriptions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Metodo para añadir comentario
    private void addComment() {

        progressDialog.setMessage("Añadiendo comentario..");
        progressDialog.show();

        //timestamp de comentario
        String timestamp = ""+System.currentTimeMillis();

        //setup data to add in db

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("bookId",""+bookId);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("comment",""+comment);
        hashMap.put("uid",""+firebaseAuth.getUid());

        //DB path to data into it
        //Books > bookID > Comments > comentId > commentData

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).child("Comments").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Toast.makeText(BookInfoActivity.this, "Comentario añadido", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(BookInfoActivity.this, "Error al añadir comentario" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    //Metodo para cargar detalles de lectura
    private void loadBookDetails() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //get data
                String title = ""+snapshot.child("title").getValue();
                String descriptions = ""+snapshot.child("description").getValue();
                String genreId = ""+snapshot.child("genreId").getValue();
                String url = ""+snapshot.child("url").getValue();
                String visits = ""+snapshot.child("viewsCount").getValue();
                String author =""+snapshot.child("author").getValue();
                String timestamp = ""+snapshot.child("timestamp").getValue();


                //set data
                titleBookTextView.setText(title);
                descriptionBookTextView.setText(descriptions);
                authorTextView.setText(author);
                visitsTextView.setText(visits);

                loadBookFrontPage(url);
                loadGenre(genreId);

                String formatDate = timestampConverter.convertTimestamp(Long.parseLong(timestamp));
                dateTextView.setText(formatDate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Metodo para cargar foto de perfil
    private void loadBookFrontPage(String imgUrl) {

        // Cargar la imagen usando Glide
        Glide.with(BookInfoActivity.this)
                .load(imgUrl)  // El URL de la imagen
                .placeholder(R.drawable.ic_person_white)
                .into(bookFrontPage);
    }

    //Metodo para cargar los generos
    private void loadGenre( String categoryId) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Genres");
        ref.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String genre = ""+snapshot.child("genre").getValue();

                genreBookTextView.setText(genre);
                //set to category text view

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //Metodo para comprobar que el libro este en favoritos
    private void checkIsFavorite() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isInMyFavorite = snapshot.exists(); //true id exists, false if not exist
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}