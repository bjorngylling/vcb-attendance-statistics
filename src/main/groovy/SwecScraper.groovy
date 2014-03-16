import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SwecScraper extends Configuration {
    private static dateFormatter = DateTimeFormatter.ofPattern(dateFormat)

    private connection = Jsoup.connect(siteUrl)
    private serverUrl = "server/data/${writer -> writer << serverId}?page=" // Lazy eval strings, FUCKING AWESOME!
    private serverId;

    SwecScraper(serverId) {
        this.serverId = serverId
    }

    public scrapeGames() {
        def games = []
        for (def i = 1; ; i++) {
            Document doc;
            try {
                doc = connection.url(siteUrl + serverUrl + i).get()
            }
            catch (HttpStatusException e) {
                // This could mean we're fucked or the host is nice enough to indicate we've reached the last page.
                break;
            }

            def elements = doc.select("#table > table > tbody > tr")
            if (elements.size() == 1) {
                break; // No games in the list, time to do other stuff
            }

            // First row is the header so we skip immediately to the second row
            games.addAll(extractGameInfo(elements.subList(1, elements.size() - 1)))
        }
        games
    }

    public scrapePlayers(gameIds) {
        def playerDb = [:]
        gameIds.each { gameId ->
            Document doc = connection.url(siteUrl + gameUrl + gameId).get()
            def players = doc.select("#player_data > table > tbody > tr")

            players.subList(1, players.size()).each { playerRow ->
                def playerName = playerRow.select("td:nth-child(1)").text()
                if (getAttendancePercentage(playerRow) > attendancePercentLimit)
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

    private static extractGameInfo(List<Element> rows) {
        def games = []
        rows.each {
            def row = it.getElementsByTag("td")
            def playerCount = getPlayerCount(row)

            if (playerCount > playerCountLimit) {
                games.add([id: getId(row),
                           missioName: getMissionName(row),
                           island: getIslandName(row),
                           playerCount: playerCount,
                           timeStart: getStartTime(row),
                           timeEnd: getEndTime(row)])
            }
        }
        games
    }

    private static getAttendancePercentage(Element playerRow) {
        def style = playerRow.select("td:nth-child(4) > div").attr("style")
        (style =~ /width: (.*)%;/)[0].last().toInteger()
    }

    private static getPlayerCount(Elements row) {
        row.get(1).text().split('/').first().toInteger()
    }

    private static getMissionName(Elements row) {
        row.get(0).text()
    }

    private static getId(Elements row) {
        // Remove /game/data/ from the url and only the ID remains
        row.first().getElementsByTag("a").attr("href").substring(gameUrl.size() + 1)
    }

    private static getIslandName(Elements row) {
        row.get(2).text()
    }

    private static getStartTime(Elements row) {
        LocalDateTime.parse(row.get(3).text(), dateFormatter)
    }

    private static getEndTime(Elements row) {
        LocalDateTime.parse(row.get(4).text(), dateFormatter)
    }
}
