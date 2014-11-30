package org.jnet.core;

import java.io.Serializable;

public interface UpdateableObject extends Serializable {
	void update(long delta);
}
