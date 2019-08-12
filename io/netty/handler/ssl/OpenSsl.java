// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import java.util.Collections;
import org.apache.tomcat.jni.SSLContext;
import org.apache.tomcat.jni.Pool;
import java.util.LinkedHashSet;
import org.apache.tomcat.jni.Library;
import io.netty.util.internal.NativeLibraryLoader;
import org.apache.tomcat.jni.SSL;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.Set;
import io.netty.util.internal.logging.InternalLogger;

public final class OpenSsl
{
    private static final InternalLogger logger;
    private static final Throwable UNAVAILABILITY_CAUSE;
    private static final Set<String> AVAILABLE_CIPHER_SUITES;
    
    public static boolean isAvailable() {
        return OpenSsl.UNAVAILABILITY_CAUSE == null;
    }
    
    public static void ensureAvailability() {
        if (OpenSsl.UNAVAILABILITY_CAUSE != null) {
            throw (Error)new UnsatisfiedLinkError("failed to load the required native library").initCause(OpenSsl.UNAVAILABILITY_CAUSE);
        }
    }
    
    public static Throwable unavailabilityCause() {
        return OpenSsl.UNAVAILABILITY_CAUSE;
    }
    
    public static Set<String> availableCipherSuites() {
        return OpenSsl.AVAILABLE_CIPHER_SUITES;
    }
    
    public static boolean isCipherSuiteAvailable(String cipherSuite) {
        final String converted = CipherSuiteConverter.toOpenSsl(cipherSuite);
        if (converted != null) {
            cipherSuite = converted;
        }
        return OpenSsl.AVAILABLE_CIPHER_SUITES.contains(cipherSuite);
    }
    
    static boolean isError(final long errorCode) {
        return errorCode != 0L;
    }
    
    private OpenSsl() {
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(OpenSsl.class);
        Throwable cause = null;
        try {
            Class.forName("org.apache.tomcat.jni.SSL", false, OpenSsl.class.getClassLoader());
        }
        catch (ClassNotFoundException t) {
            cause = t;
            OpenSsl.logger.debug("netty-tcnative not in the classpath; " + OpenSslEngine.class.getSimpleName() + " will be unavailable.");
        }
        if (cause == null) {
            try {
                NativeLibraryLoader.load("netty-tcnative", SSL.class.getClassLoader());
                Library.initialize("provided");
                SSL.initialize((String)null);
            }
            catch (Throwable t2) {
                cause = t2;
                OpenSsl.logger.debug("Failed to load netty-tcnative; " + OpenSslEngine.class.getSimpleName() + " will be unavailable. " + "See http://netty.io/wiki/forked-tomcat-native.html for more information.", t2);
            }
        }
        if ((UNAVAILABILITY_CAUSE = cause) == null) {
            final Set<String> availableCipherSuites = new LinkedHashSet<String>(128);
            final long aprPool = Pool.create(0L);
            try {
                final long sslCtx = SSLContext.make(aprPool, 28, 1);
                try {
                    SSLContext.setOptions(sslCtx, 4095);
                    SSLContext.setCipherSuite(sslCtx, "ALL");
                    final long ssl = SSL.newSSL(sslCtx, true);
                    try {
                        for (final String c : SSL.getCiphers(ssl)) {
                            if (c != null && c.length() != 0) {
                                if (!availableCipherSuites.contains(c)) {
                                    availableCipherSuites.add(c);
                                }
                            }
                        }
                    }
                    finally {
                        SSL.freeSSL(ssl);
                    }
                }
                finally {
                    SSLContext.free(sslCtx);
                }
            }
            catch (Exception e) {
                OpenSsl.logger.warn("Failed to get the list of available OpenSSL cipher suites.", e);
            }
            finally {
                Pool.destroy(aprPool);
            }
            AVAILABLE_CIPHER_SUITES = Collections.unmodifiableSet((Set<? extends String>)availableCipherSuites);
        }
        else {
            AVAILABLE_CIPHER_SUITES = Collections.emptySet();
        }
    }
}
