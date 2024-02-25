package com.example.mimanhwa.Activities.HelpActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.mimanhwa.R;

public class ContactActivity extends AppCompatActivity {

    //Toolbar
    private Toolbar toolbar;
    private ImageButton backBtn;

    //Items view

    private EditText name;
    private EditText message;

    private AppCompatButton sendMessageBtn;


    //Elementos de cambio de tema
    private boolean nightMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String direction = "constantinopladwar@gmail.com";
    private String asunt = "Centro de ayuda de aplicación MiManhwa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        //Inicialización  de tool bar y sus elementos
        toolbar = (Toolbar)findViewById(R.id.toolbar_back);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        backBtn = findViewById(R.id.toolbar_back_imbtn);

        //Init item views
        name = findViewById(R.id.contact_name_et);
        message = findViewById(R.id.contact_message_et);
        sendMessageBtn = findViewById(R.id.send_message_btn);


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

        // click listener send message button
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()) {
                    sendMessage();
                }
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


    //Metodo para validar la iformacion introducida por los campos
    public boolean validate(){


        boolean retrono = true;

        String nameText = name.getText().toString();
        String messageText = message.getText().toString();

        if(nameText.isEmpty()){
            name.setError("Este campo no puede quedar vacio.");
            retrono = false;
        }
        if(messageText.isEmpty()){
            message.setError("Este campo no puede quedar vacio.");
            retrono = false;
        }

        return retrono;
    }

    //Metodo para enviar mensaje
    public void sendMessage(){

        String nameText = name.getText().toString();
        String messageText = "Nombre: " + nameText  + "Consulta: \n" + message.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.putExtra(Intent.EXTRA_EMAIL,new String[]{direction});
        intent.putExtra(Intent.EXTRA_SUBJECT,asunt);
        intent.putExtra(Intent.EXTRA_TEXT,messageText);

        intent.setType("message/rfc822");

        startActivity(Intent.createChooser(intent,"Elige un cliente de Correo"));

    }
}