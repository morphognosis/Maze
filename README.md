# Maze-learning.

This project compares the maze-learning performance of two artificial neural network architectures: a Long Short-Term Memory (LSTM)
recurrent network, and Morphognosis, a neural network based on a hierarchy of spatial and temporal contexts.
 
The mazes are sequences of distinctly marked rooms interconnected by doors. Mazes are used to examine two important problems related 
to artificial neural networks: (1) the retention of long-term state information and (2) the modular use of learned information. 
Addressing the former, mazes impose a context learning demand: at the beginning of the maze, an initial door choice forms a context 
that must be retained until the end of the maze, where the same door must be chosen again in order to reach the goal. For the latter, 
the effect of modular and non-modular training is examined. In modular training, the door-associations are trained in separate trials 
from the intervening maze paths, and only presented together in testing trials.

This work is a continuation of a previous project. See references.

## Description.

There are three learning goals:
1. Maze learning: learn to navigate a sequence of rooms connected by doors.
2. Context learning: learn correspondences between room configurations separated
   by intervening mazes.
3. Modular context learning: the intervening mazes are trained independently and
   presenting only during testing. This measures the ability to dynamically combine
   independently learned sequences to achieve success.

A maze consists of a sequence of rooms connected by doors.
There are a fixed number of doors.
The learner outputs a door choice or a wait.
A room contains a room-specific set of on/off marks.

These are the types of rooms:

1. Context begin room: in this room the learner is presented with marks having a single
   on value that corresponds to the correct door choice.
   This door leads to either a maze entry or directly to a context end room. If it leads to
   a maze entry room, a maze must be navigated before reaching the context end room.
   In the context end room, the learner must choose the door that was marked on in the
   context begin room.

2. Maze entry room: the marks identify the configuration of the upcoming maze sequence,
   consisting of maze interior rooms. The learner uses this information to navigate
   the maze.

3. Maze interior room: in the maze entry room determine the correct door choice sequence
   to move through the maze.

4. Context end room: in this room the correct door choice is determined by the context begin
   room door marks.

Input format:

```
<context room marks><maze entry room marks><maze interior room mark><context end room marks>
```

Output:
A door choice or a wait.

Training and testing.

There are three types of sequences:
1. Context: context begin room and context end room. To learn context associations.
2. Context maze: context begin room, maze entry and interior sequence, and context end room.
3. Independent maze: maze entry and maze interior sequence. To learn maze sequences.

For training, a set of the above sequences are created. This evaluates maze and context
learning.

For testing, context-mazes are created from novel context and independent maze combinations
taken from the training set. This evaluates modular context learning.

```
Output dataset files:

output_dataset_module = 'maze_dataset.py'
Contains:
X_train_shape = [<number of sequences>, <steps per sequence>, <input size>]
X_train_seq = [<input sequences (0|1)>]
y_train_shape = [<number of sequences>, <steps per sequence>, <output size>]
y_train_seq = [<output sequences>]
X_test_shape = [<number of sequences>, <steps per sequence>, <input size>]
X_test_seq = [<input sequences (0|1)>]
y_test_shape = [<number of sequences>, <steps per sequence>, <output size>]
y_test_seq = [<output sequences>]

output_dataset_csv = 'maze_dataset.csv'
Contains:
Training input shape and sequences:
<number of sequences>, <steps per sequence>, <input size>
<input sequences (0|1)>
Training output shape and sequences:
<number of sequences>, <steps per sequence>, <output size>
<output sequences>]
Testing input shape and sequences:
<number of sequences>, <steps per sequence>, <input size>
input sequences (0|1)>
Testing output shape and sequences:
<number of sequences>, <steps per sequence>, <output size>
<output sequences>
```

## Requirements.

Java 1.8 or later, Python 3.6.8 or later.

## Setup.

1. Clone or download and unzip the code from https://github.com/morphognosis/Maze.
2. Optional: Import Eclipse project.
3. Build: click or run the build.bat/build.sh in the work folder to build the code.

## Run.

Click on or run the maze.bat/maze.sh command in the work folder to create the mazes and bring up the dashboards.

```
Usage:
    java morphognosis.maze.Main
      [-batch (batch mode)]
      [-responseDriver <metamorphDB | metamorphNN> (response driver, default=metamorphDB)]
      [-randomSeed <random number seed> (default=4517)]
      [-writeMetamorphDataset [<file name>] (default=metamorphs.csv)]
      Maze maker parameters:
        [-numDoors <quantity> (default=3)]
        [-mazeInteriorLength <length> (default=4)]
        [-numContextMazes <quantity> (default=10)]
        [-numIndependentMazes <quantity> (default=10)]
      Morphognosis parameters:
        [-neighborhoodDurations <comma-separated values> (implies number of neighborhoods)]
      Metamorph Weka neural network parameters:
        [-NNlearningRate <quantity> (default=0.1)]
        [-NNmomentum <quantity> (default=0.2)]
        [-NNhiddenLayers <string of comma-separated numbers specifying number of neurons in each layer> (default="63")]
        [-NNtrainingTime <quantity> (default=5000)]
  Print parameters:
    java morphognosis.maze.Main -printParameters
  Version:
    java morphognosis.maze.Main -version
Exit codes:
  0=success
  1=error
```

## Other commands in work folder.

1. maze_maker.py/.bat/.sh: Make the mazes.
2. maze_rnn.py/.bat/.sh: Train and test using LSTM NN (requires numpy and keras Python packages).

## References

T. E. Portegys, "A Maze Learning Comparison of Elman, Long Short-Term Memory, and Mona Neural Networks", Neural Networks, 2010.
http://tom.portegys.com/research.html#maze
https://www.sciencedirect.com/science/article/abs/pii/S0893608009002871?via%3Dihub

Thomas E. Portegys, "Generating an artificial nest building pufferfish in a cellular automaton through behavior decomposition", International Journal of Artificial Intelligence and Machine Learning, 2019.
 
Thomas E. Portegys, "Morphognostic honey bees communicating nectar location through dance movements", preprint.
https://www.researchgate.net/publication/339899154_MORPHOGNOSTIC_HONEY_BEES_COMMUNICATING_NECTAR_LOCATION_THROUGH_DANCE_MOVEMENTS 