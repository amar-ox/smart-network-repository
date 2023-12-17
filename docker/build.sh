#!/usr/bin/env bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

docker build -t "smartrepo" -f $DIR/../smartrepo/Dockerfile $DIR/../smartrepo