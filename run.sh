#!/bin/sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
classes_dir="$project_dir/build/classes"
mkdir -p "$classes_dir"

find "$project_dir/src/main/java" -name '*.java' -print0 \
  | xargs -0 javac -encoding UTF-8 --release 17 -d "$classes_dir"

java -cp "$classes_dir" by.bsuir.morse.App
