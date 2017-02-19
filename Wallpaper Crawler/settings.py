import configparser


class Settings:
    def __init__(self):
        self._subreddits = ["earthporn", "pics"]
        self.num_posts = 10
        self.min_score = 10
        self._res = (1920, 1080)
        self.path = "C:/Users/Erich/Pictures/Bot"
        self.title = ""
        self.config = configparser.ConfigParser()
        self.config.read("config.ini")
        self.get_config()

    @property
    def subreddits(self):
        return self._subreddits

    @subreddits.setter
    def subreddits(self, value):
        splitter = value.split(',')
        self._subreddits = [x.strip() for x in splitter]

    @property
    def res(self):
        return self._res

    @res.setter
    def res(self, value):
        splitter = value.split(',')
        self._res = [int(x.strip()) for x in splitter]

    def load_defaults(self):
        if self.config.get('Defaults', 'subreddits'):
            self.subreddits = self.config.get('Defaults', 'subreddits')
        if self.config.get('Defaults', 'num_posts'):
            self.num_posts = self.config.getint('Defaults', 'num_posts')
        if self.config.get('Defaults', 'min_score'):
            self.min_score = self.config.getint('Defaults', 'min_score')
        if self.config.get('Defaults', 'path'):
            self.path = self.config.get('Defaults', 'path')
        if self.config.get('Defaults', 'title'):
            self.title = self.config.get('Defaults', 'title')
        if self.config.get('Defaults', 'resolution'):
            self.resolution = self.config.get('Defaults', 'resolution')

    def get_config(self):
        if self.config.get('UserSettings', 'subreddits'):
            self.subreddits = self.config.get('UserSettings', 'subreddits')
        if self.config.get('UserSettings', 'num_posts'):
            self.num_posts = self.config.getint('UserSettings', 'num_posts')
        if self.config.get('UserSettings', 'min_score'):
            self.min_score = self.config.getint('UserSettings', 'min_score')
        if self.config.get('UserSettings', 'path'):
            self.path = self.config.get('UserSettings', 'path')
        if self.config.get('UserSettings', 'title'):
            self.title = self.config.get('UserSettings', 'title')

    def save_config(self):
        self.config.set('UserSettings', 'subreddits', ', '.join(self.subreddits))
        self.config.set('UserSettings', 'num_posts', str(self.num_posts))
        self.config.set('UserSettings', 'min_score', str(self.min_score))
        self.config.set('UserSettings', 'path', self.path)
        self.config.set('UserSettings', 'title', self.title)
        cfgfile = open("config.ini", 'w')
        self.config.write(cfgfile)
        cfgfile.close()
