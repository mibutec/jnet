package org.jnet.core.testdata;

import org.jnet.core.Action;
import org.jnet.core.UpdateableObject;

public class FigureState implements UpdateableObject {
	private static final long serialVersionUID = 1L;

	private int targetX;
	
	private float speed = 0.05f; // pixel / ms
	
	private float x;
	
	private FahrstuhlState fahrstuhl;
	
	private transient String name;
	
	public FigureState(FahrstuhlState fahrstuhl) {
		super();
		this.fahrstuhl = fahrstuhl;
	}
	
	public FigureState() {
		super();
	}



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "FigureState (" + name + ")[targetX=" + targetX + ", speed=" + speed + ", x=" + x + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(speed);
		result = prime * result + targetX;
		result = prime * result + Float.floatToIntBits(x);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FigureState other = (FigureState) obj;
		if (Float.floatToIntBits(speed) != Float.floatToIntBits(other.speed))
			return false;
		if (targetX != other.targetX)
			return false;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		return true;
	}

	@Override
	public void update(long delta) {
		if (targetX > x) {
			x = Math.min(x + speed * delta, targetX);
		} else {
			x = Math.max(x - speed * delta,  targetX);
		}
	}
	
	@Action
	public void gotoX(int x) {
		this.targetX = x;
	}

	public int getTargetX() {
		return targetX;
	}

	public void setTargetX(int targetX) {
		this.targetX = targetX;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public FahrstuhlState getFahrstuhl() {
		return fahrstuhl;
	}

	public void setFahrstuhl(FahrstuhlState fahrstuhl) {
		this.fahrstuhl = fahrstuhl;
	}
}
