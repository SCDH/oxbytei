# Changes

## 0.18.0

- new editing action for `<rs>` (referring string)
  - choose `@type`
  - choose `@ref` (or `@key`) from a set of values (persons, places,
    etc.) based on the `@type` value
- transformation scenario for updating config file from default config
  file: add required properties

## 0.17.1

- added editing atoms `move.caret.after.current` and
  `move.caret.before.current` for moving the caret (point) outside the
  current element. These editing atoms can be used by downstream
  frameworks.

## 0.17.0

- added `sel.tag` action for selecting analytical or interpretative
  tags
- improved markup generation for the annotation action: Keep the
  `endID` for the `<span>`, if the end anchor is replaced with
  it. This is generally wanted when the annotation is used in a way
  similar to an internal double-end-point apparatus encoding.

## 0.16.4

- fix descriptor file (see issue #9)

## 0.16.3

- support Oxygen 26 (see issue #8)
- git release tags are now the single source of truth for version numbers

## 0.16.1

- fix CI pipeline

## 0.16.0

- compatiblity with Oxygen 25
- provide framework compiled for different versions of Oxygen
- do not use XML catalog from the editor in the `LabelledEntriesXSLT`
  and `LabelledEntriesXSLTWithContext` providers
  - This was required due to API changes in Saxon 11, which ships with
	Oxygen 25. We gain compatiblity with Oxygen 25.
- restructured code base into a multi modules project
- removed dependency on `ediarum.jar`
- changed license to MIT

## 0.15.0

- new XPath extension function `obt:current-element`
  - `obt:current-element(url as xs:string) as node()*`
  - takes URL of a document and returns the node where the caret
    is located if the document is currently edited
  - works in text mode as well as in author mode
  - handy to transform only the part (paragraph, section, page, etc.)
    which is currently being edited
- schema attribute editor
  - added to contextual menu
  - added info about the current element to the editor dialog
  - added its icon

## 0.14.2

- fixed issue #6
- fixed incorrect argument description reported by a reviewer of a
  proposal for the TEI 2022 conference.

## 0.14.1

- experimental dialog
  `de.wwu.scdh.teilsp.ui.FilteringListSelectDialog`
  - introduces filtering based on event list
  - does not yet scroll to the selected item

## 0.14.0

- new list dialog for selecting singelton or multiple values
- new plugins for generating labelled entries
  - `de.wwu.scdh.teilsp.extensions.LabelledEntriesXSLT` for generating
    labelled entries via XSLT. This plugin does not get the current
    editing context.
  - `de.wwu.scdh.teilsp.extensions.LabelledEntriesXSLTWithContext` for
    generating labelled entries via XSLT. This plugin passes the
    document node and an XPath expression, that identifies the currently
    edited node, as stylesheet parameters.
    - the URI resolvers are passed to these plugins so that XML
      catalogs are in force.
  - `de.wwu.scdh.teilsp.extensions.LabelledEntryCSV` for generating
    labelled entries from CSV data.
    - CSV data may be in a variety of formats: CSV, TDF, Excel,
      numerous database outfile formats
  - `de.wwu.oxbytei.extensions.SchemaAttributeValuesProvider` for
	generating suggestions for attribute values from the XML schema
	- this was added to the default configuration and activated
	  everywhere
- `LabelledEntry` is an interface now
  - see `de.wwu.scdh.teilsp.extensions.LabelledEntryImpl` for an
    implementation
  - `LabelledEntryWithColumns` is an extension of this interface for
    future implementations of column views etc.

## 0.13.1

- plugin `de.wwu.scdh.teilsp.extensions.LabelledEntriesXQuery` calls a
  function from XQuery script, not the whole script. This makes
  developing and debugging XQuery much easier.
  - The name of the function is configurable (local name, prefix and
	namespace), defaults to
	`Q{http://scdh.wwu.de/oxbytei}generate-entries`.
  - The arity of the function must be 0.
  - The return value must be `map(xs:string, xs:string)*`.

## 0.13.0

- **changes of API**:
  - introduced `IConfigurablePlugin` for plugins that get
    context-sensitive config from the config file
  - `ILabelledEntriesProvider` and `ISelectionDialog` are derived from
    this new interface now
  - `ILabelledEntriesProvider` gets the current editing context now via
    `setup(...)`
  - configurable plugins are required to provide a method to describe
    their configuration arguments now
  - introduced a common loader for configurable plugins
- made `de.wwu.scdh.teilsp.services.extensions.ArgumentDescriptor` a
  polymorphic interface and `ArgumentDescriptorImpl` an implementation
  of it
  - this makes evaluating, getting and converting arguments for plugins
    from the configuration super straight forward
- introduced `de.wwu.scdh.teilsp.extensions.LabelledEntriesXQuery`, a
  provider for labelled entries which produces its collection via
  XQuery

## 0.12.0

- added **selection dialogs**
  - added editable combo box
    `de.wwu.scdh.teilsp.ui.EditableComboBoxDialog` as default dialog
  - multiple select through check boxes:
    `de.wwu.scdh.teilsp.ui.CheckBoxSelectDialog`
	- setup for `@wit`
  - singleton select through combo box:
    `de.wwu.scdh.teilsp.ui.ComboBoxSelectDialog`
- **change of API** `ISelectDialog`
  - make `ISelectionDialog` an SPI interface and load dialogs as
    plugins which are configured through plugin config for arbitrary
    editing contexts
  - removed oXygen-specific classes
  - split `doUserInteraction` into `doUserInteraction()` and
    `getSelection()` in order to make UI writing simple, because
    otherwise we would need a loop to wait for user interaction to
    finish.
  - split `init()` into `init()` and `setup()` so that we can call
    `init()` from SPI loader
  - pass icon URL into dialog
  - moved to package `de.wuu.scdh.teilsp.ui` which contains UI code
    which does not need anything from oXygen
  - old dialog classes are still in place and work as before
- added generic schema attribute editor which uses the above
  configurable dialogs
- configuration
  - added some basic config for dialogs

## 0.11.0

- added `ExpandingDeleteElementOperation` which does the same as the
  builtin `DeleteElementOperation` but expands editor variables in the
  arguments.
- fixed the logger for `InsertAnchorOperation`

## 0.10.0

- user action for generating IDs on a configurable portion of the
  document using the TEI P5 framework's "Generate IDs" action.
  - portion defaults `/*`, i.e. the whole document
- new plugin that provides labelled entries directly from the config
  file, where they are defined in a CSV-like manner
- transformation scenario that identifies broken XIncludes and adds a
  fallback if not present

## 0.9.6 ##

- fixed issue #5: removed single quotes in selection texts which
  interfere with the string generated for the `ask` editor variable

## 0.9.5 ##

- add user action for inserting XInclude
- enable selection of bibliographic reference in other contexts

## 0.9.4 ##

- make the actions for inserting an apparatus entry simpler by
  splitting them up
- make the actions for making annotations simpler and more
  configurable by splitting them up

## 0.9.3 ##

- fix issue #4: Text from enclosed elements was duplicated for
  e.g. the lemma of an apparatus entry. This was fixed.
- make annotate action more generic by removing the part for selecting
  an analytic category
  - chosing an analytic category and putting it to a target (text node
    or an attribute) should be an other action and it is simple to
    configure this in the plugin system
- improve reproduction of referenced text
  - do not reproduce variant readings
  - to not reproduce processing instructions
  - use this in `analytic` mode for persons, places etc., too

## 0.9.2 ##

- Bug fix: An apostrophe (ASCII 0x27) in the labels or values
  generated by the plugin system was breaking the selection
  dialogue. (issue #3) This was fixed.

## 0.9.1 ##

- try to fix lookup of the config file on windows, issue #1

PS. This actually fixed #1

## 0.9.0 ##

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
  - current features: remove notes, replace caesura and verse break
    with common signs
- *annotate* action is more configurable now:
  - configure which attribute to write to, default is `@ana`
  - configure whether or not to reproduce referenced text
  - configure an element to keep the reproduced text in
- added a simple rollback mechanism for the cancel event in user
  actions
  - this results in a rollback to the previous editor state, no
    changes by half-done action chains

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
