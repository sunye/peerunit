package uk.co.demon.mcdowella.algorithms;
/** This interface is implemented by objects that save and restore
 *  state in a nested manner on requested
 */
public interface StackedState
{
  /** Save state on the stack */
  void mark();
  /** Remove the previous mark from the stack, accepting all mods
   *  since then subject to any previous marks
   */
  void accept();
  /** Remove the previous mark from the stack, restoring the state
   *  as it was when the mark() was called
   */
  void reject();
}
