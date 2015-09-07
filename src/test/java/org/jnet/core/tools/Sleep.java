package org.jnet.core.tools;

import com.google.common.base.Throwables;

public interface Sleep {
	default public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw Throwables.propagate(e);
		}
	}
}
