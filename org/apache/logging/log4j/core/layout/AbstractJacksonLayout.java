// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.layout;

import java.io.Serializable;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.core.LogEvent;
import java.nio.charset.Charset;
import com.fasterxml.jackson.databind.ObjectWriter;

abstract class AbstractJacksonLayout extends AbstractStringLayout
{
    protected static final String DEFAULT_EOL = "\r\n";
    protected static final String COMPACT_EOL = "";
    private static final long serialVersionUID = 1L;
    protected final String eol;
    protected final ObjectWriter objectWriter;
    protected final boolean compact;
    protected final boolean complete;
    
    protected AbstractJacksonLayout(final ObjectWriter objectWriter, final Charset charset, final boolean compact, final boolean complete, final boolean eventEol) {
        super(charset);
        this.objectWriter = objectWriter;
        this.compact = compact;
        this.complete = complete;
        this.eol = ((compact && !eventEol) ? "" : "\r\n");
    }
    
    @Override
    public String toSerializable(final LogEvent event) {
        try {
            return this.objectWriter.writeValueAsString((Object)event) + this.eol;
        }
        catch (JsonProcessingException e) {
            AbstractJacksonLayout.LOGGER.error(e);
            return "";
        }
    }
}
