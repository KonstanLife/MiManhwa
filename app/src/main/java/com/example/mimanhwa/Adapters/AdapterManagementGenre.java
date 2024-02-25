package com.example.mimanhwa.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mimanhwa.Filters.FilterManagementGenre;
import com.example.mimanhwa.Models.ModelGenre;
import com.example.mimanhwa.databinding.RowManagementGenreBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterManagementGenre  extends RecyclerView.Adapter<AdapterManagementGenre.HolderManagementGenre> implements Filterable {

    private Context context;
    public ArrayList<ModelGenre> genreArrayList,filterList;

    //view binding
    private RowManagementGenreBinding binding;

    private FilterManagementGenre filter;

    private static final String TAG = "ADD_IMG_TAG";

    public AdapterManagementGenre(Context context, ArrayList<ModelGenre> genreArrayList){

        this.context = context;
        this.genreArrayList = genreArrayList;
        this.filterList = genreArrayList;
    }

    @NonNull
    @Override
    public HolderManagementGenre onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //bind row_category.xml
        binding = RowManagementGenreBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderManagementGenre(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderManagementGenre holder, int position) {

        //get data
        ModelGenre model = genreArrayList.get(position);
        String id = model.getId();
        String genre = model.getGenre();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();


        //set data
        holder.genreTitleTextView.setText(genre);

        //handle click delete category

        holder.genreDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete").setMessage("¿Esta seguro que quiere borrar esta categoria?").setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteCategory(model,holder);
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

    private void deleteCategory(ModelGenre model, HolderManagementGenre holder) {

        String id = model.getId();

        deleteImageFromStorage(model.getDirImg());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Genres");
        ref.child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Toast.makeText(context, "Se borro con exito", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(context, "" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void deleteImageFromStorage(String filePathAndName) {

        // Crear una referencia al archivo que deseas eliminar
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePathAndName);

        // Eliminar el archivo
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // El archivo se eliminó exitosamente
                Log.d(TAG, "onSuccess: Imagen eliminada del almacenamiento");
                // Puedes realizar otras acciones después de eliminar la imagen si es necesario
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Se produjo un error al intentar eliminar el archivo
                Log.e(TAG, "onFailure: Error al eliminar la imagen del almacenamiento " + e.getMessage());
                // Puedes manejar el error según tus necesidades
            }
        });
    }

    @Override
    public int getItemCount() {
        return genreArrayList.size();
    }

    @Override
    public Filter getFilter() {

        if(filter == null){

            filter = new FilterManagementGenre(filterList,this);
        }

        return filter;
    }



    class HolderManagementGenre extends RecyclerView.ViewHolder{

        //ui views of row_category
        TextView genreTitleTextView;
        ImageButton genreDeleteButton;

        public HolderManagementGenre(@NonNull View itemView){

            super(itemView);
            genreTitleTextView = binding.genreTitleTv;
            genreDeleteButton = binding.genreDeleteBtn;

        }

    }

}
