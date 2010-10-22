package fr.inria.peerunit.openchordtest;

import de.uniba.wiai.lspi.chord.service.Key;

/*
 * @author sunye
 */
public class StringKey implements Key {

    private String image;

    public StringKey(String str) {
        this.image = str;
    }

    public byte[] getBytes() {
        return this.image.getBytes();
    }

    @Override
    public int hashCode() {
        return this.image.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof StringKey) {
            StringKey other = (StringKey) o;
            return image.equals(other.image);
        }
        return false;
    }
}
