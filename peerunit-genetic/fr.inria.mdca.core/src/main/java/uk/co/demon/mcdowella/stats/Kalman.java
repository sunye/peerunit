package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.io.StreamTokenizer;

/** This applies a Kalman filter to a 1-dimensional series. The maths
 *  is taken from Chapter 13 of "Time Series Analysis" by 
 *  James D. Hamilton, except that I treat only 1-dimensional series,
 *  I provide only expectations of smoothed values, and I don't try
 *  to go on to do an EM pass.
 */
public class Kalman
{
  /** time series */
  private double[] y;
  /** set the observations */
  public void setY(double[] forY)
  {
    y = (double[]) forY.clone();
  }
  /** vector of noise variance after transition */
  private double[] wVariance;
  /** Set noise variance vector after transition */
  public void setTransitionNoise(double[] forNoise)
  {
    wVariance = (double[]) forNoise.clone();
  }
  /** variance of noise applied before observation */
  private double vVariance;
  /** set variance of noise applied before observation */
  public void setVariance(double v)
  {
    vVariance = v;
  }
  /** observation vector applied to state vector to produce output */
  private double[] hVec;
  /** set observation vector applied to state vector to produce output 
   */
  public void setOutputVec(double[] forVec)
  {
    hVec = (double[]) forVec.clone();
  }
  /** state transition matrix */
  private double[] fMat;
  /** set state transition matrix */
  public void setTransitionMatrix(double[] mat)
  {
    fMat = (double[]) mat.clone();
  }
  /** expected initial state */
  private double[] initialState;
  /** set expected initial state */
  public void setInitialState(double[] initial)
  {
    initialState = (double[]) initial.clone();
  }
  /** variance of initial state */
  private double[] initialVariance;
  /** set variance of initial state */
  public void setInitialVariance(double[] vc)
  {
    initialVariance = (double[]) vc.clone();
  }
  /** Calculate log likelihood of observation given params and 
   *  optionally calculate expected observation at each point from
   *  expected hidden state at each point given the entire data.
   *  @param smooth whether to smooth observations (as opposed to just
   *  calculating the log likelihood)
   *  @exception IllegalStateException if parms set up by earlier
   *  calls nonsensical or inconsistent.
   *  @exception InputException if model does not fit data. E.g.
   *  model makes zero variance prediction that does not work or
   *  some variance-covariance matrix cannot be inverted.
   */
  public void fit(boolean smooth) throws InputException
  {
    if (y == null)
    {
      throw new IllegalStateException("Observations not set up");
    }
    if (wVariance == null)
    {
      throw new IllegalStateException("Transition variance not set up");
    }
    for (int i = 0; i < wVariance.length; i++)
    {
      if (wVariance[i] < 0.0)
      {
        throw new IllegalStateException("Transition variance is -ve");
      }
    }
    if (hVec == null)
    {
      throw new IllegalStateException("Observation vector not set up");
    }
    if (fMat == null)
    {
      throw new IllegalStateException("Transition matrix not set up");
    }
    if (vVariance < 0.0)
    {
      throw new IllegalStateException("-ve observation variance");
    }
    if (initialState == null)
    {
      throw new IllegalStateException("Initial state not set up");
    }
    if (initialVariance == null)
    {
      throw new IllegalStateException("Initial variance not set up");
    }
    if (wVariance.length != hVec.length)
    {
      throw new IllegalStateException(
      "mismatch of transition variance and observation vector lengths");
    }
    if (initialState.length != hVec.length)
    {
      throw new IllegalStateException(
        "mismatch of length of initial state and observation vector");
    }
    if (hVec.length * hVec.length != fMat.length)
    {
      throw new IllegalStateException(
        "mismatch of observation vector and transition matrix length");
    }
    if (initialVariance.length != fMat.length)
    {
      throw new IllegalStateException(
        "mismatch of initial variance and transition matrix length");
    }
    for (int i = 0; i < hVec.length; i++)
    {
      if (initialVariance[hVec.length * i + i] < 0.0)
      {
	// This is an error because it means a unit vector would
	// have -ve v'Mv which means not +ve definate. Passing
	// this test does not guarantee that our variance matrix is
	// +ve definate, though.
        throw new IllegalStateException(
	  "-ve diagonal element on initial variance");
      }
    }
    // Starts off as expected value of underlying state at time t
    // given observations up to and including time t
    double[][] smoothed = new double[y.length][];
    // variance of smoothed
    double[][] smoothedVariance = new double[y.length][];
    // used to compute log-likelihood so far
    double llSoFar = 0.0;
    int predictions = 0;
    smoothed[0] = (double[]) initialState.clone();
    smoothedVariance[0] = (double[])initialVariance.clone();
    // System.out.println("Initial variance is");
    // showMat(initialState.length, initialVariance);
    // Used to work out correction term in forward recursion
    // This is the variance-covariance matrix times the observation
    // vector
    double[] ph = new double[hVec.length];
    // Used to work out v-c matrix after transition
    double[] fp = new double[fMat.length];
    for (int i = 0; i < y.length; i++)
    {
      // System.out.println("Smoothed variance for " + i);
      // showMat(initialState.length, smoothedVariance[i]);
      double obs = y[i];
      matrixVec(smoothedVariance[i], hVec, ph);
      double[] stateVec = smoothed[i];
      if (obs != Double.NaN)
      { // first of all work out prediction and variance
        double prediction = dotProduct(stateVec, hVec);
	double predVar = dotProduct(ph, hVec) + vVariance;
	if (predVar <= 0.0)
	{
	  if (prediction != obs)
	  {
	    throw new InputException(
	      "Zero variance prediction does not match data");
	  }
	}
	else
	{
	  predictions++;
	  double diff = obs - prediction;
	  double diffPerVar = diff / predVar;
	  llSoFar = llSoFar - diff * diffPerVar * 0.5 -
	    0.5 * Math.log(predVar);
	  /*
	  if (Double.isNaN(llSoFar))
	  {
	    System.out.println("Now nan from " + predVar);
	    for (int j = 0; j < stateVec.length; j++)
	    {
	      System.out.println(stateVec[j]);
	    }
	    showMat(stateVec.length, smoothedVariance[i]);
	    System.exit(1);
	  }
	  */
	  // now update state
	  for (int j = 0; j < ph.length; j++)
	  {
	    stateVec[j] += ph[j] * diffPerVar;
	  }
	  // and variance-covariance matrix of state
	  // System.out.println("PredVar is " + predVar);
	  subScaledOuter(smoothedVariance[i], ph, 1.0 / predVar);
	  // System.out.println("Smoothed variance for " + i + " after obs");
	  // showMat(initialState.length, smoothedVariance[i]);
	}
      }
      if (i < (y.length - 1))
      {
	smoothed[i + 1] = new double[initialState.length];
	// Apply transition matrix
	matrixVec(fMat, smoothed[i], smoothed[i + 1]);
	matrixMatrix(initialState.length, fMat, smoothedVariance[i],
	  fp);
	smoothedVariance[i + 1] = new double[initialVariance.length];
	matrixMatrixT(initialState.length, fp, fMat,
	  smoothedVariance[i + 1]);
	// add on noise
	int skip = wVariance.length + 1;
	for (int j = 0; j < wVariance.length; j++)
	{
	  smoothedVariance[i + 1][j * skip] += wVariance[j];
	}
      }
    }
    if (smooth)
    {
      // Now backwards pass to smooth. The estimate of the state
      // underlying the final observation already reflects all the
      // data, and we need to recurse back from there
      double[] smoothedPredict = new double[y.length];
      // error in estimate for state underlying i given data before i
      double[] currentPrevError = new double[initialState.length];
      // variance of prediction of state
      double[] predVc = new double[initialVariance.length];
      // error after multiplication by inverse
      double[] invByError = new double[initialState.length];
      // error after multiplication by transpose of transition and
      // inverse
      double[] fpError = new double[initialState.length];
      // correction to make for i-1 state
      double[] correction = new double[initialState.length];
      for (int i = y.length - 1; i >= 0; i--)
      {
	smoothedPredict[i] = dotProduct(hVec, smoothed[i]);
	if (i > 0)
	{
	  // work out prediction of current state from prev state
	  matrixVec(fMat, smoothed[i - 1], currentPrevError);
	  for (int j = 0; j < currentPrevError.length; j++)
	  {
	    currentPrevError[j] = smoothed[i][j] - currentPrevError[j];
	  }
	  // Work out variance of prediction
	  matrixMatrix(initialState.length, fMat,
	    smoothedVariance[i - 1], fp);
	  matrixMatrixT(initialState.length, fp, fMat, predVc);
	  // add on noise
	  int skip = wVariance.length + 1;
	  for (int j = 0; j < wVariance.length; j++)
	  {
	    predVc[j * skip] += wVariance[j];
	  }
	  // System.out.println("VC matrix for " + i);
	  // showMat(initialState.length, predVc);
	  // Need inverse of variance - covariance matrix to do
	  // matrix multiply with - but we will do it by equation
	  // solving
	  LU lu = new LU(initialState.length, predVc);
	  if (lu.getDeterminant() <= 0.0)
	  { // variance-covariance matrix must have determinant >= 0
	    // and we need > 0 to use inverse
	    throw new InputException(
      "Variance-Covariance matrix in smoothing has determinant <= 0");
	  }
	  lu.solve(currentPrevError, invByError);
	  matrixTransVec(fMat, invByError, fpError);
	  matrixVec(smoothedVariance[i - 1], fpError, correction);
	  for (int j = 0; j < initialState.length; j++)
	  {
	    smoothed[i - 1][j] += correction[j];
	  }
	}
      }
      smoothedObs = smoothedPredict;
    }
    else
    {
      smoothedObs = null;
    }
    logLikelihood = llSoFar -
      0.5 * predictions * Math.log(2.0 * Math.PI);
  }
  private static void showMat(int len, double[] mat)
  {
    int wp = 0;
    for (int i = 0; i < len; i++)
    {
      String sep = "";
      for (int j = 0; j < len; j++)
      {
	System.out.print(sep);
        System.out.print(mat[wp++]);
	sep = " ";
      }
      System.out.println();
    }
  }
  /** smoothed observations */
  private double[] smoothedObs;
  /** return vector of smoothed observations */
  public double[] getSmoothedObservations()
  {
    return (double[])smoothedObs.clone();
  }
  /** log-likelihood from last fit */
  private double logLikelihood;
  /** return log likelihood of data given parameters of last fit */
  public double getLogLikelihood()
  {
    return logLikelihood;
  }
  /** vector-vector dot product */
  public static double dotProduct(double[] a, double[] b)
  {
    double sum = 0.0;
    for (int i = 0; i < a.length; i++)
    {
      sum += a[i] * b[i];
    }
    return sum;
  }
  /** v'Mv - transpose of vector times matrix times vector
  public static double vtMv(double[] vec, double[] matrix)
  {
    double sum = 0.0;
    int wp = 0;
    for (int i = 0; i < vec.length; i++)
    {
      double dot = 0.0;
      for (int j = 0; j < vec.length; j++)
      {
        dot += matrix[wp++] * vec[j];
      }
      sum += dot * vec[i];
    }
    return sum;
  }
  */
  /** Matrix-vector product */
  public static void matrixVec(double[] matrix, double[] inVec,
    double[] outVec)
  {
    int wp = 0;
    for (int i = 0; i < inVec.length; i++)
    {
      double sum = 0.0;
      for (int j = 0; j < inVec.length; j++)
      {
        sum += matrix[wp++] * inVec[j];
      }
      outVec[i] = sum;
    }
  }
  /** MatrixTranspose-vector product */
  public static void matrixTransVec(double[] matrix, double[] inVec,
    double[] outVec)
  {
    for (int i = 0; i < inVec.length; i++)
    {
      double sum = 0.0;
      for (int j = 0; j < inVec.length; j++)
      {
        sum += matrix[j * inVec.length + i] * inVec[j];
      }
      outVec[i] = sum;
    }
  }
  /** matrix-matrix product */
  public static void matrixMatrix(int dim, double[] a, double[] b,
    double[] ab)
  {
    for (int i = 0; i < dim; i++)
    {
      for (int j = 0; j < dim; j++)
      {
        double sum = 0.0;
	for (int k = 0; k < dim; k++)
	{
	  sum += a[i * dim + k] * b[k * dim + j];
	}
	ab[i * dim + j] = sum;
      }
    }
  }
  /** matrix-matrix transpose product */
  public static void matrixMatrixT(int dim, double[] a, double[] b,
    double[] abt)
  {
    for (int i = 0; i < dim; i++)
    {
      for (int j = 0; j < dim; j++)
      {
        double sum = 0.0;
	for (int k = 0; k < dim; k++)
	{
	  sum += a[i * dim + k] * b[j * dim + k];
	}
	abt[i * dim + j] = sum;
      }
    }
  }
  /** subtract a scaled outer product from a matrix */
  public static void subScaledOuter(double[] mat, double[] vec,
    double scale)
  {
    int wp = 0;
    for (int i = 0; i < vec.length; i++)
    {
      double v = vec[i];
      for (int j = 0; j < vec.length; j++)
      {
        mat[wp++] -= vec[j] * v * scale;
      }
    }
  }
  /** Exception thrown if input data does not make sense according
   *  to model or if model cannot be fitted to data due to division
   *  by zero.
   */
  public static class InputException extends Exception
  {
    InputException(String reason)
    {
      super(reason);
    }
  }
  public static void main(String[] s) throws Exception
  {
    int goes = 10;
    long seed = 42;
    double ftol = 0.001;
    boolean trouble = false;
    boolean useSquares = false;
    boolean usePosition = false;
    int s1 = s.length - 1;
    int argp = 0;
    try
    {
      for (; argp < s.length; argp++)
      {
        if ((argp < s1) && "-ftol".equals(s[argp]))
	{
	  ftol = Double.parseDouble(s[++argp].trim());
	}
        else if ((argp < s1) && "-goes".equals(s[argp]))
	{
	  goes = Integer.parseInt(s[++argp].trim());
	}
	else if ("-pos".equals(s[argp]))
	{
	  usePosition = true;
	}
        else if ((argp < s1) && "-seed".equals(s[argp]))
	{
	  seed = Long.parseLong(s[++argp].trim());
	}
	else if ("-square".equals(s[argp]))
	{
	  // Don't use this - it doesn't really work as it does
	  // not amount to proper cross-validation
	  useSquares = true;
	}
	else
	{
	  System.err.println("Cannot handle flag " + s[argp]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Cannot read number in " + s[argp]);
      trouble = true;
    }
    if (trouble)
    {
      System.err.println(
        "Args are [-ftol #] [-goes #] [-pos] [-seed #]");
      return;
    }

    System.err.println("Ftol " + ftol + " goes " + goes + " seed " +
      seed + " squares " + useSquares + " position " + usePosition);
    if (useSquares)
    {
      System.err.println("Did you REALLY want to set useSquares?");
    }
    
    // read y values
    BufferedReader br = new BufferedReader(new InputStreamReader(
      System.in));
    StreamTokenizer st = new StreamTokenizer(br);
    st.resetSyntax();
    st.commentChar('#');
    // Have to read numbers as words because StreamTokenizers don't
    // handle exponentials
    st.wordChars('0', '9');
    st.wordChars('-', '-');
    st.wordChars('+', '+');
    st.wordChars('.', '.');
    st.wordChars('e', 'e');
    st.wordChars('E', 'E');
    st.slashSlashComments(true);
    st.slashStarComments(true);
    List l = new ArrayList();
    for (;;)
    {
      int tok = st.nextToken();
      if (tok == StreamTokenizer.TT_EOF)
      {
        break;
      }
      else if (tok == StreamTokenizer.TT_WORD)
      {
        try
	{
	  l.add(new Double(st.sval.trim()));
	}
	catch (NumberFormatException nfe)
	{
	  System.err.println("Cannot read number in " + st.sval);
	  return;
	}
      }
      else if (tok != StreamTokenizer.TT_EOL)
      {
	if (tok >= 0)
	{
	  char here = (char)tok;
	  if (Character.isWhitespace(here))
	  {
	    continue;
	  }
	  System.err.println("Could not handle char " + here +
	    " in input");
	  return;
	}
        System.err.println("Could not handle funny token in input");
	return;
      }
    }
    double[] y = new double[l.size()];
    int wp = 0;
    for (Iterator i = l.iterator(); i.hasNext();)
    {
      y[wp++] = ((Double)i.next()).doubleValue();
    }
    /*
    double[] smoothed = testSmoothWheel(5, y);
    */
    double[] forScore = new double[1];
    double[] smoothed = varyingVelocity(y, goes, seed, ftol, forScore,
      useSquares, usePosition);
    for (int i = 0; i < smoothed.length; i++)
    {
      System.out.println("Smoothed " + y[i] + " to " + smoothed[i]);
    }
    System.err.println("Log likelihood is " + -forScore[0]);
  }
  /** test Kalman by fitting state transition matrix that simply repeats
   *  every len times
   */
  private static double[] testSmoothWheel(int len, double[] y)
    throws InputException
  {
    Kalman kal = new Kalman();
    kal.setY(y);
    kal.setTransitionNoise(new double[len]);
    kal.setVariance(1.0);
    double[] unitV = new double[len];
    unitV[0] = 1.0;
    kal.setOutputVec(unitV);
    double[] shift = new double[len * len];
    for (int i = 0; i < len; i++)
    {
      int j = i + 1;
      if (j >= len)
      {
        j = 0;
      }
      shift[j * len + i] = 1.0;
    }
    kal.setTransitionMatrix(shift);
    kal.setInitialState(new double[len]);
    double[] var = new double[len * len];
    int len1 = len + 1;
    for (int i = 0; i < len; i++)
    {
      var[len1 * i] = 10000.0;
    }
    kal.setInitialVariance(var);
    kal.fit(true);
    System.out.println("Log likelihood is " + kal.getLogLikelihood());
    return kal.getSmoothedObservations();
  }
  private interface StateModel extends TorczonSimplex.Function
  {
    double[] smooth(double[] v) throws InputException;
  }
  /** Use this class to fit model with randomly varying velocity */
  private static class RandomVelocity implements StateModel
  {
    private Kalman kal = new Kalman();
    private double[] input;
    /** if true, score by - mean square error not log likelihood */
    private boolean squareScore;
    RandomVelocity(double velocityUncertainty,
      double positionUncertainty, double initialPosition,
      double initialVelocity, double[] y, boolean scoreSquare)
    {
      squareScore = scoreSquare;
      if (scoreSquare)
      {
	input = (double[])y.clone();
      }
      kal.setY(y);
      // 2-d vector holds velocity in 0 and position in 1
      kal.setOutputVec(new double[] {0.0, 1.0});
      kal.setTransitionMatrix(new double[] {1, 0, 1, 1});
      kal.setInitialState(new double[] {initialVelocity,
        initialPosition});
      System.err.println("Velocity uncertainty is " +
        velocityUncertainty);
      kal.setInitialVariance(new double[] {velocityUncertainty, 0, 0,
        positionUncertainty});
    }
    /** two parameters are log output variance and log velocity
     *  variance but don't accept very small variances
     */
    private void setParams(double[] v)
    {
      double v0 = Math.exp(v[0]) + 1.0e-10;
      kal.setVariance(v0);
      double v1 = Math.exp(v[1]) + 1.0e-10;
      kal.setTransitionNoise(new double[] {v1, 0});
    }
    /** two parameters are log output variance and log velocity
     *  variance but don't accept very small variances
     */
    public double f(double[] v)
    {
      setParams(v);
      double result;
      if (squareScore)
      {
	try
	{
	  kal.fit(true);
	}
	catch (InputException ie)
	{
	  throw new IllegalStateException("got input exception " + ie);
	}
	// want to minimise squared error
	double[] smoothed = kal.getSmoothedObservations();
	result = 0.0;
	for (int i = 0; i < input.length; i++)
	{
	  double here = input[i];
	  if (Double.isNaN(here))
	  {
	    continue;
	  }
	  double diff = here - smoothed[i];
	  result += diff * diff;
	}
	/*
	if (result < 10)
	{
	  for (int i = 0; i < 100; i++)
	  {
	    System.out.println(" I " + i + " data " + input[i] +
	      " smoothed " + smoothed[i]);
	  }
	}
	*/
      }
      else
      {
	try
	{
	  kal.fit(false);
	}
	catch (InputException ie)
	{
	  throw new IllegalStateException("got input exception " + ie);
	}
	// want to minimise - log likelihood
	result = -kal.getLogLikelihood();
      }
      /*
      System.out.println("Computed " + result + " from " + v[0] + ", " +
        v[1]);
      */
      return result;
    }
    /** return smoothed result */
    public double[] smooth(double[] v) throws InputException
    {
      setParams(v);
      kal.fit(true);
      return kal.getSmoothedObservations();
    }
  }
  /** Use this class to fit model with randomly varying position */
  private static class RandomPosition implements StateModel
  {
    private Kalman kal = new Kalman();
    /** if true, score by - mean square error not log likelihood */
    RandomPosition(double positionUncertainty, double initialPosition,
      double[] y)
    {
      kal.setY(y);
      // 2-d vector holds position in 0 and position output in 1
      kal.setOutputVec(new double[] {1.0});
      kal.setTransitionMatrix(new double[] {1.0});
      kal.setInitialState(new double[] {initialPosition});
      kal.setInitialVariance(new double[] {positionUncertainty});
    }
    /** two parameters are log position variance and position output
     *  variance but don't accept very small variances
     */
    private void setParams(double[] v)
    {
      double v0 = Math.exp(v[1]) + 1.0e-10;
      kal.setVariance(v0);
      double v1 = Math.exp(v[0]) + 1.0e-10;
      kal.setTransitionNoise(new double[] {v1});
    }
    /** two parameters are log output variance and log velocity
     *  variance but don't accept very small variances
     */
    public double f(double[] v)
    {
      setParams(v);
      double result;
      try
      {
	kal.fit(false);
      }
      catch (InputException ie)
      {
	throw new IllegalStateException("got input exception " + ie);
      }
      // want to minimise - log likelihood
      result = -kal.getLogLikelihood();
      /*
      System.out.println("Computed " + result + " from " + v[0] + ", " +
        v[1]);
      */
      return result;
    }
    /** return smoothed result */
    public double[] smooth(double[] v) throws InputException
    {
      setParams(v);
      kal.fit(true);
      return kal.getSmoothedObservations();
    }
  }
  /** Smooth by fitting Kalman model with varying velocity */
  public static double[] varyingVelocity(double[] y, int goes,
    long seed, double ftol, double[] ll, boolean useSquares,
    boolean usePosition)
    throws InputException
  {
    if (y.length < 3)
    {
      throw new IllegalArgumentException("Length must be at least 3");
    }
    // Work out mean increment to start off with
    Deviant d = new Deviant();
    double start = y[0];
    for (int i = 1; i < y.length; i++)
    {
      if (Double.isNaN(start))
      {
        start = y[i];
      }
      double diff = y[i] - y[i - 1];
      if (!Double.isNaN(diff))
      {
	d.sample(diff);
      }
    }
    double variance = d.getVariance() * 10.0;
    if (variance <= 0.0)
    {
      variance = 1.0;
    }
    StateModel rv;
    if (usePosition)
    {
      rv = new RandomVelocity(variance, variance,
	start, d.getMean(), y, useSquares);
    }
    else
    {
      rv = new RandomPosition(variance, start, y);
    }
    double sd = Math.sqrt(variance);
    double bestScore = Double.MAX_VALUE;
    double[] bestIs = new double[2];
    Random r = new Random(seed);
    for (int go = 0; go < goes; go++)
    {
      double[][] startFrom = new double[3][];
      for (int i = 0; i < 3; i++)
      {
        startFrom[i] = new double[] { Math.log(sd) + r.nextGaussian(),
	  Math.log(sd) + r.nextGaussian()};
      }
      double score = TorczonSimplex.min(startFrom, rv, ftol);
      System.err.println("Score " + score + " at " +
        startFrom[0][0] + " " + startFrom[0][1]);
      if (score < bestScore)
      {
        bestScore = score;
	bestIs = startFrom[0];
      }
    }
    ll[0] = bestScore;
    return rv.smooth(bestIs);
  }
}
