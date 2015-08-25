package org.jnet.core;

import java.util.concurrent.Callable;

import com.google.common.base.Throwables;

public class Unchecker {
	public static<T> T uncheck(Callable<T> rwt) {
		try {
			return rwt.call();
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
}
