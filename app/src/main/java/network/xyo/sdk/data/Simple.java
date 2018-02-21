package network.xyo.sdk.data;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

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

    public static int putUnsignedByte(ByteBuffer buffer, int offset, int value) {
        buffer.put(offset, (byte) (value & 0xff));
        return offset + 1;
    }

    // ---------------------------------------------------------------

    public static int getUnsignedShort(ByteBuffer buffer, int offset) {
        return (buffer.getShort(offset) & 0xffff);
    }

    public static int putUnsignedShort(ByteBuffer buffer, int offset, int value) {
        buffer.putShort(offset, (short) (value & 0xffff));
        return offset + 2;
    }

    // ---------------------------------------------------------------

    public static long getUnsignedInt(ByteBuffer buffer, int offset) {
        return ((long) buffer.getInt(offset) & 0xffffffffL);
    }

    public static int putUnsignedInt(ByteBuffer buffer, int offset, long value) {
        buffer.putInt(offset, (int) (value & 0xffffffffL));
        return offset + 4;
    }

    // ----------------------------------------------------------------

    public static BigInteger getUnsigned256(ByteBuffer buffer, int offset) {
        byte[] bytes = new byte[32];
        buffer.get(bytes, offset, bytes.length);
        return new BigInteger(bytes);
    }

    public static int putUnsigned256(ByteBuffer buffer, int offset, BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length != 32) {
            throw new IndexOutOfBoundsException();
        }
        buffer.put(bytes, offset, bytes.length);
        return offset + bytes.length;
    }

    // ----------------------------------------------------------------

    public static ByteBuffer[] getBufferArray(ByteBuffer buffer, int offset) {
        int count = getUnsignedShort(buffer, offset);
        offset += 2;

        ByteBuffer[] result = new ByteBuffer[count];
        for (int i = 0; i < count; i++) {
            result[i] = getBuffer(buffer, offset);
            offset += result[i].capacity();
        }
        return result;
    }

    public static int putBufferArray(ByteBuffer buffer, int offset, ByteBuffer[] value) {
        offset += putUnsignedShort(buffer, offset, value.length);
        for (int i = 0; i < value.length; i++) {
            offset += putBuffer(buffer, offset, value[i]);
        }
        return offset;
    }

    public static int getBufferArrayLength(ByteBuffer[] array) {
        int length = 2;
        for (int i = 0; i < array.length; i++) {
            length += array[i].capacity();
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

    public static int putUtf8String(ByteBuffer buffer, int offset, String value) {
        byte[] bytes;

        try {
            bytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new UnsupportedOperationException(ex);
        }
        putUnsignedShort(buffer, offset, bytes.length);
        offset += 2;
        buffer.put(bytes, offset, bytes.length);
        return offset + bytes.length;
    }

    // ----------------------------------------------------------------

    public static ByteBuffer getBuffer(ByteBuffer buffer, int offset) {
        int length = (int)getUnsignedInt(buffer, offset);
        offset += 4;
        byte[] bytes = new byte[length];
        buffer.get(bytes, offset, length);
        return ByteBuffer.wrap(bytes);
    }

    public static int putBuffer(ByteBuffer buffer, int offset, ByteBuffer value) {
        byte[] bytes = buffer.array();

        putUnsignedInt(buffer, offset, buffer.capacity());
        offset += 4;
        buffer.put(bytes, offset, bytes.length);

        return offset + bytes.length;
    }

    public int getLength() {
        return 2;
    }

    public ByteBuffer toBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(getLength());
        return toBuffer(buffer);
    }

    public ByteBuffer toBuffer(ByteBuffer buffer) {
        toBuffer(buffer, 0);
        return buffer;
    }

    public int toBuffer(ByteBuffer buffer, int offset) {
        return offset + putUnsignedShort(buffer, offset, type);
    }

    static public Simple fromBuffer(ByteBuffer buffer, int offset) {
        int type = getUnsignedShort(buffer, offset);
        switch (type) {
            case 0x1001:
                return new Simple(buffer, offset);
            case 0x1002:
                return new Proximity(buffer, offset);
            case 0x1003:
                return new Id(buffer, offset);
            case 0x1004:
                return new Location(buffer, offset);
            case 0x1005:
                return new Entry(buffer, offset);
        }
        return null;
    }
}
