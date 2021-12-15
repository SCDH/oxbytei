# oXbytei #

<!--
![release](https://github.com/scdh/scdh-oxygen-extension/actions/workflows/release.yml/badge.svg)
![tests](https://github.com/scdh/scdh-oxygen-extension/actions/workflows/test-main.yml/badge.svg)
-->

## tl;dr ##

Install from [https://scdh.zivgitlabpages.uni-muenster.de/tei-processing/oxbytei/descriptor.xml](https://scdh.zivgitlabpages.uni-muenster.de/tei-processing/oxbytei/descriptor.xml).


## An oXygen framework configured by TEI ##

oXbytei [ɔx bʌtaj] (greco-english tongue and french ears) is an oXygen
framework for editing TEI, that is configured by TEI's header. It
offers high-level functions that facilitate everyday work on TEI
documents.

- set `@xml:lang` by selecting a language registered in the header by
  [`langUsage`](https://www.tei-c.org/release/doc/tei-p5-doc/de/html/ref-langUsage.html)
- set `@ref` of `<persName>` etc. by selecting a person from a locally
  stored personography that is bound to a local URI scheme defined in
  the header by
  [`<prefixDef>`](https://www.tei-c.org/release/doc/tei-p5-doc/de/html/ref-listPrefixDef.html)
- set `@ref` of `<placeName>` etc. by selecting a place from a locally
  stored geography that is bound to a local URI scheme defined in
  the header by
  [`<prefixDef>`](https://www.tei-c.org/release/doc/tei-p5-doc/de/html/ref-listPrefixDef.html)
- update the header by pulling in information from the project's central header file

Read more in the [**Usage Notes**](docs/README.md).

on the road map:

- bibliography
- (index)
- metre of lyrics
- critical apparatus


Note: Yes, there are some little assumptions that have poured
into this framework and blur its generality, e.g. assumptions about
sensible URI schemes for certain purposes, like `psn` or `pers` for
persons. But these are endangered animals that will become extinct as
soon as oXygen allows [parametrized
frameworks](https://www.oxygenxml.com/forum/topic23764.html).

See the section [Usage and Customization](#usage-and-customization)
for how to write TEI and setup your project in order to make use of
the framework's features.



## Package ##

### Requirements ###

oXbytei requires oXygen >= 23.1, because it makes use of an [extension
script](https://www.oxygenxml.com/doc/versions/23.1/ug-editor/topics/framework-customization-script.html)
for framework configuration.

### Installation ###

The framework can be installed with &lt;oXygen/>'s installation and
update mechanism. Therefore, the following URL has to be entered into
the form *Show addons from:* of the dialogue box from ***Help** ->
**Install new addons**...*.

<!--
[https://scdh.github.io/oxbytei/descriptor.xml](https://scdh.github.io/oxbytei/descriptor.xml)
-->

[https://scdh.zivgitlabpages.uni-muenster.de/tei-processing/oxbytei/descriptor.xml](https://scdh.zivgitlabpages.uni-muenster.de/tei-processing/oxbytei/descriptor.xml)

![Dialog "Install new addons"](images/install.png)


As an alternative, the framework can be packaged locally for
installation or it can be installed for hacking.

#### Packaging locally ####

Packaging is done with [`maven`](https://maven.apache.org/).

```{shell}
mvn package
```
	
This will create a file named `oXbytei-<VERSION>-package.zip` in the
`target` folder. This zip-File is the same as the one distributed
under the above mentioned URL.

In order to use oXbytei with older versions of oXygen, you can
generate an old-school dot-framework file from the extension script
using the *Compile Framework Extension script* as described in the
[documentation](https://www.oxygenxml.com/doc/versions/23.1/ug-editor/topics/framework-customization-script.html)
and then use local packaging.

#### Hacking / Development ####

Installing the framework as an &lt;oXygen/> package will make it
read-only. For hacking on it's code you can install it by registering
the path to the cloned repository in &lt;oXygen/>'s settings.

- 1) Clone this repository into a subfolder of an &lt;oXygen/>
  project, e.g. `oXbytei`. (It may also be sym-linked there.)

- 2) Start &lt;oXygen/> and select `Options` -> `Preferences` from the
  menu. Expand `Document Type Association` on the left and select
  `Locations [P]` under it. Click `Add` to add a new additional
  framework directory.  Enter `${pdu}/oxbytei` as directory and click
  `OK`. (Note: `${pdu}` is an [editor
  variable](https://www.oxygenxml.com/doc/versions/22.1/ug-editor/topics/editor-variables.html)
  and points to the root folder of the current project.
  
- 3) Close and restart &lt;oXygen/>. The framework is now present as an
  extension to the default TEI P5 framework.

### Regression Tests ###

There are regression tests based on
[`XSpec`](https://github.com/xspec/xspec) in `test/xspec`. The tests
can easily be run with maven from the root directory of the
repository:

```{shell}
mvn test
```

Maven will install all required packages for running the tests,
e.g. `XSpec` and `Saxon-HE`. A detailed test report can be viewed with
the browser in `target/xspec-reports/index.html`.

Note, that the tests need intermediate files to be generated in mavens
`generate-resources` phase. If you want to run the tests using saxon,
have a look at the pom-file to see what's needed.


### Releasing ###

Releases of installable packages will be created with github actions
on tags with a name matching the pattern
`v\.[0-9]+\.[0-9]+\.[0-9]+(-.*)?`,
i.e. `v<MAJOR>.<MINOR>.<BUGFIX>[-<SUFFIX>]`. To produce a release:

- first push the branch to be released
- then tag it with the tag name matching the above pattern

This will produce a release on [releases/tag/<TAG_NAME>](releases/tag)
and update the [descriptor
file](https://scdh.github.io/scdh-oxygen-framework/descriptor.xml).

NOTE: The tag name **must equal** the version name in the
[pom.xml](pom.xml)!


# License #

Copyright (c) 2021 Christian Lück

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see
[http://www.gnu.org/licenses/](http://www.gnu.org/licenses/).


## Included Software and other Material ##

The included ![languageicon](frameworks/oxbytei/images/lang-24.png)
icon was desigend by Onur Mustak Cobanli an is distributed on
[http://languageicon.org/](http://languageicon.org/) by under a CC
licence with Relax-Attribution term.

The framework packetized for installation ships with a copy of
[ediarum.JAR](https://github.com/ediarum/ediarum.JAR), which is
distributed under the terms of the GPL v3, written by Martin Fechner
and copyrighted by the Berlin-Brandenburg Academy of Sciences and
Humanities.
