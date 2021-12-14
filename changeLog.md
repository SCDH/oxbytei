# Change log #

## v1.1.0 ##

- Changed the license to GPL v3
  - Because it is more satisfing to write *free* software than writing
    only *os* software. And it's better for the community to have
    *free* software.
  - Because GPLed ediarum.jar is bundled within the framework.
- A java implementation of an author mode action component for
  selecting an item from a referatory (personography, bibliography,
  geography, etc.)  defined in `prefixDef` was added. It provides a
  mechanism for adding plugins for reference providers, like local
  norm data or global norm data.
- an implementation of such a plugin which provides selection items
  generated from an XML file. It is configured with XPaths, to access
  the items, their keys (IDs) and make a label displayed in selection
  dialogues.
- general classes have been sourced to the java namespace
  `de.wwu.scdh.teilsp` which serves as a germ for a TEI LSP server.
- added an author mode action for selecting a place from a geographic
  referatory
  - it makes is a recursively defined action
	- stop at adding `@ref` attribute in context of `placeName`
	- start with surrounding a selection

## v1.0.6 ##

- fixed the assembled package: The root directory does not contain the
  Version number any more, now.
  - This makes rewriting file lookup by xml catalogues much more
    simple.
  - This allows us to rename the samples directory.

## v1.0.5 ##

- fixed descriptor file

## v1.0.4 ##

- renamed framework to oXbytei

## v1.0.3 ##

- added regression testing to sel.language author mode action

## v1.0.2 ##

- added regression testing for complicated XPath expressions contained
  in author mode actions

## v1.0.1 ##

- New author mode action for setting `@xml:lang` from the set of
  languages registered in the header
  `teiHeader/profileDesc/langUsage`.
