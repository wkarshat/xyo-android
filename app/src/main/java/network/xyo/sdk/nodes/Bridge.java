package network.xyo.sdk.nodes;

public class Bridge extends Node {

    public Bridge(String host, short apiPort, short pipePort) {
        super(host, apiPort, pipePort);
    }

    @Override
    public String getName() {
        return "Bridge";
    }
}