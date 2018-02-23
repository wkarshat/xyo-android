package network.xyo.sdk.data;

import java.math.BigInteger;
import java.nio.ByteBuffer;


public class Proximity extends Simple {
    private static String TAG = "Proximity";
    public BigInteger range; //18 decimal places

    public Proximity() {
        this.type = 0x1002;
    }

    public Proximity(ByteBuffer buffer) {
        super(buffer);
        range = getUnsigned256(buffer);
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
