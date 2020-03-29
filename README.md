# Run-REDUCE

## A Java GUI to run any CLI version of the REDUCE Computer Algebra System

### Francis Wright, March 2020

This is an incomplete prototype!

For information about how to install and run Run-REDUCE please see the
[User Guide](UserGuide.md).

Run-REDUCE should run on any platform that supports Java 8, but I have
only tested it on Microsoft Windows and Ubuntu Linux.  (Whilst Java is
portable, filesystem structures and installation conventions are not!)

This version uses the Java Swing library, but at some future date I
will probably switch to using JavaFX.  Swing has the advantage that it
is still a standard component of the latest Java SE SDK.  I am
developing using Java 13, but compiling for Java 8 to support running
a jar file using the latest readily available JRE.

REDUCE is an open source project available from
[SourceForge](https://sourceforge.net/projects/reduce-algebra/).  I'm
releasing this project under the [BSD 2-Clause License](LICENSE),
mainly because it's the license used by REDUCE.

This project is set up for development using [IntelliJ
IDEA](https://www.jetbrains.com/idea/) with Run-REDUCE as the
top-level directory.

### To do soon

* More sophisticated font colouring, including a full emulation of
  redfront.
* Multiple tabs that each run an independent invocation of REDUCE.

### To do later

* Better support for non-default look-and-feels.
* Detect whether REDUCE is running more reliably (maybe).
* Remember all user choices, including window configurations (maybe).
* Typeset output supporting copy and paste. Could parse `off nat`
  output, or use it together with TeX or MathML output.
* Merge input region into output region (maybe).
* Integrated plotting support, perhaps with plots in a new tab or in a
  vertically split window (maybe).
* A calculator-style interface option (maybe).

## Release Notes

### Version 0.4 user-visible updates (since v0.2)

* Add a semicolon to the end of input if there is no terminator.
* Optional redfront-style font colouring to distinguish input and
  output in the IO display pane.
* Rename Preferences menu to View.
* Explicit support for Ubuntu Linux.
* A new item at the bottom of the REDUCE menu opens a dialogue box to
  configure REDUCE directories and commands.  Using this should allow
  Run-REDUCE to run on any platform.

### Version 0.5 user-visible updates (since v0.4)

* Button to duplicate selected version added to REDUCEConfigDialog.
* Descenders in text are no longer cut off (on Windows) in
  REDUCEConfigDialog.
* Better handling of `Load Packages...` and `Packages Root Dir`.
* Disable the earlier and later input buttons as appropriate.
* Enable or disable menu items etc. according to whether REDUCE is
  running.  Allow REDUCE to be restarted.
* `Quit` menu item renamed to `Exit`, and a new `Stop REDUCE` item
  added to the `REDUCE` menu.
* Correct REDUCEConfigDialog with a non-default look-and-feel.

### Pre-release user-visible updates since v0.5

* Keyboard shortcuts to send input to REDUCE and scroll the input
  list.  Scrolling beyond the last input clears the input pane.
* Set the main application window to 2/3 the linear dimension of the screen.
