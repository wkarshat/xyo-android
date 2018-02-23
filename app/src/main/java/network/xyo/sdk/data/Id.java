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

    public Id(ByteBuffer buffer) {
        super(buffer);

        this.domain = getUtf8String(buffer);

        this.value = getUtf8String(buffer);
    }

    @Override
    public ByteBuffer toBuffer(ByteBuffer buffer) {
        super.toBuffer(buffer);
        putUtf8String(buffer, domain);
        putUtf8String(buffer, value);

        return buffer;
    }
}
