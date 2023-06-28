#! /bin/zsh

clear

java --enable-preview \
     -Djdk.tracePinnedThreads=short \
     -jar target/quarkus-app/quarkus-run.jar
