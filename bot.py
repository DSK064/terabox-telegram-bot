"""
TeraBox Video Downloader Telegram Bot
Downloads videos/files from TeraBox share links and sends them in Telegram.
No premium account needed — free TeraBox ndus cookie works.
Uses multi-strategy link resolution (proxy API + direct API fallback).
"""

import os
import re
import logging
import asyncio
import tempfile
import shutil
from pathlib import Path

import requests
from dotenv import load_dotenv
from telegram import Update, InlineKeyboardButton, InlineKeyboardMarkup
from telegram.ext import (
    Application,
    CommandHandler,
    MessageHandler,
    filters,
    ContextTypes,
)

from terabox import get_download_info, is_terabox_url

load_dotenv()

BOT_TOKEN = os.getenv("BOT_TOKEN")
TERABOX_COOKIE = os.getenv("TERABOX_COOKIE", "")

if not BOT_TOKEN:
    raise ValueError("BOT_TOKEN not set in .env")

logging.basicConfig(
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    level=logging.INFO,
)
logger = logging.getLogger(__name__)

MAX_TELEGRAM_FILE_SIZE = 50 * 1024 * 1024  # 50 MB

TERABOX_URL_PATTERN = re.compile(
    r"https?://(?:www\.)?"
    r"(?:terabox\.app|teraboxshare\.com|terabox\.com|1024terabox\.com|"
    r"teraboxlink\.com|terasharefile\.com|terafileshare\.com|terasharelink\.com|"
    r"teraboxapp\.com|mirrobox\.com|nephobox\.com|freeterabox\.com|"
    r"momerybox\.com|tibibox\.com|4funbox\.co|1024tera\.com)"
    r"/s/[a-zA-Z0-9_-]+",
    re.IGNORECASE,
)


def format_size(size_bytes: int) -> str:
    if size_bytes < 1024:
        return f"{size_bytes} B"
    elif size_bytes < 1024 * 1024:
        return f"{size_bytes / 1024:.1f} KB"
    elif size_bytes < 1024 * 1024 * 1024:
        return f"{size_bytes / (1024 * 1024):.1f} MB"
    else:
        return f"{size_bytes / (1024 * 1024 * 1024):.2f} GB"


def extract_terabox_url(text: str) -> str | None:
    match = TERABOX_URL_PATTERN.search(text)
    return match.group(0) if match else None


def download_file(url: str, dest_path: str) -> bool:
    """Download a file from URL to dest_path with streaming."""
    try:
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        }
        with requests.get(url, headers=headers, stream=True, timeout=120) as r:
            r.raise_for_status()
            with open(dest_path, "wb") as f:
                for chunk in r.iter_content(chunk_size=8192):
                    f.write(chunk)
        return True
    except Exception as e:
        logger.error(f"Download failed: {e}")
        return False


async def start_command(update: Update, context: ContextTypes.DEFAULT_TYPE):
    welcome = (
        "🎬 *TeraBox Video Downloader Bot*\n\n"
        "Send me a TeraBox share link and I'll download the video/file for you\\!\n\n"
        "📎 *Supported links:*\n"
        "• terabox\\.com/s/\\.\\.\\.\n"
        "• teraboxshare\\.com/s/\\.\\.\\.\n"
        "• 1024terabox\\.com/s/\\.\\.\\.\n"
        "• teraboxlink\\.com/s/\\.\\.\\.\n"
        "• and more\\.\\.\\.\n\n"
        "Just paste the link and I'll handle the rest\\! 🚀"
    )
    await update.message.reply_text(welcome, parse_mode="MarkdownV2")


async def help_command(update: Update, context: ContextTypes.DEFAULT_TYPE):
    help_text = (
        "📖 *How to use:*\n\n"
        "1\\. Copy a TeraBox share link\n"
        "2\\. Paste it here\n"
        "3\\. Wait for the download\n"
        "4\\. Get your file\\!\n\n"
        "⚠️ *Limits:*\n"
        "• Max file size: 50 MB \\(Telegram bot API limit\\)\n"
        "• For larger files, I'll send the direct download link instead"
    )
    await update.message.reply_text(help_text, parse_mode="MarkdownV2")


