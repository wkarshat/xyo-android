package network.xyo.sdk.nodes;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import network.xyo.sdk.data.Entry;
import network.xyo.sdk.data.Simple;

public class Node implements Entry.Signer {

    private static String TAG = "Node";
    private static int REQUEST_TIMEOUT = 3000;

    public interface Listener {
        void in(Node node, byte[] bytes);
        void out(Node node, byte[] bytes);
    }

    private Listener listener;

    private static final List<Node> _nodes = new ArrayList<>();
    public Node[] getNodes() {
        return _nodes.toArray(new Node[_nodes.size()]);
    }

    protected ArrayList<KeyPair> _keys = new ArrayList<>();
    protected ArrayList<Integer> _keyUses = new ArrayList<>();

    protected KeyPair[] getKeyArray() {
        return this._keys.toArray(new KeyPair[this._keys.size()]);
    }

    private static final Map<String, Node> _nodeMap = new HashMap<>();

    protected ThreadPoolExecutor _threadPool;

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
    public long totalInCount;
    public long totalOutCount;

    public ArrayList<Entry> ledger;

    private Thread serverThread;

    public Node(Context context, String host, short apiPort, short pipePort) {
        this.context = context;
        this.host = host;
        this.apiPort = apiPort;
        this.pipePort = pipePort;
        this.id = host + ":" + pipePort;
        this.ledger = new ArrayList<>();
        this.startServer();
        _threadPool = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        this.generateInitialKeys();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public ArrayList<byte[]> getPublicKeys() {
        KeyPair[] keyPairs = this.getKeyArray();
        return this.publicKeysFromKeyPairs(keyPairs);
    }

    private int getKeyUses(int index) {
        return (index + 1) * (index + 1);
    }

    protected void startServer() {
        serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(Node.this.pipePort);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        if (socket != null) {
                            Node.this.in(socket);
                        }
                    }

                } catch (IOException ex) {
                    Log.e(TAG, ex.getLocalizedMessage());
                }
            }
        });
        serverThread.start();
    }

    protected void addEntryToLedger(Entry entry) {
        if (ledger.size() > 0) {
            this.signHeadAndTail(entry);
        }
        this.ledger.add(entry);
    }

    protected ArrayList<byte[]> publicKeysFromKeyPairs(KeyPair[] keyPairs) {
        ArrayList<byte[]> result = new ArrayList<>();
        for (int i = 0; i < keyPairs.length; i++) {
            result.add(keyPairs[i].getPublic().getEncoded());
        }
        return result;
    }

    protected void signHeadAndTail(Entry entry) {
        KeyPair[] headKeyPairs = this.getKeyArray();
        this.spinKeys();
        KeyPair[] tailKeyPairs = (this.getKeyArray());
        entry.headKeys = this.publicKeysFromKeyPairs(headKeyPairs);
        entry.tailKeys = this.publicKeysFromKeyPairs(tailKeyPairs);

        byte[] payload = entry.toBuffer().array();

        this.signHead(entry, payload, headKeyPairs);
        this.signTail(entry, payload, tailKeyPairs);
    }

    protected void signHead(Entry entry, byte[] payload, KeyPair[] keyPairs) {
        entry.headSignatures = this.sign(payload, keyPairs);
    }

    protected void signTail(Entry entry, byte[] payload, KeyPair[] keyPairs) {
        entry.tailSignatures = this.sign(payload, keyPairs);
    }

    protected void spinKeys() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            for (int i = 0; i < _keyUses.size(); i++) {
                _keyUses.set(i, _keyUses.get(i) + 1);
                if (_keyUses.get(i) >= getKeyUses(i)) {
                    _keys.set(i, kpg.generateKeyPair());
                    _keyUses.set(i, 0);
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            Log.e(TAG, "No RSA available");
        }
    }

    private void generateInitialKeys() {
        _keys = new ArrayList<>();
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            _keys.add(kpg.generateKeyPair());
            _keyUses.add(0);
            _keys.add(kpg.generateKeyPair());
            _keyUses.add(0);
            _keys.add(kpg.generateKeyPair());
            _keyUses.add(0);
        } catch (NoSuchAlgorithmException ex) {
            Log.e(TAG, "No RSA available");
        }
    }

    public ArrayList<byte[]> sign(byte[] payload) {
        KeyPair[] keyPairs = this.getKeyArray();
        return this.sign(payload, keyPairs);
    }

    public ArrayList<byte[]> sign(byte[] payload, KeyPair[] keyPairs) {
        ArrayList<byte[]> result = new ArrayList<>();
        try {
            for (int i = 0; i < keyPairs.length; i++) {
                Signature signature = Signature.getInstance("MD5WithRSA");
                signature.initSign(keyPairs[i].getPrivate());
                signature.update(payload);
                byte[] signatureBytes = signature.sign();
                result.add(signatureBytes);
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("No RSA");
        } catch (InvalidKeyException ex) {
            throw new RuntimeException("Invalid Keys");
        } catch (SignatureException ex) {
            throw new RuntimeException("Signing Failed");
        }
        return result;
    }

    protected void onEntry(Entry entry) {

    }

    private void stopServer() {
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }
    }

    protected void in(Socket socket) {
        try {
            long startTime = SystemClock.elapsedRealtime();
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            while (dis.available() < 4) {
                if (SystemClock.elapsedRealtime() > startTime + REQUEST_TIMEOUT) {
                    throw new TimeoutException("Node Timed Out");
                } else {
                    Thread.sleep(1000);
                }
            }
            byte[] lengthBytes = new byte[4];
            dis.read(lengthBytes, 0, 4);
            int length = (0xff & lengthBytes[0]) * 256 * 256 * 256 + (0xff & lengthBytes[1]) * 256 * 256 + (0xff & lengthBytes[2]) * 256 + (0xff & lengthBytes[3]);
            if (length > 4) {
                while (dis.available() < length) {
                    if (SystemClock.elapsedRealtime() > startTime + REQUEST_TIMEOUT) {
                        throw new TimeoutException("Node Timed Out");
                    } else {
                        Thread.sleep(1000);
                    }
                }

                Log.i(TAG, "out-in: " + dis.available());
                byte[] replyBytes = new byte[length];
                dis.read(replyBytes, 0, length);
                totalInCount += replyBytes.length;
                if (this.listener != null) {
                    this.listener.in(this, replyBytes);
                }
                Simple simple = Simple.fromBytes(replyBytes);
                if (simple instanceof Entry) {
                    Entry entry = (Entry)simple;
                    onEntry(entry);
                    this.out(socket, entry.toBytes());
                }
            } else {
                Log.i(TAG, "out-socket close");
                socket.close();
            }
        } catch (TimeoutException ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        } catch (IOException ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        } catch (InterruptedException ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    protected void out(Socket socket, byte[] bytes) {
        Log.i(TAG, "out: " + bytes.length);
        try {
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            os.writeInt(bytes.length);
            os.write(bytes, 0, bytes.length);
            totalOutCount += bytes.length;
            if (this.listener != null) {
                this.listener.out(this, bytes);
            }
            this.in(socket);

        } catch (IOException ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    protected void out(Node target, byte[] bytes) {
        try {
            InetAddress[] address = InetAddress.getAllByName(host);

            Socket socket = new Socket(address[0], target.pipePort);

            this.out(socket, bytes);

        } catch (UnknownHostException ex) {
            Log.e(TAG, ex.getLocalizedMessage());
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

    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.host);
        builder.append(":");
        builder.append(this.pipePort);
        return builder.toString();
    }
}
