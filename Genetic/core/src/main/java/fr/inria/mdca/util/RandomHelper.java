package fr.inria.mdca.util;

import java.util.ArrayList;
import java.util.Random;

public class RandomHelper {

	public static int randomValue(double lowerBound, double upperBound) {
		Random random=new Random();
		long range = (long)upperBound - (long)lowerBound + 1;
		long fraction = (long)(range * random.nextDouble());
		int value =  (int)(fraction + lowerBound);   
		return value;
	}
	
	public static Integer selectRandom(ArrayList<Integer> values){
		int pos=RandomHelper.randomValue(0, values.size()-1);
		return values.get(pos);
	}
	
	public static Integer selectRemoveRandom(ArrayList<Integer> values){
		int pos=RandomHelper.randomValue(0, values.size()-1);
		Integer t=values.get(pos);
		values.remove(pos);
		return t;
	}
}
