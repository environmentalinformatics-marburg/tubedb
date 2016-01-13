package tsdb.web;

import java.net.BindException;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.remote.RemoteTsDB;
import tsdb.remote.ServerTsDB;
import tsdb.remote.StartServerTsDB;
import tsdb.util.TsdbThrow;

/**
 * Start RMI- and Web-Server
 * @author woellauer
 *
 */
public class Run {
	public static void main(String[] args) throws RemoteException, InterruptedException {

		TsDB tsdb = TsDBFactory.createDefault();
		RemoteTsDB remoteTsdb = new ServerTsDB(tsdb);


		Runnable runnerRMI = ()->{
			try {
				StartServerTsDB.run(remoteTsdb);
			} catch (ExportException e) {
				System.out.println("ERROR could not bind RMI: "+e);
			} catch (Exception e) {
				//e.printStackTrace();
				TsdbThrow.printStackTrace(e);
				System.out.println(e.getClass());				
			}
		};

		Thread threadRMI = new Thread(runnerRMI);
		threadRMI.start();

		Runnable runnerWEB = ()->{
			try {
				Main.run(remoteTsdb);
			} catch (BindException e) {
				System.out.println("ERROR could not bind socket: "+e);
			} catch (Exception e) {
				//e.printStackTrace();
				TsdbThrow.printStackTrace(e);
			}
		};

		Thread threadWEB = new Thread(runnerWEB);
		threadWEB.start();
		
		//Runtime.getRuntime().addShutdownHook(new Thread(()->tsdb.close()));

		threadRMI.join();
		threadWEB.join();

		tsdb.close();
		
		System.exit(-1);
	}
}
