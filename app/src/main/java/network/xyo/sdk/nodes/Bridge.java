package network.xyo.sdk.nodes;

import android.content.Context;
import android.os.StrictMode;

import network.xyo.sdk.data.Entry;

public class Bridge extends Node {

    private static final int scanFrequency = 5000;

    private Thread serverThread;

    public Bridge(Context context, String host, short apiPort, short pipePort) {
        super(context, host, apiPort, pipePort);
    }

    protected void startServer() {
        super.startServer();
        serverThread = new Thread(new Runnable() {
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(scanFrequency);
                        _threadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                findSentinels();
                                findBridges();
                                findArchivists();
                            }
                        });
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
        });
        serverThread.start();
    }

    private void findSentinels() {
        Node[] nodes = getNodes();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] instanceof Sentinel) {
                initiateSentinelPull((Sentinel)nodes[i]);
            }
        }
    }

    private void initiateSentinelPull(Sentinel sentinel) {
        Entry entry = new Entry();
        entry.p2keys = getPublicKeys();
        byte[] bytes = entry.toBuffer().array();
        this.out(sentinel, bytes);
    }

    private void findBridges() {

    }

    private void findArchivists() {
        Node[] nodes = getNodes();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] instanceof Archivist) {
                initiateArchivistPush((Archivist)nodes[i], 10);
            }
        }
    }

    private void initiateArchivistPush(Archivist archivist, int maxEntries) {
        Entry entry = new Entry();

        Entry[] entries = ledger.toArray(new Entry[ledger.size()]);

        for (int i = 0; i < entries.length && i < maxEntries; i++) {
            entry.payloads.add(entries[i].toBytes());
        }

        entry.p2keys = getPublicKeys();
        byte[] bytes = entry.toBuffer().array();

        this.out(archivist, bytes);
    }

    @Override
    public String getName() {
        return "Bridge";
    }
}