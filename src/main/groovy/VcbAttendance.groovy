class Main extends Configuration {
    def calcAttendance() {
        def scraper = new SwecScraper("265")
        def games = scraper.scrapeGames()

        print "Number of games: " + games.size() + ".\n"

        def gameIds = games.collect { game ->
            game.id
        }
        printResults(scraper.scrapePlayers(gameIds), games.size())
    }

    def printResults(playerDb, gameCount) {
        playerDb.each { name, games ->
            print "${name}: ${games.size()} (${(games.size() / gameCount * 100).toInteger()}%) \n"
        }
    }
}

def main = new Main()
main.calcAttendance()