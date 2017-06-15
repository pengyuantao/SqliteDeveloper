package com.peng.sqlitedeveloper.sqlitelookup.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.peng.sqlitedeveloper.R;
import com.peng.sqlitedeveloper.sqlitelookup.utils.AppUtils;

public class SelectorDialog extends Dialog{

	private ListView mLvSelect;
	private OnItemSelectedListener mSelectListener;
	
	public SelectorDialog(Activity activity) {
		super(activity, R.style.FullScreenDialog);
		setContentView(R.layout.dlg_select);
		mLvSelect = (ListView)findViewById(R.id.list_choose);
		
		setCancelable(true);
		setCanceledOnTouchOutside(true);
		
		WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.x = 0;
        lp.y = 0;
        int[] srceen = AppUtils.getScreenSize(activity);
        lp.width = (int) (srceen[0] * 0.8); // 设置宽度
        getWindow().setAttributes(lp);
	}

	public void setSelectItems(String[] items, OnItemSelectedListener l){
		this.mSelectListener = l;
		SelectAdapter adapter = new SelectAdapter(getContext(), items);
		mLvSelect.setAdapter(adapter);
	}
	
	private class SelectAdapter extends BaseAdapter{

		String[] data;
		LayoutInflater mInflater;
		
		SelectAdapter(Context context,String[] data){
			this.data = data;
			this.mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return data == null ? 0 : data.length;
		}

		@Override
		public Object getItem(int position) {
			return data[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			SelectItemViewHolder viewHolder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.listitem_dlg_select, parent,false);
				viewHolder = new SelectItemViewHolder();	
				viewHolder.tvTitle = (TextView)convertView.findViewById(R.id.tv_title);
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (SelectItemViewHolder) convertView.getTag();
			}
			viewHolder.tvTitle.setText(data[position]);
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
					if(mSelectListener != null)
						mSelectListener.onSelected(position);
				}
			});
			return convertView;
		}
	}
	
	class SelectItemViewHolder{
		TextView tvTitle;
	}
	
	public interface OnItemSelectedListener{
		void onSelected(int position);
	}
}
