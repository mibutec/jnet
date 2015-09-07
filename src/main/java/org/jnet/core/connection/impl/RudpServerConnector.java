package org.jnet.core.connection.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.rudp.ReliableServerSocket;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jnet.core.GameServer;
import org.jnet.core.connection.ServerConnector;
import org.jnet.core.helper.Unchecker;

public class RudpServerConnector implements ServerConnector, Runnable {
	private static final Logger logger = LogManager.getLogger(RudpServerConnector.class);
	
	private GameServer server;
	
	private ServerSocket serverSocket;
	
	private final int port;
	
	public RudpServerConnector(int port) {
		super();
		logger.info("creating a RudpServerConnector on port {}", port);
		this.port = port;
	}

	@Override
	public void setGameServer(GameServer server) {
		Unchecker.uncheck(() -> {
			this.server = server;
			this.serverSocket = new ReliableServerSocket(port);
			new Thread(this).start();
			logger.info("opened RudpServerSocket on port {}", port);
		});
	}

	@Override
	public void run() {
		logger.info("RudpServerConnector waiting for clients...");
		while (!serverSocket.isClosed()) {
			try {
				Socket clientSocket = serverSocket.accept();
				server.addConnetion(new RudpConnection(server, clientSocket));
			} catch (IOException ie) {
				logger.info("server is closing, serverSocket closed...");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close() throws Exception {
		logger.info("closing Rudp-Connector on port {}", port);
		serverSocket.close();
	}
}
