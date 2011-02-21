package uk.co.demon.mcdowella.stats;

/** Interface for smoothers */
public interface Smoother
{
  /** fit automatically */
  double autoFit();
  /** fit given a window parameter */
  double fit(int windowLength);
  /** return parameter for last fit */
  int getWindowLength();
  /** return the value at a given x point */
  double getValue(double target);
}
