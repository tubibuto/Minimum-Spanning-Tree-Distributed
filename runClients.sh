#!/bin/bash
workDir="/home/tubi/Minimum-Spanning-Tree-Distributed/bin"
if [ ! -d "$workDir" ]; then
    echo "directory not found, check your path"
else
    if [ "$1" != "" ]; then
        n=$1
    else 
        n=5
    fi
    for i in $(seq 0 $((n-1)))
        do
        gnome-terminal \
	    --working-directory="$workDir" \
	    -e "java MSTTester test $i $n"
    done
fi
