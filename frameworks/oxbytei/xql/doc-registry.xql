xquery version "3.1";

declare namespace output = "w3.org/2010/xslt-xquery-serialization";
(:
declare output:method "text";
:)

(: Make document ID entities for the whole project :)

declare default element namespace "http://www.tei-c.org/ns/1.0";

(: The suffix of the documents, for which document IDs should be generated :)
declare variable $suffix external := '.tei';

(: the project directory root. This should be '${pdu}':)
declare variable $pdu external := '.';

(: the @type of an idno, where the document identifier is given :)
declare variable $docid external := 'document-identifier';

declare variable $colpath := concat($pdu, '?recurse=yes;select=*', $suffix);
declare variable $col := collection($colpath);

<TEI
    xmlns="http://www.tei-c.org/ns/1.0">
    <teiHeader>
        <fileDesc>
            <titleStmt>
                <title>Registry of the project's documents</title>
            </titleStmt>
            <publicationStmt>
                <p>
                </p>
            </publicationStmt>
            <sourceDesc>
                <p>born mechanically again</p>
            </sourceDesc>
        </fileDesc>
    </teiHeader>
    <text>
        <body>
            <list>
                {
                    for $doc in $col
                    let $file := base-uri($doc)
                        where exists($doc//teiHeader//idno[@type eq $docid])
                    let $id := $doc//teiHeader//idno[@type eq $docid]/text()
                    return
                        <item
                            xml:id="{$id}">
                            <idno>{$id}</idno>
                            <ptr
                                target="{
                                        $file
                                        (: concat('&#x26;', $id, ';') :)
                                    }"/>
                        </item>
                }
            </list>
        </body>
    </text>
</TEI>