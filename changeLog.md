# Change log #

## dev ##

- New actions for managing anchor based markup:
  - I) find annotations e.g. an external `<app>` or a `<span>` that
	reference the text at the caret, and let the user choose which one
	she wants to move to
  - II) highlight (select) the span of text referenced by an
	annotation, e.g. an external `<app>` or `<span>`
- These actions greatly simplify the navigation and orientation in
  anchor based markup.
- Both actions are configurable.
- push referenced text through XSLT when making an `<app>` or an `<span>`
  - the template can easily be replaced by user through an XML catalog
- *annotate* action is more configurable now
  - configure which attribute to write to, default is `@ana`
  - configure whether or not to reproduce referenced text
  - configure an element to keep the reproduced text

## 0.8.5 ##

- fixed a bug in the calculation of the XPath of the current element or
  the anchors' container

## 0.8.4 ##

- introduced `SelectReferencedOperation` for selecting a portion of
  the currently edited document that is references by a markup
  element, e.g. an external apparatus entry
  - an example use case is in oXbytao

PS: this action has been moved to oXbytei in 0.9.0

## 0.8.3 ##

- introduced `InsertAnchorOperation` for inserting single anchors
- introduced `${anchorId}` editor variable to get the xml ID of the
  last anchor generated with `InsertAnchorOperation`

## 0.8.2 ##

- enabled framework inheritance
  - Please, have a look at [oXbytao](https://github.com/SCDH/oxbytao)
    for an example!
- re-arranged menus and toolsbars

## 0.8.1 ##

- fixed a bug in analytic markup style
- make generated IDs customizable through redirection to an xsl
  library by an XML catalog:
  - redirect `oxbytei/xsl/generate-id.xsl` to your own implementation
- added regression tests based on XSpec for all markup styles added in
  0.8.0
- use XSpec 1.6 and Saxon 10.2 for testing XSLT 3.0

## 0.8.0 ##

- flexible markup for persons, places and bibliographic references:
- markup styles available:
  - **aggregative**: Wrap user selection into an element, if the start
    and end point are on the same parent node. Otherwise wrap each
    text nodes in the selection into its own element and link them
    with `@next` and `@prev`.
  - **analytic**: Insert empty anchors with IDs at the start and end
    point of the user selection and add a `<span from="#startId"
    to="#endId">` into the next following `<spanGrp>`. The element tag
    is put into the `<span>`. The selected text is repeated in it.
  - **spanTo**: Insert an empty anchor at the end of the user
    selection and a tag to the start point, that has
    `@spanTo="#endId"`.
  - **restricted-aggregative**: Wrap text nodes in the user selection
    into `<seg>` elements, put IDs on them and add an *empty*
    aggragative tag at the selection's end point with `@select="#id1
    id2 ... idN"`.
- Whitespace-only text nodes are not wrapped in *aggregative* and
  *restricted-aggregative* style in order to avoid invalid markup when
  annotating over paragraph or verse boundaries. This can be turned
  off.
- configurable through the configuration file

## 0.7.1 ##

- expand editor variables in the config file
  - this only works in author mode
  - thus, no editor variables are used in the default config file

## 0.7.0 ##

- introduced `${teilspProp(property.name)}` editor variable, which is
  expanded to the property named `property.name` in the config file
- load an extension state listener and register the new oXbytei editor
  variable resolver


## 0.6.1 ##

- default configuration:
  - put apparatus entry in external double end-point attached variant
	encodings into the next following `<listApp>`
- added `de.wwu.scdh.oxbytei.NoOperation` author mode operation that
  does nothing at all.
- unified the editing steps for adding a new apparatus entry by using
  `NoOperation`.
- do not use a recursive action for the appratus any more
- include the framework for editing the configuration file into the
  package and ship it with oXbytei

## 0.6.0 ##

- author mode action for adding an apparatus entry
  - user selection goes to the lemma
  - caret is placed into the reading
  - ask for witness of reading
  - **all variant encodings**, but external location-referenced
  - select witness based on `sourceDesc/listWit//witness`

## 0.5.0 ##

- new author mode operation for surrounding the user selection with
  empty anchor elements with IDs
  - per default it uses oXygen's `${id}` editor variable, to produce
    `@xml:id`s
  - But the ID can be calculated by an XPath expression from the
    document, too.
- new author mode operation for a) surrounding the user selection with
  empty anchor elements with IDs and b) then performing an XSLT
  operation
  - This is very cool because it let's us produce complicated
    interlinked markup with just one click.
  - It will be the heart of annotation, double-end attached apparatus
    etc.
- added new author mode action for annotations of overlapping structures
  - inserts anchors with `start_id` and `end_id`
  - inserts an `<span from="#start_id" to="#end_id">` to a `<spanGrp>`
    in the backmatter per default config
  - adds `@ana` with a value select from provider suggestions to the
    `<span>`.

## 0.4.6 ##

- changed inheritance hierarchy for author mode operations:
  - `SelectAttributeValueOperation` does not extend
    `AbstractOperation` any more, but the `ChangeAttributeOperation`
    from oXygen. So we needn't implement the real operation on the
    document any more.
  - The code of the old `AbstractOperation` has been moved to a class
    that can be used for composition: `SelectLabelledEntryInteraction`
    now calls the plugin loader and does the user interaction. The
    code is much better encapsulated this way.
- new author mode operation for annotating bibliographic references
  and linking them to a bibliography
- removed the `<prefixDef>` condition from author mode actionsq

## 0.4.5 ##

- Changes to the API:
  - The signature of the init method of ILabelledEntriesProvider has
    changed.
  - It takes the document's DOM node now.
- The logic of the plugin loader for loading plugins for the current
  editing context has been moved from oxbytei to teilsp
- Document DOM nodes of the currently edited document are now passed
  through from oXygen to the plugins. This speeds things up and
  reflects changes in them that have not been saved to disk yet.
- The redundant class ...commons.Resolver has been removed.
- The name of the one and only author mode operation has changed to
  SelectAttributeValueOperation, because there is nothing specific for
  `<prefixDef>` in there any more.
- A utility class for making an XPath object has been added as a
  convenience tool regarding different implementations of the document
  DOM node, that is passed around: `XPathUtil`.

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
