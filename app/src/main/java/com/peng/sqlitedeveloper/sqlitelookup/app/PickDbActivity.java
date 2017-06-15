package com.peng.sqlitedeveloper.sqlitelookup.app;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.peng.sqlitedeveloper.sqlite.DBTransaction;
import com.peng.sqlitedeveloper.sqlite.DBTransaction.DBTransactionInterface;
import com.peng.sqlitedeveloper.sqlite.DaoFactory;
import com.peng.sqlitedeveloper.sqlite.DbSqlite;
import com.peng.sqlitedeveloper.sqlite.IBaseDao;
import com.peng.sqlitedeveloper.R;
import com.peng.sqlitedeveloper.sqlitelookup.adapter.SimpleListAdapter;
import com.peng.sqlitedeveloper.sqlitelookup.model.DbModel;

public class PickDbActivity extends BaseActivity implements View.OnClickListener{

	private static final String SD_ROOT = Environment.getExternalStorageDirectory().getPath();
	
	private ImageView mIvBack;
	private TextView mTvEmptyDb;
	private RecyclerView mRvFileList;
	private FileListAdapter mFileListAdapter;
	private List<File> mFileList = new ArrayList<File>();
	private String mLastPath;
	
	private FileFilter mfileFilter = new FileFilter(){
		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().endsWith(".db");
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_db);
		setMainTitle(R.string.pick_db);
		mIvBack = findView(R.id.iv_back);
		mIvBack.setVisibility(View.VISIBLE);
		mRvFileList = findView(R.id.list_files);
		mTvEmptyDb = findView(R.id.tv_empty_db);
		mIvBack.setOnClickListener(this);
		mRvFileList.setLayoutManager(new LinearLayoutManager(this));
		listFiles(SD_ROOT);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.iv_back:
			performBack();
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		if(mLastPath == null || mLastPath.equals(SD_ROOT)){
			super.onBackPressed();
		}else{
			File parentDir = new File(mLastPath).getParentFile();
			listFiles(parentDir.getAbsolutePath());
		}
	}
	
	private void performBack(){
		if(mLastPath == null || mLastPath.equals(SD_ROOT)){
			finish();
		}else{
			File parentDir = new File(mLastPath).getParentFile();
			listFiles(parentDir.getAbsolutePath());
		}
	}
	
	private void listFiles(String basePath){
		mLastPath = basePath;
		new FileListTask(basePath).execute();
	}
	
	private void selectDb(final String dbPath){
		SQLiteDatabase db = openOrCreateDatabase(AppContext.DB_NAME, MODE_PRIVATE, null);
		DbSqlite dbSqlite = new DbSqlite(this, db);
		final IBaseDao<DbModel> dbDao =DaoFactory.createGenericDao(dbSqlite, DbModel.class);
		DBTransaction.transact(dbSqlite, new DBTransactionInterface() {
			@Override
			public void onTransact() {
				DbModel record = dbDao.queryFirstRecord("db_path=?", dbPath);
				if(record == null){
					DbModel dbModel = new DbModel();
					File dbFile = new File(dbPath);
					dbModel.dbName = dbFile.getName();
					dbModel.dbPath = dbFile.getAbsolutePath();
					dbDao.insert(dbModel);
				}
			}
		});
		dbSqlite.closeDB();
		
		Intent dbTablesIntent = new Intent(this,DbTablesActivity.class);
		dbTablesIntent.putExtra(DbTablesActivity.EXTRA_DB_PATH, dbPath);
		startActivity(dbTablesIntent);
		finish();
	}
	
	class FileListTask  extends AsyncTask<Void, Void, File[]>{

		String basePath;
		
		FileListTask(String basePath){
			this.basePath = basePath;
		}
		
		@Override
		protected File[] doInBackground(Void... params) {
			File baseFile = new File(basePath);
			return baseFile.listFiles(mfileFilter);
		}
		
		@Override
		protected void onPostExecute(File[] result) {
			super.onPostExecute(result);
			mFileList.clear();
			if(result != null && result.length != 0){
				mTvEmptyDb.setVisibility(View.GONE);
				mFileList.addAll(Arrays.asList(result));
			}else{
				mTvEmptyDb.setVisibility(View.VISIBLE);
			}
			
			if(mFileListAdapter == null){
				mFileListAdapter = new FileListAdapter(PickDbActivity.this, mFileList);
				mRvFileList.setAdapter(mFileListAdapter);
			}else{
				mFileListAdapter.notifyDataSetChanged();
			}
		}
	}
	
	class FileListAdapter extends SimpleListAdapter<File>{

		LayoutInflater layoutInflater;
		List<File> fileList;
		
		FileListAdapter(Context context,List<File> fileList){
			super(context, fileList);
			this.fileList = fileList;
		}
		
		@Override
		public void onBindViewHolder(SimpleItemViewHodler viewHolder, int position) {
			final File file = fileList.get(position);
			if(file.isDirectory()){
				viewHolder.ivIcon.setImageResource(R.drawable.ic_folder);
			}else{
				viewHolder.ivIcon.setImageResource(R.drawable.ic_db);
			}
			viewHolder.tvText.setText(file.getName());
			viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					String filePath = file.getAbsolutePath();
					if(file.isDirectory()){
						listFiles(filePath);
					}else{
						selectDb(filePath);
					}
				}
			});
		}
		
	}
}
