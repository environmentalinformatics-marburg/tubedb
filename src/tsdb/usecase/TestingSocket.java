package tsdb.usecase;

import java.io.IOException;
import java.net.Socket;

public class TestingSocket {
	
	public static void main(String[] args) {
		try {
			Socket socket = new Socket("uni-marburg.de", 80, null, 0);
			System.out.println(socket.getLocalAddress().getCanonicalHostName());
			socket.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
