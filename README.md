# What you can modify 
- The web UI
- the sceduler time of producer is refresh every 5 minutes
- cloud application
- the producer only check the first comment (you increase the amount of comment need to be check and processing through kafka)


# YouTube Analytics System

A real-time YouTube analytics system that processes video data through Kafka and provides analytics through a web interface and Telegram notifications.

## System Architecture

The system consists of four main components:

1. **Producer Service** (Port 8080)
   - Fetches YouTube video data using the YouTube API
   - Processes and sends data to Kafka topics
   - Handles video addition and updates

2. **Consumer Service** (Port 8081)
   - Processes data from Kafka
   - Provides real-time analytics
   - Serves the web dashboard

3. **Telegram Bot Service** 
   - Sends notifications to Telegram
   - Processes video updates with odd-length comments

4. **Infrastructure**
   - Kafka for message streaming
   - In-memory storage for analytics

## Prerequisites

- Docker and Docker Compose
- YouTube API Key
- Telegram Bot Token and Chat ID

### Getting Telegram Bot Token and Chat ID

1. **Create a Telegram Bot**:
   - Open Telegram and search for "@BotFather"
   - Start a chat with BotFather
   - Send `/newbot` command
   - Follow the instructions to create your bot
   - Save the bot token provided by BotFather

2. **Get Chat ID**:
   - Start a chat with your new bot
   - Send any message to the bot
   - Open this URL in your browser (replace with your bot token):
     ```
     https://api.telegram.org/bot<YourBOTToken>/getUpdates
     ```
   - Look for the "chat" object in the response:
     ```json
     {
       "message": {
         "chat": {
           "id": 123456789,  // This is your chat ID
           "type": "private",
           ...
         }
       }
     }
     ```
   - Save the chat ID number

## Environment Variables and Security

### Local Development
1. Create a `.env` file with placeholder values in the root of the project:
   ```env
   YOUTUBE_API_KEY=your_youtube_api_key_here
   TELEGRAM_BOT_TOKEN=your_telegram_bot_token_here
   TELEGRAM_BOT_USERNAME=your_bot_username_here
   TELEGRAM_CHAT_ID=your_chat_id_here
   ```

2. The `.gitignore` file is configured to:
   - Ignore `.env` file containing real credentials
   - Track `.env.example` as a template
   - Ignore other sensitive files

### Production Deployment
For production, never commit sensitive values to the repository. Instead:
1. Use environment variables in your deployment platform
2. Use secrets management services
3. Keep credentials in a secure location

## Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yt02/RealTimeKafkaSpringBootProject.git
   cd <repository-directory>
   ```

2. **Environment Setup**
   ```bash
   # Copy the example env file
   cp .env.example .env
   
   # Edit .env with your actual credentials
   nano .env
   ```

3. **Build and Start Services**
   ```bash
   docker-compose up -d
   ```

   This will start:
   - Kafka (accessible at kafka:9092)
   - Producer Service (Port 8080)
   - Consumer Service (Port 8081)
   - Telegram Bot Service

4. **Verify Services**
   
   Check if all services are running:
   ```bash
   docker-compose ps
   ```

## Using the System

### Web Dashboard

1. Access the analytics dashboard at:
   ```
   http://localhost:8081
   ```

   The dashboard shows:
   - Total statistics (views, likes, comments)
   - Top performing videos
   - Latest video updates
   - Engagement metrics

### Adding Videos

1. Use the Producer spring boot webpage to add videos:

   ```
   http://localhost:8080
   ```

    - Paste a youtube video link in the text field 
    - Click the "Add video" button
   

### Telegram Notifications

The system will automatically:
- Send notifications for videos with odd-length comments
- Include video statistics and engagement metrics
- Provide direct links to videos

To verify Telegram setup:
1. Make sure your bot is active and you've started a chat with it
2. Add a YouTube video with an odd-length comment
3. You should receive a notification in your Telegram chat

## Data Processing Rules

1. **Comment Processing**
   - Even-length comments → Analytics processing
   - Odd-length comments → Telegram notifications

2. **Analytics Calculation**
   - Engagement Rate = (Likes + Comments) / Views × 100
   - All metrics are updated in real-time
   - Top performers are calculated across all tracked videos

## Monitoring and Maintenance

### Logs
View service logs:
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f [service-name]
```

### Stopping the System
```bash
docker-compose down
```

To remove all data:
```bash
docker-compose down -v
```

## Troubleshooting

1. **Service Not Starting**
   - Check environment variables in `.env`
   - Verify Docker service is running
   - Check port conflicts

2. **No Data in Dashboard**
   - Verify Kafka is running
   - Check Producer service logs
   - Ensure valid YouTube API key

3. **No Telegram Notifications**
   - Verify bot token and chat ID are correct
   - Make sure you've started a chat with your bot
   - Check if the bot has necessary permissions
   - Verify the chat ID matches where you want to receive notifications
   - Check Telegram Bot service logs
   - Try sending a test message via the Telegram API:
     ```
     https://api.telegram.org/bot<YourBOTToken>/sendMessage?chat_id=<YourChatID>&text=Test
     ```

## API Endpoints

### Producer Service (8080)
- `GET /api/youtube/search?query=VIDEO_URL` - Add/update video
- `GET /api/youtube/videos` - List all tracked videos
- `DELETE /api/youtube/videos/{videoId}` - Remove video

### Consumer Service (8081)
- `GET /api/analytics/highest` - Get top performers
- `GET /api/analytics/videos` - Get all videos
- `GET /api/analytics/comparison` - Get comparative analytics

