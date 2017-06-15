package com.peng.sqlitedeveloper.sqlitelookup.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.peng.sqlitedeveloper.R;
import com.peng.sqlitedeveloper.sqlite.ResultSet;
import com.peng.sqlitedeveloper.sqlitelookup.adapter.SimpleTableAdapter;
import com.peng.sqlitedeveloper.sqlitelookup.adapter.SmartWordHintAdapter;
import com.peng.sqlitedeveloper.sqlitelookup.divider.RecycleViewDivider;
import com.peng.sqlitedeveloper.sqlitelookup.model.SmartWord;
import com.peng.sqlitedeveloper.sqlitelookup.utils.FileUtils;
import com.peng.sqlitedeveloper.sqlitelookup.utils.SQLKeyWordUtils;
import com.peng.sqlitedeveloper.sqlitelookup.utils.SQLiteUtils;
import com.peng.sqlitedeveloper.tablefixheaders.TableFixHeaders;
import com.trello.rxlifecycle.android.ActivityEvent;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by pyt on 2017/6/1.
 */

public class SQLConsoleActivity extends BaseActivity implements SmartWordHintAdapter.OnItemClickListener {
    public static final String PATH_DB = "PATH_DB";
    private static final String TAG = "SQLConsoleActivity";

    private static final String DB_SELECT = "SELECT";
    private static final String DB_UPDATE = "UPDATE";
    private static final String DB_INSERT = "INSERT";
    private static final String DB_DELETE = "DELETE";


    private EditText etSQLConsole;

    private TextView tvReviewSQL;

    private RecyclerView rvSmartWord;

    private SmartWordHintAdapter adapter = new SmartWordHintAdapter();

    //sql中所有的关键字
    private ArrayList<SmartWord> sqlSmartWordList;
    //当前需要智能提示的关键字，其中包括sql关键字和数据库表明和列名
    private ArrayList<SmartWord> displaySmartWordList = new ArrayList<>();
    //数据库中所有的关键字
    private ArrayList<SmartWord> databaseSmartWordList = new ArrayList<>();
    //搜索缓存的表
    private ArrayList<SmartWord> searchSmartWordList = new ArrayList<>();
    //列
    private ArrayList<String> columnList = new ArrayList<>();

    private StringBuilder builder = new StringBuilder();

    private SQLiteDatabase sqLiteDatabase;

    private FrameLayout containerView;

    private TextView tvResultText;

    private TableFixHeaders tvSelectResult;

    private ArrayList<ResultSet> selectDataList = new ArrayList<>();

