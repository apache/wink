package org.apache.wink.common.internal.registry.metadata;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import junit.framework.TestCase;

public class ProviderMetadataCollectorTest extends TestCase {

    @Path("/")
    public interface Interface {

        @GET
        @Produces("text/plain")
        public String method();

    }
    
    @Path("/")
    public class Class {
        
        @GET
        @Produces("text/plain")
        public String method() {
            return "method";
        }
    }
    
    @Provider
    public interface ProviderInterface {
        
    }
    
    public class ProviderInterfaceImpl implements ProviderInterface {
        
    }
    
    @Provider
    public abstract class AbstractProvider {
        
    }
    
    public class ProviderBaseClass extends AbstractProvider {
        
    }
    
    @Provider
    public static class ProviderStandalone {
        
    }
    
    public void testIsProvider() {
        assertFalse(ProviderMetadataCollector.isProvider(Interface.class));
        assertFalse(ProviderMetadataCollector.isProvider(Class.class));
        assertFalse(ProviderMetadataCollector.isProvider(ProviderInterface.class));
        assertTrue(ProviderMetadataCollector.isProvider(ProviderInterfaceImpl.class));
        assertFalse(ProviderMetadataCollector.isProvider(AbstractProvider.class));
        assertTrue(ProviderMetadataCollector.isProvider(ProviderBaseClass.class));
        assertTrue(ProviderMetadataCollector.isProvider(ProviderStandalone.class));
    }
}
