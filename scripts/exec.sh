#!/bin/bash
#
i=0

#        java openchord.test.ScalabilityTest &
#        java freepastry.test.TestQueryTheorem &
#        java openchord.test.TestQueryTheorem &
#        java openchord.test.TestQueryTheoremB &
#        java freepastry.test.TestQueryTheoremB &
#        java freepastry.test.TestQueryTheoremB &
#        java passpartout.sample.meteor.TestQueryTheorem &
#        java openchord.test.TestFindSuccTheorem &
#        java -ea freepastry.test.TestFindSuccTheorem &
#        java freepastry.test.TestPeerIsolation &
#        java freepastry.test.TestNewJoin &
#        java freepastry.test.TestInsertJoin &
#        java freepastry.test.TestInsertJoinB &
#        java freepastry.test.TestInsertLeave &
#        java freepastry.test.TestInsertLeaveB &
#        java freepastry.test.TestInsertLeaveC &
#        java freepastry.test.SimpleTest &
#pack="freepastry.test.TestInsertStableNew"
pack="freepastry.test.TestInsertJoin"
#        java openchord.test.TestInsertJoin &
#        java openchord.test.TestInsertJoinB &
#        java openchord.test.TestInsertLeave &
#        java openchord.test.TestInsertLeaveB &
#        java openchord.test.TestInsert &
#        java openchord.test.TestInsertMultiple &
#        java openchord.test.TestNewJoin &
#        java openchord.test.TestPeerIsolation &
#        java openchord.test.TestConsistentHashing &
#        java openchord.test.TestInconclusive &
#        java openchord.test.TestInconclusiveArray &
echo ${pack}
while [ $i -le 4 ]
do
   ##      java -Dcom.sun.management.jmxremote -Xms256m -Xmx256m -Xss2048k emmarun -verbose -raw -out coverage${node_name}_${i}.es -Dreport.txt.out.file=coverage${node_name}_${i}.txt -cp peerunit.jar:FreePastry-2.0_02.jar ${pack} > log${i}.log &
         java ${pack} &
        i=`expr ${i} + 1`
done


