package com.example.swiperdemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import IDTech.MSR.XMLManager.StructConfigParameters;
import IDTech.MSR.uniMag.UniMagTools.uniMagReaderToolsMsg;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.IdTech.TDES.CreditCardInfo;
import cn.com.IdTech.TDES.SwipeDataInterface;

import com.example.swiperdemo.objects.CardData;
import com.example.swiperdemo.objects.ProfileDatabase;
import com.example.swiperdemo.objects.UniMagTopDialog;
import com.idtechproducts.unipay.Common;
import com.idtechproducts.unipay.UniPayReader;
import com.idtechproducts.unipay.UniPayReaderMsg;
//import com.paypal.android.MEP.CheckoutButton;
//import com.paypal.android.MEP.PayPal;
//import com.paypal.android.MEP.PayPalAdvancedPayment;
//import com.paypal.android.MEP.PayPalInvoiceData;
//import com.paypal.android.MEP.PayPalPayment;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

public class HomeAct extends Activity implements UniPayReaderMsg, uniMagReaderToolsMsg{
	// ---------------------------------------------------------------------
	// TODO VARIABLES
	// ---------------------------------------------------------------------
	private UniPayReader UniMagReader = null;
	private UniMagTopDialog dlgSwipeTopShow = null, dlgPayment = null;
	private StructConfigParameters profile = null;
	
	private Handler handler = new Handler();
	
	private MenuItem CaptureDatabase = null, StartAutoConfig = null;
	
	private ProfileDatabase profileDatabase = null;
	
	private TextView tvCaption, tvConnectStatus, tvHello;
	private EditText etTopInfo, etBottomInfo;
	private Button btnSwipStart, btnSwipeCancel, btnPay;
	
	private String strStatus = null;
	private String ProgressInfoStr = ""; 
	private String popupDialogMsg = null;
	private byte[] MSRdata = null; 
	private byte[] MSRdata2 = null; 
	private String strMsrData = null;
	
	private static final int CAPTURE_DATABASE = Menu.FIRST;
	private static final int START_AUTO_CONFIG = Menu.FIRST + 1;
	
	private boolean isUseAutoConfigProfileChecked = false;
	private boolean isReaderConnected = false;
	private boolean autoconfig_running = false;	
	private boolean isWaitingForCommandResult = false;
	private boolean enableSwipeCard = false;
	
	private int percent = 0;
	private long beginTime = 0;
	private long beginTimeEachCmd = 0;
	private long beginTimeOfAutoConfig = 0;
	
	
	private boolean _paypalLibraryInit = false;
	
	// ---------------------------------------------------------------------
	// TODO LIFE CYCLES
	// ---------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main );
		
		profileDatabase = new ProfileDatabase(this);
		profileDatabase.initializedDB();
		isUseAutoConfigProfileChecked = profileDatabase.getIsUseAutoConfigProfile();
		
		InitializeUI();
		InitializeReader();
//		
		String Manufature = UniMagReader.getInfoManufacture();
		String Model = UniMagReader.getInfoModel();
		String SDKVersion = UniMagReader.getSDKVersionInfo();
		String OSVersion = android.os.Build.VERSION.RELEASE;
		
