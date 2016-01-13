package tsdb.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import tsdb.TsDBFactory;
import tsdb.util.Pair;
import tsdb.util.Util;

/**
 * Entry Point for console 
 * @author woellauer
 *
 */
public class ConsoleStarter {

	static boolean running = true;

	final static String server_url = "rmi://192.168.191.183:16826/ServerTsDB";

	public static void main(String[] args) {
		try {
			System.out.println("start RemoteTsDB...");
			Registry registry;

			registry = LocateRegistry.getRegistry("192.168.191.183",RemoteTsDBFactory.RMI_REGISTRY_PORT);

			System.out.println("list: "+Util.arrayToString(registry.list()));

			String hostname = null;
			try {
				hostname = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			System.out.println("this host IP is " + hostname);

			//RemoteTsDB remoteTsDB = (RemoteTsDB) registry.lookup(server_url);
			
			RemoteTsDB remoteTsDB = new ServerTsDB(TsDBFactory.createDefault());
			
			System.out.println("remoteTsDB: "+remoteTsDB.toString()+"  "+remoteTsDB.getClass());

			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


			String line = "intro";

			while(running&&line!=null) {

				long commandThreadId = remoteTsDB.execute_console_command(line);
				boolean command_running = true;
				while(command_running){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Pair<Boolean, String[]> pair = remoteTsDB.console_comand_get_output(commandThreadId);
					if(pair==null) {
						command_running = false;
					} else {
						command_running = pair.a;
						String[] output_lines = pair.b;
						for(String output_line:output_lines) {
							System.out.println(output_line);
						}
					}
				}
				if(running) {
					line = readLine(reader);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	public static String readLine(BufferedReader reader) {
		System.out.println();
		System.out.print("tsdb:$ ");
		try {
			return reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