async def handle_terabox_link(update: Update, context: ContextTypes.DEFAULT_TYPE):
    url = extract_terabox_url(update.message.text)
    if not url:
        return

    logger.info(f"Received TeraBox link: {url} (from user: {update.message.from_user.id})")
    status_msg = await update.message.reply_text("🔍 Fetching file info from TeraBox...")

    try:
        loop = asyncio.get_running_loop()
        file_info = await loop.run_in_executor(
            None, lambda: get_download_info(url, TERABOX_COOKIE)
        )

        logger.info(f"Resolver response: {file_info}")

        if "error" in file_info:
            await status_msg.edit_text(f"❌ {file_info['error']}")
            return

        file_name = file_info.get("file_name", "video.mp4")
        file_size = file_info.get("sizebytes", 0)
        file_size_str = file_info.get("file_size", format_size(file_size))
        thumbnail = file_info.get("thumbnail", "")
        download_link = file_info.get("download_link", "")
        hd_link = file_info.get("hd_link", "")

        if not download_link:
            await status_msg.edit_text("❌ Could not extract download link.")
            return

        # Build info text
        info_text = f"📁 **{file_name}**\n📦 Size: {file_size_str}"

        # If file too large or size unknown but likely large, send link
        if file_size > MAX_TELEGRAM_FILE_SIZE:
            info_text += f"\n\n⚠️ File too large for direct upload (>{format_size(MAX_TELEGRAM_FILE_SIZE)})."
            buttons = [[InlineKeyboardButton("⬇️ Download", url=download_link)]]
            if hd_link and hd_link != download_link:
                buttons.append([InlineKeyboardButton("🎬 HD Video", url=hd_link)])
            reply_markup = InlineKeyboardMarkup(buttons)

            if thumbnail:
                await status_msg.delete()
                await update.message.reply_photo(
                    photo=thumbnail, caption=info_text,
                    parse_mode="Markdown", reply_markup=reply_markup,
                )
            else:
                await status_msg.edit_text(info_text, parse_mode="Markdown", reply_markup=reply_markup)
            return

        # Download and send
        await status_msg.edit_text(f"⬇️ Downloading: {file_name} ({file_size_str})...")

        tmp_dir = tempfile.mkdtemp(prefix="terabox_")
        try:
            file_path = os.path.join(tmp_dir, file_name)
            success = await asyncio.get_running_loop().run_in_executor(
                None, lambda: download_file(download_link, file_path)
            )

            if not success or not Path(file_path).exists():
                # Fallback: send download link
                buttons = [[InlineKeyboardButton("⬇️ Download", url=download_link)]]
                await status_msg.edit_text(
                    f"📁 **{file_name}**\n📦 {file_size_str}\n\n⚠️ Direct download failed. Use the link:",
                    parse_mode="Markdown",
                    reply_markup=InlineKeyboardMarkup(buttons),
                )
                return

            actual_size = Path(file_path).stat().st_size
            if actual_size > MAX_TELEGRAM_FILE_SIZE:
                buttons = [[InlineKeyboardButton("⬇️ Download", url=download_link)]]
                await status_msg.edit_text(
                    f"📁 **{file_name}**\n📦 {format_size(actual_size)}\n\n⚠️ File too large. Use the link:",
                    parse_mode="Markdown",
                    reply_markup=InlineKeyboardMarkup(buttons),
                )
                return

            await status_msg.edit_text("📤 Uploading to Telegram...")

            video_extensions = {".mp4", ".mkv", ".avi", ".mov", ".webm", ".flv"}
            ext = Path(file_path).suffix.lower()

            with open(file_path, "rb") as f:
                if ext in video_extensions:
                    await update.message.reply_video(
                        video=f, caption=f"📁 {file_name}",
                        supports_streaming=True,
                        read_timeout=300, write_timeout=300,
                    )
                else:
                    await update.message.reply_document(
                        document=f, filename=file_name,
                        caption=f"📁 {file_name}",
                        read_timeout=300, write_timeout=300,
                    )

            await status_msg.delete()

        finally:
            shutil.rmtree(tmp_dir, ignore_errors=True)

    except Exception as e:
        logger.error(f"Error processing TeraBox link: {e}", exc_info=True)
        await status_msg.edit_text(f"❌ Something went wrong: {str(e)}")


async def handle_non_link(update: Update, context: ContextTypes.DEFAULT_TYPE):
    if update.message and update.message.text:
        text = update.message.text.strip()
        if "terabox" in text.lower() or "tera" in text.lower():
            await update.message.reply_text(
                "🤔 That doesn't look like a valid TeraBox link.\n\n"
                "Please send a link like:\n"
                "`https://teraboxshare.com/s/abc123`",
                parse_mode="Markdown",
            )


def build_app() -> Application:
    app = Application.builder().token(BOT_TOKEN).build()
    app.add_handler(CommandHandler("start", start_command))
    app.add_handler(CommandHandler("help", help_command))
    app.add_handler(MessageHandler(
        filters.TEXT & filters.Regex(TERABOX_URL_PATTERN), handle_terabox_link,
    ))
    app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, handle_non_link))
    return app


def main():
    mode = os.getenv("MODE", "polling").lower()
    app = build_app()

    if mode == "webhook":
        port = int(os.getenv("PORT", "8080"))
        webhook_url = os.getenv("WEBHOOK_URL", "")
        if not webhook_url:
            raise ValueError("WEBHOOK_URL must be set in webhook mode")
        logger.info(f"Bot starting in WEBHOOK mode on port {port}")
        app.run_webhook(
            listen="0.0.0.0",
            port=port,
            webhook_url=webhook_url,
            allowed_updates=Update.ALL_TYPES,
        )
    else:
        logger.info("Bot started in POLLING mode! Waiting for TeraBox links...")
        app.run_polling(allowed_updates=Update.ALL_TYPES)


if __name__ == "__main__":
    import sys
    if sys.version_info >= (3, 14):
        asyncio.set_event_loop(asyncio.new_event_loop())
    main()
