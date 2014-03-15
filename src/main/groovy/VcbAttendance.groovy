class Main extends Configuration {
    def calcAttendance() {
        def scraper = new SwecScraper()
        def gameIds = scraper.scrapeGameIds()

        print "Number of games: " + gameIds.size() + ".\n"

        printResults(scraper.scrapeGames(gameIds), gameIds.size())
    }

    def printResults(playerDb, gameCount) {
        playerDb.each { name, games ->
            print "${name}: ${games.size()} (${(games.size() / gameCount * 100).toInteger()}%) \n"
        }
    }
}

def main = new Main()
main.calcAttendance()