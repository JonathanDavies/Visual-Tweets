package onl.jon.twitter;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import java.util.concurrent.BlockingQueue;

public class TwitterHbc {

    private final BlockingQueue<String> messages;

    public TwitterHbc(BlockingQueue<String> messages) {
        this.messages = messages;
    }

    public Client startStream() throws InterruptedException {
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        Location allLocations = new Location(new Location.Coordinate(-180, -90), new Location.Coordinate(180, 90));
        endpoint.locations(Lists.newArrayList(allLocations));
        endpoint.languages(Lists.newArrayList("en"));

        Hosts host = new HttpHosts(Constants.STREAM_HOST);
        Authentication auth = new OAuth1(Settings.TWITTER_API_KEY, Settings.TWITTER_API_SECRET,
                Settings.TWITTER_ACCESS_TOKEN, Settings.TWITTER_ACCESS_SECRET);

        Client client = new ClientBuilder()
                .name("StreamClient")
                .hosts(host)
                .authentication(auth)
                .endpoint(endpoint)
                .processor(new StringDelimitedProcessor(messages))
                .build();
        client.connect();
        return client;
    }
}