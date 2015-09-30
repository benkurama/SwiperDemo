package com.example.swiperdemo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.swiperdemo.MainContentAct;
import com.example.swiperdemo.R;
import com.idtechproducts.unipay.UniPayReaderMsg;

public class MainMenuFragment extends Fragment{
 // =========================================================================
 // TODO Variables
 // =========================================================================
	private MainContentAct core;
	private FragmentActivity act;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_menu, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);//
		
		act = getActivity();
		core = (MainContentAct)act;
		
		((Button)getView().findViewById(R.id.btnPaymentTest)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				core.testPayment();
			}
		});
		//
		((Button)getView().findViewById(R.id.btnMSRReview)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (core.Unipay.sendCommandReviewMSRSettings()) {
					core.prepareToSendCommand(UniPayReaderMsg.cmdReviewMSRSetting);
					core.showContent();
				}
			}
		});
		//
		((Button)getView().findViewById(R.id.btnAutoConfig)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				core.setAutoConfig();
				core.showContent();
			}
		});
		//
		((Button)getView().findViewById(R.id.btnStopAutoConfig)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				core.stopAutoConfig();
				core.showContent();
			}
		});
		//
		((Button)getView().findViewById(R.id.btnTDES)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				core.EncryptToTDES();
				core.showContent();
			}
		});
		//
		((Button)getView().findViewById(R.id.btnSerialNum)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				core.getSerialNumber();
				core.showContent();
			}
		});
		//
		((Button)getView().findViewById(R.id.btnGetVersion)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				core.getVersion();
				core.showContent();
			}
		});
		//
		((Button)getView().findViewById(R.id.btnAudioJackSet)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				core.reviewAudioJackSettings();
				core.showContent();
			}
		});
		//
		((Button)getView().findViewById(R.id.btnDetails)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				core.goDetailsPage();
			}
		});
		//
		((Button)getView().findViewById(R.id.btnSwipeHome)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				core.goHomePage();
			}
		});
		//
		((Button)getView().findViewById(R.id.btnTransfer)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				core.goTransferTest();
				core.showContent();
			}
		});
		//
		((Button)getView().findViewById(R.id.btnPaypalTest)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				core.PayPalTest();
				core.showContent();
			}
		});
	}
}
