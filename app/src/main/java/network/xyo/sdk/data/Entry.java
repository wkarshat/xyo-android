package network.xyo.sdk.data;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class Entry extends Simple {
    public ByteBuffer[] payloads;
    public int timestamp; // UINT32
    public BigInteger nonce; // UINT256
    public int difficulty; // UINT16
    public ByteBuffer[] p1keys; // 220
    public ByteBuffer[] p2keys; // 220
    public ByteBuffer[] p2signatures; // 128
    public ByteBuffer[] p1signatures; // 128
    public ByteBuffer[] headkeys; // 220
    public ByteBuffer[] tailkeys; // 220
    public ByteBuffer[] headsignatures; // 128
    public ByteBuffer[] tailsignatures; // 128
}
