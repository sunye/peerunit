package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * This class gives us a little utility program to pick words out of input
 * files. It might just be useful when boiling down a trace file of some
 * sort into input for something like a statistics program.
 * The users specifies a list of patterns. Each pattern has a list of offsets
 * associated with it. This program writes out the words found at given
 * offsets from the pattern matches, where offsets are measured in words. Offset
 * 0 is the pattern itself.
 * It won't accept -ve offsets. It is an error for a pattern match to occur
 * while words are still being emitted from a previous pattern match. (Largely
 * on the theory that if this is going on, you probably have pattern matches
 * where you didn't expect them). It is also an error for the file to end while
 * a pattern is waiting for words to emit.
 */
public class PickWords {
    public static void main(String[] s) throws IOException, SelectException {
        if (s.length % 2 != 0) {
            System.err.println("Usage is PickWords [<pattern> <offsets>]*");
            System.exit(1);
        }
        List words = new ArrayList();
        List sequences = new ArrayList();
        try {
            for (int i = 0; i < s.length; i += 2) {
                words.add(s[i]);
                String offString = s[i + 1];
                int len = offString.length();
                List offsets = new ArrayList();
                for (int j = 0; j < len; ) {
                    char ch = offString.charAt(j);
                    if (!Character.isDigit(ch)) {
                        j++;
                        continue;
                    }
                    int k = j + 1;
                    for (; k < len; k++)
                        if (!Character.isDigit(offString.charAt(k)))
                            break;
                    offsets.add(new Integer(offString.substring(j, k)));
                    j = k;
                }
                int[] ints = new int[offsets.size()];
                int p = 0;
                for (Iterator it = offsets.iterator(); it.hasNext(); )
                    ints[p++] = ((Integer) (it.next())).intValue();
                sequences.add(ints);
            }
        } catch (NumberFormatException ne) {
            System.err.println("Unexpected number trouble " + ne);
            System.exit(1);
        }
        String[] wordArray = (String[]) words.toArray(new String[0]);
        int[][] offsets = (int[][]) sequences.toArray(new int[0][]);
        for (int i = 0; i < wordArray.length; i++) {
            System.err.print("Word " + wordArray[i] + " has offsets");
            for (int j = 0; j < offsets[i].length; j++)
                System.err.print(" " + offsets[i][j]);
            System.err.println();
        }
        PrintWriter pw = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(System.out)));
        SelectWords sw = new SelectWords(wordArray, offsets, pw);
        for (; ; ) {
            int ch = System.in.read();
            if (ch == -1)
                break;
            sw.accept((char) ch);
        }
        try {
            sw.eof();
        } catch (SelectException se) {
            pw.flush();
            System.err.println("Warning: error " + se + " on eof");
        }
        if (pw.checkError()) {
            System.err.println("Trouble with printWriter");
            System.exit(1);
        }
    }
}
