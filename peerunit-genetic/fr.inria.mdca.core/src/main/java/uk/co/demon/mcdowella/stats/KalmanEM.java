package uk.co.demon.mcdowella.stats;

import java.util.Arrays;

/** This class uses a Kalman filter to work out the
 *  probability of a series of observations from a one-dimensional
 *  regularly spaced time series, given the probability distribution
 *  for the state of the hidden variables at the beginning of the 
 *  series. It can also provide smoothed estimates of the hidden
 *  variables, hillclimb on the parameters using the EM algorithm,
 *  and so on.
 */
public class KalmanEM
{
  // Mathematical preliminary. To try and keep the calculations
  // manageable, we represent the distribution on the vector of hidden
  // variables at each point by a mean and variance-covariance matrix
  // for a normal distribution.
  // If we have an observation at a given state x we see
  // x = a'v where x is scalar and a and v are vectors. Clearly
  // the distribution of x has mean a'v and variance a'Va, so we can
  // use this to make a contribution to the observed log-likelihood.
  //
  // Moving forwards, we start with a given probability distribution
  // on the hidden states v and the fixed transition matrix M 
  // transforms this as v' = Mv. Our estimate of the probability of
  // v' then has mean Mv and variance-convariance MVM' where v and
  // V then represent the original mean and variance-covariance 
  // matrices.
  // Addition of noise with 0 mean and variance-covariance matrix
  // W simply changes V' = V + W, since the noise is taken to be
  // independent of the underlying state.
  //
  // I tend to assume that X has mean 0 in the following. Typically
  // it won't, but X2 = X - E(X) does which means that we are really
  // talking about a correction term we will later add to the true X.
  // In general when we observe Y the posterior mean for X given Y
  // (assuming that (X, Y) is multinormal) is the best linear function
  // of Y approximating X. Consider the linear function
  // L = Cov(X,Y)Var(Y)^-1 + Q. The variance of (X - LY) turns out
  // (after cancellation) to be
  // Var(X) - Cov(X,Y)(Var(Y)^-1)Cov(X,Y)' + QVar(Y)Q'
  // so we want Q = 0 and our posterior mean is Cov(X,Y)(Var(Y)^-1)Y
  // If we call this Z, then we find that 
  // Cov(X,Z) = Cov(X,Y)(Var(Y)^-1)Cov(X,Y)' = Var(Z) which actually
  // makes sense because it means that the best linear predictor of X
  // given Z is Z itself.
  //
  // Note that the mean predicted state of the system at any later time,
  // given only the mean state at a start time, is a linear function
  // of the start state. The variance-covariance matrix at later times
  // is independent of the observations and of the start state. This
  // means that we can work from end to begining and compute a
  // quadratic function for each time which allows us to work out
  // the contribution from subsequent time steps to the likelihood
  // of the observations, given the state at that time. We will use
  // this for smoothing and in the EM step

