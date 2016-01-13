package tsdb.web.api;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.eclipse.jetty.server.handler.AbstractHandler;

import tsdb.remote.RemoteTsDB;

public abstract class MethodHandler extends AbstractHandler {
	
	protected final RemoteTsDB tsdb;	
	protected final String handlerMethodName;
	
	public MethodHandler(RemoteTsDB tsdb, String handlerMethodName) {
		this.tsdb = tsdb;
		this.handlerMethodName = handlerMethodName;
	}
	
	public String getHandlerMethodName() {
		return handlerMethodName;
	}
	
	protected static void writeStringArray(PrintWriter writer, ArrayList<String> list) {
		writeStringArray(writer, list.toArray(new String[0]));
	}
	
	protected static void writeStringArray(PrintWriter writer, String[] array) {
		if(array==null) {
			return;
		}
		boolean notFirst = false;
		for(String s:array) {
			if(notFirst) {
				writer.print('\n');
			}
			writer.print(s);
			notFirst = true;
		}
	}

}
