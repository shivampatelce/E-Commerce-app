package com.example.project_2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.MyViewHolder> {

    private List<String> imageUrlList;

    public ProductImageAdapter(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }

    @NonNull
    @Override
    public ProductImageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new MyViewHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductImageAdapter.MyViewHolder holder, int position) {
        String imageUrl =imageUrlList.get(position);

        Picasso.get()
                .load(imageUrl)
                .into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return imageUrlList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;

        public MyViewHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.product_image_layout, parent, false));
            productImage = itemView.findViewById(R.id.productImg);
        }

    }
}
