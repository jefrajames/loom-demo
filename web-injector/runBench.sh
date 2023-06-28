#! /bin/zsh

if [ -z "$1" ]
then
	local uri="loom"
	
else
	local uri="$1"
fi

clear

java -Dloom.repeats=60000 \
     -Dloom.client.uri="http://localhost:8080/$uri/heap" \
     -Dloom.bench.active=true \
     -Dloom.bench.warmup=true \
     -jar target/injector-1.0.jar

echo -n "Max Server Heap Size: "
curl localhost:8080/$uri/maxheap
echo ""

echo "Server memory"

echo -n "\tJava: "
curl localhost:8080/$uri/memory

echo -n "\tRSS: "
pid=$(curl -s localhost:8080/$uri/pid)
cmd="ps -o rss -p $pid"
eval ${cmd} | grep -v RSS

echo "Forcing Server GC..."
#curl -X PUT localhost:8080/$uri/gc
curl localhost:8080/$uri/gc

sleep 5

echo "Server memory after GC"

echo -n "\tJava: "
curl localhost:8080/$uri/memory

echo -n "\tRSS: "
pid=$(curl -s localhost:8080/$uri/pid)
cmd="ps -o rss -p $pid"
eval ${cmd} | grep -v RSS
