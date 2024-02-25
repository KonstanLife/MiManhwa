package com.example.mimanhwa.Activities.LoginActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mimanhwa.Activities.EditActivity.EditProfileActivity;
import com.example.mimanhwa.Activities.HelpActivity.TermsActivity;
import com.example.mimanhwa.Activities.MainActivity;
import com.example.mimanhwa.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //View items

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordCopyEditText;
    private CheckBox checkBox;
    private AppCompatButton registerButton;
    private TextView termsButton;

    //Autentificador de firebase
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //campos para datos de usuario, registro temporal
    private String name = "",email = "",password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Init view items
        nameEditText = findViewById(R.id.reg_name_et);
        emailEditText = findViewById(R.id.reg_email_et);
        passwordEditText = findViewById(R.id.reg_password_et);
        passwordCopyEditText = findViewById(R.id.reg_confirm_password_et);
        checkBox = findViewById(R.id.checkbox);
        registerButton = findViewById(R.id.reg_login_btn);
        termsButton = findViewById(R.id.term_title_click);

        //Inicializar autentificador de firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //Setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Porfavor esperen.");
        progressDialog.setCanceledOnTouchOutside(false);





        //click listerner termsbutton
        termsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(RegisterActivity.this, TermsActivity.class));
            }
        });

        //click listener registerbutton
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }

    //Metodo para validar informacion introducida en los campos
    private void validateData() {


        //Get data
        name = nameEditText.getText().toString().trim();
        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();
        String cPassword = passwordCopyEditText.getText().toString().trim();

        //Validate data

        if(TextUtils.isEmpty(name)){

            Toast.makeText(this, "¡Introduce tu nombre...!", Toast.LENGTH_SHORT).show();
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            Toast.makeText(this, "¡Patron de correo invalido...!", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(password)){

            Toast.makeText(this, "¡Introduce la contraseña...!", Toast.LENGTH_SHORT).show();

        } else if (password.length() < 6) { // Validar que la contraseña tenga al menos 6 caracteres
            Toast.makeText(this, "¡La contraseña debe tener al menos 6 caracteres...!", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(cPassword)){
            Toast.makeText(this, "¡Confirma la contraseña...!", Toast.LENGTH_SHORT).show();
        }else if(!password.equals(cPassword)){

            Toast.makeText(this, "¡Las contraseñas no coinciden...!", Toast.LENGTH_SHORT).show();
        }else if (!checkBox.isChecked()){

            Toast.makeText(this, "¡Acepta los términos y condiciones...!", Toast.LENGTH_SHORT).show();
        }else{
            createUserAccount();
        }


    }

    //Metodo para crear cuenta de usuario
    private void createUserAccount() {

        //show progress dialog
        progressDialog.setMessage("Creando la cuenta...");
        progressDialog.show();

        //create user in firebare auth
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                updateUserInfo();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    //Metodo para guardar datos de usuario en base de datos
    private  void updateUserInfo(){
        progressDialog.setMessage("Guardando informacion del usuario");

        //timestamp
        long timestamp = System.currentTimeMillis();
        //get current user uid,since user is registred so we can get now
        String userId = firebaseAuth.getUid();

        //setup data to add in db
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("userId",userId);
        hashMap.put("email",email);
        hashMap.put("name",name);
        hashMap.put("profileImage","");
        hashMap.put("state","unblocked");
        hashMap.put("userType","user");// posibles valores es usuario y administrador
        hashMap.put("timestamp",timestamp);


        //set data to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(userId).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                //data added to db
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Cuenta creada con exito", Toast.LENGTH_SHORT).show();

                //Al ser creado usuario pasamos a pantalla principal
          //      startActivity(new Intent(RegisterActivity.this, MainActivity.class));
             //   finish();

                emailVerificationDialog();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //data failed adding to db
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Metodo para enviar mensaje de verificacion de cuenta
    private void sendEmailVerification(){
        //show progress
        progressDialog.setMessage("Enviando instrucciones de verificacion a su correo" + firebaseAuth.getCurrentUser().getEmail());
        progressDialog.show();

        firebaseAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Instrucciones enviadas, revisu su correo", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Error al enviar" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Metodo para avisar al usuario que se le envio a correo la verificacion
    private void emailVerificationDialog(){
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verificar correo").setMessage("Las instrucciones con verificación se enviaron a su correo" + firebaseAuth.getCurrentUser().getEmail());

        sendEmailVerification();
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();

    }
}