import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.io.*;
import java.util.Vector;

/**
 * J2MEIDE.java - Main MIDlet
 * Full J2ME IDE  v1.1
 * Vendor  : DASH ANIMATION V2
 * YouTube : https://youtube.com/@dash______animationv2
 * GitHub  : https://github.com/Dahmalahi/
 * CLDC 1.1 / MIDP 2.0
 */
public class J2MEIDE extends MIDlet
        implements CommandListener {

    private Display     display;
    private FileManager fm;
    private J2MEEditor  editor;

    // --- Main Menu ---
    private List    mainMenu;
    private Command cmdSelect;
    private Command cmdExit;

    private static final String[] MENU_ITEMS = {
        "  New Code",
        "  Open Project",
        "  Recent Files",
        "  Snippets",
        "  My Snippets",
        "  Code Analyze",
        "  TODO List",
        "  Code Stats",
        "  About",
        "  Settings",
        "  Exit"
    };
    private static final int MI_NEW      = 0;
    private static final int MI_OPEN     = 1;
    private static final int MI_RECENT   = 2;
    private static final int MI_SNIPPETS = 3;
    private static final int MI_MYSNIP   = 4;
    private static final int MI_ANALYZE  = 5;
    private static final int MI_TODO     = 6;
    private static final int MI_STATS    = 7;
    private static final int MI_ABOUT    = 8;
    private static final int MI_SETTINGS = 9;
    private static final int MI_EXIT     = 10;

    // --- Built-in snippets ---
    private static final String[] SNIPPET_NAMES = {
        "Hello MIDlet",
        "Canvas Game",
        "HTTP GET Request",
        "Timer Task",
        "Alert Dialog",
        "TextBox Input",
        "Gauge Progress",
        "List Menu",
        "Record Store (RMS)",
        "Sprite Animation",
        "Sound (MIDI)",
        "Player (Video)",
        "Bluetooth Discovery",
        "UDP Datagram",
        "Custom Item",
        "FileConnection Read",
        "FileConnection Write",
        "Thread Worker",
        "Math and Random",
        "Basic Game Loop",
        "Send SMS",
        "Vibrate Device",
        "DateField Picker",
        "ChoiceGroup Example",
        "Ticker Scroller"
    };

    // --- Settings ---
    private int    settingFontSize    = 0;
    private int    settingTheme       = 0;
    private int    settingAutoIndent  = 0;
    private int    settingLineNumbers = 0;
    private int    settingTabSize     = 1;
    private int    settingWordWrap    = 1;
    private int    settingBracket     = 0;
    private int    settingScrollSpeed = 0;
    private int    settingHints       = 0; // 0=On,1=Off
    private String settingDevName     = "";

    // --- RMS keys ---
    private static final String RMS_SETTINGS = "IDESettings3";
    private static final String RMS_FIRSTRUN = "IDEFirstRun";
    private static final String RMS_DEVNAME  = "IDEDevName";
    private static final String RMS_RECENT   = "IDERecent";

    // --- Recent files ---
    private Vector recentFiles = new Vector();

    // --- State ---
    private boolean snippetFromEditor = false;

    // =============================================
    // Lifecycle
    // =============================================

    public void startApp() {
        display = Display.getDisplay(this);
        fm      = new FileManager();
        editor  = new J2MEEditor(this, display, fm);

        fm.initProjectsDir();
        loadSettingsFromRMS();
        loadRecentFiles();
        applySettings();
        buildMainMenu();

        if (isFirstRun()) {
            markNotFirstRun();
            showReadmePopup();
        } else {
            display.setCurrent(mainMenu);
        }
    }

    public void pauseApp() {}
    public void destroyApp(boolean u) {}

    // =============================================
    // Apply settings to editor
    // =============================================

    private void applySettings() {
        editor.setHintEnabled(settingHints == 0);
    }

    // =============================================
    // First Run
    // =============================================

    private boolean isFirstRun() {
        try {
            javax.microedition.rms.RecordStore rs =
                javax.microedition.rms.RecordStore
                    .openRecordStore(RMS_FIRSTRUN,false);
            rs.closeRecordStore();
            return false;
        } catch (Exception e) { return true; }
    }

    private void markNotFirstRun() {
        try {
            javax.microedition.rms.RecordStore rs =
                javax.microedition.rms.RecordStore
                    .openRecordStore(RMS_FIRSTRUN,true);
            byte[] d = "1".getBytes();
            if (rs.getNumRecords() == 0)
                rs.addRecord(d, 0, d.length);
            rs.closeRecordStore();
        } catch (Exception e) {}
    }

    private void showReadmePopup() {
        Form f = new Form("Welcome to J2ME IDE!");

        f.append(new StringItem("",
            "J2ME IDE v1.0\n" +
            "by DASH ANIMATION V2\n\n"));

        f.append(new StringItem(
            "QUICK START", "\n"));
        f.append(new StringItem("",
            "1. New Code - create project\n" +
            "   with scaffold on SD card.\n\n" +
            "2. Open Project - load existing\n" +
            "   project from SD card.\n\n" +
            "3. Recent Files - reopen last\n" +
            "   5 projects instantly.\n\n" +
            "4. Snippets - 25 built-in\n" +
            "   code templates.\n\n" +
            "5. My Snippets - save your own\n" +
            "   reusable code blocks.\n\n" +
            "6. Code Analyze - scan current\n" +
            "   file for J2ME issues.\n\n" +
            "7. TODO List - find all TODO,\n" +
            "   FIXME, HACK comments.\n\n" +
            "8. Code Stats - lines, methods,\n" +
            "   classes, file size.\n\n" +
            "9. Settings - set dev name,\n" +
            "   hints on/off, font, etc.\n\n"));

        f.append(new StringItem("EDITOR KEYS","\n"));
        f.append(new StringItem("",
            " 5  = Open TextBox editor\n" +
            " 0  = New line\n" +
            " 1  = Jump to line start\n" +
            " 3  = Jump to line end\n" +
            " 7  = Page up\n" +
            " 9  = Page down\n" +
            " 2  = Move line up\n" +
            " 8  = Move line down\n" +
            " *  = Toggle hints on/off\n" +
            " #  = Find next occurrence\n" +
            " D-pad = Navigate cursor\n" +
            " Fire  = New line\n" +
            " LSK   = Commands menu\n\n"));

        f.append(new StringItem("HINT SYSTEM","\n"));
        f.append(new StringItem("",
            " Yellow bar = J2ME tip\n" +
            " Red line = incompatible code\n" +
            " Green bar = correct J2ME code\n" +
            " Toggle with * key or Settings\n\n"));

        f.append(new StringItem("TEXTBOX MODE","\n"));
        f.append(new StringItem("",
            " Press 5 to open full TextBox.\n" +
            " Supports T9/full keyboard.\n" +
            " Insert templates directly.\n" +
            " Analyze code from TextBox.\n\n"));

        f.append(new StringItem("LINKS","\n"));
        f.append(new StringItem("",
            " YT: @dash______animationv2\n" +
            " GH: github.com/Dahmalahi/\n\n" +
            "NOTE: No compile on-device.\n" +
            "Transfer .java to PC + WTK.\n"));

        Command ok = new Command(
            "Got it!", Command.OK, 1);
        f.addCommand(ok);
        f.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                showFirstRunDevNameDialog();
            }
        });
        display.setCurrent(f);
    }

    private void showFirstRunDevNameDialog() {
        final Form form = new Form(
            "Your Developer Name");
        form.append(new StringItem("",
            "Enter your name.\n" +
            "It appears in all generated\n" +
            "file headers and manifests.\n\n"));
        final TextField tf = new TextField(
            "Dev Name:", "", 40, TextField.ANY);
        form.append(tf);

        Command ok   = new Command(
            "Save & Start", Command.OK,   1);
        Command skip = new Command(
            "Skip",         Command.BACK, 2);
        form.addCommand(ok);
        form.addCommand(skip);
        form.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    String n = tf.getString().trim();
                    if (n.length() > 0) {
                        settingDevName = n;
                        saveDevName();
                    }
                }
                display.setCurrent(mainMenu);
            }
        });
        display.setCurrent(form);
    }

    // =============================================
    // Main Menu
    // =============================================

    private void buildMainMenu() {
        mainMenu = new List(
            "J2ME IDE | DASH ANIMATION V2",
            List.IMPLICIT);
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            mainMenu.append(MENU_ITEMS[i], null);
        }
        cmdSelect = new Command(
            "Select", Command.OK,   1);
        cmdExit   = new Command(
            "Exit",   Command.EXIT, 2);
        mainMenu.addCommand(cmdSelect);
        mainMenu.addCommand(cmdExit);
        mainMenu.setCommandListener(this);
    }

    public void showMainMenu() {
        display.setCurrent(mainMenu);
    }

    // =============================================
    // CommandListener
    // =============================================

    public void commandAction(Command c,
                               Displayable d) {
        if (d == mainMenu) {
            if (c == cmdExit) { doExit(); return; }
            int idx = mainMenu.getSelectedIndex();
            switch (idx) {
                case MI_NEW:     showNewCodeDialog();    break;
                case MI_OPEN:    showOpenProject();      break;
                case MI_RECENT:  showRecentFiles();      break;
                case MI_SNIPPETS:showSnippetMenu(false); break;
                case MI_MYSNIP:  showMySnippets(false);  break;
                case MI_ANALYZE: showAnalyzeMenu();      break;
                case MI_TODO:    showTodoList();         break;
                case MI_STATS:   showCodeStats();        break;
                case MI_ABOUT:   showAbout();            break;
                case MI_SETTINGS:showSettings();         break;
                case MI_EXIT:    doExit();               break;
            }
        }
    }

    // =============================================
    // OPEN PROJECT
    // =============================================

    private void showOpenProject() {
        String[] projects = fm.listProjects();

        if (projects.length == 0) {
            showAlert("No Projects",
                "No projects found in\n" +
                "j2meprojects/ directory.\n" +
                "Create a new project first.",
                AlertType.INFO, mainMenu);
            return;
        }

        final List list = new List(
            "Open Project", List.IMPLICIT);
        for (int i = 0; i < projects.length; i++) {
            list.append(projects[i], null);
        }

        final String[] proj = projects;
        Command back = new Command(
            "Back", Command.BACK, 2);
        Command ok   = new Command(
            "Open", Command.OK,   1);
        list.addCommand(back);
        list.addCommand(ok);
        list.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                if (c.getCommandType() == Command.BACK) {
                    display.setCurrent(mainMenu);
                    return;
                }
                int idx = list.getSelectedIndex();
                if (idx >= 0 && idx < proj.length) {
                    openProject(proj[idx]);
                }
            }
        });
        display.setCurrent(list);
    }

    private void openProject(String name) {
        String base = fm.getProjectBasePath();
        if (base == null) {
            showAlert("Error",
                "Storage not available.",
                AlertType.ERROR, mainMenu);
            return;
        }
        String path = base + name + "/src/" +
                      name + ".java";
        String code = fm.readFile(path);
        if (code == null) {
            showAlert("Error",
                "Cannot read " + name + ".java\n" +
                "Check file exists in src/",
                AlertType.ERROR, mainMenu);
            return;
        }
        addRecentFile(name);
        editor.loadCode(code, name + ".java");
        editor.setFileName(name + ".java");
        display.setCurrent(editor);
    }

    // =============================================
    // CODE ANALYZE
    // =============================================

    private void showAnalyzeMenu() {
        Form form = new Form("Code Analyze");
        form.append(new StringItem("",
            "Analyzes the currently open\n" +
            "file for J2ME compatibility\n" +
            "issues and gives tips.\n\n"));

        String fname = editor.getFileName();
        form.append(new StringItem(
            "Current file:", " " + fname + "\n\n"));

        Command analyze = new Command(
            "Analyze Now", Command.OK,   1);
        Command back    = new Command(
            "Back",        Command.BACK, 2);
        form.addCommand(analyze);
        form.addCommand(back);

        form.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    String result =
                        editor.analyzeDocument();
                    Alert a = new Alert(
                        "Analysis Result",
                        result, null, AlertType.INFO);
                    a.setTimeout(Alert.FOREVER);
                    display.setCurrent(a, mainMenu);
                } else {
                    display.setCurrent(mainMenu);
                }
            }
        });
        display.setCurrent(form);
    }

    // =============================================
    // NEW CODE
    // =============================================

    private void showNewCodeDialog() {
        final List tplMenu = new List(
            "Choose Template", List.IMPLICIT);
        tplMenu.append("MIDlet + CommandListener",null);
        tplMenu.append("Canvas Game",             null);
        tplMenu.append("HTTP Connection",         null);
        tplMenu.append("RMS Storage",             null);
        tplMenu.append("Splash Screen",           null);
        tplMenu.append("Settings Form",           null);
        tplMenu.append("Blank File",              null);

        Command back = new Command(
            "Back", Command.BACK, 2);
        Command ok   = new Command(
            "Next", Command.OK,   1);
        tplMenu.addCommand(back);
        tplMenu.addCommand(ok);
        tplMenu.setCommandListener(
            new CommandListener() {
                public void commandAction(
                        Command c, Displayable d) {
                    if (c.getCommandType() ==
                            Command.BACK) {
                        display.setCurrent(mainMenu);
                        return;
                    }
                    showProjectNameDialog(
                        tplMenu.getSelectedIndex());
                }
            });
        display.setCurrent(tplMenu);
    }

    private void showProjectNameDialog(
            final int tplIdx) {
        final Form form = new Form("New Project");
        form.append(new StringItem("Template: ",
            getTemplateName(tplIdx) + "\n"));
        if (settingDevName.length() > 0) {
            form.append(new StringItem("Dev: ",
                settingDevName + "\n"));
        }
        final TextField tfName = new TextField(
            "Project Name:", "", 40, TextField.ANY);
        form.append(tfName);

        Command ok   = new Command(
            "Create", Command.OK,   1);
        Command back = new Command(
            "Back",   Command.BACK, 2);
        form.addCommand(ok);
        form.addCommand(back);
        form.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                if (c.getCommandType() == Command.BACK) {
                    showNewCodeDialog();
                    return;
                }
                String name =
                    tfName.getString().trim();
                if (name.length() == 0) {
                    showAlert("Error",
                        "Name cannot be empty.",
                        AlertType.ERROR, form);
                    return;
                }
                name = sanitizeName(name);
                createNewProject(name, tplIdx);
            }
        });
        display.setCurrent(form);
    }

    private void createNewProject(String name,
                                   int tplIdx) {
        String  code = buildTemplate(name, tplIdx);
        boolean ok   = fm.createProject(
            name, code, settingDevName);

        if (ok) {
            addRecentFile(name);
            editor.loadCode(code, name + ".java");
            editor.setFileName(name + ".java");
            display.setCurrent(editor);
        } else {
            showAlert("Error",
                "Could not create project.\n" +
                "Check JSR-75 permissions.",
                AlertType.ERROR, mainMenu);
        }
    }

    private String getTemplateName(int idx) {
        switch (idx) {
            case 0: return "MIDlet+CommandListener";
            case 1: return "Canvas Game";
            case 2: return "HTTP Connection";
            case 3: return "RMS Storage";
            case 4: return "Splash Screen";
            case 5: return "Settings Form";
            default: return "Blank File";
        }
    }

    // =============================================
    // TEMPLATES
    // =============================================

    private String buildHeader(String name) {
        String dev = (settingDevName.length() > 0)
            ? settingDevName : "DASH ANIMATION V2";
        return
            "/**\n" +
            " * " + name + ".java\n" +
            " * Author  : " + dev + "\n" +
            " * Created : J2ME IDE v1.0\n" +
            " * Vendor  : DASH ANIMATION V2\n" +
            " * Platform: MIDP 2.0 / CLDC 1.1\n" +
            " */\n";
    }

    private String buildTemplate(String n, int idx) {
        switch (idx) {
            case 0: return tplMIDlet(n);
            case 1: return tplCanvas(n);
            case 2: return tplHTTP(n);
            case 3: return tplRMS(n);
            case 4: return tplSplash(n);
            case 5: return tplSettings(n);
            default:
                return buildHeader(n) +
                    "import javax.microedition.lcdui.*;\n" +
                    "import javax.microedition.midlet.*;\n\n" +
                    "// TODO: Write your code here\n";
        }
    }

    private String tplMIDlet(String n) {
        return
            buildHeader(n) +
            "import javax.microedition.lcdui.*;\n" +
            "import javax.microedition.midlet.*;\n\n" +
            "public class " + n +
            " extends MIDlet\n" +
            "        implements CommandListener {\n\n" +
            "    private Display display;\n" +
            "    private Form    mainForm;\n" +
            "    private Command cmdExit;\n\n" +
            "    public void startApp() {\n" +
            "        display  =\n" +
            "            Display.getDisplay(this);\n" +
            "        mainForm =\n" +
            "            new Form(\"Hello World\");\n" +
            "        mainForm.append(\n" +
            "            new StringItem(\"\",\n" +
            "            \"Hello from J2ME IDE!\"));\n" +
            "        cmdExit = new Command(\n" +
            "            \"Exit\",\n" +
            "            Command.EXIT, 1);\n" +
            "        mainForm.addCommand(cmdExit);\n" +
            "        mainForm\n" +
            "            .setCommandListener(this);\n" +
            "        display.setCurrent(mainForm);\n" +
            "    }\n\n" +
            "    public void pauseApp()  {}\n" +
            "    public void destroyApp(\n" +
            "            boolean u) {}\n\n" +
            "    public void commandAction(\n" +
            "            Command c,\n" +
            "            Displayable d) {\n" +
            "        if (c == cmdExit) {\n" +
            "            destroyApp(false);\n" +
            "            notifyDestroyed();\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
    }

    private String tplCanvas(String n) {
        return
            buildHeader(n) +
            "import javax.microedition.lcdui.*;\n" +
            "import javax.microedition.midlet.*;\n\n" +
            "public class " + n +
            " extends MIDlet {\n\n" +
            "    private Display  display;\n" +
            "    private MyCanvas canvas;\n\n" +
            "    public void startApp() {\n" +
            "        display =\n" +
            "            Display.getDisplay(this);\n" +
            "        canvas = new MyCanvas(this);\n" +
            "        display.setCurrent(canvas);\n" +
            "        canvas.start();\n" +
            "    }\n" +
            "    public void pauseApp()  {}\n" +
            "    public void destroyApp(\n" +
            "            boolean u) {}\n" +
            "}\n\n" +
            "class MyCanvas extends Canvas\n" +
            "        implements Runnable {\n\n" +
            "    private " + n + " midlet;\n" +
            "    private boolean running = false;\n" +
            "    private int x=0,y=0,dx=2,dy=2;\n\n" +
            "    public MyCanvas(" + n + " m) {\n" +
            "        midlet = m;\n" +
            "        setFullScreenMode(true);\n" +
            "    }\n\n" +
            "    public void start() {\n" +
            "        running = true;\n" +
            "        new Thread(this).start();\n" +
            "    }\n\n" +
            "    public void run() {\n" +
            "        long prev =\n" +
            "            System.currentTimeMillis();\n" +
            "        while (running) {\n" +
            "            long now =\n" +
            "                System.currentTimeMillis();\n" +
            "            long dt = now - prev;\n" +
            "            prev = now;\n" +
            "            update(dt);\n" +
            "            repaint();\n" +
            "            serviceRepaints();\n" +
            "            try {\n" +
            "                Thread.sleep(\n" +
            "                    Math.max(0,33-dt));\n" +
            "            } catch(Exception e){}\n" +
            "        }\n" +
            "    }\n\n" +
            "    private void update(long dt) {\n" +
            "        x+=dx; y+=dy;\n" +
            "        if(x<0||x>getWidth()-20) dx=-dx;\n" +
            "        if(y<0||y>getHeight()-20)dy=-dy;\n" +
            "    }\n\n" +
            "    protected void paint(Graphics g) {\n" +
            "        g.setColor(0x1E1E1E);\n" +
            "        g.fillRect(0,0,\n" +
            "            getWidth(),getHeight());\n" +
            "        g.setColor(0x007ACC);\n" +
            "        g.fillRoundRect(\n" +
            "            x,y,20,20,8,8);\n" +
            "    }\n\n" +
            "    protected void keyPressed(int k) {\n" +
            "        int a = getGameAction(k);\n" +
            "        if(a==LEFT)  x-=5;\n" +
            "        if(a==RIGHT) x+=5;\n" +
            "        if(a==UP)    y-=5;\n" +
            "        if(a==DOWN)  y+=5;\n" +
            "    }\n" +
            "}\n";
    }

    private String tplHTTP(String n) {
        return
            buildHeader(n) +
            "import javax.microedition.lcdui.*;\n" +
            "import javax.microedition.midlet.*;\n" +
            "import javax.microedition.io.*;\n" +
            "import java.io.*;\n\n" +
            "public class " + n +
            " extends MIDlet\n" +
            "        implements CommandListener,\n" +
            "        Runnable {\n\n" +
            "    private Display    display;\n" +
            "    private Form       form;\n" +
            "    private StringItem result;\n" +
            "    private Command    cmdFetch;\n" +
            "    private Command    cmdExit;\n\n" +
            "    public void startApp() {\n" +
            "        display =\n" +
            "            Display.getDisplay(this);\n" +
            "        form = new Form(\"HTTP Demo\");\n" +
            "        result = new StringItem(\n" +
            "            \"Response:\", \"\");\n" +
            "        form.append(result);\n" +
            "        cmdFetch = new Command(\n" +
            "            \"Fetch\",Command.OK,1);\n" +
            "        cmdExit = new Command(\n" +
            "            \"Exit\",Command.EXIT,2);\n" +
            "        form.addCommand(cmdFetch);\n" +
            "        form.addCommand(cmdExit);\n" +
            "        form.setCommandListener(this);\n" +
            "        display.setCurrent(form);\n" +
            "    }\n" +
            "    public void pauseApp()  {}\n" +
            "    public void destroyApp(\n" +
            "            boolean u) {}\n\n" +
            "    public void commandAction(\n" +
            "            Command c,Displayable d) {\n" +
            "        if (c == cmdFetch) {\n" +
            "            result.setText(\n" +
            "                \"Loading...\");\n" +
            "            new Thread(this).start();\n" +
            "        } else {\n" +
            "            destroyApp(false);\n" +
            "            notifyDestroyed();\n" +
            "        }\n" +
            "    }\n\n" +
            "    public void run() {\n" +
            "        HttpConnection hc = null;\n" +
            "        InputStream    is = null;\n" +
            "        try {\n" +
            "            hc = (HttpConnection)\n" +
            "                Connector.open(\n" +
            "                \"http://example.com\");\n" +
            "            hc.setRequestMethod(\n" +
            "                HttpConnection.GET);\n" +
            "            int code =\n" +
            "                hc.getResponseCode();\n" +
            "            is = hc.openInputStream();\n" +
            "            byte[] buf = new byte[256];\n" +
            "            int n = is.read(buf);\n" +
            "            if (n > 0)\n" +
            "                result.setText(\n" +
            "                \"HTTP \"+code+\"\\n\"+\n" +
            "                new String(buf,0,n));\n" +
            "        } catch (Exception e) {\n" +
            "            result.setText(\n" +
            "                \"Err:\"+e.getMessage());\n" +
            "        } finally {\n" +
            "            try{if(is!=null)\n" +
            "                is.close();}\n" +
            "            catch(Exception ig){}\n" +
            "            try{if(hc!=null)\n" +
            "                hc.close();}\n" +
            "            catch(Exception ig){}\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
    }

    private String tplRMS(String n) {
        return
            buildHeader(n) +
            "import javax.microedition.lcdui.*;\n" +
            "import javax.microedition.midlet.*;\n" +
            "import javax.microedition.rms.*;\n\n" +
            "public class " + n +
            " extends MIDlet\n" +
            "        implements CommandListener {\n\n" +
            "    private static final String STORE\n" +
            "        = \"MyStore\";\n" +
            "    private Display   display;\n" +
            "    private Form      form;\n" +
            "    private TextField tf;\n" +
            "    private Command   cmdSave,cmdLoad,\n" +
            "                      cmdExit;\n\n" +
            "    public void startApp() {\n" +
            "        display =\n" +
            "            Display.getDisplay(this);\n" +
            "        form = new Form(\"RMS Demo\");\n" +
            "        tf   = new TextField(\n" +
            "            \"Data:\",\"\",100,\n" +
            "            TextField.ANY);\n" +
            "        form.append(tf);\n" +
            "        cmdSave = new Command(\n" +
            "            \"Save\",Command.OK,1);\n" +
            "        cmdLoad = new Command(\n" +
            "            \"Load\",Command.ITEM,2);\n" +
            "        cmdExit = new Command(\n" +
            "            \"Exit\",Command.EXIT,3);\n" +
            "        form.addCommand(cmdSave);\n" +
            "        form.addCommand(cmdLoad);\n" +
            "        form.addCommand(cmdExit);\n" +
            "        form.setCommandListener(this);\n" +
            "        display.setCurrent(form);\n" +
            "    }\n" +
            "    public void pauseApp()  {}\n" +
            "    public void destroyApp(\n" +
            "            boolean u) {}\n\n" +
            "    public void commandAction(\n" +
            "            Command c,Displayable d) {\n" +
            "        if (c==cmdSave) saveData();\n" +
            "        else if(c==cmdLoad) loadData();\n" +
            "        else {\n" +
            "            destroyApp(false);\n" +
            "            notifyDestroyed();\n" +
            "        }\n" +
            "    }\n\n" +
            "    private void saveData() {\n" +
            "        RecordStore rs = null;\n" +
            "        try {\n" +
            "            rs = RecordStore\n" +
            "                .openRecordStore(\n" +
            "                    STORE,true);\n" +
            "            byte[] d =\n" +
            "                tf.getString()\n" +
            "                .getBytes();\n" +
            "            if(rs.getNumRecords()==0)\n" +
            "                rs.addRecord(\n" +
            "                    d,0,d.length);\n" +
            "            else rs.setRecord(\n" +
            "                    1,d,0,d.length);\n" +
            "        } catch(Exception e){}\n" +
            "        finally {\n" +
            "            try{if(rs!=null)\n" +
            "                rs.closeRecordStore();}\n" +
            "            catch(Exception ig){}\n" +
            "        }\n" +
            "    }\n\n" +
            "    private void loadData() {\n" +
            "        RecordStore rs = null;\n" +
            "        try {\n" +
            "            rs = RecordStore\n" +
            "                .openRecordStore(\n" +
            "                    STORE,false);\n" +
            "            if(rs.getNumRecords()>0)\n" +
            "                tf.setString(\n" +
            "                    new String(\n" +
            "                    rs.getRecord(1)));\n" +
            "        } catch(Exception e){}\n" +
            "        finally {\n" +
            "            try{if(rs!=null)\n" +
            "                rs.closeRecordStore();}\n" +
            "            catch(Exception ig){}\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
    }

    // =============================================
    // TEMPLATE: SPLASH SCREEN
    // =============================================

    private String tplSplash(String n) {
        return
            buildHeader(n) +
            "import javax.microedition.lcdui.*;\n" +
            "import javax.microedition.midlet.*;\n\n" +
            "public class " + n +
            " extends MIDlet\n" +
            "        implements CommandListener {\n\n" +
            "    private Display display;\n" +
            "    private Form    splash;\n" +
            "    private Form    main;\n" +
            "    private Command cmdExit;\n\n" +
            "    public void startApp() {\n" +
            "        display =\n" +
            "            Display.getDisplay(this);\n" +
            "        buildSplash();\n" +
            "        buildMain();\n" +
            "        display.setCurrent(splash);\n" +
            "        new Thread(new Runnable(){\n" +
            "            public void run(){\n" +
            "                try{\n" +
            "                    Thread.sleep(2000);\n" +
            "                } catch(Exception e){}\n" +
            "                display.setCurrent(main);\n" +
            "            }\n" +
            "        }).start();\n" +
            "    }\n\n" +
            "    private void buildSplash() {\n" +
            "        splash = new Form(null);\n" +
            "        splash.append(\n" +
            "            new StringItem(\"\",\n" +
            "            \"\\n\\n   " + n + "\\n\" +\n" +
            "            \"  Loading...\\n\"));\n" +
            "        Gauge g = new Gauge(\n" +
            "            null,false,10,0);\n" +
            "        splash.append(g);\n" +
            "    }\n\n" +
            "    private void buildMain() {\n" +
            "        main = new Form(\"" + n + "\");\n" +
            "        main.append(\n" +
            "            new StringItem(\"\",\n" +
            "            \"Welcome!\\n\"));\n" +
            "        cmdExit = new Command(\n" +
            "            \"Exit\",Command.EXIT,1);\n" +
            "        main.addCommand(cmdExit);\n" +
            "        main.setCommandListener(this);\n" +
            "    }\n\n" +
            "    public void pauseApp()  {}\n" +
            "    public void destroyApp(\n" +
            "            boolean u) {}\n\n" +
            "    public void commandAction(\n" +
            "            Command c, Displayable d) {\n" +
            "        destroyApp(false);\n" +
            "        notifyDestroyed();\n" +
            "    }\n" +
            "}\n";
    }

    // =============================================
    // TEMPLATE: SETTINGS FORM
    // =============================================

    private String tplSettings(String n) {
        return
            buildHeader(n) +
            "import javax.microedition.lcdui.*;\n" +
            "import javax.microedition.midlet.*;\n" +
            "import javax.microedition.rms.*;\n\n" +
            "public class " + n +
            " extends MIDlet\n" +
            "        implements CommandListener {\n\n" +
            "    private static final String STORE\n" +
            "        = \"" + n + "Cfg\";\n" +
            "    private Display     display;\n" +
            "    private Form        sForm;\n" +
            "    private TextField   tfName;\n" +
            "    private ChoiceGroup cgTheme;\n" +
            "    private ChoiceGroup cgSound;\n" +
            "    private Command     cmdSave;\n" +
            "    private Command     cmdExit;\n\n" +
            "    public void startApp() {\n" +
            "        display =\n" +
            "            Display.getDisplay(this);\n" +
            "        sForm = new Form(\"Settings\");\n" +
            "        tfName = new TextField(\n" +
            "            \"Name:\",\"\",30,\n" +
            "            TextField.ANY);\n" +
            "        cgTheme = new ChoiceGroup(\n" +
            "            \"Theme:\",\n" +
            "            ChoiceGroup.EXCLUSIVE);\n" +
            "        cgTheme.append(\"Dark\",null);\n" +
            "        cgTheme.append(\"Light\",null);\n" +
            "        cgTheme.setSelectedIndex(0,true);\n" +
            "        cgSound = new ChoiceGroup(\n" +
            "            \"Sound:\",\n" +
            "            ChoiceGroup.EXCLUSIVE);\n" +
            "        cgSound.append(\"On\",null);\n" +
            "        cgSound.append(\"Off\",null);\n" +
            "        cgSound.setSelectedIndex(0,true);\n" +
            "        sForm.append(tfName);\n" +
            "        sForm.append(cgTheme);\n" +
            "        sForm.append(cgSound);\n" +
            "        cmdSave = new Command(\n" +
            "            \"Save\",Command.OK,1);\n" +
            "        cmdExit = new Command(\n" +
            "            \"Exit\",Command.EXIT,2);\n" +
            "        sForm.addCommand(cmdSave);\n" +
            "        sForm.addCommand(cmdExit);\n" +
            "        sForm.setCommandListener(this);\n" +
            "        loadCfg();\n" +
            "        display.setCurrent(sForm);\n" +
            "    }\n\n" +
            "    public void pauseApp()  {}\n" +
            "    public void destroyApp(\n" +
            "            boolean u) {}\n\n" +
            "    public void commandAction(\n" +
            "            Command c,Displayable d) {\n" +
            "        if (c==cmdSave) saveCfg();\n" +
            "        else {\n" +
            "            destroyApp(false);\n" +
            "            notifyDestroyed();\n" +
            "        }\n" +
            "    }\n\n" +
            "    private void saveCfg() {\n" +
            "        RecordStore rs=null;\n" +
            "        try {\n" +
            "            rs=RecordStore\n" +
            "                .openRecordStore(STORE,true);\n" +
            "            String v=tfName.getString()\n" +
            "                +\"|\"+cgTheme\n" +
            "                .getSelectedIndex()\n" +
            "                +\"|\"+cgSound\n" +
            "                .getSelectedIndex();\n" +
            "            byte[] b=v.getBytes();\n" +
            "            if(rs.getNumRecords()==0)\n" +
            "                rs.addRecord(b,0,b.length);\n" +
            "            else\n" +
            "                rs.setRecord(1,b,0,b.length);\n" +
            "        } catch(Exception e){}\n" +
            "        finally{\n" +
            "            try{if(rs!=null)\n" +
            "                rs.closeRecordStore();}\n" +
            "            catch(Exception ig){}\n" +
            "        }\n" +
            "    }\n\n" +
            "    private void loadCfg() {\n" +
            "        RecordStore rs=null;\n" +
            "        try {\n" +
            "            rs=RecordStore\n" +
            "                .openRecordStore(STORE,false);\n" +
            "            if(rs.getNumRecords()>0){\n" +
            "                String v=new String(\n" +
            "                    rs.getRecord(1));\n" +
            "                int p1=v.indexOf('|');\n" +
            "                int p2=v.indexOf('|',p1+1);\n" +
            "                if(p1!=-1){\n" +
            "                    tfName.setString(\n" +
            "                        v.substring(0,p1));\n" +
            "                    if(p2!=-1){\n" +
            "                        cgTheme\n" +
            "                        .setSelectedIndex(\n" +
            "                        Integer.parseInt(\n" +
            "                        v.substring(\n" +
            "                        p1+1,p2)),true);\n" +
            "                        cgSound\n" +
            "                        .setSelectedIndex(\n" +
            "                        Integer.parseInt(\n" +
            "                        v.substring(\n" +
            "                        p2+1)),true);\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        } catch(Exception e){}\n" +
            "        finally{\n" +
            "            try{if(rs!=null)\n" +
            "                rs.closeRecordStore();}\n" +
            "            catch(Exception ig){}\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
    }

    // =============================================
    // SNIPPETS
    // =============================================

    public void showSnippetMenu(boolean fromEditor) {
        snippetFromEditor = fromEditor;
        final List menu = new List(
            "Built-in Snippets", List.IMPLICIT);
        for (int i = 0; i < SNIPPET_NAMES.length; i++)
            menu.append(SNIPPET_NAMES[i], null);

        Command back = new Command(
            "Back", Command.BACK, 2);
        Command ok   = new Command(
            "View", Command.OK,   1);
        menu.addCommand(back);
        menu.addCommand(ok);
        menu.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                if (c.getCommandType() == Command.BACK) {
                    returnFromSnippet();
                    return;
                }
                showSnippetDetail(
                    menu.getSelectedIndex());
            }
        });
        display.setCurrent(menu);
    }

    private void showSnippetDetail(final int idx) {
        final String code = getSnippetCode(idx);
        Form form = new Form(SNIPPET_NAMES[idx]);
        form.append(new StringItem("", code));

        Command back   = new Command(
            "Back",   Command.BACK, 2);
        Command insert = new Command(
            "Insert", Command.OK,   1);
        Command save   = new Command(
            "Save as My Snippet", Command.ITEM, 3);
        form.addCommand(back);
        form.addCommand(save);
        if (snippetFromEditor) form.addCommand(insert);

        form.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    editor.insertSnippetAtCursor(code);
                    display.setCurrent(editor);
                } else if (c.getLabel().equals(
                        "Save as My Snippet")) {
                    if (fm != null) {
                        fm.saveCustomSnippet(
                            SNIPPET_NAMES[idx], code);
                        showAlert("Saved",
                            "Saved to My Snippets!",
                            AlertType.CONFIRMATION,
                            display.getCurrent());
                    }
                } else {
                    showSnippetMenu(snippetFromEditor);
                }
            }
        });
        display.setCurrent(form);
    }

    private void returnFromSnippet() {
        if (snippetFromEditor)
            display.setCurrent(editor);
        else
            display.setCurrent(mainMenu);
    }

    // =============================================
    // MY SNIPPETS
    // =============================================

    public void showMySnippets(boolean fromEditor) {
        snippetFromEditor = fromEditor;
        final String[] names = fm.listCustomSnippets();

        final List menu = new List(
            "My Snippets", List.IMPLICIT);
        menu.append("  + Create New Snippet", null);
        for (int i = 0; i < names.length; i++) {
            menu.append("  " + names[i], null);
        }

        Command back   = new Command(
            "Back",   Command.BACK, 2);
        Command ok     = new Command(
            "Open",   Command.OK,   1);
        Command delete = new Command(
            "Delete", Command.ITEM, 3);
        menu.addCommand(back);
        menu.addCommand(ok);
        if (names.length > 0)
            menu.addCommand(delete);

        menu.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                int idx = menu.getSelectedIndex();
                if (c.getCommandType() == Command.BACK) {
                    returnFromSnippet();
                    return;
                }
                if (c.getLabel().equals("Delete")) {
                    if (idx > 0 &&
                        idx <= names.length) {
                        fm.deleteCustomSnippet(
                            names[idx-1]);
                        showMySnippets(
                            snippetFromEditor);
                    }
                    return;
                }
                if (idx == 0) {
                    showCreateSnippetDialog();
                } else if (idx <= names.length) {
                    showCustomSnippetDetail(
                        names[idx-1]);
                }
            }
        });
        display.setCurrent(menu);
    }

    private void showCreateSnippetDialog() {
        final Form form = new Form("New Snippet");
        final TextField tfName = new TextField(
            "Snippet Name:", "", 40, TextField.ANY);
        final TextField tfCode = new TextField(
            "Code (or use From Editor):",
            "", 2000, TextField.ANY);
        form.append(tfName);
        form.append(tfCode);
        form.append(new StringItem("Tip:",
            " Press 'From Editor' to use\n" +
            " the currently open file.\n"));

        Command save  = new Command(
            "Save",        Command.OK,   1);
        Command back  = new Command(
            "Back",        Command.BACK, 2);
        Command paste = new Command(
            "From Editor", Command.ITEM, 3);
        form.addCommand(save);
        form.addCommand(back);
        form.addCommand(paste);

        form.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                if (c.getCommandType() == Command.BACK) {
                    showMySnippets(snippetFromEditor);
                    return;
                }
                if (c.getLabel().equals(
                        "From Editor")) {
                    tfCode.setString(
                        editor.getCode());
                    if (tfName.getString().trim()
                            .length() == 0) {
                        tfName.setString(
                            stripJavaExt(
                            editor.getFileName()));
                    }
                    return;
                }
                String name =
                    tfName.getString().trim();
                if (name.length() == 0) {
                    showAlert("Error",
                        "Snippet name required.",
                        AlertType.ERROR, form);
                    return;
                }
                boolean ok = fm.saveCustomSnippet(
                    name, tfCode.getString());
                if (ok) {
                    showAlert("Saved",
                        "'" + name + "' saved!",
                        AlertType.CONFIRMATION,
                        mainMenu);
                    showMySnippets(snippetFromEditor);
                } else {
                    showAlert("Error",
                        "Could not save.",
                        AlertType.ERROR, form);
                }
            }
        });
        display.setCurrent(form);
    }

    private void showCustomSnippetDetail(
            final String name) {
        String code = fm.loadCustomSnippet(name);
        if (code == null) code = "(empty)";
        final String finalCode = code;

        Form form = new Form(name);
        form.append(new StringItem("", finalCode));

        Command back   = new Command(
            "Back",   Command.BACK, 2);
        Command insert = new Command(
            "Insert", Command.OK,   1);
        Command edit   = new Command(
            "Edit",   Command.ITEM, 3);
        form.addCommand(back);
        form.addCommand(edit);
        if (snippetFromEditor) form.addCommand(insert);

        form.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    editor.insertSnippetAtCursor(
                        finalCode);
                    display.setCurrent(editor);
                } else if (c.getLabel()
                        .equals("Edit")) {
                    showEditSnippetDialog(
                        name, finalCode);
                } else {
                    showMySnippets(snippetFromEditor);
                }
            }
        });
        display.setCurrent(form);
    }

    private void showEditSnippetDialog(
            final String name, String code) {
        final Form form = new Form("Edit: " + name);
        final TextField tf = new TextField(
            "Code:", code, 2000, TextField.ANY);
        form.append(tf);

        Command save = new Command(
            "Save", Command.OK,   1);
        Command back = new Command(
            "Back", Command.BACK, 2);
        form.addCommand(save);
        form.addCommand(back);
        form.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    boolean ok = fm.saveCustomSnippet(
                        name, tf.getString());
                    showAlert(
                        ok ? "Saved" : "Error",
                        ok ? "Updated!" :
                             "Could not save.",
                        ok ? AlertType.CONFIRMATION :
                             AlertType.ERROR,
                        mainMenu);
                    showMySnippets(snippetFromEditor);
                } else {
                    showCustomSnippetDetail(name);
                }
            }
        });
        display.setCurrent(form);
    }

    // =============================================
    // RECENT FILES
    // =============================================

    private void addRecentFile(String name) {
        for (int i = 0; i < recentFiles.size(); i++) {
            if (recentFiles.elementAt(i).equals(name)){
                recentFiles.removeElementAt(i);
                break;
            }
        }
        recentFiles.insertElementAt(name, 0);
        while (recentFiles.size() > 5) {
            recentFiles.removeElementAt(
                recentFiles.size() - 1);
        }
        saveRecentFiles();
    }

    private void saveRecentFiles() {
        try {
            javax.microedition.rms.RecordStore rs =
                javax.microedition.rms.RecordStore
                    .openRecordStore(RMS_RECENT, true);
            StringBuffer sb = new StringBuffer();
            for (int i = 0;
                 i < recentFiles.size(); i++) {
                if (i > 0) sb.append('|');
                sb.append(
                    (String)recentFiles.elementAt(i));
            }
            byte[] b = sb.toString().getBytes();
            if (rs.getNumRecords() == 0)
                rs.addRecord(b, 0, b.length);
            else
                rs.setRecord(1, b, 0, b.length);
            rs.closeRecordStore();
        } catch (Exception e) {}
    }

    private void loadRecentFiles() {
        try {
            javax.microedition.rms.RecordStore rs =
                javax.microedition.rms.RecordStore
                    .openRecordStore(RMS_RECENT, false);
            if (rs.getNumRecords() > 0) {
                String data =
                    new String(rs.getRecord(1));
                String[] parts = splitBy(data, '|');
                recentFiles = new Vector();
                for (int i = 0;
                     i < parts.length; i++) {
                    if (parts[i].trim().length() > 0)
                        recentFiles.addElement(
                            parts[i]);
                }
            }
            rs.closeRecordStore();
        } catch (Exception e) {}
    }

    // =============================================
    // ABOUT
    // =============================================

    private void showAbout() {
        Form about = new Form("About J2ME IDE");

        about.append(new StringItem("",
            "J2ME IDE v1.1\n" +
            "=============\n\n"));

        about.append(new StringItem(
            "Vendor:", " DASH ANIMATION V2\n\n"));

        // Clickable YouTube
        about.append(new StringItem(
            "YouTube:", "\n"));
        final StringItem ytItem = new StringItem(
            null,
            "youtube.com/@dash______animationv2",
            Item.HYPERLINK);
        ytItem.setDefaultCommand(new Command(
            "Open YouTube", Command.ITEM, 1));
        ytItem.setItemCommandListener(
            new ItemCommandListener() {
                public void commandAction(
                        Command c, Item item) {
                    openBrowser(
                        "https://youtube.com/" +
                        "@dash______animationv2");
                }
            });
        about.append(ytItem);
        about.append(new StringItem("","\n\n"));

        // Clickable GitHub
        about.append(new StringItem("GitHub:","\n"));
        final StringItem ghItem = new StringItem(
            null,
            "github.com/Dahmalahi/",
            Item.HYPERLINK);
        ghItem.setDefaultCommand(new Command(
            "Open GitHub", Command.ITEM, 2));
        ghItem.setItemCommandListener(
            new ItemCommandListener() {
                public void commandAction(
                        Command c, Item item) {
                    openBrowser(
                        "https://github.com/" +
                        "Dahmalahi/");
                }
            });
        about.append(ghItem);
        about.append(new StringItem("","\n\n"));

        about.append(new StringItem(
            "Platform:",
            " MIDP 2.0 / CLDC 1.1\n\n"));

        if (settingDevName.length() > 0) {
            about.append(new StringItem(
                "Developer:",
                " " + settingDevName + "\n\n"));
        }

        if (recentFiles.size() > 0) {
            about.append(new StringItem(
                "Recent:", "\n"));
            for (int i = 0;
                 i < recentFiles.size(); i++) {
                about.append(new StringItem(
                    " " + (i+1) + ".",
                    " " + recentFiles.elementAt(i)
                    + "\n"));
            }
            about.append(new StringItem("","\n"));
        }

        about.append(new StringItem(
            "Hints count:", " " +
            J2MEEditor.ERROR_PATTERNS.length +
            " patterns\n\n"));

        about.append(new StringItem(
            "Version:", " 1.1.0\n"));
        about.append(new StringItem(
            "License:", " Free to use\n\n"));

        Command back = new Command(
            "Back", Command.BACK, 1);
        about.addCommand(back);
        about.setCommandListener(
            new CommandListener() {
                public void commandAction(
                        Command c, Displayable d) {
                    display.setCurrent(mainMenu);
                }
            });
        display.setCurrent(about);
    }

    private void openBrowser(String url) {
        try {
            platformRequest(url);
        } catch (Exception e) {
            showAlert("Link",
                "Open manually:\n" + url,
                AlertType.INFO,
                display.getCurrent());
        }
    }

    // =============================================
    // SETTINGS
    // =============================================

    private void showSettings() {
        final Form form = new Form("Settings");

        // Dev Name
        final TextField tfDev = new TextField(
            "Developer Name:",
            settingDevName, 60, TextField.ANY);
        form.append(tfDev);
        form.append(new StringItem("",
            "Appears in file headers\n" +
            "and generated manifests.\n\n"));

        // Hints system
        final ChoiceGroup cgHints =
            new ChoiceGroup("Code Hints / Tips",
                ChoiceGroup.EXCLUSIVE);
        cgHints.append("On  (recommended)", null);
        cgHints.append("Off",               null);
        cgHints.setSelectedIndex(settingHints, true);
        form.append(cgHints);
        form.append(new StringItem("",
            "Hints show J2ME tips and\n" +
            "flag incompatible code.\n" +
            "Toggle with * key in editor.\n\n"));

        // Font Size
        final ChoiceGroup cgFont =
            new ChoiceGroup("Font Size",
                ChoiceGroup.EXCLUSIVE);
        cgFont.append("Small",  null);
        cgFont.append("Medium", null);
        cgFont.append("Large",  null);
        cgFont.setSelectedIndex(settingFontSize, true);
        form.append(cgFont);

        // Theme
        final ChoiceGroup cgTheme =
            new ChoiceGroup("Theme",
                ChoiceGroup.EXCLUSIVE);
        cgTheme.append("Dark (VS Code Dark+)", null);
        cgTheme.append("Light (coming soon)",  null);
        cgTheme.setSelectedIndex(settingTheme, true);
        form.append(cgTheme);

        // Auto-Indent
        final ChoiceGroup cgIndent =
            new ChoiceGroup("Auto-Indent",
                ChoiceGroup.EXCLUSIVE);
        cgIndent.append("On",  null);
        cgIndent.append("Off", null);
        cgIndent.setSelectedIndex(
            settingAutoIndent, true);
        form.append(cgIndent);

        // Line Numbers
        final ChoiceGroup cgLines =
            new ChoiceGroup("Line Numbers",
                ChoiceGroup.EXCLUSIVE);
        cgLines.append("Show", null);
        cgLines.append("Hide", null);
        cgLines.setSelectedIndex(
            settingLineNumbers, true);
        form.append(cgLines);

        // Indent Size
        final ChoiceGroup cgTab =
            new ChoiceGroup("Indent Size",
                ChoiceGroup.EXCLUSIVE);
        cgTab.append("2 spaces", null);
        cgTab.append("4 spaces", null);
        cgTab.append("8 spaces", null);
        cgTab.setSelectedIndex(settingTabSize, true);
        form.append(cgTab);

        // Word Wrap
        final ChoiceGroup cgWrap =
            new ChoiceGroup("Word Wrap",
                ChoiceGroup.EXCLUSIVE);
        cgWrap.append("On",  null);
        cgWrap.append("Off", null);
        cgWrap.setSelectedIndex(settingWordWrap, true);
        form.append(cgWrap);

        // Auto-close Brackets
        final ChoiceGroup cgBracket =
            new ChoiceGroup("Auto-close Brackets",
                ChoiceGroup.EXCLUSIVE);
        cgBracket.append("On",  null);
        cgBracket.append("Off", null);
        cgBracket.setSelectedIndex(
            settingBracket, true);
        form.append(cgBracket);

        // Scroll Speed
        final ChoiceGroup cgScroll =
            new ChoiceGroup("Scroll Speed",
                ChoiceGroup.EXCLUSIVE);
        cgScroll.append("Normal", null);
        cgScroll.append("Fast",   null);
        cgScroll.setSelectedIndex(
            settingScrollSpeed, true);
        form.append(cgScroll);

        // Storage info
        form.append(new StringItem(
            "Storage Root:",
            "\n " + fm.getDetectedRoot() + "\n"));
        form.append(new StringItem(
            "Projects Dir:",
            "\n j2meprojects/\n"));
        form.append(new StringItem(
            "Snippets Dir:",
            "\n j2mesnippets/\n"));
        form.append(new StringItem(
            "Key Reference:", "\n" +
            " 5=TextBox  0=NewLine\n" +
            " 1=Home     3=End\n" +
            " 7=PageUp   9=PageDown\n" +
            " 2=LineUp   8=LineDown\n" +
            " *=Hints    #=FindNext\n"));

        Command cmdSave  = new Command(
            "Save",  Command.OK,   1);
        Command cmdBack  = new Command(
            "Back",  Command.BACK, 2);
        Command cmdReset = new Command(
            "Reset", Command.ITEM, 3);
        form.addCommand(cmdSave);
        form.addCommand(cmdBack);
        form.addCommand(cmdReset);

        form.setCommandListener(new CommandListener() {
            public void commandAction(
                    Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    settingDevName =
                        tfDev.getString().trim();
                    settingHints =
                        cgHints.getSelectedIndex();
                    settingFontSize =
                        cgFont.getSelectedIndex();
                    settingTheme =
                        cgTheme.getSelectedIndex();
                    settingAutoIndent =
                        cgIndent.getSelectedIndex();
                    settingLineNumbers =
                        cgLines.getSelectedIndex();
                    settingTabSize =
                        cgTab.getSelectedIndex();
                    settingWordWrap =
                        cgWrap.getSelectedIndex();
                    settingBracket =
                        cgBracket.getSelectedIndex();
                    settingScrollSpeed =
                        cgScroll.getSelectedIndex();
                    saveSettingsToRMS();
                    saveDevName();
                    applySettings();
                    showAlert("Settings",
                        "Saved!\nDev: " +
                        settingDevName,
                        AlertType.CONFIRMATION,
                        mainMenu);
                } else if (c.getLabel()
                        .equals("Reset")) {
                    resetSettings();
                    showSettings();
                } else {
                    display.setCurrent(mainMenu);
                }
            }
        });
        display.setCurrent(form);
    }

    // =============================================
    // RMS
    // =============================================

    private void saveSettingsToRMS() {
        try {
            javax.microedition.rms.RecordStore rs =
                javax.microedition.rms.RecordStore
                    .openRecordStore(RMS_SETTINGS,true);
            StringBuffer sb = new StringBuffer();
            sb.append(settingFontSize);    sb.append(',');
            sb.append(settingTheme);       sb.append(',');
            sb.append(settingAutoIndent);  sb.append(',');
            sb.append(settingLineNumbers); sb.append(',');
            sb.append(settingTabSize);     sb.append(',');
            sb.append(settingWordWrap);    sb.append(',');
            sb.append(settingBracket);     sb.append(',');
            sb.append(settingScrollSpeed); sb.append(',');
            sb.append(settingHints);
            byte[] b = sb.toString().getBytes();
            if (rs.getNumRecords() == 0)
                rs.addRecord(b, 0, b.length);
            else
                rs.setRecord(1, b, 0, b.length);
            rs.closeRecordStore();
        } catch (Exception e) {}
    }

    private void loadSettingsFromRMS() {
        try {
            javax.microedition.rms.RecordStore rs =
                javax.microedition.rms.RecordStore
                    .openRecordStore(RMS_SETTINGS,false);
            if (rs.getNumRecords() > 0) {
                String   data =
                    new String(rs.getRecord(1));
                String[] p    = splitBy(data, ',');
                if (p.length >= 8) {
                    settingFontSize    =
                        Integer.parseInt(p[0]);
                    settingTheme       =
                        Integer.parseInt(p[1]);
                    settingAutoIndent  =
                        Integer.parseInt(p[2]);
                    settingLineNumbers =
                        Integer.parseInt(p[3]);
                    settingTabSize     =
                        Integer.parseInt(p[4]);
                    settingWordWrap    =
                        Integer.parseInt(p[5]);
                    settingBracket     =
                        Integer.parseInt(p[6]);
                    settingScrollSpeed =
                        Integer.parseInt(p[7]);
                }
                if (p.length >= 9) {
                    settingHints =
                        Integer.parseInt(p[8]);
                }
            }
            rs.closeRecordStore();
        } catch (Exception e) {}

        try {
            javax.microedition.rms.RecordStore rs =
                javax.microedition.rms.RecordStore
                    .openRecordStore(RMS_DEVNAME,false);
            if (rs.getNumRecords() > 0) {
                settingDevName =
                    new String(rs.getRecord(1));
            }
            rs.closeRecordStore();
        } catch (Exception e) {}
    }

    private void saveDevName() {
        try {
            javax.microedition.rms.RecordStore rs =
                javax.microedition.rms.RecordStore
                    .openRecordStore(RMS_DEVNAME,true);
            byte[] b = settingDevName.getBytes();
            if (rs.getNumRecords() == 0)
                rs.addRecord(b, 0, b.length);
            else
                rs.setRecord(1, b, 0, b.length);
            rs.closeRecordStore();
        } catch (Exception e) {}
    }

    private void resetSettings() {
        settingFontSize    = 0;
        settingTheme       = 0;
        settingAutoIndent  = 0;
        settingLineNumbers = 0;
        settingTabSize     = 1;
        settingWordWrap    = 1;
        settingBracket     = 0;
        settingScrollSpeed = 0;
        settingHints       = 0;
        saveSettingsToRMS();
    }

    // =============================================
    // SNIPPETS BODIES
    // =============================================

    private String getSnippetCode(int idx) {
        switch (idx) {
            case 0:  return snipHelloMIDlet();
            case 1:  return snipCanvas();
            case 2:  return snipHTTP();
            case 3:  return snipTimer();
            case 4:  return snipAlert();
            case 5:  return snipTextBox();
            case 6:  return snipGauge();
            case 7:  return snipList();
            case 8:  return snipRMS();
            case 9:  return snipSprite();
            case 10: return snipSound();
            case 11: return snipVideo();
            case 12: return snipBluetooth();
            case 13: return snipUDP();
            case 14: return snipCustomItem();
            case 15: return snipFileRead();
            case 16: return snipFileWrite();
            case 17: return snipThread();
            case 18: return snipMath();
            case 19: return snipGameLoop();
            case 20: return snipSMS();
            case 21: return snipVibrate();
            case 22: return snipDateField();
            case 23: return snipChoiceGroup();
            case 24: return snipTicker();
            default: return "// snippet\n";
        }
    }

    private String snipHelloMIDlet() {
        return
            "public class Hello extends MIDlet\n" +
            "        implements CommandListener {\n" +
            "    private Display display;\n" +
            "    private Form    form;\n" +
            "    private Command cmdExit;\n" +
            "    public void startApp() {\n" +
            "        display=Display.getDisplay(this);\n" +
            "        form=new Form(\"Hello\");\n" +
            "        form.append(\"Hello World!\");\n" +
            "        cmdExit=new Command(\n" +
            "            \"Exit\",Command.EXIT,1);\n" +
            "        form.addCommand(cmdExit);\n" +
            "        form.setCommandListener(this);\n" +
            "        display.setCurrent(form);\n" +
            "    }\n" +
            "    public void pauseApp(){}\n" +
            "    public void destroyApp(boolean u){}\n" +
            "    public void commandAction(\n" +
            "            Command c,Displayable d){\n" +
            "        destroyApp(false);\n" +
            "        notifyDestroyed();\n" +
            "    }\n" +
            "}\n";
    }

    private String snipCanvas() {
        return
            "class MyCanvas extends Canvas {\n" +
            "    protected void paint(Graphics g) {\n" +
            "        g.setColor(0x000000);\n" +
            "        g.fillRect(0,0,\n" +
            "            getWidth(),getHeight());\n" +
            "        g.setColor(0xFFFFFF);\n" +
            "        g.drawString(\"Canvas!\",\n" +
            "            getWidth()/2,getHeight()/2,\n" +
            "            Graphics.HCENTER|\n" +
            "            Graphics.BASELINE);\n" +
            "    }\n" +
            "    protected void keyPressed(int k){\n" +
            "        repaint();\n" +
            "    }\n" +
            "}\n";
    }

    private String snipHTTP() {
        return
            "HttpConnection hc=null;\n" +
            "InputStream is=null;\n" +
            "try {\n" +
            "    hc=(HttpConnection)\n" +
            "        Connector.open(\n" +
            "        \"http://example.com\");\n" +
            "    hc.setRequestMethod(\n" +
            "        HttpConnection.GET);\n" +
            "    int code=hc.getResponseCode();\n" +
            "    is=hc.openInputStream();\n" +
            "} catch(Exception e){\n" +
            "} finally {\n" +
            "    try{if(is!=null)is.close();}\n" +
            "    catch(Exception ig){}\n" +
            "    try{if(hc!=null)hc.close();}\n" +
            "    catch(Exception ig){}\n" +
            "}\n";
    }

    private String snipTimer() {
        return
            "import java.util.Timer;\n" +
            "import java.util.TimerTask;\n\n" +
            "Timer t=new Timer();\n" +
            "t.schedule(new TimerTask(){\n" +
            "    public void run(){\n" +
            "        // every 1000ms\n" +
            "    }\n" +
            "},0,1000);\n";
    }

    private String snipAlert() {
        return
            "Alert a=new Alert(\n" +
            "    \"Title\",\"Message\",\n" +
            "    null,AlertType.INFO);\n" +
            "a.setTimeout(Alert.FOREVER);\n" +
            "display.setCurrent(a,next);\n";
    }

    private String snipTextBox() {
        return
            "TextBox tb=new TextBox(\n" +
            "    \"Input\",\"\",256,\n" +
            "    TextField.ANY);\n" +
            "tb.addCommand(new Command(\n" +
            "    \"OK\",Command.OK,1));\n" +
            "tb.setCommandListener(this);\n" +
            "display.setCurrent(tb);\n";
    }

    private String snipGauge() {
        return
            "Gauge g=new Gauge(\n" +
            "    \"Loading\",false,100,0);\n" +
            "form.append(g);\n" +
            "g.setValue(50);\n";
    }

    private String snipList() {
        return
            "List list=new List(\n" +
            "    \"Menu\",List.IMPLICIT);\n" +
            "list.append(\"Item 1\",null);\n" +
            "list.append(\"Item 2\",null);\n" +
            "list.setCommandListener(this);\n" +
            "display.setCurrent(list);\n";
    }

    private String snipRMS() {
        return
            "RecordStore rs=null;\n" +
            "try {\n" +
            "    rs=RecordStore\n" +
            "        .openRecordStore(\"s\",true);\n" +
            "    byte[] d=\"val\".getBytes();\n" +
            "    if(rs.getNumRecords()==0)\n" +
            "        rs.addRecord(d,0,d.length);\n" +
            "    else\n" +
            "        rs.setRecord(1,d,0,d.length);\n" +
            "} finally {\n" +
            "    try{if(rs!=null)\n" +
            "        rs.closeRecordStore();}\n" +
            "    catch(Exception ig){}\n" +
            "}\n";
    }

    private String snipSprite() {
        return
            "import javax.microedition.lcdui.game.*;\n" +
            "Image sh=Image.createImage(\n" +
            "    \"/sprite.png\");\n" +
            "Sprite sp=new Sprite(sh,32,32);\n" +
            "sp.setPosition(x,y);\n" +
            "sp.nextFrame();\n" +
            "LayerManager lm=new LayerManager();\n" +
            "lm.append(sp);\n" +
            "lm.paint(g,0,0);\n";
    }

    private String snipSound() {
        return
            "import javax.microedition.media.*;\n" +
            "InputStream is=getClass()\n" +
            "    .getResourceAsStream(\"/s.mid\");\n" +
            "Player p=Manager.createPlayer(\n" +
            "    is,\"audio/midi\");\n" +
            "p.realize(); p.prefetch();\n" +
            "p.start();\n";
    }

    private String snipVideo() {
        return
            "import javax.microedition.media.*;\n" +
            "import javax.microedition.media" +
            ".control.*;\n" +
            "Player p=Manager.createPlayer(\n" +
            "    \"rtsp://example.com/v.3gp\");\n" +
            "p.realize();\n" +
            "VideoControl vc=(VideoControl)\n" +
            "    p.getControl(\"VideoControl\");\n" +
            "Item i=(Item)vc.initDisplayMode(\n" +
            "    VideoControl.USE_GUI_PRIMITIVE,null);\n" +
            "form.append(i); p.start();\n";
    }

    private String snipBluetooth() {
        return
            "import javax.bluetooth.*;\n" +
            "LocalDevice ld=\n" +
            "    LocalDevice.getLocalDevice();\n" +
            "ld.setDiscoverable(\n" +
            "    DiscoveryAgent.GIAC);\n" +
            "ld.getDiscoveryAgent()\n" +
            "    .startInquiry(\n" +
            "    DiscoveryAgent.GIAC,listener);\n";
    }

    private String snipUDP() {
        return
            "UDPDatagramConnection c=\n" +
            "    (UDPDatagramConnection)\n" +
            "    Connector.open(\n" +
            "    \"datagram://host:1234\");\n" +
            "byte[] b=\"ping\".getBytes();\n" +
            "c.send(c.newDatagram(b,b.length));\n" +
            "Datagram r=c.newDatagram(256);\n" +
            "c.receive(r); c.close();\n";
    }

    private String snipCustomItem() {
        return
            "class MyItem extends CustomItem{\n" +
            "    public MyItem(){super(\"Item\");}\n" +
            "    protected int getMinContentWidth(){\n" +
            "        return 80;}\n" +
            "    protected int getMinContentHeight(){\n" +
            "        return 30;}\n" +
            "    protected int getPrefContentWidth(\n" +
            "        int h){return 120;}\n" +
            "    protected int getPrefContentHeight(\n" +
            "        int w){return 40;}\n" +
            "    protected void paint(Graphics g,\n" +
            "            int w,int h){\n" +
            "        g.setColor(0x007ACC);\n" +
            "        g.fillRoundRect(0,0,w,h,8,8);\n" +
            "    }\n" +
            "}\n";
    }

    private String snipFileRead() {
        return
            "FileConnection fc=(FileConnection)\n" +
            "    Connector.open(\n" +
            "    \"file:///Card/t.txt\",\n" +
            "    Connector.READ);\n" +
            "if(fc.exists()){\n" +
            "    InputStream is=\n" +
            "        fc.openInputStream();\n" +
            "    byte[] b=new byte[512];\n" +
            "    int n=is.read(b);\n" +
            "    String s=new String(b,0,n);\n" +
            "    is.close();\n" +
            "}\n" +
            "fc.close();\n";
    }

    private String snipFileWrite() {
        return
            "FileConnection fc=(FileConnection)\n" +
            "    Connector.open(\n" +
            "    \"file:///Card/t.txt\",\n" +
            "    Connector.READ_WRITE);\n" +
            "if(!fc.exists()) fc.create();\n" +
            "else fc.truncate(0);\n" +
            "OutputStream os=\n" +
            "    fc.openOutputStream();\n" +
            "os.write(\"Hello\".getBytes());\n" +
            "os.flush(); os.close();\n" +
            "fc.close();\n";
    }

    private String snipThread() {
        return
            "new Thread(new Runnable(){\n" +
            "    public void run(){\n" +
            "        // background work\n" +
            "    }\n" +
            "}).start();\n";
    }

    private String snipMath() {
        return
            "import java.util.Random;\n" +
            "Random r=new Random();\n" +
            "int n=r.nextInt(100);\n" +
            "double sq=Math.sqrt(16);\n" +
            "double pw=Math.pow(2,8);\n" +
            "double sin=Math.sin(Math.PI/2);\n" +
            "int abs=Math.abs(-5);\n" +
            "int max=Math.max(3,7);\n";
    }

    private String snipGameLoop() {
        return
            "boolean running=true;\n" +
            "new Thread(new Runnable(){\n" +
            "    public void run(){\n" +
            "        long prev=\n" +
            "            System.currentTimeMillis();\n" +
            "        while(running){\n" +
            "            long now=\n" +
            "                System.currentTimeMillis();\n" +
            "            long dt=now-prev;prev=now;\n" +
            "            update(dt);\n" +
            "            canvas.repaint();\n" +
            "            canvas.serviceRepaints();\n" +
            "            try{Thread.sleep(\n" +
            "                Math.max(0,33-dt));}\n" +
            "            catch(Exception e){}\n" +
            "        }\n" +
            "    }\n" +
            "}).start();\n";
    }

    // =============================================
    // RECENT FILES SCREEN
    // =============================================

    private void showRecentFiles() {
        if (recentFiles.size() == 0) {
            showAlert("Recent Files",
                "No recent projects yet.\n" +
                "Open or create a project first.",
                AlertType.INFO, mainMenu);
            return;
        }
        final String[] names =
            new String[recentFiles.size()];
        recentFiles.copyInto(names);

        final List list = new List(
            "Recent Files", List.IMPLICIT);
        for (int i = 0; i < names.length; i++) {
            list.append(names[i], null);
        }
        Command back = new Command(
            "Back", Command.BACK, 2);
        Command open = new Command(
            "Open", Command.OK,   1);
        Command clr  = new Command(
            "Clear All", Command.ITEM, 3);
        list.addCommand(back);
        list.addCommand(open);
        list.addCommand(clr);
        list.setCommandListener(
            new CommandListener() {
                public void commandAction(
                        Command c, Displayable d) {
                    if (c.getCommandType() ==
                            Command.BACK) {
                        display.setCurrent(mainMenu);
                        return;
                    }
                    if (c.getLabel()
                            .equals("Clear All")) {
                        recentFiles = new Vector();
                        saveRecentFiles();
                        display.setCurrent(mainMenu);
                        return;
                    }
                    int idx = list.getSelectedIndex();
                    if (idx >= 0 && idx < names.length)
                        openProject(names[idx]);
                }
            });
        display.setCurrent(list);
    }

    // =============================================
    // TODO LIST
    // =============================================

    private void showTodoList() {
        String code = editor.getCode();
        String fname = editor.getFileName();

        Form form = new Form("TODO List");
        form.append(new StringItem(
            "File: ", fname + "\n\n"));

        String[] lines = splitBy(code, '\n');
        int count = 0;
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i];
            int ti = indexOfIgnoreCase(l, "// todo");
            int fi = indexOfIgnoreCase(l, "// fixme");
            int hi = indexOfIgnoreCase(l, "// hack");
            if (ti != -1 || fi != -1 || hi != -1) {
                String tag = (fi != -1) ? "FIXME"
                           : (hi != -1) ? "HACK"
                           : "TODO";
                String text = l.trim();
                if (text.length() > 50)
                    text = text.substring(0, 50)
                        + "...";
                form.append(new StringItem(
                    tag + " L" + (i + 1) + ":",
                    " " + text + "\n"));
                count++;
            }
        }
        if (count == 0) {
            form.append(new StringItem("",
                "No TODO / FIXME / HACK\n" +
                "comments found.\n\n" +
                "Great - clean code!\n"));
        } else {
            form.append(new StringItem(
                "\nTotal:", " " + count +
                " item(s)\n"));
        }

        Command back = new Command(
            "Back", Command.BACK, 1);
        form.addCommand(back);
        form.setCommandListener(
            new CommandListener() {
                public void commandAction(
                        Command c, Displayable d) {
                    display.setCurrent(mainMenu);
                }
            });
        display.setCurrent(form);
    }

    private int indexOfIgnoreCase(
            String src, String pat) {
        if (src == null || pat == null) return -1;
        String sl = src.toLowerCase();
        String pl = pat.toLowerCase();
        return sl.indexOf(pl);
    }

    // =============================================
    // CODE STATS
    // =============================================

    private void showCodeStats() {
        String code  = editor.getCode();
        String fname = editor.getFileName();

        int chars   = code.length();
        int lines   = 1;
        int blank   = 0;
        int comment = 0;
        int methods = 0;
        int classes = 0;
        int imports = 0;
        int todos   = 0;

        String[] lns = splitBy(code, '\n');
        lines = lns.length;
        for (int i = 0; i < lns.length; i++) {
            String t = lns[i].trim();
            if (t.length() == 0) {
                blank++;
            } else if (t.startsWith("//") ||
                       t.startsWith("/*") ||
                       t.startsWith("*")) {
                comment++;
            }
            if (t.startsWith("import ")) imports++;
            if (t.indexOf("void ")   != -1 ||
                t.indexOf("int ")    != -1 ||
                t.indexOf("String ") != -1 ||
                t.indexOf("boolean ") != -1) {
                if (t.indexOf("(") != -1 &&
                    t.indexOf(")") != -1 &&
                    t.indexOf("{") != -1) {
                    methods++;
                }
            }
            if ((t.indexOf("class ") != -1 ||
                 t.indexOf("interface ") != -1) &&
                t.indexOf("{") != -1) {
                classes++;
            }
            if (indexOfIgnoreCase(t, "// todo") != -1 ||
                indexOfIgnoreCase(t, "// fixme") != -1) {
                todos++;
            }
        }

        int code_lines = lines - blank - comment;

        Form form = new Form("Code Stats");
        form.append(new StringItem(
            "File:", " " + fname + "\n\n"));
        form.append(new StringItem(
            "Total lines:", " " + lines + "\n"));
        form.append(new StringItem(
            "Code lines:", " " + code_lines + "\n"));
        form.append(new StringItem(
            "Blank lines:", " " + blank + "\n"));
        form.append(new StringItem(
            "Comments:", " " + comment + "\n"));
        form.append(new StringItem(
            "Imports:", " " + imports + "\n"));
        form.append(new StringItem(
            "Classes/Ifaces:", " " + classes + "\n"));
        form.append(new StringItem(
            "Methods (est.):", " " + methods + "\n"));
        form.append(new StringItem(
            "TODO/FIXME:", " " + todos + "\n"));
        form.append(new StringItem(
            "Characters:", " " + chars + "\n"));
        form.append(new StringItem(
            "Est. file size:", " ~" +
            (chars / 1024) + "." +
            ((chars % 1024) * 10 / 1024) +
            " KB\n"));

        Command back = new Command(
            "Back", Command.BACK, 1);
        form.addCommand(back);
        form.setCommandListener(
            new CommandListener() {
                public void commandAction(
                        Command c, Displayable d) {
                    display.setCurrent(mainMenu);
                }
            });
        display.setCurrent(form);
    }

    private String snipSMS() {
        return
            "// JSR-120 Wireless Messaging\n" +
            "import javax.wireless.messaging.*;\n" +
            "MessageConnection mc =\n" +
            "    (MessageConnection)\n" +
            "    Connector.open(\n" +
            "    \"sms://+1234567890\");\n" +
            "TextMessage msg =\n" +
            "    (TextMessage) mc.newMessage(\n" +
            "    MessageConnection.TEXT_MESSAGE);\n" +
            "msg.setPayloadText(\"Hello!\");\n" +
            "mc.send(msg);\n" +
            "mc.close();\n";
    }

    private String snipVibrate() {
        return
            "// Vibrate for 500ms\n" +
            "Display d =\n" +
            "    Display.getDisplay(this);\n" +
            "boolean ok = d.vibrate(500);\n" +
            "// Flash backlight for 1000ms\n" +
            "d.flashBacklight(1000);\n" +
            "// Check support first:\n" +
            "// if(d.isColor()) ...\n";
    }

    private String snipDateField() {
        return
            "import java.util.Date;\n" +
            "DateField df = new DateField(\n" +
            "    \"Pick date:\",\n" +
            "    DateField.DATE_TIME);\n" +
            "df.setDate(new Date());\n" +
            "form.append(df);\n" +
            "// Retrieve value:\n" +
            "Date chosen = df.getDate();\n";
    }

    private String snipChoiceGroup() {
        return
            "ChoiceGroup cg = new ChoiceGroup(\n" +
            "    \"Choose one:\",\n" +
            "    ChoiceGroup.EXCLUSIVE);\n" +
            "cg.append(\"Option A\", null);\n" +
            "cg.append(\"Option B\", null);\n" +
            "cg.append(\"Option C\", null);\n" +
            "cg.setSelectedIndex(0, true);\n" +
            "form.append(cg);\n" +
            "// Get choice:\n" +
            "int sel = cg.getSelectedIndex();\n" +
            "String label = cg.getString(sel);\n";
    }

    private String snipTicker() {
        return
            "// Scrolling ticker text\n" +
            "Ticker t = new Ticker(\n" +
            "    \"Welcome to my MIDlet! \");\n" +
            "// Attach to any Displayable:\n" +
            "form.setTicker(t);\n" +
            "// Or update text later:\n" +
            "t.setString(\"New message! \");\n";
    }

    // =============================================
    // UTILITY
    // =============================================

    private void doExit() {
        destroyApp(false);
        notifyDestroyed();
    }

    private void showAlert(String title, String msg,
                            AlertType type,
                            Displayable next) {
        Alert a = new Alert(title, msg, null, type);
        a.setTimeout(2000);
        display.setCurrent(a, next);
    }

    private String[] splitBy(String s, char delim) {
        Vector v     = new Vector();
        int    start = 0;
        for (int i = 0; i <= s.length(); i++) {
            if (i == s.length() ||
                s.charAt(i) == delim) {
                v.addElement(s.substring(start, i));
                start = i + 1;
            }
        }
        String[] arr = new String[v.size()];
        v.copyInto(arr);
        return arr;
    }

    private String stripJavaExt(String name) {
        if (name != null && name.endsWith(".java")) {
            return name.substring(0, name.length() - 5);
        }
        return (name != null) ? name : "";
    }

    private String sanitizeName(String raw) {
        StringBuffer sb  = new StringBuffer();
        boolean capNext  = true;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c==' '||c=='-'||c=='_') {
                capNext = true; continue;
            }
            boolean lo = (c>='a'&&c<='z');
            boolean up = (c>='A'&&c<='Z');
            boolean dg = (c>='0'&&c<='9');
            if (lo||up||dg) {
                if (capNext && lo)
                    c = (char)(c - 32);
                capNext = false;
                sb.append(c);
            }
        }
        if (sb.length() == 0) sb.append("MyApp");
        char f = sb.charAt(0);
        if (f>='0'&&f<='9') sb.insert(0,'A');
        return sb.toString();
    }
}