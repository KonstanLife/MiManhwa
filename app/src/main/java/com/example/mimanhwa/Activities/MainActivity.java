package com.example.mimanhwa.Activities;

import static com.example.mimanhwa.Extensions.MyApplication.getUserState;
import static com.example.mimanhwa.Extensions.MyApplication.getUserType;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.mimanhwa.Activities.EditActivity.EditProfileActivity;
import com.example.mimanhwa.Activities.GeneralActivity.GenreActivity;
import com.example.mimanhwa.Activities.GeneralActivity.MarkFavoritesActivity;
import com.example.mimanhwa.Activities.LoginActivity.IntroActivity;
import com.example.mimanhwa.Activities.LoginActivity.LoginActivity;
import com.example.mimanhwa.Activities.ManagementActivity.ManagementBookActivity;
import com.example.mimanhwa.Activities.ManagementActivity.ManagementGenreActivity;
import com.example.mimanhwa.Activities.ManagementActivity.ManagementUserActivity;
import com.example.mimanhwa.Adapters.AdapterBook;
import com.example.mimanhwa.Adapters.AdapterManagementBook;
import com.example.mimanhwa.Adapters.AdapterPopularBook;
import com.example.mimanhwa.Extensions.MyApplication;
import com.example.mimanhwa.Models.ModelBook;
import com.example.mimanhwa.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    //Elementos de drawer menu
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;

    //Drawer menu header view items
    private View headerView;
    private ShapeableImageView profileImageView;
    private ImageButton notificationImgBtn;
    private  TextView nameTextView;
    private  TextView emailTextView;

    //Elementos de cambio de tema
    private boolean nightMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //Autentificacion firebase
    private FirebaseAuth firebaseAuth;

    //Constantes
    private static final String TAG = "PROFILE_TAG";

    //Recicler views
    private RecyclerView popularBookRecyclerView;
    private RecyclerView allBooksRecyclerView;

    //arrayList to store popular books
    private ArrayList<ModelBook> popularBookArrayList;
    //adapter
    private AdapterPopularBook adapterPopularBook;

    //arrayList to store all  books
    private ArrayList<ModelBook> allBooksArrayList;
    //adapter
    private AdapterBook adapterBook;

    //user state
    private String userStateVariable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializar recycler views
        popularBookRecyclerView = findViewById(R.id.popular_home_page_rv);
        allBooksRecyclerView = findViewById(R.id.new_home_page_rv);
        //Inicialización  de tool bar y sus elementos
        drawerLayout = findViewById(R.id.drawerlayout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        // Acceder a la cabecera del menú lateral
        headerView = navigationView.getHeaderView(0);
        profileImageView = headerView.findViewById(R.id.hd_profile_img);
        notificationImgBtn = headerView.findViewById(R.id.notification_img_btn);
        nameTextView = headerView.findViewById(R.id.hd_name_tv);
        emailTextView = headerView.findViewById(R.id.hd_email_tv);

        //Inicializacion firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //Cargar informacion de usuario
        loadUserInfo();

        //Caragar libros populares
        loadPopularBooks();
        //Cargar todos libros
        loadBooks();

        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigration_open,R.string.navigration_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //callback para datos  de estado de  usuario
        getUserState(new MyApplication.UserStateCallback() {
            @Override
            public void onUserStateReceived(String userState) {
                // Manejar el userState aquí
                userStateVariable = userState;
            }
        });

        //callback para datos de tipo de usuario
        getUserType(new MyApplication.UserTypeCallback() {
            @Override
            public void onUserTypeReceived(String userType) {
                if(userStateVariable.equals("unblocked")){
                    // Manejar el userType aquí
                    if (userType.equals("admin")) {
                        navigationView.getMenu().clear(); // Limpiar el menú existente
                        navigationView.inflateMenu(R.menu.side_admin_menu); // Inflar el menú de administrador
                    } else {
                        navigationView.getMenu().clear(); // Limpiar el menú existente
                        navigationView.inflateMenu(R.menu.side_user_menu); // Inflar el menú de usuario regular
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Este usuario esta bloqueado", Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                    checkUser();
                }
            }
        });


        //Click listener de items de drawer menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.main_page:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.user_profile:
                        startActivity(new Intent(MainActivity.this, MarkFavoritesActivity.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.user_genre:
                        startActivity(new Intent(MainActivity.this, GenreActivity.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.add_book:
                        startActivity(new Intent(MainActivity.this, ManagementBookActivity.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.userManagment:
                        startActivity(new Intent(MainActivity.this, ManagementUserActivity.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.categoryManagment:
                        startActivity(new Intent(MainActivity.this, ManagementGenreActivity.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.settings_pr:
                        startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.closeSession:
                        firebaseAuth.signOut();
                        checkUser();
                        break;

                }
                return true;
            }
        });


        //Control del tema nocturno/diurno
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode",false);
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //Image click listener
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
            }
        });

        notificationImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Actialmente la opción de notificaciones se encuentra en mantenimiento", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Click listener de main menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(toggle.onOptionsItemSelected(item)){
            return true;
        }

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
                    adapterBook.getFilter().filter(newText);
                }catch (Exception e){

                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void checkUser() {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null){

            startActivity(new Intent(MainActivity.this, IntroActivity.class));
            finish();
        }
    }

    //Metodo para cargar informacion de usuario
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
                emailTextView.setText(email);
                nameTextView.setText(name);

                if(!(profileImage.equals(""))){
                    Picasso.get().load(profileImage).into(profileImageView, new Callback() {
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

                startActivity(new Intent(MainActivity.this,EditProfileActivity.class));
            }
        });

    }


    //Metodo para cargar libros  que tienen mas visitas
    private void loadPopularBooks() {
        // Inicializar el ArrayList
        popularBookArrayList = new ArrayList<>();

        // Obtener todos los libros de Firebase > Books
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Limpiar el ArrayList antes de agregar datos
                popularBookArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Obtener datos
                    ModelBook model = ds.getValue(ModelBook.class);
                    // Agregar al ArrayList
                    popularBookArrayList.add(model);
                }

                // Ordenar la lista en función del número de visitas (suponiendo que hay un método getVisits en tu ModelBook)
                Collections.sort(popularBookArrayList, new Comparator<ModelBook>() {
                    @Override
                    public int compare(ModelBook book1, ModelBook book2) {
                        return Integer.compare(book2.getViewsCount(), book1.getViewsCount());
                    }
                });

                // Limitar la cantidad de elementos a 5 o al tamaño actual si es menor a 5
                int limit = Math.min(popularBookArrayList.size(), 5);

                // Crear una nueva lista limitada
                List<ModelBook> limitedList = new ArrayList<>(popularBookArrayList.subList(0, limit));

                // Limpiar el ArrayList original
                popularBookArrayList.clear();

                // Agregar los elementos limitados al ArrayList original
                popularBookArrayList.addAll(limitedList);

                // Configurar el LayoutManager para la orientación horizontal
                LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);

                // Establecer el LayoutManager en el RecyclerView
                popularBookRecyclerView.setLayoutManager(layoutManager);

                // Configurar el adaptador con el ArrayList original (ahora limitado)
                adapterPopularBook = new AdapterPopularBook(MainActivity.this, popularBookArrayList);

                // Establecer el adaptador en el RecyclerView
                popularBookRecyclerView.setAdapter(adapterPopularBook);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar la cancelación si es necesario
            }
        });
    }



    //Metodo para cargar todos los libros
    private void loadBooks() {
        // Inicializar el ArrayList
        allBooksArrayList = new ArrayList<>();

        // Obtener todos los libros de Firebase > Books
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Limpiar el ArrayList antes de agregar datos
                allBooksArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Obtener datos
                    ModelBook model = ds.getValue(ModelBook.class);
                    // Agregar al ArrayList
                    allBooksArrayList.add(model);
                }

                // Configurar el adaptador
                adapterBook = new AdapterBook(MainActivity.this, allBooksArrayList);

                // Establecer el adaptador en el RecyclerView
                allBooksRecyclerView.setAdapter(adapterBook);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar la cancelación si es necesario
            }
        });
    }


}