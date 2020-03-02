# Run-REDUCE User Guide

### Francis Wright, March 2020

Run-REDUCE is an open-source Java GUI to run the REDUCE Computer
Algebra System, which must be installed separately from
[SourceForge](https://sourceforge.net/projects/reduce-algebra/).
Run-REDUCE should find a normal REDUCE installation automatically.

Run-REDUCE does **not** (yet) provide typeset-quality display of
mathematical notation.

## Installation

Download the file
[Run-REDUCE.jar](out/artifacts/Run_REDUCE_jar/Run-REDUCE.jar)
and run it from the directory to which you downloaded it using the command
`java -jar Run-REDUCE.jar`.

## The Main Window

The main window consists of two panes one above the other.  The top
pane displays a log of all the REDUCE input and output in the current
session.  This pane is read-only.  The bottom pane is an input editor,
which supports all the standard keyboard and mouse-based editing
facilities normally provided by the platform.  Both panes display
vertical and horizontal scroll bars when appropriate; text does not wrap.
The horizontal divider
separating the two panes can be dragged up and down, and it can be
moved all the way up or down by clicking on one of the two triangular
icons at the left-hand side.

You type (or paste) REDUCE input into the input editor pane, edit it
as necessary, and then click on the `Send Input` button, which sends
the input to REDUCE and echos it in the top pane.  This clears the
input editor, but you can scroll through the previous input using the
`Earlier Input` and `Later Input` buttons.  Previous input recalled
into the input editor can be edited as necessary and then sent to
REDUCE.  Input can be multi-line, in which case Run-REDUCE processes
all the lines together.

## The File Menu

Some items in this menu pop up a dialogue that allows you to select
one or more items, such as filenames.  This dialog supports all the
standard keyboard and mouse-based selection facilities normally
provided by the platform.  In particular, holding the `Control` key
down while clicking on an item selects or deselects it without
affecting any other selections, and holding a `Shift` key down while
clicking on an item extends the selection to that item.

Most of the menu items run a REDUCE command, which is echoed in the
display pane but does not appear in the input editor.  The file
selector dialogues all share the same default directory, which is
initially your home directory, but the last directory you used will be
the default directory the next time you use a file selector dialogue.

This default directory is independent of REDUCE's default directory,
which is irrelevant because this GUI always uses absolute file paths.

The File menu provides the following items.

### Input from Files...

This brings up a file selector dialogue that allows you to input one
or more source code files into REDUCE using the REDUCE `IN` command.
By default, the file selector only shows `*.red` and `*.txt` files,
but you can reset it to show all files.  Note that the recommended
extension for REDUCE source files is `.red`.  There is a check box
labelled `Echo` on the right hand side of the file window, which is
selected by default.  Selecting this option causes file input to be
echoed to the `Input/Output Display` pane.  Note that a REDUCE source
code file should end with `;end;` to avoid an error message.

### Output to File...

This brings up a file selector dialogue that allows you to send output
to a file instead of the GUI using the REDUCE `OUT` command.  By
default, the file selector only shows `*.rlg` and `*.txt` files, but
you can reset it to show all files.  Note that the recommended
extension for REDUCE output files is `.rlg`.  Only one output file can
be selected at a time, but selecting a new output file redirects
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
uses a special dialogue, not a file selector, that only shows open
output files.

### Shut Last Output File

This item is inactive unless there are open output files, in which
case it shuts the last used open output file using the REDUCE `SHUT`
command.

### Load Packages...

This item brings up a dialogue that allows you to select and load one
or more REDUCE packages using the REDUCE `LOAD_PACKAGE` command.  It
uses a special dialogue, not a file selector, that only shows standard
REDUCE packages, excluding those that are pre-loaded, sorted
alphabetically.  Run-REDUCE determines the list of packages each time
it start up by reading the `package.map` file in the REDUCE
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

## The Help Menu

### About RunREDUCE

This pops up a dialogue displaying brief information about this
version of Run-REDUCE.
