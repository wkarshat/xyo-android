package network.xyo.sdk.nodes;

import android.content.Context;

public class Bridge extends Node {

    public Bridge(Context context, String host, short apiPort, short pipePort) {
        super(context, host, apiPort, pipePort);
    }

    @Override
    public String getName() {
        return "Bridge";
    }
}