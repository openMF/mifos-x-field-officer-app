/*
 * Copyright 2025 Mifos Initiative
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
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.databasesDir
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

val appCache = FileKit.cacheDir
val appPrivateInternalStorage = FileKit.filesDir
val appInternalStorage = FileKit.databasesDir

actual suspend fun platformPickDirectory(): PlatformFile? {
    return FileKit.openDirectoryPicker()
}

actual fun platformWriteFileToCache(
    fileName: String,
    fileExtension: String,
    filesByteArray: ByteArray,
): Flow<Result<PlatformFile>> = flow {
    val filePath = appCache / "$fileName.$fileExtension"
    filePath.write(filesByteArray)
    emit(filePath)
}.map { Result.success(it) }
    .catch { emit(Result.failure(it)) }

actual fun platformWriteFileToApplicationPrivateInternalStorage(
    fileName: String,
    fileExtension: String,
    filesByteArray: ByteArray,
): Flow<Result<PlatformFile?>> = flow {
    val privateInternalStorage = appPrivateInternalStorage / "$fileName.$fileExtension"
    privateInternalStorage.write(filesByteArray)
    emit(privateInternalStorage)
}.map { Result.success(it) }
    .catch { emit(Result.failure(it)) }

actual fun platformWriteFileToApplicationInternalStorage(
    fileName: String,
    fileExtension: String,
    filesByteArray: ByteArray,
): Flow<Result<PlatformFile?>> = flow {
    val internalStorage = appInternalStorage / "$fileName.$fileExtension"
    internalStorage.write(filesByteArray)
    emit(internalStorage)
}.map { Result.success(it) }
    .catch { emit(Result.failure(it)) }

actual fun platformWriteToSelectedDirectory(
    filesByteArray: ByteArray,
    platformFile: PlatformFile,
): Flow<Result<Unit>> = flow {
    emit(platformFile.write(filesByteArray))
}.map { Result.success(it) }
    .catch { emit(Result.failure(it)) }

actual suspend fun platformDeleteFile(file: PlatformFile) {
    file.delete(false)
}

actual fun platformTakePhoto(): Flow<Result<PlatformFile?>> = flow {
    val result = FileKit.openCameraPicker(
        FileKitCameraType.Photo,
    )
    emit(result)
}.map { Result.success(it) }
    .catch { emit(Result.failure(it)) }
