// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.buffer.ByteBufUtil;
import java.lang.reflect.Method;
import io.netty.util.internal.EmptyArrays;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Arrays;
import io.netty.util.internal.ThreadLocalRandom;
import java.util.Map;
import java.net.SocketException;
import java.net.NetworkInterface;
import java.util.LinkedHashMap;
import java.net.UnknownHostException;
import io.netty.util.internal.PlatformDependent;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import io.netty.util.internal.logging.InternalLogger;

final class DefaultChannelId implements ChannelId
{
    private static final long serialVersionUID = 3884076183504074063L;
    private static final InternalLogger logger;
    private static final Pattern MACHINE_ID_PATTERN;
    private static final int MACHINE_ID_LEN = 8;
    private static final byte[] MACHINE_ID;
    private static final int PROCESS_ID_LEN = 4;
    private static final int MAX_PROCESS_ID = 4194304;
    private static final int PROCESS_ID;
    private static final int SEQUENCE_LEN = 4;
    private static final int TIMESTAMP_LEN = 8;
    private static final int RANDOM_LEN = 4;
    private static final AtomicInteger nextSequence;
    private final byte[] data;
    private int hashCode;
    private transient String shortValue;
    private transient String longValue;
    
    DefaultChannelId() {
        this.data = new byte[28];
    }
    
    static ChannelId newInstance() {
        final DefaultChannelId id = new DefaultChannelId();
        id.init();
        return id;
    }
    
    private static byte[] parseMachineId(String value) {
        value = value.replaceAll("[:-]", "");
        final byte[] machineId = new byte[8];
        for (int i = 0; i < value.length(); i += 2) {
            machineId[i] = (byte)Integer.parseInt(value.substring(i, i + 2), 16);
        }
        return machineId;
    }
    
