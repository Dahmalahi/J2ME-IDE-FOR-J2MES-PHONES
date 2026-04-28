import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * FileManager.java
 * JSR-75 File System handler for J2ME IDE
 * Vendor: DASH ANIMATION V2
 */
public class FileManager {

    private static final String PROJECT_DIR  = "j2meprojects/";
    private static final String SRC_DIR      = "src/";
    private static final String RES_DIR      = "res/";
    private static final String SNIPPETS_DIR = "j2mesnippets/";

    private static final String MANIFEST_TPL =
        "MIDlet-1: {NAME}, /res/icon.png, {NAME}\r\n" +
        "MIDlet-Name: {NAME}\r\n" +
        "MIDlet-Vendor: {DEVNAME}\r\n" +
        "MIDlet-Version: 1.0.0\r\n" +
        "MIDlet-Description: My J2ME App\r\n" +
        "MicroEdition-Configuration: CLDC-1.1\r\n" +
        "MicroEdition-Profile: MIDP-2.0\r\n" +
        "MIDlet-Permissions: " +
        "javax.microedition.io.Connector.file.read," +
        " javax.microedition.io.Connector.file.write\r\n";

    private static final String JAD_TPL =
        "MIDlet-1: {NAME}, /res/icon.png, {NAME}\r\n" +
        "MIDlet-Name: {NAME}\r\n" +
        "MIDlet-Vendor: {DEVNAME}\r\n" +
        "MIDlet-Version: 1.0.0\r\n" +
        "MIDlet-Jar-URL: {NAME}.jar\r\n" +
        "MIDlet-Jar-Size: 0\r\n" +
        "MicroEdition-Configuration: CLDC-1.1\r\n" +
        "MicroEdition-Profile: MIDP-2.0\r\n" +
        "MIDlet-Permissions: " +
        "javax.microedition.io.Connector.file.read," +
        " javax.microedition.io.Connector.file.write\r\n";

    private String bestRoot = null;

    public FileManager() {
        bestRoot = detectBestRoot();
    }

    // =============================================
    // ROOT DETECTION
    // =============================================

    private String detectBestRoot() {
        String sdRoot    = null;
        String firstRoot = null;
        try {
            Enumeration roots =
                FileSystemRegistry.listRoots();
            while (roots.hasMoreElements()) {
                String root  =
                    (String) roots.nextElement();
                String lower = root.toLowerCase();
                if (firstRoot == null)
                    firstRoot = root;
                if (lower.indexOf("card") != -1 ||
                    lower.indexOf("mmc")  != -1 ||
                    lower.indexOf("sd")   != -1 ||
                    lower.indexOf("ext")  != -1 ||
                    lower.indexOf("e:/")  != -1) {
                    sdRoot = root;
                    break;
                }
            }
        } catch (Exception e) {}
        return (sdRoot != null) ? sdRoot : firstRoot;
    }

    public String getProjectBasePath() {
        if (bestRoot == null) return null;
        return "file:///" + bestRoot + PROJECT_DIR;
    }

    public String getSnippetsBasePath() {
        if (bestRoot == null) return null;
        return "file:///" + bestRoot + SNIPPETS_DIR;
    }

    public String getDetectedRoot() {
        return (bestRoot != null) ?
            bestRoot : "Not available";
    }

    // =============================================
    // DIRECTORY OPERATIONS
    // =============================================

