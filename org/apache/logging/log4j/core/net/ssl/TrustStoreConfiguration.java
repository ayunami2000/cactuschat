// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.net.ssl;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import javax.net.ssl.TrustManagerFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "TrustStore", category = "Core", printObject = true)
public class TrustStoreConfiguration extends AbstractKeyStoreConfiguration
{
    private final String trustManagerFactoryAlgorithm;
    
    public TrustStoreConfiguration(final String location, final String password, final String keyStoreType, final String trustManagerFactoryAlgorithm) throws StoreConfigurationException {
        super(location, password, keyStoreType);
        this.trustManagerFactoryAlgorithm = ((trustManagerFactoryAlgorithm == null) ? TrustManagerFactory.getDefaultAlgorithm() : trustManagerFactoryAlgorithm);
    }
    
    @PluginFactory
    public static TrustStoreConfiguration createKeyStoreConfiguration(@PluginAttribute("location") final String location, @PluginAttribute("password") final String password, @PluginAttribute("type") final String keyStoreType, @PluginAttribute("trustManagerFactoryAlgorithm") final String trustManagerFactoryAlgorithm) throws StoreConfigurationException {
        return new TrustStoreConfiguration(location, password, keyStoreType, trustManagerFactoryAlgorithm);
    }
    
    public TrustManagerFactory initTrustManagerFactory() throws NoSuchAlgorithmException, KeyStoreException {
        final TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(this.trustManagerFactoryAlgorithm);
        tmFactory.init(this.getKeyStore());
        return tmFactory;
    }
}
