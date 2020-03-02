# Run-REDUCE

## A Java GUI to run any CLI version of the REDUCE Computer Algebra System

### Francis Wright, March 2020

This is the first prototype that does anything useful and my first
ever GitHub repository, which is currently private.  I will make it
public when I have developed it a bit further.

This version uses the Java Swing library, but at some future date I
will probably switch to using JavaFX.  Swing has the advantage that it
is still a standard component of the latest Java SE SDK.  I am using
Java 13 and the command line to compile and run.

This version only runs on Microsoft Windows, but I plan to add support
for Linux.  (Whilst Java is portable, filesystem structures and
installation conventions are not!)

This version only runs PSL and CSL REDUCE, but I plan to add support
for other versions such as CL REDUCE.

REDUCE is an open source project available from
[SourceForge](https://sourceforge.net/projects/reduce-algebra/).  I'm
releasing this project under the [BSD 2-Clause License](LICENSE),
mainly because it's the license used by REDUCE.

The project is set up for development using [IntelliJ
IDEA](https://www.jetbrains.com/idea/) with Run-REDUCE as the
top-level directory.

### To do soon

* Multiple tabs that each run an independent invocation of REDUCE.
* Select REDUCE version to run.
* Remember user choices, such as default REDUCE version, commands to
  run REDUCE, window configurations.

### To do later

* Typeset output supporting copy and paste. Could parse `off nat`
  output, or use it together with TeX or MathML output.
* Merge input region into output region (maybe).
* Integrated plotting support; maybe plots in a new tab or in a
  horizontally split window (maybe).
* A calculator-style interface option (maybe).
