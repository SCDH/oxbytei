# Change log #

## dev ##

- new author mode operation for annotating bibliographic references
  and linking them to a bibliography
- changed inheritance hierarchy for author mode operations:
  - `SelectAttributeValueOperation` does not extend
    `AbstractOperation` any more, but the `ChangeAttributeOperation`
    from oXygen.
  - The code of the old `AbstractOperation` has been moved to a class
    that can be used for composition: `SelectLabelledEntryInteraction`
    now calls the plugin loader and does the user interaction. The
    code is much better encapsulated this way.

## 0.4.4 ##

- deploy descriptor file on pages in separate action

## 0.4.3 ##

- fixed link to package in the package descriptor file
- added index.html for github pages

## 0.4.2 ##

- fixed github workflow for making releases

## v0.4.1 ##

- added logging through slf4j

## v0.4.0

- added a schema manager filter that suggests attribute values
  according to labelled entries from plugins in text mode
- added an attribute condition in the config file

## v0.3.2

- send debugging message to stderr and to an exception window when no
  labelled items are found for the action. It prints the used
  providers and the arguments, they are configured with
- plugin config
  - moved default config file from `samples` to `config` folder
  - moved XPath for getting `<prefixDef>` from arguments section to
    conditions section, because it's not an argument of the plugin,
    but evaluated by the editor
- removed the matching of @ident of `<prefixDef>` from the activation
  XPath in the author mode action, because these made to much
  assumptions

## v0.3.1

- made selection dialogue configurable through author mode action xml
- changed API for selection dialogs to handle multiple selection
- reflected these changes in the wrappers around the oxygen dialog and
  the ediarum dialog
- discovered bugs in these dialogs and documented them in the wrappers
- moved reusable code to abstract base class AbstractOperation
  - saw some problems with protected fields of this base class
  - Should abstract base classed be used to share state?

## v0.3.0

- generate selections from `<prefixDef>` through configurable Java
  plugins
- we can now put everything but the activation context into a
  configuration file. So we do not need no hardcoding of assumptions
  into frameworks any more.
- use oXygen's `${ask(...)}` user dialog from within Java
- Actions based on new Java actions:
  - link.person
  - link.place


## v0.2.0 ##

- added transformation scenario for updating the header by merging in
  information from a central header file

## v0.1.0 ##

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

## v0.0.6 ##

- fixed the assembled package: The root directory does not contain the
  Version number any more, now.
  - This makes rewriting file lookup by xml catalogues much more
    simple.
  - This allows us to rename the samples directory.

## v0.0.5 ##

- fixed descriptor file

## v0.0.4 ##

- renamed framework to oXbytei

## v0.0.3 ##

- added regression testing to sel.language author mode action

## v0.0.2 ##

- added regression testing for complicated XPath expressions contained
  in author mode actions

## v0.0.1 ##

- New author mode action for setting `@xml:lang` from the set of
  languages registered in the header
  `teiHeader/profileDesc/langUsage`.
