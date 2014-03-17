package secureChat;
import java.util.*; 
import java.math.*;

import javax.xml.bind.DatatypeConverter;

public class TestMain {

	public static void main(String[] args) {
		System.out.println("is this going to work?");
		
		//simulated input string in client
		String testString = "Is this going to be able to be turned into a binint, then to a byte, then back to a bigint then to a string again?";
		
		System.out.println(testString);
		System.out.println(testString.length());
		int len = testString.length();
		
		//convert string to be sent to byte form
		byte[] byteMessage = testString.getBytes();
		System.out.println(byteMessage);
		System.out.println(byteMessage.length);
		
		//convert byte to BigInteger for encryption
		BigInteger bintMessage = new BigInteger(byteMessage);
		System.out.println(bintMessage);
		
		//simulated encryption key
		byte[] bkey = DatatypeConverter.parseHexBinary("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		BigInteger bikey = new BigInteger(bkey);
		// using bikey causes decryption problems, probably because of sign bit in BigInteger
		System.out.println(bkey.length);
		BigInteger key = new BigInteger("123456789");
	
		//encrypted
		BigInteger cypherIntOut = bintMessage.xor(key);
		
		//convert encrypted message to string to transmit
		byte[] cypherByteOut = cypherIntOut.toByteArray();
		System.out.println(cypherByteOut.length);
		String cypherTxt = new String(cypherByteOut);
		
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
		
		
		
	}

}
