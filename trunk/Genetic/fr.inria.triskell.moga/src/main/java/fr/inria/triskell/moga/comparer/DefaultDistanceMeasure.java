package fr.inria.triskell.moga.comparer;



import org.jgap.Chromosome;
import org.jgap.Population;

public class DefaultDistanceMeasure implements IDistanceMeasure {

	public double[][] distanceObjectives(Population population) {
		Chromosome solutionI,solutionJ;
		double [][] distance = new double [population.size()][population.size()];  
		for (int i = 0; i < population.size(); i++){
			distance[i][i] = 0.0;
			solutionI = (Chromosome) population.getChromosome(i);
			for (int j = i + 1; j < population.size(); j++){
				solutionJ = (Chromosome) population.getChromosome(j);
				distance[i][j] = this.distanceBetweenObjectives(solutionI,solutionJ);                
				distance[j][i] = distance[i][j];            
			} // for
		} // for        

		//->Return the matrix of distances
		return distance;
	}
	public double distanceBetweenObjectives(Chromosome solutionI, Chromosome solutionJ){                
		double diff;    //Auxiliar var
		double distance = 0.0;
		//-> Calculate the euclidean distance
		for (int nObj = 0; nObj < solutionI.getMultiObjectives().size()-1;nObj++){
			diff = ((Double)solutionI.getMultiObjectives().get(nObj)).doubleValue() - ((Double)solutionJ.getMultiObjectives().get(nObj)).doubleValue();
			distance += Math.pow(diff,2.0);           
		} // for   

		//Return the euclidean distance
		return Math.sqrt(distance);
	} // dista
}
