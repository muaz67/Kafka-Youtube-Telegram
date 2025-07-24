# Kafka Configuration

This directory contains Kafka configuration files for the YouTube Analytics project.

## Configuration Details

- Using KRaft mode (no Zookeeper required)
- Single node setup for development
- Configured with both broker and controller roles
- Internal communication on port 29092
- External access on port 9092

## Topics

- `youtube-data`: Main topic for YouTube video data
  - Partitions: 3
  - Replication Factor: 1
  - Retention: 24 hours (86400000 ms)

## Usage

The Kafka service is configured in the root `docker-compose.yml` file. To manage topics:

1. Make the script executable:
   ```bash
   chmod +x create-topics.sh
   ```

2. Run the script:
   ```bash
   docker exec -it youtube-kafka ./create-topics.sh
   ```

## Connection Details

- Bootstrap Servers:
  - Internal (within Docker): `kafka:29092`
  - External (from host): `localhost:9092` 