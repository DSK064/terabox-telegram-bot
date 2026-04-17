"""
TeraBox link resolver — extracts direct download links from TeraBox share URLs.
Uses multiple strategies for reliability:
  1. Third-party proxy API (no cookie needed)
  2. Direct TeraBox API with cookie (fallback)
"""

import re
import logging
from urllib.parse import urlparse, parse_qs

import requests

logger = logging.getLogger(__name__)

TERABOX_DOMAINS = [
    r"terabox\.app", r"terabox\.com", r"teraboxshare\.com",
    r"1024terabox\.com", r"teraboxlink\.com", r"terasharefile\.com",
    r"terafileshare\.com", r"terasharelink\.com", r"teraboxapp\.com",
    r"mirrobox\.com", r"nephobox\.com", r"freeterabox\.com",
    r"momerybox\.com", r"tibibox\.com", r"4funbox\.co",
    r"1024tera\.com",
]


def is_terabox_url(url: str) -> bool:
    for pattern in TERABOX_DOMAINS:
        if re.search(pattern, url, re.IGNORECASE):
            return True
    return False


def normalize_url(url: str) -> str:
    netloc = urlparse(url).netloc
    return url.replace(netloc, "1024terabox.com")


def _find_between(data: str, first: str, last: str) -> str | None:
    try:
        start = data.index(first) + len(first)
        end = data.index(last, start)
        return data[start:end]
    except ValueError:
        return None


def _format_size(size_bytes: int) -> str:
    if not size_bytes:
        return "Unknown"
    if size_bytes < 1024:
        return f"{size_bytes} B"
    elif size_bytes < 1024 * 1024:
        return f"{size_bytes / 1024:.1f} KB"
    elif size_bytes < 1024 * 1024 * 1024:
        return f"{size_bytes / (1024 * 1024):.1f} MB"
    else:
        return f"{size_bytes / (1024 * 1024 * 1024):.2f} GB"


def get_via_proxy_api(url: str) -> dict | None:
    """Strategy 1: Third-party proxy API (r0ld3x approach). No cookie needed."""
    normalized = normalize_url(url)
    logger.info(f"[proxy-api] Trying: {normalized}")
    try:
        page_resp = requests.get(normalized, timeout=15, allow_redirects=True)
        thumbnail = None
        if page_resp.status_code == 200:
            thumbnail = _find_between(page_resp.text, 'og:image" content="', '"')

        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0",
            "Accept": "application/json, text/plain, */*",
            "Content-Type": "application/json",
            "Origin": "https://ytshorts.savetube.me",
            "Sec-Fetch-Dest": "empty",
            "Sec-Fetch-Mode": "cors",
            "Sec-Fetch-Site": "same-origin",
        }
        resp = requests.post(
            "https://ytshorts.savetube.me/api/v1/terabox-downloader",
            headers=headers, json={"url": normalized}, timeout=30,
        )
        logger.info(f"[proxy-api] Status: {resp.status_code}")
        if resp.status_code != 200:
            return None

        data = resp.json()
        responses = data.get("response", [])
        if not responses:
            return None

        resolutions = responses[0].get("resolutions", {})
        if not resolutions:
            return None

        fast_download = resolutions.get("Fast Download", "")
        hd_video = resolutions.get("HD Video", "")
        download_link = hd_video or fast_download
        if not download_link:
            return None

        file_name = None
        content_length = 0
        try:
            head_resp = requests.head(download_link, timeout=15, allow_redirects=True)
            content_length = int(head_resp.headers.get("Content-Length", 0))
            disposition = head_resp.headers.get("content-disposition", "")
            if disposition:
                fname_match = re.findall('filename="(.+)"', disposition)
                if fname_match:
                    file_name = fname_match[0]
        except Exception as e:
            logger.warning(f"[proxy-api] HEAD failed: {e}")

        direct_link = download_link
        try:
            redir = requests.head(fast_download or download_link, timeout=15, allow_redirects=False)
            if redir.headers.get("location"):
                direct_link = redir.headers["location"]
        except Exception:
            pass

        return {
            "file_name": file_name or responses[0].get("title", "video.mp4"),
            "download_link": direct_link or download_link,
            "hd_link": hd_video,
            "fast_link": fast_download,
            "thumbnail": thumbnail or responses[0].get("thumbnail", ""),
            "file_size": _format_size(content_length),
            "sizebytes": content_length,
        }
    except Exception as e:
        logger.error(f"[proxy-api] Error: {e}")
        return None


def get_via_direct_api(url: str, cookie: str) -> dict | None:
    """Strategy 2: Direct TeraBox API with cookie."""
    normalized = normalize_url(url)
    logger.info(f"[direct-api] Trying: {normalized}")
    try:
        session = requests.Session()
        headers = {
            "Cookie": cookie,
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        }

        resp = session.get(normalized, headers=headers, timeout=20, allow_redirects=True)
        final_url = resp.url
        host = urlparse(final_url).netloc

        # Update Host header to match redirected domain
        headers["Host"] = host

        text = resp.text
        logid = _find_between(text, "dp-logid=", "&")
        js_token = _find_between(text, "fn%28%22", "%22%29")

        parsed = urlparse(final_url)
        shorturl = parse_qs(parsed.query).get("surl", [None])[0]
        if not shorturl:
            path = parsed.path
            if "/s/" in path:
                shorturl = path.split("/s/")[1].lstrip("1")

        if not shorturl or not js_token:
            logger.warning(f"[direct-api] Missing shorturl={shorturl} jsToken={bool(js_token)}")
            return None

        api_url = (
            f"https://{host}/share/list?app_id=250528&web=1&channel=0"
            f"&jsToken={js_token}&dp-logid={logid}&page=1&num=20&by=name"
            f"&order=asc&shorturl={shorturl}&root=1"
        )
        resp2 = session.get(api_url, headers=headers, timeout=15)
        if resp2.status_code != 200:
            return None

        data = resp2.json()
        if data.get("errno") or data.get("code"):
            logger.warning(f"[direct-api] API error: errno={data.get('errno')} code={data.get('code')} msg={data.get('errmsg')}")
            return None

        file_list = data.get("list", [])
        if not file_list:
            return None

        item = file_list[0]
        dlink = item.get("dlink", "")
        direct_link = dlink
        try:
            head = session.head(dlink, headers=headers, timeout=15)
            if head.headers.get("location"):
                direct_link = head.headers["location"]
        except Exception:
            pass

        return {
            "file_name": item.get("server_filename", "file"),
            "download_link": direct_link or dlink,
            "thumbnail": item.get("thumbs", {}).get("url3", ""),
            "file_size": _format_size(int(item.get("size", 0))),
            "sizebytes": int(item.get("size", 0)),
        }
    except Exception as e:
        logger.error(f"[direct-api] Error: {e}")
        return None


def get_download_info(url: str, cookie: str = "") -> dict:
    """
    Main entry point. Tries proxy API first, falls back to direct API.
    Returns dict with file info or {'error': '...'} on failure.
    """
    if not is_terabox_url(url):
        return {"error": "Not a valid TeraBox URL"}

    result = get_via_proxy_api(url)
    if result:
        logger.info(f"[resolver] Success via proxy API: {result['file_name']}")
        return result

    if cookie:
        result = get_via_direct_api(url, cookie)
        if result:
            logger.info(f"[resolver] Success via direct API: {result['file_name']}")
            return result

    return {"error": "Could not extract download link. The link may be expired, invalid, or the file was removed."}
