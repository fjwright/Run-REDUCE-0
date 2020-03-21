package fjwright.runreduce;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class RunREDUCEPrefs {
    static boolean windowsOS = System.getProperty("os.name").startsWith("Windows");
    static final Preferences prefs = Preferences.userRoot().node("/fjwright/runreduce");  // cf. package name
    // On Windows, Java stores the preferences for this application in the registry under the key
    // Computer\HKEY_CURRENT_USER\Software\JavaSoft\Prefs\fjwright\runreduce.

    // Preference keys:
    static final String FONTSIZE = "fontSize";
    static final String AUTORUN = "autoRun";
    static final String AUTORUNVERSION = "autoRunVersion";
    static final String COLOUREDIO = "colouredIO";

    static int fontSize = (int) prefs.getFloat(FONTSIZE, 12);
    static boolean autoRunState = prefs.getBoolean(AUTORUN, false);
    static String autoRunVersion = prefs.get(AUTORUNVERSION, REDUCEConfigurationDefault.CSL_REDUCE);
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

/*
 * This class defines a list of commands to run different versions of REDUCE.
 */
class RunREDUCECommandList extends ArrayList<RunREDUCECommand> {
    RunREDUCECommandList copy() {
        RunREDUCECommandList runREDUCECommandList = new RunREDUCECommandList();
        for (RunREDUCECommand cmd : this) // Build a deep copy of cmd
            runREDUCECommandList.add(new RunREDUCECommand(cmd.version, cmd.versionRootDir, cmd.command));
        return runREDUCECommandList;
    }
}

/**
 * This class defines a template for REDUCEConfigurationDefaults and REDUCEConfiguration.
 */
abstract class REDUCEConfigurationType {
    String reduceRootDir;
    String packagesRootDir;
    RunREDUCECommandList runREDUCECommandList;
}

/*
 * This class represents the application default REDUCE directory and command configuration.
 * It is initialised when the application starts.
 * **Note that no value can be null because preference values cannot be null.**
 */
class REDUCEConfigurationDefault extends REDUCEConfigurationType {
    static final String CSL_REDUCE = "CSL REDUCE";
    static final String PSL_REDUCE = "PSL REDUCE";

    REDUCEConfigurationDefault() {
        runREDUCECommandList = new RunREDUCECommandList();
        reduceRootDir = System.getenv("REDUCE");
        // $REDUCE below will be replaced by versionRootDir if set or reduceRootDir otherwise
        // before attempting to run REDUCE.
        if (RunREDUCEPrefs.windowsOS) {
            // On Windows, all REDUCE directories should be found automatically in "/Program Files/Reduce".
            if (reduceRootDir == null) reduceRootDir = findREDUCERootDir();
            if (reduceRootDir == null) reduceRootDir = "";
            packagesRootDir = reduceRootDir;
            runREDUCECommandList.add(new RunREDUCECommand(CSL_REDUCE,
                    "",
                    "$REDUCE/lib/csl/reduce.exe",
                    "--nogui"));
            runREDUCECommandList.add(new RunREDUCECommand(PSL_REDUCE,
                    "",
                    "$REDUCE/lib/psl/psl/bpsl.exe",
                    "-td", "1000", "-f",
                    "$REDUCE/lib/psl/red/reduce.img"));
        } else {
            // This is appropriate for Ubuntu:
            reduceRootDir = "/usr/lib/reduce";
            packagesRootDir = "/usr/share/reduce";
            runREDUCECommandList.add(new RunREDUCECommand(CSL_REDUCE,
                    "",
                    "$REDUCE/cslbuild/csl/reduce",
                    "--nogui"));
            runREDUCECommandList.add(new RunREDUCECommand(PSL_REDUCE,
                    "",
                    "$REDUCE/pslbuild/psl/bpsl",
                    "-td", "1000", "-f",
                    "$REDUCE/pslbuild/red/reduce.img"));
        }
    }

    /**
     * This method attempts to locate the REDUCE installation directory on Windows (only).
     */
    private static String findREDUCERootDir() {
        Path targetPath = Paths.get("Program Files", "Reduce");
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            Path reduceRootPath = root.resolve(targetPath);
            if (Files.exists(reduceRootPath)) return reduceRootPath.toString();
        }
        return null;
    }
}

