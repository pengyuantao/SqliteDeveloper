package com.peng.sqlitedeveloper.sqlitelookup.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peng.sqlitedeveloper.R;
import com.peng.sqlitedeveloper.tablefixheaders.adapters.BaseTableAdapter;

import java.util.List;

public abstract class SimpleTableAdapter<H,C> extends BaseTableAdapter{

	private static final int VIEW_TYPE_HEADER = 0;
	private static final int VIEW_TYPE_CELL = 1;
	
	private final List<C> cellData;
	private final List<H> headData;
	private final int cellHeight;
	private final Context context;
	private final Resources res;
	private final LayoutInflater inflater;
	
	public SimpleTableAdapter(Context context, List<H> headData, List<C> cellData){
		this.headData = headData;
		this.cellData = cellData;
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		res = context.getResources();
		this.cellHeight = res.getDimensionPixelSize(R.dimen.table_design_cell_height);
	}
	
	public Context getContext(){
		return context;
	}

	@Override
	public int getColumnCount() {
		return headData == null ? -1 : headData.size() - 1;
	}
	
	@Override
	public int getRowCount() {
		return cellData == null ? 0 : cellData.size();
	}

	@Override
	public View getView(int row, int column, View convertView, ViewGroup parent) {
		CellViewHolder viewHolder;
		if(convertView == null){
			final int viewType = getItemViewType(row, column);
			switch(viewType){
			case VIEW_TYPE_HEADER:
				convertView = inflater.inflate(R.layout.table_header_cell, parent,false);
				break;
			case VIEW_TYPE_CELL:
				convertView = inflater.inflate(R.layout.table_cell, parent,false);
				break;
			}
			
			viewHolder = new CellViewHolder();
			viewHolder.tvText = (TextView) convertView.findViewById(R.id.tv_text);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (CellViewHolder) convertView.getTag();
		}
		if(row >= 0){
			if(row % 2 == 0){
				convertView.setBackgroundColor(res.getColor(R.color.table_even_cell_bg_color));
			}else{
				convertView.setBackgroundColor(res.getColor(R.color.table_odd_cell_bg_color));
			}
		}
		
		if(row == -1){
			bindHeaderText(viewHolder.tvText, column,headData.get(column + 1));
		}else{
			bindCellText(viewHolder.tvText, row, column,cellData.get(row));
		}
		
		return convertView;
	}

	public abstract void bindHeaderText(TextView tvHeader,int column,H hRecord);
	
	public abstract void bindCellText(TextView tvCell, int row, int column,C cRecord);

	@Override
	public int getHeight(int row) {
		return cellHeight;
	}

	@Override
	public int getItemViewType(int row, int column) {
		if (row < 0) {
			return VIEW_TYPE_HEADER;
		} else {
			return VIEW_TYPE_CELL;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	public static class CellViewHolder {
		TextView tvText;
	}
}
