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

    public static BigInteger float2BigInt(double value, int neededPlaces) {
        String s = String.valueOf(value);
        int pt = s.indexOf('.');
        int places = s.length() - pt;
        int placesToMove = neededPlaces - places;
        s = s.replace(".", "");
        for (int i = 0; i < placesToMove; i++) {
            s = s.concat("0");
        }
        return new BigInteger(s);
    }

    public Location(android.location.Location location) {
        this.type = 0x1004;
        this.latitude = float2BigInt(location.getLatitude(), 18);
        this.longitude = float2BigInt(location.getLongitude(), 18);
        this.altitude = float2BigInt(location.getAltitude(), 18);
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
    public int getLength() {
        return 96 + super.getLength();
    }

    @Override
    public ByteBuffer toBuffer(ByteBuffer buffer) {
        super.toBuffer(buffer);

        putSigned256(buffer, latitude);
        putSigned256(buffer, longitude);
        putSigned256(buffer, altitude);

        return buffer;
    }
}
