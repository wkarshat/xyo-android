package network.xyo.sdk.data;

import android.os.SystemClock;

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
    public long epoch; // UINT32
    public BigInteger nonce; // UINT256
    public int difficulty; // UINT16
    public ArrayList<byte[]> p1keys; // 65
    public ArrayList<byte[]> p2keys; // 65
    public ArrayList<byte[]> p2signatures; // 64
    public ArrayList<byte[]> p1signatures; // 64
    public ArrayList<byte[]> headKeys; // 65
    public ArrayList<byte[]> tailKeys; // 65
    public ArrayList<byte[]> headSignatures; // 64
    public ArrayList<byte[]> tailSignatures; // 64

    // Total = X + 4 + 32 + 2

    public Entry() {
        this.type = 0x1005;
        this.nonce = new BigInteger(255, new SecureRandom());
        this.epoch = SystemClock.elapsedRealtime();
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

        this.epoch = getUnsigned32(buffer);

        this.nonce = getUnsigned256(buffer);

        this.difficulty = getUnsigned16(buffer);

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

        putUnsigned32(buffer, this.epoch);

        putUnsigned256(buffer, this.nonce);

        putUnsigned16(buffer, this.difficulty);

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

    @Override
    public String getTypeString() {
        return "Entry (" + byteArrayToHexString(this.getTypeBytes()) + ")";
    }

    public String getEpochString() {
        ByteBuffer bytes = ByteBuffer.allocate(4);
        putUnsigned32(bytes, this.epoch);
        return byteArrayToHexString(bytes.array());
    }

    public String getNonceString() {
        ByteBuffer bytes = ByteBuffer.allocate(32);
        putUnsigned256(bytes, this.nonce);
        return byteArrayToHexString(bytes.array());
    }

    public String getDifficultyString() {
        ByteBuffer bytes = ByteBuffer.allocate(2);
        putUnsigned16(bytes, this.difficulty);
        return byteArrayToHexString(bytes.array());
    }

    public String getPayloadsString() {
        return byteArrayToHexString(payloads);
    }

    public String getP1KeysString() {
        return byteArrayToHexString(p1keys);
    }

    public String getP2KeysString() {
        return byteArrayToHexString(p2keys);
    }

    public String getP1SignaturesString() {
        return byteArrayToHexString(p1signatures);
    }

    public String getP2SignaturesString() {
        return byteArrayToHexString(p2signatures);
    }

    public String getHeadKeysString() {
        return byteArrayToHexString(headKeys);
    }

    public String getTailKeysString() {
        return byteArrayToHexString(tailKeys);
    }

    public String getHeadSignaturesString() {
        return byteArrayToHexString(headSignatures);
    }

    public String getTailSignaturesString() {
        return byteArrayToHexString(tailSignatures);
    }
}
