package network.xyo.sdk.data;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class Location extends Simple {
    public BigInteger latitude; //18 decimal places
    public BigInteger longitude; //18 decimal places
    public BigInteger altitude; //18 decimal places

    public Location() {
        this.type = 0x1004;
    }

    public Location(ByteBuffer buffer, int offset) {
        super(buffer, offset);
        offset += super.getLength();
        latitude = getUnsigned256(buffer, offset);
        offset += 32;
        longitude = getUnsigned256(buffer, offset);
        offset += 32;
        altitude = getUnsigned256(buffer, offset);
    }

    @Override
    public int toBuffer(ByteBuffer buffer, int offset) {
        offset += super.toBuffer(buffer, offset);

        offset += putUnsigned256(buffer, offset, latitude);
        offset += putUnsigned256(buffer, offset, longitude);
        offset += putUnsigned256(buffer, offset, altitude);

        return offset;
    }
}
