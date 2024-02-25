package com.example.mimanhwa.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mimanhwa.Extensions.MyApplication;
import com.example.mimanhwa.Models.ModelChapter;
import com.example.mimanhwa.databinding.RowManagementChapterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterManagementChapter extends RecyclerView.Adapter<AdapterManagementChapter.HolderChapterManagment> {

    private static final String TAG ="CHAPTER_MANAGEMENT_TAG";

    //context
    private Context context;

    //View binding row_book_managment
    private RowManagementChapterBinding binding;

    //arraylist to hold list of data
    public ArrayList<ModelChapter> chapterArrayList;


    //progress dialog
    private ProgressDialog progressDialog;

    public AdapterManagementChapter(Context context, ArrayList<ModelChapter> chapterArrayList) {
        this.context = context;
        this.chapterArrayList = chapterArrayList;


        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Porfavor esperen");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderChapterManagment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowManagementChapterBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderChapterManagment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterManagementChapter.HolderChapterManagment holder, int position) {

        //get data , set data , handle clicks

        //get data
        ModelChapter model = chapterArrayList.get(position);
        String title = model.getTitle();

        //set data
        holder.chapterTextView.setText(title);




        holder.chapterDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete").setMessage("¿Esta seguro que quiere borrar esta categoria?").setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteChapter(model,holder);
                        Toast.makeText(context, "Borrando...", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                }).show();

            }
        });

    }



    private void deleteChapter(ModelChapter model, AdapterManagementChapter.HolderChapterManagment holder) {

        String chapterId = model.getId();
        String chapterUrl = model.getUrl();
        String chapterTitle = model.getTitle();

        MyApplication.removeFromAllChapterReaded(context, chapterId);

        Log.d(TAG,"borando lectura: Borando...");
        progressDialog.setMessage("Borando " +chapterTitle+"...");
        progressDialog.show();

        Log.d(TAG,"borando lectura: Borando desde storage...");

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(chapterUrl);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Log.d(TAG,"onSucces: Borado desde storage");
                Log.d(TAG,"onSucces: Borando desde DB");

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chapters");

                reference.child(chapterId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG,"onSucces: Borrado desde data base");
                        progressDialog.dismiss();
                        Toast.makeText(context, "Lectura borrada...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"onFailure: Fallo al borrar desde db" + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d(TAG,"onFailure: Borrado desde storage fallido" + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return chapterArrayList.size(); //return number of records | list size
    }



//Viewb Holder class for row_book_managment

    class HolderChapterManagment extends RecyclerView.ViewHolder{

        //Ui Views row_book_managment

        TextView chapterTextView;
        ImageButton chapterDeleteBtn;

        public HolderChapterManagment(@NonNull View itemView) {
            super(itemView);

            chapterTextView = binding.chapterTitleTv;
            chapterDeleteBtn = binding.chapterDeleteBtn;

        }
    }
}