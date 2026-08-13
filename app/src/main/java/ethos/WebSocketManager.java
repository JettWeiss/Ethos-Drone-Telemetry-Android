package ethos;

import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";

    private final String serverUrl;
    private final OkHttpClient client;
    private WebSocket webSocket;

    public WebSocketManager(String serverUrl) {
        this.serverUrl = serverUrl;
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .pingInterval(20, TimeUnit.SECONDS)
                .build();
    }

    public void connect() {
        Request request = new Request.Builder().url(serverUrl).build();

        WebSocketListener listener = new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // Connected
            }
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // Received message
            }
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                // Connection failed
            }
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                // Connection closed
            }
        };

        webSocket = client.newWebSocket(request, listener);
    }

    public boolean send(String message) {
        if (webSocket == null) return false;
        return webSocket.send(message);
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Client closing normally");
            webSocket = null;
        }
    }

    public void shutdown() {
        disconnect();
        client.dispatcher().executorService().shutdown();
    }
}