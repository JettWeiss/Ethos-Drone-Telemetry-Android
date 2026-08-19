package ethos;

import android.util.Log;

import java.util.concurrent.TimeUnit;
import okhttp3.*;
import okio.ByteString;

public class WebSocketManager {
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
                Log.d("WebSocket", "CONNECTED");
            }
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WebSocket", "MESSAGE: " + text);
            }
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "Connection failed", t);
            }
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket", "CLOSED: " + code + " " + reason);
            }
        };

        webSocket = client.newWebSocket(request, listener);
    }

    public boolean send(String message) {
        if (webSocket == null) return false;
        return webSocket.send(message);
    }

    public boolean send(byte[] message){
        if (webSocket == null) return false;
        Log.d("CAMERA", "sent websocket");
        return webSocket.send(ByteString.of(message));
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