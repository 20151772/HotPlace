package com.kookminuniv.team17.hotplace;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

public class PhotoViewHolder extends RecyclerView.ViewHolder {
    private ImageView imageView;

    ImgData data;

    public PhotoViewHolder(View itemView){
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);
    }
    public void onBind(ImgData data){
        this.data = data;

        imageView.setImageResource(data.getImg());
    }
}