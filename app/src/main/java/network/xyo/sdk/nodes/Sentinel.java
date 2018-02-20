package network.xyo.sdk.nodes;

public class Sentinel extends Node {

    public Sentinel(String host, short apiPort, short pipePort) {
        super(host, apiPort, pipePort);
    }

    @Override
    public String getName() {
        return "Sentinel";
    }
}