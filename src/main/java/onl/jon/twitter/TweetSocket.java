package onl.jon.twitter;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/tweetSocket")
public class TweetSocket {

    private static final Set<Session> clients = new HashSet<Session>();

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        sendToAll(message);
    }

    public static void sendToAll(String message) throws IOException {
        synchronized (clients) {
            for (Session client : clients) {
                client.getBasicRemote().sendText(message);
            }
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        synchronized (clients) {
            clients.add(session);
        }
    }

    @OnClose
    public void onClose(Session session) {
        synchronized (clients) {
            clients.remove(session);
        }
    }

    @OnError
    public void onError(Throwable cause) {
        cause.printStackTrace(System.err);
    }
}
