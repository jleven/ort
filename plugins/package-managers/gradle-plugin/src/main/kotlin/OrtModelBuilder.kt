/*
 * Copyright (C) 2023 The ORT Project Authors (see <https://github.com/oss-review-toolkit/ort/blob/main/NOTICE>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package org.ossreviewtoolkit.plugins.packagemanagers.gradleplugin

import OrtDependency
import OrtDependencyTreeModel

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import org.gradle.api.internal.artifacts.DefaultProjectComponentIdentifier
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier
import org.gradle.internal.deprecation.DeprecatableConfiguration
import org.gradle.tooling.provider.model.ToolingModelBuilder
import org.gradle.util.GradleVersion

class OrtModelBuilder : ToolingModelBuilder {
    private val listener = object : DependencyResolutionListener {
        override fun beforeResolve(dependencies: ResolvableDependencies) {
            TODO("Not yet implemented")
        }

        override fun afterResolve(dependencies: ResolvableDependencies) {
            TODO("Not yet implemented")
        }
    }

    override fun canBuild(modelName: String): Boolean =
        modelName == OrtDependencyTreeModel::class.java.name

    private fun Configuration.isResolvable(): Boolean {
        val canBeResolved = GradleVersion.current() < GradleVersion.version("3.3") || isCanBeResolved
        val isDeprecatedConfiguration = this is DeprecatableConfiguration && resolutionAlternatives != null
        return canBeResolved && !isDeprecatedConfiguration
    }

    private fun Collection<DependencyResult>.toOrtDependencies(): List<OrtDependency> =
        if (GradleVersion.current() < GradleVersion.version("5.1")) {
            this
        } else {
            filterNot { it.isConstraint }
        }.mapNotNull { d ->
            when (d) {
                is ResolvedDependencyResult -> {
                    val selectedComponent = d.selected
                    val id = selectedComponent.id

                    when (id) {
                        is DefaultModuleComponentIdentifier -> {
                            OrtDependencyImpl(
                                groupId = id.moduleIdentifier.group,
                                artifactId = id.moduleIdentifier.name,
                                version = id.version,
                                classifier = "",
                                extension = "",
                                dependencies = selectedComponent.dependencies.toOrtDependencies(),
                                error = null,
                                warning = null,
                                pomFile = "",
                                localPath = null
                            )
                        }

                        is DefaultProjectComponentIdentifier -> {
                            OrtDependencyImpl(
                                groupId = selectedComponent.moduleVersion?.group.orEmpty(),
                                artifactId = id.projectName,
                                version = selectedComponent.moduleVersion?.version.orEmpty(),
                                classifier = "",
                                extension = "",
                                dependencies = selectedComponent.dependencies.toOrtDependencies(),
                                error = null,
                                warning = null,
                                pomFile = "",
                                localPath = id.projectPath
                            )
                        }

                        else -> throw IllegalStateException()
                    }
                }

                is UnresolvedDependencyResult -> {
                    // Create an issue.
                    null
                }

                else -> throw IllegalStateException()
            }
        }

    override fun buildAll(modelName: String, project: Project): OrtDependencyTreeModel {
        //project.gradle.useLogger(listener)

        val resolvableConfigurations = project.configurations.filter { it.isResolvable() }

        val ortConfigurations = resolvableConfigurations.mapNotNull { c ->
            val resolvableDependencies = c.incoming

            // Get the root of resolved dependency graph. This is also what Gradle's own "dependencies" tasks uses to
            // recursively obtain information about resolved dependencies.
            val root = resolvableDependencies.resolutionResult.root

            //resolvableDependencies.artifactView { it.lenient(true) }. .artifacts.resolvedArtifacts.get().map { it. }

            // Omit configurations without dependencies.
            root.dependencies.takeUnless { it.isEmpty() }?.let { d ->
                OrtConfigurationImpl(name = c.name, dependencies = d.toOrtDependencies())
            }
        }

        return OrtDependencyTreeModelImpl(
            group = project.group.toString(),
            name = project.name,
            version = project.version.toString(),
            configurations = ortConfigurations,
            repositories = emptyList(),
            errors = emptyList(),
            warnings = emptyList()
        )
    }
}
