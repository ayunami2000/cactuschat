// 
// Decompiled by Procyon v0.5.36
// 

package jline.console;

import java.util.ListIterator;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.awt.datatransfer.DataFlavor;
import java.awt.Toolkit;
import java.util.Iterator;
import jline.internal.Preconditions;
import java.util.Collections;
import java.util.Collection;
import java.awt.event.ActionEvent;
import java.util.Stack;
import jline.UnixTerminal;
import java.util.Arrays;
import jline.internal.Log;
import org.fusesource.jansi.AnsiOutputStream;
import java.io.ByteArrayOutputStream;
import jline.internal.InputStreamReader;
import java.io.ByteArrayInputStream;
import jline.internal.Urls;
import java.io.File;
import java.io.OutputStreamWriter;
import jline.TerminalFactory;
import java.util.HashMap;
import jline.console.history.MemoryHistory;
import jline.console.completer.CandidateListCompletionHandler;
import java.util.LinkedList;
import jline.internal.Configuration;
import jline.internal.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileDescriptor;
import java.awt.event.ActionListener;
import java.util.Map;
import jline.console.history.History;
import jline.console.completer.CompletionHandler;
import jline.console.completer.Completer;
import java.util.List;
import java.net.URL;
import java.io.Reader;
import jline.internal.NonBlockingInputStream;
import java.io.Writer;
import jline.Terminal;
import java.util.ResourceBundle;

public class ConsoleReader
{
    public static final String JLINE_NOBELL = "jline.nobell";
    public static final String JLINE_ESC_TIMEOUT = "jline.esc.timeout";
    public static final String JLINE_INPUTRC = "jline.inputrc";
    public static final String INPUT_RC = ".inputrc";
    public static final String DEFAULT_INPUT_RC = "/etc/inputrc";
    public static final char BACKSPACE = '\b';
    public static final char RESET_LINE = '\r';
    public static final char KEYBOARD_BELL = '\u0007';
    public static final char NULL_MASK = '\0';
    public static final int TAB_WIDTH = 4;
    private static final ResourceBundle resources;
    private final Terminal terminal;
    private final Writer out;
    private final CursorBuffer buf;
    private String prompt;
    private int promptLen;
    private boolean expandEvents;
    private boolean bellEnabled;
    private boolean handleUserInterrupt;
    private Character mask;
    private Character echoCharacter;
    private StringBuffer searchTerm;
    private String previousSearchTerm;
    private int searchIndex;
    private int parenBlinkTimeout;
    private NonBlockingInputStream in;
    private long escapeTimeout;
    private Reader reader;
    private boolean isUnitTestInput;
    private char charSearchChar;
    private char charSearchLastInvokeChar;
    private char charSearchFirstInvokeChar;
    private String yankBuffer;
    private KillRing killRing;
    private String encoding;
    private boolean recording;
    private String macro;
    private String appName;
    private URL inputrcUrl;
    private ConsoleKeys consoleKeys;
    private String commentBegin;
    private boolean skipLF;
    private boolean copyPasteDetection;
    private State state;
    public static final String JLINE_COMPLETION_THRESHOLD = "jline.completion.threshold";
    private final List<Completer> completers;
    private CompletionHandler completionHandler;
    private int autoprintThreshold;
    private boolean paginationEnabled;
    private History history;
    private boolean historyEnabled;
    public static final String CR;
    private final Map<Character, ActionListener> triggeredActions;
    private Thread maskThread;
    
    public ConsoleReader() throws IOException {
        this(null, new FileInputStream(FileDescriptor.in), System.out, null);
    }
    
    public ConsoleReader(final InputStream in, final OutputStream out) throws IOException {
        this(null, in, out, null);
    }
    
    public ConsoleReader(final InputStream in, final OutputStream out, final Terminal term) throws IOException {
        this(null, in, out, term);
    }
    
    public ConsoleReader(@Nullable final String appName, final InputStream in, final OutputStream out, @Nullable final Terminal term) throws IOException {
        this(appName, in, out, term, null);
    }
    
    public ConsoleReader(@Nullable final String appName, final InputStream in, final OutputStream out, @Nullable final Terminal term, @Nullable final String encoding) throws IOException {
        this.buf = new CursorBuffer();
        this.expandEvents = true;
        this.bellEnabled = !Configuration.getBoolean("jline.nobell", true);
        this.handleUserInterrupt = false;
        this.searchTerm = null;
        this.previousSearchTerm = "";
        this.searchIndex = -1;
        this.parenBlinkTimeout = 500;
        this.charSearchChar = '\0';
        this.charSearchLastInvokeChar = '\0';
        this.charSearchFirstInvokeChar = '\0';
        this.yankBuffer = "";
        this.killRing = new KillRing();
        this.macro = "";
        this.commentBegin = null;
        this.skipLF = false;
        this.copyPasteDetection = false;
        this.state = State.NORMAL;
        this.completers = new LinkedList<Completer>();
        this.completionHandler = new CandidateListCompletionHandler();
        this.autoprintThreshold = Configuration.getInteger("jline.completion.threshold", 100);
        this.history = new MemoryHistory();
        this.historyEnabled = true;
        this.triggeredActions = new HashMap<Character, ActionListener>();
        this.appName = ((appName != null) ? appName : "JLine");
        this.encoding = ((encoding != null) ? encoding : Configuration.getEncoding());
        this.terminal = ((term != null) ? term : TerminalFactory.get());
        final String outEncoding = (this.terminal.getOutputEncoding() != null) ? this.terminal.getOutputEncoding() : this.encoding;
        this.out = new OutputStreamWriter(this.terminal.wrapOutIfNeeded(out), outEncoding);
        this.setInput(in);
        this.inputrcUrl = this.getInputRc();
        this.consoleKeys = new ConsoleKeys(this.appName, this.inputrcUrl);
    }
    
    private URL getInputRc() throws IOException {
        final String path = Configuration.getString("jline.inputrc");
        if (path == null) {
            File f = new File(Configuration.getUserHome(), ".inputrc");
            if (!f.exists()) {
                f = new File("/etc/inputrc");
            }
            return f.toURI().toURL();
        }
        return Urls.create(path);
    }
    
    public KeyMap getKeys() {
        return this.consoleKeys.getKeys();
    }
    
    void setInput(final InputStream in) throws IOException {
        this.escapeTimeout = Configuration.getLong("jline.esc.timeout", 100L);
        this.isUnitTestInput = (in instanceof ByteArrayInputStream);
        final boolean nonBlockingEnabled = this.escapeTimeout > 0L && this.terminal.isSupported() && in != null;
        if (this.in != null) {
            this.in.shutdown();
        }
        final InputStream wrapped = this.terminal.wrapInIfNeeded(in);
        this.in = new NonBlockingInputStream(wrapped, nonBlockingEnabled);
        this.reader = new InputStreamReader(this.in, this.encoding);
    }
    
    public void shutdown() {
        if (this.in != null) {
            this.in.shutdown();
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            this.shutdown();
        }
        finally {
            super.finalize();
        }
    }
    
    public InputStream getInput() {
        return this.in;
    }
    
    public Writer getOutput() {
        return this.out;
    }
    
    public Terminal getTerminal() {
        return this.terminal;
    }
    
    public CursorBuffer getCursorBuffer() {
        return this.buf;
    }
    
    public void setExpandEvents(final boolean expand) {
        this.expandEvents = expand;
    }
    
    public boolean getExpandEvents() {
        return this.expandEvents;
    }
    
    public void setCopyPasteDetection(final boolean onoff) {
        this.copyPasteDetection = onoff;
    }
    
    public boolean isCopyPasteDetectionEnabled() {
        return this.copyPasteDetection;
    }
    
    public void setBellEnabled(final boolean enabled) {
        this.bellEnabled = enabled;
    }
    
    public boolean getBellEnabled() {
        return this.bellEnabled;
    }
    
    public void setHandleUserInterrupt(final boolean enabled) {
        this.handleUserInterrupt = enabled;
    }
    
    public boolean getHandleUserInterrupt() {
        return this.handleUserInterrupt;
    }
    
    public void setCommentBegin(final String commentBegin) {
        this.commentBegin = commentBegin;
    }
    
    public String getCommentBegin() {
        String str = this.commentBegin;
        if (str == null) {
            str = this.consoleKeys.getVariable("comment-begin");
            if (str == null) {
                str = "#";
            }
        }
        return str;
    }
    
    public void setPrompt(final String prompt) {
        this.prompt = prompt;
        this.promptLen = ((prompt == null) ? 0 : this.stripAnsi(this.lastLine(prompt)).length());
    }
    
    public String getPrompt() {
        return this.prompt;
    }
    
    public void setEchoCharacter(final Character c) {
        this.echoCharacter = c;
    }
    
    public Character getEchoCharacter() {
        return this.echoCharacter;
    }
    
    protected final boolean resetLine() throws IOException {
        if (this.buf.cursor == 0) {
            return false;
        }
        final StringBuilder killed = new StringBuilder();
        while (this.buf.cursor > 0) {
            final char c = this.buf.current();
            if (c == '\0') {
                break;
            }
            killed.append(c);
            this.backspace();
        }
        final String copy = killed.reverse().toString();
        this.killRing.addBackwards(copy);
        return true;
    }
    
    int getCursorPosition() {
        return this.promptLen + this.buf.cursor;
    }
    
    private String lastLine(final String str) {
        if (str == null) {
            return "";
        }
        final int last = str.lastIndexOf("\n");
        if (last >= 0) {
            return str.substring(last + 1, str.length());
        }
        return str;
    }
    
