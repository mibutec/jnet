package org.jnet.core.synchronizer;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class LookAheadObjectMasterTest extends AbstractLookAheadObjectTest {
//	@Test
	public void shouldIncreaseTimestampOnEvolve() {
		UpdateableTestobject object = new UpdateableTestobject();
		testee = createTestee(object);
		testee().evolveLastTrustedState(1000);
		assertThat(testee.lastTrustedState.getTimestamp(), is(1000));
	}
	
	private LookAheadObjectMaster<?> testee() {
		return (LookAheadObjectMaster<?>) testee;
	}

	@Override
	protected <T> LookAheadObject<T> createTestee(T object) {
		return new LookAheadObjectMaster<T>(object);
	}
}
