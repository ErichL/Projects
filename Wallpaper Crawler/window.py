import sys
from PyQt5.QtWidgets import (QMainWindow, QFrame, QApplication, QLabel,
                             QLineEdit, QGridLayout, QFileDialog, QPushButton)
from crawler import Crawler


class WallpaperCrawler(QMainWindow):
    def __init__(self):
        super().__init__()
        self.initUI()

    def initUI(self):
        self.settings = SettingsBox(self)
        self.setCentralWidget(self.settings)
        self.statusbar = self.statusBar()
        self.resize(600, 100)
        self.setWindowTitle('Wallpaper Crawler')
        self.show()


class SettingsBox(QFrame):
    def __init__(self, parent):
        super().__init__(parent)
        self.crawler = Crawler()
        self.initUI()
        self.load()

    def initUI(self):
        title = QLabel('Title')
        subreddits = QLabel('Subreddits')
        n_posts = QLabel('Number of Posts')
        min_score = QLabel('Minimum Score')
        folder = QLabel('Folder')
        resolution = QLabel('Resolution')
        width = QLabel('Width')
        height = QLabel('Height')

        self.titleEdit = QLineEdit()
        self.subredditsEdit = QLineEdit()
        self.postsEdit = QLineEdit()
        self.scoreEdit = QLineEdit()
        self.folderEdit = QLineEdit()
        self.widthEdit = QLineEdit()
        self.heightEdit = QLineEdit()

        folder_btn = QPushButton("Browse...", self)
        folder_btn.clicked.connect(self.show_dialog)

        defaults_btn = QPushButton("Load Defaults", self)
        defaults_btn.clicked.connect(self.l_default)

        save_btn = QPushButton("Save", self)
        save_btn.clicked.connect(self.save_settings)

        run_btn = QPushButton("Run", self)
        run_btn.clicked.connect(self.run)

        grid = QGridLayout()
        grid.setSpacing(10)

        grid.addWidget(title, 1, 0)
        grid.addWidget(self.titleEdit, 1, 1, 1, 4)

        grid.addWidget(subreddits, 2, 0)
        grid.addWidget(self.subredditsEdit, 2, 1, 1, 4)

        grid.addWidget(n_posts, 3, 0)
        grid.addWidget(self.postsEdit, 3, 1, 1, 4)

        grid.addWidget(min_score, 4, 0)
        grid.addWidget(self.scoreEdit, 4, 1, 1, 4)

        grid.addWidget(folder, 5, 0)
        grid.addWidget(self.folderEdit, 5, 1, 1, 3)
        grid.addWidget(folder_btn, 5, 4, 1, 1)

        grid.addWidget(resolution, 6, 0)
        grid.addWidget(width, 6, 1)
        grid.addWidget(self.widthEdit, 6, 2)
        grid.addWidget(height, 6, 3)
        grid.addWidget(self.heightEdit, 6, 4)

        grid.addWidget(defaults_btn, 7, 0)
        grid.addWidget(save_btn, 7, 1)
        grid.addWidget(run_btn, 7, 4)

        self.setLayout(grid)

    def show_dialog(self):
        directory = QFileDialog.getExistingDirectory(self, "Select Directory")
        self.folderEdit.setText(str(directory))

    def set_settings(self):
        self.crawler.config.title = self.titleEdit.text()
        self.crawler.config.subreddits = self.subredditsEdit.text()
        self.crawler.config.num_posts = int(self.postsEdit.text())
        self.crawler.config.min_score = int(self.scoreEdit.text())
        self.crawler.config.path = self.folderEdit.text()
        self.crawler.config.res = self.widthEdit.text() + ', ' + self.heightEdit.text()

    def save_settings(self):
        self.set_settings()
        self.crawler.config.save_config()

    def l_default(self):
        self.crawler.config.load_defaults()
        self.load()

    def load(self):
        self.titleEdit.setText(self.crawler.config.title)
        self.subredditsEdit.setText(', '.join(self.crawler.config.subreddits))
        self.postsEdit.setText(str(self.crawler.config.num_posts))
        self.scoreEdit.setText(str(self.crawler.config.min_score))
        self.folderEdit.setText(str(self.crawler.config.path))
        self.widthEdit.setText(str(self.crawler.config.res[0]))
        self.heightEdit.setText(str(self.crawler.config.res[1]))

    def run(self):
        self.set_settings()
        self.crawler.crawl()


if __name__ == '__main__':
    app = QApplication(sys.argv)
    wc = WallpaperCrawler()
    sys.exit(app.exec_())
