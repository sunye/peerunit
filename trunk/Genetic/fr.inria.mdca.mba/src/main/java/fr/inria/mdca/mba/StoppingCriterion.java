package fr.inria.mdca.mba;

public abstract class StoppingCriterion {

	public abstract boolean run();

	private BactereologicAlgorithm bactereologicAlgorithm;

	public void setBactereologicAlgorithm(BactereologicAlgorithm bactereologicAlgorithm) {
		this.bactereologicAlgorithm = bactereologicAlgorithm;
	}
	public BactereologicAlgorithm getBactereologicAlgorithm() {
		return bactereologicAlgorithm;
	}

}
