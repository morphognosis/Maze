# Maze-learning.

This project compares the maze-learning performance of two artificial neural network architectures: a Long Short-Term Memory (LSTM)
recurrent network, and Morphognosis, a neural network based on a hierarchy of spatial and temporal contexts.
 
The mazes are sequences of distinctly marked rooms interconnected by doors. Mazes are used to examine two important problems related 
to artificial neural networks: (1) the retention of long-term state information and (2) the modular use of learned information. 
Addressing the former, mazes impose a context learning demand: at the beginning of the maze, an initial door choice forms a context 
that must be retained until the end of the maze, where the same door must be chosen again in order to reach the goal. For the latter, 
the effect of modular and non-modular training is examined. In modular training, the door-associations are trained in separate trials 
from the intervening maze paths, and only presented together in testing trials.

For maze specifics, see src/morphognosis/maze/maze_maker.py

This work is a continuation of a previous project. See references. 

References:

T. E. Portegys, "A Maze Learning Comparison of Elman, Long Short-Term Memory, and Mona Neural Networks", Neural Networks, 2010.
http://tom.portegys.com/research.html#maze
https://www.sciencedirect.com/science/article/abs/pii/S0893608009002871?via%3Dihub

Thomas E. Portegys, "Generating an artificial nest building pufferfish in a cellular automaton through behavior decomposition", International Journal of Artificial Intelligence and Machine Learning, 2019.
 
Thomas E. Portegys, "Morphognostic honey bees communicating nectar location through dance movements", preprint.
https://www.researchgate.net/publication/339899154_MORPHOGNOSTIC_HONEY_BEES_COMMUNICATING_NECTAR_LOCATION_THROUGH_DANCE_MOVEMENTS 