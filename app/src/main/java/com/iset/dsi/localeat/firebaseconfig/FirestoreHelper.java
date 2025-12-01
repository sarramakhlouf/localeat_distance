package com.iset.dsi.localeat.firebaseconfig;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.iset.dsi.localeat.models.Place;

import java.util.*;

public class FirestoreHelper {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String FAVORITES = "favorites";

    public interface Callback {
        void onComplete(boolean success);
    }

    public interface GetFavoritesCallback {
        void onResult(List<Place> places);
    }

    public static void toggleFavorite(Place place, Callback callback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) {
            callback.onComplete(false);
            return;
        }
        CollectionReference favRef = db.collection("users").document(uid).collection(FAVORITES);
        if (place.getId() == null || place.getId().isEmpty()) {
            // create a new favorite (auto id)
            Map<String, Object> data = new HashMap<>();
            data.put("name", place.getName());
            data.put("address", place.getAddress());
            data.put("lat", place.getLat());
            data.put("lng", place.getLng());
            favRef.add(data).addOnSuccessListener(docRef -> {
                place.setId(docRef.getId());
                callback.onComplete(true);
            }).addOnFailureListener(e -> callback.onComplete(false));
        } else {
            // if exists -> delete (toggle). This simple approach always adds; you can implement check first.
            favRef.document(place.getId()).delete().addOnSuccessListener(aVoid -> callback.onComplete(false))
                    .addOnFailureListener(e -> callback.onComplete(false));
        }
    }

    public static void getFavorites(GetFavoritesCallback callback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) { callback.onResult(new ArrayList<>()); return; }
        db.collection("users").document(uid).collection(FAVORITES).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Place> list = new ArrayList<>();
                    for (DocumentSnapshot ds : queryDocumentSnapshots) {
                        Place p = ds.toObject(Place.class);
                        if (p != null) {
                            p.setId(ds.getId());
                            list.add(p);
                        }
                    }
                    callback.onResult(list);
                }).addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }
}
