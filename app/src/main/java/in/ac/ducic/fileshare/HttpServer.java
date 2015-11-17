package in.ac.ducic.fileshare;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer extends Thread {
	private static int port;
	private static UriInterpreter mfileUri;

	private static ServerSocket serversocket = null;
	private boolean webserverLoop = true;

	public static void SetFiles(UriInterpreter fileUri) {
		HttpServer.mfileUri = fileUri;
	}
	public HttpServer(int listen_port) {
		port = listen_port;
		if (serversocket == null) {
			this.start();
		}
	}



	public synchronized void stopServer() {
		webserverLoop = false;
		if (serversocket != null) {
			try {
				serversocket.close();
				serversocket = null;
			} catch (IOException e) {

				 e.printStackTrace();
			}
		}
	}




	private boolean normalBind(int thePort) {
		try {
			serversocket = new ServerSocket(thePort);
		} catch (Exception e) {
			return false;
		}
		port = thePort;
		return true;
	}

	private static final ExecutorService threadPool = Executors.newCachedThreadPool();

	public void run() {
		if (!normalBind(port)) {
			return;
		}


		while (webserverLoop) {
			try {
				Socket connectionsocket = serversocket.accept();
				HttpServerConnection theHttpConnection = new HttpServerConnection(mfileUri, connectionsocket);

				threadPool.submit(theHttpConnection);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	public static String getIPAddress() {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress();
						boolean isIPv4 = sAddr.contains("192");
							if (isIPv4)
								return "http://"+sAddr+":"+port;

					}
				}
			}
		} catch (Exception ex) { }
		return "";
	}



}
