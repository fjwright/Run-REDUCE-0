# Run-REDUCE User Guide

### Francis Wright, April 2020

Run-REDUCE is an open-source Java GUI to run the REDUCE Computer
Algebra System.  REDUCE must be obtained from
[SourceForge](https://sourceforge.net/projects/reduce-algebra/) and
installed separately.  Run-REDUCE should find a standard REDUCE
installation automatically and not **require** any initial
configuration, at least on Microsoft Windows and Ubuntu Linux.  With
suitable configuration is **should** run on any platform that supports
Java 8 (or later), but I can only test on 64-bit Windows 10 and Ubuntu 18.

Run-REDUCE does **not** (yet) provide typeset-quality display of
mathematical notation.

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

### Microsoft Windows

You can install a suitable Java 8 JRE from
[java.com](https://www.java.com/).

An easy way to run Run-REDUCE using a shell command is first to open
File Explorer and navigate to the folder to which you downloaded
`Run-REDUCE.jar`.  In the address bar, type `cmd` and then press the
*Return* key.  This will open a Command Prompt window in the current
folder.

### Ubuntu Linux

You can install a suitable JRE by opening a terminal window and
executing the command

    sudo apt install openjdk-11-jre

(Java 8 has some known bugs on Ubuntu 18, so I recommend using
Java 11.  After I upgraded to Java 11 I found that I also needed to
install `canberra-gtk-module`.)

If you set `Run-REDUCE.jar` to be executable then you should be able
to run Run-REDUCE as an executable file, e.g. by double-clicking on
it.  An easy way to run Run-REDUCE using a shell command is first to
open Files and navigate to the directory to which you downloaded
`Run-REDUCE.jar`.  Right-click on this directory and select `Open in
Terminal`.

### Other Platforms

[AdoptOpenJDK](https://adoptopenjdk.net/) provides "Prebuilt OpenJDK
Binaries for Free!" for Java 8 and later for most current platforms
including Linux, Windows and macOS.  I recommend the JRE build of
either OpenJDK 8 (LTS) or OpenJDK 11 (LTS).

## Look and Feel

The "look" of an application refers to its appearance and the "feel"
refers to how the widgets behave.  By default, Run-REDUCE uses the
standard Swing look-and-feel, sometimes called "Metal", which is the
same on all platforms (apart from the title bar, which always follows
the platform default).  You can use a different look-and-feel if you
start Run-REDUCE by executing one of the following shell commands in the
directory containing `Run-REDUCE.jar`, which apply to all platforms:

    java -jar Run-REDUCE.jar -lfNative

uses the native look-and-feel and

    java -jar Run-REDUCE.jar -lfMotif

uses the Motif look-and-feel.

## General Information

Run-REDUCE remembers user preferences and uses them the next time it
starts.  It uses the standard Java package
[java.util.prefs](https://docs.oracle.com/en/java/javase/13/docs/api/java.prefs/java/util/prefs/package-summary.html),
which stores data persistently in an implementation-dependent backing
store.  For example, on Microsoft Windows the preferences for this
application are stored in the registry under the key
`Computer\HKEY_CURRENT_USER\Software\JavaSoft\Prefs\fjwright\runreduce`
and on Ubuntu Linux they are stored in the XML file
`~/.java/.userPrefs/fjwright/runreduce/prefs.xml`.

By default, Run-REDUCE assumes a standard installation of REDUCE and
uses commands based on that assumption to run REDUCE.  If the
environment variable named `REDUCE` is set then optionally (see below)
Run-REDUCE uses its value as the root of the REDUCE installation; a
final directory separator is optional.  On Microsoft Windows, you can
use either a backward or a forward slash as directory separator and
case is not significant.  For example, the following input to
`cmd.exe` works (although it is unnecessary) if you have installed
REDUCE on your `D` drive:

    set REDUCE=D:/Program Files/Reduce

Otherwise, by default Run-REDUCE looks in the standard places for a
REDUCE installation.  On Microsoft Windows it looks for the folder
`\Program Files\Reduce` on all accessible drives.  On Ubuntu Linux it
assumes that executable files are under `/usr/lib/reduce`, package
information is under `/usr/share/reduce`, and documentation is under
`/usr/share/doc/reduce`.

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
input editor, but you can scroll through the previous input entered
via the input editor using the `Earlier Input` and `Later Input`
buttons.  Scrolling to input later than the last previous input clears
the input editor pane back to its state before you started scrolling.

When keyboard focus is in the input editor pane, the following
keyboard shortcuts are active:

Keyboard Shortcuts    | Action
:--------------------:|:------------:
*Control+Enter*       | Send Input (auto-terminated)
*Control+Shift+Enter* | Send Input (no auto-termination)
*Control+UpArrow*     | Earlier Input
*Control+DownArrow*   | Later Input

where *Enter* is the *Return* or *Enter* key and *UpArrow* /
*DownArrow* are the cursor up / down keys, respectively.

You can edit previous input recalled into the input editor as
necessary and then send it to REDUCE.  Input can be multi-line, in
which case Run-REDUCE processes all the lines together.  The `Send
Input` action is disabled unless REDUCE is running, and the `Earlier
Input` and `Later Input` actions are disabled unless there is earlier
or later input, respectively.

Sending input to REDUCE strips any trailing white space, normally
auto-terminates it by adding a semicolon if there was no final
terminator, and then adds a final newline.  However, if Run-REDUCE
detects a question mark in the input prompt then it suppresses
auto-termination (so if you really want a terminator you must enter it
explicitly).  As an additional precaution, holding the Shift key while
pressing *Control+Enter* or clicking on the `Send Input` button always
suppresses auto-termination.

## The File Menu

Some items in this menu pop up a dialogue that allows you to select
one or more items, such as filenames.  This dialogue supports all the
standard keyboard and mouse-based selection facilities normally
provided by your platform.  In particular, in dialogues that allow
selection of multiple items, holding the `Control` key down while
clicking on an item selects or deselects it without affecting any
other selections, and holding a `Shift` key down while clicking on an
item extends the selection to that item.  Double-clicking on an item
selects it and then runs the action associated with the confirmation
button.

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
This menu item is disabled unless REDUCE is running.

The `Packages` button switches to the REDUCE packages directory
(mainly for my benefit during testing).


### Output to File...

This brings up a file selector dialogue that allows you to send output
to a file instead of the GUI using the REDUCE `OUT` command.  By
default, the file selector only shows `*.rlg` and `*.txt` files, but
you can reset it to show all files.  (Note that the recommended
extension for REDUCE output files is `.rlg`.)  Only one output file
can be selected at a time, but selecting a new output file redirects
output to that file without shutting the previous output file.  The
GUI remember all the open output files to facilitate shutting them
&ndash; see below.  This menu item is disabled unless REDUCE is
running.

### Output Here

This redirects output to the GUI without shutting the current output
file using the REDUCE `OUT T` command.  This menu item is disabled
unless REDUCE is running.

### Shut Output Files...

This is disabled unless there are open output files, in which case it
brings up a dialogue that allows you to select and shut one or more of
the open output files using the REDUCE `SHUT` command.  It uses a
special dialogue, not a file selector, and only shows open output
files.

### Shut Last Output File

This is disabled unless there are open output files, in which case it
shuts the last used open output file using the REDUCE `SHUT` command.

### Load Packages...

This brings up a dialogue that allows you to select and load one or
more REDUCE packages using the REDUCE `LOAD_PACKAGE` command.  It uses
a special dialogue, not a file selector, and only shows standard
REDUCE packages, excluding those that are pre-loaded, sorted
alphabetically.  Run-REDUCE determines the list of packages each time
it starts up by reading the `package.map` file in the REDUCE
installation.  This menu item is disabled unless REDUCE is running.

### Save Session Log...

This brings up a file selector dialogue that allows you to select a
file to which to save the current REDUCE session log, part of which is
displayed in the `Input/Output Display` pane.  By default, the file
selector only shows `*.rlg` and `*.txt` files, but you can reset it to
show all files.  There is a check box labelled `Append` on the
right-hand side of the packages window, which is deselected by
default.  Selecting this option appends the session log to the
selected file; otherwise, it overwrites any previous file content.

### Exit

This terminates both REDUCE and the Run-REDUCE GUI, as does the close
widget that is normally at the top right-hand corner of the main
window frame.

## The REDUCE Menu

The REDUCE menu provides the following items.

### Run REDUCE...

This sub-menu provides an item for each configured version of REDUCE
(typically based on different versions of Lisp &ndash; by default CSL
and PSL).  Clicking on a version of REDUCE runs it.  This sub-menu is
disabled when REDUCE is running.

### Auto-run REDUCE...

This sub-menu allows you to select a version of REDUCE that Run-REDUCE
will run automatically when it starts, or none.  Run-REDUCE remembers
this selection and uses it the next time it starts.  If REDUCE is not
running then Run-REDUCE also runs the selected version of REDUCE
immediately.

### Stop REDUCE

This terminates REDUCE but **not** the Run-REDUCE GUI.  **It is the
recommended way to stop REDUCE because then Run-REDUCE reliably knows
that REDUCE is no longer running.** (Run-REDUCE tries to detect input
of the `BYE` and `QUIT` commands via the input editor, but this is
less reliable.)  This menu item is disabled unless REDUCE is running.

### Clear I/O Display

This completely erases all text from the `Input/Output Display` pane
and its associated buffer.  If you want to save it, use the `Save
Session Log...` item on the `File` menu before erasing it!

### Configure REDUCE...

This brings up a dialogue box that allows you to configure the
versions of REDUCE available and how they are run.  Defaults are
preset that should work for Microsoft Windows and Ubuntu Linux.
Run-REDUCE currently assumes that if it is not running on Windows then
the Ubuntu default configuration is appropriate, so that is what you
will see by default on (say) Apple MacOS. There is a button to reset
the whole REDUCE configuration to the application defaults.

Changes to the dialogue fields are only saved if you click on the
`Save` button, in which case the new configuration is used within
Run-REDUCE and saved as preferences.  Clicking on the `Cancel` button
causes all changes made since the dialogue box opened to be forgotten.

Clicking on the `Delete Selected Version` button deletes the
configuration for the selected version of REDUCE.  If you delete all
available versions then a new (blank) version is automatically
created.

Clicking on the `Duplicate Selected Version` button duplicates the
configuration for the selected version of REDUCE immediately below it.
The name of the duplicate version is the name of the duplicated
version with ` NEW` appended, which you can edit as you wish.  (But
keep version names fairly short!).  Changes to the `Version Name`
field are immediately reflected in the list of REDUCE versions.  This
provides a convenient way, for example, to add one or more versions of
REDUCE with a non-standard memory size.

Clicking on the `Add a New Version` button adds a new version of
REDUCE, which is initially called `NEW VERSION`, with all other fields
empty.  You could, for example, add one or more versions of REDUCE
running on Common Lisp.  (At some future date I may include CL REDUCE
in the default configuration.)

The full command to run a particular REDUCE version must be split into
an executable command filename and a sequence of up to 5 command
arguments.  (It would be easy to increase this limit, but I hope 5
arguments is enough!)  The components of the command that would be
separated by white space in a shell command must be entered into
separate fields on this form.  Run-REDUCE does not currently use a
shell to run REDUCE and the command specified is used directly to
create a separate process to run REDUCE.  So do not include any shell
escapes or other shell syntax!  You can use absolute file pathnames if
you want, but often (as with the PSL REDUCE executable and image file
names) command components share the same root segment.  This root
segment can be specified as `$REDUCE`, which Run-REDUCE will replace
with the value of the `Version Root Dir` field if it is set, or with
the value of the `Default Root Dir` field if it is set, or by the
value of the environment variable named `REDUCE` if that is set.  (I
have borrowed environment variable syntax, but `$REDUCE` here is not
necessarily related to an environment variable.)

The two versions of REDUCE that are distributed, namely CSL and PSL
REDUCE, share the same root directory, which is why the default
configuration uses `Default Root Dir` and leaves `Version Root Dir`
empty.  But a version of REDUCE running on Common Lisp would probably
use a different root directory.

The `Load Packages...` facility in the `File` menu requires a standard
REDUCE packages directory, which should exist in the directory
specified by the `Packages Root Dir` field.  The information used is
independent of the REDUCE implementation, so this directory is fixed
for all REDUCE versions.  It is normally associated with the portable
REDUCE source code.  The value of the `Packages Root Dir` field is
only used by `Load Packages...`.  If this directory does not exist or
is mis-configured then `Load Packages...` will pop up a warning
dialogue, and closing this will automatically pop up the `Configure
REDUCE...` dialogue to allow you to correct the problem.

If the `Packages Root Dir` field is empty on start-up (because you
previously saved a blank value) then it reverts to the platform
default value.

All field values are stripped of leading and trailing space before
they are used or saved, and empty fields are not saved at all.

## The View Menu

The View menu provides the following items.

### Font Size...

This brings up a dialogue box that allows you to change the font size
used in the `Input/Output Display` and `Input Editor` panes.
Run-REDUCE remembers the selected size and uses it the next time
Run-REDUCE starts.

### Bold Prompts

Selecting this checkbox causes Run-REDUCE to embolden the display of
all input prompts.  (This works independently of any I/O colouring but
does not take effect until the **next** input prompt.)

### I/O Colouring

This sub-menu allows you to select an I/O colouring option: `None`,
`Modal` or `Redfront`.  Modal colouring depends on REDUCE's current
input mode: algebraic-mode prompts and input are red, algebraic-mode
output is blue, symbolic-mode prompts and input are brown,
symbolic-mode output is purple.  Redfront colouring is intended to
provide a full emulation of the standard REDUCE `redfront` facility
and it loads the `redfront` package (silently), which outputs
additional markup that is then interpreted by Run-REDUCE in the same
way that it is normally interpreted by the `redfront` executable
running in a suitable terminal (emulator).  In Redfront mode all
prompts and interactive input are red, algebraic-mode output is blue,
and echoed file input and symbolic-mode output are not coloured.  Note
that turning Redfront mode on or off does not take effect until REDUCE
is (re-)started, and turning other modes on or off does not fully take
effect until the next input prompt.

## The Help Menu

The Help menu provides the following items.

### Run-REDUCE User Guide (HTML)

This opens the Run-REDUCE User Guide on GitHub in the default web
browser.

### REDUCE Manual etc.

These menu items open the manuals and other guides that are
distributed with REDUCE in the default web browser or PDF viewer, as
appropriate.

### About Run-REDUCE

This pops up a dialogue displaying brief information about Run-REDUCE.
