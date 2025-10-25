#!/bin/bash

# Build script for Render deployment
echo "Starting build process..."

# Set Java version
export JAVA_HOME=/opt/java/openjdk
export PATH=$JAVA_HOME/bin:$PATH

# Install dependencies and build
./mvnw clean package -DskipTests

echo "Build completed successfully!"