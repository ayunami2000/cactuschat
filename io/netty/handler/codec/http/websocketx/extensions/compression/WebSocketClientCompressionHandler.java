// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http.websocketx.extensions.compression;

import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandshaker;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;

public class WebSocketClientCompressionHandler extends WebSocketClientExtensionHandler
{
    public WebSocketClientCompressionHandler() {
        super(new WebSocketClientExtensionHandshaker[] { new PerMessageDeflateClientExtensionHandshaker(), new DeflateFrameClientExtensionHandshaker(false), new DeflateFrameClientExtensionHandshaker(true) });
    }
}
