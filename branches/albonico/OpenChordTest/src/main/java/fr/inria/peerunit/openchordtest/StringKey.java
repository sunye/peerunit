package fr.inria.peerunit.openchordtest;

import de.uniba.wiai.lspi.chord.service.Key;

/*
 * Class implemented by the authors of the Open Chord
 */
public class StringKey implements Key {

    private String theString;

    public StringKey(String str) {
        theString = str;
    }

    public byte[] getBytes() {
        return this.theString.getBytes();
    }

    @Override
    public int hashCode() {
        return this.theString.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StringKey) {
            return theString.equals(o);
        }
        return false;
    }
}
