package com.example.swiperdemo;


import java.io.File; 
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import IDTech.MSR.XMLManager.StructConfigParameters;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.com.IdTech.TDES.CreditCardInfo;
import cn.com.IdTech.TDES.InvalidDataException;
import cn.com.IdTech.TDES.InvalidDecryptionKeyException;
import cn.com.IdTech.TDES.SwipeDataInterface;
import cn.com.IdTech.TDES.UnknownDecryptionException;

import com.example.swiperdemo.fragments.DetailsFragment;
import com.example.swiperdemo.fragments.HomePageFragment;
import com.example.swiperdemo.fragments.MainMenuFragment;
import com.example.swiperdemo.fragments.ProgressDialogFragment;
import com.example.swiperdemo.fragments.SubMenuFragment;
import com.example.swiperdemo.objects.CardData;
import com.example.swiperdemo.objects.DoSwipe;
import com.example.swiperdemo.objects.Encription;
import com.example.swiperdemo.objects.PaypalPay;
import com.example.swiperdemo.objects.ProfileDatabase;
import com.example.swiperdemo.objects.UniMagTopDialog;
import com.example.swiperdemo.utils.Frag;
import com.example.swiperdemo.utils.Sets;
import com.idtechproducts.unipay.UniPayReader;
import com.idtechproducts.unipay.UniPayReaderMsg;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;
//import com.paypal.api.payments.Amount;
//import com.paypal.api.payments.CreditCard;
//import com.paypal.api.payments.FundingInstrument;
//import com.paypal.api.payments.Payer;
//import com.paypal.api.payments.Payment;
//import com.paypal.api.payments.PaymentExecution;
//import com.paypal.api.payments.RedirectUrls;
//import com.paypal.api.payments.Transaction;
//import com.paypal.api.payments.Transactions;
//import com.paypal.core.ConfigManager;
//import com.paypal.core.rest.APIContext;
//import com.paypal.core.rest.OAuthTokenCredential;
//import com.paypal.core.rest.PayPalRESTException;
//import com.paypal.sdk.openidconnect.Address;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Charge;
import com.stripe.model.ChargeCollection;
import com.stripe.model.Customer;
import com.stripe.model.Recipient;
import com.stripe.model.Transfer;
import com.stripe.android.*;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

public class MainContentAct extends BaseActivity implements UniPayReaderMsg{
 // =========================================================================
 // TODO Variables
 // =========================================================================
	private ProfileDatabase profiledatabase;
	public UniPayReader Unipay;
	private StructConfigParameters profile;
	private Handler handler = new Handler();
	private byte[] MSRData = null;
	private byte[] msrTDESdata = null;
	private UniMagTopDialog dlgSwipeTopShow = null;
	//
	private boolean isUseAutoConfigProfileChecked = false;
	private boolean isReaderConnected = false;
	private boolean isWaitingForCommandResult = false;
	private boolean isAlreadySwipe = false;
	private boolean autoconfig_running = false;
	//
	private String strStatusInfo = null;
	private String strCommandInfo = null;
	private String popupDialogMsg = null;
	private String strMsrData = null;
	
	private static String UnipayCommand = "Unipay Command";
	
	private long beginTime = 0;
	//
	private TextView tvCommandType, tvReaderStats;
	private EditText etStatusInfo, etAmount;
	private LinearLayout llPaymentForm;
	private Button btnPaymentStart;

	//
	public MainContentAct() {
		super("Card Swiper");
	}
 // =========================================================================
 // TODO Life cycles
 // =========================================================================
	@Override
	@SuppressLint("Recycle")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
		getSlidingMenu().setShadowWidth(5);
		getSlidingMenu().setBehindScrollScale(0);
		getSlidingMenu().setFadeEnabled(false);
		//
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		//
		setContentView(R.layout.frame_content);
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.frame_content, new HomePageFragment()).commit();
		//
		getSlidingMenu().setSecondaryMenu(R.layout.frame_sub);
		getSlidingMenu().setSecondaryShadowDrawable(R.drawable.shadowright);
		getSlidingMenu().setRightBehindOffset(110);
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.frame_sub, new SubMenuFragment()).commit();
		
		getSlidingMenu().setSecondaryOnOpenListner(new OnOpenListener() {
			@Override
			public void onOpen() {
				Fragment ofrag = getSupportFragmentManager().findFragmentById(R.id.frame_sub);
				((SubMenuFragment)ofrag).callDetails();
			}
		});
		//
		profiledatabase = new ProfileDatabase(this);
		profiledatabase.initializedDB();
		isUseAutoConfigProfileChecked = profiledatabase.getIsUseAutoConfigProfile();
		//
		InitializeUI();
		//InitializeReader();
		//
