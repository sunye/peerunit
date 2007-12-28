#!/bin/bash
#
i=0
while [ $i -le 7 ]
do
#        java openchord.test.ScalabilityTest &
#        java freepastry.test.TestQueryTheorem &
#        java openchord.test.TestQueryTheorem &
#        java openchord.test.TestQueryTheoremB &
#        java freepastry.test.TestQueryTheoremB &
#        java freepastry.test.TestQueryTheoremB &
#        java passpartout.sample.meteor.TestQueryTheorem &
#	 java openchord.test.TestFindSuccTheorem &
#	 java -ea freepastry.test.TestFindSuccTheorem &
#	 java freepastry.test.TestPeerIsolation &
#	 java freepastry.test.TestNewJoin &
	 java freepastry.test.TestInsertJoin &
#	 java freepastry.test.TestInsertJoinB &
#	 java freepastry.test.TestInsertLeave &
#	 java freepastry.test.TestInsertLeaveB &
#	 java freepastry.test.TestInsertLeaveC &
#	 java freepastry.test.SimpleTest &
#	 java freepastry.test.TestInsertStable &
#	 java openchord.test.TestInsertJoin &
#	 java openchord.test.TestInsertJoinB &
#	 java openchord.test.TestInsertLeave &
#	 java openchord.test.TestInsertLeaveB &
#	 java openchord.test.TestInsert &
#	 java openchord.test.TestInsertMultiple &
#	 java openchord.test.TestNewJoin &
#	 java openchord.test.TestPeerIsolation &
#	 java openchord.test.TestConsistentHashing &
#  	 java openchord.test.TestInconclusive &
#  	 java openchord.test.TestInconclusiveArray &
        i=`expr ${i} + 1`
done

