package fjwright.runreduce;

import javax.swing.text.StyleConstants;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class RRPreferences {
    static final boolean windowsOS = System.getProperty("os.name").startsWith("Windows");
    static final Preferences prefs = Preferences.userRoot().node("/fjwright/runreduce");  // cf. package name
    // On Windows, Java stores the preferences for this application in the registry under the key
    // Computer\HKEY_CURRENT_USER\Software\JavaSoft\Prefs\fjwright\runreduce.

    // Preference keys:
    static final String FONTSIZE = "fontSize";
    static final String AUTORUNVERSION = "autoRunVersion";
    static final String BOLDPROMPTS = "boldPrompts";
    static final String COLOUREDIO = "colouredIO";
    static final String TABBEDPANE = "tabbedPane";

    // colouredIOState values:
    enum ColouredIO {NONE, MODAL, REDFRONT}

    static final String NONE = "None";

    static int fontSize = Math.max(prefs.getInt(FONTSIZE, 12), 5);
    // in case a very small font size gets saved accidentally!
    // Minimum of 5 matches minimum set for font size SpinnerModel.
    static String autoRunVersion = prefs.get(AUTORUNVERSION, NONE);
    static boolean boldPromptsState = prefs.getBoolean(BOLDPROMPTS, false);
    static ColouredIO colouredIOIntent;
    static boolean tabbedPaneState = prefs.getBoolean(TABBEDPANE, false);

    static {
        try {
            colouredIOIntent = ColouredIO.valueOf(prefs.get(COLOUREDIO, ColouredIO.NONE.toString()));
        } catch (IllegalArgumentException e) {
            colouredIOIntent = ColouredIO.NONE;
        }
    }

    static ColouredIO colouredIOState = colouredIOIntent;

    static void save(String key, Object... values) {
        switch (key) {
            case FONTSIZE:
                prefs.putInt(FONTSIZE, fontSize = (int) values[0]);
                break;
            case AUTORUNVERSION:
                prefs.put(AUTORUNVERSION, autoRunVersion = (String) values[0]);
                break;
            case BOLDPROMPTS:
                prefs.putBoolean(BOLDPROMPTS, boldPromptsState);
                break;
            case COLOUREDIO:
                prefs.put(COLOUREDIO, (colouredIOIntent = (ColouredIO) values[0]).toString());
                // Update colouredIOState immediately unless switching to or from REDFRONT:
                if (colouredIOIntent != ColouredIO.REDFRONT && colouredIOState != ColouredIO.REDFRONT) {
                    colouredIOState = colouredIOIntent;
                    if (colouredIOState == ColouredIO.NONE) {
                        REDUCEOutputThread.inputAttributeSet = REDUCEOutputThread.outputAttributeSet = null;
                        StyleConstants.setForeground(REDUCEOutputThread.promptAttributeSet, null);
                    }
                }
                break;
            case TABBEDPANE:
                prefs.putBoolean(TABBEDPANE, tabbedPaneState);
                break;
            default:
                System.err.println("Attempt to save unexpected preference key: " + key);
        }
    }
}

/*
 * This class defines a list of commands to run different versions of REDUCE.
 */
class REDUCECommandList extends ArrayList<REDUCECommand> {
    REDUCECommandList copy() {
        REDUCECommandList reduceCommandList = new REDUCECommandList();
        for (REDUCECommand cmd : this) // Build a deep copy of cmd
            reduceCommandList.add(new REDUCECommand(cmd.version, cmd.versionRootDir, cmd.command));
        return reduceCommandList;
    }
}

/**
 * This class defines a template for REDUCEConfigurationDefaults and REDUCEConfiguration.
 */
abstract class REDUCEConfigurationType {
    String reduceRootDir;
    String packagesRootDir;
    REDUCECommandList reduceCommandList;
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
        if (RunREDUCE.debugPlatform) System.err.println("OS name: " + System.getProperty("os.name"));

