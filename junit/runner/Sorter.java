// 
// Decompiled by Procyon v0.5.36
// 

package junit.runner;

import java.util.Vector;

public class Sorter
{
    public static void sortStrings(final Vector values, int left, int right, final Swapper swapper) {
        final int oleft = left;
        final int oright = right;
        final String mid = values.elementAt((left + right) / 2);
        while (true) {
            if (values.elementAt(left).compareTo(mid) < 0) {
                ++left;
            }
            else {
                while (mid.compareTo((String)values.elementAt(right)) < 0) {
                    --right;
                }
                if (left <= right) {
                    swapper.swap(values, left, right);
                    ++left;
                    --right;
                }
                if (left > right) {
                    break;
                }
                continue;
            }
        }
        if (oleft < right) {
            sortStrings(values, oleft, right, swapper);
        }
        if (left < oright) {
            sortStrings(values, left, oright, swapper);
        }
    }
    
    public interface Swapper
    {
        void swap(final Vector p0, final int p1, final int p2);
    }
}
