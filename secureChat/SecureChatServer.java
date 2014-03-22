package secureChat;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

/**
 * A multithreaded chat room server. When a client connects the server requests
 * a screen name by sending the client the text "SUBMITNAME", and keeps
 * requesting a name until a unique one is received. After a client submits a
 * unique name, the server acknowledges with "NAMEACCEPTED". Then all messages
 * from that client will be broadcast to all other clients that have submitted a
 * unique screen name. The broadcast messages are prefixed with "MESSAGE ".
 * 
 * Because this is just a teaching example to illustrate a simple chat server,
 * there are a few features that have been left out. Two are very useful and
 * belong in production code:
 * 
 * 1. The protocol should be enhanced so that the client can send clean
 * disconnect messages to the server.
 * 
 * 2. The server should do some logging. 
 */
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

	void KGen() {
		k1 = BigInteger.probablePrime(127, rand).toByteArray();
		k2 = BigInteger.probablePrime(127, rand).toByteArray();
	}

	// if user is NOT logged in
	public static byte[] EncryptKs(byte[] b) throws Exception {
		BigInteger ciphertxt = new BigInteger(b);
		ciphertxt = ciphertxt.modPow(e, n);
		b = ciphertxt.toByteArray();
		return b;
	}

	// if user is NOT logged in
	public byte[] DecryptKs(byte[] b) throws Exception { 
		BigInteger ciphertxt = new BigInteger(b);
		ciphertxt = ciphertxt.modPow(d, n);
		b = ciphertxt.toByteArray();
		return b;
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
	private static HashSet<DataOutputStream> byteWriters = new HashSet<DataOutputStream>();

	/**
	 * The appplication main method, which just listens on a port and spawns
	 * handler threads.
	 */

	public static void main(String[] args) throws Exception {
		LoadUsers();

		System.out.println("The chat server is running.");
		ServerSocket listener = new ServerSocket(PORT);
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

		// iterate through entire map
		// for (Entry<String, String> entry : newmap.entrySet()) {
		// String key = entry.getKey();
		// Object value = entry.getValue();
		// System.out.println("key "+entry.getKey()+" value "+entry.getValue());
		// }
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

		/**
		 * Constructs a handler thread, squirreling away the socket. All the
		 * interesting work is done in the run method.
		 */
		public Handler(Socket socket) {
			this.socket = socket;
		}

		/**
		 * Services this thread's client by repeatedly requesting a screen name
		 * until a unique one has been submitted, then acknowledges the name and
		 * registers the output stream for the client in a global set, then
		 * repeatedly gets inputs and broadcasts them.
		 */
		public void run() {
			boolean valid = false;
			try {
				// Create data streams for the socket.
				InputStream inStream = socket.getInputStream();
				dis = new DataInputStream(inStream);

				OutputStream outStream = socket.getOutputStream();
				dos = new DataOutputStream(outStream);

				// Wait for a login name and password from this client. 					
				while (true) //login loop
				{
					System.out.println("server receiver waiting for login dis.readInt()");
					int len = dis.readInt();
					byte[] data = new byte[len];
					if (len > 0) {
						System.out.println("server reading data");
						dis.readFully(data);
					} else
						return;

					// use server private key to decrypt 'data'

					//Separate input data into components
					int lenOfmsgOut = 0;
					byte blomOut;
					byte[] b16in = new byte[data.length - 17];
					byte[] Ks = new byte[16];

					blomOut = data[0];
					System.arraycopy(data, 1, b16in, 0, b16in.length);
					System.arraycopy(data, 1 + b16in.length, Ks, 0, 16);
					int lom = (int) blomOut;

					// login test process goes here

					System.out.println(lom);
					System.out.println(new String(b16in));
					System.out.println(new String(Ks));

					
					//return k1 k2 and Ks to the client
					// generate this string from k1 k2 encoded using Ks
					String k1k2 = "12345678123456781234567812345678"; //bogus data
					
					byte[] retData = k1k2.getBytes();
					
					int msgSize = retData.length;
					byte size = (byte)msgSize;
					int numBlock = (int) Math.ceil((double)msgSize / 16.0f);
					byte[] b16 = new byte[numBlock * 16];
					System.arraycopy( retData, 0, b16, 0, retData.length );	
					byte[] c = new byte[1 + numBlock*16 + 16];
					c[0] =  size;
					System.arraycopy(b16, 0, c, 1, b16.length);
					System.arraycopy(Ks, 0, c, c.length-16, 16);
					
					//send login response c back to the client
					sendBytes(c, dos);
					
					// test if user is on name password list
					if (true) {
						valid = true;
						byteWriters.add(dos);
						break;
					}			
				}
				

				// Accept messages from this client and broadcast them.
				// Ignore other clients that cannot be broadcasted to.
				while (valid) {
					System.out.println("receiver waiting for dis.readInt()");
					int len = dis.readInt();
					byte[] data = new byte[len];
					if (len > 0) {
						dis.readFully(data);
					} else
						return;

					int lenOfmsgOut = 0;
					byte blomOut;
					byte[] b16out = new byte[data.length - 17];
					byte[] MACout = new byte[16];

					blomOut = data[0];
					System.arraycopy(data, 1, b16out, 0, b16out.length);
					System.arraycopy(data, 1 + b16out.length, MACout, 0, 16);
					int lom = (int) blomOut;

					// test for valid message here
					// use k1 on b16out
					// use k2 on MACout
					

					// if message is valid broadcast to everyone
					if (true) {
						for (DataOutputStream bWriter : byteWriters) {
							//TODO make this so that it adds the client 
							//name to the message before broadcasting
							sendBytes(data, bWriter); 
						}
					}

				}
			} catch (IOException e) {
				System.out.println(e);
			} finally {
				if (dos != null) {
					byteWriters.remove(dos);
				}
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}
}