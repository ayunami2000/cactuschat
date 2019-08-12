// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util;

import java.util.Arrays;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.Map;
import io.netty.util.concurrent.FastThreadLocal;
import java.util.concurrent.atomic.AtomicInteger;
import io.netty.util.internal.logging.InternalLogger;

public abstract class Recycler<T>
{
    private static final InternalLogger logger;
    private static final AtomicInteger ID_GENERATOR;
    private static final int OWN_THREAD_ID;
    private static final int DEFAULT_MAX_CAPACITY;
    private static final int INITIAL_CAPACITY;
    private final int maxCapacity;
    private final FastThreadLocal<Stack<T>> threadLocal;
    private static final FastThreadLocal<Map<Stack<?>, WeakOrderQueue>> DELAYED_RECYCLED;
    
    protected Recycler() {
        this(Recycler.DEFAULT_MAX_CAPACITY);
    }
    
    protected Recycler(final int maxCapacity) {
        this.threadLocal = new FastThreadLocal<Stack<T>>() {
            @Override
            protected Stack<T> initialValue() {
                return new Stack<T>(Recycler.this, Thread.currentThread(), Recycler.this.maxCapacity);
            }
        };
        this.maxCapacity = Math.max(0, maxCapacity);
    }
    
    public final T get() {
        final Stack<T> stack = this.threadLocal.get();
        DefaultHandle<T> handle = stack.pop();
        if (handle == null) {
            handle = stack.newHandle();
            ((DefaultHandle<Object>)handle).value = this.newObject((Handle<Object>)handle);
        }
        return (T)((DefaultHandle<Object>)handle).value;
    }
    
    public final boolean recycle(final T o, final Handle<T> handle) {
        final DefaultHandle<T> h = (DefaultHandle<T>)(DefaultHandle)handle;
        if (((DefaultHandle<Object>)h).stack.parent != this) {
            return false;
        }
        h.recycle(o);
        return true;
    }
    
    final int threadLocalCapacity() {
        return ((Stack<Object>)this.threadLocal.get()).elements.length;
    }
    
    final int threadLocalSize() {
        return ((Stack<Object>)this.threadLocal.get()).size;
    }
    
    protected abstract T newObject(final Handle<T> p0);
    
    static {
        logger = InternalLoggerFactory.getInstance(Recycler.class);
        ID_GENERATOR = new AtomicInteger(Integer.MIN_VALUE);
        OWN_THREAD_ID = Recycler.ID_GENERATOR.getAndIncrement();
        int maxCapacity = SystemPropertyUtil.getInt("io.netty.recycler.maxCapacity", 0);
        if (maxCapacity <= 0) {
            maxCapacity = 262144;
        }
        DEFAULT_MAX_CAPACITY = maxCapacity;
        if (Recycler.logger.isDebugEnabled()) {
            Recycler.logger.debug("-Dio.netty.recycler.maxCapacity: {}", (Object)Recycler.DEFAULT_MAX_CAPACITY);
        }
        INITIAL_CAPACITY = Math.min(Recycler.DEFAULT_MAX_CAPACITY, 256);
        DELAYED_RECYCLED = new FastThreadLocal<Map<Stack<?>, WeakOrderQueue>>() {
            @Override
            protected Map<Stack<?>, WeakOrderQueue> initialValue() {
                return new WeakHashMap<Stack<?>, WeakOrderQueue>();
            }
        };
    }
    
    static final class DefaultHandle<T> implements Handle<T>
    {
        private int lastRecycledId;
        private int recycleId;
        private Stack<?> stack;
        private Object value;
        
        DefaultHandle(final Stack<?> stack) {
            this.stack = stack;
        }
        
        @Override
        public void recycle(final Object object) {
            if (object != this.value) {
                throw new IllegalArgumentException("object does not belong to handle");
            }
            final Thread thread = Thread.currentThread();
            if (thread == this.stack.thread) {
                this.stack.push(this);
                return;
            }
            final Map<Stack<?>, WeakOrderQueue> delayedRecycled = Recycler.DELAYED_RECYCLED.get();
            WeakOrderQueue queue = delayedRecycled.get(this.stack);
            if (queue == null) {
                delayedRecycled.put(this.stack, queue = new WeakOrderQueue(this.stack, thread));
            }
            queue.add(this);
        }
    }
    
    private static final class WeakOrderQueue
    {
        private static final int LINK_CAPACITY = 16;
        private Link head;
        private Link tail;
        private WeakOrderQueue next;
        private final WeakReference<Thread> owner;
        private final int id;
        
        WeakOrderQueue(final Stack<?> stack, final Thread thread) {
            this.id = Recycler.ID_GENERATOR.getAndIncrement();
            final Link link = new Link();
            this.tail = link;
            this.head = link;
            this.owner = new WeakReference<Thread>(thread);
            synchronized (stack) {
                this.next = ((Stack<Object>)stack).head;
                ((Stack<Object>)stack).head = this;
            }
        }
        
        void add(final DefaultHandle<?> handle) {
            ((DefaultHandle<Object>)handle).lastRecycledId = this.id;
            Link tail = this.tail;
            int writeIndex;
            if ((writeIndex = tail.get()) == 16) {
                tail = (this.tail = (tail.next = new Link()));
                writeIndex = tail.get();
            }
            (tail.elements[writeIndex] = handle).stack = null;
            tail.lazySet(writeIndex + 1);
        }
        
        boolean hasFinalData() {
            return this.tail.readIndex != this.tail.get();
        }
        
