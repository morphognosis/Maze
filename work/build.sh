#!/bin/bash
javac -classpath "../lib/morphognosis.jar:../lib/weka.jar" -d . ../src/morphognosis/*.java ../src/morphognosis/maze/*.java
cp ../src/morphognosis/maze/maze.py .
jar cvfm ../bin/maze.jar honey_bees.mf morphognosis maze.py
