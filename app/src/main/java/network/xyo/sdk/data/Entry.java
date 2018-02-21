package network.xyo.sdk.data;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class Entry extends Simple {
    public ByteBuffer[] payloads;
    public long timestamp; // UINT32
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

    public Entry() {
        this.type = 0x1005;
    }

    public Entry(ByteBuffer buffer, int offset) {
        super(buffer, offset);
        offset += super.getLength();

        this.payloads = getBufferArray(buffer, offset);
        offset += getBufferArrayLength(this.payloads);

        this.timestamp = getUnsignedInt(buffer, offset);
        offset += 4;

        this.nonce = getUnsigned256(buffer, offset);
        offset += 32;

        this.difficulty = getUnsignedShort(buffer, offset);
        offset += 2;

        this.p1keys = getBufferArray(buffer, offset);
        offset += getBufferArrayLength(this.p1keys);

        this.p2keys = getBufferArray(buffer, offset);
        offset += getBufferArrayLength(this.p2keys);

        this.p2signatures = getBufferArray(buffer, offset);
        offset += getBufferArrayLength(this.p2signatures);

        this.p1signatures = getBufferArray(buffer, offset);
        offset += getBufferArrayLength(this.p1signatures);

        this.headkeys = getBufferArray(buffer, offset);
        offset += getBufferArrayLength(this.headkeys);

        this.tailkeys = getBufferArray(buffer, offset);
        offset += getBufferArrayLength(this.tailkeys);

        this.headsignatures = getBufferArray(buffer, offset);
        offset += getBufferArrayLength(this.headsignatures);

        this.tailsignatures = getBufferArray(buffer, offset);
    }

    @Override
    public int toBuffer(ByteBuffer buffer, int offset) {
        offset += super.toBuffer(buffer, offset);

        offset += putBufferArray(buffer, offset, this.payloads);

        offset += putUnsignedInt(buffer, offset, this.timestamp);

        offset += putUnsigned256(buffer, offset, this.nonce);

        offset += putUnsignedShort(buffer, offset, this.difficulty);

        offset += putBufferArray(buffer, offset, this.p1keys);

        offset += putBufferArray(buffer, offset, this.p2keys);

        offset += putBufferArray(buffer, offset, this.p2signatures);

        offset += putBufferArray(buffer, offset, this.p1signatures);

        offset += putBufferArray(buffer, offset, this.headkeys);

        offset += putBufferArray(buffer, offset, this.tailkeys);

        offset += putBufferArray(buffer, offset, this.headsignatures);

        offset += putBufferArray(buffer, offset, this.tailsignatures);

        return offset;
    }
}
