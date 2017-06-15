package com.peng.sqlitedeveloper.sqlitelookup.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.peng.sqlitedeveloper.sqlitelookup.model.SmartWord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by pyt on 2017/6/1.
 */

public class SQLKeyWordUtils {

    public static final String FIEL_SQL_WORD_NAME = "sql_word.pyt";


    private static ArrayList<SmartWord> readSQLWordListFromCacheFile(Context context) {
        ObjectInputStream ois = null;
        File cacheFile = null;
        try {
            cacheFile = new File(context.getCacheDir(), FIEL_SQL_WORD_NAME);
            ois = new ObjectInputStream(new FileInputStream(cacheFile));
            return (ArrayList<SmartWord>) ois.readObject();
        }catch (Exception e){
            if (cacheFile != null && cacheFile.exists()) {
                cacheFile.delete();
            }
            return readSQLWordListFromAssets(context);
        }
        finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ArrayList<SmartWord> readSQLWordList(@NonNull Context context) {
        File file = new File(context.getCacheDir(), FIEL_SQL_WORD_NAME);
        if (file.exists()) {
            return readSQLWordListFromCacheFile(context);
        } else {
            return readSQLWordListFromAssets(context);
        }
    }

    private static ArrayList<SmartWord> readSQLWordListFromAssets(@NonNull Context context) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(context.getAssets().open("sql_keyword.txt")));
            ArrayList<SmartWord> wordArrayList = new ArrayList<>();
            String line = null;
            while ((line = br.readLine()) != null) {
                wordArrayList.add(new SmartWord(line));
            }
            br.close();
            return wordArrayList;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return null;
    }

    public static void writeSQLWordList(@NonNull Context context, ArrayList<SmartWord> words) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(new File(context.getCacheDir(), FIEL_SQL_WORD_NAME)));
            oos.writeObject(words);
            oos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

}
