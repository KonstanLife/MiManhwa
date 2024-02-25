package com.example.mimanhwa.Activities.LoginActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mimanhwa.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {


    //view items
    private EditText mailEditText;
    private AppCompatButton sendButton;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress diag
    private ProgressDialog progressDialog;

    private String email = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //init view items
        mailEditText = findViewById(R.id.forgot_password_email_et);
        sendButton = findViewById(R.id.forgot_password_btn);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // init/ setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Porfavor esperen");
        progressDialog.setCanceledOnTouchOutside(false);

        //click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    //Metodo para validar datos introducidos por los campos
    private void validateData(){
        //get data
        email = mailEditText.getText().toString().trim();
        //validate data
        if(email.isEmpty()){
            Toast.makeText(this, "Introdusca un correo", Toast.LENGTH_SHORT).show();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Patron de correo invalido", Toast.LENGTH_SHORT).show();
        }else{
            recoverPassword();
        }

    }

    //Metodo para enviar a correo instrucciones con recuperacion de contraseña
    private void recoverPassword(){
        //show progress
        progressDialog.setMessage("Enviando instrucciones de recuperación  al correo " + email);
        progressDialog.show();
        //begin sending recovery

        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //sent 
                progressDialog.dismiss();
                Toast.makeText(ForgotPasswordActivity.this, "Instrucciones de restablecer contraseña se enviaron a correo" + email, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ForgotPasswordActivity.this, "Se fallo envio de instrucciones" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

}