		etTopInfo.setText("Phone: "+Manufature+"\nModel: "+Model+"\nSDK Ver: "+SDKVersion+"\nOS Ver: "+OSVersion);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		
		//initWithAppID();
		
	}
	 
	@Override
	protected void onPause() {
		if(UniMagReader != null)
			UniMagReader.stopSwipeCard();
		
		hideSwipeTopDialog();
		
		super.onPause();
	}

	@Override
	protected void onResume() {
		//
		isWaitingForCommandResult = false;
		super.onResume();
	}
	
	
	@Override
	protected void onDestroy() {
		//
		UniMagReader.release();
		profileDatabase.closeDB();
		super.onDestroy();
	}

	// ---------------------------------------------------------------------
	// TODO OVERRIDES
	// ---------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//
		//getMenuInflater().inflate(R.menu.main, menu);
		
		CaptureDatabase = menu.add(0,CAPTURE_DATABASE, Menu.NONE, "Capture Database to SD");
		//CaptureDatabase.setEnabled(true);
		StartAutoConfig = menu.add(0, START_AUTO_CONFIG, Menu.NONE, "Start Auto Config");
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case CAPTURE_DATABASE:
			captureDatabaseToSD();
			break;

		case START_AUTO_CONFIG:
			
			startAutoConfig();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	// ---------------------------------------------------------------------
	// TODO MAIN FUNCIONS
	// ---------------------------------------------------------------------
	private void InitializeUI(){
		//
		tvCaption = (TextView)findViewById(R.id.tvCaption);
		tvConnectStatus = (TextView)findViewById(R.id.tvConnectStatus);
		etTopInfo = (EditText)findViewById(R.id.etInfoTop);
		etBottomInfo = (EditText)findViewById(R.id.etInfoBottom);
		btnSwipStart = (Button)findViewById(R.id.btnSwipStart);
		btnSwipeCancel = (Button)findViewById(R.id.btnSwipeCancel);
		btnPay = (Button)findViewById(R.id.btnPay);
		
		tvCaption.setText("MSR Data");
		tvConnectStatus.setText("Disconnected");
		
		
		btnSwipStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//
				if(UniMagReader != null)
				{
					if(!isWaitingForCommandResult)
					{
						if(UniMagReader.sendCommandEnableSwipingMSRCard())
						{
							prepareToSendCommand(UniPayReaderMsg.cmdEnableSwipingMSRCard);
						}
					}
				}
			}
		});
		
		btnSwipeCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//
				/*UniMagReader.stopSwipeCard();
				//
				if(UniMagReader.sendCommandCancelSwipingMSRCard()){
					prepareToSendCommand(UniPayReaderMsg.cmdCancelSwipingMSRCard);
				} else {
					Toast.makeText(getBaseContext(), "Cannot cancel the swipe", Toast.LENGTH_SHORT).show();
				}*/
				MSRtoAES();
				
			}
		});
		
		btnPay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//TestBuy();
				//showPayPalButton();
				//STripeCard();
				//showPaymentDialog();
			}
		});
		
		//
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		///
		//SwipeCard swipe = new SwipeCard("");
		
		///
	}
	
	private void InitializeReader(){
		//
		if(UniMagReader!=null){
			UniMagReader.unregisterListen();
			UniMagReader.release();
			UniMagReader = null;
		}
		
		///
		UniMagReader = new UniPayReader(this, this);
		
		UniMagReader.setVerboseLoggingEnable(true);
		UniMagReader.registerListen();
		//
		String fileNameWithPath = getXMLFileFromRaw("idt_unimagcfg_default.xml");
		//
		if(!isFileExist(fileNameWithPath))
			fileNameWithPath = null;
		//
		
		
		if(isUseAutoConfigProfileChecked){
			//
			if(profileDatabase.updateProfileFromDB()){
				//
				this.profile = profileDatabase.getProfile();
				handler.post(doConnectUsingProfile);
				Toast.makeText(this, "AutoConfig from DB has been Loaded", Toast.LENGTH_SHORT).show();
			}
		} else {
			new Ass().execute(fileNameWithPath);
		}
		///
	}
	
	private void prepareToSendCommand(int cmdID){
		
		isWaitingForCommandResult = true;
		 switch (cmdID) {
		 //MSR List
		case UniPayReaderMsg.cmdEnableSwipingMSRCard:
			strStatus = "To Enable Swiping MSR Card, wait for response";
			break;
		case UniPayReaderMsg.cmdCancelSwipingMSRCard:
			strStatus = " To Cancel Swiping MSR Card, wait for response.";
		case 1002:
			strStatus = "  To Set MSR AES Setting, wait for response.";
			break;
		default:
			break;
		}
		 //
		 MSRdata = null;
		 handler.post(doUpdateStatus);
	}
	
	private void TestBuy(){
		//
		
//		PayPal pp = PayPal.getInstance();
//		
//		if (pp == null){
//			Toast.makeText(this, "Init", Toast.LENGTH_SHORT).show();
//		} else {
//			Toast.makeText(this, "Not", Toast.LENGTH_SHORT).show();
//		}
//		
//		pp = PayPal.initWithAppID(this, "APP-80W284485P519543T", PayPal.ENV_SANDBOX);
//		pp.setLanguage("en_US");
//		
//		pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);
//		
//		pp.setShippingEnabled(true);
		
		////
		

		
		
		/*SDKProperties.setHostAddress("pilot-payflowpro.paypal.com");
		SDKProperties.setHostPort(443);
		SDKProperties.setTimeOut(45);
		
		SDKProperties.setStackTraceOn(true);
		
		UserInfo user = new UserInfo("benkurama", "benkurama", "PayPal", "Redfoot123_");
		
		PayflowConnectionData connection = new PayflowConnectionData();
		
		Invoice inv = new Invoice();
		
		
		
		Currency amt = new Currency(new Double(1.00), "USD");
		
		inv.setAmt(amt);
		inv.setPoNum("PO123456");
		inv.setInvNum("INV12345");
        inv.setCustRef("CUSTREF1");
        inv.setMerchDescr("Merchant Descr");
        inv.setMerchSvc("Merchant Svc");
        inv.setComment1("Comment1");
        inv.setComment2("Comment2");
        
        BillTo bill = new BillTo();
        
        bill.setFirstName("Joe & Bob");
        bill.setLastName("Smith");
        bill.setCompanyName("Joe's Hardware");
        
        inv.setBillTo(bill);
        
        ShipTo ship =  new ShipTo();
        
        ship = bill.copy();
        
        inv.setShipTo(ship);
        
        CreditCard cc = new CreditCard("4511251801130645", "0619");
        cc.setCvv2("123");
        
        CardTender card = new CardTender(cc);
        
        //String strRequestID = PayflowUtility.getRequestId();
        
        SaleTransaction trans = new SaleTransaction(user, connection, inv, card, "15915919");
        
        trans.setVerbosity("HIGH");
        
        ClientInfo clinfo = new ClientInfo();
        
        trans.setClientInfo(clinfo);
        
        Response resp = trans.submitTransaction();
        */
//        if (resp != null){
//        	//
//        	TransactionResponse tranRes = resp.getTransactionResponse();
//        	
//        	//Toast.makeText(this, tranRes.getResult(), Toast.LENGTH_SHORT).show();
//        } else {
//        	Toast.makeText(this, "Response is null", Toast.LENGTH_SHORT).show();
//        }
		
	}
	
