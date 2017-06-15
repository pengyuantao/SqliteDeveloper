package com.peng.sqlitedeveloper.sqlitelookup.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peng.sqlitedeveloper.R;

import java.util.List;

public abstract class SimpleListAdapter<T> extends RecyclerView.Adapter<SimpleListAdapter.SimpleItemViewHodler>{

	private List<T> mData;
	private LayoutInflater mInflater;
	
	public SimpleListAdapter(Context context,List<T> data){
		this.mData = data;
		this.mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getItemCount() {
		return mData == null ? 0 : mData.size();
	}
	
	@Override
	public SimpleItemViewHodler onCreateViewHolder(ViewGroup parent, int type) {
		return new SimpleItemViewHodler(mInflater.inflate(R.layout.listitem_simple, parent, false));
	}

	public static class SimpleItemViewHodler extends RecyclerView.ViewHolder{
		public ImageView ivIcon;
		public TextView tvText;
		
		public SimpleItemViewHodler(View v) {
			super(v);
			ivIcon = (ImageView) v.findViewById(R.id.iv_icon);
			tvText = (TextView) v.findViewById(R.id.tv_text);
		}
	}
}
