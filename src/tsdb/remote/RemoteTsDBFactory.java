package tsdb.remote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.util.Util;

/**
 * Factory of RemoteTsDB
 * - create local instance 
 * - start server for RMI
 * - connect to RMI 
 * @author woellauer
 *
 */
public class RemoteTsDBFactory {
	private static final Logger log = LogManager.getLogger();
	
	public static final String RMI_SERVER_NAME = "ServerTsDB";
	public static final int RMI_REGISTRY_PORT = 16825;
	public static final int RMI_SERVER_PORT = 16826;
	public static String RMI_DEFAULT_SERVER_IP = "192.168.191.183";
	
	public static ServerTsDB createDefaultServer() {
		TsDB tsdb = TsDBFactory.createDefault();
		ServerTsDB serverTsDB;
		try {
			serverTsDB = new ServerTsDB(tsdb);
			return serverTsDB;
		} catch (RemoteException e) {
			log.error(e);
			return null;
		}
	}
	
	public static String get_rmi_server_url() {
		return get_rmi_server_url(RMI_DEFAULT_SERVER_IP);
	}

	public static String get_rmi_server_url(String server_ip) {
		return "rmi://"+server_ip+':'+RMI_SERVER_PORT+'/'+RMI_SERVER_NAME;
	}

	public static RemoteTsDB createRemoteConnection() {
		return createRemoteConnection(RMI_DEFAULT_SERVER_IP);
	}

	public static RemoteTsDB createRemoteConnection(String server_ip) {
		try {
			System.out.println("get registry from: "+server_ip+":"+RMI_REGISTRY_PORT);
			Registry registry = LocateRegistry.getRegistry(server_ip,RMI_REGISTRY_PORT);
			System.out.println(registry.getClass());
			String serverUrl = null;
			try {
				try{

					log.info("available RMI servers: "+Util.arrayToString(registry.list()));

					String hostname = InetAddress.getLocalHost().getHostAddress();
					log.info("IP of this client: " + hostname);
				} catch(Exception e) {
					log.warn(e);
				}


				for(String entry:registry.list()) {
					//if(entry.endsWith(RMI_SERVER_NAME)) {
					if(entry.equals(RMI_SERVER_NAME)) {
						if(serverUrl != null) {
							log.warn("multiple server entries: "+serverUrl+"   "+entry);
						}
						serverUrl = entry;
					}
				}


			} catch (Exception e) {
				log.warn(e);
			}
			if(serverUrl==null) {
				serverUrl = get_rmi_server_url(server_ip);
			}
			log.info("conntect to "+serverUrl+ " with registry at "+server_ip+":"+RMI_REGISTRY_PORT);
			RemoteTsDB remoteTsDB = (RemoteTsDB) registry.lookup(serverUrl);
			log.info("connected remoteTsDB: "+remoteTsDB.toString());
			return remoteTsDB;
		} catch (Exception e) {
			log.error(e);
			return null;
		}	
	}

	public static String getLocalIP() {
		try {
			Socket socket = new Socket("uni-marburg.de", 80, null, 0);
			String ip = socket.getLocalAddress().getCanonicalHostName();
			socket.close();
			return ip;			
		} catch (IOException e) {
			log.error(e);
			return null;
		}
	}
}
