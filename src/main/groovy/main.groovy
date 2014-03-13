import org.jsoup.HttpStatusException
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

def siteUrl = "http://arma3.swec.se/"
def playerCountLimit = 16
def gameUrl = "game/data/"
def serverUrl = "server/data/265?page="

def playerDb = {}
def connection = Jsoup.connect(siteUrl)

def gameIds = [];
for (i = 1; ; i++) {
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
            e = td.first().getElementsByTag("a").attr("href")
            gameIds.add(e.substring(11)) // Remove /game/data and only the ID remains
        }
    }
}

print "Number of games: " + gameIds.size() + ".\n"

gameIds.each { id ->
    Document doc = connection.url(siteUrl + gameUrl + id).get()
    def players = doc.select("#player_data > table > tbody > tr")
}


