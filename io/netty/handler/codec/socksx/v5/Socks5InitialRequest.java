// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.socksx.v5;

import java.util.List;

public interface Socks5InitialRequest extends Socks5Message
{
    List<Socks5AuthMethod> authMethods();
}
