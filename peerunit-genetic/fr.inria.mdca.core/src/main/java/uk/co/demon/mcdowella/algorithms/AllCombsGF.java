package uk.co.demon.mcdowella.algorithms;
import java.util.Random;
/** 
 *  This is an idea that turns out not to work. It would be a
 *  good way of creating orthogonal arrays with large numbers of
 *  columns if it turned out that the resulting arrays had good
 *  symmettry and collapsed down to produce designs with few rows.
 *  They don't.
 *  Input to this class is a number of variables, a number
 *  of options per variable, and a strength. It will produce a set
 *  of tests, such that each combination of options for variables
 *  naming at most strength variables occurs in at least one test.
 *  <br>
 *  It does this by working over GF(p^n), where p^n &ge; max(
 *  options per variable, columns). Each column is assigned a number
 *  in GF(p^n). Each test corresponds to the result of evaluating
 *  a polynomial at each of the numbers given to each column. If we
 *  consider all polynomials of degree less than strength, then all
 *  combinations of that strength or less variables will be covered,
 *  because you can interpolate at the given points to find the
 *  polynomial. It is possible that all combinations
 *  will be covered many times, especially if the number of options
 *  fits into p^m where m &lt n, so that we only really need to pay
 *  attention to m of the n coefficients when assigning an option.
 *  To check for this we consider each
 *  polynomial in GF(p^n) as a linear map from the coefficients of
 *  the polynomial, treated as concatenated n-long vectors mod p, to the
 *  vector consisting of the test options. We will check and see if
 *  knowledge of the test options uniquely identifies the polynomial
 *  used to produce them. This amounts to checking that a system of
 *  equations mod p is of full rank. If it is not, we can take this
 *  into account when we generate the answer.
 *  <br>
 *  There are some random choices we make here, so allow a random
 *  seed and a number of tries.
 *  <br>
 *  The prime used is chosen based on the number of options, and
 *  a power of this may then be used to fit the number of columns.
 *  It is just possible that you might get a better solution by
 *  increasing the number of options to force it to try different
 *  primes and prime powers.
 */
