#!/usr/bin/env bash

# Loading module for QE:
#
module load plgrid/apps/espresso

# Running simulation using MPI:
#
mpirun pw.x -nband 1 -ntg 1 < simulation.in > simulation.out

# Extracting simulation steps to mol files:
#
date
module load plgrid/tools/atomsk
module load plgrid/tools/openbabel

mkdir mol
cd mol
cp ../simulation.out simulation.out

atomsk --one-in-all simulation.out xyz
for xyzFile in *.xyz
do
    babel -ixyz "${xyzFile}" -omol "${xyzFile%.xyz}.mol"
done
rm *.xyz simulation.out
date
