package com.example.mimanhwa.Activities.LoginActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mimanhwa.Activities.MainActivity;
import com.example.mimanhwa.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    //View items
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView resetPasswordButton;
    private TextView registerButton;
    private AppCompatButton loginButton;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //Campos para guardar temporalmente datos de usuario
    private String email = "",password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Init view items
        emailEditText = findViewById(R.id.log_email_et);
        passwordEditText = findViewById(R.id.log_password_et);
        resetPasswordButton = findViewById(R.id.log_help_tv);
        registerButton = findViewById(R.id.log_register_btn);
        loginButton = findViewById(R.id.log_login_btn);


        //inicializacion de firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();


        //Setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Porfavor esperen.");
        progressDialog.setCanceledOnTouchOutside(false);


        //click listener resetPassword
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
            }
        });

        //click listener register
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        //click listener login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    //Metodo para validar informacion introducida por los campos
    private void validateData() {
        //before loggin, lets do some data validation

        //get data
        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            Toast.makeText(this, "¡Patron de correo invalido...!", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(password)){

            Toast.makeText(this, "¡Introduce la contraseña...!", Toast.LENGTH_SHORT).show();
        }else {
            logginUser();
        }
    }

    //Metodo para iniciar sesion
    private void logginUser() {
        //show progress
        progressDialog.setMessage("Iniciando sesion");
        progressDialog.show();
        //login user

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //login succes
                checkUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //login failed
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Metodo para comprobar tipo de usuario y si este usuario tiene cuenta verificada
    private void checkUser() {

        progressDialog.setMessage("Comprobando a usuario");
        //check if user is user or admin from database
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                //get user type
                String userType =""+ snapshot.child("userType").getValue();

                if(firebaseUser.isEmailVerified()) {
                    //check userType
                    if (userType.equals("user")) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();

                    } else if (userType.equals("admin")) {

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                }else{
                    Toast.makeText(LoginActivity.this, "Parfavor, verifique su correo para poder inciar sesion.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}