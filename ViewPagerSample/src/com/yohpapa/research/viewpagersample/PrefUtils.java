package com.yohpapa.research.viewpagersample;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {

	public static int getInt(Context context, int key, int defValue) {
		if(context == null)
			return defValue;
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if(pref == null)
			return defValue;
		
		return pref.getInt(context.getString(key), defValue);
	}
	
	public static boolean setInt(Context context, int key, int value) {
		if(context == null)
			return false;
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if(pref == null)
			return false;
		
		SharedPreferences.Editor editor = pref.edit();
		if(editor == null)
			return false;
		
		editor.putInt(context.getString(key), value);
		return editor.commit();
	}
}
