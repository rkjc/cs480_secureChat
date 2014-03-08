package secureChatVer2;



import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientClientVer2 implements Runnable {

	
	private Socket socket;
	
	public ClientClientVer2(Socket s)
	{
		socket = s;
	}
	
	@Override
	public void run()
	{
		try
		{
			Scanner chat = new Scanner(System.in);	
			Scanner in = new Scanner(socket.getInputStream());	
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			
			while (true)
			{						
				String input = chat.nextLine();
				out.println(input);	
				out.flush();
				
				if(in.hasNext())
					System.out.println(in.nextLine());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		} 
	}

}
