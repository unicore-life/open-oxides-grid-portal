package pl.edu.icm.oxides.portal.feed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.icm.oxides.config.FeedProperties;
import pl.edu.icm.oxides.config.FeedsConfig;

import java.util.List;

import static pl.edu.icm.oxides.portal.feed.FeedsResource.EMPTY_FEEDS_RESPONSE;

@RestController
public class FeedsRestEndpoint {
    private final FeedsResource feedsResource;
    private final FeedsConfig feedsConfig;

    @Autowired
    public FeedsRestEndpoint(FeedsResource feedsResource, FeedsConfig feedsConfig) {
        this.feedsResource = feedsResource;
        this.feedsConfig = feedsConfig;
    }

    @RequestMapping("/feeds/{feedsIndex}")
    public FeedsResponse getFeedEntries(@PathVariable(value = "feedsIndex") Integer feedsIndex) {
        final List<FeedProperties> feedEntries = feedsConfig.getSources();
        if (feedsIndex >= 0 && feedsIndex < feedEntries.size()) {
            return feedsResource.getFeeds(feedEntries.get(feedsIndex).getUrl());
        }
        return EMPTY_FEEDS_RESPONSE;
    }
}
