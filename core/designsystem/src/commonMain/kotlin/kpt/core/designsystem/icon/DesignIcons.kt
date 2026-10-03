/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Calender: ImageVector
    get() {
        if (calander != null) {
            return calander!!
        }
        calander = ImageVector.Builder(
            name = "Calander",
            defaultWidth = 18.dp,
            defaultHeight = 20.dp,
            viewportWidth = 18f,
            viewportHeight = 20f,
        ).apply {
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(16f, 2f)
                horizontalLineTo(15f)
                verticalLineTo(0f)
                horizontalLineTo(13f)
                verticalLineTo(2f)
                horizontalLineTo(5f)
                verticalLineTo(0f)
                horizontalLineTo(3f)
                verticalLineTo(2f)
                horizontalLineTo(2f)
                curveTo(0.89f, 2f, 0.01f, 2.9f, 0.01f, 4f)
                lineTo(0f, 18f)
                curveTo(0f, 19.1f, 0.89f, 20f, 2f, 20f)
                horizontalLineTo(16f)
                curveTo(17.1f, 20f, 18f, 19.1f, 18f, 18f)
                verticalLineTo(4f)
                curveTo(18f, 2.9f, 17.1f, 2f, 16f, 2f)
                close()
                moveTo(16f, 18f)
                horizontalLineTo(2f)
                verticalLineTo(8f)
                horizontalLineTo(16f)
                verticalLineTo(18f)
                close()
                moveTo(16f, 6f)
                horizontalLineTo(2f)
                verticalLineTo(4f)
                horizontalLineTo(16f)
                verticalLineTo(6f)
                close()
                moveTo(6f, 12f)
                horizontalLineTo(4f)
                verticalLineTo(10f)
                horizontalLineTo(6f)
                verticalLineTo(12f)
                close()
                moveTo(10f, 12f)
                horizontalLineTo(8f)
                verticalLineTo(10f)
                horizontalLineTo(10f)
                verticalLineTo(12f)
                close()
                moveTo(14f, 12f)
                horizontalLineTo(12f)
                verticalLineTo(10f)
                horizontalLineTo(14f)
                verticalLineTo(12f)
                close()
                moveTo(6f, 16f)
                horizontalLineTo(4f)
                verticalLineTo(14f)
                horizontalLineTo(6f)
                verticalLineTo(16f)
                close()
                moveTo(10f, 16f)
                horizontalLineTo(8f)
                verticalLineTo(14f)
                horizontalLineTo(10f)
                verticalLineTo(16f)
                close()
                moveTo(14f, 16f)
                horizontalLineTo(12f)
                verticalLineTo(14f)
                horizontalLineTo(14f)
                verticalLineTo(16f)
                close()
            }
        }.build()

        return calander!!
    }

private var calander: ImageVector? = null

val Edit: ImageVector
    get() {
        if (edit != null) {
            return edit!!
        }
        edit = ImageVector.Builder(
            name = "Edit",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f,
        ).apply {
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(2f, 16f)
                horizontalLineTo(3.425f)
                lineTo(13.2f, 6.225f)
                lineTo(11.775f, 4.8f)
                lineTo(2f, 14.575f)
                verticalLineTo(16f)
                close()
                moveTo(0f, 18f)
                verticalLineTo(13.75f)
                lineTo(13.2f, 0.575f)
                curveTo(13.4f, 0.392f, 13.621f, 0.25f, 13.863f, 0.15f)
                curveTo(14.104f, 0.05f, 14.358f, 0f, 14.625f, 0f)
                curveTo(14.892f, 0f, 15.15f, 0.05f, 15.4f, 0.15f)
                curveTo(15.65f, 0.25f, 15.867f, 0.4f, 16.05f, 0.6f)
                lineTo(17.425f, 2f)
                curveTo(17.625f, 2.183f, 17.771f, 2.4f, 17.862f, 2.65f)
                curveTo(17.954f, 2.9f, 18f, 3.15f, 18f, 3.4f)
                curveTo(18f, 3.667f, 17.954f, 3.921f, 17.862f, 4.162f)
                curveTo(17.771f, 4.404f, 17.625f, 4.625f, 17.425f, 4.825f)
                lineTo(4.25f, 18f)
                horizontalLineTo(0f)
                close()
                moveTo(12.475f, 5.525f)
                lineTo(11.775f, 4.8f)
                lineTo(13.2f, 6.225f)
                lineTo(12.475f, 5.525f)
                close()
            }
        }.build()

        return edit!!
    }

