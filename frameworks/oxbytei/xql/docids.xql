xquery version "3.1";

(: Make document ID entities for the whole project :)

declare default element namespace "http://www.tei-c.org/ns/1.0";
declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";
declare option output:method "text";

(: The suffix of the documents, for which document IDs should be generated :)
declare variable $suffix external := '.tei';

(: the project directory root. This should be '${pdu}':)
declare variable $pdu external := '.';

(: the @type of an idno, where the document identifier is given :)
declare variable $docid external := 'document-identifier';

declare variable $colpath := concat($pdu, '?recurse=yes;select=*', $suffix);
declare variable $col := collection($colpath);

for $doc in $col
let $file := base-uri($doc)
where exists($doc//teiHeader//idno[@type eq $docid])
let $id := $doc//teiHeader//idno[@type eq $docid]
return concat('&lt;!ENTITY ', $id, ' "',$file, '"&gt;&#xa;')
