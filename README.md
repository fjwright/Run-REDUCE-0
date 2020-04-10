# Run-REDUCE

## A Java GUI to run any CLI version of the REDUCE Computer Algebra System

### Francis Wright, April 2020

This application is an incomplete prototype!

For information about how to install and run Run-REDUCE please see the
[User Guide](docs/UserGuide.md).

Run-REDUCE should run on any platform that supports Java 8 (or later),
but I can only test it on Microsoft Windows and Ubuntu Linux.  (Whilst
Java is portable, filesystem structures and installation conventions
are not!)

This version uses the Java Swing library, but at some future date I
will probably switch to using JavaFX.  Swing has the advantage that it
is still a standard component of the latest Java SE SDK.  I am
developing using Java 13, but compiling for Java 8 to support running
the jar file using all current JREs.

REDUCE itself is an open source project available from
[SourceForge](https://sourceforge.net/projects/reduce-algebra/).  I'm
releasing Run-REDUCE under the [BSD 2-Clause License](LICENSE), mainly
because it's the license used by REDUCE.

This project is set up for development using [IntelliJ
IDEA](https://www.jetbrains.com/idea/) with Run-REDUCE as the
top-level directory.

### To do soon

* Better support for non-default look-and-feels.
* Multiple tabs that each run an independent invocation of REDUCE.

### To do later

* Font and colour selectors.
* Detect whether REDUCE is running more reliably (maybe).
* Remember all user choices, including window configurations (maybe).
* Typeset output supporting copy and paste. Could parse `off nat`
  output, or use it together with TeX or MathML output.
* Merge input region into output region (maybe).
* Integrated plotting support, perhaps with plots in a new tab or in a
  vertically split window (maybe).
* A calculator-style interface option (maybe).
* Hybrid I/O colouring combining modal and redfront modes (maybe).

## Release Notes

### Version 0.4 user-visible updates

* Add a semicolon to the end of input if there is no terminator.
* Optional redfront-style font colouring to distinguish input and
  output in the IO display pane.
* Rename Preferences menu to View.
* Explicit support for Ubuntu Linux.
* A new item at the bottom of the REDUCE menu opens a dialogue box to
  configure REDUCE directories and commands.  Using this should allow
  Run-REDUCE to run on any platform.

### Version 0.5 user-visible updates

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

### Version 0.6 user-visible updates

* Keyboard shortcuts to send input to REDUCE and scroll the input
  list.  Scrolling beyond the last input clears the input pane.
* Set the initial main application window size to 2/3 the linear
  dimension of the screen.
* Add a `Packages` button to the `Input from Files...` dialogue that
  switches to the REDUCE packages directory (mainly for my benefit
  during testing).
* Add a `Clear I/O Display` item to the REDUCE menu.
* Add a `Bold Prompts` checkbox to the View menu.
* Replace the `Coloured I/O` item in the `View` menu with an `I/O
  Colouring` sub-menu.
* Provide a full emulation of Redfront.
* Add a double-click handler for the `Shut Output Files...` and `Load
  Packages...` dialogues that makes them more consistent with the file
  chooser dialogue.
* Use a more solid font than the default for the I/O display pane.
* Provide easy access to the distributed documentation via the Help
  menu.
* Replace the `Auto-run REDUCE?` checkbox with a `None` option in the
  `Auto-run REDUCE` radio button group, and start the selected version
  of REDUCE immediately if REDUCE is not running.

### Version 0.7 user-visible updates

* Detect a question prompt and don't auto-terminate the response.
* Holding Shift always suppresses auto-termination of input.
* Fix the activated status of the `Output Here` menu item.

### Pre-release user-visible updates
