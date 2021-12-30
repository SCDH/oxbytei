# Personography #

If there is a `<prefixDef>` in the header, which defines a URI scheme
with the prefix (protocol) `psn`, `pers`, `prs`, `prsn` or `person`,
an author mode action ![icon](frameworks/oxbytei/images/person-24.png)
for selecting a person from a personography is activated. For example,
put this in the header:

```{xml}
...
<encodingDesc>
	...
	<listPrefixDef>
		<prefixDef
			matchPattern="([a-zA-Z0-9_-]+)"
			replacementPattern="persons.xml#$1"
			ident="psn"/>
	</listPrefixDef>
	...
</encodingDesc>
```

If the linked file `persons.xml` is present, then all the persones
found in `<listPerson>` elements are presented in a selection
dialog. If the caret (pointer) is on a `<persName>` or `<person>`
element, the `@ref` attribute is updated by your selection (and
deleted if you choose the empty name). If the caret is not in such a
context, an empty fragment or a surrounding fragment like this is
created:

```{xml}
<persName ref="psn:BadraddinbalAttar">Badraddīn</persName>
```

In oXygen's text mode, suggestions for the value of `@ref` of
`<persName>` are offered.


## Configuration ##


## Local norm data ##

I strongly encourage providing a personography in a local file as a
broker to global norm data on the WWW, instead of linking to triple
stores on the WWW directly from the TEI documents. See [this
discussion](https://listserv.brown.edu/cgi-bin/wa?A2=ind1711&L=TEI-L&D=0&P=43750)
on the TEI mailing list on the subject. I also encourage defining URI
schemes via
[`<prefixDef>`](https://www.tei-c.org/release/doc/tei-p5-doc/de/html/ref-listPrefixDef.html),
instead of linking to external elements by IDs directly. The prefix
definition serves as an abstraction layer, makes everything explicit,
and thus enables us to write generic tools and actions like
[![icon](frameworks/oxbytei/images/person-24.png)](frameworks/oxbytei/externalAuthorActions/link-person.xml).
