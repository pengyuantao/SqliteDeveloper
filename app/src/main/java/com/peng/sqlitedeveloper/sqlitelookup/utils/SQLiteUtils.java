package com.peng.sqlitedeveloper.sqlitelookup.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peng on 2017/6/3.
 */

public class SQLiteUtils {

    private static final String TAG = "SQLiteUtils";

    /**
     * 从创建表的sql语句中，找到所有的列名
     * @param createSql
     * @return
     */
    public static List<String> getColumNameFromCreateSql(String createSql){
        int startTable = createSql.indexOf("(");
        int endTable = createSql.lastIndexOf(")");
        String curTableText = createSql.substring(startTable + 1, endTable);
        String[] split = curTableText.split(",");
        List<String> tableName = new ArrayList<>();
        for (String s : split) {
            s = s.trim();
            int spaceIndex = s.indexOf(" ");
            if (spaceIndex != -1) {
                tableName.add(s.substring(0, spaceIndex));
            } else {
                tableName.add(s);
            }
        }
        return tableName;
    }

}
