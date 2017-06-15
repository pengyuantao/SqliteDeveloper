package com.peng.sqlitedeveloper.sqlite;


/**
 * use this factory to create your sqlite data access object
 * @author Darcy yeguozhong@yeah.net
 */
public class DaoFactory {
	
	/**
	 * call this method to new a GenericDao
	 * @param context
	 * @param modelClazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> IBaseDao<T> createGenericDao(DbSqlite db, Class<?> modelClazz){
		return new GenericDao<T>(db,modelClazz);
	}  

}
