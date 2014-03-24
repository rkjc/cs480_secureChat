package secureChat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.swing.*;

/**
 * A simple Swing-based client for the chat server. Graphically it is a frame
 * with a text field for entering messages and a textarea to see the whole
 * dialog.
 * 
 * The client follows the Chat Protocol which is as follows. When the server
 * sends "SUBMITNAME" the client replies with the desired screen name. The
 * server will keep sending "SUBMITNAME" requests as long as the client submits
 * screen names that are already in use. When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start sending the server
 * arbitrary strings to be broadcast to all chatters connected to the server.
 * When the server sends a line beginning with "MESSAGE " then all characters
 * following this string should be displayed in its message area.
 */
public class SecureChatClient {

	// BufferedReader in;
	// PrintWriter out;
	DataOutputStream dos;
	private DataInputStream dis;

	JFrame frame = new JFrame("Chatter");
	JTextField textField = new JTextField(40);
	JTextArea messageArea = new JTextArea(8, 40);

	public static byte[] Ks = null;
	public static byte[] k1 = null;
	public static byte[] k2 = null;
	public static BigInteger e = null;
	public static BigInteger d = null;
	public static BigInteger n = null;
	public static Random rand = new Random();
	
	public void setk1(byte[] k1){
		this.k1 = k1;
	}
	
	public byte[] getk1(){
		return k1;
	}
	
	public void setk2(byte[] k2){
		this.k2 = k2;
	}
	
	public byte[] getk2(){
		return k2;
	}
	
	public byte[] getKs(){
		return Ks;
	}

	public void KGen() {
		Ks = BigInteger.probablePrime(127, rand).toByteArray();
	}
	
