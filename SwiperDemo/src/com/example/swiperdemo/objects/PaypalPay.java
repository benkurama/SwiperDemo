package com.example.swiperdemo.objects;

import java.util.UUID;

import com.example.swiperdemo.MainContentAct;
import com.example.swiperdemo.fragments.ProgressDialogFragment;

import paypal.payflow.PayflowAPI;
import paypal.payflow.SDKProperties;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.EditText;

public class PaypalPay {

	private Context core;
	private EditText et;
	@SuppressWarnings("static-access")
	private ProgressDialogFragment prog = new ProgressDialogFragment().newInstance("Loading","Payment is process...");
	
	
	public PaypalPay(Context core, EditText txt, String dataSwipe){
		this.core = core;
		this.et = txt;
		//
		prog.show(((MainContentAct)this.core).getSupportFragmentManager(), "progress");
		new PaymentGateway().execute(dataSwipe);
	}
	
	private class PaymentGateway extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... params) {
			
			SDKProperties.setHostAddress("pilot-payflowpro.paypal.com");
			SDKProperties.setHostPort(443);
			SDKProperties.setTimeOut(45);
			//
			PayflowAPI pa = new PayflowAPI();
			//
			// for basic credit card fill up
//			String request = "USER=benkurama&VENDOR=benkurama&PARTNER=PayPal&PWD=Redfoot123_" +
//					"&TRXTYPE=S&TENDER=C" +
//					"&ACCT=6011000990139424&EXPDATE=0119&CVV2=123" +
//					"&FIRSTNAME=Red&LASTNAME=Foot&STREET=123 Main St.&ZIP=12345"+
//					"&INVNUM=INV12345&PONUM=PO12345" +
//					"&AMT=2.00";
			
			// for Credit Transaction "refund" - non-referenced credit not allowed
//			String request = "USER=benkurama&VENDOR=benkurama&PARTNER=PayPal&PWD=Redfoot123_" +
//			"&TRXTYPE=C&TENDER=C&ORIGID=A12A7CADFE00";
			
			// for Inquiry Method
//			String request = "USER=benkurama&VENDOR=benkurama&PARTNER=PayPal&PWD=Redfoot123_" +
//			"&TRXTYPE=I&TENDER=C&ORIGID=A71A7AF64A7C&VERBOSITY=HIGH";
			
					 
			// for Swipe methods for sales
			String request = "USER=benkurama&VENDOR=benkurama&PARTNER=PayPal&PWD=Redfoot123_" +
			"&TRXTYPE=S&TENDER=C" +
			"&SWIPE="+params[0]+		
			"&FIRSTNAME=Red&LASTNAME=Foot&STREET=123 Main St.&ZIP=12345"+
			"&INVNUM=INV12345&PONUM=PO12345" +
			"&AMT=2.00";
			
			// for Swipe methods for Authorization
//			String request = "USER=benkurama&VENDOR=benkurama&PARTNER=PayPal&PWD=Redfoot123_" +
//			"&TRXTYPE=A&TENDER=C" +
//			"&SWIPE="+params[0]+
//			"&AMT=10.00";
			
			// for Swipe methods for Delayed Captured
//			String request = "USER=benkurama&VENDOR=benkurama&PARTNER=PayPal&PWD=Redfoot123_" +
//			"&TRXTYPE=D&TENDER=C&ORIGID=A71A7AF64A7C" +
//			"&AMT=6.00";
			
			// for Swipe methods for Reference Transaction
//			String request = "USER=benkurama&VENDOR=benkurama&PARTNER=PayPal&PWD=Redfoot123_" +
//			"&TRXTYPE=S&TENDER=C&ORIGID=A71A7AF64A7C" +
//			"&AMT=4.00";
			
			// for Swipe methods To Void Transactions while not settled
//			String request = "USER=benkurama&VENDOR=benkurama&PARTNER=PayPal&PWD=Redfoot123_" +
//			"&TRXTYPE=V&TENDER=C&ORIGID=A71A7AF3B7B1" +
//			"&VERBOSITY=MEDIUM";
			
			UUID uid = UUID.randomUUID();

	        String response = pa.submitTransaction(request, uid.toString());
	        
	        String transErrors = pa.getTransactionContext().toString();
	        if (transErrors != null && transErrors.length() > 0) {
	            response = transErrors;
	        }
			
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			//
			et.setText(result);
			prog.dismiss();
			super.onPostExecute(result);
		}
		
	}
}
