import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * 
 * @author meher
 *
 * This class executes all commands on behalf of the user.
 * It contains functions to directly to the network stream
 * as well as functions that perform file operations on the
 * server through requests.
 *  
 */

public class CommandExecuter {
	public final static int KB2B = 1024;
	static int chunkSize = 0;
	static String clientRoot = "./"; 
	static PrintStream log = null;
	static int reqNo = 0;
	static String clientId = "";
	public CommandExecuter() {
		try {
			reqNo = 0;
			SecureRandom keyGen = new SecureRandom();
			clientId = new BigInteger(130, keyGen).toString(32);
			clientRoot += "clientdir-" + clientId + "/";
			new File(clientRoot).mkdir();
			log = new PrintStream(clientRoot + "data.log");
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	static DataObject Hello(DataObject a) {
		reqNo++;
		a.reqNo = reqNo;
		a.clientId = clientId;
		a.message = "Req Hello " + String.valueOf(a.reqNo);
		Write(a);
		a = Read(a);
		return a;
	}
	static DataObject Bye(DataObject a) {
		a.message = "Req Bye";
		a.clientId = clientId;
		Write(a);
		a = Read(a);
		log.flush();
		log.close();
		return a;
	}
	static DataObject List(DataObject a, int start, int max, int priority) {
		reqNo++;
		a.reqNo = reqNo;
		a.clientId = clientId;
		a.message = "Req List " + String.valueOf(a.reqNo) + " " + Integer.toString(start) + " " + Integer.toString(max) + " " + Integer.toString(priority);
		Write(a);
		a = Read(a);
	    return a;
	}
	static DataObject Get(DataObject a, String fileName, int priority, int limit) {
		reqNo++;
		a.reqNo = reqNo;
		a.clientId = clientId;
		//if(limit == 0)
			a.message = "Req Get " + String.valueOf(a.reqNo) + " " + fileName + " " + priority;
		//else
			//a.message = "Req Aget " + String.valueOf(a.reqNo) + " " + fileName + " " + priority + " " + limit;
		Write(a);
		a = Read(a);
	    return a;
	}
	static DataObject Put(DataObject a, String fileName, int priority, int limit) {
		reqNo++;
		a.reqNo = reqNo;
		a.clientId = clientId;
		//if(limit == 0)
			a.message = "Req Put " + String.valueOf(a.reqNo) + " " + fileName + " " + priority;
		//else
			//a.message = "Req Aput " + String.valueOf(a.reqNo) + " " + fileName + " " + priority + " " + limit;
		Write(a);
		a = Read(a);
	    return a;
	}
	static DataObject Pull(DataObject a, String fileId, String fileName, int limit) {
		//reqNo++;
		a.reqNo = reqNo;
		boolean notLast = true;
		int offset = 0, currentChunk = 0;
		try {
			FileOutputStream os = new FileOutputStream(clientRoot + fileName);
			while(notLast){
				currentChunk++;
				a.clientId = clientId;
				a.message = "Req Pull " + String.valueOf(a.reqNo) + " " + fileId + " " + (offset) + " " + chunkSize*KB2B;
				Write(a);
				a = Read(a);
				if(a.success)
				{
					os.write(a.data, 0, a.length-1);
					os.flush();
					if(isLast(a) || currentChunk == limit)
						notLast = false;
					else
						offset += a.length;
				}
				else
					break;
			}
			os.close();
		}
		catch(IOException e) {
			
		}
	    return a;
	}
	static DataObject Delete(DataObject a, String fileName, int priority) {
		reqNo++;
		a.reqNo = reqNo;
		a.clientId = clientId;
		a.message = "Req Delete " + String.valueOf(a.reqNo) + " " + fileName + " " + priority;
		Write(a);
		a = Read(a);
	    return a;
	}
	static DataObject Push(DataObject a, String fileId, String fileName, int limit) {
		//reqNo++;
		a.reqNo = reqNo;
		int offset = 0;
		boolean notLast = true;
		int currentChunk = 0;
		try {
			FileInputStream is = new FileInputStream(clientRoot + fileName);
			while(notLast) {
				currentChunk++;
				a.clientId = clientId;
				a.message = "Req Push " + String.valueOf(a.reqNo) + " " + fileId;
				int length = is.read(a.data, 0, chunkSize*KB2B);
				a.length = length;
				if(length < chunkSize * KB2B  || currentChunk == limit) {
					notLast = false;
				}
				if(notLast) {
					a.message += " NOTLAST";
				}
				else {
					a.message += " LAST";
				}
				a.message += " " + (offset) + " " + length;
				if(notLast) {
					offset += a.length;
				}
				Write(a);
				a = Read(a);
			}
			is.close();
		}
		catch(IOException e) {
			
		}
	    return a;
	}
	private static boolean isLast(DataObject a) {
		String[] params = a.message.split(" ");
		if(params[3].compareTo("SUCCESS") == 0) {
			if(params[4].compareTo("NOTLAST") == 0)
				return false;
		}
		return true;
	}
	static void Write(DataObject output) {
		try {
			Talker.os.writeObject(output);
			Talker.os.flush();
			log.println(output.message);
		}
		catch(Exception E) {
			
		}
	}
	static DataObject Read(DataObject input) {
		try {
			input = (DataObject) Talker.is.readObject();
			log.println(input.message);
		}
		catch(Exception E) {
			
		}
		return input;
	}
};