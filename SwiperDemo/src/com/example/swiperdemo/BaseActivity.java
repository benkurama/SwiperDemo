package com.example.swiperdemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;

import com.example.swiperdemo.fragments.MainMenuFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class BaseActivity extends SlidingFragmentActivity {
	 // =========================================================================
	 // TODO Variables
	 // =========================================================================
	private String PageTitles;
	protected Fragment oFrag;
	 // =========================================================================
	 // TODO Constructors
	 // =========================================================================
	public BaseActivity(String title){
		PageTitles = title;
	}
	 // =========================================================================
	 // TODO Overrides
	 // =========================================================================
	@SuppressLint("Recycle")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		setTitle(PageTitles);
		setBehindContentView(R.layout.frame_menu);
		
		if(savedInstanceState == null){
			FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
			oFrag = new MainMenuFragment();
			ft.replace(R.id.frame_menu, oFrag);
			ft.commit();
		} else {
			oFrag = getSupportFragmentManager().findFragmentById(R.id.frame_menu);
		}
		//
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		//
	}
	
	@Override
	public void onBackPressed() {
		
//		final AlertDialog.Builder dial = new AlertDialog.Builder(this);
//		dial.setTitle("Leave the app?");
//		dial.setPositiveButton("Yes", new OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				exit();
//			}
//		});
//		dial.setNegativeButton("No", null);
//		dial.show();
		
		super.onBackPressed();
	}
	
	private void exit(){
		this.finish();
	}
	 // =========================================================================
	 // TODO Final
}
