package org.itxtech.synapseapi.utils;

public class BlobTrack {

    private final long hash;
    private final byte[] blob;
    private int refCnt;

    public BlobTrack(long hash, byte[] blob) {
        this(hash, blob, 1);
    }

    public BlobTrack(long hash, byte[] blob, int refCnt) {
        this.hash = hash;
        this.blob = blob;
        this.refCnt = refCnt;
    }

    public long getHash() {
        return this.hash;
    }

    public byte[] getBlob() {
        return this.blob;
    }

    /**
     * Returns the reference count of this object.
     */
    public int refCnt() {
        return this.refCnt;
    }

    /**
     * Increases the reference count by 1.
     */
    public void retain() {
        ++this.refCnt;
    }

    /**
     * Increases the reference count by the specified increment.
     */
    public void retain(int increment) {
        this.refCnt += increment;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BlobTrack) {
            return this.hash == ((BlobTrack) obj).hash;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) this.hash;
    }
}
