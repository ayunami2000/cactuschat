// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.net.UnknownHostException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.InetAddress;

public class WKSRecord extends Record
{
    private static final long serialVersionUID = -9104259763909119805L;
    private byte[] address;
    private int protocol;
    private int[] services;
    
    WKSRecord() {
    }
    
    Record getObject() {
        return new WKSRecord();
    }
    
    public WKSRecord(final Name name, final int dclass, final long ttl, final InetAddress address, final int protocol, final int[] services) {
        super(name, 11, dclass, ttl);
        if (Address.familyOf(address) != 1) {
            throw new IllegalArgumentException("invalid IPv4 address");
        }
        this.address = address.getAddress();
        this.protocol = Record.checkU8("protocol", protocol);
        for (int i = 0; i < services.length; ++i) {
            Record.checkU16("service", services[i]);
        }
        System.arraycopy(services, 0, this.services = new int[services.length], 0, services.length);
        Arrays.sort(this.services);
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.address = in.readByteArray(4);
        this.protocol = in.readU8();
        final byte[] array = in.readByteArray();
        final List list = new ArrayList();
        for (int i = 0; i < array.length; ++i) {
            for (int j = 0; j < 8; ++j) {
                final int octet = array[i] & 0xFF;
                if ((octet & 1 << 7 - j) != 0x0) {
                    list.add(new Integer(i * 8 + j));
                }
            }
        }
        this.services = new int[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            this.services[i] = list.get(i);
        }
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        String s = st.getString();
        this.address = Address.toByteArray(s, 1);
        if (this.address == null) {
            throw st.exception("invalid address");
        }
        s = st.getString();
        this.protocol = Protocol.value(s);
        if (this.protocol < 0) {
            throw st.exception("Invalid IP protocol: " + s);
        }
        final List list = new ArrayList();
        while (true) {
            final Tokenizer.Token t = st.get();
            if (!t.isString()) {
                st.unget();
                this.services = new int[list.size()];
                for (int i = 0; i < list.size(); ++i) {
                    this.services[i] = list.get(i);
                }
                return;
            }
            final int service = Service.value(t.value);
            if (service < 0) {
                throw st.exception("Invalid TCP/UDP service: " + t.value);
            }
            list.add(new Integer(service));
        }
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(Address.toDottedQuad(this.address));
        sb.append(" ");
        sb.append(this.protocol);
        for (int i = 0; i < this.services.length; ++i) {
            sb.append(" " + this.services[i]);
        }
        return sb.toString();
    }
    
    public InetAddress getAddress() {
        try {
            return InetAddress.getByAddress(this.address);
        }
        catch (UnknownHostException e) {
            return null;
        }
    }
    
    public int getProtocol() {
        return this.protocol;
    }
    
    public int[] getServices() {
        return this.services;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeByteArray(this.address);
        out.writeU8(this.protocol);
        final int highestPort = this.services[this.services.length - 1];
        final byte[] array = new byte[highestPort / 8 + 1];
        for (int i = 0; i < this.services.length; ++i) {
            final int port = this.services[i];
            final byte[] array2 = array;
            final int n = port / 8;
            array2[n] |= (byte)(1 << 7 - port % 8);
        }
        out.writeByteArray(array);
    }
    
    public static class Protocol
    {
        public static final int ICMP = 1;
        public static final int IGMP = 2;
        public static final int GGP = 3;
        public static final int ST = 5;
        public static final int TCP = 6;
        public static final int UCL = 7;
        public static final int EGP = 8;
        public static final int IGP = 9;
        public static final int BBN_RCC_MON = 10;
        public static final int NVP_II = 11;
        public static final int PUP = 12;
        public static final int ARGUS = 13;
        public static final int EMCON = 14;
        public static final int XNET = 15;
        public static final int CHAOS = 16;
        public static final int UDP = 17;
        public static final int MUX = 18;
        public static final int DCN_MEAS = 19;
        public static final int HMP = 20;
        public static final int PRM = 21;
        public static final int XNS_IDP = 22;
        public static final int TRUNK_1 = 23;
        public static final int TRUNK_2 = 24;
        public static final int LEAF_1 = 25;
        public static final int LEAF_2 = 26;
        public static final int RDP = 27;
        public static final int IRTP = 28;
        public static final int ISO_TP4 = 29;
        public static final int NETBLT = 30;
        public static final int MFE_NSP = 31;
        public static final int MERIT_INP = 32;
        public static final int SEP = 33;
        public static final int CFTP = 62;
        public static final int SAT_EXPAK = 64;
        public static final int MIT_SUBNET = 65;
        public static final int RVD = 66;
        public static final int IPPC = 67;
        public static final int SAT_MON = 69;
        public static final int IPCV = 71;
        public static final int BR_SAT_MON = 76;
        public static final int WB_MON = 78;
        public static final int WB_EXPAK = 79;
        private static Mnemonic protocols;
        
