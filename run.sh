#!/bin/bash

echo "================================================"
echo "   Courier Tracker Service - Starting..."
echo "================================================"

# Check Java version
if ! java -version 2>&1 | grep -q '"21'; then
    echo "Error: Java 21 is required. Please set JAVA_HOME to Java 21."
    java -version
    exit 1
fi

echo "Using Java from: $JAVA_HOME"
java -version

cd "$(dirname "$0")"

echo ""
echo "Building the application..."
echo ""

./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "Build failed!"
    exit 1
fi

echo ""
echo -e "Build successful!"
echo ""


echo "Starting the application..."

./mvnw spring-boot:run

