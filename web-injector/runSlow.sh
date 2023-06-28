#! /bin/zsh

if [ -z "$1" ]
then
	local uri="loom"
	
else
	local uri="$1"
fi

clear

java -Dloom.repeats=200000 \
     -Dloom.client.uri="http://localhost:8080/$uri/slow" \
     -Dloom.client.read-timeout-millis=7000 \
     -Dloom.bench.active=false \
     -Dloom.bench.warmup=false \
     -jar target/injector-1.0.jar
