#!/bin/bash
cd ~/PeerFolder/scripts/
rm traceroute/*.class
rm tools/*.class
rm *.o
rm *.so

javac -cp ../Lib/addressmaper/:../Lib/traceroute-1.2.jar  ../Lib/addressmaper/tools/AddressMaper.java

#jar -cfm addressmaper.jar manifest.m traceroute-1.2.jar  ./tools

java -cp ../Lib/addressmaper/:../Lib/traceroute-1.2.jar tools.AddressMaper -f $1
cat $1ip

#java -cp ../Lib/addressmaper/:../Lib/traceroute-1.2.jar tools.AddressMaper $1

#java -cp  traceroute.jar:. traceroute.TraceRoute -n google.com
