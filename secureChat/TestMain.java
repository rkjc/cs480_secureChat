package secureChat;
import java.util.*; 
import java.math.*;
import java.nio.charset.Charset;

import javax.xml.bind.DatatypeConverter;

public class TestMain {

	public static void main(String[] args) {
		
													
//		byte[] byt = new byte[16];
//		//byte[] byt = zeros.getBytes();
//		
//		System.out.println(byt.length);
//		String msg = "less_than_16";
//		byte[] msgByt = msg.getBytes();
//		System.arraycopy( msgByt, 0, byt, 0, msgByt.length );
//		System.out.println(byt.length);
//		String str = new String(byt);
//		System.out.print(str);
//		System.out.println("|");
//		for(int i = 0; i < 16; i++){
//			System.out.println((char)byt[i]);		
//		}
//		
//		System.out.println("--end--");
		
//	/*	###################################################################
		System.out.println("is this going to work?");
		
		//simulated input string in client
		String testString = "Is this going to be able to be turned into a binint, then to a byte, then back to a bigint then to a string again?";
		
		System.out.println(testString);
		System.out.println(testString.length());
		int len = testString.length();
		
		//convert string to be sent to byte form
		byte[] byteMessage = new byte[128];
		System.out.println(byteMessage.length);
		byteMessage = testString.getBytes();	
		System.out.println(byteMessage);
		System.out.println(byteMessage.length);
		int messLen = byteMessage.length;
		//encrypt-decrypt using byte[]
		
		byte[] bkey = DatatypeConverter.parseHexBinary("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		byte[] cypherByteMessage = new byte[byteMessage.length];
		for(int i = 0; i < messLen -1; i++)
		{			
			cypherByteMessage[i] = (byte) ((byteMessage[i] ^ bkey[i]) & 0xFF);
			//System.out.println(i + "  " + byteMessage[i] + "  " + bkey[i] +  "  " +  cypherByteMessage[i] );
		}
		cypherByteMessage[messLen -1] = (byte) (byteMessage[messLen -1] ^  0xFF);
		System.out.println("");
		
		//convert byte to BigInteger for encryption
		BigInteger bintMessage = new BigInteger(byteMessage);
		System.out.println(bintMessage);
		
		//simulated encryption key
		
		// using bikey causes decryption problems, probably because of sign bit in BigInteger
		System.out.println(bkey.length);
		BigInteger key = new BigInteger("123456789");
		BigInteger bikey = new BigInteger(bkey);
		//encrypted
		BigInteger cypherIntOut = bintMessage.xor(key);
		
		//convert encrypted message to string to transmit
		byte[] cypherByteOut = cypherIntOut.toByteArray();
		System.out.println(cypherByteOut.length);
		String cypherTxt = new String(cypherByteOut);
		
		//byte to string ver
		String cypherTxt2 = new String(cypherByteMessage);
		
		// this is then sent through chat channel
		
		//recived text message is then converted to byte form
		byte[] cypherByteIn = cypherTxt.getBytes();
		//converted to BigInteger
		BigInteger cypherIntIn = new BigInteger(cypherByteIn);
		//decrypted
		BigInteger plainInt = cypherIntIn.xor(key);
		//converted back to byte form (BigInteger does not convert to string well)
		byte[] printBytes = plainInt.toByteArray();
		System.out.println(printBytes.length);
		//convert to string for printing to chat console
		String printString = new String(printBytes);
		System.out.println(printString);
		
		//alt pure byte version;
		byte[] cypherByteIn2 = cypherTxt2.getBytes();
		byte[] cypherByteMessageIn = new byte[cypherByteIn2.length];;
		for(int i = 0; i < messLen - 1; i++)
		{			
			cypherByteMessageIn[i] = (byte) ((cypherByteIn2[i] ^ bkey[i]));
			//System.out.println(i + "  " + cypherByteIn2[i] + "  " + bkey[i] +  "  " +  cypherByteMessageIn[i] );
		}
		cypherByteMessageIn[messLen -1] = (byte) ((cypherByteIn2[messLen -1] ^ 0x00));
		
		String printString2 = new String(cypherByteMessageIn);
		System.out.println(printString2);
		
		
		
		
		//##################################
		
		
		String plainText = "hello world, there are sheep";
		Charset charSet = Charset.forName("UTF-8");
		byte[] plainBytes = plainText.getBytes(charSet);
		//String key3 = "secret";
		//byte[] keyBytes = key3.getBytes(charSet);
		byte[] keyBytes = bkey;
		

		byte[] cipherBytes = new byte[plainBytes.length];
		for (int i = 0; i < plainBytes.length; i++) {

		    cipherBytes[i] = (byte) ((plainBytes[i] ^ keyBytes[i]) & 0xFF); //  % keyBytes.length]);
		}
		String cipherText = new String(cipherBytes);
		System.out.println(cipherText);

		
		//To decrypt just reverse the process.
		
		//byte[] cipherBytes2 = cipherText.getBytes(charSet);
		//String plainText2 = new String(cipherBytes2, charSet);
		//System.out.println(plainText2);

		// decode
		for (int i = 0; i < cipherBytes.length; i++) {

		    plainBytes[i] = (byte) (cipherBytes[i] ^ keyBytes[i]); // % keyBytes.length]);
		}
		String plainText3 = new String(plainBytes, charSet);
		System.out.println(plainText3);
//		*/
	}

}
