package tsdb.remote;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;

/**
 * Start RMI Server
 * @author woellauer
 *
 */
public class StartServerTsDB {

	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) throws RemoteException {

		log.info("open database...");
		TsDB tsdb = TsDBFactory.createDefault();
		ServerTsDB servertsdb = new ServerTsDB(tsdb);
		RemoteTsDB remoteTsDB = servertsdb;

		run(remoteTsDB);		
	}

	public static void run(RemoteTsDB remoteTsDB) throws RemoteException {
		/*System.out.println("start...");
		TsDB tsdb = FactoryTsDB.createDefault();
		try {
			ServerTsDB servertsdb = new ServerTsDB(tsdb);
			RemoteTsDB stubTsDB = (RemoteTsDB) UnicastRemoteObject.exportObject((RemoteTsDB)servertsdb, 0);
			System.out.println("create registry..."+stubTsDB.getClass().getMethods().length);
			Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			System.out.println("bind tsdserver...");
			registry.rebind(SERVERTSDB_NAME, stubTsDB);
			System.out.println("...tsdserver bound");
		} catch (Exception e) {
			e.printStackTrace();
		}		
		System.out.println("...ready");
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {		
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.out.println(e);				
			}
		});*/

		String hostname = null;
		try {
			hostname = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		log.info("server IP is " + hostname);

		//if(hostname.equals("127.0.1.1")) {
		final String localhost_prefix = "127";
		if(hostname.startsWith(localhost_prefix)) {
			String ip = RemoteTsDBFactory.getLocalIP();
			hostname = RemoteTsDBFactory.RMI_DEFAULT_SERVER_IP;
			if(ip!=null) {
				hostname = ip;
			}
			log.info("set server IP to " + hostname);
			System.setProperty("java.rmi.server.hostname", hostname);
		}		

		String server_url = "rmi://"+hostname+":"+RemoteTsDBFactory.RMI_SERVER_PORT+"/"+RemoteTsDBFactory.RMI_SERVER_NAME;
		log.info("server url: "+server_url);

		RemoteServer.setLog(System.out);
		Thread.setDefaultUncaughtExceptionHandler((t,e) -> System.out.println(e));


		log.info("create registry...");
		Registry registry = LocateRegistry.createRegistry(RemoteTsDBFactory.RMI_REGISTRY_PORT);
		log.info("bind remote...");
		RemoteTsDB stub = (RemoteTsDB) UnicastRemoteObject.exportObject( remoteTsDB, 0 ); 
		//registry.rebind(server_url, stub);
		registry.rebind(RemoteTsDBFactory.RMI_SERVER_NAME, stub);
		log.info("ready...");



	}
}
