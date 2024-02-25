package com.example.mimanhwa.Activities.GeneralActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.mimanhwa.Adapters.AdapterChapter;
import com.example.mimanhwa.Models.ModelChapter;
import com.example.mimanhwa.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChaptersActivity extends AppCompatActivity {


    Toolbar toolbar;
    ImageButton backBtn;

    //Elementos de cambio de tema
    boolean nightMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //recyclerview
    private RecyclerView recyclerView;

    //id de lectura
    private  String bookId;



    //arrayList to store chapters
    private ArrayList<ModelChapter> chaptersArrayList;
    //adapter
    private AdapterChapter adapterChapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapters);

        //Inicialización  de tool bar y sus elementos
        toolbar = (Toolbar)findViewById(R.id.toolbar_back);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        backBtn = findViewById(R.id.toolbar_back_imbtn);


        //Init view camps
        recyclerView = findViewById(R.id.chapter_rv);

        //Bundle
        Bundle takeDates = getIntent().getExtras();
        bookId = takeDates.getString("bookId");



        //---------
        firebaseAuth = FirebaseAuth.getInstance();
        loadChapters();


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

    //controlador de search view
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);


        MenuItem.OnActionExpandListener onActionExpandListener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                return true;
            }
        };

        menu.findItem(R.id.search).setOnActionExpandListener(onActionExpandListener);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint("Buscar lectura...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                try {
                    // adapterBook.getFilter().filter(newText);
                }catch (Exception e){

                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    //Metodo para cargar capitulos desde la base de datos y guardarlos en una arraylist
    private void loadChapters() {

        chaptersArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chapters");
        ref.orderByChild("bookId").equalTo(bookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chaptersArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){

                    ModelChapter model = ds.getValue(ModelChapter.class);
                    chaptersArrayList.add(model);
                }

                adapterChapter = new AdapterChapter(ChaptersActivity.this,chaptersArrayList);
                recyclerView.setAdapter(adapterChapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}