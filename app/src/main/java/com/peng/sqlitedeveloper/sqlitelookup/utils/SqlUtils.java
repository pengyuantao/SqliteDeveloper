package com.peng.sqlitedeveloper.sqlitelookup.utils;

import com.peng.sqlitedeveloper.sqlitelookup.model.ColumnInfo;

import java.util.List;

public class SqlUtils {

	public static void resolveCreateSql(String createSql,List<ColumnInfo> columnInfoList){
		createSql = preProcessSql(createSql);
		
		String subSql = createSql.substring(createSql.indexOf('(') + 1, createSql.lastIndexOf(')'));
		String[] columnInfos = subSql.split(",");
		
		ColumnInfo columnInfo;
		String columnInfoStr;
		String upperColumnInfoStr;
		int firstBlankIndex,secBlankIndex,defaultBeginIndex,defaultEndIndex;
		String name,type,defaultValue;
		for(int i = 0; i < columnInfos.length ; ++i){
			columnInfo = new ColumnInfo();
			columnInfoStr = columnInfos[i].trim();
			firstBlankIndex = columnInfoStr.indexOf(' ');
			secBlankIndex = columnInfoStr.indexOf(' ', firstBlankIndex+1);
			secBlankIndex = secBlankIndex == -1 ? columnInfoStr.length() : secBlankIndex;
			name = columnInfoStr.substring(0, firstBlankIndex);
			type = columnInfoStr.substring(firstBlankIndex+1, secBlankIndex);
			columnInfo.setName(name);
			columnInfo.setType(type);
			upperColumnInfoStr = columnInfoStr.toUpperCase();
			defaultBeginIndex = upperColumnInfoStr.indexOf("DEFAULT");
			if(defaultBeginIndex != -1){
				defaultBeginIndex = defaultBeginIndex + 8;
				int semiIndex = columnInfoStr.indexOf("'",defaultBeginIndex);
				if(semiIndex != -1){
					defaultBeginIndex = semiIndex + 1;
					defaultEndIndex = columnInfoStr.lastIndexOf("'");
				}else{
					semiIndex = columnInfoStr.indexOf("\"", defaultBeginIndex);
					if(semiIndex != -1){
						defaultBeginIndex = semiIndex + 1;
						defaultEndIndex = columnInfoStr.lastIndexOf("\"");
					}else{
						defaultEndIndex = columnInfoStr.indexOf(' ', defaultBeginIndex);
						defaultEndIndex = defaultEndIndex == -1 ?  columnInfoStr.length() : defaultEndIndex;
					}
				}
				defaultValue = columnInfoStr.substring(defaultBeginIndex, defaultEndIndex);
				columnInfo.setDefaultValue(defaultValue);
			}
			
			
			if(upperColumnInfoStr.contains("PRIMARY KEY")){
				columnInfo.setPrimaryKey(true);
			}
			
			if(upperColumnInfoStr.contains("NOT NULL")){
				columnInfo.setNull(false);
			}
			
			if(upperColumnInfoStr.contains("UNIQUE")){
				columnInfo.setUnique(true);
			}
			columnInfoList.add(columnInfo);
		}
	}
	
	private static String preProcessSql(String sql){
		StringBuilder resultSql = new StringBuilder();
		char[] chs = sql.trim().toCharArray();
		
		boolean isText = false;
		boolean isLastBlank = false;
		for(char ch : chs){
			
			if(ch != ' '|| !isLastBlank || isText){
				resultSql.append(ch);
			}
			
			if(ch == '\"' || ch == '\''){
				isText = !isText;
			}else if(ch == ' '){
				isLastBlank = true;
			}else{
				isLastBlank = false;
			}
		}
		
		return resultSql.toString();
	}
	
}
