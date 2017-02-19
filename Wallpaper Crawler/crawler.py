import sys
import mimetypes
import urllib
import praw
from PIL import ImageFile
from settings import Settings


class Crawler:
    def __init__(self):
        self.reddit = praw.Reddit('bot1')
        self.config = Settings()

    def get_resolution(self, url):
        file = urllib.request.urlopen(url)
        size = file.headers.get("content-length")
        if size:
            size = int(size)
        p = ImageFile.Parser()
        while 1:
            data = file.read(1024)
            if not data:
                break
            p.feed(data)
            if p.image:
                file.close()
                return p.image.size
                break
        file.close()
        return None

    def download(self, url):
        mimetype, encoding = mimetypes.guess_type(url)
        filename = url.split('/')[-1]
        if mimetype and mimetype.startswith('image'):
            res = self.get_resolution(url)
            if self.config.res[0] < res[0] and self.config.res[1] < res[1]:
                urllib.request.urlretrieve(url, self.config.path + "/" + filename)
                return "Downloading " + filename
            else:
                return -1
        else:
            return -1

    def crawl(self):
        for subreddit in self.config.subreddits:
            print(subreddit)
            subreddit = self.reddit.subreddit(subreddit)
            i = 0
            limit = self.config.num_posts
            for submission in subreddit.hot(limit=100):
                i += 1
                if submission.score > self.config.min_score:
                    if self.config.title in submission.title:
                        non_bmp_map = dict.fromkeys(range(0x10000, sys.maxunicode + 1), 0xfffd)
                        print("Title: ", submission.title.translate(non_bmp_map))
                        print("Score: ", submission.score)
                        print("URL: ", submission.url)
                        dl_info = self.download(submission.url)
                        print(dl_info)
                        print("--------------------------------")
                        if dl_info == -1:
                            limit += 1
                        if i == limit:
                            break
                    else:
                        limit += 1
                else:
                    limit += 1
