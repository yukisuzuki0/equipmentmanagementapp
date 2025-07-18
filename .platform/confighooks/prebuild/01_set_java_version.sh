#!/bin/bash
# Javaバージョンを設定
echo "Setting Java version to 21"
sudo update-alternatives --set java /usr/lib/jvm/java-21-amazon-corretto.x86_64/bin/java
sudo update-alternatives --set javac /usr/lib/jvm/java-21-amazon-corretto.x86_64/bin/javac 