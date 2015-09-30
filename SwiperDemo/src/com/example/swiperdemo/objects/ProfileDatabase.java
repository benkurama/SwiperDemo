package com.example.swiperdemo.objects;

import java.text.SimpleDateFormat;
import java.util.Date;

import IDTech.MSR.XMLManager.StructConfigParameters;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

public class ProfileDatabase {
	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	Context core = null;
	private SQLiteDatabase db = null;
	private StructConfigParameters profile = null;
	private Handler handler = new Handler();
	
	private boolean isUseAutoConfigProfileChecked = false;
	private Cursor cursor = null;
	//
	private static final String DB_NAME = "REDFOOTTECH.AutoConfig";
	private static final String DB_PROFILE = "profiles";
	// ---------------------------------------------------------------------
	// CONSTRUCTOR
	// ---------------------------------------------------------------------	
	public ProfileDatabase(Context core){
		this.core = core;
	}
	// ---------------------------------------------------------------------
	// MAIN FUNCTION
	// ---------------------------------------------------------------------
	public void initializedDB()
	{
		
		db = core.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS " +DB_PROFILE+ "( " +
				"search_date DATETIME, "+
        		"direction_output_wave INTEGER, "+
        		"input_frequency INTEGER, "+
        		"output_frequency INTEGER, "+
        		"record_buffer_size INTEGER, "+
        		"read_buffer_size INTEGER, "+
        		"wave_direction INTEGER, "+
        		"high_threshold INTEGER, "+
        		"low_threshold INTEGER, "+
        		"min INTEGER, "+
        		"max INTEGER, "+
        		"baud_rate INTEGER, "+
        		"preamble_factor INTEGER, "+
        		"set_default INTEGER,"+
        		"shuttle_channel INTEGER,"+
        		"headset_force_plug INTEGER,"+
         		"use_voice_recognizition INTEGER,"+
         		"volumeLevelAdjust INTEGER)"
				);
		//temporay code only
		handler.post(donInsertResultIntoDB2);
		//
		isUseAutoConfigProfileChecked = useAutoConfigProfileAsDefault();
		/// test
//		ContentValues insertValues = new ContentValues();
//		
//		SimpleDateFormat dateFormat = new SimpleDateFormat();
//		Date date = new Date();
//		 
//		insertValues.put("search_date", dateFormat.format(date));
//		
//		db.execSQL("delete from "+ DB_PROFILE);
//		db.insert(DB_PROFILE, null, insertValues);
	}
	
