package onl.jon.twitter;

import com.twitter.hbc.core.Client;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.server.ServerContainer;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TweetServer {

    final static int tweetsPerSecond = 5;

    public Server startSocketServer() {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        try {
            ServerContainer container = WebSocketServerContainerInitializer.configureContext(context);
            container.addEndpoint(TweetSocket.class);
            server.start();
            server.dump(System.err);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
        return server;
    }

    public static class MessageThread extends Thread {

        final BlockingQueue<String> messages;
        final int delay;

        public MessageThread(BlockingQueue<String> messages, int delay) {
            this.messages = messages;
            this.delay = delay;
        }

        public void run() {
            while (true) {
                try {

                    TweetSocket.sendToAll(messages.take());
                    Thread.sleep(delay);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final BlockingQueue<String> messages = new LinkedBlockingQueue<String>(tweetsPerSecond);
        TwitterHbc hbc = new TwitterHbc(messages);
        Client c = hbc.startStream();
        MessageThread mT = new MessageThread(messages, 1000 / tweetsPerSecond);
        TweetServer tS = new TweetServer();
        Server s = tS.startSocketServer();

        mT.run();
        s.join();
    }
}