private var edit: ImageVector? = null

val Folder: ImageVector
    get() {
        if (folder != null) {
            return folder!!
        }
        folder = ImageVector.Builder(
            name = "Folder",
            defaultWidth = 20.dp,
            defaultHeight = 16.dp,
            viewportWidth = 20f,
            viewportHeight = 16f,
        ).apply {
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(18f, 2f)
                horizontalLineTo(10f)
                lineTo(8f, 0f)
                horizontalLineTo(2f)
                curveTo(0.9f, 0f, 0.01f, 0.9f, 0.01f, 2f)
                lineTo(0f, 14f)
                curveTo(0f, 15.1f, 0.9f, 16f, 2f, 16f)
                horizontalLineTo(18f)
                curveTo(19.1f, 16f, 20f, 15.1f, 20f, 14f)
                verticalLineTo(4f)
                curveTo(20f, 2.9f, 19.1f, 2f, 18f, 2f)
                close()
                moveTo(18f, 14f)
                horizontalLineTo(2f)
                verticalLineTo(2f)
                horizontalLineTo(7.17f)
                lineTo(9.17f, 4f)
                horizontalLineTo(18f)
                verticalLineTo(14f)
                close()
                moveTo(15.5f, 8.12f)
                verticalLineTo(11.5f)
                horizontalLineTo(12.5f)
                verticalLineTo(6.5f)
                horizontalLineTo(13.88f)
                lineTo(15.5f, 8.12f)
                close()
                moveTo(11f, 5f)
                verticalLineTo(13f)
                horizontalLineTo(17f)
                verticalLineTo(7.5f)
                lineTo(14.5f, 5f)
                horizontalLineTo(11f)
                close()
            }
        }.build()

        return folder!!
    }

private var folder: ImageVector? = null

val ShieldOutlined: ImageVector
    get() {
        if (shieldOutlined != null) {
            return shieldOutlined!!
        }
        shieldOutlined = ImageVector.Builder(
            name = "ShieldOutlined",
            defaultWidth = 16.dp,
            defaultHeight = 20.dp,
            viewportWidth = 16f,
            viewportHeight = 20f,
        ).apply {
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(8f, 0f)
                lineTo(0f, 3f)
                verticalLineTo(9.09f)
                curveTo(0f, 14.14f, 3.41f, 18.85f, 8f, 20f)
                curveTo(12.59f, 18.85f, 16f, 14.14f, 16f, 9.09f)
                verticalLineTo(3f)
                lineTo(8f, 0f)
                close()
                moveTo(14f, 9.09f)
                curveTo(14f, 13.09f, 11.45f, 16.79f, 8f, 17.92f)
                curveTo(4.55f, 16.79f, 2f, 13.1f, 2f, 9.09f)
                verticalLineTo(4.39f)
                lineTo(8f, 2.14f)
                lineTo(14f, 4.39f)
                verticalLineTo(9.09f)
                close()
            }
        }.build()

        return shieldOutlined!!
    }

private var shieldOutlined: ImageVector? = null

