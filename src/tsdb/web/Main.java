package tsdb.web;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.PropertyUserStore;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConfiguration.Customizer;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.OptionalSslConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.yaml.snakeyaml.Yaml;

import tsdb.TsDBFactory;
import tsdb.remote.RemoteTsDB;
import tsdb.remote.ServerTsDB;
import tsdb.util.Table;
import tsdb.util.Table.ColumnReaderString;
import tsdb.util.gui.TimeSeriesPainterGraphics2D;
import tsdb.util.yaml.YamlMap;
import tsdb.web.api.IotAPIHandler;
import tsdb.web.api.SupplementHandler;
import tsdb.web.api.TsDBAPIHandler;
import tsdb.web.api.TsDBExportAPIHandler;
/**
 * Start Web-Server
 * @author woellauer
 *
 */
public class Main {
	private static final Logger log = LogManager.getLogger();

	private static final long DATA_TRANSFER_TIMEOUT_MILLISECONDS = 2*60*60*1000; // set timeout to 2 hours

	private static final String WEBCONTENT_PART_URL = "/content";
	private static final String TSDB_API_PART_URL = "/tsdb";
	private static final String EXPORT_API_PART_URL = "/export";
	private static final String DOWNLOAD_PART_URL = "/download";

	private static final String SUPPLEMENT_PART_URL = "/supplement";
	private static final String FILES_PART_URL = "/files";
	private static final String IOT_PART_URL = "/iot";

	private static final String WEB_SERVER_LOGIN_PROPERTIES_FILENAME = "realm.properties";
	private static final String REALM_IP_CSV_FILENAME = "realm_ip.csv";
	private static final String WEB_SERVER_HTTPS_KEY_STORE_FILENAME = "keystore.jks";
	
	private static HttpConfiguration createBaseHttpConfiguration() {
		HttpConfiguration httpConfiguration = new HttpConfiguration();
		httpConfiguration.setSendServerVersion(false);
		httpConfiguration.setSendDateHeader(false);
		httpConfiguration.setSendXPoweredBy(false);
		return httpConfiguration;
	}

	private static HttpConfiguration createJwsHttpConfiguration() {
		HttpConfiguration httpConfiguration = createBaseHttpConfiguration();
		httpConfiguration.addCustomizer(new Customizer() {			
			@Override
			public void customize(Connector connector, HttpConfiguration channelConfig, Request request) {
				request.setAttribute("JWS", "JWS");				
			}
		});
		return httpConfiguration;
	}

	private static HttpConfiguration createBaseHttpsConfiguration(int https_port) {
		HttpConfiguration httpsConfiguration = createBaseHttpConfiguration();
		httpsConfiguration.setSecureScheme("https");
		httpsConfiguration.setSecurePort(https_port);
		httpsConfiguration.setOutputBufferSize(32768);
		SecureRequestCustomizer src = new SecureRequestCustomizer();
		src.setStsMaxAge(2000);
		src.setStsIncludeSubDomains(true);
		httpsConfiguration.addCustomizer(src);
		return httpsConfiguration;
	}

	private static HttpConfiguration createJwsHttpsConfiguration(int https_port) {
		HttpConfiguration httpsConfiguration = createBaseHttpsConfiguration(https_port);
		httpsConfiguration.addCustomizer(new Customizer() {			
			@Override
			public void customize(Connector connector, HttpConfiguration channelConfig, Request request) {
				request.setAttribute("JWS", "JWS");	
			}
		});
		return httpsConfiguration;
	}

