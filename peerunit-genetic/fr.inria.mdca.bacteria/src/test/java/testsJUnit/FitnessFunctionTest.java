package testsJUnit;

import java.util.Vector;

import Dummies.DummyBacterium;
import Dummies.DummyFitness;
import Dummies.DummyMutationFunction;
import Dummies.DummyStoppingCriterion;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import projetBacterioJava.BacteriologicAlgorithm;

import static org.junit.Assert.*;


/**
 * FitnessFunction test
 */
public class FitnessFunctionTest  {


    DummyBacterium Bact1;
    DummyBacterium Bact2;
    DummyBacterium Bact3;
    DummyBacterium Bact4;
    DummyBacterium Bact5;
    DummyFitness testFitnessFunction;
    DummyMutationFunction testMutationFunction;
    DummyStoppingCriterion testStoppingCriterion;
    BacteriologicAlgorithm testBactAlg;


    /**
     * Test for RelativeFitness
     */
    @Ignore
    public void testRelativeFitness() {
        testFitnessFunction.updateSolutionSetFitness();
        assertTrue(near(testFitnessFunction.relativeFitness(Bact1), 1000.0204f));
        assertTrue(near(testFitnessFunction.relativeFitness(Bact2), 1000.0222f));
        assertTrue(near(testFitnessFunction.relativeFitness(Bact3), 1000.0192f));
        assertTrue(near(testFitnessFunction.relativeFitness(Bact4), 1000.0185f));
        assertTrue(near(testFitnessFunction.relativeFitness(Bact5), 1000.02325f));
    }


    /**
     * Test for Fitness
     */
    @Test
    @Ignore
    public void testFitness() {
        Vector vect = testBactAlg.getSolutionSet();
        Vector vectVide = new Vector();
        vect.add(Bact1);
        vect.add(Bact2);
        vect.add(Bact3);
        assertTrue(near(testFitnessFunction.fitness(vect), (1.0f / 46)));
        assertTrue(near(testFitnessFunction.fitness(vectVide), -1000));
    }


    /**
     * @param f1 a float
     * @param f2 another float
     * @return true if f1 is near f2
     */
    public boolean near(float f1, float f2) {
        return (Math.abs(f1 - f2) < 0.0001);
    }


    /**
     * @see TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        //super.setUp();
        Bact1 = new DummyBacterium(1);
        Bact2 = new DummyBacterium(5);
        Bact3 = new DummyBacterium(-2);
        Bact4 = new DummyBacterium(-4);
        Bact5 = new DummyBacterium(7);
        Vector vectMedium = new Vector();
        testFitnessFunction = new DummyFitness();
        testMutationFunction = new DummyMutationFunction(5);
        testStoppingCriterion = new DummyStoppingCriterion();
        vectMedium.add(Bact1);
        vectMedium.add(Bact2);
        vectMedium.add(Bact3);
        vectMedium.add(Bact4);
        vectMedium.add(Bact5);
        testBactAlg = new BacteriologicAlgorithm(
                vectMedium,
                testFitnessFunction,
                testMutationFunction,
                testStoppingCriterion, 3, 5, 100, 0f, 0f);
    }

}
