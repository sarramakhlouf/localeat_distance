package com.iset.dsi.localeat.adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iset.dsi.localeat.R;
import com.iset.dsi.localeat.firebaseconfig.FirestoreHelper;
import com.iset.dsi.localeat.models.Place;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceVH> {

    public interface OnPlaceClick { void onPlaceClicked(Place place); }
    private List<Place> places;
    private Context ctx;
    private OnPlaceClick listener;

    public PlaceAdapter(List<Place> places, Context ctx, OnPlaceClick listener) {
        this.places = places;
        this.ctx = ctx;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaceVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_place, parent, false);
        return new PlaceVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceVH holder, int position) {
        Place p = places.get(position);
        holder.tvName.setText(p.getName());
        holder.tvAddress.setText(p.getAddress());
        holder.ivFav.setOnClickListener(v -> {
            // toggle favorite
            FirestoreHelper.toggleFavorite(p, success -> {
                Toast.makeText(ctx, success ? "Saved" : "Removed", Toast.LENGTH_SHORT).show();
            });
        });
        holder.itemView.setOnClickListener(v -> listener.onPlaceClicked(p));
    }

    @Override
    public int getItemCount() { return places.size(); }

    static class PlaceVH extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress;
        ImageView ivFav;
        PlaceVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.item_name);
            tvAddress = itemView.findViewById(R.id.item_address);
            ivFav = itemView.findViewById(R.id.item_fav);
        }
    }
}
