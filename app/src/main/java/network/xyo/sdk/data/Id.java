package network.xyo.sdk.data;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by arietrouw on 2/20/18.
 */

public class Id extends Simple {
    public String domain;
    public String value;

    public Id() {
        this.type = 0x1003;
    }

    public Id(ByteBuffer buffer, int offset) {
        super(buffer, offset);
        offset += super.getLength();
        this.domain = getUtf8String(buffer, offset);
        offset += this.domain.length();
        this.value = getUtf8String(buffer, offset);
    }

    @Override
    public int toBuffer(ByteBuffer buffer, int offset) {
        offset += super.toBuffer(buffer, offset);
        offset += putUtf8String(buffer, offset, domain);
        offset += putUtf8String(buffer, offset, value);

        return offset;
    }
}
