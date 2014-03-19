package secureChatVer2;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.util.Scanner;

public class ServerMainVer2 {

	public static void main(String[] args) throws IOException {
		
		try 
		{
			final int PORT = 6677;
			ServerSocket server = new ServerSocket(PORT);
			System.out.println("Waiting for clients...");
		
			while (true)
			{												
				Socket s = server.accept();
				
				System.out.println("Client connected from " + s.getLocalAddress().getHostName());
				
				ServerClientVer2 chat = new ServerClientVer2(s);
				Thread t = new Thread(chat);
				t.start();
			}
		} 
		catch (Exception e) 
		{
			System.out.println("An error occured.");
			e.printStackTrace();
		}
	}

}


class ServerClientVer2 implements Runnable{

	private Socket socket;
	
	
	public ServerClientVer2(Socket s)
	{
		socket = s;
	}
	
	@Override
	public void run()
	{
		try
		{
			Scanner in = new Scanner(socket.getInputStream());
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			
			while (true)
			{		
				if (in.hasNext())
				{
					String input = in.nextLine();
					System.out.println("Client Said: " + input);
					out.println("You Said: " + input);
					out.flush();
				}
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}	
	}

}