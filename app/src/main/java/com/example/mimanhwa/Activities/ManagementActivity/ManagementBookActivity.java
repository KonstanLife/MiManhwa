package com.example.mimanhwa.Activities.ManagementActivity;

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

public class ManagementBookActivity extends AppCompatActivity {

    //View items
    private FloatingActionButton addBookBtn;
    private RecyclerView recyclerView;

    //Firebase Auth
    private FirebaseAuth firebaseAuth;

    //arrayList to store book
    private ArrayList<ModelBook> bookArrayList;
    //adapter
    private AdapterManagementBook adapterManagementBook;

    //user date
    private FirebaseUser firebaseUser;
    private String userType;


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
        setContentView(R.layout.activity_management_book);

        //Inicializacion de view items
        addBookBtn = findViewById(R.id.add_book_float_btn);
        recyclerView = findViewById(R.id.management_book_rv);

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


      //  checkUser();
        loadBooks();


        //Click listener de addBookBtn

        addBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //use bundle for indicate to bookAddActivity their function
                Bundle sendDates = new Bundle();
                sendDates.putString("stateActivity","add");

                Intent intent = new Intent(ManagementBookActivity.this,AddBookActivity.class);
                intent.putExtras(sendDates);
                startActivity(intent);
            }
        });

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
                     adapterManagementBook.getFilter().filter(newText);
                }catch (Exception e){

                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    //Metodo para cargar lectura
    private void loadBooks() {

        //init arraylist
        bookArrayList = new ArrayList<>();
        loadUserType();

        //get all books from firebase > Books
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //clear arraylist before add data
                bookArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelBook model = ds.getValue(ModelBook.class);
                    //add to arrayList
                    if(userType=="admin"){
                        bookArrayList.add(model);
                    }else{
                        if(model.getPublisherId().equals(firebaseAuth.getCurrentUser().getUid())){
                            bookArrayList.add(model);
                        }
                    }
                }

                //setup adapter
                adapterManagementBook = new AdapterManagementBook(ManagementBookActivity.this,bookArrayList);

                //set adapte to recicler view
                recyclerView.setAdapter(adapterManagementBook);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Metodo para cargar tipo de usuario
    private void loadUserType(){

        DatabaseReference refBook = FirebaseDatabase.getInstance().getReference("Users");
        refBook.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get book info
                userType = "" + snapshot.child("userType").getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}