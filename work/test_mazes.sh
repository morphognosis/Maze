#!/bin/bash
# Generate results.
# Parameters:
#
#      Maze maker parameters:
#        [-numDoors <quantity> (default=3)]
#        [-mazeInteriorLength <length> (default=4)]
#        [-numContextMazes <quantity> (default=5)]
#        [-numIndependentMazes <quantity> (default=2)]
#      Morphognosis parameters:
#        [-neighborhoodDurations <comma-separated values> (implies number of neighborhoods)]
#      Metamorph Weka neural network parameters:
#        [-NNlearningRate <quantity> (default=0.1)]
#        [-NNmomentum <quantity> (default=0.2)]
#        [-NNhiddenLayers <quantity> (default="50")]
#        [-NNtrainingTime <quantity> (default=5000)];
MIN_NUM_DOORS=2
MAX_NUM_DOORS=5
MIN_MAZE_INTERIOR_LENGTH=2
MAX_MAZE_INTERIOR_LENGTH=7
MIN_CONTEXT_MAZES=2
MAX_CONTEXT_MAZES=8
MIN_NUM_INDEPENDENT_MAZES=2
MAX_NUM_INDEPENDENT_MAZES=8

for (( numDoors=${MIN_NUM_DOORS}; numDoors <= $MAX_NUM_DOORS; ++numDoors)); do
  echo numDoors = $numDoors
  for (( mazeInteriorLength=${MIN_MAZE_INTERIOR_LENGTH}; mazeInteriorLength <= $MAX_MAZE_INTERIOR_LENGTH; ++mazeInteriorLength)); do
    echo mazeInteriorLength = $mazeInteriorLength
    for (( numContextMazes=${MIN_CONTEXT_MAZES}; numContextMazes <= $MAX_CONTEXT_MAZES; ++numContextMazes)); do
      echo numContextMazes = $numContextMazes
      for (( numIndependentMazes=${MIN_NUM_INDEPENDENT_MAZES}; numIndependentMazes <= $MAX_NUM_INDEPENDENT_MAZES; ++numIndependentMazes)); do
        echo numIndependentMazes = $numIndependentMazes
        trials=10
        echo Trials = $trials
        for (( i=0; i< $trials; ++i)); do
            echo trial = $i
            randomSeed=`expr $i + 1`
            ./maze.sh -batch -responseDriver metamorphDB -randomSeed $randomSeed -numDoors $numDoors -mazeInteriorLength $mazeInteriorLength -numContextMazes $numContextMazes -numIndependentMazes $numIndependentMazes
            #python maze_rnn.py
            exit 0
        done
      done
    done
  done
done