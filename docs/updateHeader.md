# XSL Transformation for Updating the Header #

Your TEI documents' headers may contain the same pieces of information
again and again. Keeping all this redundant information up to date
can be very tedious.

There is a transformation scenario called **oXbytei :: update header**
for this purpose. But it needs a prepared central header file like the
one described below.

As an example, have a look at the following central header file. Note
the `@source` attributes!  `@source="local"` means, that the local
version of the elment and its subtree is to be
preserved. `@source="local*"` means that all siblings with the same
element element name from the local header are to be preserved.

```{xml}
<TEI xmlns="http://www.tei-c.org/ns/1.0" xml:lang="en">
   <teiHeader>
      <fileDesc>
         <titleStmt>
            <title source="local*">Central Header Document</title>
         </titleStmt>
         <publicationStmt>
            <p>Shipped as a sample with a framework</p>
         </publicationStmt>
         <sourceDesc source="local">
            <p>borne again</p>
         </sourceDesc>
      </fileDesc>
      <profileDesc>
         <langUsage>
            <language ident="de" xml:lang="de">deutsch</language>
            <language ident="fr" xml:lang="fr">français</language>
            <language ident="en">english</language>
         </langUsage>
      </profileDesc>
      <encodingDesc>
         <listPrefixDef>
            <prefixDef matchPattern="([a-zA-Z0-9_-]+)" replacementPattern="personography.xml#$1"
               ident="psn"/>
            <prefixDef matchPattern="([a-zA-Z0-9_-]+)" replacementPattern="geography.xml#$1"
               ident="place"/>
         </listPrefixDef>
      <xenoData/>
      <revisionDesc source="local">
         <change/>
      </revisionDesc>
      </encodingDesc>
   </teiHeader>
   <text>
      <body>
         <p>...</p>
      </body>
   </text>
</TEI>
```

And now think of this local TEI document, which's header needs an
update:

```{xml}
<TEI xmlns="http://www.tei-c.org/ns/1.0">
  <teiHeader>
      <fileDesc>
         <titleStmt>
            <title>Stable</title>
            <title type="subtitle">Really stable</title>
            <author>me</author>
         </titleStmt>
         <publicationStmt>
            <p>Publication Information to be Updated</p>
         </publicationStmt>
         <sourceDesc>
            <listWit>
               <witness xml:id="A"/>
               <witness xml:id="B"/>
            </listWit>
         </sourceDesc>
      </fileDesc>
     <revisionDesc>
        <change who="me" when="2021-12-23">Anything done</change>
     </revisionDesc>
  </teiHeader>
  <text>
      <body>
         <p>Some text here.</p>
      </body>
  </text>
</TEI>
```

Merging the header from the central file into the local TEI document
would result in this:

```{xml}
<TEI xmlns="http://www.tei-c.org/ns/1.0">
  <teiHeader>
      <fileDesc>
         <titleStmt>
            <title>Stable</title>
            <title type="subtitle">Really stable</title>
         </titleStmt>
         <publicationStmt>
            <p>Shipped as a sample with a framework</p>
         </publicationStmt>
         <sourceDesc>
            <listWit>
               <witness xml:id="A"/>
               <witness xml:id="B"/>
            </listWit>
         </sourceDesc>
      </fileDesc>
      <profileDesc>
         <langUsage>
            <language ident="de" xml:lang="de">deutsch</language>
            <language ident="fr" xml:lang="fr">français</language>
            <language ident="en">english</language>
         </langUsage>
      </profileDesc>
      <encodingDesc>
         <listPrefixDef>
            <prefixDef matchPattern="([a-zA-Z0-9_-]+)" replacementPattern="personography.xml#$1" ident="psn"/>
            <prefixDef matchPattern="([a-zA-Z0-9_-]+)" replacementPattern="geography.xml#$1" ident="plc"/>
            <prefixDef matchPattern="([a-zA-Z0-9_-]+)" replacementPattern="geography.xml#$1" ident="place"/>
         </listPrefixDef>
      </encodingDesc>
      <xenoData/>
      <revisionDesc>
        <change who="me" when="2021-12-23">Anything done</change>
     </revisionDesc>
   </teiHeader>
  <text>
      <body>
         <p>Some text here.</p>
      </body>
  </text>
</TEI>
```

Provided, 
- that you tag the elements which should be kept in the local file,
  and
- that the header of the local file does not contain paths, which are
  not present,

it's possible to use a [generic XSL
transformation](../frameworks/oxbytei/xsl/updateHeader.xsl), for
merging the non-tagged elements from the central file into the local
document. You can even run this as a mass transformation on all your
TEI documents (except the central header file).

So you can just keep the redundant information in a central file and
are relieved from the burden to keep all the headers up to date.

## How it works ##

The XSL script calculats XPath expressions like
`fileDesc[1]/titleStmt[1]/title[1]` for accessing the corresponding
element in the other file's header. With the starred tag `local*`, the
trailing `\[[0-9]+\]$` of this XPath is removed.

This said, it's clear why the following header tagging leads to a
growing number of `<title>` elements in the local document:

```{xml}
<titleStmt>
	<title type="maintitle">Kritische Schriften</title>
	<title type="subtitle" source="local*">Kritik der Urteilskraft</title>
</titleStmt>
```

So, leave away the star.

TODO: Should there be more complex tagging added? Or should there be
feature added to reproduce subtrees in the local document, for which
are no corresponding elements in the central header?

TODO: Caculate a hash of the central header add put in into an
attribute.

## Further Notes ##

It would be possible to tag the elements in the local file, but then,
we would have all these misused `@source` attributes in the documents
that we want to ship to the outside world.

There are different approaches, to handle the redundancy: 1) linking
the redundant pieces to locations in a project's central header file
using `XInclude`, 2) transforming the header. The first approach has
the downside of a compilation need before shipping the TEI documents
to outside world. Moreover, it's only well suited for including
biggers pieces. But including redundant smaller pieces soon gets as
tedious as keeping them directly in the header.
