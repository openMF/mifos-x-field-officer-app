/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 *  Do not pass the path or absolute path you get by using any picker,
 *  to PlatformFile class to create an instance of PlatformFile.
 *  It won't work.
 *  Use PlatformFile object returned by pickers directly.
 */
object FileKitUtil {
    /**
     * Lets the user pick one file with one of [extensions]; emits the file, or null when the picker
     * was dismissed, wrapped in a [Result] that carries a picker failure instead of throwing it.
     */
    fun pickFile(
        extensions: Set<String> = setOf("pdf", "jpeg", "jpg", "png"),
    ): Flow<Result<PlatformFile?>> = flow {
        val file = FileKit.openFilePicker(
            type = FileKitType.File(extensions),
            mode = FileKitMode.Single,
        )
        emit(file)
    }.map { Result.success(it) }
        .catch { emit(Result.failure(it)) }

    /** Lets the user pick one image; emits it, or null when dismissed, as a [Result] like [pickFile]. */
    fun pickImage(): Flow<Result<PlatformFile?>> = flow {
        val image = FileKit.openFilePicker(
            type = FileKitType.Image,
            mode = FileKitMode.Single,
        )
        emit(image)
    }.map { Result.success(it) }
        .catch { emit(Result.failure(it)) }

    /** Opens the camera for one photo; platforms without a camera emit a failure. */
    fun takePhoto(): Flow<Result<PlatformFile?>> = platformTakePhoto()
}

/** The platform's camera capture behind [FileKitUtil.takePhoto]. */
expect fun platformTakePhoto(): Flow<Result<PlatformFile?>>
