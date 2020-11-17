package com.kookminuniv.team17.hotplace;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {

    private ArrayList<ListData> listViewData = new ArrayList<ListData>();

    public ListAdapter(){

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


        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1) ;
        TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1) ;
        TextView descTextView = (TextView) convertView.findViewById(R.id.textView2) ;


        ListData listData = listViewData.get(position);

        iconImageView.setImageDrawable(listData.getIcon());
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

    public void addItem(Drawable icon, String title, String desc){
        ListData item = new ListData();

        item.setIcon(icon);
        item.setTitle(title);
        item.setDesc(desc);

        listViewData.add(item);
    }
}
