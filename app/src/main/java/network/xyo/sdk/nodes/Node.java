package network.xyo.sdk.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {

    private static final List<Node> _nodes = new ArrayList<>();

    private static final Map<String, Node> _nodeMap = new HashMap<>();

    static {
        add(new Sentinel("localhost", (short)21456, (short)25456));
        add(new Bridge("localhost", (short)22456, (short)26456));
        add(new Archivist("10.0.2.2", (short)23456, (short)27456));
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

    public Node(String host, short apiPort, short pipePort) {
        this.host = host;
        this.apiPort = apiPort;
        this.pipePort = pipePort;
        id = host + ":" + pipePort;
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
