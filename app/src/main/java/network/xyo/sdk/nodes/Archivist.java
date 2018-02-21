package network.xyo.sdk.nodes;

import android.content.Context;

public class Archivist extends Node {

    public Archivist(Context context, String host, short apiPort, short pipePort) {
        super(context, host, apiPort, pipePort);
    }

    @Override
    public String getName() {
        return "Archivist";
    }
}
