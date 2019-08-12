// 
// Decompiled by Procyon v0.5.36
// 

package org.fusesource.jansi;

import java.util.Iterator;
import java.util.ArrayList;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

public class HtmlAnsiOutputStream extends AnsiOutputStream
{
    private boolean concealOn;
    private static final String[] ANSI_COLOR_MAP;
    private static final byte[] BYTES_QUOT;
    private static final byte[] BYTES_AMP;
    private static final byte[] BYTES_LT;
    private static final byte[] BYTES_GT;
    private List<String> closingAttributes;
    
    @Override
    public void close() throws IOException {
        this.closeAttributes();
        super.close();
    }
    
    public HtmlAnsiOutputStream(final OutputStream os) {
        super(os);
        this.concealOn = false;
        this.closingAttributes = new ArrayList<String>();
    }
    
    private void write(final String s) throws IOException {
        super.out.write(s.getBytes());
    }
    
    private void writeAttribute(final String s) throws IOException {
        this.write("<" + s + ">");
        this.closingAttributes.add(0, s.split(" ", 2)[0]);
    }
    
    private void closeAttributes() throws IOException {
        for (final String attr : this.closingAttributes) {
            this.write("</" + attr + ">");
        }
        this.closingAttributes.clear();
    }
    
    @Override
    public void write(final int data) throws IOException {
        switch (data) {
            case 34: {
                this.out.write(HtmlAnsiOutputStream.BYTES_QUOT);
                break;
            }
            case 38: {
                this.out.write(HtmlAnsiOutputStream.BYTES_AMP);
                break;
            }
            case 60: {
                this.out.write(HtmlAnsiOutputStream.BYTES_LT);
                break;
            }
            case 62: {
                this.out.write(HtmlAnsiOutputStream.BYTES_GT);
                break;
            }
            default: {
                super.write(data);
                break;
            }
        }
    }
    
    public void writeLine(final byte[] buf, final int offset, final int len) throws IOException {
        this.write(buf, offset, len);
        this.closeAttributes();
    }
    
    @Override
    protected void processSetAttribute(final int attribute) throws IOException {
        switch (attribute) {
            case 8: {
                this.write("\u001b[8m");
                this.concealOn = true;
                break;
            }
            case 1: {
                this.writeAttribute("b");
                break;
            }
            case 22: {
                this.closeAttributes();
                break;
            }
            case 4: {
                this.writeAttribute("u");
                break;
            }
            case 24: {
                this.closeAttributes();
            }
        }
    }
    
    @Override
    protected void processAttributeRest() throws IOException {
        if (this.concealOn) {
            this.write("\u001b[0m");
            this.concealOn = false;
        }
        this.closeAttributes();
    }
    
    @Override
    protected void processSetForegroundColor(final int color) throws IOException {
        this.writeAttribute("span style=\"color: " + HtmlAnsiOutputStream.ANSI_COLOR_MAP[color] + ";\"");
    }
    
    @Override
    protected void processSetBackgroundColor(final int color) throws IOException {
        this.writeAttribute("span style=\"background-color: " + HtmlAnsiOutputStream.ANSI_COLOR_MAP[color] + ";\"");
    }
    
    static {
        ANSI_COLOR_MAP = new String[] { "black", "red", "green", "yellow", "blue", "magenta", "cyan", "white" };
        BYTES_QUOT = "&quot;".getBytes();
        BYTES_AMP = "&amp;".getBytes();
        BYTES_LT = "&lt;".getBytes();
        BYTES_GT = "&gt;".getBytes();
    }
}
