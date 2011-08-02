package fr.inria.mdca.util;

public class MathHelper {
	
	public static int calculateCombination(int elements,int selection){

		
		return (factorial(elements)/(factorial(selection)*factorial(elements-selection)));
	}
	public static int calculateCombinationRep(int elements,int selection){
		int return_=0;
		if(elements==selection)
			return_=factorial(elements);
		else
			return_=factorial(elements)/(factorial(elements-selection));
		return return_;
	}
	
	public static int permutations(int elements,int selection){
		return selection;
	}
	
	public static int factorial(int n){
		if(n>0)
			return n*factorial(n-1);
		else return 1;
	}
	
	

}
