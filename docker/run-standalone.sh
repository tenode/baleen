#!/bin/bash
echo "Running Baleen as current user in this working directory, press Ctrl+C to exit"
docker run -p 6413:6413 -u $(id -u) -v $(pwd):/baleen dstl/baleen ${@}
