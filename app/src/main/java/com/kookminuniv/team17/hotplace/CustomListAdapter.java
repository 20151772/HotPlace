package com.kookminuniv.team17.hotplace;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.ArrayList;

public class CustomListAdapter extends BaseAdapter {

    private ArrayList<ListData> listViewData = new ArrayList<ListData>();

    public CustomListAdapter(){

    }

    @Override
    public int getCount() {
        return listViewData.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_inlist, parent, false);
        }

        final ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1) ;
        TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1) ;
        TextView descTextView = (TextView) convertView.findViewById(R.id.textView2) ;

        ListData listData = listViewData.get(position);

        Log.d("asdf", "1");

        StorageReference imageSRef = FirebaseStorage.getInstance().getReference().child(listData.getImageName());
        imageSRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Log.d("asdf", "2");
                    Glide.with(context)
                            .load(task.getResult())
                            .into(imageView);
                }
            }
        });

        titleTextView.setText(listData.getTitle());
        descTextView.setText(listData.getDesc());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return listViewData.get(position);
    }

    public void addItem(String imageName, String title, String desc){
        ListData item = new ListData();

        item.setImageName(imageName);
        item.setTitle(title);
        item.setDesc(desc);

        listViewData.add(item);
    }
}
