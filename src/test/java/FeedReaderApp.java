import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;

/**
 * It Reads and prints any RSS/Atom feed type.
 * <p>
 *
 * @author Alejandro Abdelnur
 */
public class FeedReaderApp {

    /**
     * See: <a href="http://rometools.github.io/rome/">Rome</a>
     *
     * @param args
     */
    public static void main(String[] args) {
        boolean ok = false;
        try {
            URL feedUrl = new URL("https://unicore-life.github.io/feed");

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));

            System.out.println(feed);

            ok = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("ERROR: " + ex.getMessage());
        }

        if (!ok) {
            System.out.println();
            System.out.println("FeedReaderApp reads and prints any RSS/Atom feed type.");
            System.out.println();
        }
    }
}
