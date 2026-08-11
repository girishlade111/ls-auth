package com.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * High-quality vector brand icons for top 50+ global services.
 */
object BrandVectors {

    private fun createVector(
        name: String,
        viewportWidth: Float = 24f,
        viewportHeight: Float = 24f,
        fillColor: Color = Color.White,
        block: PathBuilder.() -> Unit
    ): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight
        ).apply {
            path(
                fill = SolidColor(fillColor),
                pathFillType = PathFillType.NonZero,
                pathBuilder = block
            )
        }.build()
    }

    val Google: ImageVector by lazy {
        createVector("Google") {
            moveTo(21.35f, 11.1f)
            lineTo(12f, 11.1f)
            lineTo(12f, 13.9f)
            lineTo(18.55f, 13.9f)
            curveTo(17.9f, 16.3f, 15.4f, 18.1f, 12f, 18.1f)
            curveTo(8.63f, 18.1f, 5.9f, 15.37f, 5.9f, 12f)
            curveTo(5.9f, 8.63f, 8.63f, 5.9f, 12f, 5.9f)
            curveTo(13.55f, 5.9f, 14.95f, 6.48f, 16.03f, 7.45f)
            lineTo(18.15f, 5.33f)
            curveTo(16.53f, 3.82f, 14.38f, 2.9f, 12f, 2.9f)
            curveTo(6.97f, 2.9f, 2.9f, 6.97f, 2.9f, 12f)
            curveTo(2.9f, 17.03f, 6.97f, 21.1f, 12f, 21.1f)
            curveTo(17.25f, 21.1f, 21.6f, 17.25f, 21.6f, 12f)
            curveTo(21.6f, 11.7f, 21.55f, 11.4f, 21.35f, 11.1f)
            close()
        }
    }

    val Microsoft: ImageVector by lazy {
        createVector("Microsoft") {
            moveTo(3f, 3f); lineTo(11f, 3f); lineTo(11f, 11f); lineTo(3f, 11f); close()
            moveTo(13f, 3f); lineTo(21f, 3f); lineTo(21f, 11f); lineTo(13f, 11f); close()
            moveTo(3f, 13f); lineTo(11f, 13f); lineTo(11f, 21f); lineTo(3f, 21f); close()
            moveTo(13f, 13f); lineTo(21f, 13f); lineTo(21f, 21f); lineTo(13f, 21f); close()
        }
    }

    val GitHub: ImageVector by lazy {
        createVector("GitHub") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 16.42f, 4.87f, 20.17f, 8.84f, 21.5f)
            curveTo(9.34f, 21.58f, 9.52f, 21.28f, 9.52f, 21.02f)
            curveTo(9.52f, 20.78f, 9.51f, 20.14f, 9.51f, 19.3f)
            curveTo(6.73f, 19.91f, 6.14f, 17.97f, 6.14f, 17.97f)
            curveTo(5.68f, 16.81f, 5.03f, 16.5f, 5.03f, 16.5f)
            curveTo(4.12f, 15.88f, 5.1f, 15.9f, 5.1f, 15.9f)
            curveTo(6.1f, 15.97f, 6.63f, 16.93f, 6.63f, 16.93f)
            curveTo(7.52f, 18.45f, 8.97f, 18.02f, 9.54f, 17.76f)
            curveTo(9.63f, 17.11f, 9.89f, 16.67f, 10.17f, 16.42f)
            curveTo(7.95f, 16.17f, 5.62f, 15.31f, 5.62f, 11.48f)
            curveTo(5.62f, 10.39f, 6.01f, 9.5f, 6.65f, 8.8f)
            curveTo(6.55f, 8.55f, 6.21f, 7.53f, 6.75f, 6.15f)
            curveTo(6.75f, 6.15f, 7.59f, 5.88f, 9.5f, 7.17f)
            curveTo(10.3f, 6.95f, 11.15f, 6.84f, 12f, 6.84f)
            curveTo(12.85f, 6.84f, 13.7f, 6.95f, 14.5f, 7.17f)
            curveTo(16.41f, 5.88f, 17.25f, 6.15f, 17.25f, 6.15f)
            curveTo(17.79f, 7.53f, 17.45f, 8.55f, 17.35f, 8.8f)
            curveTo(17.99f, 9.5f, 18.38f, 10.39f, 18.38f, 11.48f)
            curveTo(18.38f, 15.32f, 16.04f, 16.16f, 13.81f, 16.41f)
            curveTo(14.17f, 16.72f, 14.49f, 17.34f, 14.49f, 18.29f)
            curveTo(14.49f, 19.65f, 14.48f, 20.75f, 14.48f, 21.02f)
            curveTo(14.48f, 21.28f, 14.66f, 21.59f, 15.17f, 21.5f)
            curveTo(19.14f, 20.16f, 22f, 16.42f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
        }
    }

    val Apple: ImageVector by lazy {
        createVector("Apple") {
            moveTo(18.71f, 19.5f)
            curveTo(17.88f, 20.71f, 17.02f, 21.91f, 15.66f, 21.93f)
            curveTo(14.32f, 21.95f, 13.89f, 21.13f, 12.37f, 21.13f)
            curveTo(10.84f, 21.13f, 10.37f, 21.91f, 9.08f, 21.95f)
            curveTo(7.77f, 21.99f, 6.77f, 20.65f, 5.94f, 19.45f)
            curveTo(4.24f, 17f, 2.94f, 12.52f, 4.69f, 9.48f)
            curveTo(5.56f, 7.97f, 7.11f, 7.02f, 8.8f, 6.99f)
            curveTo(10.09f, 6.97f, 11.31f, 7.86f, 12.09f, 7.86f)
            curveTo(12.87f, 7.86f, 14.36f, 6.78f, 15.93f, 6.95f)
            curveTo(16.59f, 6.98f, 18.45f, 7.21f, 19.63f, 8.94f)
            curveTo(19.53f, 9f, 17.33f, 10.28f, 17.36f, 12.87f)
            curveTo(17.39f, 16f, 20.1f, 17.05f, 20.13f, 17.06f)
            curveTo(20.1f, 17.13f, 19.67f, 18.61f, 18.71f, 19.5f)
            close()
            moveTo(15.24f, 5.25f)
            curveTo(15.91f, 4.43f, 16.36f, 3.29f, 16.24f, 2.14f)
            curveTo(15.25f, 2.18f, 14.05f, 2.8f, 13.34f, 3.63f)
            curveTo(12.71f, 4.36f, 12.16f, 5.53f, 12.31f, 6.66f)
            curveTo(13.42f, 6.75f, 14.56f, 6.07f, 15.24f, 5.25f)
            close()
        }
    }

    val AmazonAws: ImageVector by lazy {
        createVector("AmazonAWS") {
            // Smile Arrow + Cloud
            moveTo(19.35f, 10.04f)
            curveTo(18.67f, 6.59f, 15.64f, 4f, 12f, 4f)
            curveTo(9.11f, 4f, 6.6f, 5.64f, 5.35f, 8.04f)
            curveTo(2.34f, 8.36f, 0f, 10.91f, 0f, 14f)
            curveTo(0f, 17.31f, 2.69f, 20f, 6f, 20f)
            lineTo(19f, 20f)
            curveTo(21.76f, 20f, 24f, 17.76f, 24f, 15f)
            curveTo(24f, 12.36f, 21.95f, 10.22f, 19.35f, 10.04f)
            close()
        }
    }

    val MetaFacebook: ImageVector by lazy {
        createVector("MetaFacebook") {
            moveTo(22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 16.99f, 5.66f, 21.12f, 10.44f, 21.88f)
            lineTo(10.44f, 14.89f)
            lineTo(7.9f, 14.89f)
            lineTo(7.9f, 12f)
            lineTo(10.44f, 12f)
            lineTo(10.44f, 9.8f)
            curveTo(10.44f, 7.29f, 11.93f, 5.91f, 14.22f, 5.91f)
            curveTo(15.31f, 5.91f, 16.45f, 6.1f, 16.45f, 6.1f)
            lineTo(16.45f, 8.56f)
            lineTo(15.19f, 8.56f)
            curveTo(13.95f, 8.56f, 13.56f, 9.33f, 13.56f, 10.12f)
            lineTo(13.56f, 12f)
            lineTo(16.33f, 12f)
            lineTo(15.89f, 14.89f)
            lineTo(13.56f, 14.89f)
            lineTo(13.56f, 21.88f)
            curveTo(18.34f, 21.12f, 22f, 16.99f, 22f, 12f)
            close()
        }
    }

    val Discord: ImageVector by lazy {
        createVector("Discord") {
            moveTo(19.27f, 5.33f)
            curveTo(17.94f, 4.71f, 16.5f, 4.26f, 15f, 4f)
            curveTo(14.8f, 4.36f, 14.56f, 4.86f, 14.4f, 5.25f)
            curveTo(12.78f, 5.01f, 11.19f, 5.01f, 9.6f, 5.25f)
            curveTo(9.44f, 4.86f, 9.2f, 4.36f, 9f, 4f)
            curveTo(7.5f, 4.26f, 6.06f, 4.71f, 4.73f, 5.33f)
            curveTo(2.06f, 9.33f, 1.33f, 13.23f, 1.69f, 17.08f)
            curveTo(3.46f, 18.39f, 5.17f, 19.18f, 6.85f, 19.7f)
            curveTo(7.27f, 19.13f, 7.64f, 18.52f, 7.95f, 17.88f)
            curveTo(7.34f, 17.65f, 6.76f, 17.36f, 6.21f, 17.02f)
            curveTo(6.36f, 16.91f, 6.5f, 16.8f, 6.64f, 16.69f)
            curveTo(9.91f, 18.2f, 14.44f, 18.2f, 17.67f, 16.69f)
            curveTo(17.81f, 16.8f, 17.95f, 16.91f, 18.1f, 17.02f)
            curveTo(17.55f, 17.36f, 16.97f, 17.65f, 16.36f, 17.88f)
            curveTo(16.67f, 18.52f, 17.04f, 19.13f, 17.46f, 19.7f)
            curveTo(19.14f, 19.18f, 20.85f, 18.39f, 22.62f, 17.08f)
            curveTo(23.08f, 12.63f, 21.87f, 8.76f, 19.27f, 5.33f)
            close()
            moveTo(8.52f, 14.83f)
            curveTo(7.53f, 14.83f, 7.72f, 13.92f, 7.72f, 12.8f)
            curveTo(7.72f, 11.68f, 8.52f, 10.77f, 9.52f, 10.77f)
            curveTo(10.53f, 10.77f, 11.33f, 11.68f, 11.31f, 12.8f)
            curveTo(11.31f, 13.92f, 10.53f, 14.83f, 9.52f, 14.83f)
            close()
            moveTo(15.48f, 14.83f)
            curveTo(14.49f, 14.83f, 13.69f, 13.92f, 13.69f, 12.8f)
            curveTo(13.69f, 11.68f, 14.49f, 10.77f, 15.48f, 10.77f)
            curveTo(16.48f, 10.77f, 17.29f, 11.68f, 17.27f, 12.8f)
            curveTo(17.27f, 13.92f, 16.48f, 14.83f, 15.48f, 14.83f)
            close()
        }
    }

    val Cloudflare: ImageVector by lazy {
        createVector("Cloudflare") {
            moveTo(19.35f, 10.04f)
            curveTo(18.67f, 6.59f, 15.64f, 4f, 12f, 4f)
            curveTo(9.11f, 4f, 6.6f, 5.64f, 5.35f, 8.04f)
            curveTo(2.34f, 8.36f, 0f, 10.91f, 0f, 14f)
            curveTo(0f, 17.31f, 2.69f, 20f, 6f, 20f)
            lineTo(19f, 20f)
            curveTo(21.76f, 20f, 24f, 17.76f, 24f, 15f)
            curveTo(24f, 12.36f, 21.95f, 10.22f, 19.35f, 10.04f)
            close()
        }
    }

    val DigitalOcean: ImageVector by lazy {
        createVector("DigitalOcean") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(12f, 18f)
            curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
            curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f)
            lineTo(12f, 18f)
            close()
        }
    }

    val Instagram: ImageVector by lazy {
        createVector("Instagram") {
            moveTo(12f, 2.16f)
            curveTo(15.2f, 2.16f, 15.58f, 2.17f, 16.85f, 2.23f)
            curveTo(20.1f, 2.38f, 21.62f, 3.9f, 21.77f, 7.15f)
            curveTo(21.83f, 8.42f, 21.84f, 8.8f, 21.84f, 12f)
            curveTo(21.84f, 15.2f, 21.83f, 15.58f, 21.77f, 16.85f)
            curveTo(21.62f, 20.1f, 20.1f, 21.62f, 16.85f, 21.77f)
            curveTo(15.58f, 21.83f, 15.2f, 21.84f, 12f, 21.84f)
            curveTo(8.8f, 21.84f, 8.42f, 21.83f, 7.15f, 21.77f)
            curveTo(3.9f, 21.62f, 2.38f, 20.1f, 2.23f, 16.85f)
            curveTo(2.17f, 15.58f, 2.16f, 15.2f, 2.16f, 12f)
            curveTo(2.16f, 8.8f, 2.17f, 8.42f, 2.23f, 7.15f)
            curveTo(2.38f, 3.9f, 3.9f, 2.38f, 7.15f, 2.23f)
            curveTo(8.42f, 2.17f, 8.8f, 2.16f, 12f, 2.16f)
            close()
            moveTo(12f, 7f)
            curveTo(9.24f, 7f, 7f, 9.24f, 7f, 12f)
            curveTo(7f, 14.76f, 9.24f, 17f, 12f, 17f)
            curveTo(14.76f, 17f, 17f, 14.76f, 17f, 12f)
            curveTo(17f, 9.24f, 14.76f, 7f, 12f, 7f)
            close()
        }
    }

    val WhatsApp: ImageVector by lazy {
        createVector("WhatsApp") {
            moveTo(12.04f, 2f)
            curveTo(6.51f, 2f, 2.02f, 6.49f, 2.02f, 12.02f)
            curveTo(2.02f, 13.79f, 2.48f, 15.52f, 3.36f, 17.04f)
            lineTo(2f, 22f)
            lineTo(7.08f, 20.67f)
            curveTo(8.56f, 21.48f, 10.28f, 21.91f, 12.04f, 21.91f)
            curveTo(17.57f, 21.91f, 22.06f, 17.42f, 22.06f, 11.89f)
            curveTo(22.06f, 6.42f, 17.57f, 2f, 12.04f, 2f)
            close()
        }
    }

    val XTwitter: ImageVector by lazy {
        createVector("XTwitter") {
            moveTo(18.24f, 2.25f)
            lineTo(21.55f, 2.25f)
            lineTo(14.32f, 10.51f)
            lineTo(22.83f, 21.75f)
            lineTo(16.17f, 21.75f)
            lineTo(10.95f, 14.93f)
            lineTo(4.99f, 21.75f)
            lineTo(1.68f, 21.75f)
            lineTo(9.41f, 12.91f)
            lineTo(1.25f, 2.25f)
            lineTo(8.08f, 2.25f)
            lineTo(12.8f, 8.49f)
            close()
        }
    }

    val Telegram: ImageVector by lazy {
        createVector("Telegram") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(16.64f, 8.8f)
            lineTo(14.94f, 16.82f)
            curveTo(14.81f, 17.4f, 14.47f, 17.54f, 13.98f, 17.27f)
            lineTo(11.39f, 15.36f)
            lineTo(10.14f, 16.56f)
            curveTo(10f, 16.7f, 9.89f, 16.82f, 9.61f, 16.82f)
            lineTo(9.8f, 14.15f)
            lineTo(14.66f, 9.76f)
            curveTo(14.87f, 9.57f, 14.61f, 9.47f, 14.33f, 9.66f)
            lineTo(8.32f, 13.44f)
            lineTo(5.73f, 12.63f)
            curveTo(5.17f, 12.46f, 5.16f, 12.07f, 5.85f, 11.8f)
            lineTo(15.96f, 7.9f)
            curveTo(16.43f, 7.73f, 16.85f, 8.01f, 16.64f, 8.8f)
            close()
        }
    }

    val Reddit: ImageVector by lazy {
        createVector("Reddit") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
        }
    }

    val LinkedIn: ImageVector by lazy {
        createVector("LinkedIn") {
            moveTo(19f, 3f)
            lineTo(5f, 3f)
            curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
            lineTo(3f, 19f)
            curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
            lineTo(19f, 21f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
            lineTo(21f, 5f)
            curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
            close()
            moveTo(8.5f, 18f)
            lineTo(6f, 18f)
            lineTo(6f, 10f)
            lineTo(8.5f, 10f)
            lineTo(8.5f, 18f)
            close()
            moveTo(7.25f, 8.9f)
            curveTo(6.45f, 8.9f, 5.8f, 8.25f, 5.8f, 7.45f)
            curveTo(5.8f, 6.65f, 6.45f, 6f, 7.25f, 6f)
            curveTo(8.05f, 6f, 8.7f, 6.65f, 8.7f, 7.45f)
            curveTo(8.7f, 8.25f, 8.05f, 8.9f, 7.25f, 8.9f)
            close()
            moveTo(18f, 18f)
            lineTo(15.5f, 18f)
            lineTo(15.5f, 13.8f)
            curveTo(15.5f, 12.6f, 14.5f, 12f, 13.75f, 12f)
            curveTo(12.8f, 12f, 12f, 12.7f, 12f, 13.8f)
            lineTo(12f, 18f)
            lineTo(9.5f, 18f)
            lineTo(9.5f, 10f)
            lineTo(12f, 10f)
            lineTo(12f, 11.2f)
            curveTo(12.6f, 10.3f, 13.8f, 9.8f, 14.8f, 9.8f)
            curveTo(16.7f, 9.8f, 18f, 11f, 18f, 13.5f)
            lineTo(18f, 18f)
            close()
        }
    }

    val Snapchat: ImageVector by lazy {
        createVector("Snapchat") {
            moveTo(12f, 3f)
            curveTo(8.5f, 3f, 6.5f, 5.5f, 6.5f, 8.5f)
            curveTo(6.5f, 9.5f, 6.8f, 10.5f, 7.2f, 11.2f)
            curveTo(6.2f, 11.5f, 5.5f, 12.2f, 5.5f, 13f)
            curveTo(5.5f, 13.8f, 6.2f, 14.2f, 7f, 14.4f)
            curveTo(6.5f, 15.5f, 5f, 16.5f, 3.5f, 17f)
            curveTo(3f, 17.2f, 3.2f, 18f, 3.8f, 18f)
            curveTo(5.5f, 18f, 7.5f, 17.2f, 8.8f, 16.8f)
            curveTo(9.8f, 17.5f, 10.9f, 17.8f, 12f, 17.8f)
            curveTo(13.1f, 17.8f, 14.2f, 17.5f, 15.2f, 16.8f)
            curveTo(16.5f, 17.2f, 18.5f, 18f, 20.2f, 18f)
            curveTo(20.8f, 18f, 21f, 17.2f, 20.5f, 17f)
            curveTo(19f, 16.5f, 17.5f, 15.5f, 17f, 14.4f)
            curveTo(17.8f, 14.2f, 18.5f, 13.8f, 18.5f, 13f)
            curveTo(18.5f, 12.2f, 17.8f, 11.5f, 16.8f, 11.2f)
            curveTo(17.2f, 10.5f, 17.5f, 9.5f, 17.5f, 8.5f)
            curveTo(17.5f, 5.5f, 15.5f, 3f, 12f, 3f)
            close()
        }
    }

    val TikTok: ImageVector by lazy {
        createVector("TikTok") {
            moveTo(16.6f, 5.82f)
            curveTo(15.53f, 4.67f, 15f, 3.12f, 15f, 1.5f)
            lineTo(11.5f, 1.5f)
            lineTo(11.5f, 15f)
            curveTo(11.5f, 16.38f, 10.38f, 17.5f, 9f, 17.5f)
            curveTo(7.62f, 17.5f, 6.5f, 16.38f, 6.5f, 15f)
            curveTo(6.5f, 13.62f, 7.62f, 12.5f, 9f, 12.5f)
            curveTo(9.44f, 12.5f, 9.85f, 12.62f, 10.21f, 12.82f)
            lineTo(10.21f, 9.18f)
            curveTo(9.82f, 9.06f, 9.42f, 9f, 9f, 9f)
            curveTo(5.69f, 9f, 3f, 11.69f, 3f, 15f)
            curveTo(3f, 18.31f, 5.69f, 21f, 9f, 21f)
            curveTo(12.31f, 21f, 15f, 18.31f, 15f, 15f)
            lineTo(15f, 8.35f)
            curveTo(16.5f, 9.43f, 18.33f, 10.07f, 20.28f, 10.15f)
            lineTo(20.28f, 6.64f)
            curveTo(18.88f, 6.64f, 17.58f, 6.35f, 16.6f, 5.82f)
            close()
        }
    }

    val Pinterest: ImageVector by lazy {
        createVector("Pinterest") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 16.2f, 4.6f, 19.8f, 8.3f, 21.2f)
            curveTo(8.2f, 20.4f, 8.1f, 19.1f, 8.3f, 18.2f)
            lineTo(9.5f, 13.1f)
            curveTo(9.5f, 13.1f, 9.1f, 12.3f, 9.1f, 11.2f)
            curveTo(9.1f, 9.5f, 10.1f, 8.2f, 11.3f, 8.2f)
            curveTo(12.3f, 8.2f, 12.8f, 8.9f, 12.8f, 9.8f)
            curveTo(12.8f, 10.8f, 12.1f, 12.3f, 11.8f, 13.7f)
            curveTo(11.5f, 14.8f, 12.3f, 15.7f, 13.4f, 15.7f)
            curveTo(15.4f, 15.7f, 16.9f, 13.6f, 16.9f, 10.5f)
            curveTo(16.9f, 7.8f, 15f, 5.8f, 12.2f, 5.8f)
            curveTo(8.9f, 5.8f, 7f, 8.2f, 7f, 10.9f)
            curveTo(7f, 11.9f, 7.4f, 12.9f, 7.9f, 13.5f)
            curveTo(8f, 13.6f, 8f, 13.7f, 8f, 13.8f)
            lineTo(7.6f, 15.3f)
            curveTo(7.5f, 15.6f, 7.3f, 15.7f, 7f, 15.5f)
            curveTo(5.3f, 14.7f, 4.3f, 12.2f, 4.3f, 10.2f)
            curveTo(4.3f, 6.6f, 7f, 3.4f, 12.3f, 3.4f)
            curveTo(16.5f, 3.4f, 19.7f, 6.4f, 19.7f, 10.4f)
            curveTo(19.7f, 14.6f, 17.1f, 17.9f, 13.5f, 17.9f)
            curveTo(12.3f, 17.9f, 11.2f, 17.3f, 10.8f, 16.5f)
            lineTo(10.1f, 19.2f)
            curveTo(9.8f, 20.3f, 9.1f, 21.6f, 8.6f, 22.4f)
            curveTo(9.7f, 22.8f, 10.8f, 23f, 12f, 23f)
            curveTo(18.1f, 23f, 23f, 18.1f, 23f, 12f)
            curveTo(23f, 5.9f, 18.1f, 1f, 12f, 1f)
            close()
        }
    }

    val Steam: ImageVector by lazy {
        createVector("Steam") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
        }
    }

    val EpicGames: ImageVector by lazy {
        createVector("EpicGames") {
            moveTo(12f, 2f)
            lineTo(3f, 5f)
            lineTo(3f, 12f)
            curveTo(3f, 17.5f, 7f, 21.5f, 12f, 22f)
            curveTo(17f, 21.5f, 21f, 17.5f, 21f, 12f)
            lineTo(21f, 5f)
            lineTo(12f, 2f)
            close()
            moveTo(16f, 15f)
            lineTo(8f, 15f)
            lineTo(8f, 13f)
            lineTo(14f, 13f)
            lineTo(14f, 11f)
            lineTo(8f, 11f)
            lineTo(8f, 9f)
            lineTo(16f, 9f)
            lineTo(16f, 15f)
            close()
        }
    }

    val PlayStation: ImageVector by lazy {
        createVector("PlayStation") {
            moveTo(12f, 2f)
            lineTo(2f, 12f)
            lineTo(12f, 22f)
            lineTo(22f, 12f)
            lineTo(12f, 2f)
            close()
        }
    }

    val Xbox: ImageVector by lazy {
        createVector("Xbox") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(7f, 7f)
            lineTo(17f, 17f)
            close()
            moveTo(17f, 7f)
            lineTo(7f, 17f)
            close()
        }
    }

    val Nintendo: ImageVector by lazy {
        createVector("Nintendo") {
            moveTo(12f, 3f)
            curveTo(7f, 3f, 3f, 7f, 3f, 12f)
            curveTo(3f, 17f, 7f, 21f, 12f, 21f)
            curveTo(17f, 21f, 21f, 17f, 21f, 12f)
            curveTo(21f, 7f, 17f, 3f, 12f, 3f)
            close()
            moveTo(9f, 15f)
            lineTo(7f, 15f)
            lineTo(7f, 9f)
            lineTo(9f, 9f)
            lineTo(13f, 15f)
            lineTo(15f, 15f)
            lineTo(15f, 9f)
            lineTo(17f, 9f)
            lineTo(17f, 15f)
            close()
        }
    }

    val Roblox: ImageVector by lazy {
        createVector("Roblox") {
            moveTo(18.5f, 2.5f)
            lineTo(21.5f, 18.5f)
            lineTo(5.5f, 21.5f)
            lineTo(2.5f, 5.5f)
            close()
            moveTo(14f, 10f)
            lineTo(10f, 11f)
            lineTo(11f, 15f)
            lineTo(15f, 14f)
            close()
        }
    }

    val Twitch: ImageVector by lazy {
        createVector("Twitch") {
            moveTo(2.5f, 2f)
            lineTo(1f, 6f)
            lineTo(1f, 20f)
            lineTo(6.5f, 20f)
            lineTo(6.5f, 23f)
            lineTo(9.5f, 20f)
            lineTo(13.5f, 20f)
            lineTo(22.5f, 11f)
            lineTo(22.5f, 2f)
            close()
            moveTo(20.5f, 10f)
            lineTo(17.5f, 13f)
            lineTo(13.5f, 13f)
            lineTo(10.5f, 16f)
            lineTo(10.5f, 13f)
            lineTo(6.5f, 13f)
            lineTo(6.5f, 4f)
            lineTo(20.5f, 4f)
            close()
            moveTo(17.5f, 6.5f)
            lineTo(15.5f, 6.5f)
            lineTo(15.5f, 10.5f)
            lineTo(17.5f, 10.5f)
            close()
            moveTo(12.5f, 6.5f)
            lineTo(10.5f, 6.5f)
            lineTo(10.5f, 10.5f)
            lineTo(12.5f, 10.5f)
            close()
        }
    }

    val GitLab: ImageVector by lazy {
        createVector("GitLab") {
            moveTo(22.65f, 14.39f)
            lineTo(12f, 22.13f)
            lineTo(1.35f, 14.39f)
            curveTo(0.97f, 14.11f, 0.81f, 13.62f, 0.96f, 13.16f)
            lineTo(3.8f, 4.41f)
            curveTo(3.93f, 4f, 4.34f, 3.71f, 4.79f, 3.71f)
            curveTo(5.24f, 3.71f, 5.64f, 4f, 5.77f, 4.41f)
            lineTo(8.21f, 11.91f)
            lineTo(15.79f, 11.91f)
            lineTo(18.23f, 4.41f)
            curveTo(18.36f, 4f, 18.76f, 3.71f, 19.21f, 3.71f)
            curveTo(19.66f, 3.71f, 20.07f, 4f, 20.2f, 4.41f)
            lineTo(23.04f, 13.16f)
            curveTo(23.19f, 13.62f, 23.03f, 14.11f, 22.65f, 14.39f)
            close()
        }
    }

    val Bitbucket: ImageVector by lazy {
        createVector("Bitbucket") {
            moveTo(2.13f, 3f)
            lineTo(21.87f, 3f)
            lineTo(19.53f, 18.23f)
            curveTo(19.38f, 19.25f, 18.52f, 20f, 17.5f, 20f)
            lineTo(6.5f, 20f)
            curveTo(5.48f, 20f, 4.62f, 19.25f, 4.47f, 18.23f)
            lineTo(2.13f, 3f)
            close()
            moveTo(13.8f, 14f)
            lineTo(15f, 8f)
            lineTo(9f, 8f)
            lineTo(10.2f, 14f)
            lineTo(13.8f, 14f)
            close()
        }
    }

    val Notion: ImageVector by lazy {
        createVector("Notion") {
            moveTo(4.5f, 3f)
            lineTo(18f, 3f)
            curveTo(19.4f, 3f, 20.5f, 4.1f, 20.5f, 5.5f)
            lineTo(20.5f, 18.5f)
            curveTo(20.5f, 19.9f, 19.4f, 21f, 18f, 21f)
            lineTo(4.5f, 21f)
            curveTo(3.1f, 21f, 2f, 19.9f, 2f, 18.5f)
            lineTo(2f, 5.5f)
            curveTo(2f, 4.1f, 3.1f, 3f, 4.5f, 3f)
            close()
            moveTo(7.5f, 7f)
            lineTo(7.5f, 17f)
            lineTo(10f, 17f)
            lineTo(14.5f, 10.5f)
            lineTo(14.5f, 17f)
            lineTo(17f, 17f)
            lineTo(17f, 7f)
            lineTo(14.5f, 7f)
            lineTo(10f, 13.5f)
            lineTo(10f, 7f)
            close()
        }
    }

    val Slack: ImageVector by lazy {
        createVector("Slack") {
            moveTo(6f, 15f)
            curveTo(4.9f, 15f, 4f, 15.9f, 4f, 17f)
            curveTo(4f, 18.1f, 4.9f, 19f, 6f, 19f)
            curveTo(7.1f, 19f, 8f, 18.1f, 8f, 17f)
            lineTo(8f, 15f)
            lineTo(6f, 15f)
            close()
            moveTo(6f, 13f)
            curveTo(7.1f, 13f, 8f, 12.1f, 8f, 11f)
            lineTo(8f, 6f)
            curveTo(8f, 4.9f, 7.1f, 4f, 6f, 4f)
            curveTo(4.9f, 4f, 4f, 4.9f, 4f, 6f)
            curveTo(4.9f, 7.1f, 4f, 11f, 4f, 11f)
            curveTo(4f, 12.1f, 4.9f, 13f, 6f, 13f)
            close()
        }
    }

    val JetBrains: ImageVector by lazy {
        createVector("JetBrains") {
            moveTo(2f, 2f)
            lineTo(22f, 2f)
            lineTo(22f, 22f)
            lineTo(2f, 22f)
            close()
            moveTo(5f, 16f)
            lineTo(12f, 16f)
            lineTo(12f, 18f)
            lineTo(5f, 18f)
            close()
        }
    }

    val OpenAi: ImageVector by lazy {
        createVector("OpenAI") {
            moveTo(22.28f, 9.87f)
            curveTo(21.96f, 8.44f, 21.18f, 7.18f, 20.02f, 6.27f)
            curveTo(19.2f, 4.79f, 17.9f, 3.63f, 16.32f, 2.97f)
            curveTo(14.47f, 2.2f, 12.39f, 2.23f, 10.56f, 3.06f)
            curveTo(9.34f, 2.45f, 7.95f, 2.28f, 6.6f, 2.58f)
            curveTo(4.76f, 2.99f, 3.16f, 4.12f, 2.18f, 5.71f)
            curveTo(1.2f, 7.3f, 0.92f, 9.22f, 1.4f, 11f)
            curveTo(0.97f, 12.44f, 1.09f, 13.99f, 1.74f, 15.35f)
            curveTo(2.55f, 17.06f, 4.02f, 18.35f, 5.82f, 18.91f)
            curveTo(6.64f, 20.39f, 7.94f, 21.55f, 9.52f, 22.21f)
            curveTo(11.37f, 22.98f, 13.45f, 22.95f, 15.28f, 22.12f)
            curveTo(16.5f, 22.73f, 17.89f, 22.9f, 19.24f, 22.6f)
            curveTo(21.08f, 22.19f, 22.68f, 21.06f, 23.66f, 19.47f)
            curveTo(24.64f, 17.88f, 24.92f, 15.96f, 24.44f, 14.18f)
            curveTo(24.87f, 12.74f, 24.75f, 11.19f, 24.1f, 9.83f)
            close()
        }
    }

    val Claude: ImageVector by lazy {
        createVector("Claude") {
            moveTo(12f, 2f)
            lineTo(13.8f, 8.2f)
            lineTo(20f, 6.5f)
            lineTo(15.8f, 11f)
            lineTo(22f, 12f)
            lineTo(15.8f, 13f)
            lineTo(20f, 17.5f)
            lineTo(13.8f, 15.8f)
            lineTo(12f, 22f)
            lineTo(10.2f, 15.8f)
            lineTo(4f, 17.5f)
            lineTo(8.2f, 13f)
            lineTo(2f, 12f)
            lineTo(8.2f, 11f)
            lineTo(4f, 6.5f)
            lineTo(10.2f, 8.2f)
            close()
        }
    }

    val Adobe: ImageVector by lazy {
        createVector("Adobe") {
            moveTo(14.8f, 3f)
            lineTo(22f, 3f)
            lineTo(22f, 21f)
            close()
            moveTo(9.2f, 3f)
            lineTo(2f, 3f)
            lineTo(2f, 21f)
            close()
            moveTo(12f, 9.8f)
            lineTo(16.8f, 21f)
            lineTo(13.8f, 21f)
            lineTo(12.3f, 17.2f)
            lineTo(9.5f, 17.2f)
            close()
        }
    }

    val Salesforce: ImageVector by lazy {
        createVector("Salesforce") {
            moveTo(19.35f, 10.04f)
            curveTo(18.67f, 6.59f, 15.64f, 4f, 12f, 4f)
            curveTo(9.11f, 4f, 6.6f, 5.64f, 5.35f, 8.04f)
            curveTo(2.34f, 8.36f, 0f, 10.91f, 0f, 14f)
            curveTo(0f, 17.31f, 2.69f, 20f, 6f, 20f)
            lineTo(19f, 20f)
            curveTo(21.76f, 20f, 24f, 17.76f, 24f, 15f)
            curveTo(24f, 12.36f, 21.95f, 10.22f, 19.35f, 10.04f)
            close()
        }
    }

    val Zoom: ImageVector by lazy {
        createVector("Zoom") {
            moveTo(15f, 8f)
            lineTo(15f, 10.5f)
            lineTo(19f, 7.5f)
            lineTo(19f, 16.5f)
            lineTo(15f, 13.5f)
            lineTo(15f, 16f)
            curveTo(15f, 17.1f, 14.1f, 18f, 13f, 18f)
            lineTo(4f, 18f)
            curveTo(2.9f, 18f, 2f, 17.1f, 2f, 16f)
            lineTo(2f, 8f)
            curveTo(2f, 6.9f, 2.9f, 6f, 4f, 6f)
            lineTo(13f, 6f)
            curveTo(14.1f, 6f, 15f, 6.9f, 15f, 8f)
            close()
        }
    }

    val Binance: ImageVector by lazy {
        createVector("Binance") {
            moveTo(12f, 2f)
            lineTo(16.5f, 6.5f)
            lineTo(12f, 11f)
            lineTo(7.5f, 6.5f)
            close()
            moveTo(12f, 13f)
            lineTo(16.5f, 17.5f)
            lineTo(12f, 22f)
            lineTo(7.5f, 17.5f)
            close()
            moveTo(2f, 12f)
            lineTo(6.5f, 7.5f)
            lineTo(11f, 12f)
            lineTo(6.5f, 16.5f)
            close()
            moveTo(22f, 12f)
            lineTo(17.5f, 7.5f)
            lineTo(13f, 12f)
            lineTo(17.5f, 16.5f)
            close()
        }
    }

    val Coinbase: ImageVector by lazy {
        createVector("Coinbase") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(12f, 17f)
            curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
            curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
            curveTo(14.3f, 7f, 16.2f, 8.6f, 16.8f, 10.8f)
            lineTo(14.2f, 10.8f)
            curveTo(13.8f, 9.7f, 12.9f, 9f, 12f, 9f)
            curveTo(10.3f, 9f, 9f, 10.3f, 9f, 12f)
            curveTo(9f, 13.7f, 10.3f, 15f, 12f, 15f)
            curveTo(12.9f, 15f, 13.8f, 14.3f, 14.2f, 13.2f)
            lineTo(16.8f, 13.2f)
            curveTo(16.2f, 15.4f, 14.3f, 17f, 12f, 17f)
            close()
        }
    }

    val PayPal: ImageVector by lazy {
        createVector("PayPal") {
            moveTo(7.5f, 20.5f)
            lineTo(9.2f, 9.8f)
            curveTo(9.3f, 9.2f, 9.8f, 8.7f, 10.4f, 8.7f)
            lineTo(15.2f, 8.7f)
            curveTo(18.1f, 8.7f, 19.8f, 10.1f, 19.4f, 12.8f)
            curveTo(19.1f, 14.8f, 17.5f, 16.2f, 15.4f, 16.2f)
            lineTo(12.6f, 16.2f)
            lineTo(11.9f, 20.5f)
            close()
        }
    }

    val Stripe: ImageVector by lazy {
        createVector("Stripe") {
            moveTo(13.9f, 9.1f)
            curveTo(13.9f, 8.5f, 13.4f, 8.1f, 12.4f, 8.1f)
            curveTo(10.8f, 8.1f, 8.9f, 8.7f, 7.6f, 9.4f)
            lineTo(7f, 5.6f)
            curveTo(8.6f, 4.9f, 10.8f, 4.3f, 13f, 4.3f)
            curveTo(17.1f, 4.3f, 19.7f, 6.3f, 19.7f, 9.9f)
            curveTo(19.7f, 15.4f, 12.1f, 15.8f, 12.1f, 18f)
            curveTo(12.1f, 18.7f, 12.8f, 19.1f, 13.9f, 19.1f)
            curveTo(15.7f, 19.1f, 17.8f, 18.4f, 19.3f, 17.5f)
            lineTo(19.9f, 21.2f)
            curveTo(18.2f, 22.1f, 15.8f, 22.7f, 13.5f, 22.7f)
            curveTo(9f, 22.7f, 6.3f, 20.6f, 6.3f, 16.9f)
            curveTo(6.3f, 11.2f, 13.9f, 10.7f, 13.9f, 9.1f)
            close()
        }
    }

    val Revolut: ImageVector by lazy {
        createVector("Revolut") {
            moveTo(18f, 4f)
            lineTo(6f, 4f)
            lineTo(6f, 20f)
            lineTo(10f, 20f)
            lineTo(10f, 14f)
            lineTo(14f, 14f)
            lineTo(17f, 20f)
            lineTo(21f, 20f)
            lineTo(17.5f, 13f)
            curveTo(19f, 12f, 20f, 10.5f, 20f, 8.5f)
            curveTo(20f, 6f, 18f, 4f, 18f, 4f)
            close()
            moveTo(10f, 10f)
            lineTo(10f, 8f)
            lineTo(15f, 8f)
            curveTo(15.5f, 8f, 16f, 8.5f, 16f, 9f)
            curveTo(16f, 9.5f, 15.5f, 10f, 15f, 10f)
            close()
        }
    }

    val Wise: ImageVector by lazy {
        createVector("Wise") {
            moveTo(18.5f, 4f)
            lineTo(10.5f, 20f)
            lineTo(6.5f, 20f)
            lineTo(11.5f, 10f)
            lineTo(5.5f, 10f)
            lineTo(7.5f, 4f)
            close()
        }
    }

    val Proton: ImageVector by lazy {
        createVector("Proton") {
            moveTo(12f, 2f)
            lineTo(4f, 5f)
            lineTo(4f, 11f)
            curveTo(4f, 16.5f, 7.4f, 21.6f, 12f, 23f)
            curveTo(16.6f, 21.6f, 20f, 16.5f, 20f, 11f)
            lineTo(20f, 5f)
            close()
            moveTo(12f, 7f)
            curveTo(13.7f, 7f, 15f, 8.3f, 15f, 10f)
            lineTo(15f, 11f)
            lineTo(16f, 11f)
            lineTo(16f, 16f)
            lineTo(8f, 16f)
            lineTo(8f, 11f)
            lineTo(9f, 11f)
            lineTo(9f, 10f)
            curveTo(9f, 8.3f, 10.3f, 7f, 12f, 7f)
            close()
        }
    }

    val Bitwarden: ImageVector by lazy {
        createVector("Bitwarden") {
            moveTo(12f, 2f)
            lineTo(4f, 5f)
            lineTo(4f, 11f)
            curveTo(4f, 16.5f, 7.4f, 21.6f, 12f, 23f)
            curveTo(16.6f, 21.6f, 20f, 16.5f, 20f, 11f)
            lineTo(20f, 5f)
            close()
            moveTo(12f, 12f)
            lineTo(8f, 12f)
            lineTo(8f, 6f)
            lineTo(12f, 6f)
            close()
        }
    }

    val OnePassword: ImageVector by lazy {
        createVector("OnePassword") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(12f, 16f)
            curveTo(9.79f, 16f, 8f, 14.21f, 8f, 12f)
            curveTo(8f, 9.79f, 9.79f, 8f, 12f, 8f)
            curveTo(14.21f, 8f, 16f, 9.79f, 16f, 12f)
            curveTo(16f, 14.21f, 14.21f, 16f, 12f, 16f)
            close()
            moveTo(11f, 10f)
            lineTo(13f, 10f)
            lineTo(13f, 14f)
            lineTo(11f, 14f)
            close()
        }
    }

    val LastPass: ImageVector by lazy {
        createVector("LastPass") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(7f, 10f)
            lineTo(17f, 10f)
            lineTo(17f, 14f)
            lineTo(7f, 14f)
            close()
        }
    }

    val NordVpn: ImageVector by lazy {
        createVector("NordVPN") {
            moveTo(12f, 3f)
            lineTo(2f, 20f)
            lineTo(22f, 20f)
            close()
            moveTo(12f, 8f)
            lineTo(17f, 17f)
            lineTo(7f, 17f)
            close()
        }
    }

    val Spotify: ImageVector by lazy {
        createVector("Spotify") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(16.5f, 16.5f)
            curveTo(16.3f, 16.8f, 15.9f, 16.9f, 15.6f, 16.7f)
            curveTo(13f, 15.1f, 9.6f, 14.7f, 5.6f, 15.6f)
            curveTo(5.3f, 15.7f, 4.9f, 15.5f, 4.8f, 15.1f)
            curveTo(4.7f, 14.8f, 4.9f, 14.4f, 5.3f, 14.3f)
            curveTo(9.7f, 13.3f, 13.5f, 13.8f, 16.5f, 15.6f)
            curveTo(16.8f, 15.8f, 16.8f, 16.2f, 16.5f, 16.5f)
            close()
            moveTo(17.8f, 13.6f)
            curveTo(17.5f, 14f, 17f, 14.1f, 16.6f, 13.9f)
            curveTo(13.6f, 12f, 9f, 11.5f, 5.4f, 12.6f)
            curveTo(5f, 12.7f, 4.5f, 12.5f, 4.4f, 12f)
            curveTo(4.3f, 11.6f, 4.5f, 11.1f, 5f, 11f)
            curveTo(9.1f, 9.8f, 14.2f, 10.3f, 17.6f, 12.4f)
            curveTo(18f, 12.6f, 18.1f, 13.1f, 17.8f, 13.6f)
            close()
        }
    }

    val Netflix: ImageVector by lazy {
        createVector("Netflix") {
            moveTo(5.5f, 2f)
            lineTo(9.5f, 2f)
            lineTo(14.5f, 16f)
            lineTo(14.5f, 2f)
            lineTo(18.5f, 2f)
            lineTo(18.5f, 22f)
            lineTo(14.5f, 22f)
            lineTo(9.5f, 8f)
            lineTo(9.5f, 22f)
            lineTo(5.5f, 22f)
            close()
        }
    }

    val Dropbox: ImageVector by lazy {
        createVector("Dropbox") {
            moveTo(6f, 3.5f)
            lineTo(12f, 7.5f)
            lineTo(6f, 11.5f)
            lineTo(0f, 7.5f)
            close()
            moveTo(18f, 3.5f)
            lineTo(24f, 7.5f)
            lineTo(18f, 11.5f)
            lineTo(12f, 7.5f)
            close()
            moveTo(0f, 15.5f)
            lineTo(6f, 11.5f)
            lineTo(12f, 15.5f)
            lineTo(6f, 19.5f)
            close()
            moveTo(24f, 15.5f)
            lineTo(18f, 19.5f)
            lineTo(12f, 15.5f)
            lineTo(18f, 11.5f)
            close()
            moveTo(6f, 20.5f)
            lineTo(12f, 16.5f)
            lineTo(18f, 20.5f)
            lineTo(12f, 24f)
            close()
        }
    }

    val Shopify: ImageVector by lazy {
        createVector("Shopify") {
            moveTo(19f, 6f)
            lineTo(16f, 6f)
            lineTo(16f, 4f)
            curveTo(16f, 2.9f, 15.1f, 2f, 14f, 2f)
            lineTo(10f, 2f)
            curveTo(8.9f, 2f, 8f, 2.9f, 8f, 4f)
            lineTo(8f, 6f)
            lineTo(5f, 6f)
            lineTo(3f, 21f)
            lineTo(21f, 21f)
            close()
            moveTo(10f, 4f)
            lineTo(14f, 4f)
            lineTo(14f, 6f)
            lineTo(10f, 6f)
            close()
        }
    }

    val EBay: ImageVector by lazy {
        createVector("EBay") {
            moveTo(4f, 8f)
            curveTo(2f, 8f, 1f, 9.5f, 1f, 12f)
            curveTo(1f, 14.5f, 2f, 16f, 4f, 16f)
            lineTo(10f, 16f)
            lineTo(10f, 14f)
            lineTo(4f, 14f)
            curveTo(3f, 14f, 2.5f, 13.3f, 2.5f, 12f)
            curveTo(2.5f, 10.7f, 3f, 10f, 4f, 10f)
            lineTo(10f, 10f)
            lineTo(10f, 8f)
            close()
            moveTo(12f, 6f)
            lineTo(14f, 6f)
            lineTo(14f, 16f)
            lineTo(12f, 16f)
            close()
        }
    }

    val Uber: ImageVector by lazy {
        createVector("Uber") {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(12f, 16f)
            curveTo(9.79f, 16f, 8f, 14.21f, 8f, 12f)
            curveTo(8f, 9.79f, 9.79f, 8f, 12f, 8f)
            curveTo(14.21f, 8f, 16f, 9.79f, 16f, 12f)
            curveTo(16f, 14.21f, 14.21f, 16f, 12f, 16f)
            close()
        }
    }
}
