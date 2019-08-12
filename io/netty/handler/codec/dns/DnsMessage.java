// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

import io.netty.util.ReferenceCounted;
import java.util.Iterator;
import io.netty.util.ReferenceCountUtil;
import java.util.LinkedList;
import java.util.Collections;
import java.util.List;
import io.netty.util.AbstractReferenceCounted;

public abstract class DnsMessage extends AbstractReferenceCounted
{
    private List<DnsQuestion> questions;
    private List<DnsResource> answers;
    private List<DnsResource> authority;
    private List<DnsResource> additional;
    private final DnsHeader header;
    
    DnsMessage(final int id) {
        this.header = this.newHeader(id);
    }
    
    public DnsHeader header() {
        return this.header;
    }
    
    public List<DnsQuestion> questions() {
        if (this.questions == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList((List<? extends DnsQuestion>)this.questions);
    }
    
    public List<DnsResource> answers() {
        if (this.answers == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList((List<? extends DnsResource>)this.answers);
    }
    
    public List<DnsResource> authorityResources() {
        if (this.authority == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList((List<? extends DnsResource>)this.authority);
    }
    
    public List<DnsResource> additionalResources() {
        if (this.additional == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList((List<? extends DnsResource>)this.additional);
    }
    
    public DnsMessage addAnswer(final DnsResource answer) {
        if (this.answers == null) {
            this.answers = new LinkedList<DnsResource>();
        }
        this.answers.add(answer);
        return this;
    }
    
    public DnsMessage addQuestion(final DnsQuestion question) {
        if (this.questions == null) {
            this.questions = new LinkedList<DnsQuestion>();
        }
        this.questions.add(question);
        return this;
    }
    
    public DnsMessage addAuthorityResource(final DnsResource resource) {
        if (this.authority == null) {
            this.authority = new LinkedList<DnsResource>();
        }
        this.authority.add(resource);
        return this;
    }
    
    public DnsMessage addAdditionalResource(final DnsResource resource) {
        if (this.additional == null) {
            this.additional = new LinkedList<DnsResource>();
        }
        this.additional.add(resource);
        return this;
    }
    
    @Override
    protected void deallocate() {
    }
    
    @Override
    public boolean release() {
        release(this.questions());
        release(this.answers());
        release(this.additionalResources());
        release(this.authorityResources());
        return super.release();
    }
    
    private static void release(final List<?> resources) {
        for (final Object resource : resources) {
            ReferenceCountUtil.release(resource);
        }
    }
    
    @Override
    public boolean release(final int decrement) {
        release(this.questions(), decrement);
        release(this.answers(), decrement);
        release(this.additionalResources(), decrement);
        release(this.authorityResources(), decrement);
        return super.release(decrement);
    }
    
    private static void release(final List<?> resources, final int decrement) {
        for (final Object resource : resources) {
            ReferenceCountUtil.release(resource, decrement);
        }
    }
    
    @Override
    public DnsMessage touch(final Object hint) {
        touch(this.questions(), hint);
        touch(this.answers(), hint);
        touch(this.additionalResources(), hint);
        touch(this.authorityResources(), hint);
        return this;
    }
    
    private static void touch(final List<?> resources, final Object hint) {
        for (final Object resource : resources) {
            ReferenceCountUtil.touch(resource, hint);
        }
    }
    
    @Override
    public DnsMessage retain() {
        retain(this.questions());
        retain(this.answers());
        retain(this.additionalResources());
        retain(this.authorityResources());
        super.retain();
        return this;
    }
    
    private static void retain(final List<?> resources) {
        for (final Object resource : resources) {
            ReferenceCountUtil.retain(resource);
        }
    }
    
    @Override
    public DnsMessage retain(final int increment) {
        retain(this.questions(), increment);
        retain(this.answers(), increment);
        retain(this.additionalResources(), increment);
        retain(this.authorityResources(), increment);
        super.retain(increment);
        return this;
    }
    
    private static void retain(final List<?> resources, final int increment) {
        for (final Object resource : resources) {
            ReferenceCountUtil.retain(resource, increment);
        }
    }
    
    @Override
    public DnsMessage touch() {
        super.touch();
        return this;
    }
    
    protected abstract DnsHeader newHeader(final int p0);
}
