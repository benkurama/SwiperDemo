package com.example.swiperdemo.objects;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Base64DataException;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;

public class Encription {
	//NOTES: KEY MUST BE 32 OR 48 CHAR.
	//IT CAN BE 16, BUT REPEAT FIRST 16 TO MAKE IT 32.
	private static final String KEY="0123456789ABCDEFFEDCBA9876543210";
	private static final String ALGORITHM = "DESede";
	private static final String CIPHER_PARAMETERS = "DESede/CBC/PKCS5Padding";

	private static final byte[] HEX_EIGHT_ZEROS = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
	private static final char[] HEXTAB = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static final byte[] KEY_BYTES = new byte[24];
	private static final IvParameterSpec ivSpec1 = new IvParameterSpec(HEX_EIGHT_ZEROS);
	
    private static String strFinalKey = KEY;
	static {
		if (strFinalKey.length() == 32) {
			strFinalKey = strFinalKey.concat(strFinalKey.substring(0, 16));
		}
	}
    private static final String KEY_LOWER = strFinalKey.toLowerCase();
	static{
		init();
	}

	public Encription() {
	}

	private static void init(){
		Encription objSelf = new Encription();
		objSelf.binHexToBytes(KEY_LOWER, KEY_BYTES, 0, KEY_BYTES.length);
	}//
	
	
	public byte[] cryptTest(String msg, String secretKey){
		//
		String base64EncryptedString = null;
		byte[] base64Bytes = null;
		try {
			
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
			byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
			
			SecretKey key = new SecretKeySpec(keyBytes, "DESede");
			Cipher cipher = Cipher.getInstance("DESede");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			
			byte[] plainTextBytes = msg.getBytes("utf-8");
			byte[] buf = cipher.doFinal(plainTextBytes);
			base64Bytes = Base64.encode(buf, 0);
			base64EncryptedString = new String(base64Bytes);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		//
		return base64Bytes;
	}
	
	public String testingConvert(){
		
		
		return null;
	}
	
	public String encrypt(String str){
		//
		String strEncryptedPassword = null;
		byte[] bytesFormat = str.getBytes();
		
		try {
			if (binHexToBytes(KEY_LOWER, KEY_BYTES, 0, KEY_BYTES.length) != (KEY_BYTES.length)) {
			}
			
			SecretKey desEdeKey = new SecretKeySpec(KEY_BYTES, ALGORITHM);
			Cipher desEdCipher = Cipher.getInstance(CIPHER_PARAMETERS);
			desEdCipher.init(Cipher.ENCRYPT_MODE, desEdeKey, ivSpec1);
			byte[] cipherText = desEdCipher.doFinal(bytesFormat);
			strEncryptedPassword = bytesToBinHex(cipherText, 0, cipherText.length);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return strEncryptedPassword;
	}
	
	public byte[] encryptByte(byte[] data){
		//
		byte[] cipherText = null;
		try {
			if (binHexToBytes(KEY_LOWER, KEY_BYTES, 0, KEY_BYTES.length) != (KEY_BYTES.length)) {
			}
			
			SecretKey desEdeKey = new SecretKeySpec(KEY_BYTES, ALGORITHM);
			Cipher desEdCipher = Cipher.getInstance(CIPHER_PARAMETERS);
			desEdCipher.init(Cipher.ENCRYPT_MODE, desEdeKey, ivSpec1);
			cipherText = desEdCipher.doFinal(data);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
		return cipherText;
	}
	
    private String bytesToBinHex(byte[] data, int nStartPos, int nNumOfBytes) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.setLength(nNumOfBytes << 1);
	
		int nPos = 0;
		for (int nI = 0; nI < nNumOfBytes; nI++) {
			sbuf.setCharAt(nPos++, HEXTAB[(data[nI + nStartPos] >> 4) & 0x0f]);
			sbuf.setCharAt(nPos++, HEXTAB[data[nI + nStartPos] & 0x0f]);
		}
		return sbuf.toString();
    }

	private int binHexToBytes(String sBinHex, byte[] data, int nSrcPos, int nNumOfBytes) {
		// Dest pos set to zero.
		int nDstPos = 0;
		// check for correct ranges
		int nStrLen = sBinHex.length();
		int nAvailBytes = (nStrLen - nSrcPos) >> 1;
		if (nAvailBytes < nNumOfBytes) {
			nNumOfBytes = nAvailBytes;
		}
		int nOutputCapacity = data.length;
		if (nNumOfBytes > nOutputCapacity) {
			nNumOfBytes = nOutputCapacity;
		}
		// convert now
		int nResult = 0;
		for (int nI = 0; nI < nNumOfBytes; nI++) {
			byte bActByte = 0;
			boolean blConvertOK = true;
			for (int nJ = 0; nJ < 2; nJ++) {
				bActByte <<= 4;
				char cActChar = sBinHex.charAt(nSrcPos++);

				if ((cActChar >= 'a') && (cActChar <= 'f')) {
					bActByte |= (byte) (cActChar - 'a') + 10;
				} else if ((cActChar >= 'A') && (cActChar <= 'F')) {
					bActByte |= (byte) (cActChar - 'A') + 10;
				} else {
					if ((cActChar >= '0') && (cActChar <= '9')) {
						bActByte |= (byte) (cActChar - '0');
					} else {
						blConvertOK = false;
					}
				}
			}
			if (blConvertOK) {
				data[nDstPos++] = bActByte;
				nResult++;
			}
		}
		return nResult;
	}
}
