package pl.edu.icm.oxides.portal.feed;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.edu.icm.oxides.config.FeedsConfig;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
class FeedsResource {
    private final Map<String, FeedsResponse> feedsResponses = new ConcurrentHashMap();
    private final FeedsConfig feedsConfig;

    @Autowired
    FeedsResource(FeedsConfig feedsConfig) {
        this.feedsConfig = feedsConfig;
    }

    FeedsResponse getFeeds(String url) {
        return feedsResponses.getOrDefault(url, EMPTY_FEEDS_RESPONSE);
    }

    @Scheduled(fixedDelay = FIVE_HOURS_IN_MILLIS)
    public void updateFeedSources() {
        feedsConfig.getSources().stream()
                .forEach(feedsSource -> {
                    final FeedsResponse feedsResponse = new FeedsResponse();

                    final String feedsSourceUrl = feedsSource.getUrl();
                    final int feedsSourceCount = feedsSource.getCount();

                    try {
                        retrieveFeeds(feedsSourceUrl, feedsSourceCount, feedsResponse);

                        feedsResponses.put(feedsSourceUrl, feedsResponse);
                        log.info("Fetched feeds for URL: " + feedsSourceUrl);
                    } catch (IOException | FeedException ex) {
                        log.warn("Could not retrieve feeds for: " + feedsSourceUrl, ex);
                    }
                });
    }

    private void retrieveFeeds(String url, int count, FeedsResponse feedsResponse) throws IOException, FeedException {
        final URL feedUrl = new URL(url);

        SyndFeedInput syndFeedInput = new SyndFeedInput();
        SyndFeed feed = syndFeedInput.build(new XmlReader(feedUrl));

        List<SyndEntry> entries = feed.getEntries();

        int upperBound = entries.size();
        if (count > 0) {
            upperBound = Math.min(count, upperBound);
        }

        entries.subList(0, upperBound).stream()
                .forEach(syndEntry -> {
                    final String title = syndEntry.getTitle();
                    final String link = syndEntry.getLink();
                    final String content = syndEntry.getContents().isEmpty() ? "" : syndEntry.getContents().get(0).getValue();
                    final String description = syndEntry.getDescription().getValue();

                    feedsResponse.addFeedEntry(title, link, content, description);
                });
    }

    private Log log = LogFactory.getLog(FeedsResource.class);

    static final FeedsResponse EMPTY_FEEDS_RESPONSE = new FeedsResponse();
    private static final long FIVE_HOURS_IN_MILLIS = 5L * 60L * 60L * 1000L;
}
