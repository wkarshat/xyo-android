package network.xyo.sdk.data;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;

public class Entry extends Simple {

    private static String TAG = "Entry";

    public interface Signer {
        ArrayList<byte[]> sign(byte[] payload);
        ArrayList<byte[]> getPublicKeys();
    }

    public ArrayList<byte[]> payloads;
    public long timestamp; // UINT32
    public BigInteger nonce; // UINT256
    public int difficulty; // UINT16
    public ArrayList<byte[]> p1keys; // 220
    public ArrayList<byte[]> p2keys; // 220
    public ArrayList<byte[]> p2signatures; // 128
    public ArrayList<byte[]> p1signatures; // 128
    public ArrayList<byte[]> headKeys; // 220
    public ArrayList<byte[]> tailKeys; // 220
    public ArrayList<byte[]> headSignatures; // 128
    public ArrayList<byte[]> tailSignatures; // 128

    // Total = X + 4 + 32 + 2

    public Entry() {
        this.type = 0x1005;
        this.nonce = new BigInteger(32, new SecureRandom());
        this.payloads = new ArrayList<>();
        this.p1keys = new ArrayList<>();
        this.p2keys = new ArrayList<>();
        this.p2signatures = new ArrayList<>();
        this.p1signatures = new ArrayList<>();
        this.headKeys = new ArrayList<>();
        this.tailKeys = new ArrayList<>();
        this.headSignatures = new ArrayList<>();
        this.tailSignatures = new ArrayList<>();
    }

    public Entry(ByteBuffer buffer) {
        super(buffer);

        this.payloads = getBytesArray(buffer);

        this.timestamp = getUnsignedInt(buffer);

        this.nonce = getUnsigned256(buffer);

        this.difficulty = getUnsignedShort(buffer);

        this.p1keys = getBytesArray(buffer);

        this.p2keys = getBytesArray(buffer);

        this.p2signatures = getBytesArray(buffer);

        this.p1signatures = getBytesArray(buffer);

        this.headKeys = getBytesArray(buffer);

        this.tailKeys = getBytesArray(buffer);

        this.headSignatures = getBytesArray(buffer);

        this.tailSignatures = getBytesArray(buffer);
    }

    public void p1Sign(Signer signer) {
        p1keys = signer.getPublicKeys();
        p1signatures = signer.sign(toBuffer().array());
    }

    public void p2Sign(Signer signer) {
        p2keys = signer.getPublicKeys();
        p2signatures = signer.sign(toBuffer().array());
    }

    @Override
    public int getLength() {
        int length = super.getLength();
        length += 38; //static sizes
        length += getBytesArrayLength(payloads);
        length += getBytesArrayLength(p1keys);
        length += getBytesArrayLength(p2keys);
        length += getBytesArrayLength(p1signatures);
        length += getBytesArrayLength(p2signatures);
        length += getBytesArrayLength(headKeys);
        length += getBytesArrayLength(tailKeys);
        length += getBytesArrayLength(headSignatures);
        length += getBytesArrayLength(tailSignatures);
        return length;
    }

    @Override
    public ByteBuffer toBuffer(ByteBuffer buffer) {
        super.toBuffer(buffer);

        putBytesArray(buffer, this.payloads);

        putUnsignedInt(buffer, this.timestamp);

        putUnsigned256(buffer, this.nonce);

        putUnsignedShort(buffer, this.difficulty);

        putBytesArray(buffer, this.p1keys);

        putBytesArray(buffer, this.p2keys);

        putBytesArray(buffer, this.p2signatures);

        putBytesArray(buffer, this.p1signatures);

        putBytesArray(buffer, this.headKeys);

        putBytesArray(buffer, this.tailKeys);

        putBytesArray(buffer, this.headSignatures);

        putBytesArray(buffer, this.tailSignatures);

        return buffer;
    }
}