        private Protocol() {
        }
        
        public static String string(final int type) {
            return Protocol.protocols.getText(type);
        }
        
        public static int value(final String s) {
            return Protocol.protocols.getValue(s);
        }
        
        static {
            (Protocol.protocols = new Mnemonic("IP protocol", 3)).setMaximum(255);
            Protocol.protocols.setNumericAllowed(true);
            Protocol.protocols.add(1, "icmp");
            Protocol.protocols.add(2, "igmp");
            Protocol.protocols.add(3, "ggp");
            Protocol.protocols.add(5, "st");
            Protocol.protocols.add(6, "tcp");
            Protocol.protocols.add(7, "ucl");
            Protocol.protocols.add(8, "egp");
            Protocol.protocols.add(9, "igp");
            Protocol.protocols.add(10, "bbn-rcc-mon");
            Protocol.protocols.add(11, "nvp-ii");
            Protocol.protocols.add(12, "pup");
            Protocol.protocols.add(13, "argus");
            Protocol.protocols.add(14, "emcon");
            Protocol.protocols.add(15, "xnet");
            Protocol.protocols.add(16, "chaos");
            Protocol.protocols.add(17, "udp");
            Protocol.protocols.add(18, "mux");
            Protocol.protocols.add(19, "dcn-meas");
            Protocol.protocols.add(20, "hmp");
            Protocol.protocols.add(21, "prm");
            Protocol.protocols.add(22, "xns-idp");
            Protocol.protocols.add(23, "trunk-1");
            Protocol.protocols.add(24, "trunk-2");
            Protocol.protocols.add(25, "leaf-1");
            Protocol.protocols.add(26, "leaf-2");
            Protocol.protocols.add(27, "rdp");
            Protocol.protocols.add(28, "irtp");
            Protocol.protocols.add(29, "iso-tp4");
            Protocol.protocols.add(30, "netblt");
            Protocol.protocols.add(31, "mfe-nsp");
            Protocol.protocols.add(32, "merit-inp");
            Protocol.protocols.add(33, "sep");
            Protocol.protocols.add(62, "cftp");
            Protocol.protocols.add(64, "sat-expak");
            Protocol.protocols.add(65, "mit-subnet");
            Protocol.protocols.add(66, "rvd");
            Protocol.protocols.add(67, "ippc");
            Protocol.protocols.add(69, "sat-mon");
            Protocol.protocols.add(71, "ipcv");
            Protocol.protocols.add(76, "br-sat-mon");
            Protocol.protocols.add(78, "wb-mon");
            Protocol.protocols.add(79, "wb-expak");
        }
    }
    
    public static class Service
    {
        public static final int RJE = 5;
        public static final int ECHO = 7;
        public static final int DISCARD = 9;
        public static final int USERS = 11;
        public static final int DAYTIME = 13;
        public static final int QUOTE = 17;
        public static final int CHARGEN = 19;
        public static final int FTP_DATA = 20;
        public static final int FTP = 21;
        public static final int TELNET = 23;
        public static final int SMTP = 25;
        public static final int NSW_FE = 27;
        public static final int MSG_ICP = 29;
        public static final int MSG_AUTH = 31;
        public static final int DSP = 33;
        public static final int TIME = 37;
        public static final int RLP = 39;
        public static final int GRAPHICS = 41;
        public static final int NAMESERVER = 42;
        public static final int NICNAME = 43;
        public static final int MPM_FLAGS = 44;
        public static final int MPM = 45;
        public static final int MPM_SND = 46;
        public static final int NI_FTP = 47;
        public static final int LOGIN = 49;
        public static final int LA_MAINT = 51;
        public static final int DOMAIN = 53;
        public static final int ISI_GL = 55;
        public static final int NI_MAIL = 61;
        public static final int VIA_FTP = 63;
        public static final int TACACS_DS = 65;
        public static final int BOOTPS = 67;
        public static final int BOOTPC = 68;
        public static final int TFTP = 69;
        public static final int NETRJS_1 = 71;
        public static final int NETRJS_2 = 72;
        public static final int NETRJS_3 = 73;
        public static final int NETRJS_4 = 74;
        public static final int FINGER = 79;
        public static final int HOSTS2_NS = 81;
        public static final int SU_MIT_TG = 89;
        public static final int MIT_DOV = 91;
        public static final int DCP = 93;
        public static final int SUPDUP = 95;
        public static final int SWIFT_RVF = 97;
        public static final int TACNEWS = 98;
        public static final int METAGRAM = 99;
        public static final int HOSTNAME = 101;
        public static final int ISO_TSAP = 102;
        public static final int X400 = 103;
        public static final int X400_SND = 104;
        public static final int CSNET_NS = 105;
        public static final int RTELNET = 107;
        public static final int POP_2 = 109;
        public static final int SUNRPC = 111;
        public static final int AUTH = 113;
        public static final int SFTP = 115;
        public static final int UUCP_PATH = 117;
        public static final int NNTP = 119;
        public static final int ERPC = 121;
        public static final int NTP = 123;
        public static final int LOCUS_MAP = 125;
        public static final int LOCUS_CON = 127;
        public static final int PWDGEN = 129;
        public static final int CISCO_FNA = 130;
        public static final int CISCO_TNA = 131;
        public static final int CISCO_SYS = 132;
        public static final int STATSRV = 133;
        public static final int INGRES_NET = 134;
        public static final int LOC_SRV = 135;
        public static final int PROFILE = 136;
        public static final int NETBIOS_NS = 137;
        public static final int NETBIOS_DGM = 138;
        public static final int NETBIOS_SSN = 139;
        public static final int EMFIS_DATA = 140;
        public static final int EMFIS_CNTL = 141;
        public static final int BL_IDM = 142;
        public static final int SUR_MEAS = 243;
        public static final int LINK = 245;
        private static Mnemonic services;
        
