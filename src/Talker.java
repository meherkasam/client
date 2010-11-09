import java.io.*;
import java.net.*;

/**
 * 
 * @author meher
 *
 * This class acts as the interface between the user and
 * the class that actually executes the commands. Threading
 * can be easily implemented here in order to support abort
 * operations.
 * 
 */

public class Talker {
	static ObjectInputStream is = null;
	static ObjectOutputStream os = null;
	static BufferedReader configFile = null;
	static BufferedReader consoleInput = null;
	static String clientHostName = null;
	static String serverHost = null;
	static int serverPort = 0;
	static int livenessPort = 0;
	static String command = null;
	public Talker() {
		DataObject input = null, output = null;
		try {
			configFile = new BufferedReader(new FileReader ("./config.ini"));
			if(!ReadConfig()) {
				System.out.println("Error in configuration file. Exiting");
				System.exit(1);
			}
			
			InetAddress serverAddress = InetAddress.getByName(serverHost);
			Socket serverSocket = new Socket(serverAddress, serverPort);
			os = new ObjectOutputStream(serverSocket.getOutputStream());
			is = new ObjectInputStream(serverSocket.getInputStream());
			
			consoleInput = new BufferedReader(new InputStreamReader(System.in));
			while((command = consoleInput.readLine()) != null ) {
				String[] params = command.split(" ");
				if (params[0].compareToIgnoreCase("HELLO") == 0) {
					input = new DataObject(0);
		    		output = new DataObject(0);
					input = CommandExecuter.Hello(output);
					System.out.println("Hello from server\n");
				}
				else if (params[0].compareToIgnoreCase("BYE") == 0 || params[0].compareToIgnoreCase("TERMINATE") == 0) {
					input = new DataObject(0);
		    		output = new DataObject(0);
		    		input = CommandExecuter.Bye(output);
		    		System.out.println("Bye from server\n");
		    		System.exit(0);
				}
				else if(params.length > 1) {
					if (params[1].compareToIgnoreCase("LIST") == 0) {
						input = new DataObject(0);
			    		output = new DataObject(0);
			    		input = CommandExecuter.List(output, Integer.parseInt(params[2]), Integer.parseInt(params[3]),  Integer.parseInt(params[4]));
			    		System.out.println("Directory listing successful\n");
					}
					else if (params[1].compareToIgnoreCase("GET") == 0) {
						input = new DataObject(0);
			    		output = new DataObject(0);
			    		String fileName = params[2];
			    		input = CommandExecuter.Get(output, fileName, Integer.parseInt(params[3]), 0);
			    		if(input.success){
			    			String message = input.message;
			    			params = message.split(" ");
			    			input = new DataObject(0);
			    			output = new DataObject(0);
			    			input = CommandExecuter.Pull(output, params[5], fileName, 0);
			    			if (input.success)
			    				System.out.println("Get file " + fileName + " successful\n");
			    			else
			    				System.out.println("Error getting " + fileName + "\n");
			    		}
			    		else {
			    			System.out.println("Error getting " + fileName + "\n");
			    		}
					}
					else if (params[1].compareToIgnoreCase("AGET") == 0) {
						input = new DataObject(0);
			    		output = new DataObject(0);
			    		String fileName = params[2];
			    		int limit = Integer.parseInt(params[4]);
			    		input = CommandExecuter.Get(output, fileName, Integer.parseInt(params[3]), limit);
			    		if(input.success){
			    			String message = input.message;
			    			params = message.split(" ");
			    			input = new DataObject(0);
			    			output = new DataObject(0);
			    			input = CommandExecuter.Pull(output, params[5], fileName, limit);
			    			if(input.success)
			    				System.out.println("Aget file " + fileName + " successful\n");
			    			else
			    				System.out.println("Error getting " + fileName + "\n");
			    		}
			    		else {
			    			System.out.println("Error getting " + fileName + "\n");
			    		}
					}
					else if (params[1].compareToIgnoreCase("PUT") == 0) {
						input = new DataObject(0);
			    		output = new DataObject(0);
			    		String fileName = params[2];
			    		boolean fileFound = false;
			    		File folder = new File(CommandExecuter.clientRoot);
			    		File[] listOfFiles = folder.listFiles();
			    		for(int i = 0; (i < listOfFiles.length); i++) {
			    			if(listOfFiles[i].getName().compareTo(fileName) == 0)
			    			{
			    				fileFound = true;
			    				break;
			    			}
			    		}
			    		if(!fileFound) {
			    			System.out.println("File does not exist locally\n");
			    			continue;
			    		}
			    		input = CommandExecuter.Put(output, fileName, Integer.parseInt(params[3]), 0);
			    		if(input.success) {
			    			String message = input.message;
			    			params = message.split(" ");
			    			input = new DataObject(0);
			    			output = new DataObject(CommandExecuter.chunkSize);
			    			input = CommandExecuter.Push(output, params[5], fileName, 0);
			    			if (input.success)
			    				System.out.println("Put file " + fileName + " successful\n");
			    			else
			    				System.out.println("Error putting " + fileName + "\n");
			    		}
			    		else {
			    			System.out.println("Error putting " + fileName + "\n");
			    		}
					}
					else if (params[1].compareToIgnoreCase("APUT") == 0) {
						input = new DataObject(0);
			    		output = new DataObject(0);
			    		String fileName = params[2];
			    		int limit = Integer.parseInt(params[4]);
			    		input = CommandExecuter.Put(output, fileName, Integer.parseInt(params[3]), limit);
			    		if(input.success) {
			    			String message = input.message;
			    			params = message.split(" ");
			    			input = new DataObject(0);
			    			output = new DataObject(CommandExecuter.chunkSize);
			    			input = CommandExecuter.Push(output, params[5], fileName, limit);
			    			if(input.success)
			    				System.out.println("Aput file " + fileName + " successful\n");
			    			else
			    				System.out.println("Error putting " + fileName + "\n");
			    		}
			    		else {
			    			System.out.println("Error putting " + fileName + "\n");
			    		}
					}
					else if (params[1].compareToIgnoreCase("DELETE") == 0) {
						input = new DataObject(0);
			    		output = new DataObject(0);
			    		String fileName = params[2];
			    		input = CommandExecuter.Delete(output, fileName, Integer.parseInt(params[3]));
			    		if(input.success) {
			    			System.out.println("Delete file " + fileName + " successful\n");
			    		}
			    		else {
			    			System.out.println("Error deleting " + fileName + "\n");
			    		}
					}
					else {
						System.out.println("Incorrect command. Please retry\n");
					}
				}
				else {
					System.out.println("Incorrect command. Please retry\n");
				}
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	boolean ReadConfig() {
		try {
			String configLine = configFile.readLine();
			String[] params = configLine.split(" ");
			if(params[0].compareToIgnoreCase("server-hostname:") == 0) {
				serverHost = params[1];
			}
			else
				return false;
			
			configLine = configFile.readLine();
			params = configLine.split(" ");
			if(params[0].compareToIgnoreCase("server-port:") == 0) {
				serverPort = Integer.parseInt(params[1]);
			}
			else
				return false;
			
			configLine = configFile.readLine();
			params = configLine.split(" ");
			if(params[0].compareToIgnoreCase("chunk-size:") == 0) {
				CommandExecuter.chunkSize = Integer.parseInt(params[1]);
			}
			
			configLine = configFile.readLine();
			params = configLine.split(" ");
			if(params[0].compareToIgnoreCase("liveness-port:") == 0) {
				livenessPort = Integer.parseInt(params[1]);
			}
			else
				return false;
			
			/*configLine = configFile.readLine();
			params = configLine.split(" ");
			if(params[0].compareToIgnoreCase("client-hostname:") == 0) {
				clientHostName = params[1];
			}
			else
				return false;*/
		}
		catch (IOException e) {
			return false;
		}
		return true;
	}
}
