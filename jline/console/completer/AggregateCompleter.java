// 
// Decompiled by Procyon v0.5.36
// 

package jline.console.completer;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.Arrays;
import jline.internal.Preconditions;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class AggregateCompleter implements Completer
{
    private final List<Completer> completers;
    
    public AggregateCompleter() {
        this.completers = new ArrayList<Completer>();
    }
    
    public AggregateCompleter(final Collection<Completer> completers) {
        this.completers = new ArrayList<Completer>();
        Preconditions.checkNotNull(completers);
        this.completers.addAll(completers);
    }
    
    public AggregateCompleter(final Completer... completers) {
        this(Arrays.asList(completers));
    }
    
    public Collection<Completer> getCompleters() {
        return this.completers;
    }
    
    public int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
        Preconditions.checkNotNull(candidates);
        final List<Completion> completions = new ArrayList<Completion>(this.completers.size());
        int max = -1;
        for (final Completer completer : this.completers) {
            final Completion completion = new Completion(candidates);
            completion.complete(completer, buffer, cursor);
            max = Math.max(max, completion.cursor);
            completions.add(completion);
        }
        for (final Completion completion2 : completions) {
            if (completion2.cursor == max) {
                candidates.addAll(completion2.candidates);
            }
        }
        return max;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + "completers=" + this.completers + '}';
    }
    
    private class Completion
    {
        public final List<CharSequence> candidates;
        public int cursor;
        
        public Completion(final List<CharSequence> candidates) {
            Preconditions.checkNotNull(candidates);
            this.candidates = new LinkedList<CharSequence>(candidates);
        }
        
        public void complete(final Completer completer, final String buffer, final int cursor) {
            Preconditions.checkNotNull(completer);
            this.cursor = completer.complete(buffer, cursor, this.candidates);
        }
    }
}