	public boolean updateProfileFromDB(){
		
		try{
			cursor = db.rawQuery("select * from "+DB_PROFILE + " order by search_date", null);
			
			if(cursor.moveToFirst()){
				//
				profile = new StructConfigParameters();
				profile.setDirectionOutputWave((short) cursor.getInt(1));
	        	profile.setFrequenceInput(cursor.getInt(2));
	        	profile.setFrequenceOutput(cursor.getInt(3));
	        	profile.setRecordBufferSize(cursor.getInt(4));
	        	profile.setRecordReadBufferSize(cursor.getInt(5));
	        	profile.setWaveDirection(cursor.getInt(6));
	        	profile.sethighThreshold((short) cursor.getInt(7));
	        	profile.setlowThreshold((short) cursor.getInt(8));
	        	profile.setMin((short) cursor.getInt(9));
	        	profile.setMax((short) cursor.getInt(10));
	        	profile.setBaudRate(cursor.getInt(11));
	        	profile.setPreAmbleFactor((short) cursor.getInt(12));
	        	profile.setShuttleChannel((byte)cursor.getInt(13));
	        	profile.setForceHeadsetPlug((short)cursor.getInt(14));	        	
	        	profile.setUseVoiceRecognition((short)cursor.getInt(15));	        	
	        	profile.setVolumeLevelAdjust((short)cursor.getInt(16));
	        	//
	        	cursor.close();
			} else {
				return false;
			}
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		 return true;
	}
	
	public void closeDB(){
		//
		if(cursor!=null)
			cursor.close();
		if (db!=null)
			db.close();
		cursor = null;
		db = null;
	}
	// ---------------------------------------------------------------------
	// SUB FUNCTION
	// ---------------------------------------------------------------------
	private boolean useAutoConfigProfileAsDefault(){
		
		try{
			cursor = db.query(DB_PROFILE, new String[]{"set_default"},
					null, null, null, null, "search_date");
			
			if(cursor.moveToFirst()){
				if(cursor.getInt(0) == 1){
					cursor.close();
					return true;
				}
				
				if(cursor != null) cursor.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean getIsUseAutoConfigProfile(){
		return isUseAutoConfigProfileChecked;
	}
	
	public StructConfigParameters getProfile(){
		return profile;
	}
	
	public void setProfile(StructConfigParameters profile){
		this.profile = profile;
	}
	
	public void insertResultIntoDB(){
		handler.post(donInsertResultIntoDB);
	}
	
	private Runnable donInsertResultIntoDB = new Runnable() {
		@SuppressLint("SimpleDateFormat")
		@Override
		public void run() {
			
			 if(profile == null){
				 return;
			 }
			 
			 ContentValues insertValues = new ContentValues();
			 
			 SimpleDateFormat dateFormat = new SimpleDateFormat();
			 Date date = new Date();
			 
			 insertValues.put("search_date", dateFormat.format(date));
			 insertValues.put("direction_output_wave", profile.getDirectionOutputWave());
			 insertValues.put("input_frequency", profile.getFrequenceInput());
			 insertValues.put("output_frequency", profile.getFrequenceOutput());
			 insertValues.put("record_buffer_size", profile.getRecordBufferSize());
			 insertValues.put("read_buffer_size", profile.getRecordReadBufferSize());
			 insertValues.put("wave_direction", profile.getWaveDirection());
			 insertValues.put("high_threshold", profile.gethighThreshold());
			 insertValues.put("low_threshold", profile.getlowThreshold());
			 insertValues.put("min", profile.getMin());
			 insertValues.put("max", profile.getMax());
			 insertValues.put("baud_rate", profile.getBaudRate());
			 insertValues.put("preamble_factor", profile.getPreAmbleFactor());
			 insertValues.put("shuttle_channel", profile.getShuttleChannel());
			 insertValues.put("headset_force_plug", profile.getForceHeadsetPlug());
			 insertValues.put("use_voice_recognizition", profile.getUseVoiceRecognition());
			 insertValues.put("volumeLevelAdjust", profile.getVolumeLevelAdjust());
			 //
			 insertValues.put("set_default", 1);
			 
			 try{
				 //
				 db.execSQL("delete from "+ DB_PROFILE);
				 db.insert(DB_PROFILE, null, insertValues);
				 //
			 }catch(Exception e){
				 e.printStackTrace();
			 }
		}
	};
	
	private Runnable donInsertResultIntoDB2 = new Runnable() {
		@Override
		public void run() {
//			if(profile == null){
//				 return;
//			 }
			 
			 ContentValues insertValues = new ContentValues();
			 
			 SimpleDateFormat dateFormat = new SimpleDateFormat();
			 Date date = new Date();
			 
			 insertValues.put("search_date", dateFormat.format(date));
			 insertValues.put("direction_output_wave", 1);
			 insertValues.put("input_frequency", 48000);
			 insertValues.put("output_frequency", 48000);
			 insertValues.put("record_buffer_size", 8192);
			 insertValues.put("read_buffer_size", 163840);
			 insertValues.put("wave_direction", 1);
			 insertValues.put("high_threshold", 4000);
			 insertValues.put("low_threshold", -4000);
			 insertValues.put("min", 2);
			 insertValues.put("max", 8);
			 insertValues.put("baud_rate", 9600);
			 insertValues.put("preamble_factor", 2);
			 insertValues.put("shuttle_channel", -10000);
			 insertValues.put("headset_force_plug", 10000);
			 insertValues.put("use_voice_recognizition", -2000);
			 insertValues.put("volumeLevelAdjust", 2000);
			 //
			 insertValues.put("set_default", 1);
			 
			 try{
				 //
				 db.execSQL("delete from "+ DB_PROFILE);
				 db.insert(DB_PROFILE, null, insertValues);
				 //
			 }catch(Exception e){
				 e.printStackTrace();
			 }
		}
	};
}
