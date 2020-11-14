package com.kookminuniv.team17.hotplace;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {
    private ArrayList<ImgData> mItems;

    public PhotoAdapter(ArrayList<ImgData> data) {
        this.mItems = data;
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_viewpager, parent, false);

        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {
        if(holder instanceof PhotoViewHolder){
            PhotoViewHolder pvHolder = (PhotoViewHolder) holder;
            pvHolder.onBind(mItems.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}