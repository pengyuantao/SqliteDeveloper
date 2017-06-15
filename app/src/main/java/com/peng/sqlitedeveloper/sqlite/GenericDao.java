package com.peng.sqlitedeveloper.sqlite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;


/**
 * an implement of IBaseDao
 * @author Darcy
 * @param <T>
 * 
 */
class GenericDao<T> implements IBaseDao<T> {

	private static final String PREFS_TABLE_VERSION = "prefs_table_versioins";
	
	private Class<?> modelClazz;
	
	private String mTableName;

	private DbSqlite mDb;
	
	public GenericDao(DbSqlite db, Class<?> modelClazz) {
		this.mDb = db;
		this.modelClazz = modelClazz;
		mTableName = SqlHelper.getTableName(modelClazz);
	}
	
	@Override
	public void createTable() {
		String createTableSQL = SqlHelper.getCreateTableSQL(modelClazz);
		mDb.execSQL(createTableSQL);
	}

	@Override
	public void updateTable(){
		final int newTableVersion = SqlHelper.getTableVersion(modelClazz);
		final int curTableVersion = getCurTableVersion();
		if(newTableVersion != curTableVersion){
			DBTransaction.transact(mDb, new DBTransaction.DBTransactionInterface() {
				public void onTransact() {
					List<ResultSet> rs = mDb.query("sqlite_master", new String[]{"sql"}, "type=? AND name=?", new String[]{"table",mTableName});
					String curTableSql = rs.get(0).getStringValue("sql");
					Map<String,Boolean>  curColumns = getTableColumnsInfo(curTableSql);
					List<ColumnInfo> newColumnInfos = SqlHelper.getTableColumnInfos(modelClazz);
					int newColumnSize = newColumnInfos.size();
					ColumnInfo newColumnInfo;
					String newColumnName;
					String sql;
					for(int index = 0; index < newColumnSize ; ++index){
						newColumnInfo = newColumnInfos.get(index);
						newColumnName = newColumnInfo.getName().toLowerCase();
						if(curColumns.containsKey(newColumnName)){
							curColumns.put(newColumnName, false);
						}else{
							sql = SqlHelper.getAddColumnSql(mTableName, newColumnInfo);
							mDb.execSQL(sql);
						}
					}
					saveTableVersion(newTableVersion);
				}
			});
		}
	}
	
	@Override
	public long insert(T model) {
		ContentValues contentValues = new ContentValues();
		SqlHelper.parseModelToContentValues(model, contentValues);
		return mDb.insert(mTableName, contentValues);
	}

	@Override
	public boolean batchInsert(List<T> dataList) {
		List<ContentValues> listVal = new ArrayList<ContentValues>();
		for (T model : dataList) {
			ContentValues contentValues = new ContentValues();
			SqlHelper.parseModelToContentValues(model, contentValues);
			listVal.add(contentValues);
		}
		return mDb.batchInsert(mTableName, listVal);
	}

	@Override
	public int update(T model, String whereClause, String... whereArgs) {
		ContentValues contentValues = new ContentValues();
		SqlHelper.parseModelToContentValues(model, contentValues);
		return mDb.update(mTableName, contentValues, whereClause, whereArgs);
	}

	@Override
	public int delete(String whereClause, String... whereArgs) {
		return mDb.delete(mTableName, whereClause, whereArgs);
	}

	@Override
	public List<T> query(String selection, String[] selectionArgs) {
		return query(null, selection, selectionArgs, null);
	}

	@Override
	public List<T> query(String[] columns, String selection,String[] selectionArgs,
			String orderBy) {
		return query(columns, selection, selectionArgs, null, null, orderBy);
	}

	@Override
	public List<T> query(String[] columns, String selection,String[] selectionArgs,
			String groupBy, String having, String orderBy) {
		List<ResultSet> queryList = mDb.query(mTableName, columns, selection, selectionArgs, groupBy, having, orderBy);
		if(queryList == null || queryList.isEmpty()){
			return null;
		}
		List<T> resultList = new ArrayList<T>();
		SqlHelper.parseResultSetListToModelList(queryList, resultList, modelClazz);
		return resultList;
	}
	
	@Override
	public List<ResultSet> execQuerySQL(String sql, String... bindArgs){
		return mDb.execQuerySQL(sql, bindArgs);
	}
	
	@Override
	public boolean deleteAll() {
		return delete("1") == 1;
	}

	@Override
	public T queryFirstRecord(String selection, String... selectionArgs) {
		List<T> resultList = query(selection, selectionArgs);
		if(resultList!=null && !resultList.isEmpty()){
			return resultList.get(0);
		}else{
			return null;
		}
	}

	@Override
	public List<T> queryAll() {
		return query(null, null);
	}

	
	@Override
	public PagingList<T> pagingQuery(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, int page, int pageSize) {
		if(orderBy == null){
			orderBy = SqlHelper.getPrimaryKey(modelClazz);
		}
		
		PagingList<ResultSet> queryList = mDb.pagingQuery(mTableName, columns, selection, selectionArgs, 
				groupBy, having, orderBy, page, pageSize);
		
		if(queryList == null){
			return null;
		}
		
		PagingList<T> resultList = new PagingList<T>();
		resultList.setTotalSize(queryList.getTotalSize());
		SqlHelper.parseResultSetListToModelList(queryList, resultList, modelClazz);
		return resultList;
	}

	@Override
	public PagingList<T> pagingQuery(String selection, String[] selectionArgs, int page, int pageSize) {
		return pagingQuery(null, selection, selectionArgs, null,page,pageSize);
	}

	@Override
	public PagingList<T> pagingQuery(String[] columns, String selection,
			String[] selectionArgs, String orderBy, int page, int pageSize) {
		return pagingQuery(columns, selection, selectionArgs, null, null, orderBy, page, pageSize);
	}

	/**
	 * get current table version 
	 * @return
	 */
	private int getCurTableVersion(){
		Context ctx = mDb.getContext();
		SharedPreferences tableVersions = ctx.getSharedPreferences(PREFS_TABLE_VERSION, Context.MODE_PRIVATE);
		return tableVersions.getInt(mTableName, 1);
	}
	
	/**
	 * save table version
	 * @param t_version
	 */
	private void saveTableVersion(int t_version){
		Context ctx = mDb.getContext();
		SharedPreferences tableVersions = ctx.getSharedPreferences(PREFS_TABLE_VERSION, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = tableVersions.edit();
	    editor.putInt(mTableName, t_version);
	    editor.commit();
	}
	
	/**
	 * get table columns in createSql
	 * @param createSql
	 * @return map, key is column name, value default true means need to delete
	 */
	private  Map<String,Boolean> getTableColumnsInfo(String createSql){
		String subSql = createSql.substring(createSql.indexOf('(') + 1, createSql.lastIndexOf(')'));
		String[] columnInfos = subSql.split(",");
		Map<String,Boolean> tableInfo = new HashMap<String, Boolean>();
		
		String columnName;
		String columnInfo;
		for(int i = 0; i < columnInfos.length ; ++i){
			columnInfo = columnInfos[i].trim();
			columnName = columnInfo.substring(0, columnInfo.indexOf(' '));
			tableInfo.put(columnName.toLowerCase(), true);
		}
		
		return tableInfo;
	}
}
