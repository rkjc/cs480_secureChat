package secureChat;
import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.Cipher;

public class CommandSplit {

	public static String e;
	public static String n;
	public static byte[] k1 = null;
	public static byte[] k2 = null;
	static Random rand = new Random();
	static HashMap<String, String> newmap = new HashMap();

	public static byte[] Ks = null;
	
	
	public static void main(String[] args) throws Exception {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		LoadUsers();
		KGen();
		System.out.println("Ks Length: "+ Ks.length);
		// input

		System.out.print("Enter String: ");
		String s = br.readLine();

		// convert to string then lowercase and trim the string
		s = s.toLowerCase().trim();
		// GetKeys();
		// call comsplit
		String msg = ComSplit(s);
		System.out.println(msg);
	}

	// public static void GetKeys() throws Exception {
	// try {
	// BufferedReader keys = new BufferedReader(new FileReader(
	// "pub_key.txt"));
	//
	// e = keys.readLine().replaceAll("\\s+", "").substring(2);
	// n = keys.readLine().replaceAll("\\s+", "").substring(2);
	// System.out.println(e);
	// System.out.println(n);
	//
	// } catch (FileNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }
	static void KGen() {
		k1 = BigInteger.probablePrime(128, rand).toByteArray();
		k2 = BigInteger.probablePrime(128, rand).toByteArray();
		Ks = BigInteger.probablePrime(127, rand).toByteArray();

	}
	
	
	public static void LoadUsers() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("userlist.txt"));
		String line;

		while ((line = br.readLine()) != null) {
			String[] columns = line.split(" ");
			newmap.put(columns[0], columns[1]);
		}

		// iterate through entire map
		// for (Entry<String, String> entry : newmap.entrySet()) {
		// String key = entry.getKey();
		// Object value = entry.getValue();
		// System.out.println("key "+entry.getKey()+" value "+entry.getValue());
		// }
	}

	static String ComSplit(String s) { // comsplit input then call method and send
										// to server
		// login and send conditions needs a space after command because of
		// extra input

		String message = null;

		if (s.startsWith("login ")) {
			System.out.println("login");
			message = s.substring(6);
			System.out.println(message);

			// send to the server (cmd, user, pass, Ks)
			String[] columns = message.split(" ");
			String username = columns[0];
			String password = columns[1];
			
			if (newmap.containsKey(username) == true) {
				if (newmap.containsValue(password)== true) {
					String key1 = new String(k1);
					String key2 = new String(k2);

					message ="k1 " +k1 +" "+k2;

				} else {
					message = "Password invalid.";
				}
			} else {
				message = "User invalid.";
			}
		}

		else if (s.startsWith("send ")) {
			System.out.println("send");
			message = s.substring(5);
			System.out.println(message);

			// encrypt by calling MsgEncryption if from sender
			// byte[] cipherText = MsgEncryption(message);
			// byte[] cipherText = MsgDecryption(message); if from receiver

			// send to server (cmd, user, msg)
			// code here
		}

		// sends command
		else if (s.equals("who")) {
			System.out.println("who");
			// sends to server for who (cmd)
			// code here
		}

		else if (s.equals("logout")) {
			System.out.println("logout");
			// sends command to server to logout client (cmd)
			// code here
		}
		return message;
	}

	static byte[] MsgEncryption(String s) { // double check with richard

		byte[] cipherText = s.getBytes();

		// byte[] cipherText = null;
		// try {
		// // get an RSA cipher object and print the provider
		// final Cipher cipher = Cipher.getInstance("RSA");
		// // encrypt the plain text using the public key
		// cipher.init(Cipher.ENCRYPT_MODE, e);
		// cipherText = cipher.doFinal(s.getBytes());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		return cipherText;

	}

}