	public void GetKeys() throws Exception {
		BufferedReader keys = new BufferedReader(new FileReader("pub_key.txt"));
		try {
			e = new BigInteger(keys.readLine().replaceAll("\\s+", "").substring(2));
			n = new BigInteger(keys.readLine().replaceAll("\\s+", "").substring(2));
			keys.close();
			keys = new BufferedReader(new FileReader("pri_key.txt"));
			d = new BigInteger(keys.readLine().replaceAll("\\s+", "").substring(2));
			keys.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean equalBytes(byte[] a, byte[] b){
		boolean pass = true;
		for(int i = 0; i < a.length; i++) {
			if(a[i] != b[i]){
				pass = false;
				break;
			}			
		}
		return pass;
	}
	
	
	public static byte[] MD5(byte[] b) throws NoSuchAlgorithmException{
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(b);
		return m.digest();
	}

	public void sendBytes(byte[] myByteArray) throws IOException {
		sendBytes(myByteArray, 0, myByteArray.length);
	}

	public void sendBytes(byte[] myByteArray, int start, int len)
			throws IOException {
		dos.writeInt(len);
		if (len > 0) {
			dos.write(myByteArray, start, len);
		}
	}

	
	/**
	 * Constructs the client by laying out the GUI and registering a listener
	 * with the textfield so that pressing Return in the listener sends the
	 * textfield contents to the server. Note however that the textfield is
	 * initially NOT editable, and only becomes editable AFTER the client
	 * receives the NAMEACCEPTED message from the server.
	 */
	public SecureChatClient() {
		
		KGen(); // makes the random key Ks
		// Layout GUI
		// textField.setEditable(false);
		textField.setEditable(true);
		messageArea.setEditable(false);
		frame.getContentPane().add(textField, "North");
		frame.getContentPane().add(new JScrollPane(messageArea), "Center");
		frame.pack();
		
		// Add Listeners
		textField.addActionListener(new ActionListener() {
			/**
			 * Responds to pressing the enter key in the textfield by sending
			 * the contents of the text field to the server. Then clear the text
			 * area in preparation for the next message.
			 */
			
			public void actionPerformed(ActionEvent e) {
				byte[] b = (textField.getText()).getBytes();
				
				// encrypt here
				try {
					if (k1 == null) {// not logged in
//						System.out.println("logging in with " + new String(b));
						messageArea.append(new String(b) + "\n");
						int msgSize = b.length;
						byte size = (byte)msgSize;
						int numBlock = (int) Math.ceil((double)msgSize / 16.0f);
						byte[] b16 = new byte[numBlock * 16];
						System.arraycopy( b, 0, b16, 0, b.length );			
						byte[] c = new byte[1 + numBlock*16 + 16];				
						c[0] =  size;
						System.arraycopy(b16, 0, c, 1, b.length);
						System.arraycopy(Ks, 0, c, c.length-16, 16);
						
						// encrypt c using server public key					
						c = EncryptKs(c);
											
//						System.out.println("sending login data to server");
//						System.out.println("sending Ks= " + new String(Ks));
						sendBytes(c);
					} else {
					// logged in	
//						System.out.println("sending message " + new String(b));
						byte[] MAC = new byte[16];
						MessageDigest m = MessageDigest.getInstance("MD5");
						m.update(b);
						byte[] dig = m.digest();
						
						MAC = EncryptK2(dig);
						b = EncryptK1(b);
										
						int msgSize = b.length;
						byte size = (byte)msgSize;
						int numBlock = (int) Math.ceil((double)msgSize / 16.0f);
						byte[] b16 = new byte[numBlock * 16];
						System.arraycopy( b, 0, b16, 0, b.length );
						//byte[] c = new byte[b.length + Ks.length+ 1];  // Concatenate Ks to the end of b
						byte[] c = new byte[1 + numBlock*16 + 16];
						
						c[0] =  size;
						System.arraycopy(b16, 0, c, 1, b.length);
						System.arraycopy(Ks, 0, c, c.length-16, 16);
						
						b = EncryptKs(c);
//						System.out.println("sending message data to server");
						sendBytes(b);								
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	

				textField.setText("");
			}
		});
	}

	/**
	 * Prompt for and return the address of the server.
	 * 
	 * @return
	 * @throws Exception 
	 */

	// if user is NOT logged in
	public byte[] EncryptKs(byte[] b) throws Exception { 		
//		BigInteger ciphertxt = new BigInteger(b);
//		ciphertxt=ciphertxt.modPow(e,n);
//		b=ciphertxt.toByteArray();				
		return b;
	}
	
	// if user is NOT logged in
	public byte[] DecryptKs(byte[] b) throws Exception { 		
//		BigInteger ciphertxt = new BigInteger(b);
//		ciphertxt=ciphertxt.modPow(d,n);
//		b=ciphertxt.toByteArray();				
		return b;
	}
	

	public byte[] EncryptK1(byte[] b) { // if user IS logged in
		return b;
	}
	
	public byte[] EncryptK2(byte[] b) { // if user IS logged in
		return b;
	}

	private String getServerAddress() {
		return (String) JOptionPane.showInputDialog(frame,
				"Enter IP Address of the Server:", "Welcome to the Chatter",
				JOptionPane.QUESTION_MESSAGE, null, null, "localhost");
	}

	/**
	 * Prompt for and return the desired screen name.
	 */
	private String getName() {
		return JOptionPane.showInputDialog(frame, "Choose a screen name:",
				"Screen name selection", JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Connects to the server then enters the processing loop.
	 * @throws NoSuchAlgorithmException 
	 */
	private void run() throws IOException, NoSuchAlgorithmException {
		int len;
		byte[] data = null;
		byte[] k1 = null;
		byte[] k2 = null;
		int lenOfmsg;
		int lom;
		byte blom;
		byte[] b16;
		//byte[] Ks;
		byte[] MAC;
		
		// Make connection and initialize streams
		String serverAddress = getServerAddress();
		Socket socket = new Socket(serverAddress, 9001);

		InputStream inStream = socket.getInputStream();
		dis = new DataInputStream(inStream);

		OutputStream outStream = socket.getOutputStream();
		dos = new DataOutputStream(outStream);
		
		boolean loggedin = false;
		while(! loggedin){
			//catch response to login request
//			System.out.println("client receiver waiting for login response dis.readInt()");
			len = dis.readInt();
			data = new byte[len];
			if (len > 0) {
				dis.readFully(data);
			}
			
			lenOfmsg = 0;
			byte[] KsMD5in = new byte[16];
			b16 = new byte[32];

			blom = data[0];
			System.arraycopy(data, 1, b16, 0, 32);
			System.arraycopy(data, 33, KsMD5in, 0, 16);
			
			lom = (int) blom;			
			k1 = new byte[16];
			k2 = new byte[16];
			
			System.arraycopy(b16, 0, k1, 0, 16);
			System.arraycopy(b16, 16, k2, 0, 16);
			
			if(equalBytes(KsMD5in, MD5(Ks))){  
				setk1(k1);
				setk2(k2);
				loggedin = true;
//				System.out.println("client is logged in");
				messageArea.append("login confirmed\n");
			}		
		}

		// Process all messages from server, according to the protocol.
		while (true) {
			// String line = in.readLine();
//			System.out.println("client receiver waiting for dis.readInt()");
			len = dis.readInt();
			data = new byte[len];
			if (len > 0) {
				dis.readFully(data);
			}
			
			lenOfmsg = 0;
			b16 = new byte[data.length - 17];
			MAC = new byte[16];

			blom = data[0];
			System.arraycopy(data, 1, b16, 0, b16.length);
			System.arraycopy(data, 1 + b16.length, MAC, 0, 16);
			lom = (int) blom;
			
			
			b16 = EncryptK1(b16); //decrypt b16
			byte[] digMess = MD5(b16); //make digest
			byte[] digMAC = EncryptK2(MAC); //decrypt digest
			//compare
			
//			System.out.println("compare " + equalBytes(digMess, digMAC));
//			System.out.println("b16 " + new String(b16));
//			System.out.println("length of message " + lom);
//			System.out.println("MAC " + new String(MAC));
			
			//decrypt using k1 and k2
			//print to UI if MAC is valid
			
			messageArea.append(new String(b16) + "\n");
					
		}
	}

	/**
	 * Runs the client as an application with a closeable frame.
	 */
	public static void main(String[] args) throws Exception {
		SecureChatClient client = new SecureChatClient();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.setVisible(true);
		client.run();
	}
}