package org.jnet.core.connection.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractRudpConnection implements Runnable {
	private static final Logger logger = LogManager.getLogger(AbstractRudpConnection.class);

	protected final Socket socket;
	
	protected AbstractRudpConnection(Socket socket) {
		this.socket = socket;
	}
	
	public void close() throws IOException {
		if (!socket.isClosed()) {
			socket.close();
		}
	}
	
	protected void send(Map<String, Object> map) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new ObjectOutputStream(baos).writeObject(map);
		byte[] data = baos.toByteArray();
		logger.debug("{} sending {} bytes ({})", myName(), data.length, map);
		socket.getOutputStream().write(data);
		socket.getOutputStream().flush();
	}
	
	abstract protected void handleMap(Map<String, Object> map);
	
	abstract protected String myName();
	
	public SocketAddress getAddress() {
		return InetSocketAddress.createUnresolved(socket.getInetAddress().getHostAddress(), socket.getLocalPort());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		while (!socket.isClosed()) {
			try {
				Map<String, Object> map = (Map<String, Object>) new ObjectInputStream(socket.getInputStream()).readObject();
				logger.debug("{} received new message: {}", myName(), map);
				handleMap(map);
			} catch (Exception e) {
				logger.error("error receiving message from at {}", myName(), e);
			}
		}
	}
}
