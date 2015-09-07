package org.jnet.core.connection.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import net.rudp.ReliableSocket;
import net.rudp.ReliableSocketProfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnet.core.AbstractGameEngine;
import org.jnet.core.connection.AbstractConnection;

public class RudpConnection extends AbstractConnection {
	private static final Logger logger = LogManager.getLogger(RudpConnection.class);
	
	protected final Socket socket;
	
	public RudpConnection(AbstractGameEngine gameEngine, String host, int port) throws IOException {
		this(gameEngine, new ReliableSocket(createProfile()));
		socket.connect(new InetSocketAddress(host, port));
		logger.info("created RudpConnection to " + host + ":" + port);
	}

	public RudpConnection(AbstractGameEngine gameEngine, String host, int port, String localAddr, int localPort) throws IOException {
		this(gameEngine, new ReliableSocket(createProfile(),
				new InetSocketAddress(host, port),
				new InetSocketAddress(localAddr, localPort)));
	}
	
	public RudpConnection(AbstractGameEngine gameEngine, Socket socket) throws IOException {
		super(gameEngine);
		this.socket = socket;
		logSocket();
		startListening();
	}
	
	private void logSocket() {
		logger.info("created RudpConnection to " + socket.getRemoteSocketAddress() + " on " + socket.getLocalAddress() + ":" + socket.getLocalPort());
	}

	private static ReliableSocketProfile createProfile() {
		ReliableSocketProfile profile = new ReliableSocketProfile();
		profile.setMaxSegmentSize(1400);
		return profile;
	}

	public void close() throws IOException {
		if (!socket.isClosed()) {
			socket.close();
		}
	}
	
	public SocketAddress getAddress() {
		return InetSocketAddress.createUnresolved(socket.getInetAddress().getHostAddress(), socket.getLocalPort());
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	@Override
	protected boolean isClosed() {
		return socket.isClosed();
	}
}
