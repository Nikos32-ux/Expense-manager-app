#!/bin/bash

# --- CONFIGURATION ---
APP_NAME="expense-app"
LOG_FILE="ci-build.log"

echo " Starting CI for $APP_NAME..."

#INTEGRATION

# 1. CLEANING PHASE
echo "🧹 Step 1: Cleaning previous builds..."
mvn clean > $LOG_FILE 2>&1
if [ $? -ne 0 ]; then
    echo "❌ Clean failed! Check $LOG_FILE"
    exit 1
fi

# 2. UNIT TESTING PHASE (The most important part of CI)
echo "🧪 Step 2: Running JUnit Tests..."
mvn test >> $LOG_FILE 2>&1
if [ $? -ne 0 ]; then
    echo "❌ Tests failed! Fix your code before deploying."
    exit 1
fi
echo "✅ Tests Passed!"

# 3. PACKAGING PHASE
echo "📦 Step 3: Packaging Application (.jar)..."
mvn package -DskipTests >> $LOG_FILE 2>&1
if [ $? -ne 0 ]; then
    echo "❌ Packaging failed!"
    exit 1
fi

# 4. VERIFICATION (Final Check)
if [ -f target/*.jar ]; then
    echo "✨ CI SUCCESS: Artifact created successfully."
    echo "------------------------------------------"
    echo "Proceeding to Dockerization (CD)..."
else
    echo "❌ CI FAILED: Jar file not found."
    exit 1
fi

echo "✨ CI Success! Starting Deployment..."

# DEPLOYMENT
echo "🛑 Stopping and removing existing container..."

docker stop $APP_NAME 2>/dev/null || echo "Container not running."

docker rm $APP_NAME 2>/dev/null || echo "Container does not exist."

echo "🐳 Building Docker image..."
docker build -t $APP_NAME-image . || { echo "❌ Image build failed"; exit 1; }

echo "🚀 Running Docker container..."
docker run -d --name $APP_NAME -p 8081:8080 $APP_NAME-image

echo "✅ Deployment Complete! App is live at http://localhost:8081"
