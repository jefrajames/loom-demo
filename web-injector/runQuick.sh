#! /bin/zsh

if [ -z "$1" ]
then
	local uri="loom"
	
else
	local uri="$1"
fi

clear

java -Dloom.repeats=200000 \
     -Dloom.client.uri="http://localhost:8080/$uri/quick" \
     -Dloom.bench.warmup=false \
     -Dloom.bench.active=false \
     -jar target/injector-1.0.jar
