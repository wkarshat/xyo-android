package network.xyo.sdk.nodes;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import network.xyo.sdk.data.Entry;
import network.xyo.sdk.data.Simple;

public class Node {

    private static String TAG = "Node";

    private static final List<Node> _nodes = new ArrayList<>();

    private static final Map<String, Node> _nodeMap = new HashMap<>();

    private ThreadPoolExecutor _threadPool;

    public static void init(Context context) {
        add(new Sentinel(context, "localhost", (short)21456, (short)25456));
        add(new Bridge(context, "localhost", (short)22456, (short)26456));
        add(new Archivist(context, "10.0.2.2", (short)23456, (short)27456));
    }

    private static Node add(Node node) {
        _nodes.add(node);
        _nodeMap.put(node.getName(), node);
        return node;
    }

    public static Node get(String name) {
        return _nodeMap.get(name);
    }

    public static List<Node> get() {
        return _nodes;
    }

    public final String host;
    public final int apiPort;
    public final int pipePort;
    public final String id;
    public final Context context;

    private Thread serverThread;

    public Node(Context context, String host, short apiPort, short pipePort) {
        this.context = context;
        this.host = host;
        this.apiPort = apiPort;
        this.pipePort = pipePort;
        this.id = host + ":" + pipePort;
        startServer();
        _threadPool = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    private void startServer() {
        serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(Node.this.pipePort);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        if (socket != null) {
                            Node.this.connectInbound(socket);
                        }
                    }

                } catch (IOException ex) {
                    Log.e(TAG, ex.getLocalizedMessage());
                }
            }
        });
        serverThread.start();
    }

    protected void onEntry(Entry entry) {

    }

    private void in(byte[] data) {
        Simple obj = Simple.fromBuffer(ByteBuffer.wrap(data), 0);
        if (obj instanceof Entry) {
            onEntry((Entry)obj);
        }
    }

    private void connectInbound(final Socket socket) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    InputStream is = socket.getInputStream();
                    while (true) {
                        int count = is.available();
                        if (count > 0) {
                            final byte[] data = new byte[count];
                            is.read(data);
                            final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                                @Override
                                protected Void doInBackground(Void... params) {
                                    Node.this.in(data);
                                    return null;
                                }
                            };
                            asyncTask.executeOnExecutor(Node.this._threadPool);
                        }
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "Connection Dropped");
                }
            }
        }).start();
    }

    private void stopServer() {
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }
    }

    public void out(byte[] bytes) {
        try {
            Socket client = new Socket(host, pipePort);
            OutputStream os = client.getOutputStream();
            os.write(bytes);
        } catch (IOException ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.host);
        builder.append(":");
        builder.append(this.pipePort);
        builder.append(":");
        builder.append(this.apiPort);
        return builder.toString();
    }

    public String getName() {
        return "Node";
    }
}
