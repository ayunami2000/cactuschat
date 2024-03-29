// 
// Decompiled by Procyon v0.5.36
// 

package org.fusesource.jansi;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.io.FilterOutputStream;

public class AnsiOutputStream extends FilterOutputStream
{
    public static final byte[] REST_CODE;
    private static final int MAX_ESCAPE_SEQUENCE_LENGTH = 100;
    private byte[] buffer;
    private int pos;
    private int startOfValue;
    private final ArrayList<Object> options;
    private static final int LOOKING_FOR_FIRST_ESC_CHAR = 0;
    private static final int LOOKING_FOR_SECOND_ESC_CHAR = 1;
    private static final int LOOKING_FOR_NEXT_ARG = 2;
    private static final int LOOKING_FOR_STR_ARG_END = 3;
    private static final int LOOKING_FOR_INT_ARG_END = 4;
    private static final int LOOKING_FOR_OSC_COMMAND = 5;
    private static final int LOOKING_FOR_OSC_COMMAND_END = 6;
    private static final int LOOKING_FOR_OSC_PARAM = 7;
    private static final int LOOKING_FOR_ST = 8;
    int state;
    private static final int FIRST_ESC_CHAR = 27;
    private static final int SECOND_ESC_CHAR = 91;
    private static final int SECOND_OSC_CHAR = 93;
    private static final int BEL = 7;
    private static final int SECOND_ST_CHAR = 92;
    protected static final int ERASE_SCREEN_TO_END = 0;
    protected static final int ERASE_SCREEN_TO_BEGINING = 1;
    protected static final int ERASE_SCREEN = 2;
    protected static final int ERASE_LINE_TO_END = 0;
    protected static final int ERASE_LINE_TO_BEGINING = 1;
    protected static final int ERASE_LINE = 2;
    protected static final int ATTRIBUTE_INTENSITY_BOLD = 1;
    protected static final int ATTRIBUTE_INTENSITY_FAINT = 2;
    protected static final int ATTRIBUTE_ITALIC = 3;
    protected static final int ATTRIBUTE_UNDERLINE = 4;
    protected static final int ATTRIBUTE_BLINK_SLOW = 5;
    protected static final int ATTRIBUTE_BLINK_FAST = 6;
    protected static final int ATTRIBUTE_NEGATIVE_ON = 7;
    protected static final int ATTRIBUTE_CONCEAL_ON = 8;
    protected static final int ATTRIBUTE_UNDERLINE_DOUBLE = 21;
    protected static final int ATTRIBUTE_INTENSITY_NORMAL = 22;
    protected static final int ATTRIBUTE_UNDERLINE_OFF = 24;
    protected static final int ATTRIBUTE_BLINK_OFF = 25;
    protected static final int ATTRIBUTE_NEGATIVE_Off = 27;
    protected static final int ATTRIBUTE_CONCEAL_OFF = 28;
    protected static final int BLACK = 0;
    protected static final int RED = 1;
    protected static final int GREEN = 2;
    protected static final int YELLOW = 3;
    protected static final int BLUE = 4;
    protected static final int MAGENTA = 5;
    protected static final int CYAN = 6;
    protected static final int WHITE = 7;
    
    public AnsiOutputStream(final OutputStream os) {
        super(os);
        this.buffer = new byte[100];
        this.pos = 0;
        this.options = new ArrayList<Object>();
        this.state = 0;
    }
    
