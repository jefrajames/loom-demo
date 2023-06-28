java \
     --enable-preview \
     -Djdk.tracePinnedThreads=short \
     -Dserver.executor-service.virtual-threads=true \
     -Dserver.executor-service.virtual-enforced=true \
     -jar target/server-1.0.jar