public class AllCombsGF
{
  /** Number of options for each variable */
  private int numOptions;
  /** number of variables **/
  private int numVars;
  /** Strength - maximum number of variables to consider together */
  private int strength;
  /** prime used for GF and modular arithmetic */
  private int prime;
  /** return the prime used */
  public int getPrime()
  {
    return prime;
  }
  /** full power of prime needed to handle number of columns and
   *  options - work GF(p^fullPower)
   */
  private int fullPower;
  public int getFullPower()
  {
    return fullPower;
  }
  /** power of prime needed to produce all possible options */
  private int optionPower;
  public int getOptionPower()
  {
    return optionPower;
  }
  /** value of prime ^ optionPower */
  private int primeToOption;
  public AllCombsGF(int options, int variables, int forStrength)
  {
    if ((options < 0) || (variables < 0) || (forStrength < 0))
    {
      throw new IllegalArgumentException("-ve parameters");
    }
    numOptions = options;
    numVars = variables;
    strength = forStrength;
    if (strength > numVars)
    {
      strength = numVars;
    }
    if (options <= 1)
    {
      return;
    }
    // Chose prime and prime power
    for (int possible = numOptions;; possible++)
    {
      if (checkPrime(possible))
      {
        break;
      }
    }
    // work out inverse
    inverse = new int[prime];
    int p2 = prime - 2;
    for (int i = 1; i < prime; i++)
    { // inverse of x is x^(p-2) mod p
      // and we work this out by computing and multiplying
      // together x, x^2, x^4, x^8...
      int sofar = 1;
      int powerHere = 1;
      // i ^ powerHere
      int powered = i;
      while (powerHere <= p2)
      {
        if ((powerHere & p2) != 0)
	{
	  sofar = (sofar * powered) % prime;
	}
	powerHere += powerHere;
	powered = (powered * powered) % prime;
      }
      /*
      System.err.println("Inverse of " + i + " mod " + prime +
        " is " +  sofar);
      */
      inverse[i] = sofar;
    }
    // Work out power we need to cater for the number of variables
    fullPower = optionPower;
    int full = primeToOption;
    while (full < numVars)
    {
      full *= prime;
      fullPower++;
    }
    // Working in GF(prime^fullPower = full) here so we get
    // enough columns, as well as enough options.
    // Need to do arithmetic over a Galois field. For the most
    // part we can think of this as working with polynomials mod
    // some polynomial, where the other polynomial happens to
    // be chosen such that this is a field, not a ring (that is,
    // so that every non-zero element has an inverse). Galois theory
    // is only necessary to guarantee that such a polynomial exists,
    // and that one exists such that 1, x, x^2, x^3... produces
    // all non-zero polynomials. We search for one such here, of the
    // form x^fullPower + ax^n + bx^(n-1) + ... = 0;
    int[] magicPoly = new int[fullPower];
    int[] trial = new int[fullPower];
    int forcePositive = prime * prime;
    int exhaust = 1;
    exhaustLoop: for (; exhaust < full; exhaust++)
    {
      int val = exhaust;
      for (int i = 0; i < fullPower; i++)
      {
	magicPoly[i] = val % prime;
	val = val / prime;
      }
      // start off with 1
      for (int i = 1; i < trial.length; i++)
      {
	trial[i] = 0;
      }
      trial[0] = 1;
      iLoop:for (int i = 0;;i++)
      {
	// multiply by x.
	int top = trial[trial.length - 1];
	for (int j = trial.length - 1; j > 0; j--)
	{
	  trial[j] = trial[j - 1];
	}
	trial[0] = 0;
	// top.x^power =  - top * magicPoly
	for (int j = 0; j < trial.length; j++)
	{
	  int coeff = trial[j] - top * magicPoly[j] + forcePositive;
	  trial[j] = coeff % prime;
	}
	if (i == (full - 2))
	{ // should have cycled round to 1 again
	  for (int j = 1; j < trial.length; j++)
	  {
	    if (trial[j] != 0)
	    {
	      continue exhaustLoop;
	    }
	  }
	  if (trial[0] == 1)
	  { // success!
	    break exhaustLoop;
	  }
	  // failure
	  continue exhaustLoop;
	}
	// Check for early failure cases: 1 or 0
	for (int j = 1; j < trial.length; j++)
	{
	  if (trial[j] != 0)
	  {
	    continue iLoop;
	  }
	}
	if ((trial[0] == 1) || (trial[0] == 0))
	{ // early failure
	  continue exhaustLoop;
	}
      }
    }
    if (exhaust == full)
    {
      throw new IllegalStateException(
	"Could not find good polynomial");
    }
    /*
    System.err.print("Coefficients are:1");
    for (int i = 0; i < magicPoly.length; i++)
    {
      System.err.print(", " + magicPoly[i]);
    }
    System.err.println();
    */
    // We will build strength matrices for each column. Each matrix
    // is multiplication by a constant in GF[prime^fullPower]
    // represented as a matrix giving a linear transform for a
    // vector of fullPower numbers mod prime.
    int perColumn = fullPower * fullPower * strength;
    // Everything starts off 0, and some of it stays zero.
    coefficients = new int[full * perColumn];
    // The first coefficient for every column is 1, and this
    // is just the identity matrix
    for (int i = 0; i < fullPower; i++)
    {
      int offset = i * (fullPower + 1);
      for (int j = 0; j < full; j++)
      {
        coefficients[perColumn * j + offset] = 1;
      }
    }
    // fill in the second coefficient: matrix m does multiplication
    // by m
    for (int i = 0; i < full; i++)
    {
      // split out column number
      int num = i;
      for (int j = 0; j < fullPower; j++)
      {
        trial[j] = num % prime;
	num = num / prime;
      }
      int target = i * perColumn + fullPower * fullPower;
      for (int j = 0; j < fullPower; j++)
      { // Set current column of matrix to trial
        // So we start by putting in the contribution to the
	// final result of the constant coefficient
        for (int k = 0; k < fullPower; k++)
	{
	  coefficients[target + k * fullPower] = trial[k];
	}
	// multiply trial by x
	int top = trial[trial.length - 1];
	for (int k = trial.length - 1; k > 0; k--)
	{
	  trial[k] = trial[k - 1];
	}
	trial[0] = 0;
	// top.x^power =  - top * magicPoly
	for (int k = 0; k < trial.length; k++)
	{
	  int coeff = trial[k] - top * magicPoly[k] + forcePositive;
	  trial[k] = coeff % prime;
	}
	target++;
      }
    }
    // Fill in the other coefficients using matrix multiplication
    for (int i = 2; i < strength; i++)
    {
      for (int j = 0; j < full; j++)
      {
        int xPosition = j * perColumn + fullPower * fullPower;
	int prevPosition = j * perColumn +
	  fullPower * fullPower * (i - 1);
	// Do matrix multiplication: Xij = AikBkj
        for (int k = 0; k < fullPower; k++)
	{
	  for (int l = 0; l < fullPower; l++)
	  {
	    int sum = 0;
	    for (int m = 0; m < fullPower; m++)
	    {
	      sum = (sum + coefficients[xPosition + k * fullPower + m] *
	        coefficients[prevPosition + m * fullPower + l]) %
		prime;
	    }
	    coefficients[prevPosition + fullPower * fullPower +
	      k * fullPower + l] = sum;
	  }
	}
      }
    }
    /*
    System.err.println("Will print out identity matrices");
    for (int i = 0; i < full; i++)
    {
      int startsAt = i * perColumn;
      System.err.println("Matrix for " + i);
      for (int j = 0; j < fullPower; j++)
      {
	String sp = "";
        for (int k = 0; k < fullPower; k++)
	{
	  System.err.print(sp + coefficients[startsAt + j * fullPower
	  + k]);
	  sp = " ";
	}
	System.err.println();
      }
    }
    */
    columnChoice = new int[full];
    cutdownMatrix = new int[fullPower * optionPower];
    triangularCutdown = new int[fullPower * optionPower];
    fellSwoop = new int[numVars * optionPower * fullPower *
      strength];
  }
  /** Used to pick column numbers */
  private int[] columnChoice;
  /** matrix mapping from fullPower to optionPower */
  private int[] cutdownMatrix;
  /** triangular version of cutDownMatrix */
  private int[] triangularCutdown;
  /** one fell swoop matrix gives you one whole column from
   *  some information identifyint the row
   */
  private int[] fellSwoop;
  /** number of tests in solution found */
  private int lastTests;
  /** return the number of tests in the last solution found */
  public int getLastTests()
  {
    return lastTests;
  }
  /** maximum number of tests we want to know about */
  private int maxTests = 100;
  public void setMaxTests(int max)
  {
    maxTests = max;
  }
  /** Generate a solution. Return it unless it is bigger than
   *  maxTests, in which case return null.
   */
  private int[][] generate(Random r)
  {
    if (numOptions < 0)
    {
      lastTests = 0;
      return new int[numVars][];
    }
    if (numVars < 1)
    {
      return new int[0][];
    }
    if (numOptions == 1)
    {
      return new int[][] {new int[numVars]};
    }
    // choose a random subset of columns
    for (int j = 0; j < columnChoice.length; j++)
    {
      columnChoice[j] = j;
    }
    for (int j = 0; j < numVars; j++)
    {
      int x = j + r.nextInt(columnChoice.length - j);
      int t = columnChoice[j];
      columnChoice[j] = columnChoice[x];
      columnChoice[x] = t;
    }
    // produce a random linear function from fullPower variables
    // to optionPower variables. It therefore has optionPower
    // rows and fullPower columns.
    for (int i = 0; i < optionPower; i++)
    {
      for (;;)
      {
        for (int j = 0; j < fullPower; j++)
	{
	  cutdownMatrix[i * fullPower + j] = r.nextInt(prime);
	}
	int rowsNow = i + 1;
	for (int j = 0; j < (fullPower * rowsNow); j++)
	{
	  triangularCutdown[j] = cutdownMatrix[j];
	}
	if (triangularise(rowsNow, fullPower,
	  triangularCutdown) == rowsNow)
	{ // matrix is of full rank
	  break;
	}
      }
    }
    /*
    System.err.println("Cutdown matrix:");
    for (int i = 0; i < optionPower; i++)
    {
      String sp = "";
      for (int j = 0; j < fullPower; j++)
      {
        System.err.print(sp + cutdownMatrix[i * fullPower + j]);
	sp = " ";
      }
      System.err.println();
    }
    */
    // Each row is generated by every combination of strength
    // coefficients from GF(p^fullPower), which we are now
    // representing as a vector of fullPower * strength integers.
    // To produce the value for one variable of one test we multiply
    // this by concatenated
    // fullPower x fullPower matrices [M1 M2 M3..] and then multiply
    // that by cutdown. This is the same as multiplying by
    // [CM1 CM2 CM3]. If we stack the result for different columns
    // on top of each other we get all columns of a single row out
    // in one fell swoop, with the optionPower numbers mod p that
    // make up a single option continguous:
    // [CM1 CM2 CM3]
    // [CN1 CN2 CN3]
    // ....
    // We are interested in how many truly different outputs there
    // are of this matrix, as we go through all possible input
    // vectors. In fact, it is easier if we build it in transposed
    // order, and think of vector * matrix, not matrix * vector,
    // as then all possible outputs corresponds to all possible
    // linear combinations of rows

    // first build it, in fellSwoop.
    // These cols and rows are for the transposed version
    // So we now have one row for each input variable used to
    // generate a set of columns
    int numRows = strength * fullPower;
    // and one column for each output variable from the generation
    // process
    int rawCols = numVars * optionPower;
    for (int i = 0; i < numVars; i++)
    {
      int realColumn = columnChoice[i];
      for (int j = 0; j < strength; j++)
      {
        int matStart = i * optionPower +
	  j * numVars * optionPower * fullPower;
	int coeffStart = realColumn * fullPower * fullPower *
	  strength + j * fullPower * fullPower;
	/*
	if (j == 0)
	{
	  System.err.println("Cutdown is ");
	  for (int k = 0; k < optionPower; k++)
	  {
	    String sp = "";
	    for (int l = 0; l < fullPower; l++)
	    {
	      System.err.print(sp +
	        cutdownMatrix[k * fullPower + l]);
	      sp = " ";
	    }
	    System.err.println();
	  }
	  System.err.println("Coeff sub-matrix is");
	  for (int k = 0; k < fullPower; k++)
	  {
	    String sp = "";
	    for (int l = 0; l < fullPower; l++)
	    {
	      System.err.print(sp +
	        coefficients[coeffStart + k * fullPower + l]);
	      sp = " ";
	    }
	    System.err.println();
	  }
	}
	*/
	// Xkl = Akm * Bml
	for (int k = 0; k < optionPower; k++)
	{
	  for (int l = 0; l < fullPower; l++)
	  {
	    int sum = 0;
	    for (int m = 0; m < fullPower; m++)
	    {
	      sum = (sum + cutdownMatrix[k * fullPower + m] *
	        coefficients[coeffStart + m * fullPower + l]) % prime;
	    }
	    // Xkl is addressed by k = option and l = input var
	    fellSwoop[matStart + l * rawCols + k] = sum;
	  }
	}
	/*
	if (j == 0)
	{
	  System.err.println("Product is ");
	  for (int k = 0; k < optionPower; k++)
	  {
	    String sp = "";
	    for (int l = 0; l < fullPower; l++)
	    {
	      System.err.print(sp +
	        fellSwoop[matStart + l * rawCols + k]);
	      sp = " ";
	    }
	    System.err.println();
	  }
	}
	*/
      }
    }
    /*
    System.err.println("Before triangularise");
    for (int i = 0; i < numRows; i++)
    {
      String sp = "";
      for (int j = 0; j < rawCols; j++)
      {
        System.err.print(sp + fellSwoop[i * rawCols + j]);
	sp = " ";
      }
      System.err.println();
    }
    */
    int rank = triangularise(numRows, rawCols, fellSwoop);
    /*
    System.err.println("Rank is " + rank);
    System.err.println("After triangularise");
    for (int i = 0; i < numRows; i++)
    {
      String sp = "";
      for (int j = 0; j < rawCols; j++)
      {
        System.err.print(sp + fellSwoop[i * rawCols + j]);
	sp = " ";
      }
      System.err.println();
    }
    */
    lastTests = 1;
    for (int i = 0; i < rank; i++)
    {
      lastTests *= prime;
    }
    if (lastTests > maxTests)
    {
      return null;
    }
    int[][] result = new int[lastTests][];
    for (int i = 0; i < lastTests; i++)
    {
      result[i] = new int[numVars];
    }
    int[] downColumn = new int[lastTests];
    for (int i = 0; i < numVars; i++)
    {
      for (int j = 0; j < optionPower; j++)
      {
	// Work out every possibility by starting with the
	// possibilities for 0 input variables and successively
	// multiplying number of possibilities by prime
        downColumn[0] = 0;
	int numPoss = 1;
	int wp = 1;
	for (int k = 0; k < rank; k++)
	{
	  int addIn = fellSwoop[k * rawCols + i * optionPower + j];
	  for (int l = 1; l < prime; l++)
	  {
	    for (int m = 0; m < numPoss; m++)
	    {
	      int valueHere = (downColumn[m] + l * addIn) %
		prime;
	      downColumn[wp] = valueHere;
	      result[wp][i] = result[wp][i] * prime + valueHere;
	      wp++;
	    }
	  }
	  numPoss *= prime;
	}
      }
    }
    for (int i = 0; i < lastTests; i++)
    {
      int[] row = result[i];
      for (int j = 0; j < numVars; j++)
      {
        if (row[j] >= numOptions)
	{
	  row[j] = r.nextInt(numOptions);
	}
      }
    }
    return result;
  }
  // Coefficients for each column, as a sequence of matrices, with
  // the first matrix being the identity matrix representing i,
  // the second matrix representing multiplication by c, where c is
  // the column's position as number in GF(p^fullPower), the next
  // representing multiplication by c^2, and so on
  private int[] coefficients;
  /** Check to see if possible prime is prime or prime power. Slow
   *  but this is probably the least of our problems
   */
  boolean checkPrime(int possiblePrime)
  {
    for (int i = 2;;)
    {
      if ((i * i) > possiblePrime)
      { // prime found
        prime = possiblePrime;
        optionPower = 1;
	primeToOption = prime;
	return true;
      }
      int div = possiblePrime / i;
      int rem = possiblePrime - i * div;
      if (rem == 0)
      {
        // divisible by i: is it a prime power?
	// we know i is <= sqrt(possible), so possible / i > i
	// and if it is a prime power this is also divisible by
	// i
        if (((div % i) == 0) && checkPrime(div))
	{
	  while (primeToOption < possiblePrime)
	  {
	    optionPower++;
	    primeToOption *= prime;
	  }
	  return true;
	}
	return false;
      }
      // i is not a factor, so increase it
      if (i <= 2)
      {
	i++;
      }
      else
      {
	i += 2;
      }
    }
  }
  /** Method to triangularise a matrix in place, with pivoting.
   *  Returns rank
   */
  int triangularise(int numRows, int numCols, int[] matrix)
  {
    int column = 0;
    int rankSofar = 0;
    int forcePositive = prime * prime;
    for (int i = 0; i < numRows; i++)
    {
      // Find position we can use to pivot on
      int row = -1;
      searchLoop:for (;column < numCols; column++)
      {
        for (int j = i; j < numRows; j++)
	{
	  if (matrix[numCols * j + column] != 0)
	  {
	    row = j;
	    break searchLoop;
	  }
	}
      }
      if (column >= numCols)
      {
        return rankSofar;
      }
      rankSofar++;
      // Swap up pivot row
      for (int j = column; j < numCols; j++)
      {
        int t = matrix[row * numCols + j];
	matrix[row * numCols + j] = matrix[i * numCols + j];
	matrix[i * numCols + j] = t;
      }
      // Multiply by inverse of leading term
      int times = inverse[matrix[i * numCols + column]];
      for (int j = column; j < numCols; j++)
      {
        matrix[i * numCols + j] = (matrix[i * numCols + j] * times) %
	  prime;
      }
      if (matrix[i * numCols + column] != 1)
      {
        throw new IllegalStateException("Big trouble in triangularise");
      }
      // and subtract from everything else
      for (int j = i + 1; j < numRows; j++)
      {
        times = matrix[j * numCols + column];
	for (int k = column; k < numCols; k++)
	{
	  matrix[j * numCols + k] = (matrix[j * numCols + k] -
	    times * matrix[i * numCols + k] + forcePositive) % prime;
	}
      }
      // Have used up this column
      column++;
      if (column >= numCols)
      {
        return rankSofar;
      }
    }
    return rankSofar;
  }
  /** inverse of each number mod the prime */
  private int[] inverse;
  public static void main(String[] s)
  {
    int goes = 100;
    int numOptions = 2;
    int numVars = 64;
    long seed = 42;
    int strength = 2;
    int argp = 0;
    int max = 100;
    boolean trouble = false;
    int s1 = s.length - 1;
    try
    {
      for (argp = 0; argp < s.length; argp++)
      {
	if ((argp < s1) && "-goes".equals(s[argp]))
	{
	  goes = Integer.parseInt(s[++argp].trim());
	}
	else if ((argp < s1) && "-max".equals(s[argp]))
	{
	  max = Integer.parseInt(s[++argp].trim());
	}
	else if ((argp < s1) && "-options".equals(s[argp]))
	{
	  numOptions = Integer.parseInt(s[++argp].trim());
	}
	else if ((argp < s1) && "-seed".equals(s[argp]))
	{
	  seed = Long.parseLong(s[++argp].trim());
	}
	else if ((argp < s1) && "-strength".equals(s[argp]))
	{
	  strength = Integer.parseInt(s[++argp].trim());
	}
	else if ((argp < s1) && "-vars".equals(s[argp]))
	{
	  numVars = Integer.parseInt(s[++argp].trim());
	}
	else
	{
	  System.err.println("Could not handle flag " + s[argp]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Could not read number in " + s[argp]);
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are [-goes #] [-max #] [-options #] " +
        "[-seed #] [-strength #] [-vars #]");
      return;
    }
    System.out.println("Options " + numOptions + " strength " +
      strength + " variables " + numVars);
    System.err.println("Seed " + seed + " goes " + goes + " max " +
      max);
    AllCombsGF acg = new AllCombsGF(numOptions, numVars, strength);
    acg.setMaxTests(max);
    System.out.println("Using prime " + acg.getPrime() +
      " Option power " + acg.getOptionPower() + " full power " +
      acg.getFullPower());
    Random r = new Random(seed);
    int bestSofar = Integer.MAX_VALUE;
    int[][] best = null;
    int nextProgress = 1;
    for (int i = 0;; i++)
    {
      if ((goes > 0) && (i >= goes))
      {
        break;
      }
      int[][] result = acg.generate(r);
      if (i >= nextProgress)
      {
        System.err.println("Just done test " + (i + 1));
	nextProgress += nextProgress;
      }
      int tests = acg.getLastTests();
      if (tests < bestSofar)
      {
	bestSofar = tests;
	best = result;
	System.out.println("Got result len " + tests);
	if (result != null)
	{
	  for (int j = 0; j < tests; j++)
	  {
	    String sp = "";
	    for (int k = 0; k < numVars; k++)
	    {
	      System.out.print(sp);
	      System.out.print(result[j][k]);
	      sp = " ";
	    }
	    System.out.println();
	  }
	  System.out.println("Got result len " + tests);
	}
      }
    }
  }
}
