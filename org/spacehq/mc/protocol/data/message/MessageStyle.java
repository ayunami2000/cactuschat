// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.message;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class MessageStyle implements Cloneable
{
    private static final MessageStyle DEFAULT;
    private ChatColor color;
    private List<ChatFormat> formats;
    private ClickEvent click;
    private HoverEvent hover;
    private String insertion;
    private MessageStyle parent;
    
    public MessageStyle() {
        this.color = ChatColor.WHITE;
        this.formats = new ArrayList<ChatFormat>();
        this.parent = MessageStyle.DEFAULT;
    }
    
    public boolean isDefault() {
        return this.equals(MessageStyle.DEFAULT);
    }
    
    public ChatColor getColor() {
        return this.color;
    }
    
    public List<ChatFormat> getFormats() {
        return new ArrayList<ChatFormat>(this.formats);
    }
    
    public ClickEvent getClickEvent() {
        return this.click;
    }
    
    public HoverEvent getHoverEvent() {
        return this.hover;
    }
    
    public String getInsertion() {
        return this.insertion;
    }
    
    public MessageStyle getParent() {
        return this.parent;
    }
    
    public MessageStyle setColor(final ChatColor color) {
        this.color = color;
        return this;
    }
    
    public MessageStyle setFormats(final List<ChatFormat> formats) {
        this.formats = new ArrayList<ChatFormat>(formats);
        return this;
    }
    
    public MessageStyle addFormat(final ChatFormat format) {
        this.formats.add(format);
        return this;
    }
    
    public MessageStyle removeFormat(final ChatFormat format) {
        this.formats.remove(format);
        return this;
    }
    
    public MessageStyle clearFormats() {
        this.formats.clear();
        return this;
    }
    
    public MessageStyle setClickEvent(final ClickEvent event) {
        this.click = event;
        return this;
    }
    
    public MessageStyle setHoverEvent(final HoverEvent event) {
        this.hover = event;
        return this;
    }
    
    public MessageStyle setInsertion(final String insertion) {
        this.insertion = insertion;
        return this;
    }
    
    protected MessageStyle setParent(MessageStyle parent) {
        if (parent == null) {
            parent = MessageStyle.DEFAULT;
        }
        this.parent = parent;
        return this;
    }
    
    @Override
    public String toString() {
        return "MessageStyle{color=" + this.color + ",formats=" + this.formats + ",clickEvent=" + this.click + ",hoverEvent=" + this.hover + ",insertion=" + this.insertion + "}";
    }
    
    public MessageStyle clone() {
        return new MessageStyle().setParent(this.parent).setColor(this.color).setFormats(this.formats).setClickEvent((this.click != null) ? this.click.clone() : null).setHoverEvent((this.hover != null) ? this.hover.clone() : null).setInsertion(this.insertion);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o instanceof MessageStyle && this.color == ((MessageStyle)o).color && this.formats.equals(((MessageStyle)o).formats)) {
            if (this.click != null) {
                if (!this.click.equals(((MessageStyle)o).click)) {
                    return false;
                }
            }
            else if (((MessageStyle)o).click != null) {
                return false;
            }
            if (this.hover != null) {
                if (!this.hover.equals(((MessageStyle)o).hover)) {
                    return false;
                }
            }
            else if (((MessageStyle)o).hover != null) {
                return false;
            }
            if (this.insertion != null) {
                if (!this.insertion.equals(((MessageStyle)o).insertion)) {
                    return false;
                }
            }
            else if (((MessageStyle)o).insertion != null) {
                return false;
            }
            if (this.parent.equals(((MessageStyle)o).parent)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = this.color.hashCode();
        result = 31 * result + this.formats.hashCode();
        result = 31 * result + ((this.click != null) ? this.click.hashCode() : 0);
        result = 31 * result + ((this.hover != null) ? this.hover.hashCode() : 0);
        result = 31 * result + ((this.insertion != null) ? this.insertion.hashCode() : 0);
        result = 31 * result + this.parent.hashCode();
        return result;
    }
    
    static {
        DEFAULT = new MessageStyle();
    }
}
