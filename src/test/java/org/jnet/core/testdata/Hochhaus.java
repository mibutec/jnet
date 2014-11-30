package org.jnet.core.testdata;

public class Hochhaus {
	private FigureState[] figures;
	
	private FahrstuhlState fahrstuhl;
	
	private volatile FigureState volatileState;

	public Hochhaus() {
		super();
		this.fahrstuhl = new FahrstuhlState();
		this.figures = new FigureState[4];
		for (int i = 0; i < 4; i++) {
			figures[i] = new FigureState(fahrstuhl);
		}
		
		volatileState = new FigureState(fahrstuhl);
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

	public FigureState getVolatileState() {
		return volatileState;
	}

	public void setVolatileState(FigureState volatileState) {
		this.volatileState = volatileState;
	}
}
