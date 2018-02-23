package network.xyo.sdk.nodes;

import android.content.Context;
import android.util.Log;

import network.xyo.sdk.data.Entry;

public class Bridge extends Node {
    private static final int scanFrequency = 60000;

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
                        _threadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                findSentinels();
                                findBridges();
                                findArchivists();
                            }
                        });
                        Thread.sleep(scanFrequency);
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
        this.out(sentinel, bytes, false);
    }

    @Override
    protected boolean onEntry(Entry entry) {
        logInfo("onEntry");
        boolean done = false;
        if (entry.p2signatures.size() == 0) {
            logInfo( "P2-NOTDONE");
            entry.p2keys = this.getPublicKeys();
            entry.p2Sign(this);
        } else if (entry.p1signatures.size() == 0) {
            logInfo( "P1-DONE");
            entry.p1keys = this.getPublicKeys();
            entry.p1Sign(this);
            addEntryToLedger(entry);
            done = true;
        } else {
            logInfo( "IN-DONE");
            addEntryToLedger(entry);
            done = true;
        }
        return done;
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

        this.out(archivist, bytes, false);
    }

    @Override
    public String getName() {
        return "Bridge";
    }
}