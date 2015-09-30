package com.example.swiperdemo.objects;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CardData {
	// ---------------------------------------------------------------------
	// TODO VARIABLES
	// ---------------------------------------------------------------------
	private String stringData;
	private byte[] byteData;
	private byte[] encryptData;
	// ---------------------------------------------------------------------
	// TODO CONSTRUCTOR
	// ---------------------------------------------------------------------	
	public CardData(String data){
		if(data.length()%2 == 0){
			stringData = data;
			byteData = new byte[stringData.length()/2];
			convertStringToByte();
		}
	}
	
//	public CardData(byte[] msrData, byte[] encrypted){
//		stringData = "";
//		byteData = msrData;
//		encryptData = encrypted;
//		convertByteToString();
//	}
	
	public CardData(byte[] msrData){
		stringData = "";
		byteData = msrData;
		convertByteToString();
	}
	// ---------------------------------------------------------------------
	// TODO MAIN FUNCTIONS
	// ---------------------------------------------------------------------
	public String toString(){
		///
		///
		String encryptionType = "No Encryption";
		
		if (isEncryptedWithTDES()) {
			encryptionType = "TDES";
		} else if(isEncryptedWithAES()){
			encryptionType = "AES";
		}
		
		if (isDataEncrypted()) {
			return "Data is Encrypted \n" +
					"Encryption Type "+ encryptionType;
		} else {
			return "Data in Not Encrypted yet\n" +
					"Encryption Type "+encryptionType +"\n" +
					"Track 1 Data: "+getT1Data() +"\n\n"+
					"Track 1 Data: (Ascii)"+getT1DataAscii() +"\n\n" +
					"Track 2 Data: "+getT2Data() +"\n\n"+
					"Track 2 Data: (Ascii)"+getT2DataAscii() +"\n\n"+
					"Track 3 Data: "+getT3Data() +"\n\n"+
					"Track 3 Data: (Ascii)"+getT3DataAscii() +"\n\n"
					;
		}
		
	}
	
//	public String getAccountNumber(){
//		//
//		String data = getT2DataAscii();
//		String[] array = data.split("=");
//		//
//		String val = array[0].substring(1,array[0].length());
//		
//		return val;
//	}
//	
//	public String getValidYear(){
//		//
//		String data = getT2DataAscii();
//		String[] array = data.split("=");
//		//
//		String year = "20"+array[1].substring(0,2);
//		
//		return year;
//	}
//	
//	public String getValidMonth(){
//		//
//		String data = getT2DataAscii();
//		String[] array = data.split("=");
//		//
//		String month = array[1].substring(2,4);
//		return month;
//	}
//	
//	public String getAccountName(){
//		//
//		String data = getT1DataAscii();
//		String[] array = data.split("\\^");
//		
//		return array[1];
//	}
	
	public HashMap<String, String> getDetails(){
		//
		//String res = toString();
		//
		String data = getT1DataAscii();
		HashMap<String,String> details = null;
		//
		if(!data.equals("")){
			String[] array1 = data.split("\\^");
			String accountNum = array1[0].substring(2,array1[0].length());
			//
			String accountName = array1[1];
			String ctype = array1[0].substring(2,3);
			int cardType = Integer.parseInt(ctype);
			String cardStr = null;
			switch (cardType) {
			case 3:
				cardStr = "Amex";
				break;
			case 4:
				cardStr = "Visa";
				break;
			case 5:
				cardStr = "MasterCard";
				break;
			case 6:
				cardStr = "Discover";
				break;
			default:
				cardStr = "Other Brand";
				break;
			}
			//
			String year = "20" +array1[2].substring(0, 2);
			String month = array1[2].substring(2,4);
			String cvv = array1[2].substring(array1[2].length()-10, array1[2].length()-7);
			//
			details = new HashMap<String, String>();
			details.put("accountnumber", accountNum);
			details.put("accountname", accountName);
			details.put("year", year);
			details.put("month", month);
			details.put("cardtype", cardStr);
			details.put("cvv", cvv);
		}
		//
		return details;
	}
	
	public String getSwipeData(){
		
//		String data = getT2DataAscii();
//		
//		String res = null;
//		
//		if (!data.equals("")) {
//			String[] array1 = data.split("\\?");
//			res = array1[1];
//		}
		
		return getT2DataAscii();
	}
	// ---------------------------------------------------------------------
	// TODO SUB FUNCTIONS
	// ---------------------------------------------------------------------
	private void convertStringToByte(){
		for (int i = 0; i < stringData.length()/2; i++) {
			//
			byteData[i] = (byte) ((Character.digit(stringData.charAt(i*2), 16) << 4) + Character.digit(stringData.charAt(i*2+1), 16));
		}
	}
	
	private void convertByteToString(){
		//
		String str = null;
		StringBuffer hexString = new StringBuffer();
		
		for (int i = 0; i < byteData.length; i++) {
			//
			str = Integer.toHexString(0xFF & byteData[i]);
			if (str.length() == 1) {
				str = "0"+str;
			}
			hexString.append(str);
		}
		stringData = hexString.toString();
	}
	
	public boolean isDataEncrypted(){
		if (byteData[0]==0x25 && byteData[byteData.length-1]==0x0d)
			return false;
		if (byteData[0]==0x02 && byteData[byteData.length-1]==0x03)
			return true;
		return false;
	}
	
//	public boolean isDataEncrypted(){
//		if (encryptData[0]==0x25 && encryptData[encryptData.length-1]==0x0d)
//			return false;
//		if (encryptData[0]==0x02 && encryptData[encryptData.length-1]==0x03)
//			return true;
//		return false;
//	}
	
	public boolean isEncryptedWithTDES() {
		if (!isDataEncrypted())
			return false;
		String binaryString = getFieldByte1Binary();
		while (binaryString.length()<8)
			binaryString = "00"+binaryString;
		if (binaryString.substring(2,4).equals("00"))
			return true;
		return false;	
	}
	
	public String getFieldByte1Binary() {
		String binaryString =  hexToBin(getFieldByte1());
		while (binaryString.length()<8)
			binaryString = "00"+binaryString;
		return binaryString;
	}
	
	public String getFieldByte1 () {
		if (!isDataEncrypted())
			return null;
		return stringData.substring(16, 18);
	}
	
	public String hexToBin (String hex){
		return new BigInteger(hex, 16).toString(2);
	}
	
	public boolean isEncryptedWithAES() {
		if (!isDataEncrypted())
			return false;
		String binaryString = getFieldByte1Binary();
		if (binaryString.length()!=8)
			return false;
		if (binaryString.substring(2,4).equals("01"))
			return true;
		return false;
	}
	
	public String getT1Data() {
		if (!isDataEncrypted()) {
			int endIndex = stringData.indexOf("3f"); 
			return stringData.substring(0, endIndex+2);
		}
		if (!isT1DataPresent())
			return null;
		return stringData.substring(20, (getTrack1Length()+10)*2);
	}
	
	public boolean isT1DataPresent() {
		if (!isDataEncrypted())
			return false;
		String binaryString = getFieldByte1Binary();
		if (binaryString.length()!=8)
			return false;
		if (binaryString.charAt(7)=='1')
			return true;
		return false;
	}
	
	public int getTrack1Length() {
		if (!isDataEncrypted())
			return getT1Data().length()/2;
		return (int) byteData[5];
	}
	
	public String getT1DataAscii() {
		StringBuffer sf = new StringBuffer();
		if (!isDataEncrypted()) {
			for (int i=0; i<getT1Data().length()/2; i++){
				sf.append((char)byteData[i]);
			}
			return sf.toString();
		}
		if (!isT1DataPresent())
			return null;
		
		for (int i=10; i<getTrack1Length()+10; i++){
			sf.append((char)byteData[i]);
		}
		return sf.toString();
	}
	
	public String getT2Data() {
		if (!isDataEncrypted()) {
			int startIndex = stringData.indexOf("3b");
			int endIndex1 = stringData.indexOf("3f");
			int endIndex2 = stringData.indexOf("3f",endIndex1+1);
			if (endIndex2 > endIndex1 && startIndex > 0)
				return stringData.substring(startIndex, endIndex2+2);
			else 
				return null;
		}
		if (!isT1DataPresent())
			return null;
		return stringData.substring((getTrack1Length()+10)*2, (getTrack1Length()+10)*2+getTrack2Length()*2);
	}
	
	public int getTrack2Length() {
		if (!isDataEncrypted())
			return getT2Data().length()/2;
		return (int) byteData[6];
	}
	
	public String getT2DataAscii() {
		StringBuffer sf = new StringBuffer();
		if (!isDataEncrypted()) {
			String strData = getT2Data();
			if(null!=strData)
			{
				if (strData.length()>0) {
					int startIndex = stringData.indexOf("3b");
					int endIndex = stringData.lastIndexOf("3f")+2;
					for (int i=startIndex/2; i<endIndex/2; i++){
						sf.append((char)byteData[i]);
					}
					return sf.toString();
				} else 
					return null;
			}
		}
		if (!isT1DataPresent())
			return null;
		for (int i=getTrack1Length()+10; i<getTrack1Length()+10+getTrack2Length(); i++){
			sf.append((char)byteData[i]);
		}
		return sf.toString();
	}	
	
	public String getT3Data() {
		if (!isDataEncrypted()) {
			int startIndex1 = stringData.indexOf("3b");
			int startIndex2 = stringData.indexOf("3b",startIndex1+1);
			int endIndex1 = stringData.indexOf("3f");
			int endIndex2 = stringData.indexOf("3f", endIndex1+1);
			
			int endIndex3 = stringData.lastIndexOf("3f");
			if ((endIndex3 > endIndex2) && (startIndex1 > 0&&startIndex2>startIndex1))
				return stringData.substring(startIndex2, endIndex3+2);
			else 
				return null;
		}
		if (!isT1DataPresent())
			return null;
		return stringData.substring((getTrack1Length()+10)*2+getTrack2Length()*2, (getTrack1Length()+10)*2+getTrack2Length()*2+getTrack3Length()*2);
	}
	
	public int getTrack3Length() {
		if (!isDataEncrypted())
			return 0;
		return (int) byteData[7];
	}
	
	public String getT3DataAscii() {
		StringBuffer sf = new StringBuffer();
		if (!isDataEncrypted()) {
			String strData = getT3Data();
			if(null!=strData)
			{
				if (strData.length()>0) {
					
					int startIndex1 = stringData.indexOf("3b");
					int startIndex2 = stringData.indexOf("3b",startIndex1+1);

					int endIndex3 = stringData.lastIndexOf("3f");
					
					for (int i=startIndex2/2; i<endIndex3/2+1; i++){
						sf.append((char)byteData[i]);
					}
					return sf.toString();
				} else 
					return null;
			}
		}
		if (!isT1DataPresent())
			return null;
		for (int i=getTrack1Length()+10+10+getTrack2Length(); i<getTrack1Length()+10+getTrack2Length()+getTrack3Length(); i++){
			sf.append((char)byteData[i]);
		}
		return sf.toString();
	}	
	// ---------------------------------------------------------------------
	// FINAL BANGUMI
}