    private String stripAnsi(final String str) {
        if (str == null) {
            return "";
        }
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final AnsiOutputStream aos = new AnsiOutputStream(baos);
            aos.write(str.getBytes());
            aos.flush();
            return baos.toString();
        }
        catch (IOException e) {
            return str;
        }
    }
    
    public final boolean setCursorPosition(final int position) throws IOException {
        return position == this.buf.cursor || this.moveCursor(position - this.buf.cursor) != 0;
    }
    
    private void setBuffer(final String buffer) throws IOException {
        if (buffer.equals(this.buf.buffer.toString())) {
            return;
        }
        int sameIndex = 0;
        for (int i = 0, l1 = buffer.length(), l2 = this.buf.buffer.length(); i < l1 && i < l2 && buffer.charAt(i) == this.buf.buffer.charAt(i); ++i) {
            ++sameIndex;
        }
        int diff = this.buf.cursor - sameIndex;
        if (diff < 0) {
            this.moveToEnd();
            diff = this.buf.buffer.length() - sameIndex;
        }
        this.backspace(diff);
        this.killLine();
        this.buf.buffer.setLength(sameIndex);
        this.putString(buffer.substring(sameIndex));
    }
    
    private void setBuffer(final CharSequence buffer) throws IOException {
        this.setBuffer(String.valueOf(buffer));
    }
    
    private void setBufferKeepPos(final String buffer) throws IOException {
        final int pos = this.buf.cursor;
        this.setBuffer(buffer);
        this.setCursorPosition(pos);
    }
    
    private void setBufferKeepPos(final CharSequence buffer) throws IOException {
        this.setBufferKeepPos(String.valueOf(buffer));
    }
    
    public final void drawLine() throws IOException {
        final String prompt = this.getPrompt();
        if (prompt != null) {
            this.print(prompt);
        }
        this.print(this.buf.buffer.toString());
        if (this.buf.length() != this.buf.cursor) {
            this.back(this.buf.length() - this.buf.cursor - 1);
        }
        this.drawBuffer();
    }
    
    public final void redrawLine() throws IOException {
        this.print(13);
        this.drawLine();
    }
    
    final String finishBuffer() throws IOException {
        String historyLine;
        String str = historyLine = this.buf.buffer.toString();
        if (this.expandEvents) {
            try {
                str = this.expandEvents(str);
                historyLine = str.replace("!", "\\!");
                historyLine = historyLine.replaceAll("^\\^", "\\\\^");
            }
            catch (IllegalArgumentException e) {
                Log.error("Could not expand event", e);
                this.beep();
                this.buf.clear();
                str = "";
            }
        }
        if (str.length() > 0) {
            if (this.mask == null && this.isHistoryEnabled()) {
                this.history.add(historyLine);
            }
            else {
                this.mask = null;
            }
        }
        this.history.moveToEnd();
        this.buf.buffer.setLength(0);
        this.buf.cursor = 0;
        return str;
    }
    
    protected String expandEvents(final String str) throws IOException {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            switch (c) {
                case '\\': {
                    if (i + 1 < str.length()) {
                        final char nextChar = str.charAt(i + 1);
                        if (nextChar == '!' || (nextChar == '^' && i == 0)) {
                            c = nextChar;
                            ++i;
                        }
                    }
                    sb.append(c);
                    break;
                }
                case '!': {
                    if (i + 1 < str.length()) {
                        c = str.charAt(++i);
                        boolean neg = false;
                        String rep = null;
                        switch (c) {
                            case '!': {
                                if (this.history.size() == 0) {
                                    throw new IllegalArgumentException("!!: event not found");
                                }
                                rep = this.history.get(this.history.index() - 1).toString();
                                break;
                            }
                            case '#': {
                                sb.append(sb.toString());
                                break;
                            }
                            case '?': {
                                int i2 = str.indexOf(63, i + 1);
                                if (i2 < 0) {
                                    i2 = str.length();
                                }
                                final String sc = str.substring(i + 1, i2);
                                i = i2;
                                final int idx = this.searchBackwards(sc);
                                if (idx < 0) {
                                    throw new IllegalArgumentException("!?" + sc + ": event not found");
                                }
                                rep = this.history.get(idx).toString();
                                break;
                            }
                            case '$': {
                                if (this.history.size() == 0) {
                                    throw new IllegalArgumentException("!$: event not found");
                                }
                                final String previous = this.history.get(this.history.index() - 1).toString().trim();
                                final int lastSpace = previous.lastIndexOf(32);
                                if (lastSpace != -1) {
                                    rep = previous.substring(lastSpace + 1);
                                    break;
                                }
                                rep = previous;
                                break;
                            }
                            case '\t':
                            case ' ': {
                                sb.append('!');
                                sb.append(c);
                                break;
                            }
                            case '-': {
                                neg = true;
                                ++i;
                            }
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9': {
                                final int i2 = i;
                                while (i < str.length()) {
                                    c = str.charAt(i);
                                    if (c < '0') {
                                        break;
                                    }
                                    if (c > '9') {
                                        break;
                                    }
                                    ++i;
                                }
                                int idx = 0;
                                try {
                                    idx = Integer.parseInt(str.substring(i2, i));
                                }
                                catch (NumberFormatException e) {
                                    throw new IllegalArgumentException((neg ? "!-" : "!") + str.substring(i2, i) + ": event not found");
                                }
                                if (neg) {
                                    if (idx > 0 && idx <= this.history.size()) {
                                        rep = this.history.get(this.history.index() - idx).toString();
                                        break;
                                    }
                                    throw new IllegalArgumentException((neg ? "!-" : "!") + str.substring(i2, i) + ": event not found");
                                }
                                else {
                                    if (idx > this.history.index() - this.history.size() && idx <= this.history.index()) {
                                        rep = this.history.get(idx - 1).toString();
                                        break;
                                    }
                                    throw new IllegalArgumentException((neg ? "!-" : "!") + str.substring(i2, i) + ": event not found");
                                }
                                break;
                            }
                            default: {
                                final String ss = str.substring(i);
                                i = str.length();
                                final int idx = this.searchBackwards(ss, this.history.index(), true);
                                if (idx < 0) {
                                    throw new IllegalArgumentException("!" + ss + ": event not found");
                                }
                                rep = this.history.get(idx).toString();
                                break;
                            }
                        }
                        if (rep != null) {
                            sb.append(rep);
                        }
                        break;
                    }
                    sb.append(c);
                    break;
                }
                case '^': {
                    if (i == 0) {
                        final int i3 = str.indexOf(94, i + 1);
                        int i4 = str.indexOf(94, i3 + 1);
                        if (i4 < 0) {
                            i4 = str.length();
                        }
                        if (i3 > 0 && i4 > 0) {
                            final String s1 = str.substring(i + 1, i3);
                            final String s2 = str.substring(i3 + 1, i4);
                            final String s3 = this.history.get(this.history.index() - 1).toString().replace(s1, s2);
                            sb.append(s3);
                            i = i4 + 1;
                            break;
                        }
                    }
                    sb.append(c);
                    break;
                }
                default: {
                    sb.append(c);
                    break;
                }
            }
        }
        final String result = sb.toString();
        if (!str.equals(result)) {
            this.print(result);
            this.println();
            this.flush();
        }
        return result;
    }
    
    public final void putString(final CharSequence str) throws IOException {
        this.buf.write(str);
        if (this.mask == null) {
            this.print(str);
        }
        else if (this.mask != '\0') {
            this.print((char)this.mask, str.length());
        }
        this.drawBuffer();
    }
    
    private void drawBuffer(final int clear) throws IOException {
        if (this.buf.cursor != this.buf.length() || clear != 0) {
            final char[] chars = this.buf.buffer.substring(this.buf.cursor).toCharArray();
            if (this.mask != null) {
                Arrays.fill(chars, this.mask);
            }
            if (this.terminal.hasWeirdWrap()) {
                final int width = this.terminal.getWidth();
                final int pos = this.getCursorPosition();
                for (int i = 0; i < chars.length; ++i) {
                    this.print(chars[i]);
                    if ((pos + i + 1) % width == 0) {
                        this.print(32);
                        this.print(13);
                    }
                }
            }
            else {
                this.print(chars);
            }
            this.clearAhead(clear, chars.length);
            if (this.terminal.isAnsiSupported()) {
                if (chars.length > 0) {
                    this.back(chars.length);
                }
            }
            else {
                this.back(chars.length);
            }
        }
        if (this.terminal.hasWeirdWrap()) {
            final int width2 = this.terminal.getWidth();
            if (this.getCursorPosition() > 0 && this.getCursorPosition() % width2 == 0 && this.buf.cursor == this.buf.length() && clear == 0) {
                this.print(32);
                this.print(13);
            }
        }
    }
    
    private void drawBuffer() throws IOException {
        this.drawBuffer(0);
    }
    
    private void clearAhead(final int num, final int delta) throws IOException {
        if (num == 0) {
            return;
        }
        if (this.terminal.isAnsiSupported()) {
            final int width = this.terminal.getWidth();
            final int screenCursorCol = this.getCursorPosition() + delta;
            this.printAnsiSequence("K");
            final int curCol = screenCursorCol % width;
            final int endCol = (screenCursorCol + num - 1) % width;
            int lines = num / width;
            if (endCol < curCol) {
                ++lines;
            }
            for (int i = 0; i < lines; ++i) {
                this.printAnsiSequence("B");
                this.printAnsiSequence("2K");
            }
            for (int i = 0; i < lines; ++i) {
                this.printAnsiSequence("A");
            }
            return;
        }
        this.print(' ', num);
        this.back(num);
    }
    
    protected void back(final int num) throws IOException {
        if (num == 0) {
            return;
        }
        if (this.terminal.isAnsiSupported()) {
            final int width = this.getTerminal().getWidth();
            final int cursor = this.getCursorPosition();
            final int realCursor = cursor + num;
            final int realCol = realCursor % width;
            final int newCol = cursor % width;
            int moveup = num / width;
            final int delta = realCol - newCol;
            if (delta < 0) {
                ++moveup;
            }
            if (moveup > 0) {
                this.printAnsiSequence(moveup + "A");
            }
            this.printAnsiSequence(1 + newCol + "G");
            return;
        }
        this.print('\b', num);
    }
    
    public void flush() throws IOException {
        this.out.flush();
    }
    
    private int backspaceAll() throws IOException {
        return this.backspace(Integer.MAX_VALUE);
    }
    
    private int backspace(final int num) throws IOException {
        if (this.buf.cursor == 0) {
            return 0;
        }
        int count = 0;
        final int termwidth = this.getTerminal().getWidth();
        final int lines = this.getCursorPosition() / termwidth;
        count = this.moveCursor(-1 * num) * -1;
        this.buf.buffer.delete(this.buf.cursor, this.buf.cursor + count);
        if (this.getCursorPosition() / termwidth != lines && this.terminal.isAnsiSupported()) {
            this.printAnsiSequence("K");
        }
        this.drawBuffer(count);
        return count;
    }
    
    public boolean backspace() throws IOException {
        return this.backspace(1) == 1;
    }
    
    protected boolean moveToEnd() throws IOException {
        return this.buf.cursor == this.buf.length() || this.moveCursor(this.buf.length() - this.buf.cursor) > 0;
    }
    
    private boolean deleteCurrentCharacter() throws IOException {
        if (this.buf.length() == 0 || this.buf.cursor == this.buf.length()) {
            return false;
        }
        this.buf.buffer.deleteCharAt(this.buf.cursor);
        this.drawBuffer(1);
        return true;
    }
    
    private Operation viDeleteChangeYankToRemap(final Operation op) {
        switch (op) {
            case VI_EOF_MAYBE:
            case ABORT:
            case BACKWARD_CHAR:
            case FORWARD_CHAR:
            case END_OF_LINE:
            case VI_MATCH:
            case VI_BEGNNING_OF_LINE_OR_ARG_DIGIT:
            case VI_ARG_DIGIT:
            case VI_PREV_WORD:
            case VI_END_WORD:
            case VI_CHAR_SEARCH:
            case VI_NEXT_WORD:
            case VI_FIRST_PRINT:
            case VI_GOTO_MARK:
            case VI_COLUMN:
            case VI_DELETE_TO:
            case VI_YANK_TO:
            case VI_CHANGE_TO: {
                return op;
            }
            default: {
                return Operation.VI_MOVEMENT_MODE;
            }
        }
    }
    
    private boolean viRubout(final int count) throws IOException {
        boolean ok = true;
        for (int i = 0; ok && i < count; ok = this.backspace(), ++i) {}
        return ok;
    }
    
    private boolean viDelete(final int count) throws IOException {
        boolean ok = true;
        for (int i = 0; ok && i < count; ok = this.deleteCurrentCharacter(), ++i) {}
        return ok;
    }
    
    private boolean viChangeCase(final int count) throws IOException {
        boolean ok = true;
        for (int i = 0; ok && i < count; ++i) {
            ok = (this.buf.cursor < this.buf.buffer.length());
            if (ok) {
                char ch = this.buf.buffer.charAt(this.buf.cursor);
                if (Character.isUpperCase(ch)) {
                    ch = Character.toLowerCase(ch);
                }
                else if (Character.isLowerCase(ch)) {
                    ch = Character.toUpperCase(ch);
                }
                this.buf.buffer.setCharAt(this.buf.cursor, ch);
                this.drawBuffer(1);
                this.moveCursor(1);
            }
        }
        return ok;
    }
    
    private boolean viChangeChar(final int count, final int c) throws IOException {
        if (c < 0 || c == 27 || c == 3) {
            return true;
        }
        boolean ok = true;
        for (int i = 0; ok && i < count; ++i) {
            ok = (this.buf.cursor < this.buf.buffer.length());
            if (ok) {
                this.buf.buffer.setCharAt(this.buf.cursor, (char)c);
                this.drawBuffer(1);
                if (i < count - 1) {
                    this.moveCursor(1);
                }
            }
        }
        return ok;
    }
    
    private boolean viPreviousWord(final int count) throws IOException {
        final boolean ok = true;
        if (this.buf.cursor == 0) {
            return false;
        }
        int pos = this.buf.cursor - 1;
        for (int i = 0; pos > 0 && i < count; ++i) {
            while (pos > 0 && this.isWhitespace(this.buf.buffer.charAt(pos))) {
                --pos;
            }
            while (pos > 0 && !this.isDelimiter(this.buf.buffer.charAt(pos - 1))) {
                --pos;
            }
            if (pos > 0 && i < count - 1) {
                --pos;
            }
        }
        this.setCursorPosition(pos);
        return ok;
    }
    
    private boolean viDeleteTo(int startPos, int endPos, final boolean isChange) throws IOException {
        if (startPos == endPos) {
            return true;
        }
        if (endPos < startPos) {
            final int tmp = endPos;
            endPos = startPos;
            startPos = tmp;
        }
        this.setCursorPosition(startPos);
        this.buf.cursor = startPos;
        this.buf.buffer.delete(startPos, endPos);
        this.drawBuffer(endPos - startPos);
        if (!isChange && startPos > 0 && startPos == this.buf.length()) {
            this.moveCursor(-1);
        }
        return true;
    }
    
    private boolean viYankTo(int startPos, int endPos) throws IOException {
        final int cursorPos = startPos;
        if (endPos < startPos) {
            final int tmp = endPos;
            endPos = startPos;
            startPos = tmp;
        }
        if (startPos == endPos) {
            this.yankBuffer = "";
            return true;
        }
        this.yankBuffer = this.buf.buffer.substring(startPos, endPos);
        this.setCursorPosition(cursorPos);
        return true;
    }
    
    private boolean viPut(final int count) throws IOException {
        if (this.yankBuffer.length() == 0) {
            return true;
        }
        if (this.buf.cursor < this.buf.buffer.length()) {
            this.moveCursor(1);
        }
        for (int i = 0; i < count; ++i) {
            this.putString(this.yankBuffer);
        }
        this.moveCursor(-1);
        return true;
    }
    
    private boolean viCharSearch(int count, final int invokeChar, final int ch) throws IOException {
        if (ch < 0 || invokeChar < 0) {
            return false;
        }
        char searchChar = (char)ch;
        if (invokeChar == 59 || invokeChar == 44) {
            if (this.charSearchChar == '\0') {
                return false;
            }
            if (this.charSearchLastInvokeChar == ';' || this.charSearchLastInvokeChar == ',') {
                if (this.charSearchLastInvokeChar != invokeChar) {
                    this.charSearchFirstInvokeChar = this.switchCase(this.charSearchFirstInvokeChar);
                }
            }
            else if (invokeChar == 44) {
                this.charSearchFirstInvokeChar = this.switchCase(this.charSearchFirstInvokeChar);
            }
            searchChar = this.charSearchChar;
        }
        else {
            this.charSearchChar = searchChar;
            this.charSearchFirstInvokeChar = (char)invokeChar;
        }
        this.charSearchLastInvokeChar = (char)invokeChar;
        final boolean isForward = Character.isLowerCase(this.charSearchFirstInvokeChar);
        final boolean stopBefore = Character.toLowerCase(this.charSearchFirstInvokeChar) == 't';
        boolean ok = false;
        if (isForward) {
            while (count-- > 0) {
                for (int pos = this.buf.cursor + 1; pos < this.buf.buffer.length(); ++pos) {
                    if (this.buf.buffer.charAt(pos) == searchChar) {
                        this.setCursorPosition(pos);
                        ok = true;
                        break;
                    }
                }
            }
            if (ok) {
                if (stopBefore) {
                    this.moveCursor(-1);
                }
                if (this.isInViMoveOperationState()) {
                    this.moveCursor(1);
                }
            }
        }
        else {
            while (count-- > 0) {
                for (int pos = this.buf.cursor - 1; pos >= 0; --pos) {
                    if (this.buf.buffer.charAt(pos) == searchChar) {
                        this.setCursorPosition(pos);
                        ok = true;
                        break;
                    }
                }
            }
            if (ok && stopBefore) {
                this.moveCursor(1);
            }
        }
        return ok;
    }
    
    private char switchCase(final char ch) {
        if (Character.isUpperCase(ch)) {
            return Character.toLowerCase(ch);
        }
        return Character.toUpperCase(ch);
    }
    
    private final boolean isInViMoveOperationState() {
        return this.state == State.VI_CHANGE_TO || this.state == State.VI_DELETE_TO || this.state == State.VI_YANK_TO;
    }
    
    private boolean viNextWord(final int count) throws IOException {
        int pos = this.buf.cursor;
        for (int end = this.buf.buffer.length(), i = 0; pos < end && i < count; ++i) {
            while (pos < end && !this.isDelimiter(this.buf.buffer.charAt(pos))) {
                ++pos;
            }
            if (i < count - 1 || this.state != State.VI_CHANGE_TO) {
                while (pos < end && this.isDelimiter(this.buf.buffer.charAt(pos))) {
                    ++pos;
                }
            }
        }
        this.setCursorPosition(pos);
        return true;
    }
    
    private boolean viEndWord(final int count) throws IOException {
        int pos = this.buf.cursor;
        for (int end = this.buf.buffer.length(), i = 0; pos < end && i < count; ++i) {
            if (pos < end - 1 && !this.isDelimiter(this.buf.buffer.charAt(pos)) && this.isDelimiter(this.buf.buffer.charAt(pos + 1))) {
                ++pos;
            }
            while (pos < end && this.isDelimiter(this.buf.buffer.charAt(pos))) {
                ++pos;
            }
            while (pos < end - 1 && !this.isDelimiter(this.buf.buffer.charAt(pos + 1))) {
                ++pos;
            }
        }
        this.setCursorPosition(pos);
        return true;
    }
    
    private boolean previousWord() throws IOException {
        while (this.isDelimiter(this.buf.current()) && this.moveCursor(-1) != 0) {}
        while (!this.isDelimiter(this.buf.current()) && this.moveCursor(-1) != 0) {}
        return true;
    }
    
    private boolean nextWord() throws IOException {
        while (this.isDelimiter(this.buf.nextChar()) && this.moveCursor(1) != 0) {}
        while (!this.isDelimiter(this.buf.nextChar()) && this.moveCursor(1) != 0) {}
        return true;
    }
    
    private boolean unixWordRubout(int count) throws IOException {
        boolean success = true;
        final StringBuilder killed = new StringBuilder();
        while (count > 0) {
            if (this.buf.cursor == 0) {
                success = false;
                break;
            }
            while (this.isWhitespace(this.buf.current())) {
                final char c = this.buf.current();
                if (c == '\0') {
                    break;
                }
                killed.append(c);
                this.backspace();
            }
            while (!this.isWhitespace(this.buf.current())) {
                final char c = this.buf.current();
                if (c == '\0') {
                    break;
                }
                killed.append(c);
                this.backspace();
            }
            --count;
        }
        final String copy = killed.reverse().toString();
        this.killRing.addBackwards(copy);
        return success;
    }
    
    private String insertComment(final boolean isViMode) throws IOException {
        final String comment = this.getCommentBegin();
        this.setCursorPosition(0);
        this.putString(comment);
        if (isViMode) {
            this.consoleKeys.setKeyMap("vi-insert");
        }
        return this.accept();
    }
    
    private boolean insert(final int count, final CharSequence str) throws IOException {
        for (int i = 0; i < count; ++i) {
            this.buf.write(str);
            if (this.mask == null) {
                this.print(str);
            }
            else if (this.mask != '\0') {
                this.print((char)this.mask, str.length());
            }
        }
        this.drawBuffer();
        return true;
    }
    
    private int viSearch(final char searchChar) throws IOException {
        final boolean isForward = searchChar == '/';
        final CursorBuffer origBuffer = this.buf.copy();
        this.setCursorPosition(0);
        this.killLine();
        this.putString(Character.toString(searchChar));
        this.flush();
        boolean isAborted = false;
        boolean isComplete = false;
        int ch = -1;
        while (!isAborted && !isComplete && (ch = this.readCharacter()) != -1) {
            switch (ch) {
                case 27: {
                    isAborted = true;
                    break;
                }
                case 8:
                case 127: {
                    this.backspace();
                    if (this.buf.cursor == 0) {
                        isAborted = true;
                        break;
                    }
                    break;
                }
                case 10:
                case 13: {
                    isComplete = true;
                    break;
                }
                default: {
                    this.putString(Character.toString((char)ch));
                    break;
                }
            }
            this.flush();
        }
        if (ch == -1 || isAborted) {
            this.setCursorPosition(0);
            this.killLine();
            this.putString(origBuffer.buffer);
            this.setCursorPosition(origBuffer.cursor);
            return -1;
        }
        final String searchTerm = this.buf.buffer.substring(1);
        int idx = -1;
        final int end = this.history.index();
        final int start = (end <= this.history.size()) ? 0 : (end - this.history.size());
        if (isForward) {
            for (int i = start; i < end; ++i) {
                if (this.history.get(i).toString().contains(searchTerm)) {
                    idx = i;
                    break;
                }
            }
        }
        else {
            for (int i = end - 1; i >= start; --i) {
                if (this.history.get(i).toString().contains(searchTerm)) {
                    idx = i;
                    break;
                }
            }
        }
        if (idx == -1) {
            this.setCursorPosition(0);
            this.killLine();
            this.putString(origBuffer.buffer);
            this.setCursorPosition(0);
            return -1;
        }
        this.setCursorPosition(0);
        this.killLine();
        this.putString(this.history.get(idx));
        this.setCursorPosition(0);
        this.flush();
        isComplete = false;
        while (!isComplete && (ch = this.readCharacter()) != -1) {
            boolean forward = isForward;
            switch (ch) {
                case 80:
                case 112: {
                    forward = !isForward;
                }
                case 78:
                case 110: {
                    boolean isMatch = false;
                    if (forward) {
                        for (int j = idx + 1; !isMatch && j < end; ++j) {
                            if (this.history.get(j).toString().contains(searchTerm)) {
                                idx = j;
                                isMatch = true;
                            }
                        }
                    }
                    else {
                        for (int j = idx - 1; !isMatch && j >= start; --j) {
                            if (this.history.get(j).toString().contains(searchTerm)) {
                                idx = j;
                                isMatch = true;
                            }
                        }
                    }
                    if (isMatch) {
                        this.setCursorPosition(0);
                        this.killLine();
                        this.putString(this.history.get(idx));
                        this.setCursorPosition(0);
                        break;
                    }
                    break;
                }
                default: {
                    isComplete = true;
                    break;
                }
            }
            this.flush();
        }
        return ch;
    }
    
    public void setParenBlinkTimeout(final int timeout) {
        this.parenBlinkTimeout = timeout;
    }
    
    private void insertClose(final String s) throws IOException {
        this.putString(s);
        final int closePosition = this.buf.cursor;
        this.moveCursor(-1);
        this.viMatch();
        if (this.in.isNonBlockingEnabled()) {
            this.in.peek(this.parenBlinkTimeout);
        }
        this.setCursorPosition(closePosition);
    }
    
    private boolean viMatch() throws IOException {
        int pos = this.buf.cursor;
        if (pos == this.buf.length()) {
            return false;
        }
        final int type = this.getBracketType(this.buf.buffer.charAt(pos));
        final int move = (type < 0) ? -1 : 1;
        int count = 1;
        if (type == 0) {
            return false;
        }
        while (count > 0) {
            pos += move;
            if (pos < 0 || pos >= this.buf.buffer.length()) {
                return false;
            }
            final int curType = this.getBracketType(this.buf.buffer.charAt(pos));
            if (curType == type) {
                ++count;
            }
            else {
                if (curType != -type) {
                    continue;
                }
                --count;
            }
        }
        if (move > 0 && this.isInViMoveOperationState()) {
            ++pos;
        }
        this.setCursorPosition(pos);
        return true;
    }
    
    private int getBracketType(final char ch) {
        switch (ch) {
            case '[': {
                return 1;
            }
            case ']': {
                return -1;
            }
            case '{': {
                return 2;
            }
            case '}': {
                return -2;
            }
            case '(': {
                return 3;
            }
            case ')': {
                return -3;
            }
            default: {
                return 0;
            }
        }
    }
    
    private boolean deletePreviousWord() throws IOException {
        final StringBuilder killed = new StringBuilder();
        char c;
        while (this.isDelimiter(c = this.buf.current())) {
            if (c == '\0') {
                break;
            }
            killed.append(c);
            this.backspace();
        }
        while (!this.isDelimiter(c = this.buf.current()) && c != '\0') {
            killed.append(c);
            this.backspace();
        }
        final String copy = killed.reverse().toString();
        this.killRing.addBackwards(copy);
        return true;
    }
    
    private boolean deleteNextWord() throws IOException {
        final StringBuilder killed = new StringBuilder();
        char c;
        while (this.isDelimiter(c = this.buf.nextChar())) {
            if (c == '\0') {
                break;
            }
            killed.append(c);
            this.delete();
        }
        while (!this.isDelimiter(c = this.buf.nextChar()) && c != '\0') {
            killed.append(c);
            this.delete();
        }
        final String copy = killed.toString();
        this.killRing.add(copy);
        return true;
    }
    
    private boolean capitalizeWord() throws IOException {
        boolean first = true;
        int i;
        char c;
        for (i = 1; this.buf.cursor + i - 1 < this.buf.length() && !this.isDelimiter(c = this.buf.buffer.charAt(this.buf.cursor + i - 1)); ++i) {
            this.buf.buffer.setCharAt(this.buf.cursor + i - 1, first ? Character.toUpperCase(c) : Character.toLowerCase(c));
            first = false;
        }
        this.drawBuffer();
        this.moveCursor(i - 1);
        return true;
    }
    
    private boolean upCaseWord() throws IOException {
        int i;
        char c;
        for (i = 1; this.buf.cursor + i - 1 < this.buf.length() && !this.isDelimiter(c = this.buf.buffer.charAt(this.buf.cursor + i - 1)); ++i) {
            this.buf.buffer.setCharAt(this.buf.cursor + i - 1, Character.toUpperCase(c));
        }
        this.drawBuffer();
        this.moveCursor(i - 1);
        return true;
    }
    
    private boolean downCaseWord() throws IOException {
        int i;
        char c;
        for (i = 1; this.buf.cursor + i - 1 < this.buf.length() && !this.isDelimiter(c = this.buf.buffer.charAt(this.buf.cursor + i - 1)); ++i) {
            this.buf.buffer.setCharAt(this.buf.cursor + i - 1, Character.toLowerCase(c));
        }
        this.drawBuffer();
        this.moveCursor(i - 1);
        return true;
    }
    
    private boolean transposeChars(int count) throws IOException {
        while (count > 0) {
            if (this.buf.cursor == 0 || this.buf.cursor == this.buf.buffer.length()) {
                return false;
            }
            final int first = this.buf.cursor - 1;
            final int second = this.buf.cursor;
            final char tmp = this.buf.buffer.charAt(first);
            this.buf.buffer.setCharAt(first, this.buf.buffer.charAt(second));
            this.buf.buffer.setCharAt(second, tmp);
            this.moveInternal(-1);
            this.drawBuffer();
            this.moveInternal(2);
            --count;
        }
        return true;
    }
    
    public boolean isKeyMap(final String name) {
        final KeyMap map = this.consoleKeys.getKeys();
        final KeyMap mapByName = this.consoleKeys.getKeyMaps().get(name);
        return mapByName != null && map == mapByName;
    }
    
    public String accept() throws IOException {
        this.moveToEnd();
        this.println();
        this.flush();
        return this.finishBuffer();
    }
    
    private void abort() throws IOException {
        this.beep();
        this.buf.clear();
        this.println();
        this.redrawLine();
    }
    
    public int moveCursor(final int num) throws IOException {
        int where = num;
        if (this.buf.cursor == 0 && where <= 0) {
            return 0;
        }
        if (this.buf.cursor == this.buf.buffer.length() && where >= 0) {
            return 0;
        }
        if (this.buf.cursor + where < 0) {
            where = -this.buf.cursor;
        }
        else if (this.buf.cursor + where > this.buf.buffer.length()) {
            where = this.buf.buffer.length() - this.buf.cursor;
        }
        this.moveInternal(where);
        return where;
    }
    
    private void moveInternal(final int where) throws IOException {
        final CursorBuffer buf = this.buf;
        buf.cursor += where;
        if (this.terminal.isAnsiSupported()) {
            if (where < 0) {
                this.back(Math.abs(where));
            }
            else {
                final int width = this.getTerminal().getWidth();
                final int cursor = this.getCursorPosition();
                final int oldLine = (cursor - where) / width;
                final int newLine = cursor / width;
                if (newLine > oldLine) {
                    this.printAnsiSequence(newLine - oldLine + "B");
                }
                this.printAnsiSequence(1 + cursor % width + "G");
            }
            return;
        }
        if (where < 0) {
            int len = 0;
            for (int i = this.buf.cursor; i < this.buf.cursor - where; ++i) {
                if (this.buf.buffer.charAt(i) == '\t') {
                    len += 4;
                }
                else {
                    ++len;
                }
            }
            final char[] chars = new char[len];
            Arrays.fill(chars, '\b');
            this.out.write(chars);
            return;
        }
        if (this.buf.cursor == 0) {
            return;
        }
        if (this.mask == null) {
            this.print(this.buf.buffer.substring(this.buf.cursor - where, this.buf.cursor).toCharArray());
            return;
        }
        final char c = this.mask;
        if (this.mask == '\0') {
            return;
        }
        this.print(c, Math.abs(where));
    }
    
    public final boolean replace(final int num, final String replacement) {
        this.buf.buffer.replace(this.buf.cursor - num, this.buf.cursor, replacement);
        try {
            this.moveCursor(-num);
            this.drawBuffer(Math.max(0, num - replacement.length()));
            this.moveCursor(replacement.length());
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public final int readCharacter() throws IOException {
        final int c = this.reader.read();
        if (c >= 0) {
            Log.trace("Keystroke: ", c);
            if (this.terminal.isSupported()) {
                this.clearEcho(c);
            }
        }
        return c;
    }
    
    private int clearEcho(final int c) throws IOException {
        if (!this.terminal.isEchoEnabled()) {
            return 0;
        }
        final int num = this.countEchoCharacters(c);
        this.back(num);
        this.drawBuffer(num);
        return num;
    }
    
    private int countEchoCharacters(final int c) {
        if (c == 9) {
            final int tabStop = 8;
            final int position = this.getCursorPosition();
            return tabStop - position % tabStop;
        }
        return this.getPrintableCharacters(c).length();
    }
    
    private StringBuilder getPrintableCharacters(final int ch) {
        final StringBuilder sbuff = new StringBuilder();
        if (ch >= 32) {
            if (ch < 127) {
                sbuff.append(ch);
            }
            else if (ch == 127) {
                sbuff.append('^');
                sbuff.append('?');
            }
            else {
                sbuff.append('M');
                sbuff.append('-');
                if (ch >= 160) {
                    if (ch < 255) {
                        sbuff.append((char)(ch - 128));
                    }
                    else {
                        sbuff.append('^');
                        sbuff.append('?');
                    }
                }
                else {
                    sbuff.append('^');
                    sbuff.append((char)(ch - 128 + 64));
                }
            }
        }
        else {
            sbuff.append('^');
            sbuff.append((char)(ch + 64));
        }
        return sbuff;
    }
    
    public final int readCharacter(final char... allowed) throws IOException {
        Arrays.sort(allowed);
        char c;
        while (Arrays.binarySearch(allowed, c = (char)this.readCharacter()) < 0) {}
        return c;
    }
    
    public String readLine() throws IOException {
        return this.readLine((String)null);
    }
    
    public String readLine(final Character mask) throws IOException {
        return this.readLine(null, mask);
    }
    
    public String readLine(final String prompt) throws IOException {
        return this.readLine(prompt, null);
    }
    
    public boolean setKeyMap(final String name) {
        return this.consoleKeys.setKeyMap(name);
    }
    
    public String getKeyMap() {
        return this.consoleKeys.getKeys().getName();
    }
    
    public String readLine(String prompt, final Character mask) throws IOException {
        int repeatCount = 0;
        this.mask = mask;
        if (prompt != null) {
            this.setPrompt(prompt);
        }
        else {
            prompt = this.getPrompt();
        }
        try {
            if (!this.terminal.isSupported()) {
                this.beforeReadLine(prompt, mask);
            }
            if (prompt != null && prompt.length() > 0) {
                this.out.write(prompt);
                this.out.flush();
            }
            if (!this.terminal.isSupported()) {
                return this.readLineSimple();
            }
            if (this.handleUserInterrupt && this.terminal instanceof UnixTerminal) {
                ((UnixTerminal)this.terminal).disableInterruptCharacter();
            }
            final String originalPrompt = this.prompt;
            this.state = State.NORMAL;
            boolean success = true;
            final StringBuilder sb = new StringBuilder();
            final Stack<Character> pushBackChar = new Stack<Character>();
            while (true) {
                int c = pushBackChar.isEmpty() ? this.readCharacter() : ((char)pushBackChar.pop());
                if (c == -1) {
                    return null;
                }
                sb.appendCodePoint(c);
                if (this.recording) {
                    this.macro += new String(new int[] { c }, 0, 1);
                }
                Object o = this.getKeys().getBound(sb);
                if (!this.recording && !(o instanceof KeyMap)) {
                    if (o != Operation.YANK_POP && o != Operation.YANK) {
                        this.killRing.resetLastYank();
                    }
                    if (o != Operation.KILL_LINE && o != Operation.KILL_WHOLE_LINE && o != Operation.BACKWARD_KILL_WORD && o != Operation.KILL_WORD && o != Operation.UNIX_LINE_DISCARD && o != Operation.UNIX_WORD_RUBOUT) {
                        this.killRing.resetLastKill();
                    }
                }
                if (o == Operation.DO_LOWERCASE_VERSION) {
                    sb.setLength(sb.length() - 1);
                    sb.append(Character.toLowerCase((char)c));
                    o = this.getKeys().getBound(sb);
                }
                if (o instanceof KeyMap) {
                    if (c != 27 || !pushBackChar.isEmpty() || !this.in.isNonBlockingEnabled() || this.in.peek(this.escapeTimeout) != -2) {
                        continue;
                    }
                    o = ((KeyMap)o).getAnotherKey();
                    if (o == null) {
                        continue;
                    }
                    if (o instanceof KeyMap) {
                        continue;
                    }
                    sb.setLength(0);
                }
                while (o == null && sb.length() > 0) {
                    c = sb.charAt(sb.length() - 1);
                    sb.setLength(sb.length() - 1);
                    final Object o2 = this.getKeys().getBound(sb);
                    if (o2 instanceof KeyMap) {
                        o = ((KeyMap)o2).getAnotherKey();
                        if (o == null) {
                            continue;
                        }
                        pushBackChar.push((char)c);
                    }
                }
                if (o == null) {
                    continue;
                }
                Log.trace("Binding: ", o);
                if (o instanceof String) {
                    final String macro = (String)o;
                    for (int i = 0; i < macro.length(); ++i) {
                        pushBackChar.push(macro.charAt(macro.length() - 1 - i));
                    }
                    sb.setLength(0);
                }
                else if (o instanceof ActionListener) {
                    ((ActionListener)o).actionPerformed(null);
                    sb.setLength(0);
                }
                else {
                    if (this.state == State.SEARCH || this.state == State.FORWARD_SEARCH) {
                        int cursorDest = -1;
                        switch ((Operation)o) {
                            case ABORT: {
                                this.state = State.NORMAL;
                                this.buf.clear();
                                this.buf.buffer.append(this.searchTerm);
                                break;
                            }
                            case REVERSE_SEARCH_HISTORY: {
                                this.state = State.SEARCH;
                                if (this.searchTerm.length() == 0) {
                                    this.searchTerm.append(this.previousSearchTerm);
                                }
                                if (this.searchIndex > 0) {
                                    this.searchIndex = this.searchBackwards(this.searchTerm.toString(), this.searchIndex);
                                    break;
                                }
                                break;
                            }
                            case FORWARD_SEARCH_HISTORY: {
                                this.state = State.FORWARD_SEARCH;
                                if (this.searchTerm.length() == 0) {
                                    this.searchTerm.append(this.previousSearchTerm);
                                }
                                if (this.searchIndex > -1 && this.searchIndex < this.history.size() - 1) {
                                    this.searchIndex = this.searchForwards(this.searchTerm.toString(), this.searchIndex);
                                    break;
                                }
                                break;
                            }
                            case BACKWARD_DELETE_CHAR: {
                                if (this.searchTerm.length() <= 0) {
                                    break;
                                }
                                this.searchTerm.deleteCharAt(this.searchTerm.length() - 1);
                                if (this.state == State.SEARCH) {
                                    this.searchIndex = this.searchBackwards(this.searchTerm.toString());
                                    break;
                                }
                                this.searchIndex = this.searchForwards(this.searchTerm.toString());
                                break;
                            }
                            case SELF_INSERT: {
                                this.searchTerm.appendCodePoint(c);
                                if (this.state == State.SEARCH) {
                                    this.searchIndex = this.searchBackwards(this.searchTerm.toString());
                                    break;
                                }
                                this.searchIndex = this.searchForwards(this.searchTerm.toString());
                                break;
                            }
                            default: {
                                if (this.searchIndex != -1) {
                                    this.history.moveTo(this.searchIndex);
                                    cursorDest = this.history.current().toString().indexOf(this.searchTerm.toString());
                                }
                                this.state = State.NORMAL;
                                break;
                            }
                        }
                        if (this.state == State.SEARCH || this.state == State.FORWARD_SEARCH) {
                            if (this.searchTerm.length() == 0) {
                                if (this.state == State.SEARCH) {
                                    this.printSearchStatus("", "");
                                }
                                else {
                                    this.printForwardSearchStatus("", "");
                                }
                                this.searchIndex = -1;
                            }
                            else if (this.searchIndex == -1) {
                                this.beep();
                                this.printSearchStatus(this.searchTerm.toString(), "");
                            }
                            else if (this.state == State.SEARCH) {
                                this.printSearchStatus(this.searchTerm.toString(), this.history.get(this.searchIndex).toString());
                            }
                            else {
                                this.printForwardSearchStatus(this.searchTerm.toString(), this.history.get(this.searchIndex).toString());
                            }
                        }
                        else {
                            this.restoreLine(originalPrompt, cursorDest);
                        }
                    }
                    if (this.state != State.SEARCH && this.state != State.FORWARD_SEARCH) {
                        boolean isArgDigit = false;
                        final int count = (repeatCount == 0) ? 1 : repeatCount;
                        success = true;
                        if (o instanceof Operation) {
                            Operation op = (Operation)o;
                            final int cursorStart = this.buf.cursor;
                            State origState = this.state;
                            if (this.state == State.VI_CHANGE_TO || this.state == State.VI_YANK_TO || this.state == State.VI_DELETE_TO) {
                                op = this.viDeleteChangeYankToRemap(op);
                            }
                            switch (op) {
                                case COMPLETE: {
                                    boolean isTabLiteral = false;
                                    if (this.copyPasteDetection && c == 9 && (!pushBackChar.isEmpty() || (this.in.isNonBlockingEnabled() && this.in.peek(this.escapeTimeout) != -2))) {
                                        isTabLiteral = true;
                                    }
                                    if (!isTabLiteral) {
                                        success = this.complete();
                                        break;
                                    }
                                    this.putString(sb);
                                    break;
                                }
                                case POSSIBLE_COMPLETIONS: {
                                    this.printCompletionCandidates();
                                    break;
                                }
                                case BEGINNING_OF_LINE: {
                                    success = this.setCursorPosition(0);
                                    break;
                                }
                                case YANK: {
                                    success = this.yank();
                                    break;
                                }
                                case YANK_POP: {
                                    success = this.yankPop();
                                    break;
                                }
                                case KILL_LINE: {
                                    success = this.killLine();
                                    break;
                                }
                                case KILL_WHOLE_LINE: {
                                    success = (this.setCursorPosition(0) && this.killLine());
                                    break;
                                }
                                case CLEAR_SCREEN: {
                                    success = this.clearScreen();
                                    this.redrawLine();
                                    break;
                                }
                                case OVERWRITE_MODE: {
                                    this.buf.setOverTyping(!this.buf.isOverTyping());
                                    break;
                                }
                                case SELF_INSERT: {
                                    this.putString(sb);
                                    break;
                                }
                                case ACCEPT_LINE: {
                                    return this.accept();
                                }
                                case ABORT: {
                                    if (this.searchTerm == null) {
                                        this.abort();
                                        break;
                                    }
                                    break;
                                }
                                case INTERRUPT: {
                                    if (this.handleUserInterrupt) {
                                        this.println();
                                        this.flush();
                                        final String partialLine = this.buf.buffer.toString();
                                        this.buf.clear();
                                        this.history.moveToEnd();
                                        throw new UserInterruptException(partialLine);
                                    }
                                    break;
                                }
                                case VI_MOVE_ACCEPT_LINE: {
                                    this.consoleKeys.setKeyMap("vi-insert");
                                    return this.accept();
                                }
                                case BACKWARD_WORD: {
                                    success = this.previousWord();
                                    break;
                                }
                                case FORWARD_WORD: {
                                    success = this.nextWord();
                                    break;
                                }
                                case PREVIOUS_HISTORY: {
                                    success = this.moveHistory(false);
                                    break;
                                }
                                case VI_PREVIOUS_HISTORY: {
                                    success = (this.moveHistory(false, count) && this.setCursorPosition(0));
                                    break;
                                }
                                case NEXT_HISTORY: {
                                    success = this.moveHistory(true);
                                    break;
                                }
                                case VI_NEXT_HISTORY: {
                                    success = (this.moveHistory(true, count) && this.setCursorPosition(0));
                                    break;
                                }
                                case BACKWARD_DELETE_CHAR: {
                                    success = this.backspace();
                                    break;
                                }
                                case EXIT_OR_DELETE_CHAR: {
                                    if (this.buf.buffer.length() == 0) {
                                        return null;
                                    }
                                    success = this.deleteCurrentCharacter();
                                    break;
                                }
                                case DELETE_CHAR: {
                                    success = this.deleteCurrentCharacter();
                                    break;
                                }
                                case BACKWARD_CHAR: {
                                    success = (this.moveCursor(-count) != 0);
                                    break;
                                }
                                case FORWARD_CHAR: {
                                    success = (this.moveCursor(count) != 0);
                                    break;
                                }
                                case UNIX_LINE_DISCARD: {
                                    success = this.resetLine();
                                    break;
                                }
                                case UNIX_WORD_RUBOUT: {
                                    success = this.unixWordRubout(count);
                                    break;
                                }
                                case BACKWARD_KILL_WORD: {
                                    success = this.deletePreviousWord();
                                    break;
                                }
                                case KILL_WORD: {
                                    success = this.deleteNextWord();
                                    break;
                                }
                                case BEGINNING_OF_HISTORY: {
                                    success = this.history.moveToFirst();
                                    if (success) {
                                        this.setBuffer(this.history.current());
                                        break;
                                    }
                                    break;
                                }
                                case END_OF_HISTORY: {
                                    success = this.history.moveToLast();
                                    if (success) {
                                        this.setBuffer(this.history.current());
                                        break;
                                    }
                                    break;
                                }
                                case HISTORY_SEARCH_BACKWARD: {
                                    this.searchTerm = new StringBuffer(this.buf.upToCursor());
                                    this.searchIndex = this.searchBackwards(this.searchTerm.toString(), this.history.index(), true);
                                    if (this.searchIndex == -1) {
                                        this.beep();
                                        break;
                                    }
                                    success = this.history.moveTo(this.searchIndex);
                                    if (success) {
                                        this.setBufferKeepPos(this.history.current());
                                        break;
                                    }
                                    break;
                                }
                                case HISTORY_SEARCH_FORWARD: {
                                    this.searchTerm = new StringBuffer(this.buf.upToCursor());
                                    final int index = this.history.index() + 1;
                                    if (index == this.history.size()) {
                                        this.history.moveToEnd();
                                        this.setBufferKeepPos(this.searchTerm.toString());
                                        break;
                                    }
                                    if (index >= this.history.size()) {
                                        break;
                                    }
                                    this.searchIndex = this.searchForwards(this.searchTerm.toString(), index, true);
                                    if (this.searchIndex == -1) {
                                        this.beep();
                                        break;
                                    }
                                    success = this.history.moveTo(this.searchIndex);
                                    if (success) {
                                        this.setBufferKeepPos(this.history.current());
                                        break;
                                    }
                                    break;
                                }
                                case REVERSE_SEARCH_HISTORY: {
                                    if (this.searchTerm != null) {
                                        this.previousSearchTerm = this.searchTerm.toString();
                                    }
                                    this.searchTerm = new StringBuffer(this.buf.buffer);
                                    this.state = State.SEARCH;
                                    if (this.searchTerm.length() > 0) {
                                        this.searchIndex = this.searchBackwards(this.searchTerm.toString());
                                        if (this.searchIndex == -1) {
                                            this.beep();
                                        }
                                        this.printSearchStatus(this.searchTerm.toString(), (this.searchIndex > -1) ? this.history.get(this.searchIndex).toString() : "");
                                        break;
                                    }
                                    this.searchIndex = -1;
                                    this.printSearchStatus("", "");
                                    break;
                                }
                                case FORWARD_SEARCH_HISTORY: {
                                    if (this.searchTerm != null) {
                                        this.previousSearchTerm = this.searchTerm.toString();
                                    }
                                    this.searchTerm = new StringBuffer(this.buf.buffer);
                                    this.state = State.FORWARD_SEARCH;
                                    if (this.searchTerm.length() > 0) {
                                        this.searchIndex = this.searchForwards(this.searchTerm.toString());
                                        if (this.searchIndex == -1) {
                                            this.beep();
                                        }
                                        this.printForwardSearchStatus(this.searchTerm.toString(), (this.searchIndex > -1) ? this.history.get(this.searchIndex).toString() : "");
                                        break;
                                    }
                                    this.searchIndex = -1;
                                    this.printForwardSearchStatus("", "");
                                    break;
                                }
                                case CAPITALIZE_WORD: {
                                    success = this.capitalizeWord();
                                    break;
                                }
                                case UPCASE_WORD: {
                                    success = this.upCaseWord();
                                    break;
                                }
                                case DOWNCASE_WORD: {
                                    success = this.downCaseWord();
                                    break;
                                }
                                case END_OF_LINE: {
                                    success = this.moveToEnd();
                                    break;
                                }
                                case TAB_INSERT: {
                                    this.putString("\t");
                                    break;
                                }
                                case RE_READ_INIT_FILE: {
                                    this.consoleKeys.loadKeys(this.appName, this.inputrcUrl);
                                    break;
                                }
                                case START_KBD_MACRO: {
                                    this.recording = true;
                                    break;
                                }
                                case END_KBD_MACRO: {
                                    this.recording = false;
                                    this.macro = this.macro.substring(0, this.macro.length() - sb.length());
                                    break;
                                }
                                case CALL_LAST_KBD_MACRO: {
                                    for (int j = 0; j < this.macro.length(); ++j) {
                                        pushBackChar.push(this.macro.charAt(this.macro.length() - 1 - j));
                                    }
                                    sb.setLength(0);
                                    break;
                                }
                                case VI_EDITING_MODE: {
                                    this.consoleKeys.setKeyMap("vi-insert");
                                    break;
                                }
                                case VI_MOVEMENT_MODE: {
                                    final State state = this.state;
                                    final State state2 = this.state;
                                    if (state == State.NORMAL) {
                                        this.moveCursor(-1);
                                    }
                                    this.consoleKeys.setKeyMap("vi-move");
                                    break;
                                }
                                case VI_INSERTION_MODE: {
                                    this.consoleKeys.setKeyMap("vi-insert");
                                    break;
                                }
                                case VI_APPEND_MODE: {
                                    this.moveCursor(1);
                                    this.consoleKeys.setKeyMap("vi-insert");
                                    break;
                                }
                                case VI_APPEND_EOL: {
                                    success = this.moveToEnd();
                                    this.consoleKeys.setKeyMap("vi-insert");
                                    break;
                                }
                                case VI_EOF_MAYBE: {
                                    if (this.buf.buffer.length() == 0) {
                                        return null;
                                    }
                                    return this.accept();
                                }
                                case TRANSPOSE_CHARS: {
                                    success = this.transposeChars(count);
                                    break;
                                }
                                case INSERT_COMMENT: {
                                    return this.insertComment(false);
                                }
                                case INSERT_CLOSE_CURLY: {
                                    this.insertClose("}");
                                    break;
                                }
                                case INSERT_CLOSE_PAREN: {
                                    this.insertClose(")");
                                    break;
                                }
                                case INSERT_CLOSE_SQUARE: {
                                    this.insertClose("]");
                                    break;
                                }
                                case VI_INSERT_COMMENT: {
                                    return this.insertComment(true);
                                }
                                case VI_MATCH: {
                                    success = this.viMatch();
                                    break;
                                }
                                case VI_SEARCH: {
                                    final int lastChar = this.viSearch(sb.charAt(0));
                                    if (lastChar != -1) {
                                        pushBackChar.push((char)lastChar);
                                        break;
                                    }
                                    break;
                                }
                                case VI_ARG_DIGIT: {
                                    repeatCount = repeatCount * 10 + sb.charAt(0) - 48;
                                    isArgDigit = true;
                                    break;
                                }
                                case VI_BEGNNING_OF_LINE_OR_ARG_DIGIT: {
                                    if (repeatCount > 0) {
                                        repeatCount = repeatCount * 10 + sb.charAt(0) - 48;
                                        isArgDigit = true;
                                        break;
                                    }
                                    success = this.setCursorPosition(0);
                                    break;
                                }
                                case VI_FIRST_PRINT: {
                                    success = (this.setCursorPosition(0) && this.viNextWord(1));
                                    break;
                                }
                                case VI_PREV_WORD: {
                                    success = this.viPreviousWord(count);
                                    break;
                                }
                                case VI_NEXT_WORD: {
                                    success = this.viNextWord(count);
                                    break;
                                }
                                case VI_END_WORD: {
                                    success = this.viEndWord(count);
                                    break;
                                }
                                case VI_INSERT_BEG: {
                                    success = this.setCursorPosition(0);
                                    this.consoleKeys.setKeyMap("vi-insert");
                                    break;
                                }
                                case VI_RUBOUT: {
                                    success = this.viRubout(count);
                                    break;
                                }
                                case VI_DELETE: {
                                    success = this.viDelete(count);
                                    break;
                                }
                                case VI_DELETE_TO: {
                                    if (this.state == State.VI_DELETE_TO) {
                                        success = (this.setCursorPosition(0) && this.killLine());
                                        origState = (this.state = State.NORMAL);
                                        break;
                                    }
                                    this.state = State.VI_DELETE_TO;
                                    break;
                                }
                                case VI_YANK_TO: {
                                    if (this.state == State.VI_YANK_TO) {
                                        this.yankBuffer = this.buf.buffer.toString();
                                        origState = (this.state = State.NORMAL);
                                        break;
                                    }
                                    this.state = State.VI_YANK_TO;
                                    break;
                                }
                                case VI_CHANGE_TO: {
                                    if (this.state == State.VI_CHANGE_TO) {
                                        success = (this.setCursorPosition(0) && this.killLine());
                                        origState = (this.state = State.NORMAL);
                                        this.consoleKeys.setKeyMap("vi-insert");
                                        break;
                                    }
                                    this.state = State.VI_CHANGE_TO;
                                    break;
                                }
                                case VI_KILL_WHOLE_LINE: {
                                    success = (this.setCursorPosition(0) && this.killLine());
                                    this.consoleKeys.setKeyMap("vi-insert");
                                    break;
                                }
                                case VI_PUT: {
                                    success = this.viPut(count);
                                    break;
                                }
                                case VI_CHAR_SEARCH: {
                                    final int searchChar = (c != 59 && c != 44) ? (pushBackChar.isEmpty() ? this.readCharacter() : ((char)pushBackChar.pop())) : 0;
                                    success = this.viCharSearch(count, c, searchChar);
                                    break;
                                }
                                case VI_CHANGE_CASE: {
                                    success = this.viChangeCase(count);
                                    break;
                                }
                                case VI_CHANGE_CHAR: {
                                    success = this.viChangeChar(count, pushBackChar.isEmpty() ? this.readCharacter() : ((char)pushBackChar.pop()));
                                    break;
                                }
                                case VI_DELETE_TO_EOL: {
                                    success = this.viDeleteTo(this.buf.cursor, this.buf.buffer.length(), false);
                                    break;
                                }
                                case VI_CHANGE_TO_EOL: {
                                    success = this.viDeleteTo(this.buf.cursor, this.buf.buffer.length(), true);
                                    this.consoleKeys.setKeyMap("vi-insert");
                                    break;
                                }
                                case EMACS_EDITING_MODE: {
                                    this.consoleKeys.setKeyMap("emacs");
                                    break;
                                }
                            }
                            if (origState != State.NORMAL) {
                                if (origState == State.VI_DELETE_TO) {
                                    success = this.viDeleteTo(cursorStart, this.buf.cursor, false);
                                }
                                else if (origState == State.VI_CHANGE_TO) {
                                    success = this.viDeleteTo(cursorStart, this.buf.cursor, true);
                                    this.consoleKeys.setKeyMap("vi-insert");
                                }
                                else if (origState == State.VI_YANK_TO) {
                                    success = this.viYankTo(cursorStart, this.buf.cursor);
                                }
                                this.state = State.NORMAL;
                            }
                            if (this.state == State.NORMAL && !isArgDigit) {
                                repeatCount = 0;
                            }
                            if (this.state != State.SEARCH && this.state != State.FORWARD_SEARCH) {
                                this.previousSearchTerm = "";
                                this.searchTerm = null;
                                this.searchIndex = -1;
                            }
                        }
                    }
                    if (!success) {
                        this.beep();
                    }
                    sb.setLength(0);
                    this.flush();
                }
            }
        }
        finally {
            if (!this.terminal.isSupported()) {
                this.afterReadLine();
            }
            if (this.handleUserInterrupt && this.terminal instanceof UnixTerminal) {
                ((UnixTerminal)this.terminal).enableInterruptCharacter();
            }
        }
    }
    
    private String readLineSimple() throws IOException {
        final StringBuilder buff = new StringBuilder();
        if (this.skipLF) {
            this.skipLF = false;
            final int i = this.readCharacter();
            if (i == -1 || i == 13) {
                return buff.toString();
            }
            if (i != 10) {
                buff.append((char)i);
            }
        }
        while (true) {
            final int i = this.readCharacter();
            if (i == -1 && buff.length() == 0) {
                return null;
            }
            if (i == -1 || i == 10) {
                return buff.toString();
            }
            if (i == 13) {
                this.skipLF = true;
                return buff.toString();
            }
            buff.append((char)i);
        }
    }
    
    public boolean addCompleter(final Completer completer) {
        return this.completers.add(completer);
    }
    
    public boolean removeCompleter(final Completer completer) {
        return this.completers.remove(completer);
    }
    
    public Collection<Completer> getCompleters() {
        return (Collection<Completer>)Collections.unmodifiableList((List<?>)this.completers);
    }
    
    public void setCompletionHandler(final CompletionHandler handler) {
        this.completionHandler = Preconditions.checkNotNull(handler);
    }
    
    public CompletionHandler getCompletionHandler() {
        return this.completionHandler;
    }
    
    protected boolean complete() throws IOException {
        if (this.completers.size() == 0) {
            return false;
        }
        final List<CharSequence> candidates = new LinkedList<CharSequence>();
        final String bufstr = this.buf.buffer.toString();
        final int cursor = this.buf.cursor;
        int position = -1;
        for (final Completer comp : this.completers) {
            if ((position = comp.complete(bufstr, cursor, candidates)) != -1) {
                break;
            }
        }
        return candidates.size() != 0 && this.getCompletionHandler().complete(this, candidates, position);
    }
    
    protected void printCompletionCandidates() throws IOException {
        if (this.completers.size() == 0) {
            return;
        }
        final List<CharSequence> candidates = new LinkedList<CharSequence>();
        final String bufstr = this.buf.buffer.toString();
        final int cursor = this.buf.cursor;
        for (final Completer comp : this.completers) {
            if (comp.complete(bufstr, cursor, candidates) != -1) {
                break;
            }
        }
        CandidateListCompletionHandler.printCandidates(this, candidates);
        this.drawLine();
    }
    
    public void setAutoprintThreshold(final int threshold) {
        this.autoprintThreshold = threshold;
    }
    
    public int getAutoprintThreshold() {
        return this.autoprintThreshold;
    }
    
    public void setPaginationEnabled(final boolean enabled) {
        this.paginationEnabled = enabled;
    }
    
    public boolean isPaginationEnabled() {
        return this.paginationEnabled;
    }
    
    public void setHistory(final History history) {
        this.history = history;
    }
    
    public History getHistory() {
        return this.history;
    }
    
    public void setHistoryEnabled(final boolean enabled) {
        this.historyEnabled = enabled;
    }
    
    public boolean isHistoryEnabled() {
        return this.historyEnabled;
    }
    
    private boolean moveHistory(final boolean next, final int count) throws IOException {
        boolean ok = true;
        for (int i = 0; i < count && (ok = this.moveHistory(next)); ++i) {}
        return ok;
    }
    
    private boolean moveHistory(final boolean next) throws IOException {
        if (next && !this.history.next()) {
            return false;
        }
        if (!next && !this.history.previous()) {
            return false;
        }
        this.setBuffer(this.history.current());
        return true;
    }
    
    private void print(final int c) throws IOException {
        if (c == 9) {
            final char[] chars = new char[4];
            Arrays.fill(chars, ' ');
            this.out.write(chars);
            return;
        }
        this.out.write(c);
    }
    
    private void print(final char... buff) throws IOException {
        int len = 0;
        for (final char c : buff) {
            if (c == '\t') {
                len += 4;
            }
            else {
                ++len;
            }
        }
        char[] chars;
        if (len == buff.length) {
            chars = buff;
        }
        else {
            chars = new char[len];
            int pos = 0;
            for (final char c2 : buff) {
                if (c2 == '\t') {
                    Arrays.fill(chars, pos, pos + 4, ' ');
                    pos += 4;
                }
                else {
                    chars[pos] = c2;
                    ++pos;
                }
            }
        }
        this.out.write(chars);
    }
    
    private void print(final char c, final int num) throws IOException {
        if (num == 1) {
            this.print(c);
        }
        else {
            final char[] chars = new char[num];
            Arrays.fill(chars, c);
            this.print(chars);
        }
    }
    
    public final void print(final CharSequence s) throws IOException {
        this.print(Preconditions.checkNotNull(s).toString().toCharArray());
    }
    
    public final void println(final CharSequence s) throws IOException {
        this.print(Preconditions.checkNotNull(s).toString().toCharArray());
        this.println();
    }
    
    public final void println() throws IOException {
        this.print(ConsoleReader.CR);
    }
    
    public final boolean delete() throws IOException {
        if (this.buf.cursor == this.buf.buffer.length()) {
            return false;
        }
        this.buf.buffer.delete(this.buf.cursor, this.buf.cursor + 1);
        this.drawBuffer(1);
        return true;
    }
    
    public boolean killLine() throws IOException {
        final int cp = this.buf.cursor;
        final int len = this.buf.buffer.length();
        if (cp >= len) {
            return false;
        }
        final int num = len - cp;
        this.clearAhead(num, 0);
        final char[] killed = new char[num];
        this.buf.buffer.getChars(cp, cp + num, killed, 0);
        this.buf.buffer.delete(cp, cp + num);
        final String copy = new String(killed);
        this.killRing.add(copy);
        return true;
    }
    
    public boolean yank() throws IOException {
        final String yanked = this.killRing.yank();
        if (yanked == null) {
            return false;
        }
        this.putString(yanked);
        return true;
    }
    
    public boolean yankPop() throws IOException {
        if (!this.killRing.lastYank()) {
            return false;
        }
        final String current = this.killRing.yank();
        if (current == null) {
            return false;
        }
        this.backspace(current.length());
        final String yanked = this.killRing.yankPop();
        if (yanked == null) {
            return false;
        }
        this.putString(yanked);
        return true;
    }
    
    public boolean clearScreen() throws IOException {
        if (!this.terminal.isAnsiSupported()) {
            return false;
        }
        this.printAnsiSequence("2J");
        this.printAnsiSequence("1;1H");
        return true;
    }
    
    public void beep() throws IOException {
        if (this.bellEnabled) {
            this.print(7);
            this.flush();
        }
    }
    
    public boolean paste() throws IOException {
        Clipboard clipboard;
        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        catch (Exception e2) {
            return false;
        }
        if (clipboard == null) {
            return false;
        }
        final Transferable transferable = clipboard.getContents(null);
        if (transferable == null) {
            return false;
        }
        try {
            Object content = transferable.getTransferData(DataFlavor.plainTextFlavor);
            if (content == null) {
                try {
                    content = new DataFlavor().getReaderForText(transferable);
                }
                catch (Exception ex) {}
            }
            if (content == null) {
                return false;
            }
            String value;
            if (content instanceof Reader) {
                value = "";
                final BufferedReader read = new BufferedReader((Reader)content);
                String line;
                while ((line = read.readLine()) != null) {
                    if (value.length() > 0) {
                        value += "\n";
                    }
                    value += line;
                }
            }
            else {
                value = content.toString();
            }
            if (value == null) {
                return true;
            }
            this.putString(value);
            return true;
        }
        catch (UnsupportedFlavorException e) {
            Log.error("Paste failed: ", e);
            return false;
        }
    }
    
    public void addTriggeredAction(final char c, final ActionListener listener) {
        this.triggeredActions.put(c, listener);
    }
    
    public void printColumns(final Collection<? extends CharSequence> items) throws IOException {
        if (items == null || items.isEmpty()) {
            return;
        }
        final int width = this.getTerminal().getWidth();
        final int height = this.getTerminal().getHeight();
        int maxWidth = 0;
        for (final CharSequence item : items) {
            maxWidth = Math.max(maxWidth, item.length());
        }
        maxWidth += 3;
        Log.debug("Max width: ", maxWidth);
        int showLines;
        if (this.isPaginationEnabled()) {
            showLines = height - 1;
        }
        else {
            showLines = Integer.MAX_VALUE;
        }
        final StringBuilder buff = new StringBuilder();
        for (final CharSequence item2 : items) {
            if (buff.length() + maxWidth > width) {
                this.println(buff);
                buff.setLength(0);
                if (--showLines == 0) {
                    this.print(ConsoleReader.resources.getString("DISPLAY_MORE"));
                    this.flush();
                    final int c = this.readCharacter();
                    if (c == 13 || c == 10) {
                        showLines = 1;
                    }
                    else if (c != 113) {
                        showLines = height - 1;
                    }
                    this.back(ConsoleReader.resources.getString("DISPLAY_MORE").length());
                    if (c == 113) {
                        break;
                    }
                }
            }
            buff.append(item2.toString());
            for (int i = 0; i < maxWidth - item2.length(); ++i) {
                buff.append(' ');
            }
        }
        if (buff.length() > 0) {
            this.println(buff);
        }
    }
    
    private void beforeReadLine(final String prompt, final Character mask) {
        if (mask != null && this.maskThread == null) {
            final String fullPrompt = "\r" + prompt + "                 " + "                 " + "                 " + "\r" + prompt;
            (this.maskThread = new Thread() {
                @Override
                public void run() {
                    while (!Thread.interrupted()) {
                        try {
                            final Writer out = ConsoleReader.this.getOutput();
                            out.write(fullPrompt);
                            out.flush();
                            Thread.sleep(3L);
                            continue;
                        }
                        catch (IOException e) {
                            return;
                        }
                        catch (InterruptedException e2) {
                            return;
                        }
                        break;
                    }
                }
            }).setPriority(10);
            this.maskThread.setDaemon(true);
            this.maskThread.start();
        }
    }
    
    private void afterReadLine() {
        if (this.maskThread != null && this.maskThread.isAlive()) {
            this.maskThread.interrupt();
        }
        this.maskThread = null;
    }
    
    public void resetPromptLine(final String prompt, final String buffer, int cursorDest) throws IOException {
        this.moveToEnd();
        this.buf.buffer.append(this.prompt);
        int promptLength = 0;
        if (this.prompt != null) {
            promptLength = this.prompt.length();
        }
        final CursorBuffer buf = this.buf;
        buf.cursor += promptLength;
        this.setPrompt("");
        this.backspaceAll();
        this.setPrompt(prompt);
        this.redrawLine();
        this.setBuffer(buffer);
        if (cursorDest < 0) {
            cursorDest = buffer.length();
        }
        this.setCursorPosition(cursorDest);
        this.flush();
    }
    
    public void printSearchStatus(final String searchTerm, final String match) throws IOException {
        this.printSearchStatus(searchTerm, match, "(reverse-i-search)`");
    }
    
    public void printForwardSearchStatus(final String searchTerm, final String match) throws IOException {
        this.printSearchStatus(searchTerm, match, "(i-search)`");
    }
    
    private void printSearchStatus(final String searchTerm, final String match, final String searchLabel) throws IOException {
        final String prompt = searchLabel + searchTerm + "': ";
        final int cursorDest = match.indexOf(searchTerm);
        this.resetPromptLine(prompt, match, cursorDest);
    }
    
    public void restoreLine(final String originalPrompt, final int cursorDest) throws IOException {
        final String prompt = this.lastLine(originalPrompt);
        final String buffer = this.buf.buffer.toString();
        this.resetPromptLine(prompt, buffer, cursorDest);
    }
    
    public int searchBackwards(final String searchTerm, final int startIndex) {
        return this.searchBackwards(searchTerm, startIndex, false);
    }
    
    public int searchBackwards(final String searchTerm) {
        return this.searchBackwards(searchTerm, this.history.index());
    }
    
    public int searchBackwards(final String searchTerm, final int startIndex, final boolean startsWith) {
        final ListIterator<History.Entry> it = this.history.entries(startIndex);
        while (it.hasPrevious()) {
            final History.Entry e = it.previous();
            if (startsWith) {
                if (e.value().toString().startsWith(searchTerm)) {
                    return e.index();
                }
                continue;
            }
            else {
                if (e.value().toString().contains(searchTerm)) {
                    return e.index();
                }
                continue;
            }
        }
        return -1;
    }
    
    public int searchForwards(final String searchTerm, final int startIndex) {
        return this.searchForwards(searchTerm, startIndex, false);
    }
    
    public int searchForwards(final String searchTerm) {
        return this.searchForwards(searchTerm, this.history.index());
    }
    
    public int searchForwards(final String searchTerm, int startIndex, final boolean startsWith) {
        if (startIndex >= this.history.size()) {
            startIndex = this.history.size() - 1;
        }
        final ListIterator<History.Entry> it = this.history.entries(startIndex);
        if (this.searchIndex != -1 && it.hasNext()) {
            it.next();
        }
        while (it.hasNext()) {
            final History.Entry e = it.next();
            if (startsWith) {
                if (e.value().toString().startsWith(searchTerm)) {
                    return e.index();
                }
                continue;
            }
            else {
                if (e.value().toString().contains(searchTerm)) {
                    return e.index();
                }
                continue;
            }
        }
        return -1;
    }
    
    private boolean isDelimiter(final char c) {
        return !Character.isLetterOrDigit(c);
    }
    
    private boolean isWhitespace(final char c) {
        return Character.isWhitespace(c);
    }
    
    private void printAnsiSequence(final String sequence) throws IOException {
        this.print(27);
        this.print(91);
        this.print(sequence);
        this.flush();
    }
    
    static {
        resources = ResourceBundle.getBundle(CandidateListCompletionHandler.class.getName());
        CR = Configuration.getLineSeparator();
    }
    
    private enum State
    {
        NORMAL, 
        SEARCH, 
        FORWARD_SEARCH, 
        VI_YANK_TO, 
        VI_DELETE_TO, 
        VI_CHANGE_TO;
    }
}