    private TableDataAdapter tableDataAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sql_console);
        initView();
        initData();
        initEvent();
    }

    private void initData() {
        initSQLiteDatabase();
        initAllSmartWord();
        initSelectAdapter();
    }

    private void initSelectAdapter() {
        tableDataAdapter = new TableDataAdapter(this, columnList, selectDataList);

    }

    private void initSQLiteDatabase() {
        //获取
        String databasePath = getIntent().getStringExtra(PATH_DB);
        if (!FileUtils.exists(databasePath)) {
            throw new IllegalArgumentException("the datebase path is not exist");
        }
        sqLiteDatabase = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    /**
     * 进行初始化所有的智能提示的key word
     */
    private void initAllSmartWord() {
        Observable.create(new Observable.OnSubscribe<Context>() {
            @Override
            public void call(Subscriber<? super Context> subscriber) {
                //开始执行程序，从缓存和数据库中获取所有的关键
                subscriber.onNext(SQLConsoleActivity.this.getApplicationContext());
                subscriber.onCompleted();
            }
        })

                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        displaySmartWordList.clear();
                    }
                })
                .compose(this.<Context>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(Schedulers.io())
                .map(new Func1<Context, ArrayList<SmartWord>>() {
                    @Override
                    public ArrayList<SmartWord> call(Context context) {
                        ArrayList<SmartWord> cacheSmartWord = new ArrayList<SmartWord>();
                        //获取所有sql对应的列表
                        sqlSmartWordList = SQLKeyWordUtils.readSQLWordList(context);
                        cacheSmartWord.addAll(sqlSmartWordList);
                        //获取所有的数据库表名
                        Cursor tableNameCurcor = sqLiteDatabase.rawQuery("select name,sql from sqlite_master where type='table'", null);
                        if (tableNameCurcor != null && tableNameCurcor.moveToFirst()) {
                            do {
                                String tableName = tableNameCurcor.getString(0);
                                String sql = tableNameCurcor.getString(1);
                                databaseSmartWordList.add(new SmartWord(tableName));
                                List<String> columNameFromCreateSql = SQLiteUtils.getColumNameFromCreateSql(sql);
                                for (String s : columNameFromCreateSql) {
                                    databaseSmartWordList.add(new SmartWord(s));
                                }
                            } while (tableNameCurcor.moveToNext());

                        }
                        cacheSmartWord.addAll(databaseSmartWordList);
                        return cacheSmartWord;
                    }
                })
                .flatMap(new Func1<ArrayList<SmartWord>, Observable<SmartWord>>() {
                    @Override
                    public Observable<SmartWord> call(ArrayList<SmartWord> words) {
                        return Observable.from(words);
                    }
                })
                .filter(new Func1<SmartWord, Boolean>() {
                    @Override
                    public Boolean call(SmartWord smartWord) {
                        return !displaySmartWordList.contains(smartWord);
                    }
                })
                .subscribe(new Action1<SmartWord>() {
                    @Override
                    public void call(SmartWord smartWord) {
                        displaySmartWordList.add(smartWord);
                    }
                });
    }


    private void initEvent() {
        etSQLConsole.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    executeSQL(etSQLConsole.getText().toString());
                    return true;
                }
                return false;
            }
        });
        //分割线
        rvSmartWord.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSmartWord.setAdapter(adapter);
        rvSmartWord.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL, 1, Color.GRAY));
        adapter.setOnItemClickListener(this);
        adapter.refresh(searchSmartWordList);
        etSQLConsole.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvReviewSQL.setText(s);
                Log.i(TAG, "onTextChanged() called with: s = [" + s + "], start = [" + start + "], before = [" + before + "], count = [" + count + "]");
                String curText = s.toString();
                int index = curText.lastIndexOf(" ");
                if (index != -1) {
                    String head = curText.substring(index + 1, curText.length());
                    ArrayList<SmartWord> wordArrayList = filterKeyWord(head);
                    adapter.refresh(wordArrayList);
                } else {
                    ArrayList<SmartWord> wordArrayList = filterKeyWord(s.toString());
                    adapter.refresh(wordArrayList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * 执行sql语句，除了删除数据库的操作
     *
     * @param s
     */
    private void executeSQL(final String s) {
        //只要是对数据库的增删改查，其他的直接操作数据库语句
        if (s.startsWith(DB_SELECT) || s.startsWith(DB_SELECT.toLowerCase())) {
            //这里需要进行数据展示,展示不处理
            Observable.create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {
                    subscriber.onNext(s);

                }
            })
                    .compose(this.<String>bindUntilEvent(ActivityEvent.DESTROY))
                    .observeOn(Schedulers.io())
                    .map(new Func1<String, String>() {
                        @Override
                        public String call(String s) {

                            Cursor cursor = sqLiteDatabase.rawQuery(s.trim(), null);
                            try {
                                if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                                    selectDataList.clear();
                                    columnList.clear();
                                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                                        columnList.add(cursor.getColumnName(i));
                                    }
                                    parseCursorToResult(cursor, selectDataList);
                                }

                            } finally {
                                if (cursor != null) {
                                    cursor.close();
                                }
                            }

                            return s;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            isShowSelectView(false);
                            tvResultText.setText("操作失败: " + e.getMessage());
                        }

                        @Override
                        public void onNext(String s) {
                            isShowSelectView(true);
                            Log.i(TAG, "onNext: " + selectDataList);
                            tvSelectResult.setAdapter(tableDataAdapter);
                        }
                    });
        } else {
            Observable.create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {
                    subscriber.onNext(s.trim());
                    subscriber.onCompleted();
                }
            })
                    .compose(this.<String>bindUntilEvent(ActivityEvent.DESTROY))
                    .observeOn(Schedulers.io())
                    .map(new Func1<String, String>() {
                        @Override
                        public String call(String s) {
                            sqLiteDatabase.execSQL(s);
                            return s;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            isShowSelectView(false);
                            tvResultText.setText("执行失败：" + e.getMessage());
                        }

                        @Override
                        public void onNext(String s) {
                            isShowSelectView(false);
                            tvResultText.setText("执行成功");
                            if (s.startsWith("create"))
                                initAllSmartWord();
                        }
                    });
        }


    }

    private void isShowSelectView(boolean show) {
        if (show) {
            tvSelectResult.setVisibility(View.VISIBLE);
            tvResultText.setVisibility(View.INVISIBLE);
        } else {
            tvResultText.setVisibility(View.VISIBLE);
            tvSelectResult.setVisibility(View.INVISIBLE);
        }


    }


    /**
     * 初始化View
     */
    private void initView() {
        setMainTitle("SQL Console");
        etSQLConsole = findView(R.id.et_raw_sql);
        rvSmartWord = findView(R.id.rv_smart_hint);
        tvReviewSQL = findView(R.id.tv_sql_view);
        containerView = findView(R.id.fl_result_container);
        tvResultText = findView(R.id.tv_result);
        tvSelectResult = findView(R.id.tfh_select_result);
    }

    /**
     * 启动sql控制台界面
     *
     * @param context
     * @param dbPath
     */
    public static void startUI(Context context, String dbPath) {
        Intent intent = new Intent(context, SQLConsoleActivity.class);
        intent.putExtra(PATH_DB, dbPath);
        context.startActivity(intent);
    }

    @Override
    public void onItemClick(int position, View view, SmartWord smartWord) {
        smartWord.time++;
        String s = etSQLConsole.getText().toString();
        int indexOf = s.lastIndexOf(" ");
        int len = 0;
        if (indexOf != -1) {
            builder.setLength(0);
            builder.append(s);
            builder.delete(indexOf + 1, s.length());
            StringBuilder append = builder.append(smartWord.word).append(" ");
            len = append.length();
            etSQLConsole.setText(append.toString());
        } else {
            len = smartWord.word.length() + 1;
            etSQLConsole.setText(smartWord.word + " ");

        }
        etSQLConsole.setSelection(len);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SQLKeyWordUtils.writeSQLWordList(this, sqlSmartWordList);
        if (sqLiteDatabase != null) {
            sqLiteDatabase.close();
        }
    }

    public ArrayList<SmartWord> filterKeyWord(String head) {
        searchSmartWordList.clear();
        for (SmartWord smartWord : displaySmartWordList) {
            if (smartWord.word.startsWith(head.toUpperCase()) || smartWord.word.startsWith(head.toLowerCase())) {
                searchSmartWordList.add(smartWord);
            }
        }
        return searchSmartWordList;
    }

    public void clearSql(View view) {
        etSQLConsole.setText("");
    }

    class TableDataAdapter extends SimpleTableAdapter<String, ResultSet> {

        private int MAX_TEXT_LEN = 30;

        public TableDataAdapter(Context context, List<String> headData,
                                List<ResultSet> cellData) {
            super(context, headData, cellData);
        }

        @Override
        public int getWidth(int column) {
            return 100;
        }

        @Override
        public void bindHeaderText(TextView tvHeader, int column,
                                   String hRecord) {
            tvHeader.setText(hRecord);
        }

        @Override
        public void bindCellText(TextView tvCell, int row, int column,
                                 final ResultSet cRecord) {
            Object cellValue = cRecord.getValue(column + 1);
            if (cellValue != null) {
                String cellStr = cellValue.toString();
                if (cellStr.length() > MAX_TEXT_LEN) {
                    cellStr = cellStr.substring(0, MAX_TEXT_LEN - 3);
                    cellStr += "...";
                }
                tvCell.setText(cellStr);
            } else {
                tvCell.setText("(null)");
            }

            tvCell.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    return true;
                }
            });
        }
    }


    /**
     * set data in cursor to ResultSet List
     *
     * @param cursor
     * @param resultList the data will set in it
     */
    public void parseCursorToResult(Cursor cursor, List<ResultSet> resultList) {
        int columnCount;
        int columnType;
        Object columnVal = null;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                columnCount = cursor.getColumnCount();
                ResultSet result = new ResultSet();
                for (int index = 0; index < columnCount; ++index) {
                    columnType = cursor.getType(index);
                    switch (columnType) {
                        case Cursor.FIELD_TYPE_BLOB:
                            columnVal = cursor.getBlob(index);
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            columnVal = cursor.getDouble(index);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            columnVal = cursor.getLong(index);
                            break;
                        case Cursor.FIELD_TYPE_NULL:
                            columnVal = null;
                            break;
                        default:
                            columnVal = cursor.getString(index);
                            break;
                    }
                    result.setValue(cursor.getColumnName(index), columnVal);
                }
                resultList.add(result);
            } while (cursor.moveToNext());
        }

    }
}