    @Override
    public void write(final int data) throws IOException {
        switch (this.state) {
            case 0: {
                if (data == 27) {
                    this.buffer[this.pos++] = (byte)data;
                    this.state = 1;
                    break;
                }
                this.out.write(data);
                break;
            }
            case 1: {
                this.buffer[this.pos++] = (byte)data;
                if (data == 91) {
                    this.state = 2;
                    break;
                }
                if (data == 93) {
                    this.state = 5;
                    break;
                }
                this.reset(false);
                break;
            }
            case 2: {
                this.buffer[this.pos++] = (byte)data;
                if (34 == data) {
                    this.startOfValue = this.pos - 1;
                    this.state = 3;
                    break;
                }
                if (48 <= data && data <= 57) {
                    this.startOfValue = this.pos - 1;
                    this.state = 4;
                    break;
                }
                if (59 == data) {
                    this.options.add(null);
                    break;
                }
                if (63 == data) {
                    this.options.add(new Character('?'));
                    break;
                }
                if (61 == data) {
                    this.options.add(new Character('='));
                    break;
                }
                this.reset(this.processEscapeCommand(this.options, data));
                break;
            }
            case 4: {
                this.buffer[this.pos++] = (byte)data;
                if (48 > data || data > 57) {
                    final String strValue = new String(this.buffer, this.startOfValue, this.pos - 1 - this.startOfValue, "UTF-8");
                    final Integer value = new Integer(strValue);
                    this.options.add(value);
                    if (data == 59) {
                        this.state = 2;
                    }
                    else {
                        this.reset(this.processEscapeCommand(this.options, data));
                    }
                    break;
                }
                break;
            }
            case 3: {
                this.buffer[this.pos++] = (byte)data;
                if (34 != data) {
                    final String value2 = new String(this.buffer, this.startOfValue, this.pos - 1 - this.startOfValue, "UTF-8");
                    this.options.add(value2);
                    if (data == 59) {
                        this.state = 2;
                    }
                    else {
                        this.reset(this.processEscapeCommand(this.options, data));
                    }
                    break;
                }
                break;
            }
            case 5: {
                this.buffer[this.pos++] = (byte)data;
                if (48 <= data && data <= 57) {
                    this.startOfValue = this.pos - 1;
                    this.state = 6;
                    break;
                }
                this.reset(false);
                break;
            }
            case 6: {
                this.buffer[this.pos++] = (byte)data;
                if (59 == data) {
                    final String strValue = new String(this.buffer, this.startOfValue, this.pos - 1 - this.startOfValue, "UTF-8");
                    final Integer value = new Integer(strValue);
                    this.options.add(value);
                    this.startOfValue = this.pos;
                    this.state = 7;
                    break;
                }
                if (48 <= data && data <= 57) {
                    break;
                }
                this.reset(false);
                break;
            }
            case 7: {
                this.buffer[this.pos++] = (byte)data;
                if (7 == data) {
                    final String value2 = new String(this.buffer, this.startOfValue, this.pos - 1 - this.startOfValue, "UTF-8");
                    this.options.add(value2);
                    this.reset(this.processOperatingSystemCommand(this.options));
                    break;
                }
                if (27 == data) {
                    this.state = 8;
                    break;
                }
                break;
            }
            case 8: {
                this.buffer[this.pos++] = (byte)data;
                if (92 == data) {
                    final String value2 = new String(this.buffer, this.startOfValue, this.pos - 2 - this.startOfValue, "UTF-8");
                    this.options.add(value2);
                    this.reset(this.processOperatingSystemCommand(this.options));
                    break;
                }
                this.state = 7;
                break;
            }
        }
        if (this.pos >= this.buffer.length) {
            this.reset(false);
        }
    }
    
    private void reset(final boolean skipBuffer) throws IOException {
        if (!skipBuffer) {
            this.out.write(this.buffer, 0, this.pos);
        }
        this.pos = 0;
        this.startOfValue = 0;
        this.options.clear();
        this.state = 0;
    }
    