    public boolean ensureDir(String fullUrl) {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(
                fullUrl, Connector.READ_WRITE);
            if (!fc.exists()) fc.mkdir();
            return true;
        } catch (Exception e) {
            return false;
        } finally { closeFC(fc); }
    }

    public boolean initProjectsDir() {
        String base = getProjectBasePath();
        if (base == null) return false;
        boolean ok = ensureDir(base);
        String snipBase = getSnippetsBasePath();
        if (snipBase != null) ensureDir(snipBase);
        return ok;
    }

    // =============================================
    // PROJECT CREATION
    // =============================================

    public boolean createProject(String name,
                                  String srcCode,
                                  String devName) {
        String base = getProjectBasePath();
        if (base == null) return false;
        if (!ensureDir(base))                   return false;
        String projRoot = base + name + "/";
        if (!ensureDir(projRoot))               return false;
        if (!ensureDir(projRoot + SRC_DIR))     return false;
        if (!ensureDir(projRoot + RES_DIR))     return false;
        if (!ensureDir(projRoot + "META-INF/")) return false;

        if (!writeFile(
            projRoot + SRC_DIR + name + ".java",
            srcCode)) return false;

        String vendor = (devName != null &&
            devName.length() > 0) ?
            devName : "DASH ANIMATION V2";

        String manifest = replaceToken(
            replaceToken(MANIFEST_TPL,"{NAME}",name),
            "{DEVNAME}", vendor);
        if (!writeFile(
            projRoot + "META-INF/MANIFEST.MF",
            manifest)) return false;

        String jad = replaceToken(
            replaceToken(JAD_TPL,"{NAME}",name),
            "{DEVNAME}", vendor);
        if (!writeFile(projRoot + name + ".jad", jad))
            return false;

        return true;
    }

    // =============================================
    // CUSTOM SNIPPETS
    // =============================================

    public boolean saveCustomSnippet(String name,
                                      String code) {
        String base = getSnippetsBasePath();
        if (base == null) return false;
        ensureDir(base);
        return writeFile(
            base + sanitizeFileName(name) + ".snip",
            code);
    }

    public String[] listCustomSnippets() {
        String base = getSnippetsBasePath();
        if (base == null) return new String[0];
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(
                base, Connector.READ);
            if (!fc.exists()) return new String[0];
            Enumeration en = fc.list("*.snip", false);
            Vector list    = new Vector();
            while (en.hasMoreElements()) {
                String entry =
                    (String) en.nextElement();
                if (entry.endsWith(".snip")) {
                    list.addElement(entry.substring(
                        0, entry.length() - 5));
                }
            }
            String[] result = new String[list.size()];
            list.copyInto(result);
            return result;
        } catch (Exception e) {
            return new String[0];
        } finally { closeFC(fc); }
    }

    public String loadCustomSnippet(String name) {
        String base = getSnippetsBasePath();
        if (base == null) return null;
        return readFile(
            base + sanitizeFileName(name) + ".snip");
    }

    public boolean deleteCustomSnippet(String name) {
        String base = getSnippetsBasePath();
        if (base == null) return false;
        return deleteFile(
            base + sanitizeFileName(name) + ".snip");
    }

    // =============================================
    // FILE READ / WRITE
    // =============================================

    public boolean writeFile(String fullUrl,
                              String content) {
        FileConnection fc = null;
        OutputStream   os = null;
        try {
            fc = (FileConnection) Connector.open(
                fullUrl, Connector.READ_WRITE);
            if (!fc.exists()) fc.create();
            else fc.truncate(0);
            os = fc.openOutputStream();
            os.write(content.getBytes());
            os.flush();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            closeOutStream(os);
            closeFC(fc);
        }
    }

    public String readFile(String fullUrl) {
        FileConnection fc = null;
        InputStream    is = null;
        try {
            fc = (FileConnection) Connector.open(
                fullUrl, Connector.READ);
            if (!fc.exists()) return null;
            is = fc.openInputStream();
            ByteArrayOutputStream baos =
                new ByteArrayOutputStream();
            byte[] buf = new byte[512];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return new String(baos.toByteArray());
        } catch (Exception e) {
            return null;
        } finally {
            closeInStream(is);
            closeFC(fc);
        }
    }

    public String[] listProjects() {
        String base = getProjectBasePath();
        if (base == null) return new String[0];
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(
                base, Connector.READ);
            if (!fc.exists()) return new String[0];
            Enumeration en   = fc.list("*", false);
            Vector      dirs = new Vector();
            while (en.hasMoreElements()) {
                String entry =
                    (String) en.nextElement();
                if (entry.endsWith("/")) {
                    dirs.addElement(entry.substring(
                        0, entry.length() - 1));
                }
            }
            String[] result = new String[dirs.size()];
            dirs.copyInto(result);
            return result;
        } catch (Exception e) {
            return new String[0];
        } finally { closeFC(fc); }
    }

    public boolean deleteFile(String fullUrl) {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(
                fullUrl, Connector.READ_WRITE);
            if (fc.exists()) fc.delete();
            return true;
        } catch (Exception e) {
            return false;
        } finally { closeFC(fc); }
    }

    public boolean exists(String fullUrl) {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(
                fullUrl, Connector.READ);
            return fc.exists();
        } catch (Exception e) {
            return false;
        } finally { closeFC(fc); }
    }

    // =============================================
    // HELPERS
    // =============================================

    public String replaceToken(String src,
                                String token,
                                String value) {
        StringBuffer sb  = new StringBuffer();
        int          idx = 0;
        int          pos;
        while ((pos = src.indexOf(token, idx)) != -1) {
            sb.append(src.substring(idx, pos));
            sb.append(value);
            idx = pos + token.length();
        }
        sb.append(src.substring(idx));
        return sb.toString();
    }

    private String sanitizeFileName(String name) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c>='a'&&c<='z')||(c>='A'&&c<='Z')||
                (c>='0'&&c<='9')||c=='_'||c=='-') {
                sb.append(c);
            } else if (c == ' ') {
                sb.append('_');
            }
        }
        if (sb.length() == 0) sb.append("snippet");
        return sb.toString();
    }

    private void closeFC(FileConnection fc) {
        if (fc != null) {
            try { fc.close(); } catch (Exception e) {}
        }
    }
    private void closeOutStream(OutputStream os) {
        if (os != null) {
            try { os.close(); } catch (Exception e) {}
        }
    }
    private void closeInStream(InputStream is) {
        if (is != null) {
            try { is.close(); } catch (Exception e) {}
        }
    }
}