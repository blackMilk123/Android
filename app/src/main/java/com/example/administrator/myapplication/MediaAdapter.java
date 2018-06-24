package com.example.administrator.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YJ on 2018/6/19.
 */

public class MediaAdapter extends BaseAdapter {
    private Context context;
    private List<AtyEditNote.MediaListCellData> list = new ArrayList<AtyEditNote.MediaListCellData>();

    public MediaAdapter(Context context) {
        this.context = context;
    }

    public void add(AtyEditNote.MediaListCellData data) {
        list.add(data);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public AtyEditNote.MediaListCellData getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.media_list, null);
        }

        AtyEditNote.MediaListCellData data = getItem(position);

        ImageView ivIcon = (ImageView) convertView
                .findViewById(R.id.ivIcon);
        TextView tvPath = (TextView) convertView.findViewById(R.id.tvPath);

        ivIcon.setImageResource(data.iconId);
        tvPath.setText(data.path);
        return convertView;
    }

}
