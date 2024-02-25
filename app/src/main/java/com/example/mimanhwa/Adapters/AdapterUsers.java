package com.example.mimanhwa.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mimanhwa.Filters.FilterUser;
import com.example.mimanhwa.Models.ModelUser;
import com.example.mimanhwa.R;
import com.example.mimanhwa.databinding.RowManagementUserBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.HolderUsers> implements Filterable {

    private static final String TAG ="USER_MANAGEMENT_TAG";

    //context
    private Context context;

    //View binding row_user_management
    private RowManagementUserBinding binding;

    //arraylist to hold list of data

    public ArrayList<ModelUser> usersArrayList,filterList;

   private FirebaseAuth firebaseAuth;

    private FilterUser filter;

    //progress dialog
    private ProgressDialog progressDialog;

    public AdapterUsers(Context context, ArrayList<ModelUser> usersArrayList) {
        this.context = context;
        this.usersArrayList = usersArrayList;
        this.filterList = usersArrayList;

        firebaseAuth = FirebaseAuth.getInstance();

        //Inicializar progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Porfavor esperen");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public AdapterUsers.HolderUsers onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowManagementUserBinding.inflate(LayoutInflater.from(context),parent,false);

        return new AdapterUsers.HolderUsers(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUsers.HolderUsers holder, int position) {

        //get data , set data , handle clicks

        //get data
        ModelUser model = usersArrayList.get(position);
        String name = model.getName();
        String email = model.getEmail();
        String userType = model.getUserType();
        String state = model.getState();
        String userId = model.getUserId();
        String url = model.getProfileImage();
        Long timestamp = model.getTimestamp();

        //set data

        holder.userNameTV.setText(name);
        holder.userMailTV.setText(email);
        holder.userTypeTV.setText(userType);
        holder.userStateTV.setText("Estado: " + state);

        // Cargar la imagen usando Glide
        Glide.with(context)
                .load(url)  // El URL de la imagen
                .placeholder(R.drawable.ic_person)
                .into(holder.photoUserImgView);



        //Handle click, show dialog wich options
        holder.userMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model,holder);
            }
        });

    }

    private void moreOptionsDialog(ModelUser model, AdapterUsers.HolderUsers holder) {

        //options to show in dialog
        String[] options = {"Bloquear","Desbloquear"};

        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eligir opción").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog options click
                if(which == 0){

                    //BLock
                    managementUserState("blocked",model.getUserId());

                }else if(which == 1){
                    //Unblock cliked
                    managementUserState("unblocked",model.getUserId());
                }

            }
        }).show();
    }

    private void managementUserState(String state,String userId){


        progressDialog.setMessage("Bloqueando perfil de usuario");
        progressDialog.show();

        //setup data to update
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("state",""+state);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(userId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();

                if(state.equals("blocked")){
                    Toast.makeText(context, "Usuario bloqueado", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Usuario desbloqueado", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();

                if(state.equals("blocked")){
                    Toast.makeText(context, "Error al bloquear usuario " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Error al bloquear usuario " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size(); //return number of records | list size
    }

    @Override
    public Filter getFilter() {

        if(filter ==null){

             filter = new FilterUser(filterList,this);
        }

        return filter;
    }

     //Viewb Holder class for row_book_managment

    class HolderUsers extends RecyclerView.ViewHolder{

        //Ui Views row_user_management
        ImageView photoUserImgView;
        TextView userNameTV,userMailTV,userStateTV,userTypeTV;
        ImageButton userMoreBtn;

        public HolderUsers(@NonNull View itemView) {
            super(itemView);

            photoUserImgView = binding.photoUserImg;
            userNameTV = binding.userNameTv;
            userMailTV = binding.userMailTv;
            userStateTV = binding.userStateTv;
            userTypeTV = binding.userTypeTv;

            userMoreBtn = binding.userMoreBtn;
        }
    }

}
