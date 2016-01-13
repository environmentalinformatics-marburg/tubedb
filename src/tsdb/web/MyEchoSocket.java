package tsdb.web;

public class MyEchoSocket /*implements WebSocketListener*/ {
	/*private static final Logger log = LogManager.getLogger();

	//private Session session;
	private RemoteEndpoint remote;

	public MyEchoSocket() {
		System.out.println("create MyEchoSocket");
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		System.out.println("onWebSocketBinary");

	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		System.out.println("onWebSocketClose");

	}

	@Override
	public void onWebSocketConnect(Session session) {
		//this.session = session;
		this.remote = session.getRemote();
		System.out.println(remote.getClass());
		System.out.println("onWebSocketConnect: "+session.getUpgradeRequest().getRequestURI());
		System.out.println(session.getUpgradeRequest().getSession());
		//session.getRemote().

	}

	@Override
	public void onWebSocketError(Throwable cause) {
		System.out.println("onWebSocketError");

	}

	@Override
	public void onWebSocketText(String message) {
		System.out.println("onWebSocketText: "+message);
		try {
			remote.sendString("1");
			remote.sendString("2");
			remote.sendString("3");
		} catch(Exception e) {
			log.error(e);
		}

	}*/
}
