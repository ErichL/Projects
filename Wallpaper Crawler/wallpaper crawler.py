#!/usr/bin/python
import praw
import urllib.request
import os

reddit = praw.Reddit('bot1')
subreddits = ["earthporn"]
nPosts = 10
minScore = 10
path = "C:\\Users\\Erich\\Pictures"

if os.path.exists(path):
    if os.path.isdir(path):
        for subreddit in subreddits:
            subreddit = reddit.subreddit("earthporn")
            for submission in subreddit.hot(limit=nPosts):
                if submission.score > minScore:
                    print("Title: ", submission.title)
                    print("Score: ", submission.score)
                    print("Score: ", submission.url)
                    url = submission.url
                    if url.split('.')[-1] == "jpg":
                        filename = url.split('/')[-1]
                        urllib.request.urlretrieve(url, path + "/" + filename)
                    print("---------------------------------\n")
                    #Resolution check here
    else:
        sys.exit("No a directory")
else:
    sys.exit("No such directory")
    
def download(url):
    name = random.randrange(1,1000)
    full_name = str(name) + ".png"
    urllib.request.urlretrieve(url,full_name)
