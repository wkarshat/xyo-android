package network.xyo.sdk.data;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * Created by arietrouw on 2/20/18.
 */

public class Location extends Simple {
    public BigInteger latitude; //18 decimal places
    public BigInteger longitude; //18 decimal places
    public BigInteger altitude; //18 decimal places

    @Override
    public int toBuffer(ByteBuffer buffer, int offset) {
        offset += super.toBuffer(buffer, offset);

        byte[] bytes = latitude.toByteArray();
        offset += buffer.put(bytes, offset, bytes.length).capacity();

        bytes = longitude.toByteArray();
        offset += buffer.put(bytes, offset, bytes.length).capacity();

        bytes = altitude.toByteArray();
        offset += buffer.put(bytes, offset, bytes.length).capacity();

        return offset;
    }
}
