// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.net.ssl;

import java.io.FileNotFoundException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

public class AbstractKeyStoreConfiguration extends StoreConfiguration<KeyStore>
{
    private final KeyStore keyStore;
    private final String keyStoreType;
    
    public AbstractKeyStoreConfiguration(final String location, final String password, final String keyStoreType) throws StoreConfigurationException {
        super(location, password);
        this.keyStoreType = ((keyStoreType == null) ? "JKS" : keyStoreType);
        this.keyStore = this.load();
    }
    
    @Override
    protected KeyStore load() throws StoreConfigurationException {
        AbstractKeyStoreConfiguration.LOGGER.debug("Loading keystore from file with params(location={})", new Object[] { this.getLocation() });
        try {
            if (this.getLocation() == null) {
                throw new IOException("The location is null");
            }
            try (final FileInputStream fin = new FileInputStream(this.getLocation())) {
                final KeyStore ks = KeyStore.getInstance(this.keyStoreType);
                ks.load(fin, this.getPasswordAsCharArray());
                AbstractKeyStoreConfiguration.LOGGER.debug("Keystore successfully loaded with params(location={})", new Object[] { this.getLocation() });
                return ks;
            }
        }
        catch (CertificateException e) {
            AbstractKeyStoreConfiguration.LOGGER.error("No Provider supports a KeyStoreSpi implementation for the specified type {}", new Object[] { this.keyStoreType });
            throw new StoreConfigurationException(e);
        }
        catch (NoSuchAlgorithmException e2) {
            AbstractKeyStoreConfiguration.LOGGER.error("The algorithm used to check the integrity of the keystore cannot be found");
            throw new StoreConfigurationException(e2);
        }
        catch (KeyStoreException e3) {
            AbstractKeyStoreConfiguration.LOGGER.error(e3);
            throw new StoreConfigurationException(e3);
        }
        catch (FileNotFoundException e4) {
            AbstractKeyStoreConfiguration.LOGGER.error("The keystore file({}) is not found", new Object[] { this.getLocation() });
            throw new StoreConfigurationException(e4);
        }
        catch (IOException e5) {
            AbstractKeyStoreConfiguration.LOGGER.error("Something is wrong with the format of the keystore or the given password");
            throw new StoreConfigurationException(e5);
        }
    }
    
    public KeyStore getKeyStore() {
        return this.keyStore;
    }
}
