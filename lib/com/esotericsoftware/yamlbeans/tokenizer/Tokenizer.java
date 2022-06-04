// 
// Decompiled by Procyon v0.5.36
// 

package com.esotericsoftware.yamlbeans.tokenizer;

import java.io.FileReader;
import java.util.regex.Matcher;
import java.io.IOException;
import java.util.Iterator;
import java.io.StringReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.io.Reader;
import java.util.Map;
import java.util.regex.Pattern;

public class Tokenizer
{
    private static final String LINEBR = "\n\u0085\u2028\u2029";
    private static final String NULL_BL_LINEBR = "\u0000 \r\n\u0085";
    private static final String NULL_BL_T_LINEBR = "\u0000 \t\r\n\u0085";
    private static final String NULL_OR_OTHER = "\u0000 \t\r\n\u0085";
    private static final String NULL_OR_LINEBR = "\u0000\r\n\u0085";
    private static final String FULL_LINEBR = "\r\n\u0085";
    private static final String BLANK_OR_LINEBR = " \r\n\u0085";
    private static final String S4 = "\u0000 \t\r\n([]{}";
    private static final String ALPHA = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
    private static final String STRANGE_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-#;/?:@&=+$,_.!~*'()[]";
    private static final String RN = "\r\n";
    private static final String BLANK_T = " \t";
    private static final String SPACES_AND_STUFF = "'\"\\\u0000 \t\r\n\u0085";
    private static final String DOUBLE_ESC = "\"\\";
    private static final String NON_ALPHA_OR_NUM = "\u0000 \t\r\n\u0085?:,]}%@`";
    private static final Pattern NON_PRINTABLE;
    private static final Pattern NOT_HEXA;
    private static final Pattern NON_ALPHA;
    private static final Pattern R_FLOWZERO;
    private static final Pattern R_FLOWNONZERO;
    private static final Pattern END_OR_START;
    private static final Pattern ENDING;
    private static final Pattern START;
    private static final Pattern BEG;
    private static final Map<Character, String> ESCAPE_REPLACEMENTS;
    private static final Map<Character, Integer> ESCAPE_CODES;
    private boolean done;
    private int flowLevel;
    private int tokensTaken;
    private int indent;
    private boolean allowSimpleKey;
    private boolean eof;
    private int lineNumber;
    private int column;
    private int pointer;
    private final StringBuilder buffer;
    private final Reader reader;
    private final List<Token> tokens;
    private final List<Integer> indents;
    private final Map<Integer, SimpleKey> possibleSimpleKeys;
    private boolean docStart;
    
    public Tokenizer(Reader reader) {
        this.done = false;
        this.flowLevel = 0;
        this.tokensTaken = 0;
        this.indent = -1;
        this.allowSimpleKey = true;
        this.lineNumber = 0;
        this.column = 0;
        this.pointer = 0;
        this.tokens = new LinkedList<Token>();
        this.indents = new LinkedList<Integer>();
        this.possibleSimpleKeys = new HashMap<Integer, SimpleKey>();
        this.docStart = false;
        if (reader == null) {
            throw new IllegalArgumentException("reader cannot be null.");
        }
        if (!(reader instanceof BufferedReader)) {
            reader = new BufferedReader(reader);
        }
        this.reader = reader;
        this.buffer = new StringBuilder();
        this.eof = false;
        this.fetchStreamStart();
    }
    
    public Tokenizer(final String yaml) {
        this(new StringReader(yaml));
    }
    
    public Token peekNextToken() throws TokenizerException {
        while (this.needMoreTokens()) {
            this.fetchMoreTokens();
        }
        return this.tokens.isEmpty() ? null : this.tokens.get(0);
    }
    
    public TokenType peekNextTokenType() throws TokenizerException {
        final Token token = this.peekNextToken();
        if (token == null) {
            return null;
        }
        return token.type;
    }
    
    public Token getNextToken() throws TokenizerException {
        while (this.needMoreTokens()) {
            this.fetchMoreTokens();
        }
        if (!this.tokens.isEmpty()) {
            ++this.tokensTaken;
            final Token token = this.tokens.remove(0);
            return token;
        }
        return null;
    }
    
