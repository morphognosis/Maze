#!/bin/bash
javac -classpath "../lib/weka.jar" -d . ../src/morphognosis/*.java ../src/morphognosis/maze/*.java
cp ../src/morphognosis/maze/maze_maker.py .
cp ../src/morphognosis/maze/maze_rnn.py .
jar cvfm ../bin/maze.jar maze.mf morphognosis maze_maker.py maze_rnn.py