    private boolean processEscapeCommand(final ArrayList<Object> options, final int command) throws IOException {
        try {
            switch (command) {
                case 65: {
                    this.processCursorUp(this.optionInt(options, 0, 1));
                    return true;
                }
                case 66: {
                    this.processCursorDown(this.optionInt(options, 0, 1));
                    return true;
                }
                case 67: {
                    this.processCursorRight(this.optionInt(options, 0, 1));
                    return true;
                }
                case 68: {
                    this.processCursorLeft(this.optionInt(options, 0, 1));
                    return true;
                }
                case 69: {
                    this.processCursorDownLine(this.optionInt(options, 0, 1));
                    return true;
                }
                case 70: {
                    this.processCursorUpLine(this.optionInt(options, 0, 1));
                    return true;
                }
                case 71: {
                    this.processCursorToColumn(this.optionInt(options, 0));
                    return true;
                }
                case 72:
                case 102: {
                    this.processCursorTo(this.optionInt(options, 0, 1), this.optionInt(options, 1, 1));
                    return true;
                }
                case 74: {
                    this.processEraseScreen(this.optionInt(options, 0, 0));
                    return true;
                }
                case 75: {
                    this.processEraseLine(this.optionInt(options, 0, 0));
                    return true;
                }
                case 83: {
                    this.processScrollUp(this.optionInt(options, 0, 1));
                    return true;
                }
                case 84: {
                    this.processScrollDown(this.optionInt(options, 0, 1));
                    return true;
                }
                case 109: {
                    for (final Object next : options) {
                        if (next != null && next.getClass() != Integer.class) {
                            throw new IllegalArgumentException();
                        }
                    }
                    int count = 0;
                    for (final Object next2 : options) {
                        if (next2 != null) {
                            ++count;
                            final int value = (int)next2;
                            if (30 <= value && value <= 37) {
                                this.processSetForegroundColor(value - 30);
                            }
                            else if (40 <= value && value <= 47) {
                                this.processSetBackgroundColor(value - 40);
                            }
                            else {
                                switch (value) {
                                    case 39: {
                                        this.processDefaultTextColor();
                                        continue;
                                    }
                                    case 49: {
                                        this.processDefaultBackgroundColor();
                                        continue;
                                    }
                                    case 0: {
                                        this.processAttributeRest();
                                        continue;
                                    }
                                    default: {
                                        this.processSetAttribute(value);
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                    if (count == 0) {
                        this.processAttributeRest();
                    }
                    return true;
                }
                case 115: {
                    this.processSaveCursorPosition();
                    return true;
                }
                case 117: {
                    this.processRestoreCursorPosition();
                    return true;
                }
                default: {
                    if (97 <= command && 122 <= command) {
                        this.processUnknownExtension(options, command);
                        return true;
                    }
                    if (65 <= command && 90 <= command) {
                        this.processUnknownExtension(options, command);
                        return true;
                    }
                    return false;
                }
            }
        }
        catch (IllegalArgumentException ignore) {
            return false;
        }
    }
    
    private boolean processOperatingSystemCommand(final ArrayList<Object> options) throws IOException {
        final int command = this.optionInt(options, 0);
        final String label = options.get(1);
        try {
            switch (command) {
                case 0: {
                    this.processChangeIconNameAndWindowTitle(label);
                    return true;
                }
                case 1: {
                    this.processChangeIconName(label);
                    return true;
                }
                case 2: {
                    this.processChangeWindowTitle(label);
                    return true;
                }
                default: {
                    this.processUnknownOperatingSystemCommand(command, label);
                    return true;
                }
            }
        }
        catch (IllegalArgumentException ignore) {
            return false;
        }
    }
    
    protected void processRestoreCursorPosition() throws IOException {
    }
    
    protected void processSaveCursorPosition() throws IOException {
    }
    
    protected void processScrollDown(final int optionInt) throws IOException {
    }
    
    protected void processScrollUp(final int optionInt) throws IOException {
    }
    
    protected void processEraseScreen(final int eraseOption) throws IOException {
    }
    
    protected void processEraseLine(final int eraseOption) throws IOException {
    }
    
    protected void processSetAttribute(final int attribute) throws IOException {
    }
    
    protected void processSetForegroundColor(final int color) throws IOException {
    }
    
    protected void processSetBackgroundColor(final int color) throws IOException {
    }
    
    protected void processDefaultTextColor() throws IOException {
    }
    
    protected void processDefaultBackgroundColor() throws IOException {
    }
    
    protected void processAttributeRest() throws IOException {
    }
    
    protected void processCursorTo(final int row, final int col) throws IOException {
    }
    
    protected void processCursorToColumn(final int x) throws IOException {
    }
    
    protected void processCursorUpLine(final int count) throws IOException {
    }
    
    protected void processCursorDownLine(final int count) throws IOException {
        for (int i = 0; i < count; ++i) {
            this.out.write(10);
        }
    }
    
    protected void processCursorLeft(final int count) throws IOException {
    }
    
    protected void processCursorRight(final int count) throws IOException {
        for (int i = 0; i < count; ++i) {
            this.out.write(32);
        }
    }
    
    protected void processCursorDown(final int count) throws IOException {
    }
    
    protected void processCursorUp(final int count) throws IOException {
    }
    
    protected void processUnknownExtension(final ArrayList<Object> options, final int command) {
    }
    
    protected void processChangeIconNameAndWindowTitle(final String label) {
        this.processChangeIconName(label);
        this.processChangeWindowTitle(label);
    }
    
    protected void processChangeIconName(final String label) {
    }
    
    protected void processChangeWindowTitle(final String label) {
    }
    
    protected void processUnknownOperatingSystemCommand(final int command, final String param) {
    }
    
    private int optionInt(final ArrayList<Object> options, final int index) {
        if (options.size() <= index) {
            throw new IllegalArgumentException();
        }
        final Object value = options.get(index);
        if (value == null) {
            throw new IllegalArgumentException();
        }
        if (!value.getClass().equals(Integer.class)) {
            throw new IllegalArgumentException();
        }
        return (int)value;
    }
    
    private int optionInt(final ArrayList<Object> options, final int index, final int defaultValue) {
        if (options.size() <= index) {
            return defaultValue;
        }
        final Object value = options.get(index);
        if (value == null) {
            return defaultValue;
        }
        return (int)value;
    }
    
    @Override
    public void close() throws IOException {
        this.write(AnsiOutputStream.REST_CODE);
        this.flush();
        super.close();
    }
    
    private static byte[] resetCode() {
        try {
            return new Ansi().reset().toString().getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    static {
        REST_CODE = resetCode();
    }
}
