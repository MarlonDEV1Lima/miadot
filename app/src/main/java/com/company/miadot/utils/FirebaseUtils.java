package com.company.miadot.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;

public class FirebaseUtils {

    public interface NicknameCallback {
        void onNicknameReceived(String nickname);
        void onError(String error);
    }

    public static void getCurrentUserNickname(final NicknameCallback callback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) {
            callback.onError("Usuário não autenticado");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.child("nickname").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nickname = snapshot.getValue(String.class);
                if (nickname != null && !nickname.isEmpty()) {
                    callback.onNicknameReceived(nickname);
                } else {
                    callback.onNicknameReceived("Usuário");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Erro ao buscar nickname: " + error.getMessage());
                Log.e("FirebaseUtils", "Erro ao buscar nickname", error.toException());
            }
        });
    }
}
