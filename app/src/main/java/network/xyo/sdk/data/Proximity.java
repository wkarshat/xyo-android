package network.xyo.sdk.data;

import java.math.BigInteger;
import java.nio.ByteBuffer;


public class Proximity extends Simple {
    public BigInteger range; //18 decimal places

    public Proximity() {
        this.type = 0x1002;
    }

    public Proximity(ByteBuffer buffer, int offset) {
        super(buffer, offset);
        offset += super.getLength();
        range = getUnsigned256(buffer, offset);
    }

    @Override
    public int getLength() {
        return 32 + super.getLength();
    }

    @Override
    public int toBuffer(ByteBuffer buffer, int offset) {
        offset += super.toBuffer(buffer, offset);
        byte[] bytes = range.toByteArray();
        return offset + buffer.put(bytes, offset, bytes.length).capacity();
    }
}
