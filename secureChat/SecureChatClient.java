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

class MsgPack {
	public String msg;
	byte[] data;
	byte[] message;
	byte[] b16;
	public byte[] MAC;
	public int lom; //length of message
	byte[] b16MD5client;
	byte[] b16MD5server;
	DataInputStream dis;
	
	public MsgPack(DataInputStream dis){
		this.dis = dis;
	}
	
	public void receiveData() throws IOException, NoSuchAlgorithmException{
		System.out.println("enter receiveData");
		
		int len = dis.readInt();
		System.out.println("len= " + len);
		data = new byte[len];
		if (len > 0) {
			dis.readFully(data);
		}
		System.out.println("data read");
		b16 = new byte[data.length - 17];
		MAC = new byte[16];
		
		lom = (int) data[0];
		System.arraycopy(data, 1, b16, 0, b16.length);
		System.arraycopy(data, 1 + b16.length, MAC, 0, 16);
		System.out.println("finished recive");
	}
	
	public void receiveLogin(){
		
	}

	public boolean validateMsg() throws NoSuchAlgorithmException{
		b16 = SecureChatClient.xorK1(b16); //decrypt b16 first
		message = new byte[lom];
		System.arraycopy(b16, 0, message, 0, lom);
		//then 
		b16MD5client = SecureChatClient.MD5(b16); //make digest
		b16MD5server = SecureChatClient.xorK2(MAC); //decrypt digest
		return SecureChatClient.equalBytes(b16MD5client, b16MD5server);
	}
	
	public boolean validateLogin(byte[] Ks) throws NoSuchAlgorithmException{
		b16 = SecureChatClient.xorKs(b16, Ks); //decrypt b16 first
		System.out.println("b16= " + new String(b16));
		byte[] KsMD5client = SecureChatClient.MD5(Ks); //make a Ks digest
		System.out.println("KsMD5client= " + new String(KsMD5client));
		//b16MD5server = SecureChatClient.xorKs(MAC, Ks); //decrypt digest
		System.out.println("MAC= " + new String(MAC));
		System.out.println("== " + SecureChatClient.equalBytes(KsMD5client, MAC));
		return SecureChatClient.equalBytes(KsMD5client, MAC);
	}
}

/**
 * A simple Swing-based client for the chat server. Graphically it is a frame
 * with a text field for entering messages and a textarea to see the whole
 * dialog.
 * 
 */
public class SecureChatClient {

	DataOutputStream dos;
	private DataInputStream dis;

	JFrame frame = new JFrame("Secure Chat");
	JTextField textField = new JTextField(40);
	JTextArea messageArea = new JTextArea(8, 40);

