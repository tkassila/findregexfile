#!/bin/bash
export GJARSAN2_HOME=.
#echo GJARSAN2_HOME=$GJARSAN2_HOME
java -cp $GJARSAN2_HOME/findregexfile.jar; com.metait.findregexfile.FindRegexFileApplication $*



