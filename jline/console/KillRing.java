// 
// Decompiled by Procyon v0.5.36
// 

package jline.console;

public final class KillRing
{
    private static final int DEFAULT_SIZE = 60;
    private final String[] slots;
    private int head;
    private boolean lastKill;
    private boolean lastYank;
    
    public KillRing(final int size) {
        this.head = 0;
        this.lastKill = false;
        this.lastYank = false;
        this.slots = new String[size];
    }
    
    public KillRing() {
        this(60);
    }
    
    public void resetLastYank() {
        this.lastYank = false;
    }
    
    public void resetLastKill() {
        this.lastKill = false;
    }
    
    public boolean lastYank() {
        return this.lastYank;
    }
    
    public void add(final String str) {
        this.lastYank = false;
        if (this.lastKill && this.slots[this.head] != null) {
            final StringBuilder sb = new StringBuilder();
            final String[] slots = this.slots;
            final int head = this.head;
            slots[head] = sb.append(slots[head]).append(str).toString();
            return;
        }
        this.lastKill = true;
        this.next();
        this.slots[this.head] = str;
    }
    
    public void addBackwards(final String str) {
        this.lastYank = false;
        if (this.lastKill && this.slots[this.head] != null) {
            this.slots[this.head] = str + this.slots[this.head];
            return;
        }
        this.lastKill = true;
        this.next();
        this.slots[this.head] = str;
    }
    
    public String yank() {
        this.lastKill = false;
        this.lastYank = true;
        return this.slots[this.head];
    }
    
    public String yankPop() {
        this.lastKill = false;
        if (this.lastYank) {
            this.prev();
            return this.slots[this.head];
        }
        return null;
    }
    
    private void next() {
        if (this.head == 0 && this.slots[0] == null) {
            return;
        }
        ++this.head;
        if (this.head == this.slots.length) {
            this.head = 0;
        }
    }
    
    private void prev() {
        --this.head;
        if (this.head == -1) {
            int x;
            for (x = this.slots.length - 1; x >= 0 && this.slots[x] == null; --x) {}
            this.head = x;
        }
    }
}