//		ActionBar bar = getActionBar();
//		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000099")));
		
	}

	@Override
	protected void onResume() {
		 
		 //InitializeWidgets();
		super.onResume();
	}
	 
	@Override
	protected void onPause() {
		if (Unipay != null) {
			Unipay.stopSwipeCard();
		}
		
		hideSwipeTopDialog();
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		Unipay.release();
		profiledatabase.closeDB();
		super.onDestroy();
	}
	
 // =========================================================================
 // TODO Overrides
 // =========================================================================
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//
		 if ((keyCode == KeyEvent.KEYCODE_BACK) || (keyCode == KeyEvent.KEYCODE_HOME)) {
				final AlertDialog.Builder dial = new AlertDialog.Builder(this);
				dial.setTitle("Leave the app?");
				dial.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						exit();
					}
				});
				dial.setNegativeButton("No", null);
				dial.show();
		}
		return super.onKeyDown(keyCode, event);
	}
	private void exit(){
		this.finish();
	}   
 // =========================================================================
 // TODO Main Functions
 // =========================================================================
	private void InitializeUI(){
		//
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	public void InitializeWidgets(){
		//
		View view = getFragmentView();
		
		if (view != null) {
//			
			 tvCommandType = (TextView)view.findViewById(R.id.tvCommandType);
			 etStatusInfo = (EditText)view.findViewById(R.id.etStatusInfo);
			 tvReaderStats = (TextView)view.findViewById(R.id.tvReaderStats);
			 llPaymentForm = (LinearLayout)view.findViewById(R.id.llPaymentForm);
			 etAmount = (EditText)view.findViewById(R.id.etAmount);
			 btnPaymentStart = (Button)view.findViewById(R.id.btnPaymentStart);
			 //
			 setPaymentFormEnabled(false);
		}
		
		 //
		 //getFragmentMenu().findItem(R.id.btnSwipeAction).setEnabled(false);
	}

	public void InitializeReader(){
		if (Unipay != null) {
			Unipay.unregisterListen();
			Unipay.release();
			Unipay = null;
		}
		//
		Unipay = new UniPayReader(this, this);
		Unipay.setVerboseLoggingEnable(true);
		Unipay.registerListen();
		
		String fileNameWithPath = getXMLFileFromRaw("idt_unimagcfg_default.xml");
		if (!isFileExist(fileNameWithPath)) 
			fileNameWithPath = null;
		
		if (isUseAutoConfigProfileChecked) {
			if (profiledatabase.updateProfileFromDB()) {
				profile  = profiledatabase.getProfile();
				Unipay.connectWithProfile(profile);
				Toast.makeText(this, "AutoConfig profile has been loaded", Toast.LENGTH_SHORT).show();
			}
		} else {
			new UnipayAsync().execute(fileNameWithPath);
		}
	}
	
	private void showUnipayInfo(){
		if (Unipay != null) {
			String manufacturer = Unipay.getInfoManufacture();
			String model = Unipay.getInfoModel();
			String SDKVersion = Unipay.getSDKVersionInfo();
			String xmlVersion = Unipay.getXMLVersionInfo();
			String OSVersion = android.os.Build.VERSION.RELEASE;
			//
			String TempNam = "";
			if (profile != null) {
				TempNam = "AutoConfig Template: "+profile.getModelNumber();
			} 
			
			tvCommandType.setText("SDK Info");
			etStatusInfo.setText("" +
					"Phone: " +manufacturer+"\n"+
					"Model: " +model+"\n"+
					"SDK Ver: " +SDKVersion+"\n"+
					"XML Ver: " +xmlVersion+"\n"+
					"OS Ver: "+OSVersion + "\n"+TempNam);
		}
	}
	
	public void startSwipeCard(){
		//
		//Toast.makeText(this, "Aloha!!", Toast.LENGTH_SHORT).show();
		if (Unipay != null) {
			if (!isWaitingForCommandResult) {
				if (Unipay.sendCommandEnableSwipingMSRCard()) {
					prepareToSendCommand(UniPayReaderMsg.cmdEnableSwipingMSRCard);
				}
			}
		}
	}
	
	public void prepareToSendCommand(int cmdID){
		isWaitingForCommandResult = true;
		switch (cmdID) {
		case UniPayReaderMsg.cmdEnableSwipingMSRCard:
			strStatusInfo = "To Enable Swiping MSR Card, wait for response";
			strCommandInfo = UnipayCommand;
			break;
		case UniPayReaderMsg.cmdCancelSwipingMSRCard:
			strStatusInfo = "To Cancel Swiping MSR Card, wait for response";
			strCommandInfo = UnipayCommand;
			break;
		case UniPayReaderMsg.cmdReviewMSRSetting:
			strStatusInfo = "To Review MSR Setting, wait for response";
			strCommandInfo = UnipayCommand;
			break;
		case 1001:
			strStatusInfo = "To Set MSR TDES Setting, wait for response";
			break;
		case UniPayReaderMsg.cmdGetSerialNumber:
			strStatusInfo = "Tos Get Serial Number, wait for response";
			break;
		case UniPayReaderMsg.cmdGetVersion:
			strStatusInfo = "To Get Version, wait for response";
			break;
		case UniPayReaderMsg.cmdReviewAudioJackSetting:
			strStatusInfo = "To Review Audio Jack Setting, wait for response.";
			break;
		default:
			break;
		}
		//
		//MSRData = null;
		handler.post(doUpdateStatuInfo);
	}
	
	private void paymentStart(final String accountNumber, final String month, final String year, final String name, final String cvv){
		//
		btnPaymentStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String amount = etAmount.getText().toString();
				if(!amount.equals(""))
					//new StripePayAsync().execute(accountNumber, month, year, amount);
					PaymentProcess(accountNumber, month, year, amount, name, cvv);
				else
					Toast.makeText(getBaseContext(), "Payment amount is required", Toast.LENGTH_SHORT).show();
				
				//for paypal test
				//new PaypalPay(getBaseContext(), etStatusInfo, accountNumber);
			}
		});
	}
	
	private void passPaymentToStripe(String accountNumber, String month, String year,String valAm){
		//// on tutorial
		
		
		////////////////////////////////////////////////////
		Stripe.apiKey = "sk_test_cM87J66WkEV0V8J5svBBtBTg";
		Charge charge = null;
		//
		int amount = Integer.parseInt(valAm+"00");
		
		Map<String, Object> payCharge = new HashMap<String, Object>();
		payCharge.put("amount", amount);
		payCharge.put("currency", "usd");
		
		Map<String, Object> cardMap = new HashMap<String, Object>();
		cardMap.put("number", "4242424242424242");
        cardMap.put("exp_month", 12);
        cardMap.put("exp_year", 2020);
        
//        Map<String, Object> cardMap = new HashMap<String, Object>();
//		  cardMap.put("number", accountNumber);
//        cardMap.put("exp_month", Integer.parseInt(month));
//        cardMap.put("exp_year", Integer.parseInt(year));
        
        payCharge.put("card", cardMap);
        String result = null;
        try {
			charge = Charge.create(payCharge);
			
			result = String.valueOf(charge.getAmount());
			
			strStatusInfo = "Transaction Amount is: " +result;
			strStatusInfo += "\n"+charge.getBalanceTransaction()+"" +
					"\n" + charge.getCurrency()+
					"\n" + charge.getCustomer()+
					"\n" + charge.getDescription()+
					"\n" + charge.getDispute()+
					"\n" + charge.getDisputed()+
					"\n" + charge.getId()+
					"\n" + charge.getInvoice()+
					"\n" + charge.getStatementDescription()+
					"\n" + charge.getAmountRefunded()+
					"\n" + charge.getCaptured()+
					"\n" + charge.getCard()+
					"\n" + charge.getPaid()+
					"\n" + charge.getInvoice()+
					"\n\n Transaction Complete.";
		} catch (StripeException e) {
			e.printStackTrace();
			result = e.getMessage();
			strStatusInfo = "Transaction Error: " +result;
		}
	}
	
	private void PaymentProcess(final String accountNumber, final String month, final String year, final String amount, final String name, final String cvv){
		////
		
		
		@SuppressWarnings("static-access")
		final ProgressDialogFragment prog = new ProgressDialogFragment().newInstance("Loading","Payment is process...");

//		final String Cardname = "redfoot";
//		final String AccountNum = "4242424242424242";
		//final Card card = new Card(AccountNum, 12, 2020, "123");
		
		final Card card = new Card(accountNumber, Integer.parseInt(month), Integer.parseInt(year), cvv);
		//card.setName(Cardname);
		card.setName(name);
		
		if (card.validateCard()) {
			prog.show(getSupportFragmentManager(), "progress");
			
			//	RESERVED CODE IN FUTURE TASK
			new com.stripe.android.Stripe().createToken(card, 
					Sets.TEST_PUBLISH_KEY,
					new TokenCallback() {
						@Override
						public void onSuccess(Token token) {
							new OnChargeAsync(prog,token).execute(amount, name, accountNumber);
							//new onCustomerCreateAsync(prog,token).execute();
						}
						@Override
						public void onError(Exception error) {
							prog.dismiss();
							strStatusInfo = error.getMessage();
							strCommandInfo = "Stripe Error";
							handler.post(doUpdateStatuInfo);
						}
					});
			////////////
			//new OnChargeTwoAsync(prog).execute();
			///////////
		
		} else if (!card.validateNumber()) {
			strStatusInfo ="The card number that you entered is invalid";
		} else if (!card.validateExpiryDate()) {
			strStatusInfo ="The expiration date that you entered is invalid";
		} else if (!card.validateCVC()) {
			strStatusInfo ="The CVC code that you entered is invalid";
		} else {
			strStatusInfo ="The card details that you entered are invalid";
		}
		//prog.dismiss();
		
		handler.post(doUpdateStatuInfo);
		//
	}
	
	private void TransferProcess(){
		final ProgressDialogFragment prog = new ProgressDialogFragment().newInstance("Loading", "Tranfer funds process..");
		
		final String nameCard = "Marian Rivera";
		Card card = new Card("4000056655665556", 12, 2020, "123");
		card.setName(nameCard);
		
		
		if(card.validateCard()){
			//
			prog.show(getSupportFragmentManager(), "progress");
			new com.stripe.android.Stripe().createToken(card,
					Sets.TEST_PUBLISH_KEY,
					new TokenCallback() {
						@Override
						public void onSuccess(Token token) {
							strStatusInfo = "OK";
							new onTranserAsync(prog, token).execute(nameCard);
						}
						@Override
						public void onError(Exception error) {
							prog.dismiss();
							strStatusInfo = "Failed";
							handler.post(doUpdateStatuInfo);
						}
					});
			//
		} else if (!card.validateNumber()) {
			strStatusInfo ="The card number that you entered is invalid";
		} else if (!card.validateExpiryDate()) {
			strStatusInfo ="The expiration date that you entered is invalid";
		} else if (!card.validateCVC()) {
			strStatusInfo ="The CVC code that you entered is invalid";
		} else {
			strStatusInfo ="The card details that you entered are invalid";
		}
		
		handler.post(doUpdateStatuInfo);
		}
	/** Outside Method*/
	public void setAutoConfig(){
		String fileNameWithPath = getAutoConfigProfileFileFromRaw();
		if (!isFileExist(fileNameWithPath)) {
			fileNameWithPath = null;
		}
		
		boolean startAcRet = Unipay.startAutoConfig(fileNameWithPath, true);
		if (startAcRet) {
			autoconfig_running = true;
			beginTime = getCurrentTime();
		}
	}
	
	public void stopAutoConfig(){
		if (autoconfig_running) {
			Unipay.stopAutoConfig();
			Unipay.unregisterListen();
			Unipay.release();
			
			InitializeReader();
			
			autoconfig_running = false;
		}
	}
	
	public void testPayment(){
		
		//PaymentProcess("4242424242424242", "12", "2020", "1", "redfoot", "123");
		PaymentProcess("4000056655665556", "12", "2020", "1", "Xian Lim", "123");
		
		showContent();
		
		//Encription en = new Encription();
//		//String encrypted = en.encrypt("benkurama");
		//String encrypted = en.testingConvert();
//		etStatusInfo.setText(encrypted);
	}
	
	public void EncryptToTDES(){
		if (Unipay.sendCommandSetCommonSetting((byte)0x4c, (byte)0x31)) {
			prepareToSendCommand(1001);
		} else {
			Toast.makeText(this, "Error TDES Encryption", Toast.LENGTH_LONG).show();
		}
	}
	
	public void getSerialNumber(){
		if (Unipay.sendCommandGetSerialNumber()) {
			prepareToSendCommand(UniPayReaderMsg.cmdGetSerialNumber);
		}
	}
	
	public void getVersion(){
		if (Unipay.sendCommandGetVersion()) {
			prepareToSendCommand(UniPayReaderMsg.cmdGetVersion);
		}
	}

	public void reviewAudioJackSettings(){
		if (Unipay.sendCommandReviewAudioJackSettings()) {
			prepareToSendCommand(UniPayReaderMsg.cmdReviewAudioJackSetting);
		}
	}
	
	public void goDetailsPage(){
		Fragment frag = Frag.me.getCurrentFragment(this, R.id.frame_content);
		
		if (!(frag instanceof DetailsFragment)) {
			Frag.me.set(new DetailsFragment(), this, R.id.frame_content, false);
		}
		showContent();
	}
	
	public void goHomePage(){
		Fragment frag = Frag.me.getCurrentFragment(this, R.id.frame_content);
		if (!(frag  instanceof HomePageFragment))  {
			Frag.me.set(new HomePageFragment(), this, R.id.frame_content, false);
		}
		
		showContent();
	}
	
	public void goTransferTest(){
		//
		TransferProcess();
		//PayPalTest();
	}
	
	public void PayPalTest(){
		//
		//new paypalTestPay().execute();
		
		//new DoSwipe(this, etStatusInfo);
		
		
		
		new PaypalPay(this, etStatusInfo, ";5105105105105100=15121011000012345678?");

		//new PaypalPay(this, etStatusInfo, ";4912000033330026=15121011000012345678?");
		}
 // =========================================================================
 // TODO Sub Functions
 // =========================================================================
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
			this.deleteFile(fileNameWithPath);
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
 
	private View getFragmentView(){
		View v = null;
		Fragment frag = Frag.me.getCurrentFragment(this, R.id.frame_content);
		if (frag instanceof HomePageFragment) {
			v = frag.getView();
		}
		return v;
	}
	 
	private Menu getFragmentMenu(){
		Fragment frag =  getSupportFragmentManager().findFragmentById(R.id.frame_content);
		return ((HomePageFragment)frag).getMenu();
		
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
	
	private String getAscii(String data, byte[] bit){
		
		StringBuffer sf = new StringBuffer();
		
		for (int i=0; i< data.length()/2; i++){
			sf.append((char)bit[i]);
		}
		return sf.toString();
	}
	////// ----------
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
	
	private void hideSwipeTopDialog(){
		if (dlgSwipeTopShow != null) {
			dlgSwipeTopShow.hide();
			dlgSwipeTopShow.dismiss();
			dlgSwipeTopShow = null;
		}
	}
	
	private void cancelSwipeFunction(){
		//
		Unipay.stopSwipeCard();
		//
		if(Unipay.sendCommandCancelSwipingMSRCard()){
			prepareToSendCommand(UniPayReaderMsg.cmdCancelSwipingMSRCard);
		} else {
			Toast.makeText(getBaseContext(), "Cannot cancel the swipe", Toast.LENGTH_SHORT).show();
		}
		//
		if(dlgSwipeTopShow != null){
			dlgSwipeTopShow.dismiss();
		}
	}
	
	private void setPaymentFormEnabled(boolean enable){
		
		for (int i = 0; i < llPaymentForm.getChildCount(); i++) {
			llPaymentForm.getChildAt(i).setEnabled(enable);
		 }
	}
	
	private String getAutoConfigProfileFileFromRaw( ){
		//share the same copy with the configuration file
		return getXMLFileFromRaw("idt_unimagcfg_default.xml");
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
 // =========================================================================
 // TODO Implementations
 // =========================================================================
	public boolean getUserGrant(int type, String msg) {
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
		
		autoconfig_running = false;
		this.profile = profile;
		profiledatabase.setProfile(profile);
		profiledatabase.insertResultIntoDB();
		
		Toast.makeText(getBaseContext(), "Auto Config Complete", Toast.LENGTH_LONG).show();
		handler.post(doConnectUsingProfile);
		
	}
	@Override
	public void onReceiveMsgAutoConfigProgress(int progressVal) {
		strStatusInfo = "Searching the configuration automatically, "+progressVal+"% finished. ("+getTimeInfo(beginTime)+")";
		strCommandInfo = "AutoConfig Running";
		
		Toast.makeText(getBaseContext(), strStatusInfo, Toast.LENGTH_SHORT).show();
		handler.post(doUpdateAutoConfig);
	}
	@Override
	public void onReceiveMsgAutoConfigProgress(int percent, double result, String profileName) {
		Toast.makeText(this, profileName, Toast.LENGTH_SHORT).show();
	}
///// -------------------------
	@Override
	public void onReceiveMsgCommandResult(int cmdID, byte[] cmdReturn) {
		isWaitingForCommandResult = false;
		//
		if (cmdReturn.length > 1) {
			if (cmdReturn[0] == 6 && cmdReturn[1] == (byte)0x56) {
				//
				strStatusInfo = "Failed to send command. Attached reader is in boot loader mode. Format:<"+getHexStringFromBytes(cmdReturn)+">";
				strCommandInfo = "Unipay Error";
				handler.post(doUpdateStatuInfo);
				return;
			}
		}
		//
		switch (cmdID) {
		/** COMMAND LIST OF MSR*/
		//MSR 01
		
		case UniPayReaderMsg.cmdEnableSwipingMSRCard:
			
			if (cmdReturn[0] == 0) {
				//->
				strStatusInfo = "Enable Swiping MSR Card timeout.";
				//-<
			} else if(cmdReturn[0] == 2 && cmdReturn[cmdReturn.length-1] == 3){
				///
				if (cmdReturn.length > 6) {
					short respLenght = cmdReturn[2];
					respLenght = (short) (((respLenght<<8)&0xff00)|cmdReturn[1]);
					
					if (cmdReturn.length == respLenght + 6) {
						if (cmdReturn[3] == 6) {
							///////->
							if (!isWaitingForCommandResult) {
								if (Unipay.startSwipeCard()) {
									//
									tvCommandType.setText("MSR Data");
									
								} else {
									Toast.makeText(this, "Cannot swipe", Toast.LENGTH_SHORT).show();
								}
							}
							///////-<
						} else if(cmdReturn[3] == 21){
							//->
							strStatusInfo = "Enable Swiping MSR Card Failed.Error Info: "+Unipay.getErrorCode(cmdReturn);
							//-<
						}
					} else {
						//->
						strStatusInfo = "Enable Swiping MSR Card Failed. Resp length error.";
						//-<
					}
				}
			}else {
				strStatusInfo = "Enabling Swiping MSR Card failed.";
			}
			
			break;
		//MSR 02
		case UniPayReaderMsg.cmdCancelSwipingMSRCard:
			
			if (cmdReturn[0] == 0) {
				strStatusInfo = "Cancel Swiping MSR Card timeout";
			} else if(cmdReturn[0] == 2 && cmdReturn[cmdReturn.length-1] == 3){
				///
				if (cmdReturn.length > 6) {
					short respLenght = cmdReturn[2];
					respLenght = (short) (((respLenght<<8)&0xff00)|cmdReturn[1]);
					
					if (cmdReturn.length == respLenght + 6) {
						if (cmdReturn[3] == 6) {
							///////->
							strStatusInfo = "Cancel Swiping MSR Card Succeed.";
							///////-<
						} else if(cmdReturn[3] == 21){
							strStatusInfo = "Cancel Swiping MSR Card Failed.Error Info: "+Unipay.getErrorCode(cmdReturn);
						}
					} else {
						strStatusInfo = "Enable Swiping MSR Card Failed. Resp length error.";
					}
				}
			} else {
				strStatusInfo = "Cancel Swiping MSR Card failed.";
			}
			
			break;
		//MSR 03
		case UniPayReaderMsg.cmdReviewMSRSetting:
			
			if (cmdReturn[0] == 0) {
				strStatusInfo = "Get Setting timeout.";
				strCommandInfo = "Unipay Command";
			} else if(cmdReturn[0] == 2 && cmdReturn[cmdReturn.length-1] == 3){
				///
				if (cmdReturn.length > 2) {
					short respLenght = cmdReturn[2];
					respLenght = (short) (((respLenght<<8)&0xff00)|cmdReturn[1]);
					
					if (cmdReturn.length == respLenght + 6) {
						byte cmdDataX[]  = new byte[cmdReturn.length-6];
        				System.arraycopy(cmdReturn, 3, cmdDataX, 0, cmdReturn.length-6);
						strStatusInfo = "Get Setting: " + getHexStringFromBytes(cmdDataX);
        				//strStatusInfo = "Get Setting: " + new String(cmdDataX);
						strCommandInfo = "Unipay Command";
						cmdDataX = null;
					} else {
						strStatusInfo = "Get Setting failed, data lenght error.";
					}
				}
			} else if(cmdReturn[0] == 6 && cmdReturn[1] == 2  && cmdReturn[cmdReturn.length-2] == 3){
				byte cmdDataX[]  = new byte[cmdReturn.length-4];
				System.arraycopy(cmdReturn, 2, cmdDataX, 0, cmdReturn.length-4);
				strStatusInfo = "Get Settings: "+ getHexStringFromBytes(cmdDataX);
				strCommandInfo = "Unipay Command";
				cmdDataX=null;
			} else {
				strStatusInfo = "Get Setting failed, Error Format:<"+ getHexStringFromBytes(cmdReturn)+">";
				strCommandInfo = "Unipay Command";
			}
		break;
		//MSR 04 TDES
		case UniPayReaderMsg.cmdSetOtherCommonSetting:
			//
			if (cmdReturn[0] == 0) {
				strStatusInfo = "MSR Set TDES Time out.";
				strCommandInfo = "Unipay Command";
			} else if (cmdReturn[0] == 2 && cmdReturn[cmdReturn.length - 1] == 3) {
				if (cmdReturn.length > 6) {
					short respLength = cmdReturn[2];
            		respLength = (short)(((respLength<<8)&0xff00)|cmdReturn[1]);
            		
            		if (cmdReturn.length == respLength + 6) {
						if (cmdReturn[3] == 6) {
							strStatusInfo = "Set MSR to TDES Succeed.";
							//
//							msrTDESdata = null;
//							msrTDESdata = new byte[cmdReturn.length];
//							System.arraycopy(cmdReturn, 0, msrTDESdata, 0, cmdReturn.length);
							
							byte cmdDataX[]  = new byte[cmdReturn.length];
	        				System.arraycopy(cmdReturn, 0, cmdDataX, 0, cmdReturn.length);
	        				strStatusInfo += new String(cmdDataX);
	        				
//							msrTDESdata = null;
//							msrTDESdata = new byte[cmdReturn.length + MSRData.length];
//							System.arraycopy(cmdReturn, 0, msrTDESdata, 0, cmdReturn.length);
//							System.arraycopy(MSRData, 0, msrTDESdata, cmdReturn.length, MSRData.length);
							//
						} else if (cmdReturn[3] == 21) {
							strStatusInfo =  "Set MSR to TDES Failed.Error Info: "+Unipay.getErrorCode(cmdReturn);
						}
					} else {
						strStatusInfo = "Set MSR to TDES Failed. Resp length error.";
					}
				}
			} else {
				strStatusInfo = "Set MSR to TDES failed.";
			}
			//
			break;
			//MSR 05 GET SERIAL NUMBER
		case UniPayReaderMsg.cmdGetSerialNumber:
			//
			if (cmdReturn[0] == 0) {
				strStatusInfo = "Get Serial Number timeout.";
			} else if (cmdReturn[0] == 2 && cmdReturn[cmdReturn.length -1] == 3) {
				//
				if (cmdReturn.length > 2) {
					//
					short resplenght = cmdReturn[2];
					resplenght = (short)(((resplenght<<8)&0xff00)|cmdReturn[1]);
					
					if (cmdReturn.length == resplenght + 6) {
						
						byte cmdDataX[]  = new byte[cmdReturn.length-6];
        				System.arraycopy(cmdReturn, 3, cmdDataX, 0, cmdReturn.length-6);
        				strStatusInfo = "Get Serial Number: "+ new String(cmdDataX);
					} else {
						strStatusInfo = "Get Serial Number failed, data length error.";
					}
				}
			}
			else if(6==cmdReturn[0]&&2==cmdReturn[1]&&3==cmdReturn[cmdReturn.length-2])
			{
				byte cmdDataX[]  = new byte[cmdReturn.length-4];
				System.arraycopy(cmdReturn, 2, cmdDataX, 0, cmdReturn.length-4);
				strStatusInfo = "Get Serial Number:"+new String(cmdDataX);
			}
			else
			{
				strStatusInfo = "Get Serial Number failed, Error Format:<"+ getHexStringFromBytes(cmdReturn)+">";
			}
			//
			break;
			//MSR 06 GET VERSION
		case UniPayReaderMsg.cmdGetVersion:
			//
			if (cmdReturn[0] == 0) {
				strStatusInfo = "Get Version timeout";
			} else if (cmdReturn[0] == 2 ){
				if (cmdReturn.length > 2) {
					short respLength = cmdReturn[2];
					respLength = (short)(((respLength<<8)&0xff)|cmdReturn[1]);
					if (cmdReturn.length == respLength+6) {
						byte cmdDataX[] = new byte[cmdReturn.length-6];
						System.arraycopy(cmdReturn, 3, cmdDataX, 0, cmdReturn.length-6);
						strStatusInfo = "Get Version:"+new String(cmdDataX);
					} else {
						strStatusInfo = "Get Version failed, data lenght error.";
					}
				} 
			} else {
				strStatusInfo = "Get Version failed, Error Format:<"+getHexStringFromBytes(cmdReturn)+">";
			}
			//
			break;
			//MSR 07 REVIEW AUDIO JACK SETTINGS
		case UniPayReaderMsg.cmdReviewAudioJackSetting:
			//
			if (cmdReturn[0] == 0) {
				strStatusInfo = "Review Audio Jack Setting time";
			} else if (cmdReturn[0] == 2 && cmdReturn[cmdReturn.length - 1] == 3) {
				if (cmdReturn.length > 2) {
					short respLenght = cmdReturn[2];
					respLenght = (short)(((respLenght<<8) & 0xff00) | cmdReturn[1]);
					if (cmdReturn.length  == respLenght + 6) {
						byte cmdDataX[] = new byte[cmdReturn.length + 6];
						System.arraycopy(cmdReturn, 3, cmdDataX, 0, cmdReturn.length - 6);
						strStatusInfo = "Review Audio Jack Settings " + getHexStringFromBytes(cmdDataX);
					}else{
						strStatusInfo = "Review Audio Jack Setting failed, data lenght error.";
					}
				}
			} else {
				strStatusInfo = "Review Audio Jack Setting failed, Error format:<"+getHexStringFromBytes(cmdReturn)+">";
			}
			//
			break;
		default:
			break;
		}
		//
		MSRData = null;
		//
		handler.post(doUpdateStatuInfo);
	}
///// -------------------------	
	@Override
	public void onReceiveMsgToConnect() {
		tvCommandType.setText(UnipayCommand);
		etStatusInfo.setText("UniPay Audio Jack Detected, waiting for response...\nPlease wait...");
	}
	@Override
	public void onReceiveMsgConnected() {
		//etStatusInfo.setText("");
		tvReaderStats.setText("CONNECTED");
		//
		isReaderConnected = true;
		showUnipayInfo();
		getFragmentMenu().findItem(R.id.btnSwipeAction).setEnabled(true);
		
	}
	@Override
	public void onReceiveMsgDisconnected() {
		isReaderConnected = false;
		tvReaderStats.setText("DISCONNECTED");
		etStatusInfo.setText("Unipay Reader is removed...");
		getFragmentMenu().findItem(R.id.btnSwipeAction).setEnabled(false);
	}
///// -------------------------	
	@Override
	public void onReceiveMsgToSwipeCard() {
		//
		isAlreadySwipe = false;
		popupDialogMsg = "Please swipe the card.";
		handler.post(doShowSwipeTopDlg);
		setPaymentFormEnabled(isAlreadySwipe);
	}
	@Override
	public void onReceiveMsgProcessingCardData() {
		strStatusInfo = "Card is being process. Please wait.";
		handler.post(doHideSwipeTopDlg);
	}
	@Override
	public void onReceiveMsgCardData(byte flagOfCardData, byte[] cardData) {
		//
		byte flag = (byte) (flagOfCardData&0x04);
		//
		if(flag == 0x00){
			//
			if (flagOfCardData == 0x02 && cardData[2] == 0x15 && cardData[cardData.length-1] == 0x03) {
				//
				MSRData = null;
				byte[]cmdReturn = new byte[cardData.length +1];
				System.arraycopy(cardData, 0, cmdReturn, 1, cardData.length);
				cmdReturn[0] = flagOfCardData;
				strStatusInfo = "Timeout when swipe MSR card.\nError Info: " +Unipay.getErrorCode(cmdReturn)+"\n"+getHexStringFromBytes(cmdReturn);
				//
				handler.post(doHideSwipeTopDlg);
				handler.post(doUpdateStatuInfo);
			} else {
				//
				strMsrData = new String (cardData);
			}
		} else if (flag == 0x04){
			strMsrData = new String (cardData);
		}
		//
		isAlreadySwipe = true;
		strCommandInfo = "Card Info";
		//if (msrTDESdata != null) {
			handler.post(doShowCardData);
		//}
		//strMsrData += "\n "+flagOfCardData+ " " + cardData;
		MSRData = new byte[cardData.length];
		System.arraycopy(cardData, 0, MSRData, 0, cardData.length);
	}
///// -------------------------	
	@Override
	public void onReceiveMsgFailureInfo(int index, String msg) {
		strStatusInfo = msg;
		strCommandInfo = UnipayCommand;
		handler.post(doUpdateStatuInfo);
	}
	@Override
	@Deprecated
	public void onReceiveMsgSDCardDFailed(String arg0) {
		
	}
	@Override
	public void onReceiveMsgTimeout(String arg0) {
		
	}
 // =========================================================================
 // TODO Runnable
 // =========================================================================
	private Runnable doUpdateStatuInfo = new Runnable() {
		@Override
		public void run() {
			try {
				etStatusInfo.setText(strStatusInfo);
				tvCommandType.setText(strCommandInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	private Runnable doShowSwipeTopDlg = new Runnable() {
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
	private Runnable doShowCardData = new Runnable() {
		@Override
		public void run() {
			setPaymentFormEnabled(isAlreadySwipe);
			
			/// test
			//Encription tion = new Encription();
			//byte[] encrypted = tion.encryptByte(MSRData);
			//byte[] res = tion.cryptTest(strMsrData, "0123456789ABCDEFFEDCBA9876543210");
			/// 
			
//			String strKey = new String("0123456789ABCDEFFEDCBA9876543210");
//			CreditCardInfo cInfo = new CreditCardInfo();
//			SwipeDataInterface sData = new SwipeDataInterface();
//			
//			try {
//				cInfo = sData.getCreditCard(msrTDESdata, strKey);
//			} catch (InvalidDataException e) {
//				e.printStackTrace();
//			} catch (InvalidDecryptionKeyException e) {
//				e.printStackTrace();
//			} catch (UnknownDecryptionException e) {
//				e.printStackTrace();
//			}
//			///
//			if (msrTDESdata != null) {
//				String oks = "OK";
//			}
			//
			CardData card = new CardData(MSRData);
			Map<String, String> details = card.getDetails();
			String SwipeData = card.getSwipeData();
			
			if(details != null){
				String info = "" +
						"Account Number: " +details.get("accountnumber")+ "\n"+
						"Account Name: " +details.get("accountname")+ "\n"+
						"Card Type: " +details.get("cardtype")+ "\n"+
						"CVV: " +details.get("cvv")+ "\n"+
						"Valid Date: " +details.get("month")+ "/"+details.get("year") +"\n\n"
						+"Swipe Data: "+ SwipeData;
				etStatusInfo.setText(info);
				paymentStart(details.get("accountnumber"),  details.get("month"), details.get("year"), details.get("accountname"), details.get("cvv"));
			} else {
				etStatusInfo.setText("Swipe Again... cannot retrieve data from card");
			}
			//
		}
	};
	private Runnable doUpdateAutoConfig = new Runnable() {
		@Override
		public void run() {
			tvCommandType.setText(strCommandInfo);
			etStatusInfo.setText(strStatusInfo);
		}
	};
	private Runnable doConnectUsingProfile = new Runnable() {
		@Override
		public void run() {
			if (Unipay != null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Unipay.connectWithProfile(profile);
			}
		}
	};
	@SuppressLint("HandlerLeak")
	
 // =========================================================================
 // TODO Inner Class
 // =========================================================================
	private class UnipayAsync extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... params) {
			Unipay.setXMLFileNameWithPath(params[0]);
			Unipay.loadingConfigurationXMLFile(true);
			return null;
		}
		
	}
	
	private class StripePayAsync extends AsyncTask<String, String, String>{
		@Override
		protected String doInBackground(String... params) {
			
			//
			Stripe.apiKey = "sk_test_cM87J66WkEV0V8J5svBBtBTg";
			Charge charge = null;
			
			int amount = Integer.parseInt(params[3]+"00");
			
			Map<String, Object> payCharge = new HashMap<String, Object>();
			payCharge.put("amount", amount);
			payCharge.put("currency", "usd");
			
			Map<String, Object> cardMap = new HashMap<String, Object>();
			cardMap.put("number", "4242424242424242");
	        cardMap.put("exp_month", 12);
	        cardMap.put("exp_year", 2020);
	        
//	        Map<String, Object> cardMap = new HashMap<String, Object>();
//			cardMap.put("number", params[0]);
//	        cardMap.put("exp_month", Integer.parseInt(params[1]));
//	        cardMap.put("exp_year", Integer.parseInt(params[2]));
	        
	        payCharge.put("card", cardMap);
	        String result = null;
	        try {
				charge = Charge.create(payCharge);
				
				result = String.valueOf(charge.getAmount());
				
				strStatusInfo = "Transaction Amount is: " +result;
				strStatusInfo += "\n"+charge.getBalanceTransaction()+"" +
						"\n" + charge.getCurrency()+
						"\n" + charge.getCustomer()+
						"\n" + charge.getDescription()+
						"\n" + charge.getDispute()+
						"\n" + charge.getDisputed()+
						"\n" + charge.getId()+
						"\n" + charge.getInvoice()+
						"\n" + charge.getStatementDescription()+
						"\n" + charge.getAmountRefunded()+
						"\n" + charge.getCaptured()+
						"\n" + charge.getCard()+
						"\n" + charge.getPaid()+
						"\n" + charge.getInvoice()+
						"\n\n Transaction Complete.";
			} catch (StripeException e) {
				e.printStackTrace();
				result = e.getMessage();
				strStatusInfo = "Transaction Error: " +result;
			}
			//
			return strStatusInfo;
		}

		@Override
		protected void onPostExecute(String result) {
			//
			etStatusInfo.setText(strStatusInfo);
			isAlreadySwipe = false;
			setPaymentFormEnabled(isAlreadySwipe);
			super.onPostExecute(result);
		}
		
	}
	
	private class OnChargeAsync extends AsyncTask<String, String, String>{

		ProgressDialogFragment prog = null;
		Token token = null;

		public  OnChargeAsync(ProgressDialogFragment progress, Token tuks){
			this.prog = progress;
			this.token = tuks;
		}
		
		@Override
		protected String doInBackground(String... params) {
			Stripe.apiKey = Sets.TEST_SECRET_KEY;
			@SuppressWarnings("unused")
			int amount = Integer.parseInt(params[0]+"00");
			String cardName = params[1];
			String accLst4 = params[2].substring(params[2].length() - 4, params[2].length());
			
			boolean isCustomerExist = false;
			//
			
			try {
				//
				Map<String, Object> listParams = new HashMap<String, Object>();
				listParams.put("count", 100);
				List<Customer> customerList = Customer.all(listParams).getData();
				//
				int x = 0;
				for (Customer customer : customerList) {
					//
					com.stripe.model.Card card = null;
					// if customer have a card
					if(customer.getCards().getTotalCount() != 0){
						card = customer.getCards().getData().get(x);
						// if customer exist
						if(card.getName().equals(cardName) && card.getLast4().equals(accLst4)){
							//
							String custID = customer.getId();
							chargeCustomer(amount, custID);
							isCustomerExist = true;
							break;
						}
					}
				}
				// create new customer with charge amount
				if (!isCustomerExist) {
					String custID = createCustomer(token, cardName, amount);
					chargeCustomer(amount, custID);
					publishProgress("First time to swipe, Create new customer credentials");
					//
					//charge.getMetadata().get(key)
				}
			} catch (AuthenticationException e) {
				e.printStackTrace();
				strStatusInfo = e.getMessage();
			} catch (InvalidRequestException e) {
				e.printStackTrace();
				strStatusInfo = e.getMessage();
			} catch (APIConnectionException e) {
				e.printStackTrace();
				strStatusInfo = e.getMessage();
			} catch (CardException e) {
				e.printStackTrace();
				strStatusInfo = e.getMessage();
			} catch (APIException e) {
				e.printStackTrace();
				strStatusInfo = e.getMessage();
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			//
			//prog.setMessage(values[0]);
			Toast.makeText(getBaseContext(), values[0], Toast.LENGTH_LONG).show();
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(String result) {
			etStatusInfo.setText(strStatusInfo);
			isAlreadySwipe = false;
			setPaymentFormEnabled(isAlreadySwipe);
			//
			prog.dismiss(); 
			super.onPostExecute(result);
		}
		
		public void chargeCustomer(int amount, String custid) throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException{
			//
			Map<String, Object> payCharge = new HashMap<String, Object>();
			payCharge.put("amount", amount);
			payCharge.put("currency", "usd");
			//payCharge.put("card", token.getId());
			payCharge.put("customer", custid);
			payCharge.put("description", "For Testing Only");
			payCharge.put("statement_description", "Test to STripe");
			///
//			Map<String, Object> initMetadata = new HashMap<String, Object>();
//			initMetadata.put("key", "susi");
//			payCharge.put("metadata", initMetadata);
			///
			
			Charge charge = Charge.create(payCharge);
			Customer cus = Customer.retrieve(custid);
			///
			int testing = 0;
			if(testing == 1){
				strStatusInfo = "Transaction Amount is: " +charge.getAmount();
				strStatusInfo += "\n"+charge.getBalanceTransaction()+"" +
						"\n" + charge.getCurrency()+
						"\n" + charge.getCustomer()+
						"\n" + charge.getDescription()+
						"\n" + charge.getDispute()+
						"\n" + charge.getDisputed()+
						"\n" + charge.getId()+
						"\n" + charge.getInvoice()+
						"\n" + charge.getStatementDescription()+
						"\n" + charge.getAmountRefunded()+
						"\n" + charge.getCaptured()+
						"\n" + charge.getCard()+
						"\n" + charge.getPaid()+
						"\n" + charge.getInvoice()+
						"\n" + cus.toString()+
						"\n" + charge.toString()+
						"\n\n Transaction Complete.";
			} else {
				String am = String.valueOf(charge.getAmount()) ;
				String decimal = am.substring(am.length() - 2, am.length());
				am = am.substring(0, am.length() -2 );
				am = am +"."+decimal;
				//
				com.stripe.model.Card card = charge.getCard();
				Customer resCus = Customer.retrieve(custid);
				//
				strStatusInfo = "Transaction Details# \n\n" +
						"Total Amount: " +am+"\n"+
						"Paid : " +charge.getPaid()+"\n"+
						"Currency : " +charge.getCurrency()+"\n"+
						"Description : " +charge.getDescription()+"\n\n"+
						"Card Details# \n\n"+
						"Account Number : **** **** **** " +card.getLast4()+"\n"+
						"Brand: " +card.getBrand()+"\n"+
						"Card type: " +card.getFunding()+"\n"+
						"Month exp: " +card.getExpMonth()+"\n"+
						"Year exp: " +card.getExpYear()+"\n"+
						"Country: " +card.getCountry()+"\n"+
						"Card Name Owner: " +card.getName()+"\n\n"+
						"Customer Details# \n\n"+
						"Owner: " +resCus.getMetadata().get("name")+"\n"+
						"Description: " +resCus.getDescription()+"\n"+
						"Email: " +resCus.getEmail()+"\n"+
						"Account Balance: " +resCus.getAccountBalance()+"\n"+
						
						"\nTransaction Complete.";
			}
			
			
		}
		
		private String createCustomer(Token token, String name, int amount) throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException{
			Map<String, Object> customer = new HashMap<String, Object>(); 
			customer.put("card", token.getId());
			customer.put("description", "Owner: "+name);
			// create add information about customer
			Map<String, Object> initMetadata = new HashMap<String, Object>();
			initMetadata.put("name", name);
			customer.put("metadata", initMetadata);
			//
			Customer cust = Customer.create(customer);
			//
			return cust.getId();
		}
	}
	
	private class OnChargeTwoAsync extends AsyncTask<String, String, String>{
		
		ProgressDialogFragment prog = null;

		public  OnChargeTwoAsync(ProgressDialogFragment progress){
			this.prog = progress;
		}
		
		@Override
		protected String doInBackground(String... params) {
			//
			Stripe.apiKey = "sk_test_cM87J66WkEV0V8J5svBBtBTg";
			Charge charge = null;
			
			
			Map<String, Object> payCharge = new HashMap<String, Object>();
			payCharge.put("amount", 100);
			payCharge.put("currency", "usd");
			
			Map<String, Object> cardMap = new HashMap<String, Object>();
			cardMap.put("number", "4242424242424242");
	        cardMap.put("exp_month", 12);
	        cardMap.put("exp_year", 2020);
	        
	        payCharge.put("card", cardMap);
	        String result = null;
	        try {
				charge = Charge.create(payCharge);
				
				result = String.valueOf(charge.getAmount());
				
				strStatusInfo = "Transaction Amount is: " +result;
				strStatusInfo += "\n"+charge.getBalanceTransaction()+"" +
						"\n" + charge.getCurrency()+
						"\n" + charge.getCustomer()+
						"\n" + charge.getDescription()+
						"\n" + charge.getDispute()+
						"\n" + charge.getDisputed()+
						"\n" + charge.getId()+
						"\n" + charge.getInvoice()+
						"\n" + charge.getStatementDescription()+
						"\n" + charge.getAmountRefunded()+
						"\n" + charge.getCaptured()+
						"\n" + charge.getCard()+
						"\n" + charge.getPaid()+
						"\n" + charge.getInvoice()+
						"\n" + charge.getRefunded()+
						"\n" + charge.getRefunds()+
						"\n\n Transaction Complete.";
			} catch (StripeException e) {
				e.printStackTrace();
				result = e.getMessage();
				strStatusInfo = "Transaction Error: " +result;
			}
	        
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			etStatusInfo.setText(strStatusInfo);
			isAlreadySwipe = false;
			setPaymentFormEnabled(isAlreadySwipe);
			//
			prog.dismiss(); 
			super.onPostExecute(result);
		}
	}

	private class onCustomerCreateAsync extends AsyncTask<String, String, String>{

		ProgressDialogFragment prog = null;
		Token token = null;

		public  onCustomerCreateAsync(ProgressDialogFragment progress, Token tuks){
			this.prog = progress;
			this.token = tuks;
		}
		
		@Override
		protected String doInBackground(String... params) {
			
			Stripe.apiKey = Sets.TEST_SECRET_KEY;
			//
			Map<String, Object> customerParams = new HashMap<String, Object>();
			customerParams.put("description", "Customer for test test@redfottech.com");
			
			try {
				Customer cus = Customer.create(customerParams);
				strStatusInfo = cus.getDescription();
			} catch (AuthenticationException e) {
				e.printStackTrace();
				strStatusInfo = e.getMessage();
			} catch (InvalidRequestException e) {
				e.printStackTrace();
				strStatusInfo = e.getMessage();
			} catch (APIConnectionException e) {
				e.printStackTrace();
				strStatusInfo = e.getMessage();
			} catch (CardException e) {
				e.printStackTrace();
				strStatusInfo = e.getMessage();
			} catch (APIException e) {
				e.printStackTrace();
				strStatusInfo = e.getMessage();
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			etStatusInfo.setText(strStatusInfo);
			isAlreadySwipe = false;
			setPaymentFormEnabled(isAlreadySwipe);
			//
			prog.dismiss(); 
			super.onPostExecute(result);
		}
		
	}
	
	private class onTranserAsync extends AsyncTask<String, String, String>{
		
		ProgressDialogFragment dial =  null;
		Token token = null;
		
		public onTranserAsync(ProgressDialogFragment dialog, Token tuks){
			this.dial = dialog;
			this.token = tuks;
		}

		@Override
		protected String doInBackground(String... params) {
			Stripe.apiKey = Sets.TEST_SECRET_KEY;
			String cardName = params[0];
			boolean isRecipientExist = false;
			//
			try {
				Map<String, Object> bankAccount = new HashMap<String, Object>();
				bankAccount.put("country", "US");
				bankAccount.put("routing_number", "110000000");
				bankAccount.put("account_number", "000123456789");
				//
				//get all recipient card to a list
				Map<String, Object> listCounts = new HashMap<String, Object>();
				listCounts.put("count", 50);
				List<Recipient> listRecipient = Recipient.all(listCounts).getData();
				
				int x = 0;
				for (Recipient recip : listRecipient) {
					
					// if recipient card is not 0
					
					if (recip.getName().equals(cardName)) {
						strStatusInfo = transferExistRecipient(recip);
						isRecipientExist = true;
						break;
					}
//					if (recip.getCards().getCount() != null) {
//					
//						if(recip.getCards().getCount() != 0 ){
//							card = recip.getCards().getData().get(x);
//							// if card name exist
//							if (card.getName().equals(cardName)) {
//								strStatusInfo = transferExistRecipient(recip);
//								isRecipientExist = true;
//								break;
//							}
//						}
//					
//					}
				}
				// if recipient is new
				if (!isRecipientExist) {
					// create new account
					Recipient recip = createNewRecipient(bankAccount, cardName);
					// do transfer
					strStatusInfo = transferExistRecipient(recip);
					publishProgress("New Recipient Created.");
					//
				}
				//
			} catch (AuthenticationException e) {
				strStatusInfo = e.getMessage();
				e.printStackTrace();
			} catch (InvalidRequestException e) {
				strStatusInfo = e.getMessage();
				e.printStackTrace();
			} catch (APIConnectionException e) {
				strStatusInfo = e.getMessage();
				e.printStackTrace();
			} catch (CardException e) {
				strStatusInfo = e.getMessage();
				e.printStackTrace();
			} catch (APIException e) {
				strStatusInfo = e.getMessage();
				e.printStackTrace();
			}
			//
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			
			Toast.makeText(getBaseContext(), values[0], Toast.LENGTH_LONG).show();
			super.onProgressUpdate(values);
		}
		@Override
		protected void onPostExecute(String result) {
			etStatusInfo.setText(strStatusInfo);
			isAlreadySwipe = false;
			setPaymentFormEnabled(isAlreadySwipe);
			//
			dial.dismiss(); 
			super.onPostExecute(result);
		}

		private String transferExistRecipient(Recipient recip) throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException{
			//
			Map<String, Object> transfer = new HashMap<String, Object>();
			transfer.put("amount", 100); 
			transfer.put("currency", "usd");
			transfer.put("recipient", recip.getId());
			transfer.put("card", recip.getDefaultCard());
			
			Transfer createTransfer = Transfer.create(transfer);
			
			return createTransfer.toString();
		}
		
		private Recipient createNewRecipient(Map<String, Object> bankAccount, String name) throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException{
			//
			Map<String, Object> recepientParams = new HashMap<String, Object>();
			recepientParams.put("name", name);
			recepientParams.put("type", "individual");
			recepientParams.put("tax_id", "000000000");
			recepientParams.put("bank_account", bankAccount);
			recepientParams.put("card", token.getId());
			
			//
			return Recipient.create(recepientParams);
		}
	}
	
	private class paypalTestPay extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... params) {
			
//			Map<String, String> sdkConfig = new HashMap<String, String>();
//			sdkConfig.put("mode", "sandbox");
//
//			String accessToken = null;
//			Payment createdPayment = null;
//			try {
//
//				accessToken = new OAuthTokenCredential("AUWw-BAJGzoiybQ05I3uMMKDE8VxtBqkBZeeYUJpArrRHFCPLyrBax_zUim-", "EIC9QhD-m__03QFhcySpVlLWV8xvg0uHWqbIjU-RT1SQThbQRH0PAVE0J2oS", sdkConfig).getAccessToken();
//				
//				APIContext apiContext = new APIContext(accessToken);
//				apiContext.setConfigurationMap(sdkConfig);
//				
//				Amount amount = new Amount();
//				amount.setCurrency("USD");
//				amount.setTotal("2");
//				
//				Transaction transaction = new Transaction();
//				transaction.setDescription("creating payment test");
//				transaction.setAmount(amount);
//				
//				List<Transaction> transactions = new ArrayList<Transaction>();
//				transactions.add(transaction);
//				
//				Payer payer = new Payer();
//				payer.setPaymentMethod("paypal");
//				
//				Payment payment = new Payment();
//				payment.setIntent("sale");
//				payment.setPayer(payer);
//				payment.setTransactions(transactions);
//				
//				RedirectUrls redirectUrls = new RedirectUrls();
//				redirectUrls.setCancelUrl("https://devtools-paypal.com/guide/pay_paypal?cancel=true");
//				redirectUrls.setReturnUrl("https://devtools-paypal.com/guide/pay_paypal?success=true");
//				payment.setRedirectUrls(redirectUrls);
//				
//				createdPayment = payment.create(apiContext);
//				//
////				JSONObject json = new JSONObject(createdPayment.toString());
////				String payerID = json.getString("id");
////				
////				Payment pay = new Payment(payerID,payer);
////				PaymentExecution paymentExecute = new PaymentExecution();
////				paymentExecute.setPayerId("QWERTY");
////				
////				pay.execute(apiContext, paymentExecute);
//				//
//	
//				
//			} catch (PayPalRESTException e) {
//				
//				e.printStackTrace();
////			} catch (JSONException e) {
////				
////				e.printStackTrace();
//			}
			
			//return createdPayment.toString();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			
			//Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
			
			try {
				JSONObject json = new JSONObject(result);
				//JSONObject jason = json.getJSONObject("payer");
				
				JSONArray arrs = json.getJSONArray("links");
				
				List<String> hrefs = new ArrayList<String>();
				
				for (int i = 0; i < arrs.length(); i++) {
					
					String link = arrs.getString(i);
					JSONObject js = new JSONObject(link);
					hrefs.add(js.getString("href"));
				}
				
				etStatusInfo.setText(hrefs.get(1));
				
				String url = hrefs.get(1);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			super.onPostExecute(result);
		}
	}
 // =========================================================================
 // TODO Final
}
