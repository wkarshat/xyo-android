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
    public ByteBuffer toBuffer(ByteBuffer buffer) {
        super.toBuffer(buffer);
        byte[] bytes = range.toByteArray();
        return buffer;
    }
}
