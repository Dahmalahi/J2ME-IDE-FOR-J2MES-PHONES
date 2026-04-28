import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

/**
 * J2MEEditor.java
 * Full IDE editor
 * Vendor: DASH ANIMATION V2
 */
public class J2MEEditor extends Canvas
        implements CommandListener {

    // --- Palette ---
    private static final int COL_BG          = 0x1E1E1E;
    private static final int COL_GUTTER      = 0x252526;
    private static final int COL_GUTTER_TXT  = 0x858585;
    private static final int COL_TEXT        = 0xD4D4D4;
    private static final int COL_KEYWORD     = 0x569CD6;
    private static final int COL_STRING      = 0xCE9178;
    private static final int COL_COMMENT     = 0x6A9955;
    private static final int COL_NUMBER      = 0xB5CEA8;
    private static final int COL_TYPE        = 0x4EC9B0;
    private static final int COL_ANNOTATION  = 0x9CDCFE;
    private static final int COL_CURSOR_LINE = 0x282828;
    private static final int COL_CURSOR      = 0xAEAFAD;
    private static final int COL_FIND_HL     = 0x613315;
    private static final int COL_ERR_LINE    = 0x3D1515;
    private static final int COL_ERR_TXT     = 0xFF6B6B;
    private static final int COL_HINT_TXT    = 0x89D185;
    private static final int COL_STATUS_BG   = 0x007ACC;
    private static final int COL_STATUS_TXT  = 0xFFFFFF;
    private static final int COL_TOOLBAR_BG  = 0x2D2D2D;
    private static final int COL_TOOLBAR_TXT = 0xCCCCCC;

    // --- Layout ---
    private Font    fontMono;
    private Font    fontSmall;
    private int     charW;
    private int     charH;
    private int     gutterW    = 36;
    private int     statusH    = 14;
    private int     toolbarH   = 15;
    private int     hintBarH   = 13;
    private boolean showToolbar = true;
    private boolean fullScreen  = false;
    private boolean hintEnabled = true;
    private boolean hintVisible = false;
    private int     visibleLines;
    private int     visibleCols;

    // --- Document ---
    private StringBuffer[] lines;
    private int            totalLines;
    private int            curRow    = 0;
    private int            curCol    = 0;
    private int            scrollRow = 0;
    private int            scrollCol = 0;

    // --- Find ---
    private String  findQuery  = "";
    private int     findRow    = -1;
    private int     findCol    = -1;
    private int     findLen    = 0;
    private boolean findActive = false;

    // --- Hints ---
    private String  currentHint = "";
    private int     errorRow    = -1;

    // FIX: Made PUBLIC so J2MEIDE.java can access it
    public static final String[][] ERROR_PATTERNS = {
        {"System.out.println",
         "Tip: Use Alert or StringItem to show output in J2ME"},
        {"System.err",
         "Tip: No System.err in CLDC; use try/catch instead"},
        {"public static void main",
         "Tip: J2ME uses startApp() not main(). Extend MIDlet"},
        {"ArrayList",
         "Tip: Use Vector instead of ArrayList in CLDC 1.1"},
        {"HashMap",
         "Tip: Use Hashtable instead of HashMap in CLDC 1.1"},
        {"LinkedList",
         "Tip: Use Vector instead of LinkedList in CLDC 1.1"},
        {"Iterator",
         "Tip: Use Enumeration instead of Iterator in CLDC 1.1"},
        {"StringBuilder",
         "Tip: Use StringBuffer instead of StringBuilder"},
        {"Integer.valueOf(",
         "Tip: Use Integer.parseInt() in CLDC 1.1"},
        {"Arrays.sort",
         "Tip: No Arrays.sort in CLDC; sort manually"},
        {"String.format",
         "Tip: String.format not in CLDC 1.1; use StringBuffer"},
        {".toString(\"",
         "Tip: ByteArrayOutputStream.toString(enc) not in CLDC"},
        {"new File(",
         "Tip: Use FileConnection (JSR-75) not java.io.File"},
        {"Scanner",
         "Tip: No Scanner in CLDC; read streams manually"},
        {"javax.swing",
         "Tip: No Swing in J2ME; use LCDUI"},
        {"java.awt",
         "Tip: No AWT in J2ME; use LCDUI"},
        {"@Override",
         "Tip: Annotations not supported in CLDC 1.1"},
        {"catch (Exception |",
         "Tip: Multi-catch not supported in CLDC 1.1"},
        {"enum ",
         "Tip: Enums not in CLDC 1.1; use int constants"},
        {"notifyDestroyed",
         "Good: notifyDestroyed() is correct MIDlet exit"},
        {"Display.getDisplay",
         "Good: Correct way to get Display instance"},
        {"HttpConnection",
         "Good: HttpConnection is JSR-36 MIDP 2.0 supported"},
        {"RecordStore",
         "Good: RecordStore (RMS) is standard in MIDP"},
        {"FileConnection",
         "Good: FileConnection requires JSR-75 permission"},
        {"Thread.sleep(",
         "Good: Thread.sleep() is supported in CLDC 1.1"},
        {"Vector",
         "Good: Vector is the correct collection for CLDC 1.1"},
        {"Hashtable",
         "Good: Hashtable is supported in CLDC 1.1"},
        {"StringBuffer",
         "Good: StringBuffer is correct for CLDC 1.1"},
        {"Enumeration",
         "Good: Enumeration is the iterator for CLDC 1.1"},
        {"setFullScreenMode",
         "Good: setFullScreenMode() supported in MIDP 2.0"},
    };

    // --- Keywords ---
    private static final String[] KEYWORDS = {
        "abstract","boolean","break","byte","case",
        "catch","char","class","continue","default",
        "do","double","else","extends","final",
        "finally","float","for","if","implements",
        "import","instanceof","int","interface","long",
        "native","new","null","package","private",
        "protected","public","return","short","static",
        "super","switch","synchronized","this","throw",
        "throws","transient","true","false","try",
        "void","volatile","while"
    };

    // --- J2ME Types ---
    private static final String[] J2ME_TYPES = {
        "MIDlet","Display","Form","Canvas","Command",
        "Displayable","Alert","AlertType","List",
        "TextBox","TextField","StringItem","Gauge",
        "Image","Graphics","Font","Timer","TimerTask",
        "Thread","Runnable","String","StringBuffer",
        "Vector","Hashtable","Enumeration",
        "InputStream","OutputStream",
        "ByteArrayOutputStream","HttpConnection",
        "RecordStore","Sprite","LayerManager","Player",
        "Manager","Item","CustomItem","ChoiceGroup",
        "DateField","FileConnection","Connector",
        "CommandListener","ItemCommandListener",
        "Exception","Object","Integer","Boolean",
        "Character","Long","Math","Random",
        "System","Runtime"
    };

    // --- Meta ---
    private String      fileName = "untitled.java";
    private boolean     modified = false;
    private MIDlet      midlet;
    private Display     display;
    private FileManager fm;

    // --- Commands ---
    private Command cmdSave;
    private Command cmdBack;
    private Command cmdSnippet;
    private Command cmdGoto;
    private Command cmdFind;
    private Command cmdFindNext;
    private Command cmdFullScreen;
    private Command cmdDupLine;
    private Command cmdWordCount;
    private Command cmdNewLine;
    private Command cmdTextBoxMode;
    private Command cmdToggleHint;
    private Command cmdHome;
    private Command cmdEnd;
    private Command cmdSaveSnippet;

    // =============================================
    // Constructor
    // =============================================

    public J2MEEditor(MIDlet midlet,
                      Display display,
                      FileManager fm) {
        this.midlet  = midlet;
        this.display = display;
        this.fm      = fm;

        setFullScreenMode(true);

        fontMono  = Font.getFont(Font.FACE_MONOSPACE,
            Font.STYLE_PLAIN, Font.SIZE_SMALL);
        fontSmall = Font.getFont(Font.FACE_SYSTEM,
            Font.STYLE_PLAIN, Font.SIZE_SMALL);

        charW = fontMono.charWidth('W');
        charH = fontMono.getHeight();

        recalcLayout();
        initDoc("");
        buildCommands();
    }

    private void buildCommands() {
        cmdSave        = new Command("Save",
            Command.OK,   1);
        cmdBack        = new Command("Back",
            Command.BACK, 2);
        cmdSnippet     = new Command("Snippet",
            Command.ITEM, 3);
        cmdGoto        = new Command("Go to Line",
            Command.ITEM, 4);
        cmdFind        = new Command("Find",
            Command.ITEM, 5);
        cmdFindNext    = new Command("Find Next",
            Command.ITEM, 6);
        cmdFullScreen  = new Command("Toggle Toolbar",
            Command.ITEM, 7);
        cmdDupLine     = new Command("Duplicate Line",
            Command.ITEM, 8);
        cmdWordCount   = new Command("Word Count",
            Command.ITEM, 9);
        cmdNewLine     = new Command("New Line",
            Command.ITEM, 10);
        cmdTextBoxMode = new Command("Edit in TextBox",
            Command.ITEM, 11);
        cmdToggleHint  = new Command("Toggle Hints",
            Command.ITEM, 12);
        cmdHome        = new Command("Line Start",
            Command.ITEM, 13);
        cmdEnd         = new Command("Line End",
            Command.ITEM, 14);
        cmdSaveSnippet = new Command(
            "Save as Snippet", Command.ITEM, 15);

        addCommand(cmdSave);
        addCommand(cmdBack);
        addCommand(cmdSnippet);
        addCommand(cmdGoto);
        addCommand(cmdFind);
        addCommand(cmdFindNext);
        addCommand(cmdFullScreen);
        addCommand(cmdDupLine);
        addCommand(cmdWordCount);
        addCommand(cmdNewLine);
        addCommand(cmdTextBoxMode);
        addCommand(cmdToggleHint);
        addCommand(cmdHome);
        addCommand(cmdEnd);
        addCommand(cmdSaveSnippet);
        setCommandListener(this);
    }

    // =============================================
    // Public API
    // =============================================

    public void loadCode(String code, String fname) {
        this.fileName = fname;
        initDoc(code == null ? "" : code);
        modified    = false;
        findActive  = false;
        errorRow    = -1;
        currentHint = "";
        hintVisible = false;
        repaint();
    }

    public String getCode() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < totalLines; i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines[i].toString());
        }
        return sb.toString();
    }

    public void insertSnippetAtCursor(String snippet) {
        if (snippet == null) return;
        insertText(snippet);
        repaint();
    }

    public void setFileName(String n)  { fileName = n; }
    public String getFileName()        { return fileName; }
    public boolean isModified()        { return modified; }

    public void setHintEnabled(boolean e) {
        hintEnabled = e;
        if (!e) {
            hintVisible = false;
            errorRow    = -1;
            currentHint = "";
        }
    }

    public boolean isHintEnabled() { return hintEnabled; }

    // =============================================
    // Layout
    // =============================================

    private void recalcLayout() {
        int w    = getWidth();
        int h    = getHeight();
        int usedH = statusH;
        if (showToolbar && !fullScreen)
            usedH += toolbarH;
        if (hintEnabled && hintVisible && !fullScreen)
            usedH += hintBarH;
        int gw = fullScreen ? 0 : gutterW;
        visibleLines = (h - usedH) / charH;
        visibleCols  = (w - gw)    / charW;
        if (visibleLines < 1) visibleLines = 1;
        if (visibleCols  < 1) visibleCols  = 1;
    }

    // =============================================
    // Document
    // =============================================

    private void initDoc(String code) {
        int count = 1;
        for (int i = 0; i < code.length(); i++) {
            if (code.charAt(i) == '\n') count++;
        }
        int cap = Math.max(count + 16, 64);
        lines      = new StringBuffer[cap];
        totalLines = 0;
        int start  = 0;
        for (int i = 0; i <= code.length(); i++) {
            if (i == code.length() ||
                code.charAt(i) == '\n') {
                ensureCapacity();
                lines[totalLines++] =
                    new StringBuffer(
                        code.substring(start, i));
                start = i + 1;
            }
        }
        if (totalLines == 0) {
            lines[0]   = new StringBuffer();
            totalLines = 1;
        }
        curRow = curCol = scrollRow = scrollCol = 0;
    }

    private void ensureCapacity() {
        if (totalLines >= lines.length) {
            StringBuffer[] b =
                new StringBuffer[lines.length * 2];
            System.arraycopy(lines, 0, b, 0,
                lines.length);
            lines = b;
        }
    }

    // =============================================
    // Text editing
    // =============================================

    private void insertText(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') insertNewLine();
            else           insertChar(c);
        }
    }

    private void insertChar(char c) {
        lines[curRow].insert(curCol, c);
        curCol++;
        modified = true;
        checkHintForCurrentLine();
    }

    private void insertNewLine() {
        String tail =
            lines[curRow].toString()
                .substring(curCol);
        lines[curRow].delete(
            curCol, lines[curRow].length());

        ensureCapacity();
        for (int i = totalLines; i > curRow + 1; i--) {
            lines[i] = lines[i - 1];
        }
        totalLines++;
        curRow++;

        StringBuffer prev   = lines[curRow - 1];
        StringBuffer newLn  = new StringBuffer();
        int          indent = 0;
        while (indent < prev.length() &&
               (prev.charAt(indent) == ' ' ||
                prev.charAt(indent) == '\t')) {
            indent++;
        }
        if (prev.length() > 0 &&
            prev.charAt(prev.length() - 1) == '{') {
            indent += 4;
            // Auto-insert closing brace
            ensureCapacity();
            for (int i = totalLines;
                 i > curRow + 1; i--) {
                lines[i] = lines[i - 1];
            }
            int closeIndent = indent - 4;
            if (closeIndent < 0) closeIndent = 0;
            StringBuffer cl = new StringBuffer();
            for (int s = 0; s < closeIndent; s++)
                cl.append(' ');
            cl.append('}');
            lines[curRow + 1] = cl;
            totalLines++;
        }
        for (int s = 0; s < indent; s++)
            newLn.append(' ');
        newLn.append(tail);
        lines[curRow] = newLn;
        curCol        = indent;
        modified      = true;
        checkHintForCurrentLine();
    }

    private void deleteCharBack() {
        if (curCol > 0) {
            lines[curRow].deleteCharAt(curCol - 1);
            curCol--;
            modified = true;
            checkHintForCurrentLine();
        } else if (curRow > 0) {
            String tail = lines[curRow].toString();
            curRow--;
            curCol = lines[curRow].length();
            lines[curRow].append(tail);
            for (int i = curRow + 1;
                 i < totalLines - 1; i++) {
                lines[i] = lines[i + 1];
            }
            totalLines--;
            modified = true;
        }
    }

    private void duplicateLine() {
        String text = lines[curRow].toString();
        ensureCapacity();
        for (int i = totalLines; i > curRow + 1; i--) {
            lines[i] = lines[i - 1];
        }
        totalLines++;
        lines[curRow + 1] = new StringBuffer(text);
        curRow++;
        modified = true;
    }

    // =============================================
    // HINT / ERROR SYSTEM
    // =============================================

    private void checkHintForCurrentLine() {
        if (!hintEnabled) return;
        String line  = lines[curRow].toString();
        String lower = line.toLowerCase();
        currentHint = "";
        errorRow    = -1;
        hintVisible = false;

        for (int i = 0;
             i < ERROR_PATTERNS.length; i++) {
            String pattern =
                ERROR_PATTERNS[i][0].toLowerCase();
            String hint = ERROR_PATTERNS[i][1];
            if (lower.indexOf(pattern) != -1) {
                currentHint = hint;
                hintVisible = true;
                errorRow = hint.startsWith("Tip:")
                    ? curRow : -1;
                break;
            }
        }
        recalcLayout();
    }

    public String analyzeDocument() {
        StringBuffer sb = new StringBuffer();
        sb.append("=== Code Analysis ===\n\n");
        int issues = 0;
        for (int r = 0; r < totalLines; r++) {
            String line  = lines[r].toString();
            String lower = line.toLowerCase();
            for (int p = 0;
                 p < ERROR_PATTERNS.length; p++) {
                String pattern =
                    ERROR_PATTERNS[p][0].toLowerCase();
                String hint = ERROR_PATTERNS[p][1];
                if (lower.indexOf(pattern) != -1 &&
                    hint.startsWith("Tip:")) {
                    sb.append("Ln ");
                    sb.append(r + 1);
                    sb.append(": ");
                    sb.append(hint.substring(5));
                    sb.append("\n\n");
                    issues++;
                }
            }
        }
        if (issues == 0) {
            sb.append("No issues found!\n");
            sb.append("Code looks J2ME-compatible.\n");
        } else {
            sb.append("--- ");
            sb.append(issues);
            sb.append(" issue(s) found ---\n");
        }
        return sb.toString();
    }

    // =============================================
    // Find
    // =============================================

    public void findNext() {
        if (findQuery == null ||
            findQuery.length() == 0) return;
        String lower   = findQuery.toLowerCase();
        int    startRow =
            findActive ? findRow : curRow;
        int    startCol =
            findActive ? findCol + 1 : curCol;

        for (int r = startRow; r < totalLines; r++) {
            String line =
                lines[r].toString().toLowerCase();
            int from = (r == startRow) ?
                startCol : 0;
            int pos  = line.indexOf(lower, from);
            if (pos != -1) {
                findRow    = r;
                findCol    = pos;
                findLen    = findQuery.length();
                findActive = true;
                curRow     = r;
                curCol     = pos;
                scrollToCursor();
                repaint();
                return;
            }
        }
        // Wrap around
        for (int r = 0; r < startRow; r++) {
            String line =
                lines[r].toString().toLowerCase();
            int pos = line.indexOf(lower);
            if (pos != -1) {
                findRow    = r;
                findCol    = pos;
                findLen    = findQuery.length();
                findActive = true;
                curRow     = r;
                curCol     = pos;
                scrollToCursor();
                repaint();
                return;
            }
        }
        findActive = false;
    }

    // =============================================
    // TextBox mode (Key 5)
    // =============================================

    private void activateTextBoxMode() {
        String currentCode = getCode();
        final TextBox tb = new TextBox(
            "Edit - " + fileName,
            currentCode,
            32000,
            TextField.ANY);

        Command done     = new Command("Done",
            Command.OK,   1);
        Command cancel   = new Command("Cancel",
            Command.BACK, 2);
        Command analyze  = new Command("Analyze",
            Command.ITEM, 3);
        Command clear    = new Command("Clear All",
            Command.ITEM, 4);
        Command template = new Command(
            "Insert Template", Command.ITEM, 5);

        tb.addCommand(done);
        tb.addCommand(cancel);
        tb.addCommand(analyze);
        tb.addCommand(clear);
        tb.addCommand(template);

        tb.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                if (c.getCommandType() ==
                        Command.OK) {
                    String newCode = tb.getString();
                    initDoc(newCode);
                    modified = true;
                    checkHintForCurrentLine();
                    display.setCurrent(
                        J2MEEditor.this);
                } else if (c.getLabel().equals(
                        "Analyze")) {
                    String result = analyzeDocument();
                    Alert a = new Alert(
                        "Analysis", result,
                        null, AlertType.INFO);
                    a.setTimeout(Alert.FOREVER);
                    display.setCurrent(a, tb);
                } else if (c.getLabel().equals(
                        "Clear All")) {
                    tb.setString("");
                } else if (c.getLabel().equals(
                        "Insert Template")) {
                    showTemplatePickerFor(tb);
                } else {
                    display.setCurrent(
                        J2MEEditor.this);
                }
            }
        });
        display.setCurrent(tb);
    }

    private void showTemplatePickerFor(
            final TextBox tb) {
        final List picker = new List(
            "Insert Template", List.IMPLICIT);
        picker.append("Hello MIDlet",    null);
        picker.append("Canvas skeleton", null);
        picker.append("HTTP snippet",    null);
        picker.append("RMS snippet",     null);
        picker.append("Thread snippet",  null);
        picker.append("Game loop",       null);
        picker.append("Try/catch block", null);
        picker.append("For loop",        null);
        picker.append("If/else block",   null);
        picker.append("Switch block",    null);

        Command ok   = new Command("Insert",
            Command.OK,   1);
        Command back = new Command("Back",
            Command.BACK, 2);
        picker.addCommand(ok);
        picker.addCommand(back);

        picker.setCommandListener(
            new CommandListener() {
                public void commandAction(
                        Command c, Displayable d) {
                    if (c.getCommandType() ==
                            Command.OK) {
                        int idx =
                            picker.getSelectedIndex();
                        String tpl =
                            getInlineTemplate(idx);
                        String cur = tb.getString();
                        tb.setString(cur + "\n" + tpl);
                    }
                    display.setCurrent(tb);
                }
            });
        display.setCurrent(picker);
    }

    private String getInlineTemplate(int idx) {
        switch (idx) {
            case 0:
                return
                    "public class MyMIDlet\n" +
                    "        extends MIDlet\n" +
                    "        implements CommandListener {\n" +
                    "    private Display display;\n" +
                    "    public void startApp() {\n" +
                    "        display =\n" +
                    "            Display.getDisplay(this);\n" +
                    "    }\n" +
                    "    public void pauseApp() {}\n" +
                    "    public void destroyApp(\n" +
                    "            boolean u) {}\n" +
                    "    public void commandAction(\n" +
                    "            Command c,\n" +
                    "            Displayable d) {}\n" +
                    "}\n";
            case 1:
                return
                    "class MyCanvas extends Canvas\n" +
                    "        implements Runnable {\n" +
                    "    private boolean running = true;\n" +
                    "    public void run() {\n" +
                    "        while (running) {\n" +
                    "            repaint();\n" +
                    "            serviceRepaints();\n" +
                    "            try {\n" +
                    "                Thread.sleep(33);\n" +
                    "            } catch (Exception e) {}\n" +
                    "        }\n" +
                    "    }\n" +
                    "    protected void paint(\n" +
                    "            Graphics g) {\n" +
                    "        g.setColor(0x000000);\n" +
                    "        g.fillRect(0,0,\n" +
                    "            getWidth(),\n" +
                    "            getHeight());\n" +
                    "    }\n" +
                    "    protected void keyPressed(\n" +
                    "            int k) {}\n" +
                    "}\n";
            case 2:
                return
                    "HttpConnection hc = null;\n" +
                    "InputStream is = null;\n" +
                    "try {\n" +
                    "    hc = (HttpConnection)\n" +
                    "        Connector.open(\n" +
                    "        \"http://url\");\n" +
                    "    hc.setRequestMethod(\n" +
                    "        HttpConnection.GET);\n" +
                    "    int code =\n" +
                    "        hc.getResponseCode();\n" +
                    "    is = hc.openInputStream();\n" +
                    "} catch (Exception e) {\n" +
                    "} finally {\n" +
                    "    try{if(is!=null)\n" +
                    "        is.close();}\n" +
                    "    catch(Exception ig){}\n" +
                    "    try{if(hc!=null)\n" +
                    "        hc.close();}\n" +
                    "    catch(Exception ig){}\n" +
                    "}\n";
            case 3:
                return
                    "RecordStore rs = null;\n" +
                    "try {\n" +
                    "    rs = RecordStore\n" +
                    "        .openRecordStore(\n" +
                    "        \"store\", true);\n" +
                    "    byte[] d =\n" +
                    "        \"value\".getBytes();\n" +
                    "    if (rs.getNumRecords()==0)\n" +
                    "        rs.addRecord(\n" +
                    "            d,0,d.length);\n" +
                    "    else\n" +
                    "        rs.setRecord(\n" +
                    "            1,d,0,d.length);\n" +
                    "} catch (Exception e) {\n" +
                    "} finally {\n" +
                    "    try{if(rs!=null)\n" +
                    "        rs.closeRecordStore();}\n" +
                    "    catch(Exception ig){}\n" +
                    "}\n";
            case 4:
                return
                    "new Thread(new Runnable() {\n" +
                    "    public void run() {\n" +
                    "        // background task\n" +
                    "    }\n" +
                    "}).start();\n";
            case 5:
                return
                    "private boolean running = true;\n" +
                    "new Thread(new Runnable() {\n" +
                    "    public void run() {\n" +
                    "        long prev =\n" +
                    "            System\n" +
                    "            .currentTimeMillis();\n" +
                    "        while (running) {\n" +
                    "            long now =\n" +
                    "                System\n" +
                    "                .currentTimeMillis();\n" +
                    "            long dt = now - prev;\n" +
                    "            prev = now;\n" +
                    "            update(dt);\n" +
                    "            repaint();\n" +
                    "            serviceRepaints();\n" +
                    "            try {\n" +
                    "                Thread.sleep(\n" +
                    "                    Math.max(\n" +
                    "                    0, 33-dt));\n" +
                    "            } catch(Exception e){}\n" +
                    "        }\n" +
                    "    }\n" +
                    "}).start();\n";
            case 6:
                return
                    "try {\n" +
                    "    // your code\n" +
                    "} catch (Exception e) {\n" +
                    "    e.printStackTrace();\n" +
                    "} finally {\n" +
                    "    // cleanup\n" +
                    "}\n";
            case 7:
                return
                    "for (int i = 0; i < n; i++) {\n" +
                    "    // loop body\n" +
                    "}\n";
            case 8:
                return
                    "if (condition) {\n" +
                    "    // true branch\n" +
                    "} else {\n" +
                    "    // false branch\n" +
                    "}\n";
            case 9:
                return
                    "switch (value) {\n" +
                    "    case 0:\n" +
                    "        break;\n" +
                    "    case 1:\n" +
                    "        break;\n" +
                    "    default:\n" +
                    "        break;\n" +
                    "}\n";
            default:
                return "// template\n";
        }
    }

    // =============================================
    // Key handling
    // =============================================

    protected void keyPressed(int keyCode) {
        if (keyCode == Canvas.KEY_NUM5) {
            activateTextBoxMode();
            return;
        }
        if (keyCode == Canvas.KEY_NUM1) {
            curCol = 0;
            scrollToCursor();
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_NUM3) {
            curCol = lines[curRow].length();
            scrollToCursor();
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_NUM7) {
            scrollRow = Math.max(0,
                scrollRow - visibleLines);
            curRow = scrollRow;
            curCol = Math.min(curCol,
                lines[curRow].length());
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_NUM9) {
            scrollRow = Math.min(
                Math.max(0, totalLines - visibleLines),
                scrollRow + visibleLines);
            curRow = Math.min(totalLines - 1,
                scrollRow + visibleLines - 1);
            curCol = Math.min(curCol,
                lines[curRow].length());
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_NUM0) {
            insertNewLine();
            scrollToCursor();
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_NUM2) {
            moveLineUp();
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_NUM8) {
            moveLineDown();
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_STAR) {
            hintEnabled = !hintEnabled;
            if (!hintEnabled) {
                hintVisible = false;
                errorRow    = -1;
                currentHint = "";
            }
            recalcLayout();
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_POUND) {
            findNext();
            return;
        }

        int ga = getGameAction(keyCode);
        switch (ga) {
            case UP:    moveCursorUp();    break;
            case DOWN:  moveCursorDown();  break;
            case LEFT:  moveCursorLeft();  break;
            case RIGHT: moveCursorRight(); break;
            default:    handleCharKey(keyCode); break;
        }
        scrollToCursor();
        repaint();
    }

    protected void keyRepeated(int keyCode) {
        keyPressed(keyCode);
    }

    private void handleCharKey(int keyCode) {
        if (keyCode == -8 || keyCode == 8) {
            deleteCharBack();
            return;
        }
        if (keyCode == -5 || keyCode == 10 ||
            keyCode == 13) {
            insertNewLine();
            return;
        }
        if (keyCode >= 32 && keyCode <= 126) {
            insertChar((char) keyCode);
        }
    }

    private void moveLineUp() {
        if (curRow <= 0) return;
        StringBuffer tmp  = lines[curRow];
        lines[curRow]     = lines[curRow - 1];
        lines[curRow - 1] = tmp;
        curRow--;
        modified = true;
    }

    private void moveLineDown() {
        if (curRow >= totalLines - 1) return;
        StringBuffer tmp  = lines[curRow];
        lines[curRow]     = lines[curRow + 1];
        lines[curRow + 1] = tmp;
        curRow++;
        modified = true;
    }

    private void moveCursorUp() {
        if (curRow > 0) {
            curRow--;
            int len = lines[curRow].length();
            if (curCol > len) curCol = len;
            checkHintForCurrentLine();
        }
    }

    private void moveCursorDown() {
        if (curRow < totalLines - 1) {
            curRow++;
            int len = lines[curRow].length();
            if (curCol > len) curCol = len;
            checkHintForCurrentLine();
        }
    }

    private void moveCursorLeft() {
        if (curCol > 0) {
            curCol--;
        } else if (curRow > 0) {
            curRow--;
            curCol = lines[curRow].length();
        }
    }

    private void moveCursorRight() {
        if (curCol < lines[curRow].length()) {
            curCol++;
        } else if (curRow < totalLines - 1) {
            curRow++;
            curCol = 0;
        }
    }

    private void scrollToCursor() {
        if (curRow < scrollRow)
            scrollRow = curRow;
        else if (curRow >= scrollRow + visibleLines)
            scrollRow = curRow - visibleLines + 1;
        if (curCol < scrollCol)
            scrollCol = curCol;
        else if (curCol >= scrollCol + visibleCols)
            scrollCol = curCol - visibleCols + 1;
    }

    // =============================================
    // Word Count
    // =============================================

    private void showWordCount() {
        String code  = getCode();
        int    chars = code.length();
        int    words = 0;
        boolean inW  = false;
        for (int i = 0; i < code.length(); i++) {
            char c  = code.charAt(i);
            boolean sp = (c==' '||c=='\n'||
                          c=='\t'||c=='\r');
            if (!sp && !inW) { words++; inW=true; }
            else if (sp)       inW = false;
        }
        Alert a = new Alert("Document Info",
            "File    : " + fileName + "\n" +
            "Lines   : " + totalLines + "\n" +
            "Words   : " + words + "\n" +
            "Chars   : " + chars + "\n" +
            "Cur Line: " + (curRow+1) + "\n" +
            "Cur Col : " + (curCol+1) + "\n" +
            "Modified: " + (modified?"Yes":"No") +
            "\n\nKey shortcuts:\n" +
            "5=TextBox  0=NewLine\n" +
            "1=Home     3=End\n" +
            "7=PageUp   9=PageDown\n" +
            "2=LineUp   8=LineDown\n" +
            "*=Hints    #=FindNext",
            null, AlertType.INFO);
        a.setTimeout(Alert.FOREVER);
        display.setCurrent(a, this);
    }

    // =============================================
    // Rendering
    // =============================================

    protected void paint(Graphics g) {
        recalcLayout();
        int w      = getWidth();
        int h      = getHeight();
        int gw     = fullScreen ? 0 : gutterW;
        int topOff = 0;

        if (showToolbar && !fullScreen) {
            drawToolbar(g, w);
            topOff = toolbarH;
        }

        if (hintEnabled && hintVisible &&
            !fullScreen && currentHint.length() > 0) {
            drawHintBar(g, w, topOff);
            topOff += hintBarH;
        }

        g.setColor(COL_BG);
        g.fillRect(0, topOff, w,
            h - statusH - topOff);

        if (!fullScreen) {
            g.setColor(COL_GUTTER);
            g.fillRect(0, topOff, gw,
                h - statusH - topOff);
        }

        g.setFont(fontMono);

        for (int i = 0; i < visibleLines; i++) {
            int docRow = scrollRow + i;
            if (docRow >= totalLines) break;
            int y = topOff + i * charH;

            if (hintEnabled && docRow == errorRow) {
                g.setColor(COL_ERR_LINE);
                g.fillRect(gw, y, w-gw, charH);
            } else if (docRow == curRow) {
                g.setColor(COL_CURSOR_LINE);
                g.fillRect(gw, y, w-gw, charH);
            }

            if (findActive && docRow == findRow) {
                int fx = gw +
                    (findCol - scrollCol) * charW;
                int fw = findLen * charW;
                if (fx >= gw && fx < w) {
                    g.setColor(COL_FIND_HL);
                    g.fillRect(fx, y,
                        Math.min(fw, w-fx), charH);
                }
            }

            if (!fullScreen) {
                if (hintEnabled &&
                    docRow == errorRow) {
                    g.setColor(COL_ERR_TXT);
                    g.drawChar('!', 2, y,
                        Graphics.TOP|Graphics.LEFT);
                }
                g.setColor(
                    (hintEnabled && docRow==errorRow)
                    ? COL_ERR_TXT : COL_GUTTER_TXT);
                String ln = String.valueOf(docRow+1);
                int lnX = gw -
                    fontMono.stringWidth(ln) - 4;
                g.drawString(ln, lnX, y,
                    Graphics.TOP|Graphics.LEFT);
            }

            drawSyntaxLine(g,
                lines[docRow].toString(), y, gw);

            if (docRow == curRow) {
                int cx = gw +
                    (curCol - scrollCol) * charW;
                g.setColor(COL_CURSOR);
                g.drawLine(cx, y, cx, y+charH-1);
            }
        }

        drawStatusBar(g, w, h);
    }

    private void drawToolbar(Graphics g, int w) {
        g.setColor(COL_TOOLBAR_BG);
        g.fillRect(0, 0, w, toolbarH);
        g.setColor(0x007ACC);
        g.fillRect(0, toolbarH-1, w, 1);
        g.setColor(COL_TOOLBAR_TXT);
        g.setFont(fontSmall);
        String info =
            " " + fileName +
            (modified ? "*" : " ") +
            " Ln:" + (curRow+1) +
            " Col:" + (curCol+1) +
            " [5=TB]";
        g.drawString(info, 0, 0,
            Graphics.TOP|Graphics.LEFT);
    }

    private void drawHintBar(Graphics g,
                              int w, int yOff) {
        boolean isErr = currentHint.startsWith("Tip:");
        g.setColor(isErr ? 0x3D1515 : 0x1A2E1A);
        g.fillRect(0, yOff, w, hintBarH);
        g.setColor(isErr ? COL_ERR_TXT : COL_HINT_TXT);
        g.fillRect(0, yOff, 2, hintBarH);
        g.setFont(fontSmall);
        String hint = currentHint;
        int    maxW = w - 6;
        while (hint.length() > 4 &&
               fontSmall.stringWidth(hint) > maxW) {
            hint = hint.substring(
                0, hint.length()-4) + "...";
        }
        g.drawString(hint, 4, yOff,
            Graphics.TOP|Graphics.LEFT);
    }

    private void drawSyntaxLine(Graphics g,
                                 String text,
                                 int y, int gw) {
        int x   = gw;
        int len = text.length();
        int i   = scrollCol;

        while (i < len) {
            char c = text.charAt(i);

            if (c == '@') {
                int end = i + 1;
                while (end < len &&
                    isLetterOrDigit(
                        text.charAt(end))) end++;
                String w = text.substring(i, end);
                g.setColor(COL_ANNOTATION);
                g.drawString(w, x, y,
                    Graphics.TOP|Graphics.LEFT);
                x += fontMono.stringWidth(w);
                i  = end;
                continue;
            }

            if (c=='/' && i+1<len &&
                text.charAt(i+1)=='/') {
                g.setColor(COL_COMMENT);
                g.drawString(text.substring(i),
                    x, y,
                    Graphics.TOP|Graphics.LEFT);
                break;
            }

            if (c=='/' && i+1<len &&
                text.charAt(i+1)=='*') {
                g.setColor(COL_COMMENT);
                int end = text.indexOf("*/", i+2);
                String blk = (end==-1) ?
                    text.substring(i) :
                    text.substring(i, end+2);
                g.drawString(blk, x, y,
                    Graphics.TOP|Graphics.LEFT);
                x += fontMono.stringWidth(blk);
                i  = (end==-1) ? len : end+2;
                continue;
            }

            if (c == '"') {
                int end = i + 1;
                while (end < len) {
                    if (text.charAt(end)=='\\') {
                        end+=2; continue;
                    }
                    if (text.charAt(end)=='"') {
                        end++; break;
                    }
                    end++;
                }
                String s = text.substring(i, end);
                g.setColor(COL_STRING);
                g.drawString(s, x, y,
                    Graphics.TOP|Graphics.LEFT);
                x += fontMono.stringWidth(s);
                i  = end;
                continue;
            }

            if (c == '\'') {
                int end = i + 1;
                while (end < len &&
                    text.charAt(end)!='\'') end++;
                if (end < len) end++;
                String s = text.substring(i, end);
                g.setColor(COL_STRING);
                g.drawString(s, x, y,
                    Graphics.TOP|Graphics.LEFT);
                x += fontMono.stringWidth(s);
                i  = end;
                continue;
            }

            if (c >= '0' && c <= '9') {
                int end = i;
                while (end < len &&
                    isNumChar(text.charAt(end))) end++;
                String num = text.substring(i, end);
                g.setColor(COL_NUMBER);
                g.drawString(num, x, y,
                    Graphics.TOP|Graphics.LEFT);
                x += fontMono.stringWidth(num);
                i  = end;
                continue;
            }

            if (isLetter(c)) {
                int end = i;
                while (end < len &&
                    isLetterOrDigit(
                        text.charAt(end))) end++;
                String word = text.substring(i, end);
                if (isKeyword(word)) {
                    g.setColor(COL_KEYWORD);
                } else if (isJ2meType(word)) {
                    g.setColor(COL_TYPE);
                } else {
                    g.setColor(COL_TEXT);
                }
                g.drawString(word, x, y,
                    Graphics.TOP|Graphics.LEFT);
                x += fontMono.stringWidth(word);
                i  = end;
                continue;
            }

            g.setColor(COL_TEXT);
            g.drawChar(c, x, y,
                Graphics.TOP|Graphics.LEFT);
            x += charW;
            i++;
        }
    }

    private void drawStatusBar(Graphics g,
                                int w, int h) {
        int barY = h - statusH;
        g.setColor(COL_STATUS_BG);
        g.fillRect(0, barY, w, statusH);
        g.setColor(COL_STATUS_TXT);
        g.setFont(fontSmall);
        String hm = hintEnabled ? "H+" : "H-";
        String status =
            " " + (modified ? "* " : "") +
            fileName +
            " |" + (curRow+1) + ":" + (curCol+1) +
            " |" + hm +
            (fullScreen ? "[F]" : "") +
            " |DASH V2";
        g.drawString(status, 0, barY,
            Graphics.TOP|Graphics.LEFT);
    }

    // =============================================
    // Char helpers
    // =============================================

    private boolean isLetter(char c) {
        return (c>='a'&&c<='z')||
               (c>='A'&&c<='Z')||c=='_';
    }

    private boolean isLetterOrDigit(char c) {
        return isLetter(c)||(c>='0'&&c<='9');
    }

    private boolean isNumChar(char c) {
        return (c>='0'&&c<='9')||
               c=='.'||c=='x'||c=='X'||
               c=='L'||c=='f'||c=='F';
    }

    private boolean isKeyword(String w) {
        for (int k = 0; k < KEYWORDS.length; k++) {
            if (KEYWORDS[k].equals(w)) return true;
        }
        return false;
    }

    private boolean isJ2meType(String w) {
        for (int k = 0; k < J2ME_TYPES.length; k++) {
            if (J2ME_TYPES[k].equals(w)) return true;
        }
        return false;
    }

    // =============================================
    // CommandListener
    // =============================================

    public void commandAction(Command c,
                               Displayable d) {
        if (c == cmdSave) {
            doSave();
        } else if (c == cmdBack) {
            if (modified) {
                showUnsavedDialog();
            } else {
                goBack();
            }
        } else if (c == cmdSnippet) {
            if (midlet instanceof J2MEIDE) {
                ((J2MEIDE) midlet)
                    .showSnippetMenu(true);
            }
        } else if (c == cmdGoto) {
            showGotoLineDialog();
        } else if (c == cmdFind) {
            showFindDialog();
        } else if (c == cmdFindNext) {
            findNext();
        } else if (c == cmdFullScreen) {
            fullScreen = !fullScreen;
            recalcLayout();
            repaint();
        } else if (c == cmdDupLine) {
            duplicateLine();
            repaint();
        } else if (c == cmdWordCount) {
            showWordCount();
        } else if (c == cmdNewLine) {
            insertNewLine();
            scrollToCursor();
            repaint();
        } else if (c == cmdTextBoxMode) {
            activateTextBoxMode();
        } else if (c == cmdToggleHint) {
            hintEnabled = !hintEnabled;
            if (!hintEnabled) {
                hintVisible = false;
                errorRow    = -1;
                currentHint = "";
            }
            recalcLayout();
            repaint();
        } else if (c == cmdHome) {
            curCol = 0;
            scrollToCursor();
            repaint();
        } else if (c == cmdEnd) {
            curCol = lines[curRow].length();
            scrollToCursor();
            repaint();
        } else if (c == cmdSaveSnippet) {
            showSaveAsSnippetDialog();
        }
    }

    private void goBack() {
        if (midlet instanceof J2MEIDE) {
            ((J2MEIDE) midlet).showMainMenu();
        }
    }

    private void showUnsavedDialog() {
        Alert a = new Alert("Unsaved Changes",
            "You have unsaved changes.\n" +
            "Save before leaving?",
            null, AlertType.WARNING);
        a.setTimeout(Alert.FOREVER);
        Command yes  = new Command("Save & Exit",
            Command.OK,   1);
        Command no   = new Command("Discard",
            Command.ITEM, 2);
        Command stay = new Command("Stay",
            Command.BACK, 3);
        a.addCommand(yes);
        a.addCommand(no);
        a.addCommand(stay);
        a.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                if (c.getLabel()
                        .equals("Save & Exit")) {
                    doSave();
                    goBack();
                } else if (c.getLabel()
                        .equals("Discard")) {
                    modified = false;
                    goBack();
                } else {
                    display.setCurrent(
                        J2MEEditor.this);
                }
            }
        });
        display.setCurrent(a);
    }

    private void doSave() {
        if (fm == null) return;
        String code = getCode();
        // FIX: Use replaceToken instead of
        // String.replace(String,String)
        // which is not available in CLDC 1.1
        String projName =
            fm.replaceToken(fileName, ".java", "");
        String base = fm.getProjectBasePath();
        if (base == null) {
            showSaveAlert(false);
            return;
        }
        String  path =
            base + projName + "/src/" + fileName;
        boolean ok   = fm.writeFile(path, code);
        if (ok) modified = false;
        showSaveAlert(ok);
    }

    private void showSaveAlert(boolean ok) {
        Alert a = new Alert(
            ok ? "Saved" : "Error",
            ok ? "Saved: " + fileName :
                 "Save failed. Check JSR-75.",
            null,
            ok ? AlertType.CONFIRMATION :
                 AlertType.ERROR);
        a.setTimeout(1800);
        display.setCurrent(a, this);
    }

    private void showGotoLineDialog() {
        final TextField tf = new TextField(
            "Line number:", "", 6,
            TextField.NUMERIC);
        final Form form = new Form("Go to Line");
        form.append(tf);
        form.append(new StringItem(
            "Total lines:", " " + totalLines + "\n"));
        Command ok   = new Command("Go",
            Command.OK,   1);
        Command back = new Command("Back",
            Command.BACK, 2);
        form.addCommand(ok);
        form.addCommand(back);
        form.setCommandListener(
            new CommandListener() {
                public void commandAction(
                        Command c, Displayable d) {
                    if (c.getCommandType() ==
                            Command.OK) {
                        try {
                            int ln = Integer.parseInt(
                                tf.getString().trim())
                                - 1;
                            if (ln >= 0 &&
                                ln < totalLines) {
                                curRow = ln;
                                curCol = 0;
                                scrollToCursor();
                            }
                        } catch (
                            NumberFormatException ex){}
                    }
                    display.setCurrent(
                        J2MEEditor.this);
                }
            });
        display.setCurrent(form);
    }

    private void showFindDialog() {
        final TextField tf = new TextField(
            "Find:", findQuery, 64, TextField.ANY);
        final Form form = new Form("Find in Code");
        form.append(tf);
        form.append(new StringItem("Tip:",
            " Press # key to find next\n"));
        Command ok   = new Command("Find",
            Command.OK,   1);
        Command back = new Command("Back",
            Command.BACK, 2);
        form.addCommand(ok);
        form.addCommand(back);
        form.setCommandListener(
            new CommandListener() {
                public void commandAction(
                        Command c, Displayable d) {
                    if (c.getCommandType() ==
                            Command.OK) {
                        findQuery  = tf.getString();
                        findActive = false;
                        findNext();
                    }
                    display.setCurrent(
                        J2MEEditor.this);
                }
            });
        display.setCurrent(form);
    }

    private void showSaveAsSnippetDialog() {
        if (fm == null) return;
        String defName =
            fm.replaceToken(fileName, ".java", "");
        final TextField tf = new TextField(
            "Snippet Name:", defName, 40,
            TextField.ANY);
        final Form form = new Form("Save as Snippet");
        form.append(tf);
        form.append(new StringItem("",
            "Saves current code as\n" +
            "a reusable snippet.\n"));
        Command save = new Command("Save",
            Command.OK,   1);
        Command back = new Command("Back",
            Command.BACK, 2);
        form.addCommand(save);
        form.addCommand(back);
        form.setCommandListener(
            new CommandListener() {
                public void commandAction(
                        Command c, Displayable d) {
                    if (c.getCommandType() ==
                            Command.OK) {
                        String name =
                            tf.getString().trim();
                        if (name.length() > 0) {
                            boolean ok =
                                fm.saveCustomSnippet(
                                    name, getCode());
                            Alert a = new Alert(
                                ok?"Saved":"Error",
                                ok?"Snippet saved!":
                                   "Could not save.",
                                null,
                                ok?AlertType.CONFIRMATION:
                                   AlertType.ERROR);
                            a.setTimeout(1800);
                            display.setCurrent(
                                a, J2MEEditor.this);
                            return;
                        }
                    }
                    display.setCurrent(
                        J2MEEditor.this);
                }
            });
        display.setCurrent(form);
    }
}