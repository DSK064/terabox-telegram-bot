# TeraBox Video Downloader Telegram Bot

A Telegram bot that downloads videos/files from TeraBox share links and sends them directly in chat. **No premium TeraBox account needed** — works with a free account's `ndus` cookie.

## Features
- Send a TeraBox link → get the video/file directly in Telegram
- Shows file info (name, size, thumbnail) before downloading
- Supports all TeraBox share URL formats
- Progress updates during download
- 2GB Telegram file size limit enforced

## Setup

### 1. Get your TeraBox `ndus` cookie (FREE account works)
1. Create a free account at [terabox.com](https://www.terabox.com)
2. Log in, press F12 → Application tab → Cookies → terabox.com
3. Copy the `ndus` cookie value

### 2. Create a Telegram Bot
1. Message [@BotFather](https://t.me/BotFather) on Telegram
2. Send `/newbot`, follow prompts
3. Copy the bot token

### 3. Install & Run
```bash
cd terabox-telegram-bot
pip install -r requirements.txt
```

Create a `.env` file:
```
BOT_TOKEN=your_telegram_bot_token
TERABOX_COOKIE=lang=en; ndus=your_ndus_value;
```

Run:
```bash
python bot.py
```

## Usage
1. Start the bot: `/start`
2. Send any TeraBox share link
3. Bot extracts the direct download link, downloads the file, and sends it to you

## Supported TeraBox Domains
- terabox.app
- teraboxshare.com
- terabox.com
- 1024terabox.com
- teraboxlink.com
- terasharefile.com
- terafileshare.com
- terasharelink.com