        boolean transfer(final Stack<?> dst) {
            Link head = this.head;
            if (head == null) {
                return false;
            }
            if (head.readIndex == 16) {
                if (head.next == null) {
                    return false;
                }
                head = (this.head = head.next);
            }
            final int srcStart = head.readIndex;
            int srcEnd = head.get();
            final int srcSize = srcEnd - srcStart;
            if (srcSize == 0) {
                return false;
            }
            final int dstSize = ((Stack<Object>)dst).size;
            final int expectedCapacity = dstSize + srcSize;
            if (expectedCapacity > ((Stack<Object>)dst).elements.length) {
                final int actualCapacity = dst.increaseCapacity(expectedCapacity);
                srcEnd = Math.min(srcStart + actualCapacity - dstSize, srcEnd);
            }
            if (srcStart != srcEnd) {
                final DefaultHandle[] srcElems = head.elements;
                final DefaultHandle[] dstElems = ((Stack<Object>)dst).elements;
                int newDstSize = dstSize;
                for (int i = srcStart; i < srcEnd; ++i) {
                    final DefaultHandle element = srcElems[i];
                    if (element.recycleId == 0) {
                        element.recycleId = element.lastRecycledId;
                    }
                    else if (element.recycleId != element.lastRecycledId) {
                        throw new IllegalStateException("recycled already");
                    }
                    element.stack = dst;
                    dstElems[newDstSize++] = element;
                    srcElems[i] = null;
                }
                ((Stack<Object>)dst).size = newDstSize;
                if (srcEnd == 16 && head.next != null) {
                    this.head = head.next;
                }
                head.readIndex = srcEnd;
                return true;
            }
            return false;
        }
        
        private static final class Link extends AtomicInteger
        {
            private final DefaultHandle<?>[] elements;
            private int readIndex;
            private Link next;
            
            private Link() {
                this.elements = (DefaultHandle<?>[])new DefaultHandle[16];
            }
        }
    }
    
    static final class Stack<T>
    {
        final Recycler<T> parent;
        final Thread thread;
        private DefaultHandle<?>[] elements;
        private final int maxCapacity;
        private int size;
        private volatile WeakOrderQueue head;
        private WeakOrderQueue cursor;
        private WeakOrderQueue prev;
        
        Stack(final Recycler<T> parent, final Thread thread, final int maxCapacity) {
            this.parent = parent;
            this.thread = thread;
            this.maxCapacity = maxCapacity;
            this.elements = (DefaultHandle<?>[])new DefaultHandle[Math.min(Recycler.INITIAL_CAPACITY, maxCapacity)];
        }
        
        int increaseCapacity(final int expectedCapacity) {
            int newCapacity = this.elements.length;
            final int maxCapacity = this.maxCapacity;
            do {
                newCapacity <<= 1;
            } while (newCapacity < expectedCapacity && newCapacity < maxCapacity);
            newCapacity = Math.min(newCapacity, maxCapacity);
            if (newCapacity != this.elements.length) {
                this.elements = Arrays.copyOf(this.elements, newCapacity);
            }
            return newCapacity;
        }
        
        DefaultHandle<T> pop() {
            int size = this.size;
            if (size == 0) {
                if (!this.scavenge()) {
                    return null;
                }
                size = this.size;
            }
            --size;
            final DefaultHandle ret = this.elements[size];
            if (ret.lastRecycledId != ret.recycleId) {
                throw new IllegalStateException("recycled multiple times");
            }
            ret.recycleId = 0;
            ret.lastRecycledId = 0;
            this.size = size;
            return (DefaultHandle<T>)ret;
        }
        
        boolean scavenge() {
            if (this.scavengeSome()) {
                return true;
            }
            this.prev = null;
            this.cursor = this.head;
            return false;
        }
        
        boolean scavengeSome() {
            WeakOrderQueue cursor = this.cursor;
            if (cursor == null) {
                cursor = this.head;
                if (cursor == null) {
                    return false;
                }
            }
            boolean success = false;
            WeakOrderQueue prev = this.prev;
            while (true) {
                while (!cursor.transfer(this)) {
                    final WeakOrderQueue next = cursor.next;
                    if (cursor.owner.get() == null) {
                        if (cursor.hasFinalData()) {
                            while (cursor.transfer(this)) {
                                success = true;
                            }
                        }
                        if (prev != null) {
                            prev.next = next;
                        }
                    }
                    else {
                        prev = cursor;
                    }
                    cursor = next;
                    if (cursor == null || success) {
                        this.prev = prev;
                        this.cursor = cursor;
                        return success;
                    }
                }
                success = true;
                continue;
            }
        }
        
        void push(final DefaultHandle<?> item) {
            if ((((DefaultHandle<Object>)item).recycleId | ((DefaultHandle<Object>)item).lastRecycledId) != 0x0) {
                throw new IllegalStateException("recycled already");
            }
            ((DefaultHandle<Object>)item).recycleId = (((DefaultHandle<Object>)item).lastRecycledId = Recycler.OWN_THREAD_ID);
            final int size = this.size;
            if (size >= this.maxCapacity) {
                return;
            }
            if (size == this.elements.length) {
                this.elements = Arrays.copyOf(this.elements, Math.min(size << 1, this.maxCapacity));
            }
            this.elements[size] = item;
            this.size = size + 1;
        }
        
        DefaultHandle<T> newHandle() {
            return new DefaultHandle<T>(this);
        }
    }
    
    public interface Handle<T>
    {
        void recycle(final T p0);
    }
}
