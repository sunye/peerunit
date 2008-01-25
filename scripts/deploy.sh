#!/bin/bash
##############
# Script to deploy Passpartout  
#
# Author : Eduardo Almeida
# Date : 16-mar-2006
# Update : 22-jun-2006
#############

HOST_FILE=hosts.txt
PASS_FILE=${HOME}/Passpartout.jar
CHORD_FILE=${HOME}/library/openchord_1.0.3.jar
LOG4J_FILE=${HOME}/log4j-1.2.12.jar
LOG4J_PROP_FILE=${HOME}/log4j.properties
RMI_DIR=${HOME}/passpartout

#############
# Read the file with the machine list
#############

while read line;
do
	scp -r ${RMI_DIR} ${PASS_FILE} ${CHORD_FILE} ${LOG4J_FILE} ${LOG4J_PROP_FILE} ${USER}@$line:
done< ${HOST_FILE}

