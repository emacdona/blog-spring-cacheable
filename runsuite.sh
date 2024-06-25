#!/usr/bin/env bash
set -exo pipefail

make clear-cache-for-all-replicas get-book
make bad-update-title get-book
make better-update-title get-book
make best-update-title get-book
