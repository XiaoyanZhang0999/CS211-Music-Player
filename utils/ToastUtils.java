package com.ldw.music.utils;

import android.content.Context;
import android.widget.Toast;


public class ToastUtils {

	private static Toast mToast;
	public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
	public static final int LENGTH_LONG = Toast.LENGTH_LONG;
	
	public static void showToast(Context context, String msg) {
		// if (mToast != null) {
		// mToast.cancel();
		// }
		// mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		
		clearToast();
		
		if (mToast == null) {
			mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		} else {
			
			mToast.setText(msg);
		}
		
		mToast.show();
	}
	
	public static void showToast(Context context, int res) {
		// if (mToast != null) {
		// mToast.cancel();
		// }
		// mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		
		clearToast();
		
		if (mToast == null) {
			mToast = Toast.makeText(context, res, Toast.LENGTH_SHORT);
		} else {
			
			mToast.setText(res);
		}
		
		mToast.show();
	}

	public static void showToast(Context context, String msg, int duration) {
		// if (mToast != null) {
		// mToast.cancel();
		// }
		// mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		
		clearToast();
		
		if (mToast == null) {
			mToast = Toast.makeText(context, msg, duration);
		} else {
			
			mToast.setText(msg);
		}
		
		mToast.show();
	}

	public static void showToast(Context context, String msg, int duration,
                                 int gravity, int xOffset, int yOffset) {
		
		clearToast();

		if (mToast == null) {
			mToast = Toast.makeText(context, msg, duration);
			
		} else {
			mToast.setText(msg);
		}
		mToast.setGravity(gravity, xOffset, yOffset);
		mToast.show();

	}

	public static void clearToast() {

		if (mToast != null) {
			mToast.cancel();
			mToast = null;
		}

	}

}
