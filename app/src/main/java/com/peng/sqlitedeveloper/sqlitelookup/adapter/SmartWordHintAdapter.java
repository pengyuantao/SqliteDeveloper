package com.peng.sqlitedeveloper.sqlitelookup.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peng.sqlitedeveloper.R;
import com.peng.sqlitedeveloper.sqlitelookup.model.SmartWord;

import java.util.Collections;
import java.util.List;

/**
 * Created by pyt on 2017/6/1.
 */

public class SmartWordHintAdapter extends RecyclerView.Adapter<SmartWordHintAdapter.SmartWordHintViewHolder> {

    private List<SmartWord> dataList;

    private OnItemClickListener onItemClickListener;

    @Override
    public SmartWordHintViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_smart_word, parent, false);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    int position = (int) v.getTag();
                    onItemClickListener.onItemClick(position,v,dataList.get(position));
                }
            }
        });

        return new SmartWordHintViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(SmartWordHintViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.bind(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    public void refresh(List<SmartWord> dataList){
        if (dataList == null) {
            return;
        }
        Collections.sort(dataList);
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public static class SmartWordHintViewHolder extends RecyclerView.ViewHolder{

        private TextView tvWord;

        public SmartWordHintViewHolder(View itemView) {
            super(itemView);
            tvWord = (TextView) itemView.findViewById(R.id.tv_smart_word);
        }

        public void bind(SmartWord smartWord) {
            tvWord.setText(smartWord.word);
    }
    }

    public  interface OnItemClickListener{
        void onItemClick(int position, View view, SmartWord smartWord);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

}
