package secureChat;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*; 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.*;
import java.nio.charset.Charset;

import javax.xml.bind.DatatypeConverter;

public class TestMain {
	
	public static BigInteger ee = null;
	public static BigInteger dd = null;
	public static BigInteger nn = null;

	
	public static void GetKeys() throws Exception {
		BufferedReader keys = new BufferedReader(new FileReader("pub_key.txt"));
		try {
			ee = new BigInteger(keys.readLine().replaceAll("\\s+", "")
					.substring(2));
			nn = new BigInteger(keys.readLine().replaceAll("\\s+", "")
					.substring(2));
			keys.close();
			keys = new BufferedReader(new FileReader("pri_key.txt"));
			dd = new BigInteger(keys.readLine().replaceAll("\\s+", "")
					.substring(2));
			keys.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static byte[] encodePtoC(byte[] b){
		BigInteger P = new BigInteger(1, b);	
//		System.out.println("P= " + P.toString());
		BigInteger C = P.modPow(ee, nn);
//		System.out.println("C= " + C.toString());
		return C.toByteArray();
	}
	
	
	public static byte[] decodeCtoP(byte[] b){		
		BigInteger C = new BigInteger(1, b);
		BigInteger P = C.modPow(dd, nn);
		return P.toByteArray();
	}
	
	public static void main(String[] args) throws Exception {
		
		String txt = "logout fun things)";
		
		if(txt.substring(0, 6).equals("logout")){
			System.out.println("logging out.\n");
		} else {
			System.out.println("nothing.\n");
		}
		
		
		
		
		
		
		
		
		
		
//		GetKeys();
////		System.out.println("n= " + n.toString());
//		System.out.println("ed= " + ee.toString());
//		System.out.println("dd= " + dd.toString());
//		
//		System.out.println("");
//		
//		byte[] w = "login a z".getBytes();
//		BigInteger bi = new BigInteger("121284081130085297547871328195069288767");
//		
//		byte[] x = bi.toByteArray();
//		
//		System.out.println(bi);
//		System.out.println("x= " + new String(x));
//		
//		byte[] y = new byte[w.length + x.length];
//		System.arraycopy(w, 0, y, 0, w.length);
//		System.arraycopy(x, 0, y, w.length, x.length);
//		
//		System.out.println("y= " + new String(y));
//		
//		System.out.println("");
//		
//		byte[] z = encodePtoC(y);
//		System.out.println("z= " + new String(z));
//		
//		System.out.println("");
//		
//		z = decodeCtoP(z);
//		System.out.println("z= " + new String(z));
//		
////		//O»mˆ£È$×ë(?Sì4å".getBytes();
////		System.out.println("a.length= " + a.length);
////		System.out.println("a= " + new String(a));
////		
//		System.out.println("");
//		
//		byte[] b = encodePtoC(a);
//		System.out.println("b.length= " + b.length);	
//		System.out.println("b= " + new String(b));
//		
//		System.out.println("");
//		
//		byte[] c = decodeCtoP(b);
//		System.out.println("c= " + new String(c));
//		
//		System.out.println("+++++++++++++++++++++++++++++");
		
//		//byte[] d = "123456789 123456789 123456789 123456789 123456789 123456789 ".getBytes();
//		byte[] d = "a long and winding sentence that goes nowhere but uses up a lot of space along the way. Because if 56 things the end of & rainbows can be encoded then the process is probably gong to be able to work!".getBytes();
//		System.out.println("d.length= " + d.length);
//		System.out.println("d= " + new String(d));
//		
//		byte[] e = encodePtoC(d);
//		byte[] f = decodeCtoP(e);
//		System.out.println("f= " + new String(f));
		
		//a long and winding sentence that goes nowhere but uses up a lot of space along the way. Because if 56 things @
		// the end of & rainbows can be encoded then the process is probably gong to be able to work!
//		###################################################################
													
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
		
//		System.out.println("is this going to work?");
//		
//		//simulated input string in client
//		String testString = "Is this going to be able to be turned into a binint, then to a byte, then back to a bigint then to a string again?";
//		
//		System.out.println(testString);
//		System.out.println(testString.length());
//		int len = testString.length();
//		
//		//convert string to be sent to byte form
//		byte[] byteMessage = new byte[128];
//		System.out.println(byteMessage.length);
//		byteMessage = testString.getBytes();	
//		System.out.println(byteMessage);
//		System.out.println(byteMessage.length);
//		int messLen = byteMessage.length;
//		//encrypt-decrypt using byte[]
//		
//		byte[] bkey = DatatypeConverter.parseHexBinary("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
//		byte[] cypherByteMessage = new byte[byteMessage.length];
//		for(int i = 0; i < messLen -1; i++)
//		{			
//			cypherByteMessage[i] = (byte) ((byteMessage[i] ^ bkey[i]) & 0xFF);
//			//System.out.println(i + "  " + byteMessage[i] + "  " + bkey[i] +  "  " +  cypherByteMessage[i] );
//		}
//		cypherByteMessage[messLen -1] = (byte) (byteMessage[messLen -1] ^  0xFF);
//		System.out.println("");
//		
//		//convert byte to BigInteger for encryption
//		BigInteger bintMessage = new BigInteger(byteMessage);
//		System.out.println(bintMessage);
//		
//		//simulated encryption key
//		
//		// using bikey causes decryption problems, probably because of sign bit in BigInteger
//		System.out.println(bkey.length);
//		BigInteger key = new BigInteger("123456789");
//		BigInteger bikey = new BigInteger(bkey);
//		//encrypted
//		BigInteger cypherIntOut = bintMessage.xor(key);
//		
//		//convert encrypted message to string to transmit
//		byte[] cypherByteOut = cypherIntOut.toByteArray();
//		System.out.println(cypherByteOut.length);
//		String cypherTxt = new String(cypherByteOut);
//		
//		//byte to string ver
//		String cypherTxt2 = new String(cypherByteMessage);
//		
//		// this is then sent through chat channel
//		
//		//recived text message is then converted to byte form
//		byte[] cypherByteIn = cypherTxt.getBytes();
//		//converted to BigInteger
//		BigInteger cypherIntIn = new BigInteger(cypherByteIn);
//		//decrypted
//		BigInteger plainInt = cypherIntIn.xor(key);
//		//converted back to byte form (BigInteger does not convert to string well)
//		byte[] printBytes = plainInt.toByteArray();
//		System.out.println(printBytes.length);
//		//convert to string for printing to chat console
//		String printString = new String(printBytes);
//		System.out.println(printString);
//		
//		//alt pure byte version;
//		byte[] cypherByteIn2 = cypherTxt2.getBytes();
//		byte[] cypherByteMessageIn = new byte[cypherByteIn2.length];;
//		for(int i = 0; i < messLen - 1; i++)
//		{			
//			cypherByteMessageIn[i] = (byte) ((cypherByteIn2[i] ^ bkey[i]));
//			//System.out.println(i + "  " + cypherByteIn2[i] + "  " + bkey[i] +  "  " +  cypherByteMessageIn[i] );
//		}
//		cypherByteMessageIn[messLen -1] = (byte) ((cypherByteIn2[messLen -1] ^ 0x00));
//		
//		String printString2 = new String(cypherByteMessageIn);
//		System.out.println(printString2);
//		
//		
//		
//		
//		//##################################
//		
//		
//		String plainText = "hello world, there are sheep";
//		Charset charSet = Charset.forName("UTF-8");
//		byte[] plainBytes = plainText.getBytes(charSet);
//		//String key3 = "secret";
//		//byte[] keyBytes = key3.getBytes(charSet);
//		byte[] keyBytes = bkey;
//		
//
//		byte[] cipherBytes = new byte[plainBytes.length];
//		for (int i = 0; i < plainBytes.length; i++) {
//
//		    cipherBytes[i] = (byte) ((plainBytes[i] ^ keyBytes[i]) & 0xFF); //  % keyBytes.length]);
//		}
//		String cipherText = new String(cipherBytes);
//		System.out.println(cipherText);
//
//		
//		//To decrypt just reverse the process.
//		
//		//byte[] cipherBytes2 = cipherText.getBytes(charSet);
//		//String plainText2 = new String(cipherBytes2, charSet);
//		//System.out.println(plainText2);
//
//		// decode
//		for (int i = 0; i < cipherBytes.length; i++) {
//
//		    plainBytes[i] = (byte) (cipherBytes[i] ^ keyBytes[i]); // % keyBytes.length]);
//		}
//		String plainText3 = new String(plainBytes, charSet);
//		System.out.println(plainText3);
//		*/
	}

}
