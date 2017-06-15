package com.peng.sqlitedeveloper.sqlitelookup.app;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.peng.sqlitedeveloper.R;
import com.peng.sqlitedeveloper.sqlite.DaoFactory;
import com.peng.sqlitedeveloper.sqlite.DbSqlite;
import com.peng.sqlitedeveloper.sqlite.IBaseDao;
import com.peng.sqlitedeveloper.sqlitelookup.adapter.SimpleListAdapter;
import com.peng.sqlitedeveloper.sqlitelookup.dialog.SelectorDialog;
import com.peng.sqlitedeveloper.sqlitelookup.dialog.SelectorDialog.OnItemSelectedListener;
import com.peng.sqlitedeveloper.sqlitelookup.model.SqliteMaster;

import java.io.File;
import java.util.List;

/**
 * show tables of db
 *
 * @author Darcy
 */
public class DbTablesActivity extends BaseActivity implements OnItemSelectedListener, View.OnClickListener {

    public static final String EXTRA_DB_PATH = "db-path";

    private static final String[] SELECT_ITEMS = {"Table Design", "Table Data"};

    private RecyclerView mRvTableList;
    private TableListAdapter mTableListAdapter;

    private SelectorDialog mDlgSelect;
    private String mSelectTable;

    private String mDbPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_tables);
        mRvTableList = findView(R.id.list_db_tables);
        mRvTableList.setLayoutManager(new LinearLayoutManager(this));
        mDbPath = getIntent().getStringExtra(EXTRA_DB_PATH);
        mDlgSelect = new SelectorDialog(this);
        mDlgSelect.setSelectItems(SELECT_ITEMS, this);
        enableBack();
        File dbFile = new File(mDbPath);
        setMainTitle(String.format("Tables In %s", dbFile.getName()));
        setRightIcon(R.drawable.ic_console);
        setRightClickListener(this);
        listTables();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        listTables();
    }

    @Override
    public void onSelected(int position) {
        if (position == 0) {
            selectTableDesign(mSelectTable);
        } else {
            selectTableData(mSelectTable);
        }
    }

    private void selectTableDesign(String tableName) {
        Intent designIntent = new Intent(this, TableDesignActivity.class);
        designIntent.putExtra(TableDesignActivity.EXTRA_DB_PATH, mDbPath);
        designIntent.putExtra(TableDesignActivity.EXTRA_TABLE_NAME, tableName);
        startActivity(designIntent);
    }

    private void selectTableData(String tableName) {
        Intent dataIntent = new Intent(this, TableDataActivity.class);
        dataIntent.putExtra(TableDataActivity.EXTRA_DB_PATH, mDbPath);
        dataIntent.putExtra(TableDataActivity.EXTRA_TABLE_NAME, tableName);
        startActivity(dataIntent);
    }

    private void listTables() {
        new GetDbTablesTask().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_right:
                SQLConsoleActivity.startUI(this,mDbPath);
                break;
            default:
                break;
        }


    }

    class GetDbTablesTask extends AsyncTask<Void, Void, List<SqliteMaster>> {

        @Override
        protected List<SqliteMaster> doInBackground(Void... params) {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(mDbPath, null, SQLiteDatabase.OPEN_READONLY);
            DbSqlite dbSqlite = new DbSqlite(null, db);
            IBaseDao<SqliteMaster> masterDao = DaoFactory.createGenericDao(dbSqlite, SqliteMaster.class);
            List<SqliteMaster> tables = masterDao.query(new String[]{"name"}, "type=? and name NOT like 'sqlite_%'", new String[]{"table"}, null);
            dbSqlite.closeDB();
            return tables;
        }

        @Override
        protected void onPostExecute(List<SqliteMaster> result) {
            super.onPostExecute(result);
            mTableListAdapter = new TableListAdapter(DbTablesActivity.this, result);
            mRvTableList.setAdapter(mTableListAdapter);
        }
    }

    class TableListAdapter extends SimpleListAdapter<SqliteMaster> {

        private List<SqliteMaster> data;

        public TableListAdapter(Context context, List<SqliteMaster> data) {
            super(context, data);
            this.data = data;
        }

        @Override
        public void onBindViewHolder(SimpleItemViewHodler viewHolder, int position) {
            final SqliteMaster table = data.get(position);
            viewHolder.ivIcon.setImageResource(R.drawable.ic_table);
            viewHolder.tvText.setText(table.name);
            viewHolder.itemView.setOnLongClickListener((new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mSelectTable = table.name;
                    mDlgSelect.show();
                    return true;
                }
            }));
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectTableDesign(table.name);
                }
            });
        }
    }

}
