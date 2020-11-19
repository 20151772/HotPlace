package com.kookminuniv.team17.hotplace;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PhotoViewHolder extends RecyclerView.ViewHolder {
    private ImageView imageView;

    ImgData data;

    public PhotoViewHolder(View itemView){
        super(itemView);
        imageView = itemView.findViewById(R.id.iv);
    }
    public void onBind(ImgData data, Context context){
        this.data = data;
        final Context context2 = context;

        if(data.getImg().equals("None")){
            imageView.setImageResource(R.drawable.android);
        }
        else{
            StorageReference imageSRef = FirebaseStorage.getInstance().getReference().child(data.getImg());
            imageSRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Glide.with(context2)
                                .load(task.getResult())
                                .into(imageView);
                    }
                }
            });
        }
    }
}