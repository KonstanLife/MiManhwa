package com.example.mimanhwa.Activities.GeneralActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.mimanhwa.Activities.ManagementActivity.ManagementGenreActivity;
import com.example.mimanhwa.Adapters.AdapterGenre;
import com.example.mimanhwa.Adapters.AdapterManagementGenre;
import com.example.mimanhwa.Models.ModelGenre;
import com.example.mimanhwa.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GenreActivity extends AppCompatActivity {

    //View items

    private RecyclerView recyclerView;

    //Toolbar
    Toolbar toolbar;
    ImageButton backBtn;

    //Elementos de cambio de tema
    boolean nightMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //Firebase auth
    private FirebaseAuth firebaseAuth;

    //arrayList to store category
    private ArrayList<ModelGenre> genreArrayList;
    //adapter
    private AdapterGenre adapterGenre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre);

        //Inicializacion de view items
        recyclerView = findViewById(R.id.genre_rv);

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



        //Inicializando firebaseauth
        firebaseAuth = FirebaseAuth.getInstance();

        // checkUser();

        //Cargar las generos desde firebase
        loadCategories();


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
                    adapterGenre.getFilter().filter(newText);
                }catch (Exception e){

                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void loadCategories() {

        //Inicializar arraylist
        genreArrayList = new ArrayList<>();

        //Cargar todos los generos desde firebase > Genres
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Genres");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Primero se vacia array list y despues se añden los datos
                genreArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    //Cargar data
                    ModelGenre model = ds.getValue(ModelGenre.class);
                    //Añadir a arrayList
                    genreArrayList.add(model);

                }

                //setup adapter
                adapterGenre = new AdapterGenre(GenreActivity.this,genreArrayList);

                //set adapte to recicler view
                recyclerView.setAdapter(adapterGenre);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}