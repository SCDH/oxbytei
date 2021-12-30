# Usage and Customization #

- [Selecting language](xmllang.md)
- [Linking persons](personography.md)
- [Linking places](geography.md)

- [Customzing the configuration file](config.md)

- [Keeping the header up to date](updateHeader.md)


Take a look at the folder
[`../frameworks/oxbytei/samples`](frameworks/oxbytei/samples)
for sample resources.

## Further Notes ##

Note: There's no evaluation of `@xml:base` in context of pointers
defined by `<prefixDef>`. Read on this topic in
[TEI-L](https://listserv.brown.edu/cgi-bin/wa?A2=ind1705&L=TEI-L&D=0&P=6482).
Resulting
[recommendation](https://listserv.brown.edu/cgi-bin/wa?A2=ind1705&L=TEI-L&D=0&P=46600).

Note: Splitting of parts of fragment identifiers over the URI like
practised
[here](https://listserv.brown.edu/cgi-bin/wa?A2=ind1606&L=TEI-L&D=0&P=10714)
is not supported by the current plugin.

Note: Disregarding the currently edited file, oXbytei operates on the
files as they were last saved to disk, not their current state in
oXygen.