    public Iterator iterator() {
        return new Iterator() {
            public boolean hasNext() {
                return null != Tokenizer.this.peekNextToken();
            }
            
            public Object next() {
                return Tokenizer.this.getNextToken();
            }
            
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    public int getLineNumber() {
        return this.lineNumber;
    }
    
    public int getColumn() {
        return this.column;
    }
    
    public void close() throws IOException {
        this.reader.close();
    }
    
    private char peek() {
        if (this.pointer + 1 > this.buffer.length()) {
            this.update(1);
        }
        return this.buffer.charAt(this.pointer);
    }
    
    private char peek(final int index) {
        if (this.pointer + index + 1 > this.buffer.length()) {
            this.update(index + 1);
        }
        return this.buffer.charAt(this.pointer + index);
    }
    
    private String prefix(final int length) {
        if (this.pointer + length >= this.buffer.length()) {
            this.update(length);
        }
        if (this.pointer + length > this.buffer.length()) {
            return this.buffer.substring(this.pointer, this.buffer.length());
        }
        return this.buffer.substring(this.pointer, this.pointer + length);
    }
    
    private String prefixForward(final int length) {
        if (this.pointer + length + 1 >= this.buffer.length()) {
            this.update(length + 1);
        }
        String buff = null;
        if (this.pointer + length > this.buffer.length()) {
            buff = this.buffer.substring(this.pointer, this.buffer.length());
        }
        else {
            buff = this.buffer.substring(this.pointer, this.pointer + length);
        }
        char ch = '\0';
        for (int i = 0, j = buff.length(); i < j; ++i) {
            ch = buff.charAt(i);
            ++this.pointer;
            if ("\n\u0085\u2028\u2029".indexOf(ch) != -1 || (ch == '\r' && buff.charAt(i + 1) != '\n')) {
                this.column = 0;
                ++this.lineNumber;
            }
            else if (ch != '\ufeff') {
                ++this.column;
            }
        }
        return buff;
    }
    
    private void forward() {
        if (this.pointer + 2 >= this.buffer.length()) {
            this.update(2);
        }
        final char ch1 = this.buffer.charAt(this.pointer);
        ++this.pointer;
        if (ch1 == '\n' || ch1 == '\u0085' || (ch1 == '\r' && this.buffer.charAt(this.pointer) != '\n')) {
            this.column = 0;
            ++this.lineNumber;
        }
        else {
            ++this.column;
        }
    }
    
    private void forward(final int length) {
        if (this.pointer + length + 1 >= this.buffer.length()) {
            this.update(length + 1);
        }
        char ch = '\0';
        for (int i = 0; i < length; ++i) {
            ch = this.buffer.charAt(this.pointer);
            ++this.pointer;
            if ("\n\u0085\u2028\u2029".indexOf(ch) != -1 || (ch == '\r' && this.buffer.charAt(this.pointer) != '\n')) {
                this.column = 0;
                ++this.lineNumber;
            }
            else if (ch != '\ufeff') {
                ++this.column;
            }
        }
    }
    
    private void update(final int length) {
        this.buffer.delete(0, this.pointer);
        this.pointer = 0;
        while (this.buffer.length() < length) {
            String rawData = "";
            if (!this.eof) {
                final char[] data = new char[1024];
                int converted = -2;
                try {
                    converted = this.reader.read(data);
                }
                catch (IOException ioe) {
                    throw new TokenizerException("Error reading from stream.", ioe);
                }
                if (converted == -1) {
                    this.eof = true;
                }
                else {
                    rawData = String.valueOf(data, 0, converted);
                }
            }
            this.buffer.append(rawData);
            if (this.eof) {
                this.buffer.append('\0');
                break;
            }
        }
    }
    
    private boolean needMoreTokens() {
        return !this.done && (this.tokens.isEmpty() || this.nextPossibleSimpleKey() == this.tokensTaken);
    }
    
    private Token fetchMoreTokens() {
        this.scanToNextToken();
        this.unwindIndent(this.column);
        final char ch = this.peek();
        final boolean colz = this.column == 0;
        switch (ch) {
            case '\0': {
                return this.fetchStreamEnd();
            }
            case '\'': {
                return this.fetchSingle();
            }
            case '\"': {
                return this.fetchDouble();
            }
            case '?': {
                if (this.flowLevel != 0 || "\u0000 \t\r\n\u0085".indexOf(this.peek(1)) != -1) {
                    return this.fetchKey();
                }
                break;
            }
            case ':': {
                if (this.flowLevel != 0 || "\u0000 \t\r\n\u0085".indexOf(this.peek(1)) != -1) {
                    return this.fetchValue();
                }
                break;
            }
            case '%': {
                if (colz) {
                    return this.fetchDirective();
                }
                break;
            }
            case '-': {
                if ((colz || this.docStart) && Tokenizer.ENDING.matcher(this.prefix(4)).matches()) {
                    return this.fetchDocumentStart();
                }
                if ("\u0000 \t\r\n\u0085".indexOf(this.peek(1)) != -1) {
                    return this.fetchBlockEntry();
                }
                break;
            }
            case '.': {
                if (colz && Tokenizer.START.matcher(this.prefix(4)).matches()) {
                    return this.fetchDocumentEnd();
                }
                break;
            }
            case '[': {
                return this.fetchFlowSequenceStart();
            }
            case '{': {
                return this.fetchFlowMappingStart();
            }
            case ']': {
                return this.fetchFlowSequenceEnd();
            }
            case '}': {
                return this.fetchFlowMappingEnd();
            }
            case ',': {
                return this.fetchFlowEntry();
            }
            case '*': {
                return this.fetchAlias();
            }
            case '&': {
                return this.fetchAnchor();
            }
            case '!': {
                return this.fetchTag();
            }
            case '|': {
                if (this.flowLevel == 0) {
                    return this.fetchLiteral();
                }
                break;
            }
            case '>': {
                if (this.flowLevel == 0) {
                    return this.fetchFolded();
                }
                break;
            }
        }
        if (Tokenizer.BEG.matcher(this.prefix(2)).find()) {
            return this.fetchPlain();
        }
        if (ch == '\t') {
            throw new TokenizerException("Tabs cannot be used for indentation.");
        }
        throw new TokenizerException("While scanning for the next token, a character that cannot begin a token was found: " + this.ch(ch));
    }
    
    private int nextPossibleSimpleKey() {
        for (final SimpleKey key : this.possibleSimpleKeys.values()) {
            if (key.tokenNumber > 0) {
                return key.tokenNumber;
            }
        }
        return -1;
    }
    
    private void savePossibleSimpleKey() {
        if (this.allowSimpleKey) {
            this.possibleSimpleKeys.put(this.flowLevel, new SimpleKey(this.tokensTaken + this.tokens.size(), this.column));
        }
    }
    
    private void unwindIndent(final int col) {
        if (this.flowLevel != 0) {
            return;
        }
        while (this.indent > col) {
            this.indent = this.indents.remove(0);
            this.tokens.add(Token.BLOCK_END);
        }
    }
    
    private boolean addIndent(final int col) {
        if (this.indent < col) {
            this.indents.add(0, this.indent);
            this.indent = col;
            return true;
        }
        return false;
    }
    
    private Token fetchStreamStart() {
        this.docStart = true;
        this.tokens.add(Token.STREAM_START);
        return Token.STREAM_START;
    }
    
    private Token fetchStreamEnd() {
        this.unwindIndent(-1);
        this.allowSimpleKey = false;
        this.possibleSimpleKeys.clear();
        this.tokens.add(Token.STREAM_END);
        this.done = true;
        return Token.STREAM_END;
    }
    
    private Token fetchDirective() {
        this.unwindIndent(-1);
        this.allowSimpleKey = false;
        final Token tok = this.scanDirective();
        this.tokens.add(tok);
        return tok;
    }
    
    private Token fetchDocumentStart() {
        this.docStart = false;
        return this.fetchDocumentIndicator(Token.DOCUMENT_START);
    }
    
    private Token fetchDocumentEnd() {
        return this.fetchDocumentIndicator(Token.DOCUMENT_END);
    }
    
    private Token fetchDocumentIndicator(final Token tok) {
        this.unwindIndent(-1);
        this.allowSimpleKey = false;
        this.forward(3);
        this.tokens.add(tok);
        return tok;
    }
    
    private Token fetchFlowSequenceStart() {
        return this.fetchFlowCollectionStart(Token.FLOW_SEQUENCE_START);
    }
    
    private Token fetchFlowMappingStart() {
        return this.fetchFlowCollectionStart(Token.FLOW_MAPPING_START);
    }
    
    private Token fetchFlowCollectionStart(final Token tok) {
        this.savePossibleSimpleKey();
        ++this.flowLevel;
        this.allowSimpleKey = true;
        this.forward(1);
        this.tokens.add(tok);
        return tok;
    }
    
    private Token fetchFlowSequenceEnd() {
        return this.fetchFlowCollectionEnd(Token.FLOW_SEQUENCE_END);
    }
    
    private Token fetchFlowMappingEnd() {
        return this.fetchFlowCollectionEnd(Token.FLOW_MAPPING_END);
    }
    
    private Token fetchFlowCollectionEnd(final Token tok) {
        --this.flowLevel;
        this.allowSimpleKey = false;
        this.forward(1);
        this.tokens.add(tok);
        return tok;
    }
    
    private Token fetchFlowEntry() {
        this.allowSimpleKey = true;
        this.forward(1);
        this.tokens.add(Token.FLOW_ENTRY);
        return Token.FLOW_ENTRY;
    }
    
    private Token fetchBlockEntry() {
        if (this.flowLevel == 0) {
            if (!this.allowSimpleKey) {
                throw new TokenizerException("Found a sequence entry where it is not allowed.");
            }
            if (this.addIndent(this.column)) {
                this.tokens.add(Token.BLOCK_SEQUENCE_START);
            }
        }
        this.allowSimpleKey = true;
        this.forward();
        this.tokens.add(Token.BLOCK_ENTRY);
        return Token.BLOCK_ENTRY;
    }
    
    private Token fetchKey() {
        if (this.flowLevel == 0) {
            if (!this.allowSimpleKey) {
                throw new TokenizerException("Found a mapping key where it is not allowed.");
            }
            if (this.addIndent(this.column)) {
                this.tokens.add(Token.BLOCK_MAPPING_START);
            }
        }
        this.allowSimpleKey = (this.flowLevel == 0);
        this.forward();
        this.tokens.add(Token.KEY);
        return Token.KEY;
    }
    
    private Token fetchValue() {
        final SimpleKey key = this.possibleSimpleKeys.get(this.flowLevel);
        if (null == key) {
            if (this.flowLevel == 0 && !this.allowSimpleKey) {
                throw new TokenizerException("Found a mapping value where it is not allowed.");
            }
        }
        else {
            this.possibleSimpleKeys.remove(this.flowLevel);
            this.tokens.add(key.tokenNumber - this.tokensTaken, Token.KEY);
            if (this.flowLevel == 0 && this.addIndent(key.column)) {
                this.tokens.add(key.tokenNumber - this.tokensTaken, Token.BLOCK_MAPPING_START);
            }
            this.allowSimpleKey = false;
        }
        this.forward();
        this.tokens.add(Token.VALUE);
        return Token.VALUE;
    }
    
    private Token fetchAlias() {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = this.scanAnchor(new AliasToken());
        this.tokens.add(tok);
        return tok;
    }
    
    private Token fetchAnchor() {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = this.scanAnchor(new AnchorToken());
        this.tokens.add(tok);
        return tok;
    }
    
    private Token fetchTag() {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = this.scanTag();
        this.tokens.add(tok);
        return tok;
    }
    
    private Token fetchLiteral() {
        return this.fetchBlockScalar('|');
    }
    
    private Token fetchFolded() {
        return this.fetchBlockScalar('>');
    }
    
    private Token fetchBlockScalar(final char style) {
        this.allowSimpleKey = true;
        final Token tok = this.scanBlockScalar(style);
        this.tokens.add(tok);
        return tok;
    }
    
    private Token fetchSingle() {
        return this.fetchFlowScalar('\'');
    }
    
    private Token fetchDouble() {
        return this.fetchFlowScalar('\"');
    }
    
    private Token fetchFlowScalar(final char style) {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = this.scanFlowScalar(style);
        this.tokens.add(tok);
        return tok;
    }
    
    private Token fetchPlain() {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = this.scanPlain();
        this.tokens.add(tok);
        return tok;
    }
    
    private void scanToNextToken() {
        while (true) {
            if (this.peek() == ' ') {
                this.forward();
            }
            else {
                if (this.peek() == '#') {
                    while ("\u0000\r\n\u0085".indexOf(this.peek()) == -1) {
                        this.forward();
                    }
                }
                if (this.scanLineBreak().length() == 0) {
                    break;
                }
                if (this.flowLevel != 0) {
                    continue;
                }
                this.allowSimpleKey = true;
            }
        }
    }
    
    private Token scanDirective() {
        this.forward();
        final String name = this.scanDirectiveName();
        String value = null;
        if (name.equals("YAML")) {
            value = this.scanYamlDirectiveValue();
        }
        else if (name.equals("TAG")) {
            value = this.scanTagDirectiveValue();
        }
        else {
            final StringBuilder buffer = new StringBuilder();
            while (true) {
                final char ch = this.peek();
                if ("\u0000\r\n\u0085".indexOf(ch) != -1) {
                    break;
                }
                buffer.append(ch);
                this.forward();
            }
            value = buffer.toString().trim();
        }
        this.scanDirectiveIgnoredLine();
        return new DirectiveToken(name, value);
    }
    
    private String scanDirectiveName() {
        int length = 0;
        char ch = this.peek(length);
        boolean zlen = true;
        while ("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_".indexOf(ch) != -1) {
            zlen = false;
            ++length;
            ch = this.peek(length);
        }
        if (zlen) {
            throw new TokenizerException("While scanning for a directive name, expected an alpha or numeric character but found: " + this.ch(ch));
        }
        final String value = this.prefixForward(length);
        if ("\u0000 \r\n\u0085".indexOf(this.peek()) == -1) {
            throw new TokenizerException("While scanning for a directive name, expected an alpha or numeric character but found: " + this.ch(ch));
        }
        return value;
    }
    
    private String scanYamlDirectiveValue() {
        while (this.peek() == ' ') {
            this.forward();
        }
        final String major = this.scanYamlDirectiveNumber();
        if (this.peek() != '.') {
            throw new TokenizerException("While scanning for a directive value, expected a digit or '.' but found: " + this.ch(this.peek()));
        }
        this.forward();
        final String minor = this.scanYamlDirectiveNumber();
        if ("\u0000 \r\n\u0085".indexOf(this.peek()) == -1) {
            throw new TokenizerException("While scanning for a directive value, expected a digit or '.' but found: " + this.ch(this.peek()));
        }
        return major + "." + minor;
    }
    
    private String scanYamlDirectiveNumber() {
        final char ch = this.peek();
        if (!Character.isDigit(ch)) {
            throw new TokenizerException("While scanning for a directive number, expected a digit but found: " + this.ch(ch));
        }
        int length;
        for (length = 0; Character.isDigit(this.peek(length)); ++length) {}
        final String value = this.prefixForward(length);
        return value;
    }
    
    private String scanTagDirectiveValue() {
        while (this.peek() == ' ') {
            this.forward();
        }
        final String handle = this.scanTagDirectiveHandle();
        while (this.peek() == ' ') {
            this.forward();
        }
        final String prefix = this.scanTagDirectivePrefix();
        return handle + " " + prefix;
    }
    
    private String scanTagDirectiveHandle() {
        final String value = this.scanTagHandle("directive");
        if (this.peek() != ' ') {
            throw new TokenizerException("While scanning for a directive tag handle, expected ' ' but found: " + this.ch(this.peek()));
        }
        return value;
    }
    
    private String scanTagDirectivePrefix() {
        final String value = this.scanTagUri("directive");
        if ("\u0000 \r\n\u0085".indexOf(this.peek()) == -1) {
            throw new TokenizerException("While scanning for a directive tag prefix, expected ' ' but found: " + this.ch(this.peek()));
        }
        return value;
    }
    
    private String scanDirectiveIgnoredLine() {
        while (this.peek() == ' ') {
            this.forward();
        }
        if (this.peek() == '\"') {
            while ("\u0000\r\n\u0085".indexOf(this.peek()) == -1) {
                this.forward();
            }
        }
        final char ch = this.peek();
        if ("\u0000\r\n\u0085".indexOf(ch) == -1) {
            throw new TokenizerException("While scanning a directive, expected a comment or line break but found: " + this.ch(this.peek()));
        }
        return this.scanLineBreak();
    }
    
    private Token scanAnchor(final Token tok) {
        final char indicator = this.peek();
        final String name = (indicator == '*') ? "alias" : "anchor";
        this.forward();
        int length = 0;
        int chunk_size = 16;
        Matcher m = null;
        while (true) {
            final String chunk = this.prefix(chunk_size);
            if ((m = Tokenizer.NON_ALPHA.matcher(chunk)).find()) {
                break;
            }
            chunk_size += 16;
        }
        length = m.start();
        if (length == 0) {
            throw new TokenizerException("While scanning an " + name + ", a non-alpha, non-numeric character was found.");
        }
        final String value = this.prefixForward(length);
        if ("\u0000 \t\r\n\u0085?:,]}%@`".indexOf(this.peek()) == -1) {
            throw new TokenizerException("While scanning an " + name + ", expected an alpha or numeric character but found: " + this.ch(this.peek()));
        }
        if (tok instanceof AnchorToken) {
            ((AnchorToken)tok).setInstanceName(value);
        }
        else {
            ((AliasToken)tok).setInstanceName(value);
        }
        return tok;
    }
    
    private Token scanTag() {
        char ch = this.peek(1);
        String handle = null;
        String suffix = null;
        if (ch == '<') {
            this.forward(2);
            suffix = this.scanTagUri("tag");
            if (this.peek() != '>') {
                throw new TokenizerException("While scanning a tag, expected '>' but found: " + this.ch(this.peek()));
            }
            this.forward();
        }
        else if ("\u0000 \t\r\n\u0085".indexOf(ch) != -1) {
            suffix = "!";
            this.forward();
        }
        else {
            int length = 1;
            boolean useHandle = false;
            while ("\u0000 \t\r\n\u0085".indexOf(ch) == -1) {
                if (ch == '!') {
                    useHandle = true;
                    break;
                }
                ++length;
                ch = this.peek(length);
            }
            handle = "!";
            if (useHandle) {
                handle = this.scanTagHandle("tag");
            }
            else {
                handle = "!";
                this.forward();
            }
            suffix = this.scanTagUri("tag");
        }
        if ("\u0000 \r\n\u0085".indexOf(this.peek()) == -1) {
            throw new TokenizerException("While scanning a tag, expected ' ' but found: " + this.ch(this.peek()));
        }
        return new TagToken(handle, suffix);
    }
    
    private Token scanBlockScalar(final char style) {
        final boolean folded = style == '>';
        final StringBuilder chunks = new StringBuilder();
        this.forward();
        final Object[] chompi = this.scanBlockScalarIndicators();
        final boolean chomping = (boolean)chompi[0];
        final int increment = (int)chompi[1];
        this.scanBlockScalarIgnoredLine();
        int minIndent = this.indent + 1;
        if (minIndent < 1) {
            minIndent = 1;
        }
        String breaks = null;
        int maxIndent = 0;
        int ind = 0;
        if (increment == -1) {
            final Object[] brme = this.scanBlockScalarIndentation();
            breaks = (String)brme[0];
            maxIndent = (int)brme[1];
            if (minIndent > maxIndent) {
                ind = minIndent;
            }
            else {
                ind = maxIndent;
            }
        }
        else {
            ind = minIndent + increment - 1;
            breaks = this.scanBlockScalarBreaks(ind);
        }
        String lineBreak = "";
        while (this.column == ind && this.peek() != '\0') {
            chunks.append(breaks);
            final boolean leadingNonSpace = " \t".indexOf(this.peek()) == -1;
            int length;
            for (length = 0; "\u0000\r\n\u0085".indexOf(this.peek(length)) == -1; ++length) {}
            chunks.append(this.prefixForward(length));
            lineBreak = this.scanLineBreak();
            breaks = this.scanBlockScalarBreaks(ind);
            if (this.column != ind || this.peek() == '\0') {
                break;
            }
            if (folded && lineBreak.equals("\n") && leadingNonSpace && " \t".indexOf(this.peek()) == -1) {
                if (breaks.length() != 0) {
                    continue;
                }
                chunks.append(" ");
            }
            else {
                chunks.append(lineBreak);
            }
        }
        if (chomping) {
            chunks.append(lineBreak);
            chunks.append(breaks);
        }
        return new ScalarToken(chunks.toString(), false, style);
    }
    
    private Object[] scanBlockScalarIndicators() {
        boolean chomping = false;
        int increment = -1;
        char ch = this.peek();
        if (ch == '-' || ch == '+') {
            chomping = (ch == '+');
            this.forward();
            ch = this.peek();
            if (Character.isDigit(ch)) {
                increment = Integer.parseInt("" + ch);
                if (increment == 0) {
                    throw new TokenizerException("While scanning a black scaler, expected indentation indicator between 1 and 9 but found: 0");
                }
                this.forward();
            }
        }
        else if (Character.isDigit(ch)) {
            increment = Integer.parseInt("" + ch);
            if (increment == 0) {
                throw new TokenizerException("While scanning a black scaler, expected indentation indicator between 1 and 9 but found: 0");
            }
            this.forward();
            ch = this.peek();
            if (ch == '-' || ch == '+') {
                chomping = (ch == '+');
                this.forward();
            }
        }
        if ("\u0000 \r\n\u0085".indexOf(this.peek()) == -1) {
            throw new TokenizerException("While scanning a block scalar, expected chomping or indentation indicators but found: " + this.ch(this.peek()));
        }
        return new Object[] { chomping, increment };
    }
    
    private String scanBlockScalarIgnoredLine() {
        while (this.peek() == ' ') {
            this.forward();
        }
        if (this.peek() == '#') {
            while ("\u0000\r\n\u0085".indexOf(this.peek()) == -1) {
                this.forward();
            }
        }
        if ("\u0000\r\n\u0085".indexOf(this.peek()) == -1) {
            throw new TokenizerException("While scanning a block scalar, expected a comment or line break but found: " + this.ch(this.peek()));
        }
        return this.scanLineBreak();
    }
    
    private Object[] scanBlockScalarIndentation() {
        final StringBuilder chunks = new StringBuilder();
        int maxIndent = 0;
        while (" \r\n\u0085".indexOf(this.peek()) != -1) {
            if (this.peek() != ' ') {
                chunks.append(this.scanLineBreak());
            }
            else {
                this.forward();
                if (this.column <= maxIndent) {
                    continue;
                }
                maxIndent = this.column;
            }
        }
        return new Object[] { chunks.toString(), maxIndent };
    }
    
    private String scanBlockScalarBreaks(final int indent) {
        final StringBuilder chunks = new StringBuilder();
        while (this.column < indent && this.peek() == ' ') {
            this.forward();
        }
        while ("\r\n\u0085".indexOf(this.peek()) != -1) {
            chunks.append(this.scanLineBreak());
            while (this.column < indent && this.peek() == ' ') {
                this.forward();
            }
        }
        return chunks.toString();
    }
    
    private Token scanFlowScalar(final char style) {
        final boolean dbl = style == '\"';
        final StringBuilder chunks = new StringBuilder();
        final char quote = this.peek();
        this.forward();
        chunks.append(this.scanFlowScalarNonSpaces(dbl));
        while (this.peek() != quote) {
            chunks.append(this.scanFlowScalarSpaces());
            chunks.append(this.scanFlowScalarNonSpaces(dbl));
        }
        this.forward();
        return new ScalarToken(chunks.toString(), false, style);
    }
    
    private String scanFlowScalarNonSpaces(final boolean dbl) {
        final StringBuilder chunks = new StringBuilder();
        while (true) {
            int length;
            for (length = 0; "'\"\\\u0000 \t\r\n\u0085".indexOf(this.peek(length)) == -1; ++length) {}
            if (length != 0) {
                chunks.append(this.prefixForward(length));
            }
            char ch = this.peek();
            if (!dbl && ch == '\'' && this.peek(1) == '\'') {
                chunks.append("'");
                this.forward(2);
            }
            else if ((dbl && ch == '\'') || (!dbl && "\"\\".indexOf(ch) != -1)) {
                chunks.append(ch);
                this.forward();
            }
            else {
                if (!dbl || ch != '\\') {
                    return chunks.toString();
                }
                this.forward();
                ch = this.peek();
                if (Tokenizer.ESCAPE_REPLACEMENTS.containsKey(ch)) {
                    chunks.append(Tokenizer.ESCAPE_REPLACEMENTS.get(ch));
                    this.forward();
                }
                else if (Tokenizer.ESCAPE_CODES.containsKey(ch)) {
                    length = Tokenizer.ESCAPE_CODES.get(ch);
                    this.forward();
                    final String val = this.prefix(length);
                    if (Tokenizer.NOT_HEXA.matcher(val).find()) {
                        throw new TokenizerException("While scanning a double quoted scalar, expected an escape sequence of " + length + " hexadecimal numbers but found: " + this.ch(this.peek()));
                    }
                    chunks.append(Character.toChars(Integer.parseInt(val, 16)));
                    this.forward(length);
                }
                else {
                    if ("\r\n\u0085".indexOf(ch) == -1) {
                        throw new TokenizerException("While scanning a double quoted scalar, found unknown escape character: " + this.ch(ch));
                    }
                    this.scanLineBreak();
                    chunks.append(this.scanFlowScalarBreaks());
                }
            }
        }
    }
    
    private String scanFlowScalarSpaces() {
        final StringBuilder chunks = new StringBuilder();
        int length;
        for (length = 0; " \t".indexOf(this.peek(length)) != -1; ++length) {}
        final String whitespaces = this.prefixForward(length);
        final char ch = this.peek();
        if (ch == '\0') {
            throw new TokenizerException("While scanning a quoted scalar, found unexpected end of stream.");
        }
        if ("\r\n\u0085".indexOf(ch) != -1) {
            final String lineBreak = this.scanLineBreak();
            final String breaks = this.scanFlowScalarBreaks();
            if (!lineBreak.equals("\n")) {
                chunks.append(lineBreak);
            }
            else if (breaks.length() == 0) {
                chunks.append(" ");
            }
            chunks.append(breaks);
        }
        else {
            chunks.append(whitespaces);
        }
        return chunks.toString();
    }
    
    private String scanFlowScalarBreaks() {
        final StringBuilder chunks = new StringBuilder();
        String pre = null;
        while (true) {
            pre = this.prefix(3);
            if ((pre.equals("---") || pre.equals("...")) && "\u0000 \t\r\n\u0085".indexOf(this.peek(3)) != -1) {
                throw new TokenizerException("While scanning a quoted scalar, found unexpected document separator.");
            }
            while (" \t".indexOf(this.peek()) != -1) {
                this.forward();
            }
            if ("\r\n\u0085".indexOf(this.peek()) == -1) {
                return chunks.toString();
            }
            chunks.append(this.scanLineBreak());
        }
    }
    
    private Token scanPlain() {
        final StringBuilder chunks = new StringBuilder();
        final int ind = this.indent + 1;
        String spaces = "";
        boolean f_nzero = true;
        Pattern r_check = Tokenizer.R_FLOWNONZERO;
        if (this.flowLevel == 0) {
            f_nzero = false;
            r_check = Tokenizer.R_FLOWZERO;
        }
        while (this.peek() != '#') {
            int length = 0;
            int chunkSize = 32;
            Matcher m = null;
            while (!(m = r_check.matcher(this.prefix(chunkSize))).find()) {
                chunkSize += 32;
            }
            length = m.start();
            final char ch = this.peek(length);
            if (f_nzero && ch == ':' && "\u0000 \t\r\n([]{}".indexOf(this.peek(length + 1)) == -1) {
                this.forward(length);
                throw new TokenizerException("While scanning a plain scalar, found unexpected ':'. See: http://pyyaml.org/wiki/YAMLColonInFlowContext");
            }
            if (length == 0) {
                break;
            }
            this.allowSimpleKey = false;
            chunks.append(spaces);
            chunks.append(this.prefixForward(length));
            spaces = this.scanPlainSpaces();
            if (spaces.length() == 0) {
                break;
            }
            if (this.flowLevel == 0 && this.column < ind) {
                break;
            }
        }
        return new ScalarToken(chunks.toString(), true);
    }
    
    private String scanPlainSpaces() {
        final StringBuilder chunks = new StringBuilder();
        int length;
        for (length = 0; this.peek(length) == ' ' || this.peek(length) == '\t'; ++length) {}
        final String whitespaces = this.prefixForward(length);
        final char ch = this.peek();
        if ("\r\n\u0085".indexOf(ch) != -1) {
            final String lineBreak = this.scanLineBreak();
            this.allowSimpleKey = true;
            if (Tokenizer.END_OR_START.matcher(this.prefix(4)).matches()) {
                return "";
            }
            final StringBuilder breaks = new StringBuilder();
            while (" \r\n\u0085".indexOf(this.peek()) != -1) {
                if (' ' == this.peek()) {
                    this.forward();
                }
                else {
                    breaks.append(this.scanLineBreak());
                    if (Tokenizer.END_OR_START.matcher(this.prefix(4)).matches()) {
                        return "";
                    }
                    continue;
                }
            }
            if (!lineBreak.equals("\n")) {
                chunks.append(lineBreak);
            }
            else if (breaks.length() == 0) {
                chunks.append(" ");
            }
            chunks.append((CharSequence)breaks);
        }
        else {
            chunks.append(whitespaces);
        }
        return chunks.toString();
    }
    
    private String scanTagHandle(final String name) {
        char ch = this.peek();
        if (ch != '!') {
            throw new TokenizerException("While scanning a " + name + ", expected '!' but found: " + this.ch(ch));
        }
        int length = 1;
        ch = this.peek(length);
        if (ch != ' ') {
            while ("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_".indexOf(ch) != -1) {
                ++length;
                ch = this.peek(length);
            }
            if ('!' != ch) {
                this.forward(length);
                throw new TokenizerException("While scanning a " + name + ", expected '!' but found: " + this.ch(ch));
            }
            ++length;
        }
        final String value = this.prefixForward(length);
        return value;
    }
    
    private String scanTagUri(final String name) {
        final StringBuilder chunks = new StringBuilder();
        int length;
        char ch;
        for (length = 0, ch = this.peek(length); "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-#;/?:@&=+$,_.!~*'()[]".indexOf(ch) != -1; ch = this.peek(length)) {
            if ('%' == ch) {
                chunks.append(this.prefixForward(length));
                length = 0;
                chunks.append(this.scanUriEscapes(name));
            }
            else {
                ++length;
            }
        }
        if (length != 0) {
            chunks.append(this.prefixForward(length));
        }
        if (chunks.length() == 0) {
            throw new TokenizerException("While scanning a " + name + ", expected a URI but found: " + this.ch(ch));
        }
        return chunks.toString();
    }
    
    private String scanUriEscapes(final String name) {
        final StringBuilder bytes = new StringBuilder();
        while (this.peek() == '%') {
            this.forward();
            try {
                bytes.append(Character.toChars(Integer.parseInt(this.prefix(2), 16)));
            }
            catch (NumberFormatException nfe) {
                throw new TokenizerException("While scanning a " + name + ", expected a URI escape sequence of 2 hexadecimal numbers but found: " + this.ch(this.peek(1)) + " and " + this.ch(this.peek(2)));
            }
            this.forward(2);
        }
        return bytes.toString();
    }
    
    private String scanLineBreak() {
        final char val = this.peek();
        if ("\r\n\u0085".indexOf(val) != -1) {
            if ("\r\n".equals(this.prefix(2))) {
                this.forward(2);
            }
            else {
                this.forward();
            }
            return "\n";
        }
        return "";
    }
    
    private String ch(final char ch) {
        return "'" + ch + "' (" + (int)ch + ")";
    }
    
    public static void main(final String[] args) throws Exception {
        final Iterator iter = new Tokenizer(new FileReader("test/test.yml")).iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
    }
    
    static {
        NON_PRINTABLE = Pattern.compile("[^\t\n\r -~\u0085 -\u00ff]");
        NOT_HEXA = Pattern.compile("[^0-9A-Fa-f]");
        NON_ALPHA = Pattern.compile("[^-0-9A-Za-z_]");
        R_FLOWZERO = Pattern.compile("[\u0000 \t\r\n\u0085]|(:[\u0000 \t\r\n\u0085])");
        R_FLOWNONZERO = Pattern.compile("[\u0000 \t\r\n\u0085\\[\\]{},:?]");
        END_OR_START = Pattern.compile("^(---|\\.\\.\\.)[\u0000 \t\r\n\u0085]$");
        ENDING = Pattern.compile("^---[\u0000 \t\r\n\u0085]$");
        START = Pattern.compile("^\\.\\.\\.[\u0000 \t\r\n\u0085]$");
        BEG = Pattern.compile("^([^\u0000 \t\r\n\u0085\\-?:,\\[\\]{}#&*!|>'\"%@]|([\\-?:][^\u0000 \t\r\n\u0085]))");
        ESCAPE_REPLACEMENTS = new HashMap<Character, String>();
        ESCAPE_CODES = new HashMap<Character, Integer>();
        Tokenizer.ESCAPE_REPLACEMENTS.put('0', "\u0000");
        Tokenizer.ESCAPE_REPLACEMENTS.put('a', "\u0007");
        Tokenizer.ESCAPE_REPLACEMENTS.put('b', "\b");
        Tokenizer.ESCAPE_REPLACEMENTS.put('t', "\t");
        Tokenizer.ESCAPE_REPLACEMENTS.put('\t', "\t");
        Tokenizer.ESCAPE_REPLACEMENTS.put('n', "\n");
        Tokenizer.ESCAPE_REPLACEMENTS.put('v', "\u000b");
        Tokenizer.ESCAPE_REPLACEMENTS.put('f', "\f");
        Tokenizer.ESCAPE_REPLACEMENTS.put('r', "\r");
        Tokenizer.ESCAPE_REPLACEMENTS.put('e', "\u001b");
        Tokenizer.ESCAPE_REPLACEMENTS.put(' ', " ");
        Tokenizer.ESCAPE_REPLACEMENTS.put('\"', "\"");
        Tokenizer.ESCAPE_REPLACEMENTS.put('\\', "\\");
        Tokenizer.ESCAPE_REPLACEMENTS.put('N', "\u0085");
        Tokenizer.ESCAPE_REPLACEMENTS.put('_', " ");
        Tokenizer.ESCAPE_REPLACEMENTS.put('L', "\u2028");
        Tokenizer.ESCAPE_REPLACEMENTS.put('P', "\u2029");
        Tokenizer.ESCAPE_CODES.put('x', 2);
        Tokenizer.ESCAPE_CODES.put('u', 4);
        Tokenizer.ESCAPE_CODES.put('U', 8);
    }
    
    public class TokenizerException extends RuntimeException
    {
        public TokenizerException(final String message, final Throwable cause) {
            super("Line " + Tokenizer.this.getLineNumber() + ", column " + Tokenizer.this.getColumn() + ": " + message, cause);
        }
        
        public TokenizerException(final Tokenizer this$0, final String message) {
            this(this$0, message, null);
        }
    }
    
    static class SimpleKey
    {
        public final int tokenNumber;
        public final int column;
        
        public SimpleKey(final int tokenNumber, final int column) {
            this.tokenNumber = tokenNumber;
            this.column = column;
        }
    }
}
