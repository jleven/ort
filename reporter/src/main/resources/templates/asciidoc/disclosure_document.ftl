[#--
Copyright (C) 2020 HERE Europe B.V.
Copyright (C) 2020-2021 Bosch.IO GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

SPDX-License-Identifier: Apache-2.0
License-Filename: LICENSE
--]

[#--
The AsciiDoc file generated by this template consists of the following sections:

* The licenses and associated copyrights for all projects merged into a single list.
* The archived license files, licenses and associated copyrights for dependencies listed by package.
* An Appendix of license texts of all above licenses

Excluded projects and packages are ignored.
--]

[#assign ModelExtensions = statics['org.ossreviewtoolkit.model.utils.ExtensionsKt']]

[#-- Add the licenses of the projects. --]
:title-page:
:sectnums:
:toc: preamble

[#assign errorTitle = "DISCLAIMER! THERE ARE UNRESOLVED ISSUES OR UNRESOLVED RULE VIOLATIONS.
    THIS DOCUMENT SHOULD NOT BE DISTRIBUTED UNTIL THESE PROBLEMS ARE RESOLVED."?replace("\n", " ")]

[#--
The alert role needs to be defined in the pdf-theme file, where the color can be customized.
If not present, the text is displayed normally.
--]
= [#if helper.hasUnresolvedIssues() || helper.hasUnresolvedRuleViolations()][.alert]#${errorTitle}#[#else] Disclosure Document[/#if]
:author-name: OSS Review Toolkit
[#assign now = .now]
:revdate: ${now?date?iso_local}
:revnumber: 1.0.0

[#if projects?has_content]
== Acknowledgements
This software includes external packages and source code.
The applicable license information is listed below:

<<<

[#--Merge the licenses and copyrights of all projects into a single list. The default LicenseView.ALL is used because--]
[#--projects cannot have a concluded license (compare with the handling of packages below). --]

[#assign mergedLicenses = helper.mergeLicenses(projects)]
== Project Licenses
[#list mergedLicenses as resolvedLicense]

* License: <<${resolvedLicense.license}, ${resolvedLicense.license}>>

[#assign copyrights = resolvedLicense.getCopyrights(true)]
[#list copyrights as copyright]
** +${copyright}+
[#else]
** No copyright found.
[/#list]

[/#list]
[/#if]
<<<
[#-- Add the licenses of all dependencies. --]

== Dependencies

[#if packages?has_content]
This software depends on external packages and source code.
The applicable license information is listed below:
[/#if]

[#list packages as package]
[#if !package.excluded]
*Dependency*

Package URL: _${ModelExtensions.toPurl(package.id)}_

[#-- List the content of archived license files and associated copyrights. --]
[#list package.licenseFiles.files as licenseFile]

License File: <<${ModelExtensions.toPurl(package.id)} ${licenseFile.path}, ${licenseFile.path}>>

[#assign copyrights = licenseFile.getCopyrights()]
[#list copyrights as copyright]
** +${copyright}+
[#else]
** No copyright found.
[/#list]

[/#list]
[#--
Filter the licenses of the package using LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED. This is the default view which
ignores declared and detected licenses if a license conclusion for the package was made. If copyrights were detected
for a concluded license those statements are kept.
--]
[#assign
resolvedLicenses = package.licensesNotInLicenseFiles(
    LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED.filter(package.license, package.licenseChoices).licenses
)
]
[#if resolvedLicenses?has_content]

The following licenses and copyrights were found in the source code of this package:
[/#if]

[#list resolvedLicenses as resolvedLicense]

[#-- In case of a NOASSERTION license, there is no license text; so do not add a link. --]
[#if helper.isLicensePresent(resolvedLicense)]
* License: <<${resolvedLicense.license}, ${resolvedLicense.license}>>
[#else]
* License: ${resolvedLicense.license}
[/#if]

[#assign copyrights = resolvedLicense.getCopyrights(true)]
[#list copyrights as copyright]
** +${copyright}+
[#else]
** No copyright found.
[/#list]

[/#list]
[/#if]
[/#list]
<<<
[#--
Append the text of all licenses that have been listed in the above lists for licenses and coppyrights
--]
[appendix]
== License Texts

[#assign mergedLicenses = helper.mergeLicenses(projects + packages, LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED, true)]
[#list mergedLicenses as resolvedLicense]
=== ${resolvedLicense.license}

++++
[#assign licenseText = licenseTextProvider.getLicenseText(resolvedLicense.license.simpleLicense())!""]
[#if licenseText?has_content]

[#assign copyrights = resolvedLicense.getCopyrights(true)]
[#list copyrights as copyright]
${copyright}
[/#list]

${licenseText}

[#assign exceptionText = licenseTextProvider.getLicenseText(resolvedLicense.license.exception()!"")!""]
[#if exceptionText?has_content]

${exceptionText}

[/#if]
[/#if]
++++
<<<
[/#list]

== License Files for Packages

[#list packages as package]
[#if !package.excluded]

[#list package.licenseFiles.files as licenseFile]
=== ${ModelExtensions.toPurl(package.id)} ${licenseFile.path}

++++
[#assign copyrights = licenseFile.getCopyrights()]
[#if copyrights?has_content]
[#list copyrights as copyright]
${copyright}
[/#list]
[/#if]

${licenseFile.text}
++++
<<<
[/#list]
[/#if]
[/#list]
