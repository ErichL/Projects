#!/usr/bin/python
import praw
import urllib.request
import os
import json

def get_resolution(url):
    if "imgur" in url:
        imageid =  url.split('/')[-1].split('.')[0]
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
        return (10000,10000)

reddit = praw.Reddit('bot1')
subreddits = ["earthporn"]
num_posts = 10
min_score = 10
min_width = 1920
min_height = 1080
path = "C:\\Users\\Erich\\Pictures"
title = ""

if os.path.exists(path):
    if os.path.isdir(path):
        for subreddit in subreddits:
            subreddit = reddit.subreddit("earthporn")
            for submission in subreddit.hot(limit=num_posts):
                if submission.score > min_score:
                    if title in submission.title:
                        print("Title: ", submission.title)
                        print("Score: ", submission.score)
                        print("URL: ", submission.url)
                        url = submission.url
                        res = get_resolution(url)
                        if res[0] > min_width and res[1] > min_height:
                            if url.split('.')[-1] == "jpg":
                                filename = url.split('/')[-1]
                                #urllib.request.urlretrieve(url, path + "/" + filename)
                            print("---------------------------------\n")
    else:
        sys.exit("No a directory")
else:
    sys.exit("No such directory")

