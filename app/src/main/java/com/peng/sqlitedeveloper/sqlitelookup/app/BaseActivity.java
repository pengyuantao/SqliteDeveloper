package com.peng.sqlitedeveloper.sqlitelookup.app;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.peng.sqlitedeveloper.R;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

abstract class BaseActivity extends RxAppCompatActivity{
	
	private ImageView mIvBack;
	private ImageView mIvRight;
	private TextView mTvTitle;

	@SuppressWarnings("unchecked")
	public <T extends View> T findView(int id) {
		return (T) findViewById(id);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mTvTitle = findView(R.id.tv_title);
		mIvBack = findView(R.id.iv_back);
		mIvRight = findView(R.id.iv_right);
	}
	
	protected void setMainTitle(int resId){
		if(mTvTitle != null)
			mTvTitle.setText(resId);
	}
	
	protected void setMainTitle(String title){
		if(mTvTitle != null)
			mTvTitle.setText(title);
	}

	protected void setRightIcon(@DrawableRes int resId) {
		mIvRight.setVisibility(View.VISIBLE);
		mIvRight.setImageResource(resId);
	}

	protected void setRightClickListener(View.OnClickListener listener) {
		mIvRight.setOnClickListener(listener);
	}

	protected void enableBack(){
		if(mIvBack != null){
			mIvBack.setVisibility(View.VISIBLE);
			mIvBack.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
	}
}