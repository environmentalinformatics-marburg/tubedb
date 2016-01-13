package tsdb.web;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class TestingWeb {

	public static void main(String[] args) throws Exception {
		// Setup Threadpool for multiple server connections
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMaxThreads(500);

		//ThreadPool Server
		Server server = new Server(threadPool);
		int port = 8080;
		int portSecure = 443;

		// HTTP Configuration
		HttpConfiguration http_config = new HttpConfiguration();
		http_config.setSecureScheme("https");
		http_config.setSecurePort(portSecure);

		// Configure Connector for http
		ServerConnector http = new ServerConnector(server,
				new HttpConnectionFactory(http_config));
		http.setPort(port);
		http.setIdleTimeout(30000);

		// HTTPS Configuration
		HttpConfiguration https_config = new HttpConfiguration(http_config);
		https_config.addCustomizer(new SecureRequestCustomizer());

		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath("keystore.jks");
		sslContextFactory.setKeyStorePassword("password");
		sslContextFactory.setKeyManagerPassword("password");

		ServerConnector sslConnector = new ServerConnector(server,
				new SslConnectionFactory(sslContextFactory, "http/1.1"),
				new HttpConnectionFactory(https_config));
		sslConnector.setPort(portSecure);

		server.setConnectors(new Connector[] { http, sslConnector });
		
		
		RequestLogHandler requestLogHandler = new RequestLogHandler();
		requestLogHandler.setRequestLog((Request request, Response response)->{
			System.out.println("*** request   "+request.getRequestURL()+"  "+request.getQueryString());
		});
		//requestLogHandler.setHandler(gzipHandler);
		server.setHandler(requestLogHandler);

		server.start();
		server.join();

		System.out.println("...Web Sever stopped");	

	}

}
