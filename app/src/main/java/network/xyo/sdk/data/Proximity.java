package network.xyo.sdk.data;

import java.math.BigInteger;
import java.nio.ByteBuffer;


public class Proximity extends Simple {
    public BigInteger range; //18 decimal places

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
