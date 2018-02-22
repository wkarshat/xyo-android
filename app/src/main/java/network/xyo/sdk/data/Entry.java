package network.xyo.sdk.data;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Entry extends Simple {
    public ArrayList<byte[]> payloads;
    public long timestamp; // UINT32
    public BigInteger nonce; // UINT256
    public int difficulty; // UINT16
    public ArrayList<byte[]> p1keys; // 220
    public ArrayList<byte[]> p2keys; // 220
    public ArrayList<byte[]> p2signatures; // 128
    public ArrayList<byte[]> p1signatures; // 128
    public ArrayList<byte[]> headkeys; // 220
    public ArrayList<byte[]> tailkeys; // 220
    public ArrayList<byte[]> headsignatures; // 128
    public ArrayList<byte[]> tailsignatures; // 128

    public Entry() {
        this.type = 0x1005;
        this.payloads = new ArrayList<byte[]>();
    }

    public Entry(ByteBuffer buffer, int offset) {
        super(buffer, offset);
        offset += super.getLength();

        this.payloads = getByteArray(buffer, offset);
        offset += getByteArrayLength(this.payloads);

        this.timestamp = getUnsignedInt(buffer, offset);
        offset += 4;

        this.nonce = getUnsigned256(buffer, offset);
        offset += 32;

        this.difficulty = getUnsignedShort(buffer, offset);
        offset += 2;

        this.p1keys = getByteArray(buffer, offset);
        offset += getByteArrayLength(this.p1keys);

        this.p2keys = getByteArray(buffer, offset);
        offset += getByteArrayLength(this.p2keys);

        this.p2signatures = getByteArray(buffer, offset);
        offset += getByteArrayLength(this.p2signatures);

        this.p1signatures = getByteArray(buffer, offset);
        offset += getByteArrayLength(this.p1signatures);

        this.headkeys = getByteArray(buffer, offset);
        offset += getByteArrayLength(this.headkeys);

        this.tailkeys = getByteArray(buffer, offset);
        offset += getByteArrayLength(this.tailkeys);

        this.headsignatures = getByteArray(buffer, offset);
        offset += getByteArrayLength(this.headsignatures);

        this.tailsignatures = getByteArray(buffer, offset);
    }

    @Override
    public ByteBuffer toBuffer(ByteBuffer buffer) {
        super.toBuffer(buffer);

        putByteArray(buffer, this.payloads);

        putUnsignedInt(buffer, this.timestamp);

        putUnsigned256(buffer, this.nonce);

        putUnsignedShort(buffer, this.difficulty);

        putByteArray(buffer, this.p1keys);

        putByteArray(buffer, this.p2keys);

        putByteArray(buffer, this.p2signatures);

        putByteArray(buffer, this.p1signatures);

        putByteArray(buffer, this.headkeys);

        putByteArray(buffer, this.tailkeys);

        putByteArray(buffer, this.headsignatures);

        putByteArray(buffer, this.tailsignatures);

        return buffer;
    }
}
