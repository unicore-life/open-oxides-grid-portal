#!/usr/bin/env bash

# Loading module for QE:
#
module load plgrid/apps/espresso

# Running simulation using MPI:
#
mpirun pw.x -nband 1 -ntg 1 < simulation.in > simulation.out
