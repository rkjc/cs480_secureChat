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

	public void KGen() {
		Ks = BigInteger.probablePrime(128, rand).toByteArray();
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
				
				if (k1 == null) {// not logged in
					int msgSize = b.length;
					byte size = (byte)msgSize;
					int numBlock = (int) Math.ceil((double)msgSize / 16.0f);
					byte[] b16 = new byte[numBlock * 16];
					System.arraycopy( b, 0, b16, 0, b.length );
		
					byte[] c = new byte[1 + numBlock*16 + 16];

					System.out.println("c length " + c.length);
					System.out.println("b length " + b.length);
					System.out.println("b16 length " + b16.length);
					System.out.println("Ks length " + Ks.length);
					System.out.println("Ks:  " + Ks);

					
					c[0] =  size;
					System.arraycopy(b16, 0, c, 1, b.length);
					System.arraycopy(Ks, 0, c, c.length-16, 16);
					
					// encrypt c using server public key
					
					try {
						c = EncryptKs(c);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					b = c;
					System.out.println("test522");
				}

				else{
					// logged in
					  										
					
					try {
						byte[] MAC = new byte[16];
						MessageDigest m = MessageDigest.getInstance("MD5");
						m.update(b);
						byte[] dig = m.digest();
						
						MAC = EncryptK2(dig);
						//MAC = ("1234567812345678").getBytes();  // ****** bogus MAC
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
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}				
				}

				try {
					sendBytes(b);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// out.println(textField.getText());
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

	public byte[] EncryptKs(byte[] b) throws Exception { // if user is NOT logged in
		
		BigInteger ciphertxt = new BigInteger(b);
		ciphertxt=ciphertxt.modPow(e,n);
		b=ciphertxt.toByteArray();				
		return b;
	}
	
	public byte[] DecryptKs(byte[] b) throws Exception { // if user is NOT logged in
		
		BigInteger ciphertxt = new BigInteger(b);
		ciphertxt=ciphertxt.modPow(d,n);
		b=ciphertxt.toByteArray();				
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
	 */
	private void run() throws IOException {

		KGen(); // makes the random key Ks
		// Make connection and initialize streams
		String serverAddress = getServerAddress();
		Socket socket = new Socket(serverAddress, 9001);

		InputStream inStream = socket.getInputStream();
		dis = new DataInputStream(inStream);

		OutputStream outStream = socket.getOutputStream();
		dos = new DataOutputStream(outStream);

		// in = new BufferedReader(new InputStreamReader(
		// socket.getInputStream()));
		// out = new PrintWriter(socket.getOutputStream(), true);

		// Process all messages from server, according to the protocol.
		while (true) {
			// String line = in.readLine();
			System.out.println("receiver waiting for dis.readInt()");
			int len = dis.readInt();
			byte[] data = new byte[len];
			if (len > 0) {
				dis.readFully(data);
			}
			
			String txtInput = (new String(data));
			System.out.println(txtInput);

			if (txtInput.startsWith("SUBMITNAME")) {
				// out.println(getName());
			} else if (txtInput.startsWith("NAMEACCEPTED")) {
				textField.setEditable(true);
			} else if (txtInput.startsWith("MESSAGE")) {
				messageArea.append(txtInput.substring(8) + "\n");
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