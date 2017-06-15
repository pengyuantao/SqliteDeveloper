package com.peng.sqlitedeveloper.sqlitelookup.utils;

import android.content.Context;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

public class AppUtils {
	/**
	 * dip转px
	 * 
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int dipToPx(Context context, int dipValue) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, context.getResources()
				.getDisplayMetrics());
	}
	
	/**
	 * sp转px
	 * 
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int spToPx(Context context, int spValue) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources()
				.getDisplayMetrics());
	}
	
	/**
     * 
     * 获取屏幕大小
     * 
     * @param context
     * @return [0] width, [1] height
     */
    public static int[] getScreenSize(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return new int[] {
                display.getWidth(), display.getHeight()
        };
    }
}