val GrownChart: ImageVector
    get() {
        if (grownChart != null) {
            return grownChart!!
        }
        grownChart = ImageVector.Builder(
            name = "GrownChart",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(4f, 5f)
                horizontalLineTo(0f)
                verticalLineTo(16f)
                horizontalLineTo(4f)
                verticalLineTo(5f)
                close()
            }
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(4f, 0f)
                horizontalLineTo(0f)
                verticalLineTo(4f)
                horizontalLineTo(4f)
                verticalLineTo(0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(10f, 3f)
                horizontalLineTo(6f)
                verticalLineTo(7f)
                horizontalLineTo(10f)
                verticalLineTo(3f)
                close()
            }
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(16f, 6f)
                horizontalLineTo(12f)
                verticalLineTo(10f)
                horizontalLineTo(16f)
                verticalLineTo(6f)
                close()
            }
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(16f, 11f)
                horizontalLineTo(12f)
                verticalLineTo(16f)
                horizontalLineTo(16f)
                verticalLineTo(11f)
                close()
            }
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(10f, 8f)
                horizontalLineTo(6f)
                verticalLineTo(16f)
                horizontalLineTo(10f)
                verticalLineTo(8f)
                close()
            }
        }.build()

        return grownChart!!
    }

private var grownChart: ImageVector? = null

val Wallet: ImageVector
    get() {
        if (wallet != null) {
            return wallet!!
        }
        wallet = ImageVector.Builder(
            name = "Wallet",
            defaultWidth = 20.dp,
            defaultHeight = 18.dp,
            viewportWidth = 20f,
            viewportHeight = 18f,
        ).apply {
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(18.5f, 4.28f)
                verticalLineTo(2f)
                curveTo(18.5f, 0.9f, 17.6f, 0f, 16.5f, 0f)
                horizontalLineTo(2.5f)
                curveTo(1.39f, 0f, 0.5f, 0.9f, 0.5f, 2f)
                verticalLineTo(16f)
                curveTo(0.5f, 17.1f, 1.39f, 18f, 2.5f, 18f)
                horizontalLineTo(16.5f)
                curveTo(17.6f, 18f, 18.5f, 17.1f, 18.5f, 16f)
                verticalLineTo(13.72f)
                curveTo(19.09f, 13.37f, 19.5f, 12.74f, 19.5f, 12f)
                verticalLineTo(6f)
                curveTo(19.5f, 5.26f, 19.09f, 4.63f, 18.5f, 4.28f)
                close()
                moveTo(17.5f, 6f)
                verticalLineTo(12f)
                horizontalLineTo(10.5f)
                verticalLineTo(6f)
                horizontalLineTo(17.5f)
                close()
                moveTo(2.5f, 16f)
                verticalLineTo(2f)
                horizontalLineTo(16.5f)
                verticalLineTo(4f)
                horizontalLineTo(10.5f)
                curveTo(9.4f, 4f, 8.5f, 4.9f, 8.5f, 6f)
                verticalLineTo(12f)
                curveTo(8.5f, 13.1f, 9.4f, 14f, 10.5f, 14f)
                horizontalLineTo(16.5f)
                verticalLineTo(16f)
                horizontalLineTo(2.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(13.5f, 10.5f)
                curveTo(14.328f, 10.5f, 15f, 9.828f, 15f, 9f)
                curveTo(15f, 8.172f, 14.328f, 7.5f, 13.5f, 7.5f)
                curveTo(12.672f, 7.5f, 12f, 8.172f, 12f, 9f)
                curveTo(12f, 9.828f, 12.672f, 10.5f, 13.5f, 10.5f)
                close()
            }
        }.build()

        return wallet!!
    }

private var wallet: ImageVector? = null

