package network.xyo.sdk.nodes;

import android.content.Context;
import android.os.SystemClock;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import network.xyo.sdk.Base;
import network.xyo.sdk.data.Entry;
import network.xyo.sdk.data.Simple;

public class Node extends Base implements Entry.Signer {

    private static int REQUEST_TIMEOUT = 100000;

    public interface Listener {
        void in(Node node, byte[] bytes);
        void out(Node node, byte[] bytes);
        void updated();
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

    public Entry getEntry(String hash) {
        return _entryMap.get(hash);
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

    public List<Entry> ledger;
    private final Map<String, Entry> _entryMap = new HashMap<>();

    private Thread serverThread;

    public Node(Context context, String host, short apiPort, short pipePort) {
        this.context = context;
        this.host = host;
        this.apiPort = apiPort;
        this.pipePort = pipePort;
        this.id = host + ":" + pipePort;
        this.ledger = new ArrayList<>();
        _threadPool = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        if (host.compareTo("localhost") == 0) {
            this.startServer();
        }
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
                    Node.this.logError(ex.getLocalizedMessage());
                }
            }
        });
        serverThread.start();
    }

    protected void addEntryToLedger(Entry entry) {
        this.signHeadAndTail(entry);
        this.ledger.add(entry);
        this._entryMap.put(String.valueOf(entry.hashCode()), entry);
        if (this.listener != null) {
            this.listener.updated();
        }
    }

    protected ArrayList<byte[]> publicKeysFromKeyPairs(KeyPair[] keyPairs) {
        ArrayList<byte[]> result = new ArrayList<>();

        for (int i = 0; i < keyPairs.length; i++) {
            RSAPublicKey pub = (RSAPublicKey) keyPairs[i].getPublic();
            byte[] modulus = pub.getModulus().toByteArray();
            if (pub.getPublicExponent().intValue() != 65537) {
                throw new RuntimeException("Invalid Exponent");
            }
            // if there are leading zeros, we have to pad
            if (modulus.length < 65) {
                logError("Bad Modulus-A!");
                byte[] paddedModulus = new byte[65];
                int offset = 65 - modulus.length;
                for (int j = 0; j < modulus.length; j++) {
                    paddedModulus[j + offset] = modulus[j];
                }
                modulus = paddedModulus;
            }
            if (modulus.length != 65) {
                logError("Bad Modulus-B!");
            }
            result.add(modulus);
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
        for (int i = 0; i < _keyUses.size(); i++) {
            _keyUses.set(i, _keyUses.get(i) + 1);
            if (_keyUses.get(i) >= getKeyUses(i)) {
                _keys.set(i, generateKeyPair());
                _keyUses.set(i, 0);
            }
        }
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(512, new SecureRandom());
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException ex){
            logError("No RSA available");
        }
        return null;
    }

    private void generateInitialKeys() {
        _keys = new ArrayList<>();
        _keyUses = new ArrayList<>();

        _keys.add(generateKeyPair());
        _keyUses.add(0);
        _keys.add(generateKeyPair());
        _keyUses.add(0);
        _keys.add(generateKeyPair());
        _keyUses.add(0);
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

    protected boolean onEntry(Entry entry) {
        return true;
    }

    private void stopServer() {
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }
    }

    private int intFromBytes(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IndexOutOfBoundsException();
        }
        return (0xff & bytes[0]) * 256 * 256 * 256 + (0xff & bytes[1]) * 256 * 256 + (0xff & bytes[2]) * 256 + (0xff & bytes[3]);
    }

    protected void in(Socket socket) {
        logInfo( "in");
        try {
            long startTime = SystemClock.elapsedRealtime();
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            logInfo( "in: " + dis.available());
            while (dis.available() < 4) {
                if (SystemClock.elapsedRealtime() > startTime + REQUEST_TIMEOUT) {
                    throw new TimeoutException("Node Timed Out");
                } else {
                    Thread.sleep(1000);
                }
            }
            //byte[] lengthBytes = new byte[4];
            //dis.read(lengthBytes, 0, 4);
            int length = dis.available(); //intFromBytes(lengthBytes);
            logInfo( "in: " + dis.available());
            if (length > 4) {
                while (dis.available() < length) {
                    if (SystemClock.elapsedRealtime() > startTime + REQUEST_TIMEOUT) {
                        throw new TimeoutException("Node Timed Out");
                    } else {
                        Thread.sleep(1000);
                    }
                }

                logInfo( "in: " + dis.available());
                byte[] replyBytes = new byte[length];
                dis.read(replyBytes, 0, length);
                totalInCount += replyBytes.length;
                if (this.listener != null) {
                    this.listener.in(this, replyBytes);
                }
                Simple simple = Simple.fromBytes(replyBytes);
                if (simple instanceof Entry) {
                    Entry entry = (Entry)simple;
                    if (!onEntry(entry)) {
                        logInfo( "in-continue");
                        this.out(socket, entry.toBytes(), entry.p1signatures.size() > 0 && entry.p2signatures.size() > 0);
                    } else {
                        logInfo( "in-close");
                        this.out(socket, entry.toBytes(), entry.p1signatures.size() > 0 && entry.p2signatures.size() > 0);
                        socket.close();
                    }
                } else {
                    throw new RuntimeException();
                }
            } else {
                logInfo( "in-socket close");
                socket.close();
            }
        } catch (TimeoutException ex) {
            logError(ex.getLocalizedMessage());
        } catch (IOException ex) {
            logError(ex.getLocalizedMessage());
        } catch (InterruptedException ex) {
            logError(ex.getLocalizedMessage());
        }
    }

    protected void out(Socket socket, byte[] bytes, boolean done) {
        logInfo( "out: " + bytes.length);
        try {
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            //os.writeInt(bytes.length);
            os.write(bytes, 0, bytes.length);
            totalOutCount += bytes.length;
            if (this.listener != null) {
                this.listener.out(this, bytes);
            }
            if (done) {
                logInfo( "out: done");
                socket.close();
            } else {
                logInfo( "out: continue");
                this.in(socket);
            }

        } catch (SocketException ex) {
            logInfo("Socket Disconnected - " + ex.getLocalizedMessage());
            try {
                socket.close();
            } catch (IOException ioex) {
                logError(ioex.getLocalizedMessage());
            }
        } catch (IOException ex) {
            logError(ex.getLocalizedMessage());
        }
    }

    protected void out(Node target, byte[] bytes, boolean done) {
        InetAddress[] address = new InetAddress[0];
        try {
            address = InetAddress.getAllByName(target.host);

            logInfo("out-started: " + address[0]);

            Socket socket = new Socket(address[0], target.pipePort);

            if (socket.isConnected()) {
                logInfo("out-connected: " + address[0]);
                this.out(socket, bytes, done);
            } else {
                logError("Failed to Connect to: " + address[0]);
            }

        } catch (UnknownHostException ex) {
            logError(ex.getLocalizedMessage());
        } catch (ConnectException ex){
            logInfo("Connection Error: " + address[0]);
            logInfo(ex.getLocalizedMessage());
        } catch (IOException ex) {
            logError(ex.getLocalizedMessage());
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
