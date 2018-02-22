package network.xyo.sdk.data;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/* Types */
/* =============
0x1001 = Simple
0x1002 = Proximity
0x1003 = Id
0x1004 = Location
0x1005 = Entry

================ */


public class Simple {
    public int type = 0x1001; //unsigned short

    public Simple() {

    }

    public Simple(ByteBuffer buffer, int offset) {
        this.type = getUnsignedShort(buffer, offset);
    }

    public static short getUnsignedByte(ByteBuffer buffer, int offset) {
        return ((short) (buffer.get(offset) & (short) 0xff));
    }

    public static ByteBuffer putUnsignedByte(ByteBuffer buffer, int value) {
        buffer.put((byte) (value & 0xff));
        return buffer;
    }

    // ---------------------------------------------------------------

    public static int getUnsignedShort(ByteBuffer buffer, int offset) {
        return (buffer.getShort(offset) & 0xffff);
    }

    public static ByteBuffer putUnsignedShort(ByteBuffer buffer, int value) {
        buffer.putShort((short) (value & 0xffff));
        return buffer;
    }

    // ---------------------------------------------------------------

    public static long getUnsignedInt(ByteBuffer buffer, int offset) {
        return ((long) buffer.getInt(offset) & 0xffffffffL);
    }

    public static ByteBuffer putUnsignedInt(ByteBuffer buffer, long value) {
        buffer.putInt((int) (value & 0xffffffffL));
        return buffer;
    }

    // ----------------------------------------------------------------

    public static BigInteger getUnsigned256(ByteBuffer buffer, int offset) {
        byte[] bytes = new byte[32];
        buffer.get(bytes, offset, bytes.length);
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

        buffer.put(paddedBytes, 0, bytes.length);
        return buffer;
    }

    // ----------------------------------------------------------------

    public static BigInteger getSigned256(ByteBuffer buffer, int offset) {
        byte[] bytes = new byte[32];
        buffer.get(bytes, offset, bytes.length);
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

    public static ArrayList<byte[]> getByteArray(ByteBuffer buffer, int offset) {
        int count = getUnsignedShort(buffer, offset);
        offset += 2;

        ArrayList<byte[]> result = new ArrayList<byte[]>();
        for (int i = 0; i < count; i++) {
            byte[] bytes = getBytes(buffer, offset);
            result.add(bytes);
            offset += bytes.length;
        }
        return result;
    }

    public static ByteBuffer putByteArray(ByteBuffer buffer, ArrayList<byte[]> value) {
        putUnsignedShort(buffer, value.size());
        for (int i = 0; i < value.size(); i++) {
            putBuffer(buffer, value.get(i));
        }
        return buffer;
    }

    public static int getByteArrayLength(ArrayList<byte[]> array) {
        int length = 2;
        for (int i = 0; i < array.size(); i++) {
            length += array.get(i).length;
        }
        return length;
    }

    // ----------------------------------------------------------------

    public static String getUtf8String(ByteBuffer buffer, int offset) {
        String result;
        int length = getUnsignedShort(buffer, offset);
        offset += 2;
        byte[] bytes = new byte[length];
        buffer.get(bytes, offset, length);
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

    public static byte[] getBytes(ByteBuffer buffer, int offset) {
        int length = (int)getUnsignedInt(buffer, offset);
        byte[] bytes = new byte[length];
        buffer.get(bytes, offset + 2, length);
        return bytes;
    }

    public static ByteBuffer putBuffer(ByteBuffer buffer, byte[] bytes) {
        putUnsignedInt(buffer, buffer.capacity());
        buffer.put(bytes, 0, bytes.length);

        return buffer;
    }

    public int getLength() {
        return 2;
    }

    public ByteBuffer toBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(getLength());
        return toBuffer(buffer);
    }

    public ByteBuffer toBuffer(ByteBuffer buffer) {
        return putUnsignedShort(buffer, type);
    }

    static public Simple fromBuffer(ByteBuffer buffer) {
        int type = getUnsignedShort(buffer, 0);
        switch (type) {
            case 0x1001:
                return new Simple(buffer, 0);
            case 0x1002:
                return new Proximity(buffer, 0);
            case 0x1003:
                return new Id(buffer, 0);
            case 0x1004:
                return new Location(buffer, 0);
            case 0x1005:
                return new Entry(buffer, 0);
        }
        return null;
    }
}
