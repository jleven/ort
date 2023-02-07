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

package org.ossreviewtoolkit.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * A container which holds all resolved data which augments ORT's automatically obtained data, like
 * package curations, package configurations, rule violation resolutions and issue resolutions.
 *
 * TODO: Add further data.
 */
data class ResolvedConfiguration(
    /**
     * All package curations applicable to the packages contained in the enclosing [OrtResult] ordered
     * highest-priority-first.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val packageCurations: List<PackageCuration> = emptyList()
)
