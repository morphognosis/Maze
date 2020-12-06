javac -classpath "../lib/morphognosis.jar;../lib/weka.jar" -d . ../src/morphognosis/*.java ../src/morphognosis/maze/*.java
copy ..\src\morphognosis\maze\maze.py .
jar cvfm ../bin/maze.jar maze.mf morphognosis maze.py