    private static byte[] defaultMachineId() {
        byte[] bestMacAddr;
        final byte[] NOT_FOUND = bestMacAddr = new byte[] { -1 };
        InetAddress bestInetAddr = null;
        try {
            bestInetAddr = InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 });
        }
        catch (UnknownHostException e) {
            PlatformDependent.throwException(e);
        }
        final Map<NetworkInterface, InetAddress> ifaces = new LinkedHashMap<NetworkInterface, InetAddress>();
        try {
            final Enumeration<NetworkInterface> i = NetworkInterface.getNetworkInterfaces();
            while (i.hasMoreElements()) {
                final NetworkInterface iface = i.nextElement();
                final Enumeration<InetAddress> addrs = iface.getInetAddresses();
                if (addrs.hasMoreElements()) {
                    final InetAddress a = addrs.nextElement();
                    if (a.isLoopbackAddress()) {
                        continue;
                    }
                    ifaces.put(iface, a);
                }
            }
        }
        catch (SocketException e2) {
            DefaultChannelId.logger.warn("Failed to retrieve the list of available network interfaces", e2);
        }
        for (final Map.Entry<NetworkInterface, InetAddress> entry : ifaces.entrySet()) {
            final NetworkInterface iface2 = entry.getKey();
            final InetAddress inetAddr = entry.getValue();
            if (iface2.isVirtual()) {
                continue;
            }
            byte[] macAddr;
            try {
                macAddr = iface2.getHardwareAddress();
            }
            catch (SocketException e3) {
                DefaultChannelId.logger.debug("Failed to get the hardware address of a network interface: {}", iface2, e3);
                continue;
            }
            boolean replace = false;
            int res = compareAddresses(bestMacAddr, macAddr);
            if (res < 0) {
                replace = true;
            }
            else if (res == 0) {
                res = compareAddresses(bestInetAddr, inetAddr);
                if (res < 0) {
                    replace = true;
                }
                else if (res == 0 && bestMacAddr.length < macAddr.length) {
                    replace = true;
                }
            }
            if (!replace) {
                continue;
            }
            bestMacAddr = macAddr;
            bestInetAddr = inetAddr;
        }
        if (bestMacAddr == NOT_FOUND) {
            bestMacAddr = new byte[8];
            ThreadLocalRandom.current().nextBytes(bestMacAddr);
            DefaultChannelId.logger.warn("Failed to find a usable hardware address from the network interfaces; using random bytes: {}", formatAddress(bestMacAddr));
        }
        switch (bestMacAddr.length) {
            case 6: {
                final byte[] newAddr = new byte[8];
                System.arraycopy(bestMacAddr, 0, newAddr, 0, 3);
                newAddr[3] = -1;
                newAddr[4] = -2;
                System.arraycopy(bestMacAddr, 3, newAddr, 5, 3);
                bestMacAddr = newAddr;
                break;
            }
            default: {
                bestMacAddr = Arrays.copyOf(bestMacAddr, 8);
                break;
            }
        }
        return bestMacAddr;
    }
    
    private static int compareAddresses(final byte[] current, final byte[] candidate) {
        if (candidate == null) {
            return 1;
        }
        if (candidate.length < 6) {
            return 1;
        }
        boolean onlyZeroAndOne = true;
        for (final byte b : candidate) {
            if (b != 0 && b != 1) {
                onlyZeroAndOne = false;
                break;
            }
        }
        if (onlyZeroAndOne) {
            return 1;
        }
        if ((candidate[0] & 0x1) != 0x0) {
            return 1;
        }
        if ((current[0] & 0x2) == 0x0) {
            if ((candidate[0] & 0x2) == 0x0) {
                return 0;
            }
            return 1;
        }
        else {
            if ((candidate[0] & 0x2) == 0x0) {
                return -1;
            }
            return 0;
        }
    }
    
    private static int compareAddresses(final InetAddress current, final InetAddress candidate) {
        return scoreAddress(current) - scoreAddress(candidate);
    }
    
    private static int scoreAddress(final InetAddress addr) {
        if (addr.isAnyLocalAddress()) {
            return 0;
        }
        if (addr.isMulticastAddress()) {
            return 1;
        }
        if (addr.isLinkLocalAddress()) {
            return 2;
        }
        if (addr.isSiteLocalAddress()) {
            return 3;
        }
        return 4;
    }
    
    private static String formatAddress(final byte[] addr) {
        final StringBuilder buf = new StringBuilder(24);
        for (final byte b : addr) {
            buf.append(String.format("%02x:", b & 0xFF));
        }
        return buf.substring(0, buf.length() - 1);
    }
    
    private static int defaultProcessId() {
        final ClassLoader loader = PlatformDependent.getSystemClassLoader();
        String value;
        try {
            final Class<?> mgmtFactoryType = Class.forName("java.lang.management.ManagementFactory", true, loader);
            final Class<?> runtimeMxBeanType = Class.forName("java.lang.management.RuntimeMXBean", true, loader);
            final Method getRuntimeMXBean = mgmtFactoryType.getMethod("getRuntimeMXBean", EmptyArrays.EMPTY_CLASSES);
            final Object bean = getRuntimeMXBean.invoke(null, EmptyArrays.EMPTY_OBJECTS);
            final Method getName = runtimeMxBeanType.getDeclaredMethod("getName", EmptyArrays.EMPTY_CLASSES);
            value = (String)getName.invoke(bean, EmptyArrays.EMPTY_OBJECTS);
        }
        catch (Exception e) {
            DefaultChannelId.logger.debug("Could not invoke ManagementFactory.getRuntimeMXBean().getName(); Android?", e);
            try {
                final Class<?> processType = Class.forName("android.os.Process", true, loader);
                final Method myPid = processType.getMethod("myPid", EmptyArrays.EMPTY_CLASSES);
                value = myPid.invoke(null, EmptyArrays.EMPTY_OBJECTS).toString();
            }
            catch (Exception e2) {
                DefaultChannelId.logger.debug("Could not invoke Process.myPid(); not Android?", e2);
                value = "";
            }
        }
        final int atIndex = value.indexOf(64);
        if (atIndex >= 0) {
            value = value.substring(0, atIndex);
        }
        int pid;
        try {
            pid = Integer.parseInt(value);
        }
        catch (NumberFormatException e3) {
            pid = -1;
        }
        if (pid < 0 || pid > 4194304) {
            pid = ThreadLocalRandom.current().nextInt(4194305);
            DefaultChannelId.logger.warn("Failed to find the current process ID from '{}'; using a random value: {}", value, pid);
        }
        return pid;
    }
    
    private void init() {
        int i = 0;
        System.arraycopy(DefaultChannelId.MACHINE_ID, 0, this.data, i, 8);
        i += 8;
        i = this.writeInt(i, DefaultChannelId.PROCESS_ID);
        i = this.writeInt(i, DefaultChannelId.nextSequence.getAndIncrement());
        i = this.writeLong(i, Long.reverse(System.nanoTime()) ^ System.currentTimeMillis());
        final int random = ThreadLocalRandom.current().nextInt();
        this.hashCode = random;
        i = this.writeInt(i, random);
        assert i == this.data.length;
    }
    
    private int writeInt(int i, final int value) {
        this.data[i++] = (byte)(value >>> 24);
        this.data[i++] = (byte)(value >>> 16);
        this.data[i++] = (byte)(value >>> 8);
        this.data[i++] = (byte)value;
        return i;
    }
    
    private int writeLong(int i, final long value) {
        this.data[i++] = (byte)(value >>> 56);
        this.data[i++] = (byte)(value >>> 48);
        this.data[i++] = (byte)(value >>> 40);
        this.data[i++] = (byte)(value >>> 32);
        this.data[i++] = (byte)(value >>> 24);
        this.data[i++] = (byte)(value >>> 16);
        this.data[i++] = (byte)(value >>> 8);
        this.data[i++] = (byte)value;
        return i;
    }
    
    @Override
    public String asShortText() {
        String shortValue = this.shortValue;
        if (shortValue == null) {
            shortValue = (this.shortValue = ByteBufUtil.hexDump(this.data, 24, 4));
        }
        return shortValue;
    }
    
    @Override
    public String asLongText() {
        String longValue = this.longValue;
        if (longValue == null) {
            longValue = (this.longValue = this.newLongValue());
        }
        return longValue;
    }
    
    private String newLongValue() {
        final StringBuilder buf = new StringBuilder(2 * this.data.length + 5);
        int i = 0;
        i = this.appendHexDumpField(buf, i, 8);
        i = this.appendHexDumpField(buf, i, 4);
        i = this.appendHexDumpField(buf, i, 4);
        i = this.appendHexDumpField(buf, i, 8);
        i = this.appendHexDumpField(buf, i, 4);
        assert i == this.data.length;
        return buf.substring(0, buf.length() - 1);
    }
    
    private int appendHexDumpField(final StringBuilder buf, int i, final int length) {
        buf.append(ByteBufUtil.hexDump(this.data, i, length));
        buf.append('-');
        i += length;
        return i;
    }
    
    @Override
    public int hashCode() {
        return this.hashCode;
    }
    
    @Override
    public int compareTo(final ChannelId o) {
        return 0;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof DefaultChannelId && Arrays.equals(this.data, ((DefaultChannelId)obj).data));
    }
    
    @Override
    public String toString() {
        return this.asShortText();
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(DefaultChannelId.class);
        MACHINE_ID_PATTERN = Pattern.compile("^(?:[0-9a-fA-F][:-]?){6,8}$");
        nextSequence = new AtomicInteger();
        int processId = -1;
        final String customProcessId = SystemPropertyUtil.get("io.netty.processId");
        if (customProcessId != null) {
            try {
                processId = Integer.parseInt(customProcessId);
            }
            catch (NumberFormatException ex) {}
            if (processId < 0 || processId > 4194304) {
                processId = -1;
                DefaultChannelId.logger.warn("-Dio.netty.processId: {} (malformed)", customProcessId);
            }
            else if (DefaultChannelId.logger.isDebugEnabled()) {
                DefaultChannelId.logger.debug("-Dio.netty.processId: {} (user-set)", (Object)processId);
            }
        }
        if (processId < 0) {
            processId = defaultProcessId();
            if (DefaultChannelId.logger.isDebugEnabled()) {
                DefaultChannelId.logger.debug("-Dio.netty.processId: {} (auto-detected)", (Object)processId);
            }
        }
        PROCESS_ID = processId;
        byte[] machineId = null;
        final String customMachineId = SystemPropertyUtil.get("io.netty.machineId");
        if (customMachineId != null) {
            if (DefaultChannelId.MACHINE_ID_PATTERN.matcher(customMachineId).matches()) {
                machineId = parseMachineId(customMachineId);
                DefaultChannelId.logger.debug("-Dio.netty.machineId: {} (user-set)", customMachineId);
            }
            else {
                DefaultChannelId.logger.warn("-Dio.netty.machineId: {} (malformed)", customMachineId);
            }
        }
        if (machineId == null) {
            machineId = defaultMachineId();
            if (DefaultChannelId.logger.isDebugEnabled()) {
                DefaultChannelId.logger.debug("-Dio.netty.machineId: {} (auto-detected)", formatAddress(machineId));
            }
        }
        MACHINE_ID = machineId;
    }
}
