package com.example.swiperdemo.objects;

import java.util.HashMap;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.Toast;



import paypal.payflow.BillTo;
import paypal.payflow.CardTender;
import paypal.payflow.ClientInfo;
import paypal.payflow.CreditTransaction;
import paypal.payflow.Invoice;
import paypal.payflow.PayflowAPI;
import paypal.payflow.PayflowConnectionData;
import paypal.payflow.PayflowUtility;
import paypal.payflow.Response;
import paypal.payflow.SDKProperties;
import paypal.payflow.TransactionResponse;
import paypal.payflow.UserInfo;

public class DoSwipe {
	
	Context cr = null;
	EditText et = null;
	
	public DoSwipe(Context core, EditText t){
		
		cr = core;
		et = t;
		
		new Background().execute();
		//new GroundBack().execute();
	
		
		
	}
	
	private class GroundBack extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... params) {
			
			String paymentAmount = "1.0";
			String paymentType = "Sale";
			String creditCardType = "Visa"; 
			String creditCardNumber = "4111111111111111";
			String expDate = "122014";
			String cvv2 = "962"; 
			String firstName = "Jon"; 
			String lastName  = "Doe";  
			String street = "1 Main St";  
			String city = "San Jose";  
			String state = "CA";  
			String zip = "95131"; 
			String countryCode = "US"; 
			String currencyCode = "USD";
			String orderDescription = "order description"; 
			
			@SuppressWarnings("rawtypes")
			HashMap nvp = new PaypalFunctions().DirectPayment(paymentType, paymentAmount, creditCardType, creditCardNumber, expDate, cvv2, firstName, lastName, street, city, state, zip, countryCode, currencyCode, orderDescription);
			
			String msg = null;
			
			String strAck = nvp.get("RESULT").toString();
			
			msg = strAck;
			
			if(strAck ==null || !strAck.equalsIgnoreCase("0"))
			{
				String ErrorCode = strAck;
				String ErrorMsg = nvp.get("RESPMSG").toString();
				msg = ErrorMsg;
			} 
			return msg;
		}

		@Override
		protected void onPostExecute(String result) {
			 
			et.setText(result);
			
			super.onPostExecute(result);
		}
		
	}
	
	private class Background extends AsyncTask<String, String, String>{
		
		

		@SuppressLint("UseValueOf")
		@Override
		protected String doInBackground(String... params) {
			
			String msg = "";
			
			SDKProperties.setHostAddress("pilot-payflowpro.paypal.com");
			SDKProperties.setHostPort(443);
			SDKProperties.setTimeOut(45);
			
//			UserInfo user = new UserInfo("payflowdevtools", "payflowdevtools", "PayPal","passw0rd123");
//			
//			PayflowConnectionData connection = new PayflowConnectionData();
//			
//			
//			
//			Invoice inv = new Invoice();
//			
//			paypal.payflow.Currency amt = new paypal.payflow.Currency(new Double(2.00));
//			inv.setAmt(amt);
//			inv.setPoNum("PO12345");
//			inv.setInvNum("INV12345");
//			
//			BillTo bill = new BillTo();
//			bill.setStreet("123 Main St.");
//			bill.setZip("12345");
//			inv.setBillTo(bill);
//			
//			String msg = "";
//			
//			paypal.payflow.CreditCard cc = new paypal.payflow.CreditCard("5105105105105100", "1019");
//			
//			
//			CardTender card = new CardTender(cc);
//			
//			UUID uid = UUID.randomUUID();
//			
//			CreditTransaction trans = new CreditTransaction(user, connection, inv, card, uid.toString());
//			
//			
//			
//			Response resp = trans.submitTransaction();
//			
//			if (resp != null) {
//				TransactionResponse trnsRes = resp.getTransactionResponse();
//				
//				ClientInfo cInfo = new ClientInfo();
//				
//				trans.setClientInfo(cInfo);
//				
//				msg += trnsRes.getResult()+ " : ";
//				msg += trnsRes.getPnref()+ " : ";
//				msg += trnsRes.getRespMsg()+ " : ";
//				msg += trnsRes.getAuthCode()+ " : ";
//				msg += trnsRes.getAvsAddr()+ " : ";
//				msg += trnsRes.getAvsZip()+ " : ";
//				msg += trnsRes.getIavs()+ " : ";
//				msg += trnsRes.getCvv2Match()+ " : ";
//				
//				
//			}
			
			PayflowAPI pa = new PayflowAPI();
			 
			// for basic credit card fill up
//			String request = "USER=benkurama&VENDOR=benkurama&PARTNER=PayPal&PWD=Redfoot123_" +
//					"&TRXTYPE=S&TENDER=C" +
//					"&ACCT=5100000000000008&EXPDATE=0119&CVV2=123" +
//					"&FIRSTNAME=Red&LASTNAME=Foot&STREET=123 Main St.&ZIP=12345"+
//					"&INVNUM=INV12345&PONUM=PO12345" +
//					"&AMT=2.00" +
//					"&VERBOSITY=HIGH";
			// for Swipe methods
			String request = "USER=benkurama&VENDOR=benkurama&PARTNER=PayPal&PWD=Redfoot123_" +
			"&TRXTYPE=S&TENDER=C" +
			"&SWIPE=;5105105105105100=15121011000012345678?"+
			"&AMT=2.00" +
			"&VERBOSITY=HIGH";
			
			//String requestId = pa.generateRequestId();
			
			UUID uid = UUID.randomUUID();

	        String response = pa.submitTransaction(request, uid.toString());
	        
	        String transErrors = pa.getTransactionContext().toString();
	        if (transErrors != null && transErrors.length() > 0) {
	            
	        }
			
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(cr, result, Toast.LENGTH_LONG).show();
			et.setText(result);
			super.onPostExecute(result);
		}
		
	}
}