//	private void initWithAppID(){
//		//
//		PayPal ppObj = PayPal.initWithAppID(this.getBaseContext(), "APP-80W284485P519543T", PayPal.ENV_SANDBOX);
//		
//		CheckoutButton launchPayPalButton = ppObj.getCheckoutButton(this, PayPal.BUTTON_278x43, CheckoutButton.TEXT_PAY);
//		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//		params.bottomMargin = 10;
//		
//		launchPayPalButton.setLayoutParams(params);
//		launchPayPalButton.setOnClickListener(click);
//		
//		((RelativeLayout)findViewById(R.id.rlLayout)).addView(launchPayPalButton);
//	}
//	
//	private OnClickListener click = new OnClickListener() {
//		
//		@Override
//		public void onClick(View v) {
//			
//			onPayment();
//		}
//	};
	
	
//	private void onPayment(){
//		
//		
//		
//		PayPalPayment newPayment = new PayPalPayment();
//		newPayment.setSubtotal(new BigDecimal(10.f));
//		newPayment.setCurrencyType("USD");
//		newPayment.setRecipient("alvin.sison@redfoottech.com");
//		newPayment.setMerchantName("RedFoot");
//		Intent paypalIntent = PayPal.getInstance().checkout(newPayment, this);
//		this.startActivityForResult(paypalIntent, 1);
//	}
	
	private void STripeCard(String AccounNum, String month, String year, boolean real){
		//
		
		String accountNumber = "4242424242424242";
		String Month = "12";
		String Year = "2020";
		
		if (real){
			accountNumber = AccounNum;
			Month = month;
			Year = year;
		}
		new Puss().execute(accountNumber, Month, Year);
	}
	
	// ---------------------------------------------------------------------
	// TODO SUB FUNCIONS
	// ---------------------------------------------------------------------
	@SuppressLint("SdCardPath")
	private void captureDatabaseToSD(){
		try{
			File sd = Environment.getExternalStorageDirectory();
			//File data = Environment.getDataDirectory();
			
			if(sd.canWrite()){
				String currentDBPath = "/data/data/" +getPackageName()+ "/databases/REDFOOTTECH.AutoConfig";
				String backupDBPath = "RedfootCopy.db";
				
				File currentDB = new File(currentDBPath);
				File backupDB = new File(sd, backupDBPath);
				
				if(currentDB.exists()){
					@SuppressWarnings("resource")
					FileChannel src = new FileInputStream(currentDB).getChannel();
					@SuppressWarnings("resource")
					FileChannel dst = new FileOutputStream(backupDB).getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();
					dst.close();
				}
				Toast.makeText(this, "Capture DB Success!", Toast.LENGTH_SHORT).show();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private String getXMLFileFromRaw(String fileName ){
		//the target filename in the application path
		String fileNameWithPath = null;
		fileNameWithPath = fileName;
	
		try {
			InputStream in = getResources().openRawResource(R.raw.idt_unimagcfg_default);
			int length = in.available();
			byte [] buffer = new byte[length];
			in.read(buffer);    	   
			in.close();
			deleteFile(fileNameWithPath);
			FileOutputStream fout = openFileOutput(fileNameWithPath, MODE_PRIVATE);
			fout.write(buffer);
			fout.close();
    	   
			// to refer to the application path
			File fileDir = this.getFilesDir();
			fileNameWithPath = fileDir.getParent() + java.io.File.separator + fileDir.getName();
			fileNameWithPath += java.io.File.separator+"idt_unimagcfg_default.xml";
	   	   
		} catch(Exception e){
			e.printStackTrace();
			fileNameWithPath = null;
		}
		return fileNameWithPath;
	}
	
	private boolean isFileExist(String path){
		
		if(path == null)
			return false;
		
		File file = new File(path);
		if(!file.exists())
			return false;
			
		return true;
	}
	
	private void startAutoConfig(){
		//
		String fileNameWithPath = getXMLFileFromRaw("idt_unimagcfg_default.xml");
		
		if(!isFileExist(fileNameWithPath)) fileNameWithPath = null;
		
		boolean startActRet = UniMagReader.startAutoConfig(fileNameWithPath, true);
		
		if(startActRet){
			
			ProgressInfoStr = null;
			handler.post(doUpdateAutoConfigProgressInfo);
			percent = 0;
			beginTime =  getCurrentTime();
			autoconfig_running = true;
		}
	}
	
	private long getCurrentTime(){
		return System.currentTimeMillis();
	}
	
	private String getTimeInfo(long timeBase){
		int time = (int)(getCurrentTime()-timeBase)/1000;
		int hour = (int) (time/3600);
		int min = (int) (time/60);
		int sec= (int) (time%60);
		return  hour+":"+min+":"+sec;
	}
	
	private String getHexStringFromBytes(byte []data)
    {
		if(data.length<=0) 
			return null;
		StringBuffer hexString = new StringBuffer();
		String fix = null;
		for (int i = 0; i < data.length; i++) {
			fix = Integer.toHexString(0xFF & data[i]);
			if(fix.length()==1)
				fix = "0"+fix;
			hexString.append(fix);
		}
		fix = null;
		fix = hexString.toString();
		return fix;
    }
	
	private void showSwipeTopDialog(){
		//
		try {
			if(dlgSwipeTopShow == null){
				dlgSwipeTopShow = new UniMagTopDialog(this);
			}
			
			dlgSwipeTopShow.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, 
					WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
			dlgSwipeTopShow.setTitle("UniPay");
			dlgSwipeTopShow.setContentView(R.layout.dlg_swipe_top_view);
			((TextView)dlgSwipeTopShow.findViewById(R.id.tvViewInfo)).setText(popupDialogMsg);
			((Button)dlgSwipeTopShow.findViewById(R.id.btnCancel)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					cancelSwipeFunction();
				}
			});
			
			dlgSwipeTopShow.show();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showPaymentDialog(final String acctNum,final String month,final String year, String acctName){
		//
		try{
			if(dlgPayment == null)
				dlgPayment = new UniMagTopDialog(this);
			//
			dlgPayment.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
					WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
			dlgPayment.setContentView(R.layout.dlg_payment);
			//
			dlgPayment.setTitle("Payment Method");
			
			//
			((TextView)dlgPayment.findViewById(R.id.tvAccountNum)).setText(acctNum);
			((TextView)dlgPayment.findViewById(R.id.tvAccountName)).setText(acctName);
			((TextView)dlgPayment.findViewById(R.id.tvValidMonth)).setText(month);
			((TextView)dlgPayment.findViewById(R.id.tvValidYear)).setText(year);
			//
			
			((Button)dlgPayment.findViewById(R.id.btnPayCancel)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dlgPayment.dismiss();
				}
			});
			((Button)dlgPayment.findViewById(R.id.btnPayNow)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					STripeCard(acctNum, month, year, true);
					//((Button)dlgPayment.findViewById(R.id.btnPayNow)).setEnabled(false);
					dlgPayment.dismiss();
				}
			});
			//
			dlgPayment.show();
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void cancelSwipeFunction(){
		//
		UniMagReader.stopSwipeCard();
		//
		if(UniMagReader.sendCommandCancelSwipingMSRCard()){
			prepareToSendCommand(UniPayReaderMsg.cmdCancelSwipingMSRCard);
		} else {
			Toast.makeText(getBaseContext(), "Cannot cancel the swipe", Toast.LENGTH_SHORT).show();
		}
		//
		if(dlgSwipeTopShow != null){
			dlgSwipeTopShow.dismiss();
		}
	}
	
	private void hideSwipeTopDialog(){
		try {
			if (dlgSwipeTopShow != null) {
				dlgSwipeTopShow.hide();
				dlgSwipeTopShow.dismiss();
				dlgSwipeTopShow = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void MSRtoAES(){
		//
		UniMagReader.sendCommandSetCommonSetting((byte)0x4c, (byte)0x32);
		//UniMagReader.sendCommandSetCommonSetting((byte)0x4c, (byte)0x31);
		prepareToSendCommand(1002);
	}
	// ---------------------------------------------------------------------
	// TODO RUNNABLE FUNCTIONS
	// ---------------------------------------------------------------------
	
	private class Puss extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			Stripe.apiKey = "sk_test_cM87J66WkEV0V8J5svBBtBTg";
			Charge charge = null;
			
			 Map<String, Object> chargeMap = new HashMap<String, Object>();
		        chargeMap.put("amount", 150);
		        chargeMap.put("currency", "usd");
		        
		        
		        Map<String, Object> cardMap = new HashMap<String, Object>();
		        
//		        cardMap.put("number", params[0]);
//		        cardMap.put("exp_month", Integer.parseInt(params[1]));
//		        cardMap.put("exp_year", Integer.parseInt(params[2]));
		        
		        cardMap.put("number", "4242424242424242");
		        cardMap.put("exp_month", 12);
		        cardMap.put("exp_year", 2020);
		        
		        
//		        cardMap.put("number", "4215620872971401");
//		        cardMap.put("exp_month", 10);
//		        cardMap.put("exp_year", 2016);
		        
		        chargeMap.put("card", cardMap);
		        String Result = "";
		        try {
		           charge = Charge.create(chargeMap);
		           
		           //Assert.assertEquals("123", "123");
		           if(charge.getAmount() != null)
			        	Result = String.valueOf(charge.getAmount());
		           
		        } catch (StripeException e) {
		            e.printStackTrace();
		            Result = e.getMessage();
		        }
		        
			return Result;
		}
		//

		@Override
		protected void onPostExecute(String result) {
			//
			Toast.makeText(getBaseContext(), "Transaction Success!: "+result, Toast.LENGTH_SHORT).show();
			etTopInfo.setText(result);
			super.onPostExecute(result);
		}
		
		
	}
	
	private Runnable doUpdateStatus = new Runnable() {
		@Override
		public void run() {
			
			try {
				etTopInfo.setText(strStatus);
				tvCaption.setText("Command Info");
				//
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(MSRdata!=null)
    		{
            StringBuffer hexString = new StringBuffer();
            
            hexString.append("<");
            String fix = null;
            for (int i = 0; i < MSRdata.length; i++) {
            	fix = Integer.toHexString(0xFF & MSRdata[i]);
            	if(fix.length()==1)
            		fix = "0"+fix;
                hexString.append(fix);
                if((i+1)%4==0&&i!=(MSRdata.length-1))
                	hexString.append(' ');
            }
            hexString.append(">");
            etBottomInfo.setText(hexString.toString());
            //handler.post(doUpdateTVS1);
    		}
    		else
    			etBottomInfo.setText("");
			
		}
	};
	
	private Runnable doUpdateAutoConfigProgressInfo = new Runnable() {
		@Override
		public void run() {
			try{
				//
				if(strStatus != null){
					tvCaption.setText("Command Info");
					etTopInfo.setText(strStatus);
				}
				else 
					etTopInfo.setText("");
				//
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}; 
	
	
	private Runnable doConnectUsingProfile = new Runnable() {
		@Override
		public void run() {
			if(UniMagReader != null){
				try{
					Thread.sleep(1000);
				}catch(Exception e){
					e.printStackTrace();
				}
				UniMagReader.connectWithProfile(profile);
			}
		}
	};
	
	private Runnable doShowSwipeToDlg = new Runnable() {
		@Override
		public void run() {
			showSwipeTopDialog();
		}
	};
	
	
	private Runnable doHideSwipeTopDlg = new Runnable() {
		@Override
		public void run() {
			hideSwipeTopDialog();
		}
	};
	
	private Runnable doUpdateTVS1 = new Runnable() {
		//
		String strex = "";
		SwipeDataInterface sData = new SwipeDataInterface();
		CreditCardInfo cInfo = new CreditCardInfo();
		
		String strKey = new String("0123456789ABCDEFFEDCBA9876543210");
		//String strKey = new String("17101011105578700000");
		String strDecyptlog = "";
		//
		
		//
		@Override
		public void run() {
			//
			
			etBottomInfo.setText("A");
			strDecyptlog = "";
			//Intent intent = new Intent();
			
			try {
				StringBuffer hexString = new StringBuffer();
				etBottomInfo.setText("B");
				etTopInfo.setText(strMsrData);
				long xx = System.currentTimeMillis();
				// -----------------------------------------
				try {
					etBottomInfo.setText("C");
					hexString.append("<");
					String fix = null;
					for (int i = 0; i < MSRdata.length; i++) {
						fix = Integer.toHexString(0xFF & MSRdata[i]);
						if(fix.length() == 1){
							fix = "0"+fix;
						}
						hexString.append(fix);
						if((i+1)%4 == 0 && i !=(MSRdata.length - 1)){
							hexString.append(' ');
						}
					}
					hexString.append(">");
					
//					CardData card = new CardData(MSRdata);
//					
//					etBottomInfo.setText(card.toString());
					
					//cInfo = sData.getCreditCard(MSRdata, strKey);
					//hexString.append(cInfo.toString());
					
					
				} /*catch (InvalidDecryptionKeyException e) {
					strex = "<InvalidDecriptionKeyException>:" + e.toString();
					etBottomInfo.setText(strex);
				} catch (UnknownDecryptionException e) {
					strex = "<UnknowDecryptionException>:" + e.toString();
					etBottomInfo.setText(strex);
				} catch (InvalidDataException e) {
					strex = "<InvalidDataException>:" + e.toString();
					etBottomInfo.setText(strex);
				} */
				catch (Exception e) {
					// TODO: handle exception
				}
				
				
				// -----------------------------------------
			} catch (Exception e) {
				// TODO: handle exception
			}
			//
		}
	};
	
	private Runnable doShowPayment = new Runnable() {
		
		@Override
		public void run() {
			//
			etTopInfo.setText(strMsrData);
//			CardData card = new CardData(MSRdata);
//
//			HashMap<String, String> details = card.getDetails();
//			showPaymentDialog(details.get("accountnumber"), details.get("month"), details.get("year"), details.get("accountname"));
		}
	};
	
	// ---------------------------------------------------------------------
	// TODO IMPLEMENTATIONS
	// ---------------------------------------------------------------------
	@Override
	public boolean getUserGrant(int type, String msg) {
		//Toast.makeText(this, "user grant", Toast.LENGTH_SHORT).show();
		
		switch(type)
		{
		case UniPayReaderMsg.typeToPowerupUniPay:
			//pop up dialog to get the user grant
			//Toast.makeText(this,"getUserGrant()1", Toast.LENGTH_SHORT).show();
			break;
		case UniPayReaderMsg.typeToUpdateXML:
			//pop up dialog to get the user grant
			//Toast.makeText(this,"getUserGrant()2", Toast.LENGTH_SHORT).show();
			break;
		case UniPayReaderMsg.typeToOverwriteXML:
			//pop up dialog to get the user grant
			//Toast.makeText(this,"getUserGrant()3", Toast.LENGTH_SHORT).show();
			break;
		case UniPayReaderMsg.typeToReportToIdtech:
			//pop up dialog to get the user grant
			//Toast.makeText(this,"getUserGrant()4", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		
		return true;
	}
///// -------------------------
	@Override
	public void onReceiveMsgAutoConfigCompleted(StructConfigParameters profile) {
		//
		autoconfig_running = false;
		beginTimeOfAutoConfig = beginTime;
		this.profile = profile;
		profileDatabase.setProfile(profile);
		profileDatabase.insertResultIntoDB();
		handler.post(doConnectUsingProfile);
		Toast.makeText(this,"onReceiveMsgAutoConfigCompleted", Toast.LENGTH_SHORT).show();
		etBottomInfo.setText(profile.getMax()+"");
	}

	@Override
	public void onReceiveMsgAutoConfigProgress(int progressVal) {
		//Toast.makeText(this, "auto config one progress", Toast.LENGTH_SHORT).show();
		percent = progressVal;
		strStatus = "Searching the configuration automatically, "+progressVal+"% finished."+"("+getTimeInfo(beginTime)+")";
		MSRdata = null;
		beginTimeOfAutoConfig = beginTime;
		handler.post(doUpdateAutoConfigProgressInfo);
		Toast.makeText(this, strStatus, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onReceiveMsgAutoConfigProgress(int percent, double result, String profilename) {
		//Toast.makeText(this, "auto config more progress", Toast.LENGTH_SHORT).show();
		if(ProgressInfoStr == null){
			ProgressInfoStr = "("+profilename+") <"+percent+"%>, Result="+Common.getDoubleValue(result);
		}else{
			ProgressInfoStr += "\n ("+profilename+") <"+percent+"%>, Result="+Common.getDoubleValue(result);
		}
		handler.post(doUpdateAutoConfigProgressInfo);
		Toast.makeText(this, ProgressInfoStr, Toast.LENGTH_SHORT).show();
	}

///// -------------------------
	@Override
	public void onReceiveMsgCommandResult(int cmdID, byte[] cmdReturn) {
		//Toast.makeText(this, "Command result", Toast.LENGTH_SHORT).show();
		
		isWaitingForCommandResult = false;
		//
		if(cmdReturn.length > 1){
			if (cmdReturn[0] == 6 && cmdReturn[1] == (byte)0x56){
				//
				strStatus = "Failed to send command. Attached reader is in boot loader mode. Format:<"+getHexStringFromBytes(cmdReturn)+">";
				handler.post(doUpdateStatus);
			}
		}
		//
		
		switch (cmdID) {
		//COMMAND LIST OF MSR
		//MSR 01
		case UniPayReaderMsg.cmdEnableSwipingMSRCard:
			//
			if(cmdReturn[0] == 0){
				strStatus = "Enable Swiping MSR Card timeout.";
			} else if(cmdReturn[0] == 2 && cmdReturn[cmdReturn.length-1] == 3){
				//
				Log.d("PUSType", "resp="+Common.getHexStringFromBytes(cmdReturn));
				if(cmdReturn.length > 6){
					//
					short respLenght = cmdReturn[2];
					respLenght = (short) (((respLenght<<8)&0xff00)|cmdReturn[1]);
					//
					if(cmdReturn.length == respLenght+6){
						//
						if(cmdReturn[3] == 6){
							//
							strStatus = "Enable Swiping MSR Card Succeed. Please swipe the card";
							//
							if(!isWaitingForCommandResult){
								
								if(UniMagReader.startSwipeCard()){
									//
									tvCaption.setText("MSR Data");
									etBottomInfo.setText("");
								} else {
									Toast.makeText(this, "Cannot Swipe", Toast.LENGTH_SHORT).show();
								}
							}
						} else if(cmdReturn[3] == 21){
							strStatus = "Enable Swiping MSR Card Failed.Error Info: "+UniMagReader.getErrorCode(cmdReturn);
						}
						//
					} else {
						strStatus = "Enable Swiping MSR Card Failed. Resp length error.";
					}
				}
			}
			//
			break;
			// MSR O2
		case UniPayReaderMsg.cmdCancelSwipingMSRCard:
			//
			if(cmdReturn[0] == 0){
				strStatus = "Cancel Swiping MSR Card timeout";
			} else if(cmdReturn[0] == 2 && cmdReturn[cmdReturn.length -1] == 3){
				//
				if(cmdReturn.length>6)
            	{
            		//get the length of response, low byte and high byte
            		short respLength = cmdReturn[2];
            		respLength = (short)(((respLength<<8)&0xff00)|cmdReturn[1]);
            		
            		//check the package length
            		if(cmdReturn.length==respLength+6)
            		{
            			if(6==cmdReturn[3])
            			{
	        				strStatus = "Cancel Swiping MSR Card Succeed.";
            			}
            			else if(21==cmdReturn[3])
            			{
            				//get error code
            				
            				strStatus = "Cancel Swiping MSR Card Failed.Error Info: "+UniMagReader.getErrorCode(cmdReturn);
             			}
            		}
            		else
            			
            			strStatus = "Cancel Swiping MSR Card Failed. Resp length error.";
            	}
			} else {
				strStatus = "Cancel Swiping MSR Card failed.";
			}
			//
			break;
		case UniPayReaderMsg.cmdSetOtherCommonSetting:
			// AES Convertion
			if (cmdReturn[0] == 0) {
				strStatus = "MSR Set AES Time out";
			} else if(cmdReturn[0] == 2 && cmdReturn[cmdReturn.length - 1] == 3){
				if (cmdReturn.length > 6) {
					//
					short respLenght = cmdReturn[2];
					respLenght = (short) (((respLenght<<8)&0xff00) | cmdReturn[1]);
					
					if (cmdReturn.length == respLenght + 6) {
						//
						if (cmdReturn[3] == 6) {
							strStatus = "Set Msr to AES Succeedd.";
						} else if (cmdReturn[3] == 21){
							strStatus = "Set MSR to AES Failed. Error Info: "+UniMagReader.getErrorCode(cmdReturn);
						}
					} else {
						strStatus = "Set MSR to AES Failed. Resp length error.";
					}
				}
			} else {
				strStatus = "Set MSR to AES failed.";
			}
			break;
			///
		default:
			break;
		}
		
		MSRdata = null;
		MSRdata = new byte[cmdReturn.length];
		System.arraycopy(cmdReturn, 0, MSRdata, 0, cmdReturn.length);
		handler.post(doUpdateStatus);
	}
///// -------------------------
	@Override
	public void onReceiveMsgToConnect() {
		//Toast.makeText(this, "Connecting", Toast.LENGTH_SHORT).show();
		etTopInfo.setText("UniPay Audio Jack Detected, waiting for response...\nPlease wait...");
	}
	
	@Override
	public void onReceiveMsgConnected() {
		
		isReaderConnected = true;
		
		etTopInfo.setText("Now the UniPay Unit is Connected");
		tvConnectStatus.setText("Connected");
	}

	@Override
	public void onReceiveMsgDisconnected() {
		//Toast.makeText(this, "DisConnected", Toast.LENGTH_SHORT).show();
		etTopInfo.setText("The device is Disconnected");
		tvConnectStatus.setText("Disconnected");
		isReaderConnected = false;
	}
///// -------------------------
	@Override
	public void onReceiveMsgFailureInfo(int index, String msg) {
		//Toast.makeText(this, "Failure info", Toast.LENGTH_SHORT).show();
		strStatus = msg;
		MSRdata = null;
		handler.post(doUpdateStatus);
	}

	@Override
	@Deprecated
	public void onReceiveMsgSDCardDFailed(String arg0) {
		
	}

	@Override
	public void onReceiveMsgTimeout(String msg) {
		//Toast.makeText(this, "Time Out Error"+arg0, Toast.LENGTH_SHORT).show();
		etTopInfo.setText(msg);
		enableSwipeCard = true;
	}
///// -------------------------
	@Override
	public void onReceiveMsgToSwipeCard() {
		//Toast.makeText(this, "swipe card", Toast.LENGTH_SHORT).show();
		handler.post(doHideSwipeTopDlg);
		handler.post(doShowSwipeToDlg);
	}
	
	@Override
	public void onReceiveMsgProcessingCardData() {
		//Toast.makeText(this, "processing card data", Toast.LENGTH_SHORT).show();
		strStatus = "Card data is being process. Please wait.";
		MSRdata = null;
		handler.post(doHideSwipeTopDlg);
	}
	
	@Override
	public void onReceiveMsgCardData(byte flagOfCardData, byte[] cardData) {
		//Toast.makeText(this, "card data is present", Toast.LENGTH_SHORT).show();
		Log.d("Demo Infor >>>> On receive Message Card Data flagOfCardData="+ flagOfCardData, "CardData="+getHexStringFromBytes(cardData));
		
		byte flag = (byte) (flagOfCardData&0x04);
		//
		if(flag == 0x00){
			//
			if (flagOfCardData == 0x02 && cardData[2] == 0x15 && cardData[cardData.length-1] == 0x03) {
				//
				MSRdata = null;
				byte[]cmdReturn = new byte[cardData.length +1];
				System.arraycopy(cardData, 0, cmdReturn, 1, cardData.length);
				cmdReturn[0] = flagOfCardData;
				strStatus = "Timeout when swipe MSR card.\nError Info: " +UniMagReader.getErrorCode(cmdReturn)+"\n"+getHexStringFromBytes(cmdReturn);
				//
				handler.post(doHideSwipeTopDlg);
				handler.post(doUpdateStatus);
			} else {
				//
				strMsrData = new String (cardData);
			}
		} else if (flag == 0x04){
			strMsrData = new String (cardData);
		}
		//
		
		//strMsrData += "\n "+flagOfCardData+ " " + cardData;
		MSRdata = new byte[cardData.length];
		System.arraycopy(cardData, 0, MSRdata, 0, cardData.length);
		enableSwipeCard = true;
		handler.post(doHideSwipeTopDlg);
		//handler.post(doUpdateTVS1);
		handler.post(doShowPayment);
		//
		
	}
///// -------------------------
	@Override
	public void onReceiveMsgChallengeResult(int arg0, byte[] arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceiveMsgUpdateFirmwareProgress(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceiveMsgUpdateFirmwareResult(int arg0) {
		// TODO Auto-generated method stub
		
	}
	// ---------------------------------------------------------------------
	// TODO INNER CLASS
	// ---------------------------------------------------------------------
	private class Ass extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... param) {
			// TODO Auto-generated method stub
			
			UniMagReader.setXMLFileNameWithPath(param[0]);
			UniMagReader.loadingConfigurationXMLFile(true);
			
			return null;
		}
    	
    }

}
