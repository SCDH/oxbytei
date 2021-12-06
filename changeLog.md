# Change log #

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
