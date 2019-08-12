// 
// Decompiled by Procyon v0.5.36
// 

package jline;

import org.fusesource.jansi.internal.Kernel32;
import org.fusesource.jansi.internal.WindowsSupport;
import java.nio.charset.Charset;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import jline.internal.Log;
import jline.internal.Configuration;

public class WindowsTerminal extends TerminalSupport
{
    public static final String DIRECT_CONSOLE;
    public static final String ANSI;
    private boolean directConsole;
    private int originalMode;
    
    public WindowsTerminal() throws Exception {
        super(true);
    }
    
    @Override
    public void init() throws Exception {
        super.init();
        this.setAnsiSupported(Configuration.getBoolean(WindowsTerminal.ANSI, true));
        this.setDirectConsole(Configuration.getBoolean(WindowsTerminal.DIRECT_CONSOLE, true));
        this.originalMode = this.getConsoleMode();
        this.setConsoleMode(this.originalMode & ~ConsoleMode.ENABLE_ECHO_INPUT.code);
        this.setEchoEnabled(false);
    }
    
    @Override
    public void restore() throws Exception {
        this.setConsoleMode(this.originalMode);
        super.restore();
    }
    
    @Override
    public int getWidth() {
        final int w = this.getWindowsTerminalWidth();
        return (w < 1) ? 80 : w;
    }
    
    @Override
    public int getHeight() {
        final int h = this.getWindowsTerminalHeight();
        return (h < 1) ? 24 : h;
    }
    
    @Override
    public void setEchoEnabled(final boolean enabled) {
        if (enabled) {
            this.setConsoleMode(this.getConsoleMode() | ConsoleMode.ENABLE_ECHO_INPUT.code | ConsoleMode.ENABLE_LINE_INPUT.code | ConsoleMode.ENABLE_PROCESSED_INPUT.code | ConsoleMode.ENABLE_WINDOW_INPUT.code);
        }
        else {
            this.setConsoleMode(this.getConsoleMode() & ~(ConsoleMode.ENABLE_LINE_INPUT.code | ConsoleMode.ENABLE_ECHO_INPUT.code | ConsoleMode.ENABLE_PROCESSED_INPUT.code | ConsoleMode.ENABLE_WINDOW_INPUT.code));
        }
        super.setEchoEnabled(enabled);
    }
    
    public void setDirectConsole(final boolean flag) {
        this.directConsole = flag;
        Log.debug("Direct console: ", flag);
    }
    
    public Boolean getDirectConsole() {
        return this.directConsole;
    }
    
    @Override
    public InputStream wrapInIfNeeded(final InputStream in) throws IOException {
        if (this.directConsole && this.isSystemIn(in)) {
            return new InputStream() {
                private byte[] buf = null;
                int bufIdx = 0;
                
                @Override
                public int read() throws IOException {
                    while (this.buf == null || this.bufIdx == this.buf.length) {
                        this.buf = WindowsTerminal.this.readConsoleInput();
                        this.bufIdx = 0;
                    }
                    final int c = this.buf[this.bufIdx] & 0xFF;
                    ++this.bufIdx;
                    return c;
                }
            };
        }
        return super.wrapInIfNeeded(in);
    }
    
    protected boolean isSystemIn(final InputStream in) throws IOException {
        return in != null && (in == System.in || (in instanceof FileInputStream && ((FileInputStream)in).getFD() == FileDescriptor.in));
    }
    
    @Override
    public String getOutputEncoding() {
        final int codepage = this.getConsoleOutputCodepage();
        final String charsetMS = "ms" + codepage;
        if (Charset.isSupported(charsetMS)) {
            return charsetMS;
        }
        final String charsetCP = "cp" + codepage;
        if (Charset.isSupported(charsetCP)) {
            return charsetCP;
        }
        Log.debug("can't figure out the Java Charset of this code page (" + codepage + ")...");
        return super.getOutputEncoding();
    }
    
    private int getConsoleMode() {
        return WindowsSupport.getConsoleMode();
    }
    
    private void setConsoleMode(final int mode) {
        WindowsSupport.setConsoleMode(mode);
    }
    
    private byte[] readConsoleInput() {
        Kernel32.INPUT_RECORD[] events = null;
        try {
            events = WindowsSupport.readConsoleInput(1);
        }
        catch (IOException e) {
            Log.debug("read Windows console input error: ", e);
        }
        if (events == null) {
            return new byte[0];
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < events.length; ++i) {
            final Kernel32.KEY_EVENT_RECORD keyEvent = events[i].keyEvent;
            if (keyEvent.keyDown) {
                if (keyEvent.uchar > '\0') {
                    final int altState = Kernel32.KEY_EVENT_RECORD.LEFT_ALT_PRESSED | Kernel32.KEY_EVENT_RECORD.RIGHT_ALT_PRESSED;
                    if (((keyEvent.uchar >= '@' && keyEvent.uchar <= '_') || (keyEvent.uchar >= 'a' && keyEvent.uchar <= 'z')) && (keyEvent.controlKeyState & altState) != 0x0) {
                        sb.append('\u001b');
                    }
                    sb.append(keyEvent.uchar);
                }
                else {
                    String escapeSequence = null;
                    switch (keyEvent.keyCode) {
                        case 33: {
                            escapeSequence = "\u001b[5~";
                            break;
                        }
                        case 34: {
                            escapeSequence = "\u001b[6~";
                            break;
                        }
                        case 35: {
                            escapeSequence = "\u001b[4~";
                            break;
                        }
                        case 36: {
                            escapeSequence = "\u001b[1~";
                            break;
                        }
                        case 37: {
                            escapeSequence = "\u001b[D";
                            break;
                        }
                        case 38: {
                            escapeSequence = "\u001b[A";
                            break;
                        }
                        case 39: {
                            escapeSequence = "\u001b[C";
                            break;
                        }
                        case 40: {
                            escapeSequence = "\u001b[B";
                            break;
                        }
                        case 45: {
                            escapeSequence = "\u001b[2~";
                            break;
                        }
                        case 46: {
                            escapeSequence = "\u001b[3~";
                            break;
                        }
                    }
                    if (escapeSequence != null) {
                        for (int k = 0; k < keyEvent.repeatCount; ++k) {
                            sb.append(escapeSequence);
                        }
                    }
                }
            }
            else if (keyEvent.keyCode == 18 && keyEvent.uchar > '\0') {
                sb.append(keyEvent.uchar);
            }
        }
        return sb.toString().getBytes();
    }
    
    private int getConsoleOutputCodepage() {
        return Kernel32.GetConsoleOutputCP();
    }
    
    private int getWindowsTerminalWidth() {
        return WindowsSupport.getWindowsTerminalWidth();
    }
    
    private int getWindowsTerminalHeight() {
        return WindowsSupport.getWindowsTerminalHeight();
    }
    
    static {
        DIRECT_CONSOLE = WindowsTerminal.class.getName() + ".directConsole";
        ANSI = WindowsTerminal.class.getName() + ".ansi";
    }
    
    public enum ConsoleMode
    {
        ENABLE_LINE_INPUT(2), 
        ENABLE_ECHO_INPUT(4), 
        ENABLE_PROCESSED_INPUT(1), 
        ENABLE_WINDOW_INPUT(8), 
        ENABLE_MOUSE_INPUT(16), 
        ENABLE_PROCESSED_OUTPUT(1), 
        ENABLE_WRAP_AT_EOL_OUTPUT(2);
        
        public final int code;
        
        private ConsoleMode(final int code) {
            this.code = code;
        }
    }
}
