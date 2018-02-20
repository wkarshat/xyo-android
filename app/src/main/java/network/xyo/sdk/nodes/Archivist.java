package network.xyo.sdk.nodes;

public class Archivist extends Node {

    public Archivist(String host, short apiPort, short pipePort) {
        super(host, apiPort, pipePort);
    }

    @Override
    public String getName() {
        return "Archivist";
    }
}
