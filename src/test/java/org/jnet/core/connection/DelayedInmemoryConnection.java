package org.jnet.core.connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayDeque;
import java.util.Queue;

import org.jala.mixins.Sleep;
import org.jnet.core.AbstractGameEngine;
import org.jnet.core.connection.messages.Message;

public class DelayedInmemoryConnection implements Connection, Sleep {
	private long delay;
	
	private DelayedInmemoryConnection conterpart;
	
	private Queue<Message> queue = new ArrayDeque<>();
	
	public void addMessage(Message message) {
		queue.add(message);
	}
	
	public DelayedInmemoryConnection(long delay) {
		this.delay = delay;
	}
	
	@Override
	public void send(Message message) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					sleep(delay);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					message.write(new DataOutputStream(baos));
					ByteArrayInputStream input = new ByteArrayInputStream(baos.toByteArray());
					message.read(new DataInputStream(input));
					conterpart.addMessage(message);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			}
		}).start();
	}
	
	public void setConterpart(DelayedInmemoryConnection connection) {
		this.conterpart = connection;
	}
	
	public DelayedInmemoryConnection getConterpart() {
		return conterpart;
	}

	@Override
	public void close() throws Exception {
		// nothing to do
	}

	@Override
	public Message nextMessage() {
		return queue.poll();
	}

	@Override
	public void setGameEngine(AbstractGameEngine engine) {
		// nothing to do
	}
}
