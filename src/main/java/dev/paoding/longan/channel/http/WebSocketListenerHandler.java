package dev.paoding.longan.channel.http;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebSocketListenerHandler {
    private final List<WebSocketListener> webSocketListeners = new ArrayList<>();

    public void addWebSocketListener(WebSocketListener webSocketListener) {
        webSocketListeners.add(webSocketListener);
    }

    public void onOpen(WebSocketSession session) {
        for (WebSocketListener webSocketListener : webSocketListeners) {
            if (!webSocketListener.onOpen(session)) {
                break;
            }
        }
    }

    public void onMessage(WebSocketSession session, String message) {
        for (WebSocketListener webSocketListener : webSocketListeners) {
            if (!webSocketListener.onMessage(session, message)) {
                break;
            }
        }
    }

    public void onMessage(WebSocketSession session, byte[] bytes) {
        for (WebSocketListener webSocketListener : webSocketListeners) {
            if (!webSocketListener.onMessage(session, bytes)) {
                break;
            }
        }
    }

    public void onClose(WebSocketSession session) {
        for (WebSocketListener webSocketListener : webSocketListeners) {
            if (!webSocketListener.onClose(session)) {
                break;
            }
        }
    }
}
