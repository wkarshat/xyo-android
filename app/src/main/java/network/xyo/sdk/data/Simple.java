package network.xyo.sdk.data;

import java.io.UnsupportedEncodingException;
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
}
