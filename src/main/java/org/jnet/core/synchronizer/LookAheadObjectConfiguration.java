package org.jnet.core.synchronizer;

import java.util.HashSet;
import java.util.Set;

import org.jnet.core.synchronizer.DefaultDiffIdentifier.AbstractTypeHandler;

public class LookAheadObjectConfiguration<T> {
	private CloneStrategy cloneStrategy;

	private LateEventStrategy lateEventStrategy;

	private DiffIdentifier<T, ?> diffIdentifier;
	
	private Set<AbstractTypeHandler<?, ?>> typeHandlers = new HashSet<>();

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

	public DiffIdentifier<T, ?> getDiffIdentifier() {
		return diffIdentifier;
	}

	public void setDiffIdentifier(DiffIdentifier<T, ?> diffIdentifier) {
		this.diffIdentifier = diffIdentifier;
	}
	
	public void addTypeHandler(AbstractTypeHandler<?, ?> typeHandler) {
		typeHandlers.add(typeHandler);
	}

	public Set<AbstractTypeHandler<?, ?>> getTypeHandlers() {
		return typeHandlers;
	}

	public void setTypeHandlers(Set<AbstractTypeHandler<?, ?>> typeHandlers) {
		this.typeHandlers = typeHandlers;
	}
}
