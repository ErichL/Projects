import sys
import json
import mimetypes
import urllib.request
import praw
from settings import Settings


class Crawler:
    def __init__(self):
        self.reddit = praw.Reddit('bot1')
        self.config = Settings()

    def get_resolution(self, url):
        if "imgur" in url:
            imageid = url.split('/')[-1].split('.')[0]
            url = "http://api.imgur.com/3/image/" + imageid
            print(url)
            request = urllib.request.Request(url)
            open_request = urllib.request.urlopen(request)
            response = open_request.read()
            img_info = json.loads(response)
            width = img_info['data']['width']
            height = img_info['data']['height']
            return width, height
        else:
            return (10000, 10000)

    def download(self, url):
        mimetype, encoding = mimetypes.guess_type(url)
        filename = url.split('/')[-1]
        if mimetype and mimetype.startswith('image'):
            #urllib.request.urlretrieve(url, self.config.path + "/" + filename)
            return "Downloading " + filename
        else:
            return -1

    def crawl(self):
        for subreddit in self.config.subreddits:
            print(subreddit)
            subreddit = self.reddit.subreddit(subreddit)
            i = 0
            limit = self.config.num_posts
            for submission in subreddit.hot(limit=None):
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
