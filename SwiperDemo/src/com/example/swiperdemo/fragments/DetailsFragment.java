package com.example.swiperdemo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.swiperdemo.MainContentAct;
import com.example.swiperdemo.R;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;

public class DetailsFragment extends Fragment{
 // =========================================================================
 // TODO Variables
 // =========================================================================
	private MainContentAct core;
	private FragmentActivity act;
	
	private ViewPager pager;
	private PageIndicator indicator;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.fragment_details, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//
		act = getActivity();
		core = (MainContentAct)act;
		//
		act.setTitle("Details Tab");
		//
		pager = (ViewPager)act.findViewById(R.id.pager);
		indicator = (TabPageIndicator)act.findViewById(R.id.indicator);
		
		pager.setAdapter(new DetailsPageAdapter(act, indicator));
		indicator.setViewPager(pager);
		//
	}
	
}
