package com.example.mimanhwa.Extensions;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MyApplication extends Application {
    private static MyApplication singleton;

    public MyApplication getInstance(){
        return singleton;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }


    //Metodo para añadir lectura a favoritos
    public static  void addToFavorite(Context context, String bookId){

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        long timestamp = System.currentTimeMillis();

        //setup data to add in firebase db of current user favorite book
        HashMap<String,Object> hashMap = new HashMap<>();

        hashMap.put("bookId",""+bookId);
        hashMap.put("timestamp",""+timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Toast.makeText(context, "Añadido a favoritos", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(context, "Error al añadir a favoritos"+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //Metodo para añadir capitulo a leidos
    public static  void addToReadedChapter(Context context, String chapterId){

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        long timestamp = System.currentTimeMillis();

        //setup data to add in firebase db of current user favorite book
        HashMap<String,Object> hashMap = new HashMap<>();

        hashMap.put("chapterId",""+chapterId);
        hashMap.put("timestamp",""+timestamp);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(firebaseAuth.getUid()).child("ReadedChapters").child(chapterId).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Toast.makeText(context, "Marcado como leido", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(context, "Error al marcar como favorito"+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Metodo para quitar lectura de favoritos
    public static void removeFromFavorite(Context context,String bookId){

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Toast.makeText(context, "Se elemino de  favoritos", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(context, "Error al eleminar de favoritos"+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Metodo para quitar capitulo de leidos
    public static void removeFromReaded(Context context,String chapterId){

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(firebaseAuth.getUid()).child("ReadedChapters").child(chapterId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Toast.makeText(context, "Se marco como no leido", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(context, "Error al marcar como no leido"+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    //Metodo para quitar lectura de favoritos a todos usuarios
    public static void removeFromAllFavorites(Context context, String bookId) {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("Users");

        favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null) {
                        DatabaseReference userFavoritesRef = favoritesRef.child(userId).child("Favorites").child(bookId);
                        userFavoritesRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(context, "Eliminado de favoritos de todos los usuarios", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Error al eliminar de favoritos de usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Error al acceder a la base de datos: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Metodo para quitar capitulo de leido de  todos usuarios
    public static void removeFromAllChapterReaded(Context context, String chapterId) {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("Users");

        favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null) {
                        DatabaseReference userFavoritesRef = favoritesRef.child(userId).child("ReadedChapters").child(chapterId);
                        userFavoritesRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(context, "Eliminado de leido de todos los usuarios", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Error al eliminar de leido de usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Error al acceder a la base de datos: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public interface UserTypeCallback {
        void onUserTypeReceived(String userType);
    }

    public static void getUserType(final UserTypeCallback callback){
        //Firebase auth
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userType =""+ snapshot.child("userType").getValue();
                callback.onUserTypeReceived(userType);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar el error aquí si es necesario
            }
        });
    }


    public interface UserStateCallback {
        void onUserStateReceived(String userState);
    }

    public static void getUserState(final UserStateCallback callback){
        //Firebase auth
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userState =""+ snapshot.child("state").getValue();
                callback.onUserStateReceived(userState);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar el error aquí si es necesario
            }
        });
    }

}