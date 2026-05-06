#!/bin/bash
set -e

lein test
lein uberjar
podman build -t witch-house/pronouns .
