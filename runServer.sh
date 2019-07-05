#!/bin/bash
workDir="/home/tubi/Minimum-Spanning-Tree-Distributed/bin"
if [ ! -d "$workDir" ]; then
    echo "directory not found, check your path"
else
    gnome-terminal \
	--working-directory="$workDir" \
	-e 'java NameServer'
fi
