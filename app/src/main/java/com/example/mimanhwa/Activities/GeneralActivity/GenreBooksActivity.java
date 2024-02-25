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

import com.example.mimanhwa.Activities.AddActivity.AddBookActivity;
import com.example.mimanhwa.Activities.ManagementActivity.ManagementBookActivity;
import com.example.mimanhwa.Adapters.AdapterGenreBooks;
import com.example.mimanhwa.Adapters.AdapterManagementBook;
import com.example.mimanhwa.Models.ModelBook;
import com.example.mimanhwa.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GenreBooksActivity extends AppCompatActivity {

    //View items
    private RecyclerView recyclerView;

    //Firebase Auth
    private FirebaseAuth firebaseAuth;

    //arrayList to store book
    private ArrayList<ModelBook> bookArrayList;
    //adapter
    private AdapterGenreBooks adapterGenreBooks;

    //user date
    private FirebaseUser firebaseUser;
    private String userType;

    //Genre id
    private  String genreId;

    //Tool bar
    private Toolbar toolbar;
    private ImageButton backBtn;

    //Elementos de cambio de tema
    private boolean nightMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_books);


        //Inicializacion de view items
        recyclerView = findViewById(R.id.genre_book_rv);

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

        //---------
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //Bundle
        Bundle takeDates = getIntent().getExtras();
        genreId = takeDates.getString("genreId");

        loadBooks();


        //Click listener backBtn

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
                    adapterGenreBooks.getFilter().filter(newText);
                }catch (Exception e){

                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    //Metodo para cargar lectura desde bd y guardarla en arratlist
    private void loadBooks() {

        //init arraylist
        bookArrayList = new ArrayList<>();

        //get all books from firebase > Books
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("genreId").equalTo(genreId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //clear arraylist before add data
                bookArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelBook model = ds.getValue(ModelBook.class);
                    //add to arrayList

                   bookArrayList.add(model);
                }

                //setup adapter
                adapterGenreBooks = new AdapterGenreBooks(GenreBooksActivity.this,bookArrayList);

                //set adapte to recicler view
                recyclerView.setAdapter(adapterGenreBooks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}