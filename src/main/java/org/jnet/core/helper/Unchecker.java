package org.jnet.core.helper;

import java.util.concurrent.Callable;

public class Unchecker {
	private Unchecker() {
		
	}
	
	public static void uncheck(RunnableWithException runnable) {
		try {
			runnable.run();
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static<T> T uncheck(Callable<T> callable) {
		try {
			return callable.call();
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static interface RunnableWithException {
		public void run() throws Exception;
	}
}