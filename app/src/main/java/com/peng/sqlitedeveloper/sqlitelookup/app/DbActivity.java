package com.peng.sqlitedeveloper.sqlitelookup.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.peng.sqlitedeveloper.R;
import com.peng.sqlitedeveloper.sqlite.DBTransaction;
import com.peng.sqlitedeveloper.sqlite.DaoFactory;
import com.peng.sqlitedeveloper.sqlite.DbSqlite;
import com.peng.sqlitedeveloper.sqlite.IBaseDao;
import com.peng.sqlitedeveloper.sqlitelookup.adapter.SimpleListAdapter;
import com.peng.sqlitedeveloper.sqlitelookup.model.DbModel;
import com.peng.sqlitedeveloper.sqlitelookup.utils.FileUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DbActivity extends BaseActivity implements View.OnClickListener {


    public static final String SQL_CACHE_PATH = Environment.getExternalStorageDirectory() + "/111_db_cache/";
    public static final int PERMISSION_REQUEST_CODE =111 ;

    private ImageView mIvAddDb;
    private RecyclerView mRvDbList;
    private DbHistoryAdapter mHistoryAdapter;
    private List<DbModel> mHistoryData;
    private View mVEmptyAddDb;
    private AlertDialog mDialog;
    private boolean mHasPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);
        mIvAddDb = findView(R.id.iv_right);
        mRvDbList = findView(R.id.list_db);
        mVEmptyAddDb = findView(R.id.iv_add_db);
        mRvDbList.setLayoutManager(new LinearLayoutManager(this));
        mIvAddDb.setVisibility(View.VISIBLE);
        mIvAddDb.setImageResource(R.drawable.ic_add_db);
        mIvAddDb.setOnClickListener(this);
        initView();
        checkAndRequestPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Class<?> updateManagerClass = Class.forName("com.peng.sqlitedeveloper.sqlitelookup.utils.UpdateManager");
            Method updateMethod = updateManagerClass.getMethod("update", new Class[]{Activity.class});
            updateMethod.invoke(updateManagerClass, this);
        } catch (Exception e) {
        }
    }

    private void checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            initData();
            mHasPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mHasPermission = true;
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle("权限警告：")
                            .setMessage("由于要将你创建的数据库保存在sd上，这样你可以方便的拿到数据库文件，所以需要这个权限。")
                            .setPositiveButton("授权", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(DbActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();

                                }
                            }).show();
                }


            }
        }

    }



    @Override
    protected void onRestart() {
        super.onRestart();
        refleshDbHistory();
    }

    private void initData() {
        File dbCacheDir = new File(SQL_CACHE_PATH);
        if (!dbCacheDir.exists()) {
            dbCacheDir.mkdirs();
        }
        refleshDbHistory();
    }

    private void initView() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择导入数据库方式：");
        builder.setPositiveButton("创建新数据库", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(DbActivity.this);
                //builder.setView(R.layout.dialog_input);
                builder.setTitle("请输入数据库名称：");
                final EditText inputView = ((EditText) LayoutInflater.from(DbActivity.this).inflate(R.layout.dialog_input, null, false));
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String dbName = inputView.getText().toString();

                        if (TextUtils.isEmpty(dbName)) {
                            Toast.makeText(DbActivity.this, "请输入数据库名称！", Toast.LENGTH_SHORT).show();
                        }
                        String newDbName = SQL_CACHE_PATH + dbName + ".db";
                        //TODO:执行创建创建数据库的操作
                        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(new File(newDbName), null);
                        sqLiteDatabase.close();
                        insertNewDb(newDbName);
                        refleshDbHistory();
                        Toast.makeText(DbActivity.this, newDbName, Toast.LENGTH_LONG).show();
                    }
                });
                AlertDialog inputDialog = builder.create();
                inputDialog.setView(inputView);
                inputDialog.show();
            }
        });
        builder.setNegativeButton("从SD卡选择", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performPickDb();
            }
        });

        mDialog = builder.create();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void insertNewDb(final String dbPath) {
        SQLiteDatabase db = openOrCreateDatabase(AppContext.DB_NAME, MODE_PRIVATE, null);
        DbSqlite dbSqlite = new DbSqlite(this, db);
        final IBaseDao<DbModel> dbDao = DaoFactory.createGenericDao(dbSqlite, DbModel.class);
        DBTransaction.transact(dbSqlite, new DBTransaction.DBTransactionInterface() {
            @Override
            public void onTransact() {
                DbModel record = dbDao.queryFirstRecord("db_path=?", dbPath);
                if (record == null) {
                    DbModel dbModel = new DbModel();
                    File dbFile = new File(dbPath);
                    dbModel.dbName = dbFile.getName();
                    dbModel.dbPath = dbFile.getAbsolutePath();
                    dbDao.insert(dbModel);
                }
            }
        });
        dbSqlite.closeDB();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_right:
            case R.id.iv_add_db:
//				performPickDb();
                mDialog.show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    private void refleshDbHistory() {
        new GetHistoryListTask().execute();
    }

    private void performPickDb() {
        Intent pickIntent = new Intent(this, PickDbActivity.class);
        startActivity(pickIntent);
    }

    class GetHistoryListTask extends AsyncTask<Void, Void, List<DbModel>> {

        @Override
        protected List<DbModel> doInBackground(Void... params) {
            SQLiteDatabase db = openOrCreateDatabase(AppContext.DB_NAME,
                    MODE_PRIVATE, null);
            DbSqlite dbSqlite = new DbSqlite(DbActivity.this, db);
            IBaseDao<DbModel> dbDao = DaoFactory.createGenericDao(dbSqlite, DbModel.class);
            List<DbModel> historyList = dbDao.queryAll();
            List<DbModel> existList = new ArrayList<>();
            if (historyList != null) {
                for (DbModel dbModel : historyList) {
                    if (FileUtils.exists(dbModel.dbPath)) {
                        existList.add(dbModel);
                    } else {
                        dbDao.delete("db_id=?", String.valueOf(dbModel.dbId));
                    }
                }
            }
            dbSqlite.closeDB();
            return existList;
        }

        @Override
        protected void onPostExecute(List<DbModel> result) {
            super.onPostExecute(result);
            if (result != null) {
                mVEmptyAddDb.setVisibility(View.GONE);
                if (mHistoryAdapter == null) {
                    mHistoryData = new ArrayList<DbModel>();
                    mHistoryData.addAll(result);
                    mHistoryAdapter = new DbHistoryAdapter(DbActivity.this, mHistoryData);
                    mRvDbList.setAdapter(mHistoryAdapter);
                } else {
                    mHistoryData.clear();
                    mHistoryData.addAll(result);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            } else {
                mVEmptyAddDb.setVisibility(View.VISIBLE);
                mVEmptyAddDb.setOnClickListener(DbActivity.this);
            }
        }
    }

    class DbHistoryAdapter extends SimpleListAdapter<DbModel> {

        List<DbModel> dbHistory;

        public DbHistoryAdapter(Context context, List<DbModel> data) {
            super(context, data);
            dbHistory = data;
        }

        @Override
        public void onBindViewHolder(SimpleItemViewHodler viewHolder,
                                     int position) {
            final DbModel dbModel = dbHistory.get(position);
            viewHolder.ivIcon.setImageResource(R.drawable.ic_db);
            viewHolder.tvText.setText(dbModel.dbName);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File dbFile = new File(dbModel.dbPath);
                    if (dbFile.exists()) {
                        Intent dbTablesIntent = new Intent(DbActivity.this, DbTablesActivity.class);
                        dbTablesIntent.putExtra(DbTablesActivity.EXTRA_DB_PATH, dbModel.dbPath);
                        startActivity(dbTablesIntent);
                    } else {
                        SQLiteDatabase db = openOrCreateDatabase(AppContext.DB_NAME, MODE_PRIVATE, null);
                        DbSqlite dbSqlite = new DbSqlite(DbActivity.this, db);
                        IBaseDao<DbModel> dbDao = DaoFactory.createGenericDao(dbSqlite, DbModel.class);
                        dbDao.delete("db_id=?", String.valueOf(dbModel.dbId));
                        dbSqlite.closeDB();
                        Toast.makeText(DbActivity.this, getString(R.string.db_remove_error), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