        reduceCommandList = new REDUCECommandList();
        reduceRootDir = System.getenv("REDUCE");
        // $REDUCE below will be replaced by versionRootDir if set or reduceRootDir otherwise
        // before attempting to run REDUCE.
        if (RRPreferences.windowsOS) {
            // On Windows, all REDUCE directories should be found automatically in "/Program Files/Reduce".
            if (reduceRootDir == null) reduceRootDir = findREDUCERootDir();
            if (reduceRootDir == null) reduceRootDir = "";
            packagesRootDir = reduceRootDir;
            reduceCommandList.add(new REDUCECommand(CSL_REDUCE,
                    "",
                    "$REDUCE/lib/csl/reduce.exe",
                    "--nogui"));
            reduceCommandList.add(new REDUCECommand(PSL_REDUCE,
                    "",
                    "$REDUCE/lib/psl/psl/bpsl.exe",
                    "-td", "1000", "-f",
                    "$REDUCE/lib/psl/red/reduce.img"));
        } else {
            // This is appropriate for Ubuntu:
            reduceRootDir = "/usr/lib/reduce";
            packagesRootDir = "/usr/share/reduce";
            reduceCommandList.add(new REDUCECommand(CSL_REDUCE,
                    "",
                    "$REDUCE/cslbuild/csl/reduce",
                    "--nogui"));
            reduceCommandList.add(new REDUCECommand(PSL_REDUCE,
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

/**
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
        reduceCommandList = new REDUCECommandList();
        Preferences prefs = RRPreferences.prefs;
        reduceRootDir = prefs.get(REDUCE_ROOT_DIR, RunREDUCE.reduceConfigurationDefault.reduceRootDir);
        packagesRootDir = prefs.get(PACKAGES_ROOT_DIR, RunREDUCE.reduceConfigurationDefault.packagesRootDir);
        if (packagesRootDir.isEmpty()) packagesRootDir = RunREDUCE.reduceConfigurationDefault.packagesRootDir;

        try {
            if (prefs.nodeExists(REDUCE_VERSIONS)) {
                prefs = prefs.node(REDUCE_VERSIONS);
                for (String version : prefs.childrenNames()) {
                    // Get defaults:
                    REDUCECommand cmdDefault = null;
                    for (REDUCECommand cmd : RunREDUCE.reduceConfigurationDefault.reduceCommandList)
                        if (version.equals(cmd.version)) {
                            cmdDefault = cmd;
                            break;
                        }
                    if (cmdDefault == null) cmdDefault = new REDUCECommand(); // all fields ""
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
                    reduceCommandList.add(new REDUCECommand(version, versionRootDir, command));
                    prefs = prefs.parent();
                }
            } else
                reduceCommandList = RunREDUCE.reduceConfigurationDefault.reduceCommandList.copy();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method saves the reduceRootDir, packagesRootDir and runREDUCECommands fields as preferences.
     */
    void save() {
       Preferences prefs = RRPreferences.prefs;
        prefs.put(REDUCE_ROOT_DIR, reduceRootDir);
        prefs.put(PACKAGES_ROOT_DIR, packagesRootDir);
        // Remove all saved versions before saving current versions:
        try {
            prefs.node(REDUCE_VERSIONS).removeNode();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        prefs = prefs.node(REDUCE_VERSIONS);
        for (REDUCECommand cmd : reduceCommandList) {
            prefs = prefs.node(cmd.version);
            prefs.put(REDUCE_ROOT_DIR, cmd.versionRootDir);
            int commandLength = cmd.command.length;
            prefs.putInt(COMMAND_LENGTH, commandLength);
            prefs.put(COMMAND, commandLength > 0 ? cmd.command[0] : "");
            for (int i = 1; i < cmd.command.length; i++)
                prefs.put(ARG + i, cmd.command[i]);
            prefs = prefs.parent();
        }
    }
}
