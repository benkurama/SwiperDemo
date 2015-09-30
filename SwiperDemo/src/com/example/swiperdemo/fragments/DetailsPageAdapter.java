package com.example.swiperdemo.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.swiperdemo.R;
import com.example.swiperdemo.utils.Sets;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.Recipient;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.PageIndicator;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class DetailsPageAdapter extends PagerAdapter implements IconPagerAdapter{
 // =========================================================================
 // TODO Variables
 // =========================================================================
	private int PAGE_COUNT = 3;
	private String[] titles = new String[]{"Customer","Recepient", "Charges"};
	private FragmentActivity act;
	private View new_view;
	private PageIndicator indicator;
	private ListView lvCustList, lvRecipientList, lvChargeList;
	
	private static final int[] ICONS = new int[] {R.drawable.perm_group_calendar};
 // =========================================================================
 // TODO Setup Adapter
 // =========================================================================	
	public DetailsPageAdapter(FragmentActivity act, PageIndicator ind){
		this.act = act;
		this.indicator = ind;
		indicator.setOnPageChangeListener(pageChange);
	}
	
	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

	@Override
	public boolean isViewFromObject(View view, Object o) {
		return view == o;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		
		container.removeView((View) object);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		
		return titles[position];
	}
	
	@Override
	public int getIconResId(int index) {
		
		return ICONS[0];
	}
 // =========================================================================
 // TODO Initialize
 // =========================================================================
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		
		View new_view = null;
		LayoutInflater inflater = act.getLayoutInflater();
		
		switch (position) {
		case 0:
			new_view = inflater.inflate(R.layout.fragpager_customer, null);
			
			initCustomerPage(new_view);
			new GetCustomerAsync().execute();
			break;
		case 1:
			
			new_view = inflater.inflate(R.layout.fragpager_recepient, null);
			initRecipient(new_view);
			break;
		case 2:
			new_view = inflater.inflate(R.layout.fragpager_charges, null);
			initCharge(new_view);
			break;
		default:
			break;
		}
		//
		container.addView(new_view);
		return new_view;
	}

	private OnPageChangeListener pageChange = new OnPageChangeListener() {
		@Override
		public void onPageSelected(int pos) {
			switch (pos) {
			case 0:
				act.setProgressBarIndeterminateVisibility(true);
				new GetCustomerAsync().execute();
				
				break;
			case 1:
				act.setProgressBarIndeterminateVisibility(true);
				new GetRecipientAsync().execute();
				 
			case 2:
				act.setProgressBarIndeterminateVisibility(true);
				new GetChargesAsync().execute();
				break;
			}
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}
	};
 // =========================================================================
 // TODO Customer Group
 // =========================================================================	
	private void initCustomerPage(View v){
		
		lvCustList = (ListView)v.findViewById(R.id.lvCustomerList);
		act.setProgressBarIndeterminateVisibility(true);
		//
	}
	
	private class GetCustomerAsync extends AsyncTask<String, String, ArrayList<HashMap<String,String>>>{

		private ArrayList<HashMap<String,String>> mapList = new ArrayList<HashMap<String,String>>();
		
		@Override
		protected ArrayList<HashMap<String,String>> doInBackground(String... params) {
			//
			Stripe.apiKey = Sets.TEST_SECRET_KEY;
			//
			try {
				Map<String, Object> listParams = new HashMap<String, Object>();
				listParams.put("count", 50);
				List<Customer> customerList = Customer.all(listParams).getData();
				//
				
				for (Customer customer : customerList) {
					HashMap<String,String> map = new HashMap<String, String>();
					
					map.put("Name", customer.getMetadata().get("name"));
					mapList.add(map);
				}
				
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
			
			return mapList;
		}

		@Override
		protected void onPostExecute(ArrayList<HashMap<String,String>> result) {
			
			String[] Columns = new String[]{"Name"};
			int[] Widgets = new int[]{R.id.tvCustomerName};
			
			ListAdapter lvAdapter = new SimpleAdapter(act, result, R.layout.rows, Columns, Widgets);
			lvCustList.setAdapter(lvAdapter);
			
			act.setProgressBarIndeterminateVisibility(false);
			super.onPostExecute(result);
		}
		
	}
 // =========================================================================
 // TODO Recipient Group
 // =========================================================================	
	private void initRecipient(View v){
		lvRecipientList = (ListView)v.findViewById(R.id.lvRecipientList);
		//
	}
	
	private class GetRecipientAsync extends AsyncTask<String, String, List<HashMap<String, String>>>{

		@Override
		protected List<HashMap<String, String>> doInBackground(String... params) {
			//
			Stripe.apiKey = Sets.TEST_SECRET_KEY;
			List<HashMap<String, String>> mapList = new ArrayList<HashMap<String,String>>();
			//
			
			try {
				
				Map<String, Object> listParams = new HashMap<String, Object>();
				listParams.put("count", 50);
				List<Recipient> recList = Recipient.all(listParams).getData();
				
				for (Recipient recipient : recList) {
					HashMap<String, String> map = new HashMap<String, String>();
					
					map.put("Name", recipient.getName());
					mapList.add(map);
				}
				
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
			
			return mapList;
		}

		@Override
		protected void onPostExecute(List<HashMap<String, String>> result) {
			
			String[] Columns = new String[]{"Name"};
			int[] Widgets = new int[]{R.id.tvCustomerName};
			
			ListAdapter lvAdapter = new SimpleAdapter(act, result, R.layout.rows, Columns, Widgets);
			lvRecipientList.setAdapter(lvAdapter);
			act.setProgressBarIndeterminateVisibility(false);
			super.onPostExecute(result);
		}
		
	}
 // =========================================================================
 // TODO Charge Group
 // =========================================================================
	private void initCharge(View v){
		lvChargeList = (ListView)v.findViewById(R.id.lvCharges);
	}
	
	private class GetChargesAsync extends AsyncTask<String, String, List<HashMap<String, String>>>{

		@Override
		protected List<HashMap<String, String>> doInBackground(String... params) {
			Stripe.apiKey = Sets.TEST_SECRET_KEY;
			List<HashMap<String, String>> mapList = new ArrayList<HashMap<String,String>>();
			try {
				Map<String, Object> listParams = new HashMap<String, Object>();
				listParams.put("count", 50);
				
				List<Charge> chargeList = Charge.all(listParams).getData();
				
				for (Charge charge : chargeList) {
					
					com.stripe.model.Card card = null;
					card = charge.getCard();
					//
					String am = String.valueOf(charge.getAmount()) ;
					String decimal = am.substring(am.length() - 2, am.length());
					am = am.substring(0, am.length() -2 );
					am = am +"."+decimal;
					String value = card.getName() +" was charge " +am;
					//
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("Charge", value);
					mapList.add(map);
				}
				//
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
			
			return mapList;
		}

		@Override
		protected void onPostExecute(List<HashMap<String, String>> result) {
			
			String[] Columns = new String[]{"Charge"};
			int[] Widgets = new int[]{R.id.tvCustomerName};
			
			ListAdapter lvAdapter = new SimpleAdapter(act, result, R.layout.rows, Columns, Widgets);
			lvChargeList.setAdapter(lvAdapter);
			act.setProgressBarIndeterminateVisibility(false);
			
			super.onPostExecute(result);
		}
	}

}
