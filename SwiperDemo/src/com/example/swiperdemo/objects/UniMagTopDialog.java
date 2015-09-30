package com.example.swiperdemo.objects;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;

public class UniMagTopDialog extends Dialog{

	public UniMagTopDialog(Context context) {
		super(context);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_SEARCH)) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		if ((keyCode == KeyEvent.KEYCODE_BACK||KeyEvent.KEYCODE_HOME==keyCode||KeyEvent.KEYCODE_SEARCH==keyCode)){
 			return false;
		}
		
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		
		if ((keyCode == KeyEvent.KEYCODE_BACK||KeyEvent.KEYCODE_HOME==keyCode||KeyEvent.KEYCODE_SEARCH==keyCode)){
    		 
			return false;
		}
		
		return super.onKeyMultiple(keyCode, repeatCount, event);
	}

	
}
