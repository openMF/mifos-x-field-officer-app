/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.download
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

actual suspend fun platformPickDirectory(): PlatformFile? {
    // not support in WasmJs
    return null
}

actual fun platformWriteFileToCache(
    fileName: String,
    fileExtension: String,
    filesByteArray: ByteArray,
): Flow<Result<PlatformFile>> = flow {
    emit(Result.failure(IllegalStateException("Platform not supported")))
}

actual fun platformWriteFileToApplicationPrivateInternalStorage(
    fileName: String,
    fileExtension: String,
    filesByteArray: ByteArray,
): Flow<Result<PlatformFile?>> = flow {
    FileKit.download(bytes = filesByteArray, fileName = "$fileName.$fileExtension")
    emit(null)
}.map { Result.success(it) }
    .catch { emit(Result.failure(it)) }

actual fun platformWriteFileToApplicationInternalStorage(
    fileName: String,
    fileExtension: String,
    filesByteArray: ByteArray,
): Flow<Result<PlatformFile?>> = flow {
    FileKit.download(bytes = filesByteArray, fileName = "$fileName.$fileExtension")
    emit(null)
}.map { Result.success(it) }
    .catch { emit(Result.failure(it)) }

actual fun platformWriteToSelectedDirectory(
    filesByteArray: ByteArray,
    platformFile: PlatformFile,
): Flow<Result<Unit>> = flow {
    emit(
        FileKit.download(
            bytes = filesByteArray,
            fileName = "${platformFile.name}.${platformFile.extension}",
        ),
    )
}.map { Result.success(it) }
    .catch { emit(Result.failure(it)) }

actual suspend fun platformDeleteFile(file: PlatformFile) {
    // not support in WasmJs.
}

actual fun platformTakePhoto(): Flow<Result<PlatformFile?>> = flow {
    emit(Result.failure(IllegalStateException("Platform not supported")))
}
