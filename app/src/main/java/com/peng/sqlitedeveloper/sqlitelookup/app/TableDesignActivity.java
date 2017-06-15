package com.peng.sqlitedeveloper.sqlitelookup.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.peng.sqlitedeveloper.sqlite.DaoFactory;
import com.peng.sqlitedeveloper.sqlite.DbSqlite;
import com.peng.sqlitedeveloper.sqlite.IBaseDao;
import com.peng.sqlitedeveloper.R;
import com.peng.sqlitedeveloper.sqlitelookup.adapter.SimpleTableAdapter;
import com.peng.sqlitedeveloper.sqlitelookup.model.ColumnInfo;
import com.peng.sqlitedeveloper.sqlitelookup.model.SqliteMaster;
import com.peng.sqlitedeveloper.sqlitelookup.utils.AppUtils;
import com.peng.sqlitedeveloper.sqlitelookup.utils.SqlUtils;
import com.peng.sqlitedeveloper.tablefixheaders.TableFixHeaders;

public class TableDesignActivity extends BaseActivity implements View.OnClickListener{

	public static final String EXTRA_TABLE_NAME = "table-name";
	public static final String EXTRA_DB_PATH = "db-path";
	
	private static final String[] HEADER = {
		"Index",
		"Name",
		"Type",
		"Not Null",
		"Unique",
		"Primary Key",
		"DEFAULT"
	};
	
	private static final int[] CELL_WIDTH = {
		100,150,100,80,80,100,120
	};
	
	private TableFixHeaders mTables;
	private ImageView mIvCheckout;
	
	private String mTableName;
	private String mDbPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_table_design);
		mTables = findView(R.id.table);
		mIvCheckout = findView(R.id.iv_right);
		mIvCheckout.setVisibility(View.VISIBLE);
		mIvCheckout.setImageResource(R.drawable.ic_check_data);
		mIvCheckout.setOnClickListener(this);
		Intent extraIntent = getIntent();
		mTableName = extraIntent.getStringExtra(EXTRA_TABLE_NAME);
		mDbPath = extraIntent.getStringExtra(EXTRA_DB_PATH);
		setMainTitle(String.format("Table Design of %s", mTableName));
		enableBack();
		listTableDesign();
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.iv_right){
			Intent dataIntent = new Intent(this,TableDataActivity.class);
			dataIntent.putExtra(TableDataActivity.EXTRA_DB_PATH, mDbPath);
			dataIntent.putExtra(TableDataActivity.EXTRA_TABLE_NAME, mTableName);
			startActivity(dataIntent);
		}
	}
	
	private void listTableDesign(){
		new GetTableDesignTask().execute();
	}
	
	class GetTableDesignTask  extends AsyncTask<Void, Void, List<ColumnInfo>>{

		@Override
		protected List<ColumnInfo> doInBackground(Void... params) {
			SQLiteDatabase db = SQLiteDatabase.openDatabase(mDbPath, null,  SQLiteDatabase.OPEN_READONLY);
			DbSqlite dbSqlite = new DbSqlite(null, db);
			IBaseDao<SqliteMaster> masterDao = DaoFactory.createGenericDao(dbSqlite, SqliteMaster.class);
			SqliteMaster table = masterDao.queryFirstRecord("type=? and name=?", "table",mTableName);
			List<ColumnInfo> columnInfoList = new ArrayList<ColumnInfo>();
			SqlUtils.resolveCreateSql(table.sql, columnInfoList);
			dbSqlite.closeDB();
			return columnInfoList;
		}
		
		@Override
		protected void onPostExecute(List<ColumnInfo> result) {
			super.onPostExecute(result);
			TableDesignAdapter designAdapter = new TableDesignAdapter(TableDesignActivity.this, result);
			mTables.setAdapter(designAdapter);
		}
		
	}
	
	class TableDesignAdapter extends SimpleTableAdapter<String,ColumnInfo>{
		
		TableDesignAdapter(Context context, List<ColumnInfo> columnInfoList){
			super(context,Arrays.asList(HEADER),columnInfoList);
		}
		
		@Override
		public int getWidth(int column) {
			return AppUtils.dipToPx(getContext(), CELL_WIDTH[column+1]);
		}

		@Override
		public void bindHeaderText(TextView tvHeader, int column, String hRecord) {
			tvHeader.setText(hRecord);
		}

		@Override
		public void bindCellText(TextView tvCell, int row, int column,
				ColumnInfo cRecord) {
			switch(column){
			case -1:
				tvCell.setText(String.valueOf(row + 1));
				break;
			case 0:
				tvCell.setText(cRecord.getName());
				break;	
			case 1:
				tvCell.setText(cRecord.getType());
				break;
			case 2:
				tvCell.setText(cRecord.isNull()?"":"Not Null");
				break;
			case 3:
				tvCell.setText(cRecord.isUnique()?"Unique":"");
				break;
			case 4:
				tvCell.setText(cRecord.isPrimaryKey()?"Primary Key":"");
				break;
			case 5:
				String defaultValue = cRecord.getDefaultValue();
				tvCell.setText(defaultValue.equalsIgnoreCase("null")?"":defaultValue);
				break;
			}
		}
	}

}
