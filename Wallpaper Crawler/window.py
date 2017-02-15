import sys
from PyQt5.QtWidgets import (QMainWindow, QFrame, QDesktopWidget, QApplication,
                             QWidget, QLabel, QLineEdit,
                             QGridLayout, QAction, QFileDialog, QPushButton)
from PyQt5.QtCore import Qt, QBasicTimer, pyqtSignal
from crawler import Crawler

class WallpaperCrawler(QMainWindow):
    def __init__(self):
        super().__init__()
        self.initUI()
        
    def initUI(self):    
        self.settings = SettingsBox(self)
        self.setCentralWidget(self.settings)

        self.statusbar = self.statusBar()
        
        self.resize(400, 100)
        self.setWindowTitle('Wallpaper Crawler')        
        self.show()
        
class SettingsBox(QFrame):
    def __init__(self, parent):
        super().__init__(parent)
        self.crawler = Crawler()
        self.initUI()
        self.l_default()
        
    def initUI(self):
        subreddits = QLabel('Subreddits')
        n_posts = QLabel('Number of Posts')
        min_score = QLabel('Minimum Score')
        folder = QLabel('Folder')

        self.subredditsEdit = QLineEdit()
        self.postsEdit = QLineEdit()
        self.scoreEdit = QLineEdit()
        self.folderEdit = QLineEdit()
        
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

        grid.addWidget(subreddits, 1, 0)
        grid.addWidget(self.subredditsEdit, 1, 1, 1, 4)

        grid.addWidget(n_posts, 2, 0)
        grid.addWidget(self.postsEdit, 2, 1, 1, 4)

        grid.addWidget(min_score, 3, 0)
        grid.addWidget(self.scoreEdit, 3, 1, 1, 4)

        grid.addWidget(folder, 4, 0)
        grid.addWidget(self.folderEdit, 4, 1, 1, 3)
        grid.addWidget(folder_btn, 4, 4, 1, 1)

        grid.addWidget(defaults_btn, 5, 0, 1, 1)
        grid.addWidget(save_btn, 5, 1, 1, 1)
        grid.addWidget(run_btn, 5, 4, 1, 1)
        
        self.setLayout(grid)

    def show_dialog(self):
        directory = QFileDialog.getExistingDirectory(self, "Select Directory")
        self.folderEdit.setText(str(directory))

    def save_settings(self):
        self.crawler.config.subreddits = self.subredditsEdit.text()
        self.crawler.config.num_posts = int(self.postsEdit.text())
        self.crawler.config.min_score = int(self.scoreEdit.text())
        self.crawler.config.path = self.folderEdit.text() 

    def l_default(self):
        self.crawler.config.load_defaults()
        self.subredditsEdit.setText(', '.join(self.crawler.config.subreddits))
        self.postsEdit.setText(str(self.crawler.config.num_posts))
        self.scoreEdit.setText(str(self.crawler.config.min_score))
        self.folderEdit.setText(str(self.crawler.config.path))

    def run(self):
        self.save_settings()
        self.crawler.crawl()
        
    
if __name__ == '__main__':
    app = QApplication(sys.argv)
    wc = WallpaperCrawler()
    sys.exit(app.exec_()) 
