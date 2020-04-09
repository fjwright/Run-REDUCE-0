**This is a new web site still under development!**

Run-REDUCE is a project to provide a cross-platform GUI for running
the REDUCE Computer Algebra System that provides a consistent user
experience across all platforms.  It is implemented in Java and the
executable application takes the form of the Java JAR file
`Run-REDUCE.jar` that is one of the three assets available by clicking
on `Assets` below the information about the latest release at the top
of the [Releases](https://github.com/fjwright/Run-REDUCE/releases)
page.

REDUCE itself is an open source project available from
[SourceForge](https://sourceforge.net/projects/reduce-algebra/), which
you need to install separately.  Run-REDUCE is designed to run a
standard installation of REDUCE; it does not include REDUCE.

Full information about how to install and run Run-REDUCE is available
in the [User Guide](UserGuide.md).

Run-REDUCE is currently under active development and is not yet
complete.  Here are some of the key features that it currently
provides:

* Commands to run various versions of REDUCE that are fully
  configurable but default to running the distributed CSL and PSL
  versions of REDUCE as appropriate for the standard distributions.
* A REDUCE input/output display pane that scrolls in both directions
  as necessary and supports copying but not editing.
* A multi-line input editing pane that also scrolls in both directions
  as necessary.  Previous input is remembered and can be scrolled
  through, edited and re-input.  A final terminator is added
  automatically if necessary when input is sent to REDUCE.
* Options to make the REDUCE input prompt bold and to colour the
  input/output display based on the input mode or redfront.
* Menu options to handle REDUCE file input/output and load standard
  REDUCE packages, similar to the facilities provided by the CSL
  REDUCE GUI.
* Menu options to provide easy access to all the standard manuals
  distributed with REDUCE as HTML or PDF files.

Here are some of the key enhancements that I hope to implement in
future:

* Better support for non-standard Java look-and-feels.
* Tabbed panes running independent REDUCE processes.
* Typeset-style display of mathematics that supports copying and
  pasting as input.  But at what level of granularity remains to be
  determined!

Francis Wright, April 2020
