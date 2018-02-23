package network.xyo.sdk.data;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import network.xyo.sdk.Base;

/* Types */
/* =============
0x1001 = Simple
0x1002 = Proximity
0x1003 = Id
0x1004 = Location
0x1005 = Entry

================ */


public class Simple extends Base {
    private static String TAG = "Simple";
    public int type = 0x1001; //unsigned short

    public Simple() {

    }

    public Simple(ByteBuffer buffer) {
        this.type = getUnsignedShort(buffer);
    }

    public static short getUnsignedByte(ByteBuffer buffer) {
        return ((short) (buffer.get() & (short) 0xff));
    }

    public static ByteBuffer putUnsignedByte(ByteBuffer buffer, int value) {
        buffer.put((byte) (value & 0xff));
        return buffer;
    }

    // ---------------------------------------------------------------

    public static int getUnsignedShort(ByteBuffer buffer) {
        return (buffer.getShort() & 0xffff);
    }

    public static ByteBuffer putUnsignedShort(ByteBuffer buffer, int value) {
        buffer.putShort((short) (value & 0xffff));
        return buffer;
    }

    // ---------------------------------------------------------------

    public static long getUnsignedInt(ByteBuffer buffer) {
        return ((long) buffer.getInt() & 0xffffffffL);
    }

    public static ByteBuffer putUnsignedInt(ByteBuffer buffer, long value) {
        buffer.putInt((int) (value & 0xffffffffL));
        return buffer;
    }

    // ----------------------------------------------------------------

    public static BigInteger getUnsigned256(ByteBuffer buffer) {
        byte[] bytes = new byte[32];
        buffer.get(bytes, 0, bytes.length);
        return new BigInteger(bytes);
    }

    public static ByteBuffer putUnsigned256(ByteBuffer buffer, BigInteger value) {
        byte[] bytes = value.toByteArray();
        byte[] paddedBytes = new byte[32];

        int start = 32 - bytes.length;

        if (start < 0) {
            throw new IndexOutOfBoundsException();
        }

        for (int i = 0; i < bytes.length; i++) {
            paddedBytes[start + i] = bytes[i];
        }

        buffer.put(paddedBytes, 0, paddedBytes.length);
        return buffer;
    }

    // ----------------------------------------------------------------

    public static BigInteger getSigned256(ByteBuffer buffer) {
        byte[] bytes = new byte[32];
        buffer.get(bytes, 0, bytes.length);
        return new BigInteger(bytes);
    }

    public static ByteBuffer putSigned256(ByteBuffer buffer, BigInteger value) {
        byte[] bytes = value.toByteArray();
        byte[] paddedBytes = new byte[32];

        int start = 32 - bytes.length;

        if (start < 0) {
            throw new IndexOutOfBoundsException();
        }

        for (int i = 0; i < bytes.length; i++) {
            paddedBytes[start + i] = bytes[i];
        }

        if (value.abs() != value)
        {
            for (int i = 0; i < start; i++) {
                paddedBytes[i] = -1;
            }
        }

        buffer.put(paddedBytes);
        return buffer;
    }

    // ----------------------------------------------------------------

    public static ArrayList<byte[]> getBytesArray(ByteBuffer buffer) {
        int count = getUnsignedShort(buffer);

        ArrayList<byte[]> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            byte[] bytes = getBytes(buffer);
            result.add(bytes);
        }
        return result;
    }

    public static ByteBuffer putBytesArray(ByteBuffer buffer, ArrayList<byte[]> value) {
        putUnsignedShort(buffer, value.size());

        for (int i = 0; i < value.size(); i++) {
            putBytes(buffer, value.get(i));
        }
        return buffer;
    }

    public static int getBytesArrayLength(ArrayList<byte[]> array) {
        int length = 2;
        for (int i = 0; i < array.size(); i++) {
            length += getBytesLength(array.get(i));
        }
        return length;
    }

    // ----------------------------------------------------------------

    public static String getUtf8String(ByteBuffer buffer) {
        String result;
        int length = getUnsignedShort(buffer);
        byte[] bytes = new byte[length];
        buffer.get(bytes, 0, length);
        try {
            result = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new UnsupportedOperationException();
        }
        return result;
    }

    public static ByteBuffer putUtf8String(ByteBuffer buffer, String value) {
        byte[] bytes;

        try {
            bytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new UnsupportedOperationException(ex);
        }
        putUnsignedShort(buffer, bytes.length);
        buffer.put(bytes);
        return buffer;
    }

    // ----------------------------------------------------------------

    public static byte[] getBytes(ByteBuffer buffer) {
        int length = (int)getUnsignedInt(buffer);
        byte[] bytes = new byte[length];
        buffer.get(bytes, 0, length);
        return bytes;
    }

    public static ByteBuffer putBytes(ByteBuffer buffer, byte[] bytes) {
        putUnsignedInt(buffer, bytes.length);
        buffer.put(bytes, 0, bytes.length);

        return buffer;
    }

    public static int getBytesLength(byte[] bytes) {
        return 4 + bytes.length;
    }

    public int getLength() {
        return 2;
    }

    public byte[] toBytes() {
        return toBuffer().array();
    }

    public ByteBuffer toBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(getLength());
        return toBuffer(buffer);
    }

    public ByteBuffer toBuffer(ByteBuffer buffer) {
        return putUnsignedShort(buffer, type);
    }

    static public Simple fromBytes(byte[] bytes) {
        try {
            return fromBuffer(ByteBuffer.wrap(bytes));
        } catch (Exception ex) {
            logError(Simple.class.getSimpleName(), ex.getLocalizedMessage());
        }
        return null;
    }

    static public Simple fromBuffer(ByteBuffer buffer) {
        int type = getUnsignedShort(buffer);
        buffer.position(0);
        switch (type) {
            case 0x1001:
                return new Simple(buffer);
            case 0x1002:
                return new Proximity(buffer);
            case 0x1003:
                return new Id(buffer);
            case 0x1004:
                return new Location(buffer);
            case 0x1005:
                return new Entry(buffer);
        }
        return null;
    }
}
