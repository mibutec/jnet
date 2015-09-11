package org.jnet.core.tools;

import org.jnet.core.tools.RetryService.RunnableWithThrowable;

public interface Eventually {
	default public void eventually(RunnableWithThrowable runnable) {
		eventually(1000, runnable);
	}

	@SuppressWarnings("unchecked")
	default public void eventually(int timeout, RunnableWithThrowable runnable) {
		new RetryService().runRetrying(() -> {
			runnable.run();
			
			return 1;
		}, Integer.MAX_VALUE, timeout, new Class[] {Error.class});
	}
}