/*
 * This class represents the current REDUCE directory and command configuration.
 * It is initialised when the application starts and can be updated and saved using the REDUCEConfigDialog class.
 */
class REDUCEConfiguration extends REDUCEConfigurationType {
    // Preference keys:
    static final String REDUCE_ROOT_DIR = "reduceRootDir";
    static final String PACKAGES_ROOT_DIR = "packagesRootDir";
    static final String REDUCE_VERSIONS = "reduceVersions";
    static final String COMMAND_LENGTH = "commandLength";
    static final String COMMAND = "command";
    static final String ARG = "arg";

    /**
     * This method initialises the reduceRootDir, packagesRootDir and runREDUCECommands fields from saved preferences
     * or application defaults.
     */
    REDUCEConfiguration() {
        runREDUCECommandList = new RunREDUCECommandList();
        Preferences prefs = RunREDUCEPrefs.prefs;
        reduceRootDir = prefs.get(REDUCE_ROOT_DIR, RunREDUCE.reduceConfigurationDefault.reduceRootDir);
        packagesRootDir = prefs.get(PACKAGES_ROOT_DIR, RunREDUCE.reduceConfigurationDefault.packagesRootDir);

        try {
            if (prefs.nodeExists(REDUCE_VERSIONS)) {
                prefs = prefs.node(REDUCE_VERSIONS);
                for (String version : prefs.childrenNames()) {
                    // Get defaults:
                    RunREDUCECommand cmdDefault = null;
                    for (RunREDUCECommand cmd : RunREDUCE.reduceConfigurationDefault.runREDUCECommandList)
                        if (version.equals(cmd.version)) {
                            cmdDefault = cmd;
                            break;
                        }
                    if (cmdDefault == null) cmdDefault = new RunREDUCECommand(); // all fields ""
                    prefs = prefs.node(version);
                    String versionRootDir = prefs.get(REDUCE_ROOT_DIR, cmdDefault.versionRootDir);
                    int commandLength = prefs.getInt(COMMAND_LENGTH, cmdDefault.command.length);
                    String[] command;
                    if (commandLength == 0) {
                        command = new String[]{""};
                    } else {
                        command = new String[commandLength];
                        command[0] = prefs.get(COMMAND, cmdDefault.command[0]);
                        for (int i = 1; i < commandLength; i++) {
                            command[i] = prefs.get(ARG + i,
                                    i < cmdDefault.command.length ? cmdDefault.command[i] : "");
                        }
                    }
                    runREDUCECommandList.add(new RunREDUCECommand(version, versionRootDir, command));
                    prefs = prefs.parent();
                }
            } else
                runREDUCECommandList = RunREDUCE.reduceConfigurationDefault.runREDUCECommandList.copy();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method saves the reduceRootDir, packagesRootDir and runREDUCECommands fields as preferences.
     */
    void save() {
        Preferences prefs = RunREDUCEPrefs.prefs;
        prefs.put(REDUCE_ROOT_DIR, reduceRootDir);
        prefs.put(PACKAGES_ROOT_DIR, packagesRootDir);
        prefs = prefs.node(REDUCE_VERSIONS);
        for (RunREDUCECommand cmd : runREDUCECommandList) {
            prefs = prefs.node(cmd.version);
            prefs.put(REDUCE_ROOT_DIR, cmd.versionRootDir);
            int commandLength = cmd.command.length;
            prefs.putInt(COMMAND_LENGTH, commandLength);
            prefs.put(COMMAND, commandLength > 0 ? cmd.command[0] : "");
            int i;
            for (i = 1; i < cmd.command.length; i++)
                prefs.put(ARG + i, cmd.command[i]);
            // Delete any redundant saved args:
            for (; i <= REDUCEConfigDialog.nArgs; i++)
                prefs.remove(ARG + i);
            prefs = prefs.parent();
        }
    }
}