	public static byte[] Ks = null;
	public static byte[] k1 = null;
	public static byte[] k2 = null;
	public static BigInteger e = null;
	public static BigInteger d = null;
	public static BigInteger n = null;
	public static Random rand = new Random();
	

	
	public  void setk1(byte[] k1){
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

	//XOR encrypts and decrypts
	public static byte[] xorKs(byte[] b, byte[] Ks) {
		byte temp[] = new byte[16];
		for(int i = 0; i<16; i++){
			temp[i] = (byte) (b[i] ^ Ks[i]);
		}
		//return temp;
		return b;
	}
	
	//XOR encrypts and decrypts
	public static byte[] xorK1(byte[] b) {
		byte temp[] = new byte[b.length];
		for(int i = 0; i<b.length; i++){
			temp[i] = (byte) (b[i] ^ k1[i%16]);
		}
		return temp;
		// return b;
	}
	
	//XOR encrypts and decrypts
	public static byte[] xorK2(byte[] b) { // if user IS logged in
		byte temp[] = new byte[16];
		for(int i = 0; i<16; i++){
			temp[i] = (byte) (b[i] ^ k2[i]);
		}
		return temp;
		//return b;
	}
	
	// applies the public key to a message
	public static byte[] usePU(byte[] b){
		return b;
	}
	
	// applies a private key to a message
	public static byte[] usePR(byte[] b){
		return b;
	}
	
	/**
	 * Prompt for and return the address of the server.
	 * 
	 * @return
	 * @throws Exception 
	 */
	private String getServerAddress() {
		return (String) JOptionPane.showInputDialog(frame,
				"Enter IP Address of the Server:", "Welcome to Secure Chat",
				JOptionPane.QUESTION_MESSAGE, null, null, "localhost");
	}

	
	/**
	 * Constructs the client by laying out the GUI and registering a listener
	 * with the textfield so that pressing Return in the listener sends the
	 * textfield contents to the server.
	 */
	public SecureChatClient() {
		System.out.println("Secure chat room client.\n");
		KGen(); // generates the random key Ks for this client
		// Layout GUI
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
				String send = textField.getText();
				byte[] b = (send).getBytes();
				
				// if k1 has not been set then the client still needs to login
				try {
					if (k1 == null) {// not logged in
						String[] col = textField.getText().split(" ");
						if(! col[0].equals("login")){
							messageArea.append(col[0] + "\nDenied. Please login first.\n");
						} else {
							messageArea.append(new String(b) + "\n");
							System.out.print("The encrypted message (" + col[1] + " " + col[2] + " and the session key " + new BigInteger(Ks) +") is: ");
							int msgSize = b.length;
							byte size = (byte)msgSize;
							int numBlock = (int) Math.ceil((double)msgSize / 16.0f);
							byte[] b16 = new byte[numBlock * 16];
							System.arraycopy( b, 0, b16, 0, b.length );	
							
							byte[] d = new byte[b16.length + 16];
							System.arraycopy(b16, 0, d, 0, b16.length);
							System.arraycopy(Ks, 0, d, b16.length, 16);
							
							d = usePU(d); //encrypts using server public key
							System.out.println(new String(d));
							
							byte[] c = new byte[1 + d.length];				
							c[0] =  size;
							System.arraycopy(d, 0, c, 1, d.length);
																		
							sendBytes(c);
						}
					} else {
					// logged in state (k1 is not null)	
										
						int msgSize = b.length;
						byte size = (byte)msgSize;
						int numBlock = (int) Math.ceil((double)msgSize / 16.0f);
						byte[] b16 = new byte[numBlock * 16];
						System.arraycopy( b, 0, b16, 0, b.length );
						
						byte[] dig = MD5(b16);					
						byte[] MAC = xorK2(dig);
						b16 = xorK1(b16);

						byte[] c = new byte[1 + numBlock*16 + 16];
						
						c[0] =  size;
						System.arraycopy(b16, 0, c, 1, b.length);
						System.arraycopy(MAC, 0, c, c.length-16, 16);
						
						sendBytes(c);	
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
	 * Connects to the server then enters the processing loop.
	 * @throws NoSuchAlgorithmException 
	 */
	private void run() throws IOException, NoSuchAlgorithmException {
		int len;
		byte[] data = null;
		byte[] k1 = null;
		byte[] k2 = null;
		int lom; //length of message
		byte blom;
		byte[] b16;
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
			MsgPack message = new MsgPack(dis);
			System.out.println("calling receiveData()");
			message.receiveData();
			//catch response to login request
//			System.out.println("client receiver waiting for login response dis.readInt()");
//			len = dis.readInt();
//			data = new byte[len];
//			if (len > 0) {
//				dis.readFully(data);
//			}
//			
//			byte[] KsMD5in = new byte[16];
//			b16 = new byte[32];
//
//			blom = data[0];
//			byte d[] = new byte[data.length - 1];
//			System.arraycopy(data, 1, d, 0, data.length - 1);
//			
//			//use Ks to decode the login response
//			
//			System.arraycopy(d, 0, b16, 0, 32);
//			System.arraycopy(d, 32, KsMD5in, 0, 16);
			
//			lom = (int) blom;			
//			k1 = new byte[16];
//			k2 = new byte[16];
//			
////			System.arraycopy(message.b16, 0, k1, 0, 16);
////			System.arraycopy(message.b16, 16, k2, 0, 16);	
//			System.arraycopy(b16, 0, k1, 0, 16);
//			System.arraycopy(b16, 16, k2, 0, 16);	
//
//			// validates the login by matching the local MD5(Ks) with the MD5 generated on the server
//			if(equalBytes(KsMD5in, MD5(Ks))){ 
//				messageArea.append("login confirmed\n");
//				k1 = xorKs(k1, Ks); //decodes k1
//				k2 = xorKs(k2, Ks); //decodes k2
//				System.out.println("K1=" + new BigInteger(k1) + "  and k2=" + new BigInteger(k2));
//				setk1(k1);
//				setk2(k2);
//				loggedin = true;
//			}		
			
			if(message.validateLogin(Ks)){
				k1 = new byte[16];
				k2 = new byte[16];
				System.arraycopy(message.b16, 0, k1, 0, 16);
				System.arraycopy(message.b16, 16, k2, 0, 16);
				System.out.println("K1=" + new BigInteger(k1) + "  and k2=" + new BigInteger(k2));
				setk1(k1);
				setk2(k2);
				loggedin = true;
			}
		}

		// Receive messages from server and check for validity
		while (loggedin) {
			MsgPack message = new MsgPack(dis);
			message.receiveData();
					
			if (message.validateMsg()){
				messageArea.append(new String(message.message) + "\n");
			}
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