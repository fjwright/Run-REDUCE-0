# Run-REDUCE

## A Java GUI to run any CLI version of the REDUCE Computer Algebra System

### Francis Wright, March 2020

This is the first release of a very incomplete prototype!

For information about how to install and run Run-REDUCE please see the
[User Guide](UserGuide.md).

This version uses the Java Swing library, but at some future date I
will probably switch to using JavaFX.  Swing has the advantage that it
is still a standard component of the latest Java SE SDK.  I am
developing using Java 13, but compiling for Java 8 to support running
a jar file using the latest readily available JRE.

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

* Make the commands to run REDUCE configurable.
* Remember all user choices, including commands to run REDUCE and
  window configurations.
* Support platforms other than Microsoft Windows, in particular Ubuntu
  Linux.
* Multiple tabs that each run an independent invocation of REDUCE.

### To do later

* Typeset output supporting copy and paste. Could parse `off nat`
  output, or use it together with TeX or MathML output.
* Merge input region into output region (maybe).
* Integrated plotting support, perhaps with plots in a new tab or in a
  vertically split window (maybe).
* A calculator-style interface option (maybe).
