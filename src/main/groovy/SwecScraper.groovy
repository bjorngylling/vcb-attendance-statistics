import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class SwecScraper extends Configuration {
    def connection = Jsoup.connect(siteUrl)

    def scrapeGameIds() {
        def gameIds = []
        for (def i = 1; ; i++) {
            Document doc;
            try {
                doc = connection.url(siteUrl + serverUrl + i).get()
            }
            catch (HttpStatusException e) {
                break; // This could mean we're fucked or the host is nice enough to indicate we've reached the last page.
            }

            def elements = doc.select("#table > table > tbody > tr")
            if (elements.size() == 1) {
                break; // No games in the list, time to do other stuff
            }

            // First row is the header so we skip immediately to the second row
            elements.subList(1, elements.size() - 1).each {
                def td = it.getElementsByTag("td")
                def playerCount = td.get(1).text().split('/').first()

                if (playerCount.toInteger() > playerCountLimit)
                {
                    def e = td.first().getElementsByTag("a").attr("href")
                    gameIds.add(e.substring(gameUrl.size() + 1)) // Remove /game/data/ and only the ID remains
                }
            }
        }
        gameIds
    }

    def scrapeGames(gameIds) {
        def playerDb = [:]
        gameIds.each { gameId ->
            Document doc = connection.url(siteUrl + gameUrl + gameId).get()
            def players = doc.select("#player_data > table > tbody > tr")

            players.subList(1, players.size()).each { playerRow ->
                def style = playerRow.select("td:nth-child(4) > div").attr("style")
                def playerName = playerRow.select("td:nth-child(1)").text()

                def attendancePercentage = (style =~ /width: (.*)%;/)[0].last()
                if (attendancePercentage.toInteger() > attendancePercentLimit)
                {
                    def playerGames = playerDb.get(playerName)
                    if (playerGames == null)
                    {
                        playerGames = []
                    }
                    playerGames.add(gameId)
                    playerDb.put(playerName, playerGames)
                }
            }
        }
        playerDb
    }
}
