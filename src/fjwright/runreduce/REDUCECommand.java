package fjwright.runreduce;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines a command to run REDUCE and checks that it is executable.
 */
class REDUCECommand {
    String version = ""; // e.g. "CSL REDUCE" or "PSL REDUCE"
    String versionRootDir = ""; // version-specific reduceRootDir.
    String[] command = {"", "", "", "", "", ""}; // executable pathname followed by arguments

    REDUCECommand() {
    }

    REDUCECommand(String version, String versionRootDir, String... command) {
        this.version = version;
        this.versionRootDir = versionRootDir;
        this.command = command;
    }

    // Merge this method into the constructor?
    String[] buildCommand() {
        // Replace $REDUCE by versionRootDir if non-null else by reduceRootDir.
        Path reduceRootPath = Paths.get(
                !versionRootDir.equals("") ? versionRootDir : RunREDUCE.reduceConfiguration.reduceRootDir);
        String[] command = new String[this.command.length];
        for (int i = 0; i < this.command.length; i++) {
            String element = this.command[i];
            if (element.startsWith("$REDUCE/"))
                element = reduceRootPath.resolve(element.substring(8)).toString();
            command[i] = element;
        }
        if (!Files.isExecutable(Paths.get(command[0]))) {
            RunREDUCE.errorMessageDialog(
                    command[0] + " is not executable!",
                    "REDUCE Configuration Error");
            return null;
        }
        return command;
    }
}

/**
 * This class provides a list of all REDUCE packages by parsing the package.map file.
 * The list excludes preloaded packages, and it is sorted alphabetically.
 */
class REDUCEPackageList extends ArrayList<String> {

    REDUCEPackageList() {
        Path packagesRootPath = Paths.get(RunREDUCE.reduceConfiguration.packagesRootDir);
        Path packageMapFile = packagesRootPath.resolve("packages/package.map");
        if (!Files.isReadable(packageMapFile)) {
            RunREDUCE.errorMessageDialog(
                    new String[]{"The REDUCE package map file is not available!",
                            "Please correct 'Packages Root Dir' in the 'Configure REDUCE...' dialogue,",
                            "which will open automatically when you close this dialogue."},
                    "REDUCE Package Error");
            RRMenuBar.showREDUCEConfigDialog();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(packageMapFile)) {
            String line;
            Pattern pattern = Pattern.compile("\\s*\\((\\w+)");
            // The preloaded packages are these (using non-capturing groups):
            Pattern exclude = Pattern.compile("(?:alg)|(?:arith)|(?:mathpr)|(?:poly)|(?:rlisp)");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.lookingAt()) {
                    String pkg = matcher.group(1);
                    if (!exclude.matcher(pkg).matches()) this.add(pkg);
                }
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        Collections.sort(this);

        // For testing only:
//        for (String s : this) {
//            System.out.print(s);
//            System.out.print(" ");
//        }
    }
}
