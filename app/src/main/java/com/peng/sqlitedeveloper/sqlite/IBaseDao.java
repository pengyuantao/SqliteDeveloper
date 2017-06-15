package com.peng.sqlitedeveloper.sqlite;

import java.util.List;

/**
 * the interface below is mostly design  to handle a single table itself, 
 * but if you want to do more complicated operations with more than one table,please try {@link #execQuerySQL(String, String...)}  or {@link DbSqlite}
 * 
 * @author Darcy 
 *
 * @param <T>
 */
public interface IBaseDao<T> {
	
	/**
	 * create table
	 */
	void createTable();

	/**
	 * update table, it just support add columns to table, it will check table's version if it need to update or not.
	 */
	void updateTable();
	
	/**
	 * insert an object
	 * 
	 * @param model the model to insert
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	long insert(T model);

	/**
	 * batch insert
	 * 
	 * @param dataList
	 * @return
	 */
	boolean batchInsert(List<T> dataList);

	/**
	 * update an object by the condition you set
	 * 
	 * @param model the model to update
	 * @param whereClause the optional WHERE clause to apply when updating. Passing null will update all rows.
	 * @param whereArgs You may include ?s in the where clause, which will be replaced by the values from whereArgs. The values will be bound as Strings.
	 * @return the number of rows affected , or -1 if an error occurred
	 */
	int update(T model, String whereClause, String... whereArgs);

	/**
	 * delete by condition
	 * 
	 * @param whereClause whereClause the optional WHERE clause to apply when deleting. Passing null will delete all rows.
	 * @param whereArgs whereArgs You may include ?s in the where clause, which will be replaced by the values from whereArgs. The values will be bound as Strings.
	 * @return the number of rows affected if a whereClause is passed in, 0 otherwise
	 */
	int delete(String whereClause, String... whereArgs);


	/**
	 * delete all records
	 * @return success return true, else return false
	 */
	boolean deleteAll();
	
	/**
	 * get all records in this table
	 * @return
	 */
	List<T> queryAll();
	
	/**
	 * query by condition, it will contain all columns
	 * 
	 * @param selection 
	 * @param selectionArgs 
	 * @return if exceptions happen or no match records, then return null
	 */
	List<T> query(String selection, String[] selectionArgs);

	/**
	 * query by condition
	 * @param columns
	 * @param selection
	 * @param orderBy
	 * @param selectionArgs
	 * @return if exceptions happen or no match records, then return null
	 */
	List<T> query(String[] columns, String selection, String[] selectionArgs,
                  String orderBy);

	/**
	 * query by condition
	 * @param columns
	 * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.
	 * @param groupBy A filter declaring how to group rows, formatted as an SQL GROUP BY clause (excluding the GROUP BY itself). Passing null will cause the rows to not be grouped.
	 * @param having A filter declare which row groups to include in the cursor, if row grouping is being used, formatted as an SQL HAVING clause (excluding the HAVING itself). Passing null will cause all row groups to be included, and is required when row grouping is not being used.
	 * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order, which may be unordered.
	 * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
	 * @return if exceptions happen or no match records, then return null
	 */
	List<T> query(String[] columns, String selection, String[] selectionArgs,
                  String groupBy, String having, String orderBy);
	
	
	/**
	 * query table's records by paging
	 * @param selection
	 * @param selectionArgs
	 * @return
	 */
	PagingList<T> pagingQuery(String selection, String[] selectionArgs, int page, int pageSize);
	
	/**
	 * query table's records by paging
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @return
	 */
	PagingList<T> pagingQuery(String[] columns, String selection, String[] selectionArgs, String orderBy, int page, int pageSize);
	
	/**
	 * query table's records by paging
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy 
	 * @param page first page is 1
	 * @param pageSize
	 * @return
	 */
	PagingList<T> pagingQuery(String[] columns, String selection, String[] selectionArgs,
                              String groupBy, String having, String orderBy, int page, int pageSize);
	
	/**
	 * if your query condition have only one record, this is helpful.
	 * 
	 * @return the first row of the result,or null.
	 */
	T queryFirstRecord(String selection, String... selectionArgs);
	
	/**
	 * execute raw  sql with query
	 * @param sql sql the SQL query. The SQL string must not be ; terminated
	 * @param bindArgs You may include ?s in where clause in the query, which will be replaced by the values from selectionArgs. The values will be bound as Strings.
	 * @return return result as List or null
	 */
	List<ResultSet> execQuerySQL(String sql, String... bindArgs);
}
