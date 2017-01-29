package pl.edu.icm.oxides.portal.feed;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class FeedsResponse {
    private final List<FeedEntry> feeds = new ArrayList<>();

    public List<FeedEntry> getFeeds() {
        return feeds;
    }

    @JsonIgnore
    void addFeedEntry(String title, String link, String content, String description) {
        feeds.add(new FeedEntry(title, link, content, description));
    }

    public static class FeedEntry {
        private final String title;
        private final String url;
        private final String content;
        private final String description;

        public FeedEntry(String title, String url, String content, String description) {
            this.title = title;
            this.url = url;
            this.content = content;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public String getContent() {
            return content;
        }

        public String getDescription() {
            return description;
        }
    }
}
