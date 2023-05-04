= ${title}
:experimental:
:idprefix:
:idseparator: -
ifndef::env-github[:icons: font]
ifdef::env-github,env-browser[]
:toc: preamble
:toclevels: 3
endif::[]
ifdef::env-github[]
:status:
:outfilesuffix: .adoc
:!toc-title:
:important-caption: :exclamation:
:note-caption: :paperclip:
:tip-caption: :bulb:
:warning-caption: :warning:
endif::[]

<#if includesStatus>
[options="header", cols=",,", %autowidth.stretch]
|===
|Title |Description |Status

<#list items as item>
|<<${directory}/${item.filename}#, ${item.title}>>
|${item.description}
|${item.status!}
</#list>
<#else>
[options="header", cols=",", %autowidth.stretch]
|===
|Title |Description

<#list items as item>
|<<${directory}/${item.filename}#, ${item.title}>>
|${item.description}
</#list>
</#if>
|===
