package com.peng.sqlitedeveloper.sqlitelookup.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.peng.sqlitedeveloper.sqlite.ResultSet;
import com.peng.sqlitedeveloper.R;
import com.peng.sqlitedeveloper.sqlitelookup.utils.AppUtils;

public class RecordDetailActivity extends BaseActivity{

	public static final String EXTRA_RECORD = "record";
	
	private ListView mLvRecords;
	private ResultSet mResultSet;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_detail);
		enableBack();
		setMainTitle(R.string.record_detail);
		mLvRecords = findView(R.id.list_records);
		mResultSet = (ResultSet) getIntent().getSerializableExtra(EXTRA_RECORD);
		RecordAdapter adapter = new RecordAdapter(this,mResultSet);
		mLvRecords.setAdapter(adapter);
	}
	
	private class RecordAdapter extends BaseAdapter{

		ResultSet resultSet;
		LayoutInflater infalter;
		int evenColor;
		int oddColor;
		
		int maxTextSize = 500;
		int maxByteSize = 200;
		int maxBitmapWidth;
		int maxBitmapHeight;
		Resources res;
		
		RecordAdapter(Context context,ResultSet resultSet){
			this.resultSet = resultSet;
			this.infalter = LayoutInflater.from(context);
			this.res = context.getResources();
			this.evenColor = res.getColor(R.color.table_even_cell_bg_color);
			this.oddColor = res.getColor(R.color.table_odd_cell_bg_color);
			this.maxBitmapWidth = AppUtils.dipToPx(context, 80);
			this.maxBitmapHeight = AppUtils.dipToPx(context, 100);
		}
		
		@Override
		public int getCount() {
			return resultSet == null ? 0 : resultSet.getSize();
		}

		@Override
		public Object getItem(int position) {
			return resultSet.getValue(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RecordItemViewHolder viewHodler;
			if(convertView == null){
				viewHodler = new RecordItemViewHolder();
				convertView = infalter.inflate(R.layout.listitem_record_detail, parent,false);
				viewHodler.tvName = (TextView) convertView.findViewById(R.id.tv_record_name);
				viewHodler.tvValue = (TextView) convertView.findViewById(R.id.tv_record_value);
				convertView.setTag(viewHodler);
			}else{
				viewHodler = (RecordItemViewHolder) convertView.getTag();
			}
			
			if(position % 2 == 0){
				convertView.setBackgroundColor(evenColor);
			}else{
				convertView.setBackgroundColor(oddColor);
			}
			
			viewHodler.tvName.setText(resultSet.getColumnName(position));
			
			viewHodler.tvValue.setCompoundDrawables(null, null, null, null);
			
			Object value = resultSet.getValue(position);
			if(value == null){
				viewHodler.tvValue.setText("(null)");
			}else{
				if(value instanceof byte[]){
					byte[] byteData = (byte[])value;
					Bitmap bm = BitmapFactory.decodeByteArray(byteData, 0, byteData.length);
					if(bm != null){
						viewHodler.tvValue.setText("");
						float bmWidth = bm.getWidth();
						float bmHeight = bm.getHeight();
						
						boolean sizeChanged = false;
						
						if(bmWidth > maxBitmapWidth){
							bmHeight = bmHeight/bmWidth *  maxBitmapWidth;
							bmWidth = maxBitmapWidth;
							sizeChanged = true;
						}
						
						if(bmHeight > maxBitmapHeight){
							bmWidth = bmWidth / bmHeight * maxBitmapHeight;
							bmHeight = maxBitmapHeight;
							sizeChanged = true;;
						}
						
						if(sizeChanged){
							bm = Bitmap.createScaledBitmap(bm, (int)bmWidth, (int)bmHeight, true);
						}
						Drawable drawable = new BitmapDrawable(res, bm);
						drawable.setBounds(0, 0, (int)bmWidth, (int)bmHeight);
						viewHodler.tvValue.setCompoundDrawables(drawable, null, null, null);
					}else{
						StringBuilder hexBuilder = new StringBuilder();
						if(byteData.length > maxByteSize){
							for(byte b : byteData){
								hexBuilder.append(byteToHex(b));
							}
							hexBuilder.append("...");
						}else{
							for(byte b : byteData){
								hexBuilder.append(byteToHex(b));
							}
						}
						viewHodler.tvValue.setText(hexBuilder.toString());
					}
				}else{
					final String wholeText = value.toString();
					final TextView tvValue = viewHodler.tvValue;
					if(wholeText.length() > maxTextSize){
						final String lessText = wholeText.subSequence(0, maxTextSize - 3) + "...";
						tvValue.setText(lessText);
						tvValue.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								String tvText = tvValue.getText().toString();
								if(tvText.length() == wholeText.length()){
									tvValue.setText(lessText);
								}else{
									tvValue.setText(wholeText);
								}
								
							}
						});
					}else{
						tvValue.setText(wholeText);
					}
				}
			}
			
			return convertView;
		}
		
		 private  String byteToHex(byte b){
	        String s = Integer.toHexString(b & 0xFF);
	        if (s.length() == 1){
	            return "0" + s;
	        }else{
	            return s;
	        }
		 }

	}
	
	class RecordItemViewHolder{
		TextView tvName;
		TextView tvValue;
	}
}
