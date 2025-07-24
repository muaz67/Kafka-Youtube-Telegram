#!/bin/bash

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
kafka-topics --bootstrap-server kafka:29092 --list

# Create topics with proper configurations
echo "Creating topics..."

# YouTube data topic
kafka-topics --bootstrap-server kafka:29092 \
    --create \
    --if-not-exists \
    --topic youtube-data \
    --partitions 3 \
    --replication-factor 1 \
    --config retention.ms=86400000

# Add more topics here if needed

echo "Topics created successfully!"

# List all topics
echo "Current topics:"
kafka-topics --bootstrap-server kafka:29092 --list 