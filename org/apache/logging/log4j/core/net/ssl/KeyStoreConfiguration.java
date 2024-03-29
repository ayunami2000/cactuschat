// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.net.ssl;

import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.NoSuchAlgorithmException;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import javax.net.ssl.KeyManagerFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "KeyStore", category = "Core", printObject = true)
public class KeyStoreConfiguration extends AbstractKeyStoreConfiguration
{
    private final String keyManagerFactoryAlgorithm;
    
    public KeyStoreConfiguration(final String location, final String password, final String keyStoreType, final String keyManagerFactoryAlgorithm) throws StoreConfigurationException {
        super(location, password, keyStoreType);
        this.keyManagerFactoryAlgorithm = ((keyManagerFactoryAlgorithm == null) ? KeyManagerFactory.getDefaultAlgorithm() : keyManagerFactoryAlgorithm);
    }
    
    @PluginFactory
    public static KeyStoreConfiguration createKeyStoreConfiguration(@PluginAttribute("location") final String location, @PluginAttribute("password") final String password, @PluginAttribute("type") final String keyStoreType, @PluginAttribute("keyManagerFactoryAlgorithm") final String keyManagerFactoryAlgorithm) throws StoreConfigurationException {
        return new KeyStoreConfiguration(location, password, keyStoreType, keyManagerFactoryAlgorithm);
    }
    
    public KeyManagerFactory initKeyManagerFactory() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        final KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(this.keyManagerFactoryAlgorithm);
        kmFactory.init(this.getKeyStore(), this.getPasswordAsCharArray());
        return kmFactory;
    }
}
