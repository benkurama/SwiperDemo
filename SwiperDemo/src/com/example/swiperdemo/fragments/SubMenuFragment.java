package com.example.swiperdemo.fragments;

import java.security.Timestamp;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.swiperdemo.MainContentAct;
import com.example.swiperdemo.R;
import com.example.swiperdemo.utils.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Account;
import com.stripe.model.Card;
import com.stripe.model.Event;
import com.stripe.model.EventData;

public class SubMenuFragment extends Fragment{
	 // =========================================================================
	 // TODO Variables
	 // =========================================================================
	private MainContentAct core;
	private FragmentActivity act;
	
	private Account Acc = null;
	
	private TextView tvAccEmail, tvAccName, tvAccCurr, tvAccZone;
	private EditText etEventLogs;
	private ProgressBar pbLoading;
	 // =========================================================================
	 // TODO Override
	 // =========================================================================
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_sub, container, false);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		act = getActivity();
		core = (MainContentAct)act;
		
		tvAccEmail = (TextView)getView().findViewById(R.id.tvAccEmail);
		tvAccName = (TextView)getView().findViewById(R.id.tvAccName);
		tvAccCurr = (TextView)getView().findViewById(R.id.tvAccCurrency);
		tvAccZone = (TextView)getView().findViewById(R.id.tvAccTimezone);
		etEventLogs = (EditText)getView().findViewById(R.id.etEventLogs);
		etEventLogs.setKeyListener(null);
		
		pbLoading = (ProgressBar)getView().findViewById(R.id.pbLoading);
		pbLoading.setVisibility(View.INVISIBLE);
		
		//new retrieveInfoAsync().execute();

	}
 // =========================================================================
 // TODO Main Functions
 // =========================================================================	
	public void callDetails(){
		new retrieveInfoAsync().execute();
		pbLoading.setVisibility(View.VISIBLE);
	}
 // =========================================================================
 // TODO Sub Functions
 // =========================================================================
	
 // =========================================================================
 // TODO Inner class
 // =========================================================================
	public class retrieveInfoAsync extends AsyncTask<String, String, String[]>{

		private String[] arrs = new String[5];
		
		@Override
		protected String[] doInBackground(String... params) {
			
			Stripe.apiKey = Sets.TEST_SECRET_KEY;
			
			Account acc = null;
			Map<String, Object> count = new HashMap<String, Object>();
			count.put("count", 10);
			List<Event> events = null;
			try {
				acc = Account.retrieve();
				events = Event.all(count).getData();
				
			} catch (AuthenticationException e) {
				e.printStackTrace();
			} catch (InvalidRequestException e) {
				e.printStackTrace();
			} catch (APIConnectionException e) {
				e.printStackTrace();
			} catch (CardException e) {
				e.printStackTrace();
			} catch (APIException e) {
				e.printStackTrace();
			}
			
			if (acc!=null) {
				arrs[0] = acc.getEmail();
				arrs[1] = acc.getDisplayName();
				arrs[2] = acc.getDefaultCurrency();
				arrs[3] = acc.getTimezone();
				
			}
			
			String res = "";
			if (events != null) {
				int x = 1;
				for (Event event : events) {
					
					try {
						String type = event.getType();
						res += x+". "+type + "\n\n";
						//
						String s = event.getData().getObject().toString();
						String[] sArr = s.split("JSON");
						s = "{data" + sArr[1] + "}";
						//
						JSONObject json =  new JSONObject(s);
						JSONObject js = json.getJSONObject("data");
						//
						long time = Long.parseLong(js.getString("created")) ;
						java.util.Date timeDate = new java.util.Date(time*1000);
						res += timeDate.toLocaleString()+"\n";
						//
						String[] typeArr = type.split("\\.");
						
						if(typeArr[0].equals("charge")){
							//
							String amount = js.getString("amount");
							String whole = amount.substring(0, amount.length() - 2);
							String dec = amount.substring(amount.length() - 2, amount.length());
							//
							JSONObject jsonCard = js.getJSONObject("card");
							String name = jsonCard.getString("name");
							//
							res += name+" was charge "+whole+"."+dec+"\n";
						}
				
					} catch (JSONException e) {
						e.printStackTrace();
					}
					res+= "-------------------------------\n";
					x++;
				}
				arrs[4] = res;
			}
			return arrs;
		}

		@Override
		protected void onPostExecute(String[] result) {
			
			tvAccEmail.setText(result[0]);
			tvAccName.setText(result[1]);
			tvAccCurr.setText(result[2]);
			tvAccZone.setText(result[3]);
			
			etEventLogs.setText(result[4]);
			
			pbLoading.setVisibility(View.INVISIBLE);
			
			super.onPostExecute(result);
		}
	}
	 // =========================================================================
	 // TODO Final
	
}