  /** dimension of state space */
  private final int stateDim;
  /** State transition matrix. This is constant and written with
   *  rows contiguous
   */
  private final double[] stateMatrix;
  /** variance of noise contribution to each state */
  private final double[] noise;
  /** if non-null, getLogLikelihood() stores variance-covariance state 
   *  matrix after taking into account any observation here
   */
  private double[][] stateVCHistory;
  /** if non-null, getLogLikelihood() stores mean state vector after 
   *  taking into account any observation here observation here.
   */
  private double[][] stateMeanHistory;
  /** dot product with this to produce predicted observation */
  private final double[] outputVec;
  /** observations - NaN marks missing observations */
  private final double[] obs;
  /** initial state */
  private final double[] initialState;
  /** initial variance-covariance matrix */
  private final double[] initialVC;
  /** Create from args
   *  @param forStateMatrix hidden state transition matrix
   *  @param forNoise variance of noise applied after state transition
   *  @param forOutputVec dot product state with this to produce
   *  predicted output
   *  @param forObs observations, with Double.NaN meaning unknown
   *  @param forInitial estimate of initial state as at just before
   *  production of first observation
   *  @param forInitialVC initial variance-covariance estimate
   *  @param wantReest set true if we want to do re-estimates
   */
  public KalmanEM(double[] forStateMatrix, 
    double[] forNoise,
    double[] forOutputVec, double[] forObs, double[] forInitial,
    double[] forInitialVC, boolean wantReest)
  {
    stateDim = forNoise.length;
    int s2 = stateDim * stateDim;
    if ((forStateMatrix.length != s2) || 
        (forOutputVec.length != stateDim) ||
        (forInitial.length != stateDim) || (forInitialVC.length != s2))
    {
      throw new IllegalArgumentException("Dimension mismatch");
    }
    stateMatrix = (double[])forStateMatrix.clone();
    noise = (double[])forNoise.clone();
    for (int i = 0; i < noise.length; i++)
    {
      if (noise[i] < 0.0)
      {
        throw new IllegalArgumentException("-ve noise variance");
      }
    }
    outputVec = (double[])forOutputVec.clone();
    obs = (double[])forObs.clone();
    initialState = (double[])forInitial.clone();
    initialVC = (double[])forInitialVC.clone();
    // Check that variance matrix is symmetric and has at least
    // diagonal >= 0
    for (int i = 0; i < stateDim; i++)
    {
      for (int j = 0; j < i; j++)
      {
        if (initialVC[i * stateDim + j] != initialVC[j * stateDim + i])
	{
	  throw new IllegalArgumentException(
	    "Variance-Covariance not symmetric");
	}
      }
      if (initialVC[i * stateDim + i] < 0.0)
      {
        throw new IllegalArgumentException(
	  "Variance-Covariance has -ve diagonal element");
      }
    }
    if (wantReest)
    {
      stateVCHistory = new double[obs.length][];
      stateMeanHistory = new double[obs.length][];
      for (int i = 0; i < obs.length; i++)
      {
        stateVCHistory[i] = new double[initialVC.length];
	stateMeanHistory[i] = new double[initialState.length];
      }
    }
  }
  /** Compute the log likelihood
   *  of the observations given the current parameters
   */
  public double getLogLikelihood()
  {
    double[] currentState = (double[])initialState.clone();
    double[] currentVC = (double[])initialVC.clone();
    double[] covxy = new double[currentState.length];
    double[] newState = new double[currentState.length];
    double[] forPrePost = new double[currentVC.length];
    double currentSum = 0.0;
    int obsUsed = 0;
    final int skip = initialState.length + 1;
    for (int i = 0; i < obs.length; i++)
    {
      double here = obs[i];
      if (here != Double.NaN)
      { // predict mean and variance of observation to get
        // its contribution to log-likelihood
	double mean = dotProduct(currentState, outputVec);
	double variance = vmv(currentVC, outputVec);
	if (variance < 0.0)
	{
	  throw new IllegalStateException("-ve Prediction variance");
	}
	if (variance == 0.0)
	{
	  if (here != mean)
	  {
	    throw new IllegalStateException("zero prediction variance");
	  }
	}
	else
	{
	  // Here we apply the equation for E(X|Y), where Y is the
	  // observed-predicted value. Var(Y) depends only on the
	  // variance of X and the output vector: it is
	  // outputVec' * currentVC * outputVec
	  // Cov(X, Y) = E(XY') = E(XX'L) = currentVC * outputVec
	  obsUsed++;
	  currentSum -= Math.log(variance) * 0.5;
	  double diff = here - mean;
	  currentSum -= diff * diff / (2.0 * variance);
          // work out Cov(X, Y) = VL = V'L
	  multiplyByTranspose(outputVec.length, outputVec,
	    currentVC, covxy);
	  // update mean by adding in Cov(X,Y)Var(Y)^-1 Y
	  for (int j = 0; j < stateDim; j++)
	  {
	    currentState[j] += covxy[j] * diff / variance;
	  }
	  // update variance-covariance matrix by subtracting
	  // Cov(X, Y)Var(Y)^-1Cov(X, Y)'
	  subScaledOuterProduct(stateDim, covxy, covxy, 1.0 / variance,
	    currentVC);
	}
      }
      // Record info as known after taking into account current 
      // observation
      if (stateVCHistory != null)
      {
        System.arraycopy(currentVC, 0, stateVCHistory[i], 0,
	  currentVC.length);
      }
      if (stateMeanHistory != null)
      {
        System.arraycopy(currentState, 0, stateMeanHistory[i], 0,
	  currentState.length);
      }
      // Work out mean state after transition matrix
      matrixVectorMult(stateMatrix, currentState, newState);
      System.arraycopy(newState, 0, currentState, 0,
        currentState.length);
      // Work out variance-covariance matrix first after transition
      prePostMat(stateDim, stateMatrix, currentVC, forPrePost);
      // then add in noise
      int wp = 0;
      for (int j = 0; j < noise.length; j++)
      {
        currentVC[wp] += noise[j];
	wp += skip;
      }
    }
    return currentSum - obsUsed * Math.log(2.0 * Math.PI) * 0.5;
  }
  /** Do a reestimate as a backwards pass following the calculation
   *  of the log-likelihood, which also sets up the history vectors
   *  storing the information in the forwards pass
   */
  private void reestimateBackPass()
  {
    // In the main loop we will want to work out the relative 
    // log-likelihoods of possible states at (obsTime-1, obsTime).
    // We can refer to e.g. stateVCHistory[obsTime-1] to get a 
    // contribution that considers all observations up to and including
    // obsTime-1. We want here to keep track of the contributions from 
    // obsTime on,

    // This can be used to calculate the contribution to the
    // state-dependent form of the log likelihood. Given x the 
    // contribution here is x' stateProduct x. This matrix is always
    // symmetric.
    double[] stateProduct = new double[stateMatrix.length];
    // This can be used to calculate the contribution to the 
    // state-dependent term of the log-likelihood. Given x, the 
    // contribution here is x' stateVector
    double[] stateVector = new double[initialState.length];

    double[] ammendedT = new double[stateMatrix.length];
    double[] covxy = new double[stateVector.length];
    double[] smat = new double[stateProduct.length];
    double[] linearTerm = new double[stateProduct.length];
    double[] forPrePost = new double[stateProduct.length];
    double[] ms = new double[stateProduct.length];
    double[] vcBeforeObs = new double[stateProduct.length];

    // default 0 values corresponding to final observation are
    // already correct, because there are no observations after that

    // used for indexing
    final int skip = initialState.length + 1;
    for (int obsTime = obs.length - 1; obsTime > 0; obsTime--)
    {
      // Here to consider state at times (obsTime-1, obsTime)

      if (obs[obsTime] == Double.NaN)
      { // no observation so will just translate state by T
	// Need to translate contribution in terms of next state
	// vector: x'Px and x'v when x = Ty become
	// y'T'PTy and y'T'v
	prePostMat(initialState.length, stateMatrix,
	  stateProduct, forPrePost);
	System.arraycopy(stateVector, 0, linearTerm, 0,
	  stateVector.length);
	multiplyByTranspose(stateVector.length, linearTerm, stateMatrix,
	  stateVector);
      }
      else
      {
	// Here if observation. State transformation is different
	// and we get a contribution in as well

	// We use the current observation to produce
	// x' = x + CV^-1(obs - L'x)
	// so x is transformed not by x2 = Tx but by 
	// x2 = T(I - CV^-1L')x + TCV^-1obs
	// We keep track of contribution as quadratic x2'Mx2 + x2'b and
	// when we transform using x2 = Sx + c we get
	// x'S'MSx + x'(S'b) + 2x'S'Mc + constant we don't care about

	// need to work out variance-convariance matrix before
	// observation at obstTime from matrix at previous step
	System.arraycopy(stateVCHistory[obsTime - 1], 0,
	  vcBeforeObs, 0, stateProduct.length);
	// Work out variance-covariance matrix first after transition
	prePostMat(stateDim, stateMatrix, vcBeforeObs, forPrePost);
	// then add in noise
	int wp = 0;
	for (int j = 0; j < noise.length; j++)
	{
	  vcBeforeObs[wp] += noise[j];
	  wp += skip;
	}
	double variance = vmv(vcBeforeObs, outputVec);
	if (variance > 0)
	{
	  // work out Cov(X,Y) = currentVC * outputVec
	  matrixVectorMult(vcBeforeObs, outputVec, covxy);
	  // will form S = I - CV^-1L'
	  Arrays.fill(smat, 0.0);
	  for (int i = 0; i < initialState.length; i++)
	  {
	    smat[i * skip] = 1.0;
	  }
	  // This produces I - CV^-1L'
	  subScaledOuterProduct(initialState.length, covxy,
	    outputVec, 1.0 / variance, smat);
	  // now form T times this into S=ammendedT
	  matrixMultiply(initialState.length, stateMatrix,
	    smat, ammendedT);
	  // multiplying covxy by 2obs/variance gives us
	  // twice our c, except for multiplication by T
	  double scale = 2.0 * obs[obsTime] / variance;
	  for (int j = 0; j < covxy.length; j++)
	  {
	    covxy[j] *= scale;
	  }
	  matrixVectorMult(stateMatrix, covxy, linearTerm);
	  System.arraycopy(linearTerm, 0, covxy, 0, stateVector.length);
	  // covxy now holds our c
	  // create MS
	  matrixMultiply(initialState.length, ammendedT,
	    stateProduct, ms);
	  // work out 2c'(MS) = 2(S'M'c)' and note that M is symmetric
	  // so M=M' and we get our required 2S'Mc into linearTerm
	  multiplyByTranspose(stateVector.length, stateVector, ms,
	    linearTerm);
	  // Use ammendedT to put (S'b)' into stateVector
	  multiplyByTranspose(stateVector.length, stateVector,
	    ammendedT, stateVector);
	  // now add on other linear term
	  for (int j = 0; j < initialState.length; j++)
	  {
	    stateVector[j] += linearTerm[j];
	  }

	  prePostMat(initialState.length, ammendedT,
	    stateProduct, forPrePost);

	  // Need to account for contribution to log-likelihood from
	  // observation at obsTime
	  // term subtracted is (L'x - obs)'(L'x - obs) / variance
	  // which is xLL'x/v - 2x'Lobs/v + stuff we don't care about
	  // here
	  double ss = obs[obsTime] / variance;
	  for (int i = 0; i < stateVector.length; i++)
	  {
	    stateVector[i] += 2.0 * outputVec[i] * ss;
	  }
	  subScaledOuterProduct(stateVector.length, outputVec,
	    outputVec, 1.0 / variance, stateProduct);
	}
      }

      // Here with stateProduct and stateVector set up to reflect 
      // observations from obstTime on
    }

  }
  /** work out mat' * vin and place in vout */
  public static void multiplyByTranspose(int dim, double[] vin, double[] mat,
    double[] vout)
  {
    for (int i = 0; i < dim; i++)
    {
      double sum = 0.0;
      for (int j = 0; j < dim; j++)
      {
        sum += mat[j * dim];
      }
      vout[i] = sum;
    }
  }
  /** form MV - matrix vector product
   */
  public static void matrixVectorMult(double[] matrix, 
    double[] inVec, double[] target)
  {
    int matStart = 0;
    for (int i = 0; i < inVec.length; i++)
    {
      double sum = 0.0;
      for (int j = 0; j < inVec.length; j++)
      {
        sum += matrix[matStart++] * inVec[j];
      }
      target[i] += sum;
    }
  }
  /** matrix multiply */
  public static void matrixMultiply(int dim, double[] a, double[] b,
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
  /** compute dot product of two vectors */
  public static double dotProduct(double[] va, double[] vb)
  {
    double result = 0.0;
    for (int i = 0; i < va.length; i++)
    {
      result += va[i] * vb[i];
    }
    return result;
  }
  /** compute v'mv given matrix m and vector v */
  public static double vmv(double[] m, double[] v)
  {
    double result = 0.0;
    int readPos = 0;
    for (int i = 0; i < v.length; i++)
    {
      double sumHere = 0.0;
      for (int j = 0; j < v.length; j++)
      {
        sumHere += m[readPos++] * v[j];
      }
      result += sumHere * v[i];
    }
    return result;
  }
  /** substract a scaled outer product of two vectors: v1v2' */
  public static void subScaledOuterProduct(int dim, double[] v1,
    double[] v2, double scale, double[] target)
  {
    int wp = 0;
    for (int i = 0; i < dim; i++)
    {
      double vi = v1[i] * scale;
      for (int j = 0; j < dim; j++)
      {
        target[wp++] -= vi * v2[j];
      }
    }
  }
  /** replace V with MVM' */
  public static void prePostMat(int dim, double[] m, double[] v,
    double[] temp)
  {
    // work out MV
    int ms = 0;
    for (int i = 0; i < dim; i++)
    {
      for (int j = 0; j < dim; j++)
      {
        double sum = 0.0;
	for (int k = 0; j < dim; k++)
	{
	  sum += m[ms + k] * v[k * dim + j];
	}
	temp[ms + j] = sum;
      }
      ms += dim;
    }
    // work out (MV)M'
    ms = 0;
    for (int i = 0; i < dim; i++)
    {
      for (int j = 0; j < dim; j++)
      {
        double sum = 0.0;
	int msj = j * dim;
	for (int k = 0; k < dim; k++)
	{
	  sum += temp[ms + k] * m[msj + k];
	}
	v[ms + j] = sum;
      }
      ms += dim;
    }
  }
}
