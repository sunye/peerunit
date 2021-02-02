package fr.inria.triskell.moga.jgap;

import org.jgap.InvalidConfigurationException;

public interface ISPEA2Breeder {
	public SPEA2Population evolveAlpha(SPEA2Population population,SPEA2Configuration configuration) throws InvalidConfigurationException;
	public SPEA2Population evolveBeta(SPEA2Population population,SPEA2Configuration configuration) throws InvalidConfigurationException;
}
