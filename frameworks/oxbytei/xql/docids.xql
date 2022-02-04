xquery version "3.1";

(: Make document ID entities for the whole project :)

import module namespace obt="http://scdh.wwu.de/oxbytei" at 'docid.xqm';

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";
declare option output:method "text";

(: The suffix of the documents, for which document IDs should be generated :)
declare variable $suffix external := '.tei';

(: the project directory root. This should be '${pdu}':)
declare variable $pdu external := '.';

declare variable $colpath := concat($pdu, '?recurse=yes;select=*', $suffix);
declare variable $col := collection($colpath);

for $doc in $col
let $file := base-uri($doc)
where exists(obt:getDocId($doc))
let $id := obt:getDocId($doc)
return concat('&lt;!ENTITY ', $id, ' "',$file, '"&gt;&#xa;')
