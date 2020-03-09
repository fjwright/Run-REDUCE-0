# Run-REDUCE User Guide

### Francis Wright, March 2020

Run-REDUCE is an open-source Java GUI to run the REDUCE Computer
Algebra System.  REDUCE must be obtained from
[SourceForge](https://sourceforge.net/projects/reduce-algebra/) and
installed separately.  Run-REDUCE should find a standard REDUCE
installation automatically and not **require** any initial
configuration.

Run-REDUCE does **not** (yet) provide typeset-quality display of
mathematical notation.

Run-REDUCE currently runs on Microsoft Windows and Ubuntu Linux.  It
may run on other platforms, in particular other versions of Linux that
are similar to Ubuntu, but I can only test on 64-bit Windows 10 and
Ubuntu 18.

## Install and Run

You need to have a Java Runtime Environment (JRE), version 8 (or
later), installed.  You also need to download the file
`Run-REDUCE.jar`.  To do this, click on the `release` tab on the
GitHub project page, expand `Assets`, then click on `Run-REDUCE.jar`
and save it somewhere convenient, such as your home directory or the
directory in which you store your REDUCE projects.  You should then be
able to run Run-REDUCE as an executable file, e.g. by double-clicking
on it.  Alternatively, you can run Run-REDUCE by executing the shell
command

    java -jar Run-REDUCE.jar

in the directory containing the file.  This approach has the advantage
that any error messages will be displayed in the shell window.

Here is a bit more detail:

### On Microsoft Windows

You can install a suitable JRE from [java.com](https://www.java.com/).

An easy way to run Run-REDUCE using a shell command is first to open
File Explorer and navigate to the folder to which you downloaded
`Run-REDUCE.jar`.  In the address bar, type `cmd` and then press the
*Return* key.  This will open a Command Prompt window in the current
folder.

### On Ubuntu Linux

You can install a suitable JRE by opening a terminal window and
executing the command

    sudo apt install openjdk-8-jre

If you set `Run-REDUCE.jar` to be executable then you should be able
to run Run-REDUCE as an executable file, e.g. by double-clicking on
it.  An easy way to run Run-REDUCE using a shell command is first to
open Files and navigate to the directory to which you downloaded
`Run-REDUCE.jar`.  Right-click on this directory and select `Open in
Terminal`.

## General Information

Run-REDUCE remembers user preferences and uses them the next time it
runs.  It uses the standard Java package
[java.util.prefs](https://docs.oracle.com/en/java/javase/13/docs/api/java.prefs/java/util/prefs/package-summary.html),
which stores data persistently in an implementation-dependent backing
store.  For example, on Microsoft Windows the preferences for this
application are stored in the registry under the key
`Computer\HKEY_CURRENT_USER\Software\JavaSoft\Prefs\fjwright\runreduce`
and on Ubuntu Linux they are stored in the XML file
`~/.java/.userPrefs/fjwright/runreduce/prefs.xml`.

Run-REDUCE currently assumes a standard installation of REDUCE and
uses commands based on that assumption to run REDUCE.  If the
environment variable named `REDUCE` is set then Run-REDUCE uses its
value as the root of the REDUCE installation; a final directory
separator is optional.  On Microsoft Windows, you can use either a
backward or a forward slash as directory separator and case is not
significant.  For example, the following input to `cmd.exe` works
(although it is unnecessary) if you have installed REDUCE on your `D`
drive:

    set REDUCE=D:/Program Files/Reduce

Otherwise, Run-REDUCE looks in the standard places for a REDUCE
installation.  On Microsoft Windows it looks for the folder `\Program
Files\Reduce` on all accessible drives.  On Ubuntu Linux it assumes
that package information is under `/usr/share/reduce` and executable
files are under `/usr/lib/reduce`.

## The Main Window

The main window consists of two panes one above the other.  The top
pane displays a log of all the REDUCE input and output in the current
session.  This pane is read-only.  The bottom pane is an input editor
that supports all the standard keyboard and mouse-based editing
facilities normally provided by your platform.  Both panes display
vertical and horizontal scroll bars when appropriate; text does not
wrap.  The horizontal divider separating the two panes can be dragged
up and down, and it can be moved all the way up or down by clicking on
one of the two triangular icons at the left-hand side.

You type (or paste) REDUCE input into the input editor pane, edit it
as necessary, and then click on the `Send Input` button, which sends
the input to REDUCE and echos it in the top pane.  This clears the
input editor, but you can scroll through the previous input using the
`Earlier Input` and `Later Input` buttons.  You can edit previous
input recalled into the input editor as necessary and then send it to
REDUCE.  Input can be multi-line, in which case Run-REDUCE processes
all the lines together.

Sending input to REDUCE strips any trailing white space, adds a
semicolon if there was no final terminator, and then adds a final
newline.

## The File Menu

Some items in this menu pop up a dialogue that allows you to select
one or more items, such as filenames.  This dialog supports all the
standard keyboard and mouse-based selection facilities normally
provided by your platform.  In particular, in dialogues that allow
selection of multiple items, holding the `Control` key down while
clicking on an item selects or deselects it without affecting any
other selections, and holding a `Shift` key down while clicking on an
item extends the selection to that item.

Most of the menu items run a REDUCE command, which is echoed in the
display pane but does not appear in the input editor.  The file
selector dialogues all share the same default directory, which is
initially your home directory, but the last directory you used will be
the default directory the next time you use a file selector dialogue.

This default directory is independent of REDUCE's default directory,
which is irrelevant because Run-REDUCE always uses absolute file
paths.

The File menu provides the following items.

### Input from Files...

This brings up a file selector dialogue that allows you to input one
or more source code files into REDUCE using the REDUCE `IN` command.
By default, the file selector only shows `*.red`, `*.tst` and `*.txt`
files, but you can reset it to show all files.  (Note that the
recommended extension for REDUCE source files is `.red`.)  There is a
check box labelled `Echo` on the right-hand side of the file window,
which is selected by default.  Selecting this option causes file input
to be echoed to the `Input/Output Display` pane.  (Note that a REDUCE
source code file should end with `;end;` to avoid an error message.)

### Output to File...

This brings up a file selector dialogue that allows you to send output
to a file instead of the GUI using the REDUCE `OUT` command.  By
default, the file selector only shows `*.rlg` and `*.txt` files, but
you can reset it to show all files.  (Note that the recommended
extension for REDUCE output files is `.rlg`.)  Only one output file
can be selected at a time, but selecting a new output file redirects
output to that file without shutting the previous output file.  The
GUI remember all the open output files to facilitate shutting them
&ndash; see below.

### Output Here

This item redirects output to the GUI without shutting the current
output file using the REDUCE `OUT T` command.

### Shut Output Files...

This item is inactive unless there are open output files, in which
case it brings up a dialogue that allows you to select and shut one or
more of the open output files using the REDUCE `SHUT` command.  It
uses a special dialogue, not a file selector, and only shows open
output files.

### Shut Last Output File

This item is inactive unless there are open output files, in which
case it shuts the last used open output file using the REDUCE `SHUT`
command.

### Load Packages...

This item brings up a dialogue that allows you to select and load one
or more REDUCE packages using the REDUCE `LOAD_PACKAGE` command.  It
uses a special dialogue, not a file selector, and only shows standard
REDUCE packages, excluding those that are pre-loaded, sorted
alphabetically.  Run-REDUCE determines the list of packages each time
it starts up by reading the `package.map` file in the REDUCE
installation directory.

### Save Session Log...

This brings up a file selector dialogue that allows you to select a
file to which to save the current REDUCE session log, part of which is
displayed in the `Input/Output Display` pane.  By default, the file
selector only shows `*.rlg` and `*.txt` files, but you can reset it to
show all files.  There is a check box labelled `Append` on the
right-hand side of the packages window, which is deselected by
default.  Selecting this option appends the session log to the
selected file; otherwise, it overwrites any previous file content.

### Quit

This terminates both REDUCE and the Run-REDUCE GUI, as does the close
widget at the top right-hand corner of the main window frame.

## The REDUCE Menu

The REDUCE menu provides the following items.

### The Run REDUCE... Sub-menu

This provides an item for each configured version of REDUCE (based on
different versions of Lisp, namely CSL and PSL).  Clicking on a
version of REDUCE runs it.  (Currently, another version of REDUCE
cannot be run without restarting Run-REDUCE.)

### The Auto-run REDUCE? Checkbox

Selecting this option causes Run-REDUCE to run the selected version of
REDUCE (see below) automatically when it starts.  Run-REDUCE remembers
this option value and uses it the next time it runs.

### The Auto-run REDUCE... Sub-menu

This allows you to select the version of REDUCE to auto-run when
auto-run is enabled.  Run-REDUCE remembers this selection and uses it
the next time it runs.

## The Preferences Menu

The Preferences menu provides the following items.

## Font Size...

This brings up a dialogue box that allows you to change the font size
used in the `Input/Output Display` and `Input Editor` panes.
Run-REDUCE remembers the selected size and uses it the next time
Run-REDUCE runs.

## Coloured I/O? Checkbox

Selecting this option causes Run-REDUCE to use redfront-style text
colouring with red input and blue output.

## The Help Menu

The Help menu provides the following items.

### About Run-REDUCE

This pops up a dialogue displaying brief information about Run-REDUCE.
