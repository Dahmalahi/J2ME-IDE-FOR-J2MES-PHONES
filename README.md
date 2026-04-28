<img width="128" height="128" alt="icon" src="https://github.com/user-attachments/assets/3c2a4ea5-cdba-471d-bb86-7c60bfd0d212" />
# J2ME IDE v1.0
**by DASH ANIMATION V2**

A full-featured Java MIDlet IDE that runs entirely on a J2ME device (MIDP 2.0 / CLDC 1.1). Write, organize, and manage J2ME source code directly on your phone — no PC required during editing.

---

## Features

### New Code
Create a new project from a template. The IDE scaffolds the full directory structure on your SD card:

```
j2meprojects/
  YourApp/
    src/         ← YourApp.java
    res/         ← resources (icon, etc.)
    META-INF/    ← MANIFEST.MF (auto-generated)
    YourApp.jad  ← descriptor (auto-generated)
```

**Available templates:**
- MIDlet + CommandListener
- Canvas Game (bouncing ball, game loop, key input)
- HTTP Connection (GET request with threading)
- RMS Storage (save/load with RecordStore)
- Blank File

### Open Project
Browse and open any project previously saved in the `j2meprojects/` directory. Loads the main `.java` file directly into the editor.

### Snippets
25 built-in code snippets covering the full J2ME API surface. View any snippet and optionally insert it at the cursor or save it to My Snippets.

| # | Snippet |
|---|---------|
| 1 | Hello MIDlet |
| 2 | Canvas Game |
| 3 | HTTP GET Request |
| 4 | Timer Task |
| 5 | Alert Dialog |
| 6 | TextBox Input |
| 7 | Gauge Progress |
| 8 | List Menu |
| 9 | Record Store (RMS) |
| 10 | Sprite Animation |
| 11 | Sound (MIDI) |
| 12 | Player (Video) |
| 13 | Bluetooth Discovery |
| 14 | UDP Datagram |
| 15 | Custom Item |
| 16 | FileConnection Read |
| 17 | FileConnection Write |
| 18 | Thread Worker |
| 19 | Math and Random |
| 20 | Basic Game Loop |

### My Snippets
Save your own reusable code blocks to the SD card (`j2mesnippets/`). Create, view, edit, delete, and insert custom snippets directly from the editor.

### Code Analyze
Scans the currently open file for J2ME compatibility issues — flags Java 5+ APIs, desktop-only classes, and other patterns that won't compile under CLDC 1.1 / MIDP 2.0.

### Settings
| Setting | Options |
|---------|---------|
| Developer Name | Appears in all file headers and manifests |
| Code Hints / Tips | On / Off |
| Font Size | Small / Medium / Large |
| Theme | Dark (VS Code Dark+) / Light |
| Auto-Indent | On / Off |
| Line Numbers | Show / Hide |
| Indent Size | 2 / 4 / 8 spaces |
| Word Wrap | On / Off |
| Auto-close Brackets | On / Off |
| Scroll Speed | Normal / Fast |

Settings are persisted via RMS and survive app restarts.

---

## Editor Key Reference

| Key | Action |
|-----|--------|
| `5` | Open TextBox editor (T9 / full keyboard) |
| `0` | New line |
| `1` | Jump to line start |
| `3` | Jump to line end |
| `7` | Page up |
| `9` | Page down |
| `2` | Move line up |
| `8` | Move line down |
| `*` | Toggle hints on/off |
| `#` | Find next occurrence |
| D-pad | Navigate cursor |
| Fire | New line |
| LSK | Commands menu |

---

## Hint System

The editor's hint system analyzes code as you type and shows inline indicators:

- **Yellow bar** — J2ME tip or best practice
- **Red line** — Incompatible or unsupported code detected
- **Green bar** — Correct J2ME code confirmed

Toggle hints with the `*` key or via Settings.

---

## TextBox Mode

Press `5` to open a full-screen TextBox for heavy editing. Supports T9 and full physical keyboards. From TextBox mode you can:
- Insert snippet templates directly
- Run Code Analyze on the current content
- Transfer content back to the canvas editor

---

## File Structure on Device

```
<SD Card>/
  j2meprojects/       ← all projects
    MyApp/
      src/MyApp.java
      res/
      META-INF/MANIFEST.MF
      MyApp.jad
  j2mesnippets/       ← custom snippets (.snip files)
```

The IDE auto-detects the best storage root, preferring the external SD card (`Card`, `MMC`, `SD`, `Ext`, `E:/`). Falls back to internal storage if no card is found.

---

## Workflow

J2ME IDE is focused on writing and organizing source code on-device. To build a runnable `.jar` you have two options:

### Option A — Compile on-device with J2ME SDK Mobile
If you have [J2ME SDK Mobile](https://ozuffy.blogspot.com) installed on your phone alongside this IDE:

1. Write your code in J2ME IDE
2. Save the project — files are stored in `j2meprojects/` on your SD card
3. Open J2ME SDK Mobile and point it at your `.java` source file
4. Press **Build** — the SDK will compile, preverify, and assemble the `.jar` + `.jad` fully on-device
5. Find the finished app in the SDK's `dist/` folder and install it

> **Note:** J2ME SDK Mobile may relaunch itself multiple times during the build process (compile → preverify → jar assembly). This is normal. Watch for **BUILD SUCCESSFUL** in the output screen.

### Option B — Compile on PC with Sun WTK
1. Write your code in J2ME IDE on your phone
2. Transfer the `.java` file(s) to your PC
3. Compile with **Sun WTK 2.5.2** (or a compatible toolchain)
4. Deploy the `.jar` + `.jad` back to your device

---

## Requirements

- **Platform:** MIDP 2.0 / CLDC 1.1
- **JSR-75:** FileConnection API (required for file read/write)
- **Storage:** SD card recommended; internal storage supported
- **Permissions:** `javax.microedition.io.Connector.file.read`, `javax.microedition.io.Connector.file.write`

---

## Project Info

| | |
|---|---|
| **Version** | 1.0.0 |
| **Vendor** | DASH ANIMATION V2 |
| **License** | Free to use |
| **YouTube** | [youtube.com/@dash______animationv2](https://youtube.com/@dash______animationv2) |
| **GitHub** | [github.com/Dahmalahi/](https://github.com/Dahmalahi/) |
