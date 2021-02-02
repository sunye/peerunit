rem MSDOS batch to test/demonstrate using some of these
rem routines via the command line
rem Most of them are really designed to be called from
rem inside programs. The CLI interface is just good enough
rem to be usable. That is my excuse once I worked out that
rem giving these a proper java package name would make the
rem main() routines incredibly long winded to use anyway

call setpath

java uk.co.demon.mcdowella.stats.Deviant < deviant.in
java uk.co.demon.mcdowella.stats.MultiDeviant
java uk.co.demon.mcdowella.stats.PredTest -goes 10
java uk.co.demon.mcdowella.stats.ProbCheck -goes 10
java uk.co.demon.mcdowella.stats.RowDist -cols 9 -rpt -trend -chi -mc 10000 < rowdist.in
java uk.co.demon.mcdowella.stats.RowDist -cols 9 -rpt -trend -chi -mc 10000 -probs < rowdistWithProbs.in
java uk.co.demon.mcdowella.stats.TabDist -cols 5 -ll -trend -prob -grain 10000 < TabDist.in
java uk.co.demon.mcdowella.stats.TabMC -rows 2 -cols 5 -goes 100000 < TabDist.in
java uk.co.demon.mcdowella.algorithms.TestCorasick a ab abc abcd abab ababab abababab ababababa < AhoCorasick.in > tc.out
java uk.co.demon.mcdowella.algorithms.TestCorasickRand
call pickwords
java uk.co.demon.mcdowella.algorithms.PermTester < ..\algorithms\PermTester.in
java uk.co.demon.mcdowella.algorithms.TestLCA
java uk.co.demon.mcdowella.algorithms.PatriciaTest
java uk.co.demon.mcdowella.stats.QuantileBounds -samples 6
java uk.co.demon.mcdowella.stats.Columns  -cols 10 -main 0,1 -compare 8,9 -mc 100000 -grain 0.01 < ColumnTest.txt
java uk.co.demon.mcdowella.algorithms.TestDoubleHeap -passes 5
java uk.co.demon.mcdowella.algorithms.PartialSumsTest
java uk.co.demon.mcdowella.algorithms.Assignment
