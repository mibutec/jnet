package org.jnet.core.connection;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnet.core.AbstractGameEngine;
import org.jnet.core.MetaDataManager;
import org.jnet.core.connection.messages.EventMessage;
import org.jnet.core.connection.messages.MapMessage;
import org.jnet.core.connection.messages.Message;
import org.jnet.core.connection.messages.NewStateMessage;
import org.jnet.core.connection.messages.TimeRequestMessage;
import org.jnet.core.connection.messages.TimeResponseMessage;

public abstract class AbstractConnection implements Connection, Runnable {
	private static final Logger logger = LogManager.getLogger(AbstractConnection.class);
	
	protected final Queue<Message> queue;
	
	protected AbstractGameEngine gameEngine;
	
	public AbstractConnection() {
		this.queue = new ConcurrentLinkedQueue<Message>();
	}

	@Override
	public void setGameEngine(AbstractGameEngine gameEngine) {
		this.gameEngine = gameEngine;
		new Thread(this).start();
	}

	@SuppressWarnings("unchecked")
	private static final Class<? extends Message>[] KnownMessages = new Class[] {
		EventMessage.class,
		MapMessage.class,
		TimeRequestMessage.class,
		TimeResponseMessage.class,
		NewStateMessage.class
	};
	protected static final Map<Class<? extends Message>, Byte> MessageToIdMapping = new HashMap<>();
	protected static final Map<Byte, Class<? extends Message>> IdToMessageMapping = new HashMap<>();
	
	static {
		for (int i = 0; i < KnownMessages.length; i++) {
			MessageToIdMapping.put(KnownMessages[i], (byte) i);
			IdToMessageMapping.put((byte) i, KnownMessages[i]);
		}
	}
	
	protected abstract OutputStream getOutputStream() throws IOException;

	protected abstract InputStream getInputStream() throws IOException;

	protected abstract boolean isClosed();

	public void send(Message message) throws Exception {
		byte id = MessageToIdMapping.get(message.getClass());
		getOutputStream().write(new byte[] {id});
		
		if (logger.isDebugEnabled()) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			message.write(new DataOutputStream(baos));
			byte[] data = baos.toByteArray();
			logger.debug("{} sending {} bytes ({})", gameEngine.name(), data.length + 1, message);
			getOutputStream().write(data);
		} else {
			message.write(new DataOutputStream(getOutputStream()));
		}
		getOutputStream().flush();
	}
	
	@Override
	public void run() {
		while (!isClosed()) {
			try {
				byte messageKey = (byte) getInputStream().read();
				Class<? extends Message> messageClass = IdToMessageMapping.get(messageKey);
				if (messageClass == null) {
					logger.error("Message with unknow messagekey {} arrived at {}, just know messagekeys {}", messageKey, gameEngine.name(), IdToMessageMapping);
					continue;
				} else {
					logger.debug("Message of type {} arrived at {}", messageClass.getName(), gameEngine.name());
				}
				
				Message message;
				try {
					Constructor<? extends Message> constructor = messageClass.getConstructor(new Class<?>[]{MetaDataManager.class});
					message = constructor.newInstance(new Object[] {gameEngine.getMetaDataManager()});
				} catch (Exception e) {
					message = messageClass.newInstance();
				}
				
				message.read(new DataInputStream(getInputStream()));
				logger.debug("{} received new message: {}", gameEngine.name(), message);
				
				message.setSender(this);
				queue.add(message);
			} catch (Exception e) {
				logger.error("error receiving message at {}", gameEngine.name(), e);
			}
		}
	}

	@Override
	public Message nextMessage() {
		return queue.poll();
	}
}
