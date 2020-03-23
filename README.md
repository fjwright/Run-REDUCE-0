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
* Detect whether REDUCE is running and enable or disable menu items
  appropriately.  Allow REDUCE to be restarted.
* Better scrolling facilities in the IO display pane.
* Keyboard shortcuts to send input to REDUCE and scroll the input
  list.
* Multiple tabs that each run an independent invocation of REDUCE.

### To do later

* Remember all user choices, including window configurations (maybe).
* Typeset output supporting copy and paste. Could parse `off nat`
  output, or use it together with TeX or MathML output.
* Merge input region into output region (maybe).
* Integrated plotting support, perhaps with plots in a new tab or in a
  vertically split window (maybe).
* A calculator-style interface option (maybe).

## Release Notes

### Version 0.4 updates (since v0.2)

* Add a semicolon to the end of input if there is no terminator.
* Optional redfront-style font colouring to distinguish input and
  output in the IO display pane.
* Rename Preferences menu to View.
* Explicit support for Ubuntu Linux.
* A new item at the bottom of the REDUCE menu opens a dialogue box to
  configure REDUCE directories and commands.  Using this should allow
  Run-REDUCE to run on any platform.

### Pre-release updates since v0.4

* Button to duplicate selected version added to REDUCEConfigDialog.

