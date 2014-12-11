package org.jnet.core.testdata;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Hochhaus {
	private Float something = 23f;
	
	private FigureState[] figures;
	
	private FahrstuhlState fahrstuhl;
	
	private transient FigureState transientState;
	
	private int[] intArray = new int[] {0, 1, 2, 5};
	
	private List<FigureState> moreFigures;
	
	private List<Integer> morePrimitives;
	
	private Map<FigureState, FahrstuhlState> complexeMap;
	
	public Hochhaus() {
		super();
		this.fahrstuhl = new FahrstuhlState();
		this.figures = new FigureState[4];
		for (int i = 0; i < 4; i++) {
			figures[i] = new FigureState(fahrstuhl);
		}
		
		moreFigures = new LinkedList<>();
		for (int i = 0; i < 4; i++) {
			moreFigures.add(new FigureState());
		}
		
		morePrimitives = new LinkedList<>();
		for (int i = 0; i < 4; i++) {
			morePrimitives.add(i);
		}
		
		complexeMap = new HashMap<>();
		complexeMap.put(new FigureState(), new FahrstuhlState());
		
		transientState = new FigureState(fahrstuhl);
	}

	public FigureState[] getFigures() {
		return figures;
	}

	public void setFigures(FigureState[] figures) {
		this.figures = figures;
	}

	public FahrstuhlState getFahrstuhl() {
		return fahrstuhl;
	}

	public void setFahrstuhl(FahrstuhlState fahrstuhl) {
		this.fahrstuhl = fahrstuhl;
	}

	public FigureState getTransientState() {
		return transientState;
	}

	public void setTransientState(FigureState transientState) {
		this.transientState = transientState;
	}

	public int[] getIntArray() {
		return intArray;
	}

	public void setIntArray(int[] intArray) {
		this.intArray = intArray;
	}

	public List<FigureState> getMoreFigures() {
		return moreFigures;
	}

	public void setMoreFigures(List<FigureState> moreFigures) {
		this.moreFigures = moreFigures;
	}

	public List<Integer> getMorePrimitives() {
		return morePrimitives;
	}

	public void setMorePrimitives(List<Integer> morePrimitives) {
		this.morePrimitives = morePrimitives;
	}

	public Map<FigureState, FahrstuhlState> getComplexeMap() {
		return complexeMap;
	}

	public void setComplexeMap(Map<FigureState, FahrstuhlState> complexeMap) {
		this.complexeMap = complexeMap;
	}

	public Float getSomething() {
		return something;
	}

	public void setSomething(Float something) {
		this.something = something;
	}
}
