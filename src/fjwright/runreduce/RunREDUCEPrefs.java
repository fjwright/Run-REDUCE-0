package fjwright.runreduce;

import java.util.ArrayList;
import java.util.prefs.Preferences;

public class RunREDUCEPrefs {
    static boolean windowsOS = System.getProperty("os.name").startsWith("Windows");
    static RunREDUCECommandDefaults runREDUCECommandDefaults = new RunREDUCECommandDefaults();

    static final Preferences prefs = Preferences.userRoot().node("/fjwright/runreduce");  // cf. package name
    // On Windows, the preferences for this app are stored in the registry under the key
    // Computer\HKEY_CURRENT_USER\Software\JavaSoft\Prefs\fjwright\runreduce

    // Preference keys for this package
    static final String REDUCE_ROOT_DIR = "REDUCE_root_dir";
    static final String FONTSIZE = "fontSize";
    static final String AUTORUN = "autoRun";
    static final String AUTORUNVERSION = "autoRunVersion";
    static final String COLOUREDIO = "colouredIO";

    static String reduceRootDir = prefs.get(REDUCE_ROOT_DIR, System.getenv("REDUCE"));
    static int fontSize = (int) prefs.getFloat(FONTSIZE, 12);
    static boolean autoRunState = prefs.getBoolean(AUTORUN, false);
    static String autoRunVersion = prefs.get(AUTORUNVERSION, runREDUCECommandDefaults.get(0).version);
    static boolean colouredIOState = prefs.getBoolean(COLOUREDIO, false);

    static void save(String key, Object... values) {
        switch (key) {
            case FONTSIZE:
                prefs.putFloat(FONTSIZE, (float) values[0]);
                break;
            case AUTORUN:
                prefs.putBoolean(AUTORUN, autoRunState);
                break;
            case AUTORUNVERSION:
                prefs.put(AUTORUNVERSION, (String) values[0]);
                break;
            case COLOUREDIO:
                prefs.putBoolean(COLOUREDIO, colouredIOState);
                break;
        }
    }
}

class RunREDUCECommandDefaults extends ArrayList<RunREDUCECommand> {
    RunREDUCECommandDefaults() {
        // $REDUCE will be replaced by the root of the REDUCE installation
        // before attempting to run REDUCE.
        if (RunREDUCEPrefs.windowsOS) {
            add(new RunREDUCECommand("CSL REDUCE",
                    null,
                    "$REDUCE/lib/csl/reduce.exe",
                    "--nogui"));
            add(new RunREDUCECommand("PSL REDUCE",
                    null,
                    "$REDUCE/lib/psl/psl/bpsl.exe",
                    "-td", "1000", "-f",
                    "$REDUCE/lib/psl/red/reduce.img"));
        } else {
            add(new RunREDUCECommand("CSL REDUCE",
                    "/usr/lib/reduce",
                    "$REDUCE/cslbuild/csl/reduce",
                    "--nogui"));
            add(new RunREDUCECommand("PSL REDUCE",
                    "/usr/lib/reduce",
                    "$REDUCE/pslbuild/psl/bpsl",
                    "-td", "1000", "-f",
                    "$REDUCE/pslbuild/red/reduce.img"));
        }
    }
}