	private static ServerConnector createHttpConnector(Server server, int http_port, HttpConfiguration httpConfiguration) {
		HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfiguration);		
		ServerConnector httpServerConnector = new ServerConnector(server, httpConnectionFactory);
		httpServerConnector.setPort(http_port);
		httpServerConnector.setIdleTimeout(DATA_TRANSFER_TIMEOUT_MILLISECONDS);
		httpServerConnector.setAcceptQueueSize(0);
		return httpServerConnector;
	}

	private static ServerConnector createHttpsConnector(Server server, int https_port, String keystore_password, HttpConfiguration https_config) {

		SslContextFactory sslContextFactory = new SslContextFactory.Server();
		sslContextFactory.setKeyStorePath(WEB_SERVER_HTTPS_KEY_STORE_FILENAME);
		sslContextFactory.setKeyStorePassword(keystore_password);

		SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());

		OptionalSslConnectionFactory optionalSslConnectionFactory = new OptionalSslConnectionFactory(sslConnectionFactory, HttpVersion.HTTP_1_1.asString());

		HttpConnectionFactory httpsConnectionFactory = new HttpConnectionFactory(https_config);

		//ServerConnector httpsServerConnector = new ServerConnector(server, optionalSslConnectionFactory, sslConnectionFactory, httpsConnectionFactory);
		ServerConnector httpsServerConnector = new ServerConnector(server, optionalSslConnectionFactory, sslConnectionFactory, httpsConnectionFactory);
		httpsServerConnector.setPort(https_port);
		httpsServerConnector.setIdleTimeout(DATA_TRANSFER_TIMEOUT_MILLISECONDS);
		httpsServerConnector.setAcceptQueueSize(0);
		return httpsServerConnector;
	}	

	public static void main(String[] args) throws Exception {
		RemoteTsDB tsdb = new ServerTsDB(TsDBFactory.createDefault());
		run(tsdb);
	}

	public static void run(RemoteTsDB tsdb) throws Exception {

		boolean use_https = TsDBFactory.WEB_SERVER_HTTPS;

		createRainbowScale();

		Server server = new Server();

		ServerConnector jwsServerConnector = null;
		if(TsDBFactory.WEB_SERVER_JWS_PORT > 0) {
			if(use_https) {
				jwsServerConnector = createHttpsConnector(server, TsDBFactory.WEB_SERVER_JWS_PORT, TsDBFactory.WEB_SERVER_HTTPS_KEY_STORE_PASSWORD, createJwsHttpsConfiguration(TsDBFactory.WEB_SERVER_JWS_PORT));					
			} else {
				jwsServerConnector = createHttpConnector(server, TsDBFactory.WEB_SERVER_JWS_PORT, createJwsHttpConfiguration());				
			}
		}

		if(use_https && TsDBFactory.WEB_SERVER_HTTPS_PORT > 0) {
			ServerConnector httpConnector = createHttpConnector(server, TsDBFactory.WEB_SERVER_PORT, createBaseHttpConfiguration());
			ServerConnector httpsConnector = createHttpsConnector(server, TsDBFactory.WEB_SERVER_HTTPS_PORT, TsDBFactory.WEB_SERVER_HTTPS_KEY_STORE_PASSWORD, createBaseHttpsConfiguration(TsDBFactory.WEB_SERVER_HTTPS_PORT));

			if(jwsServerConnector != null) {
				server.setConnectors(new Connector[] {httpConnector, httpsConnector, jwsServerConnector});	
			} else {
				server.setConnectors(new Connector[] {httpConnector, httpsConnector});
			}
		} else {
			ServerConnector httpConnector = createHttpConnector(server, TsDBFactory.WEB_SERVER_PORT, createBaseHttpConfiguration());
			if(jwsServerConnector != null) {
				server.setConnectors(new Connector[] {httpConnector, jwsServerConnector});
			} else {
				server.setConnectors(new Connector[] {httpConnector});
			}
		}

		ContextHandler contextRedirect = new ContextHandler(TsDBFactory.WEB_SERVER_PREFIX_BASE_URL);
		contextRedirect.setHandler(new BaseRedirector(TsDBFactory.WEB_SERVER_PREFIX_BASE_URL+WEBCONTENT_PART_URL));

		boolean wrap = TsDBFactory.WEB_SERVER_LOGIN;

		ContextHandler[] contexts = new ContextHandler[] {
				wrapLogin(createContextWebcontent(),wrap), 
				wrapLogin(createContextTsDB(tsdb),wrap), 
				wrapLogin(createContextExport(tsdb),wrap),
				wrapLogin(createContextSupplement(),wrap), 
				wrapLogin(createContextWebDownload(),wrap),
				wrapLogin(createContextWebFiles(),wrap),
				createContextIot(tsdb),
				contextRedirect,
				Robots_txt_Handler.CONTEXT_HANDLER,
				createContextShutdown(),
				createContextInvalidURL()
		};

		ContextHandlerCollection contextCollection = new ContextHandlerCollection();
		contextCollection.setStopTimeout(DATA_TRANSFER_TIMEOUT_MILLISECONDS);
		contextCollection.setHandlers(contexts);
		//GzipHandler gzipHandler = new GzipHandler(); // GzipHandler (of new Jetty version?) breaks network text output
		//gzipHandler.setHandler(contextCollection);
		/*HandlerWrapper mod = new HandlerWrapper() {
			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
				response.addHeader("Cache-Control", "max-age=1");
				super.handle(target, baseRequest, request, response);
			}

		};
		mod.setHandler(gzipHandler);*/
		RequestLogHandler requestLogHandler = new RequestLogHandler();
		requestLogHandler.setRequestLog((Request request, Response response)->{
			log.trace("*** request   "+request.getRequestURL()+"  "+request.getQueryString());
		});
		//requestLogHandler.setHandler(gzipHandler); // GzipHandler (of new Jetty version?) breaks network text output
		requestLogHandler.setHandler(contextCollection);
		
		DefaultSessionIdManager sessionIdManager = new DefaultSessionIdManager(server);
		sessionIdManager.setWorkerName(null);
		SessionHandler sessionHandler = new SessionHandler();
		sessionHandler.setSessionIdManager(sessionIdManager);
		sessionHandler.setHttpOnly(true);
		sessionHandler.setSessionCookie("session");
		SessionCookieConfig sessionCokkieConfig = sessionHandler.getSessionCookieConfig();
		sessionCokkieConfig.setPath("/");

		HandlerList handlerList = new HandlerList();
		handlerList.addHandler(new OptionalSecuredRedirectHandler());
		handlerList.addHandler(sessionHandler);
		handlerList.addHandler(new NoindexHandler());
		handlerList.addHandler(requestLogHandler);

		server.setHandler(handlerList);

		server.setStopTimeout(DATA_TRANSFER_TIMEOUT_MILLISECONDS);

		server.start();
		//server.dumpStdErr();
		System.out.println("------------------------------------------------------------");
		System.out.println();
		System.out.println("stop Web Server:");
		System.out.println();
		System.out.println("- directly:  by pressing 'Ctrl-C'");
		System.out.println();
		System.out.println("- at local terminal:  curl --proxy '' --request POST http://localhost:" + TsDBFactory.WEB_SERVER_PORT + "/shutdown?token=stop");
		System.out.println();
		System.out.println();
		System.out.println("Web Sever started at    ***      http://[HOSTNAME]:" + TsDBFactory.WEB_SERVER_PORT + TsDBFactory.WEB_SERVER_PREFIX_BASE_URL + "      ***");
		if(use_https) {
			System.out.println();
			System.out.println("secure channel    ***      https://[HOSTNAME]:" + TsDBFactory.WEB_SERVER_HTTPS_PORT + TsDBFactory.WEB_SERVER_PREFIX_BASE_URL + "      ***");
		}
		System.out.println();
		System.out.println("waiting for requests...");

		server.join();

		System.out.println("...Web Sever stopped");		
	}

	private static UserStore openUserStore() {
		PropertyUserStore userStore = new PropertyUserStore();
		userStore.setConfig(WEB_SERVER_LOGIN_PROPERTIES_FILENAME);
		try {
			userStore.start();
		} catch (Exception e) {
			log.error(e);
		}
		return userStore;
	}

	private static ContextHandler wrapLogin(ContextHandler contextHandler, boolean wrap) {
		if(!wrap) {
			return contextHandler;
		}

		HashLoginService loginService = new HashLoginService("Web Server Login");
		UserStore userStore = openUserStore();
		loginService.setUserStore(userStore);

		Map<String, String> ipMap = new HashMap<String, String>();
		if(Files.exists(Paths.get(REALM_IP_CSV_FILENAME))) {
			Table ipTable = Table.readCSV(REALM_IP_CSV_FILENAME, ',');
			ColumnReaderString ipReader = ipTable.createColumnReader("ip");
			ColumnReaderString userReader = ipTable.createColumnReader("user");
			for(String[] row:ipTable.rows) {
				String ip = ipReader.get(row);
				String user = userReader.get(row);
				if(ipMap.containsKey(ip)) {
					log.warn("overwrite existing entry of"+ip+"  "+ipMap.get(ip)+" with "+user+"    in "+REALM_IP_CSV_FILENAME);
				}
				ipMap.put(ip, user);
			}
		}

		IpAuthentication ipAuthentication = new IpAuthentication(userStore, ipMap);		

		ConstraintSecurityHandler security = new ConstraintSecurityHandler();
		security.setHandler(contextHandler);

		Constraint constraint = new Constraint();
		constraint.setName("auth1");
		constraint.setAuthenticate(true);
		constraint.setRoles(new String[] {"**"}); // any authenticated user is permitted


		ConstraintMapping mapping = new ConstraintMapping();
		mapping.setPathSpec("/*");
		mapping.setConstraint(constraint);

		security.setConstraintMappings(Collections.singletonList(mapping));
		security.setAuthenticator(new DigestAuthenticator());
		security.setLoginService(loginService);

		HandlerList handlerList = new HandlerList();
		if(TsDBFactory.WEB_SERVER_JWS_PORT > 0) {
			try {
				InputStream in = new FileInputStream(new File(TsDBFactory.WEB_SERVER_JWS_CONFIG_FILENAME));
				YamlMap yamlMap = YamlMap.ofObject(new Yaml().load(in));
				in.close();			
				ArrayList<JwsConfig> jwsConfigList = new ArrayList<JwsConfig>();
				JwsConfig e = JwsConfig.ofYAML(yamlMap);
				jwsConfigList.add(e );
				JWSAuthentication jwsAuthentication = new JWSAuthentication(jwsConfigList);
				handlerList.addHandler(jwsAuthentication);
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		handlerList.addHandler(ipAuthentication);
		handlerList.addHandler(security);

		ContextHandler security_context = new ContextHandler();
		security_context.setHandler(handlerList);		
		return security_context;
	}

	private static ContextHandler createContextWebcontent() {
		ContextHandler contextStatic = new ContextHandler(TsDBFactory.WEB_SERVER_PREFIX_BASE_URL+WEBCONTENT_PART_URL);
		//contextStatic.setStopTimeout(GENERAL_TIMEOUT_MILLISECONDS;
		ResourceHandler resource_handler = new ResourceHandler();
		//resource_handler.setStopTimeout(FILE_DOWNLOAD_TIMEOUT_MILLISECONDS);
		//resource_handler.setMinAsyncContentLength(-1); //no async
		resource_handler.setMinMemoryMappedContentLength(-1); // not memory mapped to prevent file locking
		//resource_handler.setDirectoriesListed(true);
		resource_handler.setDirectoriesListed(false); // don't show directory content
		//resource_handler.setWelcomeFiles(new String[]{ "helllo.html" });
		resource_handler.setResourceBase(TsDBFactory.WEBCONTENT_PATH);
		HandlerList handlers = new HandlerList();
		//handlers.setStopTimeout(GENERAL_TIMEOUT_MILLISECONDS);
		handlers.setHandlers(new Handler[] {resource_handler, /*new DefaultHandler()*/ new InvalidUrlHandler("content not found")});
		contextStatic.setHandler(handlers);
		return contextStatic;		
	}

	private static ContextHandler createContextTsDB(RemoteTsDB tsdb) {
		ContextHandler contextTsdb = new ContextHandler(TsDBFactory.WEB_SERVER_PREFIX_BASE_URL+TSDB_API_PART_URL);
		TsDBAPIHandler handler = new TsDBAPIHandler(tsdb);
		//handler.setStopTimeout(TSDB_API_TIMEOUT_MILLISECONDS);
		contextTsdb.setHandler(handler);
		return contextTsdb;
	}

	private static ContextHandler createContextIot(RemoteTsDB tsdb) {
		ContextHandler contextIot = new ContextHandler(IOT_PART_URL);
		if(TsDBFactory.IOT_API) {
			IotAPIHandler handler = new IotAPIHandler(tsdb);
			contextIot.setHandler(handler);
		} else {
			contextIot.setHandler(new InvalidUrlHandler("IoT API is not activated"));
		}
		return contextIot;
	}


	private static ContextHandler createContextExport(RemoteTsDB tsdb) {
		ContextHandler contextExport = new ContextHandler(TsDBFactory.WEB_SERVER_PREFIX_BASE_URL+EXPORT_API_PART_URL);
		TsDBExportAPIHandler exportHandler = new TsDBExportAPIHandler(tsdb);
		//SessionHandler sessionHandler = new SessionHandler(); // session is obsolete, moved session to root
		//sessionHandler.setMaxInactiveInterval(EXPORT_API_SESSION_TIMEOUT_SECONDS);
		//contextExport.setHandler(sessionHandler);
		//sessionHandler.setHandler(exportHandler);
		contextExport.setHandler(exportHandler);
		return contextExport;
	}

	private static ContextHandler createContextSupplement() {
		ContextHandler contextSupplement = new ContextHandler(TsDBFactory.WEB_SERVER_PREFIX_BASE_URL+SUPPLEMENT_PART_URL);
		SupplementHandler handler = new SupplementHandler();
		//handler.setStopTimeout(TSDB_API_TIMEOUT_MILLISECONDS);
		contextSupplement.setHandler(handler);
		return contextSupplement;
	}


	private static ContextHandler createContextWebDownload() {
		ContextHandler contextHandler = new ContextHandler(TsDBFactory.WEB_SERVER_PREFIX_BASE_URL+DOWNLOAD_PART_URL);
		ResourceHandler resourceHandler = new ResourceHandler();
		//resourceHandler.setStopTimeout(FILE_DOWNLOAD_TIMEOUT_MILLISECONDS);
		//resourceHandler.setMinAsyncContentLength(-1); //no async
		//resourceHandler.setDirectoriesListed(true);
		resourceHandler.setDirectoriesListed(false); // don't show directory content
		resourceHandler.setResourceBase(TsDBFactory.WEBDOWNLOAD_PATH);
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] {resourceHandler, new DefaultHandler()});
		contextHandler.setHandler(handlers);
		return contextHandler;
	}

	private static ContextHandler createContextWebFiles() {
		ContextHandler contextHandler = new ContextHandler(TsDBFactory.WEB_SERVER_PREFIX_BASE_URL+FILES_PART_URL);
		ResourceHandler resourceHandler = new ResourceHandler();
		//resourceHandler.setStopTimeout(FILE_DOWNLOAD_TIMEOUT_MILLISECONDS);
		//resourceHandler.setMinAsyncContentLength(-1); //no async
		//resourceHandler.setDirectoriesListed(true);
		resourceHandler.setDirectoriesListed(false); // !! show directory content !!
		resourceHandler.setResourceBase(TsDBFactory.WEBFILES_PATH);
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] {resourceHandler, new DefaultHandler()});
		contextHandler.setHandler(handlers);
		return contextHandler;
	}	

	private static ContextHandler createContextInvalidURL() {
		ContextHandler contextInvalidURL = new ContextHandler();
		Handler handler = new InvalidUrlHandler("page not found");
		contextInvalidURL.setHandler(handler);
		return contextInvalidURL;
	}

	private static void createRainbowScale() {

		try{
			BufferedImage rainbow = ImageIO.read(new File(TsDBFactory.CONFIG_PATH,"scale_round_rainbow.png"));
			Color[] indexedColors = new Color[rainbow.getWidth()];
			for(int i=0;i<indexedColors.length;i++) {
				int c = rainbow.getRGB(i, 0);
				Color color = new Color(c);
				indexedColors[i] = color;
			}
			TimeSeriesPainterGraphics2D.setIndexedColors("round_rainbow",indexedColors);
		} catch(Exception e) {
			log.error(e);
		}	


		try{
			BufferedImage rainbow = ImageIO.read(new File(TsDBFactory.CONFIG_PATH,"scale_rainbow.png"));
			Color[] indexedColors = new Color[rainbow.getWidth()];
			for(int i=0;i<indexedColors.length;i++) {
				int c = rainbow.getRGB(i, 0);
				Color color = new Color(c);

				indexedColors[i] = color;
			}
			TimeSeriesPainterGraphics2D.setIndexedColors("rainbow",indexedColors);
		} catch(Exception e) {
			log.error(e);
		}


	}

	private static ContextHandler createContextShutdown() {
		ContextHandler contextShutdown = new ContextHandler();
		Handler handler = new ShutdownHandler("stop", false, true);
		contextShutdown.setHandler(handler);
		return contextShutdown;
	}


}
