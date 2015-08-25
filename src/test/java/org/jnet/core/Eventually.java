package org.jnet.core;

import org.jnet.core.RetryService.RunnableWithThrowable;

public interface Eventually {
	@SuppressWarnings("unchecked")
	default public void eventually(RunnableWithThrowable runnable) {
		new RetryService().runRetrying(() -> {
			runnable.run();
			
			return 1;
		}, Integer.MAX_VALUE, 1000, new Class[] {Error.class});
	}
}
