package com.example.swiperdemo.fragments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import IDTech.MSR.XMLManager.StructConfigParameters;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.swiperdemo.MainContentAct;
import com.example.swiperdemo.R;
import com.example.swiperdemo.objects.ProfileDatabase;
import com.idtechproducts.unipay.UniPayReader;
import com.idtechproducts.unipay.UniPayReaderMsg;

public class HomePageFragment extends Fragment{
 // =========================================================================
 // TODO Variables
 // =========================================================================
	private MainContentAct core;
	private FragmentActivity act;
	private Menu UniMenu;
	//
	TextView test;
	//
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		
		return inflater.inflate(R.layout.fragment_home, container, false);
	} 
 // =========================================================================
 // TODO Overrides
 // =========================================================================
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//
		act = getActivity();
		core = (MainContentAct)act;
		core.setTitle("Swiper Page");
		//
		((EditText)getView().findViewById(R.id.etStatusInfo)).setKeyListener(null);
		setHasOptionsMenu(true);
		//
		core.InitializeWidgets();
		core.InitializeReader();
	}
 
 @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	 	inflater.inflate(R.menu.menu_swipe, menu);
	 	UniMenu = menu;
	 	menu.findItem(R.id.btnSwipeAction).setEnabled(false);
		super.onCreateOptionsMenu(menu, inflater);
	}
 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.btnSwipeAction:
			core.startSwipeCard();
			break;

		default:
			break;
		}
	return super.onOptionsItemSelected(item);
}
	
	public Menu getMenu(){
		return UniMenu;
	}
 // =========================================================================
 // TODO Main Functions
 // =========================================================================

 // =========================================================================
 // TODO Final
}
