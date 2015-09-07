package org.jnet.core.synchronizer;

public class LookAheadObjectConfiguration<T> {
	private CloneStrategy cloneStrategy;

	private LateEventStrategy lateEventStrategy;

	private DiffIdentifier<T> diffIdentifier;

	public CloneStrategy getCloneStrategy() {
		return cloneStrategy;
	}

	public void setCloneStrategy(CloneStrategy cloneStrategy) {
		this.cloneStrategy = cloneStrategy;
	}

	public LateEventStrategy getLateEventStrategy() {
		return lateEventStrategy;
	}

	public void setLateEventStrategy(LateEventStrategy lateEventStrategy) {
		this.lateEventStrategy = lateEventStrategy;
	}

	public DiffIdentifier<T> getDiffIdentifier() {
		return diffIdentifier;
	}

	public void setDiffIdentifier(DiffIdentifier<T> diffIdentifier) {
		this.diffIdentifier = diffIdentifier;
	}
}
