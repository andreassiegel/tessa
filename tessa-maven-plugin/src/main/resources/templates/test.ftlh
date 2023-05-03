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

[cols="h,", %autowidth.stretch]
|===
|Description
|${description}

<#if status??>
|Status
|${status}
</#if>

<#if link??>
|Source File Link
|link:${link}[`${path}`]
<#else>
|Source File
|`${path}`
</#if>
|===

<#if furtherInformation??>
${furtherInformation}
</#if>

== Setup

${setup}

== Cleanup

${cleanup}

== Test Cases

<#if disabledNote??>
WARNING: ${disabledNote}
</#if>

<#list testCases?keys as category>
<#if categorized>
=== ${category?index + 1}. ${category}
</#if>

<#list testCases[category] as testCase>
<#if categorized>
==== ${category?index + 1}.${testCase?index + 1}. ${testCase.title}
<#else>
=== ${testCase?index + 1}. ${testCase.title}
</#if>

[cols="h,", %autowidth.stretch]
|===
|Description
|${testCase.description}

<#if testCase.status??>
|Status
|${testCase.status}
</#if>
<#if link??>
|Method Link
|link:${link}#L${testCase.beginLine}-L${testCase.endLine}[`${testCase.methodName}`]
<#else>
|Method
|`${testCase.methodName}`
</#if>
|===

<#if testCase.disabledNote??>
WARNING: ${testCase.disabledNote}
</#if>

<#if testCase.furtherInformation??>
${testCase.furtherInformation}
</#if>

<#list testCase.sections?keys as sectionName>
<#if testCase.sectioned>
<#if categorized>=</#if>==== ${sectionName}

</#if>
<#list testCase.sections[sectionName] as section>
${section.info}

</#list>
</#list>
</#list>
</#list>
