package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.executor.ExecutorImplTest;
import test.remote.CoordinatorImplTest;
import test.remote.TesterImplTest;

@RunWith(Suite.class)
@SuiteClasses(value={
ExecutorImplTest.class,
TesterImplTest.class,
CoordinatorImplTest.class
})
public class AllTests{
}