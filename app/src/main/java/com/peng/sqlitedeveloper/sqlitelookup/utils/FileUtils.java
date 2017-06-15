package com.peng.sqlitedeveloper.sqlitelookup.utils;

import android.text.TextUtils;

import java.io.File;

/**
 * Created by pyt on 2017/6/1.
 */

public class FileUtils {

    /**
     * 判断当前文件是否存在
     * @param path
     * @return
     */
    public static boolean exists(String path){
        return TextUtils.isEmpty(path) ? false : exists(new File(path));
    }

    public static boolean exists(File file) {
        return file == null ? false : file.exists();
    }


}
