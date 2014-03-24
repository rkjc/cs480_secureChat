package secureChat;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;


public class SecureChatServer {

	public static byte[] k1 = null;
	public static byte[] k2 = null;
	static Random rand = new Random();
	@SuppressWarnings("unchecked")
	static HashMap<String, String> newmap = new HashMap();
	public static BigInteger e = null;
	public static BigInteger d = null;
	public static BigInteger n = null;

	public void GetKeys() throws Exception {
		BufferedReader keys = new BufferedReader(new FileReader("pub_key.txt"));
		try {
			e = new BigInteger(keys.readLine().replaceAll("\\s+", "")
					.substring(2));
			n = new BigInteger(keys.readLine().replaceAll("\\s+", "")
					.substring(2));
			keys.close();
			keys = new BufferedReader(new FileReader("pri_key.txt"));
			d = new BigInteger(keys.readLine().replaceAll("\\s+", "")
					.substring(2));
			keys.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void KGen() {
		k1 = BigInteger.probablePrime(127, rand).toByteArray();
		k2 = BigInteger.probablePrime(127, rand).toByteArray();
	}

	//XOR encrypts and decrypts
	public static byte[] EncryptKs(byte[] b, byte[] Ks) {
		byte temp[] = new byte[16];
		for(int i = 0; i<16; i++){
			temp[i] = (byte) (b[i] ^ Ks[i]);
		}
		//return temp;
		return b;
	}
	
	//XOR encrypts and decrypts
	public static byte[] EncryptK1(byte[] b) {
		byte temp[] = new byte[b.length];
		for(int i = 0; i<b.length; i++){
			temp[i] = (byte) (b[i] ^ k1[i%16]);
		}
		return temp;
		//return b;
	}
	
	//XOR encrypts and decrypts
	public static byte[] EncryptK2(byte[] b) { 
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
	
	public static byte[] MD5(byte[] b) throws NoSuchAlgorithmException{
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(b);
		return m.digest();
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
	
	/**
	 * The port that the server listens on.
	 */
	private static final int PORT = 9001;

	/**
	 * The set of all names of clients in the chat room. Maintained so that we
	 * can check that new clients are not registering name already in use.
	 */
	private static HashSet<String> names = new HashSet<String>();

	/**
	 * The set of all the DataOutputStreams for all the clients. This set is kept so
	 * we can easily broadcast messages.
	 */
	private static HashMap<DataOutputStream, String> clientConnections = new HashMap<DataOutputStream, String>();

	/**
	 * The application main method, which just listens on a port and spawns
	 * handler threads.
	 */
	public static void main(String[] args) throws Exception {
		LoadUsers();

		System.out.println("Secure chat room server.");
		ServerSocket listener = new ServerSocket(PORT);
		KGen();
		try {
			while (true) {
				new Handler(listener.accept()).start();
			}
		} finally {
			listener.close();
		}
	}

	public static void LoadUsers() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("userlist.txt"));
		String line;

		while ((line = br.readLine()) != null) {
			String[] columns = line.split(" ");
			newmap.put(columns[0], columns[1]);
		}
	}
	


	/**
	 * A handler thread class. Handlers are spawned from the listening loop and
	 * are responsible for a dealing with a single client and broadcasting its
	 * messages.
	 */
	private static class Handler extends Thread {
		private String name;
		private Socket socket;
		private DataOutputStream dos;
		private DataInputStream dis;

		public void sendBytes(byte[] myByteArray, DataOutputStream ldos)
				throws IOException {
			sendBytes(myByteArray, 0, myByteArray.length, ldos);
		}

		public void sendBytes(byte[] myByteArray, int start, int len,
				DataOutputStream ldos) throws IOException {
			ldos.writeInt(len);
			if (len > 0) {
				ldos.write(myByteArray, start, len);
			}
		}
		
		public void sendByteData(String msg, DataOutputStream dos) throws NoSuchAlgorithmException, IOException{
		   	byte[] b=msg.getBytes();
			byte[] MAC = new byte[16];										
			int msgSize = b.length;
			byte size = (byte)msgSize;
			int numBlock = (int) Math.ceil((double)msgSize / 16.0f);
			byte[]b16 = new byte[numBlock * 16];
			System.arraycopy( b, 0, b16, 0, b.length );
			byte[] c = new byte[1 + numBlock*16 + 16];
			
			// encrypt for broadcast
			byte[] b16MD5server = MD5(b16);		//make a digest first							
			MAC = EncryptK2(b16MD5server); 	// build a mac
			b16 = EncryptK1(b16); 		// then encrypt
								
			c[0] =  size;
			System.arraycopy(b16, 0, c, 1, b16.length);
			System.arraycopy(MAC, 0, c, c.length-16, 16);
			
			//DataOutputStream cPipe = dos.getKey();
		    sendBytes(c, dos);
		}

		/**
		 * Constructs a handler thread, squirreling away the socket. All the
		 * interesting work is done in the run method.
		 */
		public Handler(Socket socket) {
			this.socket = socket;
		}

		/**
		 * the run function that does the work in
		 * the client thread
		 */
		public void run() {
			boolean valid = false;
			try {
				// Create data streams for the socket.
				InputStream inStream = socket.getInputStream();
				dis = new DataInputStream(inStream);
				OutputStream outStream = socket.getOutputStream();
				dos = new DataOutputStream(outStream);

				// Wait for a valid login name and password from this client. 					
				while (! valid) //login loop
				{
					//System.out.println("server receiver waiting for login dis.readInt()");
					int len = dis.readInt();
					byte[] data = new byte[len];
					if (len > 0) {
						//System.out.println("server reading data");
						dis.readFully(data);
					} else { return; }			

					//Separate input data into components
					int lenOfmsgOut = 0;
					byte blomOut;
					byte[] b16in = new byte[data.length - 17];
					byte[] Ks = new byte[16];

					blomOut = data[0];
					
					byte[] d = new byte[data.length - 1];
					System.arraycopy(data, 1, d, 0, data.length - 1);
					// use server private key to decrypt 'd'
					d = usePR(d);
					
					System.arraycopy(d, 0, b16in, 0, b16in.length);
					System.arraycopy(d, b16in.length, Ks, 0, 16);
					int lom = (int) blomOut;				
												
					byte[] retKeys= new byte[48];
					// initialize response byte[] with zeros
					for(int i = 0; i < 48; i++){
						retKeys[i] = (byte)0;
					}
					
					String message = "";
					String s = new String(b16in);
					//checking login keyword in message string
					if (s.startsWith("login ")) {
//						System.out.println("running login test");
						message = s.substring(6);
//						System.out.println("Incoming login message= " + message);

						String[] columns = message.split(" ");
						String username = columns[0];
						String password = columns[1].trim();
						
						if (newmap.containsKey(username) && newmap.get(username).equals(password)) {	
//							System.out.println("login confirmed");
							System.out.println(username + " login.");
							System.out.println("K1=" + (new BigInteger(k1)) + "  K2=" + (new BigInteger(k2)) + " are sent out");
							k1 = EncryptKs(k1, Ks);
							k2 = EncryptKs(k2, Ks);
							Ks = MD5(Ks);
//							System.out.println("Ks MD5 = " + new String(Ks));
							System.arraycopy(k1, 0, retKeys, 0, 16);
							System.arraycopy(k2, 0, retKeys, 16, 16);
							
							//login is valid
							valid = true;
							//add this connection and username to the pool
							clientConnections.put(dos, username);
						}					
					
						//return k1 k2 and digest(Ks) to the client	
						//byte[] retData = message.getBytes();
						
						int msgSize = retKeys.length;
						byte size = (byte)msgSize;
						int numBlock = (int) Math.ceil((double)msgSize / 16.0f);
						byte[] b16 = new byte[32];
						System.arraycopy( retKeys, 0, b16, 0, 32);	
						System.out.println("b16= " + new String(b16));
						
						byte[] c = new byte[49];
						c[0] =  size;
						//System.out.println("sending c size= " + size);
						System.arraycopy(b16, 0, c, 1, b16.length);
						System.arraycopy(Ks, 0, c, c.length-16, 16);
						
						//encrypt c using Ks
						
						//send login response c back to the client
						sendBytes(c, dos);		
					}
				} //end login while loop
				

				// Accept messages from this client and broadcast them.
				// Ignore other clients that cannot be broadcasted to.
				while (valid) {
					int len;
					byte[] data = null;
					byte[] k1 = null;
					byte[] k2 = null;
					int lenOfmsg;
					int lom;
					byte blom;
					byte[] b16;
					byte[] Ks;
					byte[] MAC;
					byte[] b;

					//System.out.println("receiver waiting for dis.readInt()");
					len = dis.readInt();
					data = new byte[len];
					if (len > 0) {
						dis.readFully(data);
					} else
						return;

					lom = 0; //length of message
					b16 = new byte[data.length - 17];
					MAC = new byte[16];

					blom = data[0];
					System.arraycopy(data, 1, b16, 0, b16.length);
					System.arraycopy(data, 1 + b16.length, MAC, 0, 16);
					lom = (int) blom;
					
					String encryptM = new String(b16);
					b16 = EncryptK1(b16); // decrypting the message
					
					b = new byte[lom];
					System.arraycopy(b16, 0, b, 0, lom);
						
					String message = new String(b);
					String[] columns = message.split(" ");
					String command = columns[0];			
			
					// if message is valid broadcast to everyone
					if (command.equals("send")) {
						String cName = clientConnections.get(dos);
						for (Entry<DataOutputStream, String> cData : clientConnections.entrySet()) {						    
						   	String msg = cName.concat(": " + message.substring(5));
						   	
						   	System.out.println(msg);
						   	System.out.println("The received encrypted message is: " + encryptM);
						   	
						    sendByteData(msg, cData.getKey());
						}
					} else if(command.equals("who")){						
						int numClients = clientConnections.size();
						String[] names = new String[numClients];
						String mess = "who\n";
						
						int count = 0;
						for (Entry<DataOutputStream, String> cData : clientConnections.entrySet()) {						
							names[count] = cData.getValue();
							count++;
						}
						
						mess = mess.concat(names[0]);
						for(int i = 1; i<names.length; i++){					
							mess = mess.concat(", " + names[i]);
							System.out.println("adding name " + names[i]);
						}
						
					    sendByteData(mess, dos);
					} else if(command.equals("logout")){
						String cName = clientConnections.get(dos);
						for (Entry<DataOutputStream, String> cData : clientConnections.entrySet()) {						    
						   	message = cName.concat(" left");
						    
						    sendByteData(message, cData.getKey());
						    k1 = null;
						    k2 = null;
						}
						dos.close();
						clientConnections.remove(dos);
						socket.close();
						break;
					} else {
						command = "";
					}
				}
			} catch (IOException e) {
				System.out.println(e);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (dos != null) {
					clientConnections.remove(dos);
				}
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}
}