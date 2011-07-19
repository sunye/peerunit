package fr.inria.bacteria.impl;

import fr.inria.bacteria.IObjective;
import fr.inria.bacteria.IObjectiveValue;

public class DoubleObjectiveValue implements IObjectiveValue {
	
	private IObjective objective;
	
	private double value;
	
	public double getDoubleValue() {
		return value;
	}

	public DoubleObjectiveValue(double value,IObjective objective) {
		super();
		this.value = value;
		this.objective=objective;
		
	}

	public IObjective getObjective() {
		return objective;
	}

}