val GalleryIcon: ImageVector
    get() {
        if (galleryIcon != null) {
            return galleryIcon!!
        }
        galleryIcon = ImageVector.Builder(
            name = "GalleryIcon",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f,
        ).apply {
            path(fill = SolidColor(Color(0xFF33618D))) {
                moveTo(16f, 2f)
                verticalLineTo(16f)
                horizontalLineTo(2f)
                verticalLineTo(2f)
                horizontalLineTo(16f)
                close()
                moveTo(16f, 0f)
                horizontalLineTo(2f)
                curveTo(0.9f, 0f, 0f, 0.9f, 0f, 2f)
                verticalLineTo(16f)
                curveTo(0f, 17.1f, 0.9f, 18f, 2f, 18f)
                horizontalLineTo(16f)
                curveTo(17.1f, 18f, 18f, 17.1f, 18f, 16f)
                verticalLineTo(2f)
                curveTo(18f, 0.9f, 17.1f, 0f, 16f, 0f)
                close()
                moveTo(11.14f, 8.86f)
                lineTo(8.14f, 12.73f)
                lineTo(6f, 10.14f)
                lineTo(3f, 14f)
                horizontalLineTo(15f)
                lineTo(11.14f, 8.86f)
                close()
            }
        }.build()

        return galleryIcon!!
    }

private var galleryIcon: ImageVector? = null

val ClipDocument: ImageVector
    get() {
        if (clipDocument != null) {
            return clipDocument!!
        }
        clipDocument = ImageVector.Builder(
            name = "ClipDocument",
            defaultWidth = 16.dp,
            defaultHeight = 20.dp,
            viewportWidth = 16f,
            viewportHeight = 20f,
        ).apply {
            path(fill = SolidColor(Color(0xFF33618D))) {
                moveTo(10f, 0f)
                horizontalLineTo(2f)
                curveTo(0.9f, 0f, 0f, 0.9f, 0f, 2f)
                verticalLineTo(18f)
                curveTo(0f, 19.1f, 0.9f, 20f, 2f, 20f)
                horizontalLineTo(14f)
                curveTo(15.1f, 20f, 16f, 19.1f, 16f, 18f)
                verticalLineTo(6f)
                lineTo(10f, 0f)
                close()
                moveTo(14f, 18f)
                horizontalLineTo(2f)
                verticalLineTo(2f)
                horizontalLineTo(10f)
                verticalLineTo(6f)
                horizontalLineTo(14f)
                verticalLineTo(18f)
                close()
                moveTo(8f, 15f)
                curveTo(6.9f, 15f, 6f, 14.1f, 6f, 13f)
                verticalLineTo(7.5f)
                curveTo(6f, 7.22f, 6.22f, 7f, 6.5f, 7f)
                curveTo(6.78f, 7f, 7f, 7.22f, 7f, 7.5f)
                verticalLineTo(13f)
                horizontalLineTo(9f)
                verticalLineTo(7.5f)
                curveTo(9f, 6.12f, 7.88f, 5f, 6.5f, 5f)
                curveTo(5.12f, 5f, 4f, 6.12f, 4f, 7.5f)
                verticalLineTo(13f)
                curveTo(4f, 15.21f, 5.79f, 17f, 8f, 17f)
                curveTo(10.21f, 17f, 12f, 15.21f, 12f, 13f)
                verticalLineTo(9f)
                horizontalLineTo(10f)
                verticalLineTo(13f)
                curveTo(10f, 14.1f, 9.1f, 15f, 8f, 15f)
                close()
            }
        }.build()

        return clipDocument!!
    }

private var clipDocument: ImageVector? = null

val RightTick: ImageVector
    get() {
        if (rightTick != null) {
            return rightTick!!
        }
        rightTick = ImageVector.Builder(
            name = "RightTick",
            defaultWidth = 16.dp,
            defaultHeight = 12.dp,
            viewportWidth = 16f,
            viewportHeight = 12f,
        ).apply {
            path(fill = SolidColor(Color(0xFF191C20))) {
                moveTo(5.329f, 9.229f)
                lineTo(1.854f, 5.754f)
                lineTo(0.671f, 6.929f)
                lineTo(5.329f, 11.587f)
                lineTo(15.329f, 1.587f)
                lineTo(14.154f, 0.412f)
                lineTo(5.329f, 9.229f)
                close()
            }
        }.build()

        return rightTick!!
    }

private var rightTick: ImageVector? = null
