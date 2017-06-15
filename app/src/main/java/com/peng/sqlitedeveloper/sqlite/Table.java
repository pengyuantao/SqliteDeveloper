package com.peng.sqlitedeveloper.sqlite;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * model annotation corresponding to table
 * 
 * @author Darcy
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.TYPE) 
public @interface Table {
	
	/**
	 * table name
	 * @return
	 */
   public String name();
   
   /**
    * table version, if this value diff from the previous one, it will update this table
    * @return
    */
   public int version() default 1;
   
   /**
    * model field annotation corresponding to table column
    * @author Darcy
    */
   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.FIELD)
   public @interface Column {
   	
   	public static final String TYPE_INTEGER = "INTEGER";
   	public static final String TYPE_LONG = "NUMERIC";
   	public static final String TYPE_STRING = "TEXT";
   	public static final String TYPE_TIMESTAMP ="TEXT";
   	public static final String TYPE_BOOLEAN = "INTEGER";
   	public static final String TYPE_FLOAT = "REAL";
   	public static final String TYPE_DOUBLE = "REAL";
   	public static final String TYPE_BLOB = "BLOB";
   	
   	public static final class DEFAULT_VALUE{
   		public static final String TRUE = "1";
   		public static final String FALSE = "0";
   		public static final String CURRENT_TIMESTAMP = "(datetime(CURRENT_TIMESTAMP,'localtime'))";
   	}
   	
   	public String name();
   	
   	public String type();
   	
   	public String defaultValue()default "null";
   	
   	public boolean isPrimaryKey()default false;
   	
   	public boolean isNull()default true;
   	
   	public boolean isUnique()default false;
   }

}