        private Service() {
        }
        
        public static String string(final int type) {
            return Service.services.getText(type);
        }
        
        public static int value(final String s) {
            return Service.services.getValue(s);
        }
        
        static {
            (Service.services = new Mnemonic("TCP/UDP service", 3)).setMaximum(65535);
            Service.services.setNumericAllowed(true);
            Service.services.add(5, "rje");
            Service.services.add(7, "echo");
            Service.services.add(9, "discard");
            Service.services.add(11, "users");
            Service.services.add(13, "daytime");
            Service.services.add(17, "quote");
            Service.services.add(19, "chargen");
            Service.services.add(20, "ftp-data");
            Service.services.add(21, "ftp");
            Service.services.add(23, "telnet");
            Service.services.add(25, "smtp");
            Service.services.add(27, "nsw-fe");
            Service.services.add(29, "msg-icp");
            Service.services.add(31, "msg-auth");
            Service.services.add(33, "dsp");
            Service.services.add(37, "time");
            Service.services.add(39, "rlp");
            Service.services.add(41, "graphics");
            Service.services.add(42, "nameserver");
            Service.services.add(43, "nicname");
            Service.services.add(44, "mpm-flags");
            Service.services.add(45, "mpm");
            Service.services.add(46, "mpm-snd");
            Service.services.add(47, "ni-ftp");
            Service.services.add(49, "login");
            Service.services.add(51, "la-maint");
            Service.services.add(53, "domain");
            Service.services.add(55, "isi-gl");
            Service.services.add(61, "ni-mail");
            Service.services.add(63, "via-ftp");
            Service.services.add(65, "tacacs-ds");
            Service.services.add(67, "bootps");
            Service.services.add(68, "bootpc");
            Service.services.add(69, "tftp");
            Service.services.add(71, "netrjs-1");
            Service.services.add(72, "netrjs-2");
            Service.services.add(73, "netrjs-3");
            Service.services.add(74, "netrjs-4");
            Service.services.add(79, "finger");
            Service.services.add(81, "hosts2-ns");
            Service.services.add(89, "su-mit-tg");
            Service.services.add(91, "mit-dov");
            Service.services.add(93, "dcp");
            Service.services.add(95, "supdup");
            Service.services.add(97, "swift-rvf");
            Service.services.add(98, "tacnews");
            Service.services.add(99, "metagram");
            Service.services.add(101, "hostname");
            Service.services.add(102, "iso-tsap");
            Service.services.add(103, "x400");
            Service.services.add(104, "x400-snd");
            Service.services.add(105, "csnet-ns");
            Service.services.add(107, "rtelnet");
            Service.services.add(109, "pop-2");
            Service.services.add(111, "sunrpc");
            Service.services.add(113, "auth");
            Service.services.add(115, "sftp");
            Service.services.add(117, "uucp-path");
            Service.services.add(119, "nntp");
            Service.services.add(121, "erpc");
            Service.services.add(123, "ntp");
            Service.services.add(125, "locus-map");
            Service.services.add(127, "locus-con");
            Service.services.add(129, "pwdgen");
            Service.services.add(130, "cisco-fna");
            Service.services.add(131, "cisco-tna");
            Service.services.add(132, "cisco-sys");
            Service.services.add(133, "statsrv");
            Service.services.add(134, "ingres-net");
            Service.services.add(135, "loc-srv");
            Service.services.add(136, "profile");
            Service.services.add(137, "netbios-ns");
            Service.services.add(138, "netbios-dgm");
            Service.services.add(139, "netbios-ssn");
            Service.services.add(140, "emfis-data");
            Service.services.add(141, "emfis-cntl");
            Service.services.add(142, "bl-idm");
            Service.services.add(243, "sur-meas");
            Service.services.add(245, "link");
        }
    }
}
