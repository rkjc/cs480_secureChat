package secureChatVer2;

//public class ServerClientVer2 {

//}


import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class ServerClientVer2 implements Runnable{

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
