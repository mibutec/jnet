package org.jnet.core.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jala.mixins.Sleep;
import org.jala.mixins.Unchecker;

public class DelayedInmemoryConnection extends AbstractConnection implements Sleep {
	private long delay;
	
	private DelayedInmemoryConnection conterpart;

	private Queue<ByteWithTimestamp> queue = new LinkedBlockingQueue<>(1024 * 1024);

	private InputStream inputStream = new InputStream() {			
		@Override
		public synchronized int read() throws IOException {
			return Unchecker.uncheck(() -> {
				ByteWithTimestamp bwts = queue.peek();
				while (bwts == null || bwts.getTs() >= System.currentTimeMillis() + delay) {
					sleep(50);
					bwts = queue.peek();
				}
				return queue.poll().getB() + 128;
			});
		}
	};
	
	private OutputStream outputStream = new OutputStream() {
		@Override
		public synchronized void write(int b) throws IOException {
			conterpart.queue.add(new ByteWithTimestamp((byte) (b + 128), System.currentTimeMillis()));
		}
	};
	
	public DelayedInmemoryConnection getConterpart() {
		return conterpart;
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}
	
	@Override
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	public DelayedInmemoryConnection(long delay) {
		this.delay = delay;
	}
	
	public void setConterpart(DelayedInmemoryConnection connection) {
		this.conterpart = connection;
	}

	@Override
	public void close() throws Exception {
		// nothing to do
	}

	@Override
	protected boolean isClosed() {
		return false;
	}
}

class ByteWithTimestamp {
	private final byte b;
	
	private final long ts;

	public ByteWithTimestamp(byte b, long ts) {
		super();
		this.b = b;
		this.ts = ts;
	}

	public byte getB() {
		return b;
	}

	public long getTs() {
		return ts;
	}
}