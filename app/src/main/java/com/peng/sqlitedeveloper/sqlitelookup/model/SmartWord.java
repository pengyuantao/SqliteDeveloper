package com.peng.sqlitedeveloper.sqlitelookup.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by pyt on 2017/6/1.
 */

public class SmartWord implements Serializable,Comparable{

    public String word;

    public int time;

    public SmartWord(String word, int time) {
        this.word = word;
        this.time = time;
    }

    public SmartWord() {
    }

    public SmartWord(String word) {
        this.word = word;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        SmartWord o1 = (SmartWord) o;
        return o1.time-this.time;
    }

    @Override
    public String toString() {
        return "SmartWord{" +
                "word='" + word + '\'' +
                ", time=" + time +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SmartWord) {
            SmartWord obj1 = (SmartWord) obj;
            return this.word.equals(obj1.word);
        }
        return false;
    }
}
