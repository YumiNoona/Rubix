package com.cubeguide.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cubeguide.core.*
import com.cubeguide.play.VirtualCube
import com.cubeguide.play.VirtualMove
import kotlin.math.*

private data class VP(val x: Float, val y: Float, val z: Float) {
    operator fun plus(v: VP) = VP(x + v.x, y + v.y, z + v.z)
    operator fun times(k: Float) = VP(x * k, y * k, z * k)
    fun cross(v: VP) = VP(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x)
    fun dot(v: VP) = x * v.x + y * v.y + z * v.z
}

private fun Vec.vp() = VP(x.toFloat(), y.toFloat(), z.toFloat())
private data class VQuad(val points: List<VP>, val color: Color, val sticker: Boolean)

/**
 * One renderer for every regular cube size. It derives its scale from the projected
 * geometry, so a 2x2 and a 7x7 occupy the same safe viewport without clipping.
 */
@Composable
internal fun VirtualCubeView(
    cube: VirtualCube,
    modifier: Modifier = Modifier,
    move: VirtualMove? = null,
    replay: Int = 0,
    viewReset: Int = 0,
    onAnimationProgress: (Float) -> Unit = {},
) {
    val preferences = LocalAppPreferences.current
    var yaw by remember { mutableFloatStateOf(-0.58f) }
    var pitch by remember { mutableFloatStateOf(0.48f) }
    val turn = remember(cube.stickers, move) { Animatable(0f) }
    val progressCallback by rememberUpdatedState(onAnimationProgress)

    LaunchedEffect(viewReset) { yaw=-0.58f;pitch=0.48f }

    LaunchedEffect(turn) {
        snapshotFlow { turn.value }.collect { progressCallback(it) }
    }
    LaunchedEffect(turn, replay) {
        if (move != null) {
            turn.snapTo(0f)
            if(move.turns==2) { turn.animateTo(.5f,tween(preferences.animationMillis));delay(180) }
            turn.animateTo(1f,tween(preferences.animationMillis))
        }
    }

    Canvas(
        modifier
            .clipToBounds()
            .semantics {
                contentDescription = "Interactive ${cube.size} by ${cube.size} virtual cube. Drag to rotate."
            }
            .pointerInput(move) {
                if (move == null) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        yaw += drag.x * .008f
                        pitch = (pitch + drag.y * .008f).coerceIn(-1.18f, 1.18f)
                    }
                }
            },
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0x2939E4BB), Color.Transparent),
                center = Offset(size.width * .5f, size.height * .46f),
                radius = size.minDimension * .46f,
            ),
            radius = size.minDimension * .46f,
            center = Offset(size.width * .5f, size.height * .46f),
        )
        drawOval(
            Color(0x5C000000),
            topLeft = Offset(size.width * .24f, size.height * .79f),
            size = androidx.compose.ui.geometry.Size(size.width * .52f, size.height * .07f),
        )

        val edge = cube.size - 1f
        val axis = move?.let { Geometry.normals[it.face.ordinal].vp() }
        val movingLayers = move?.let { value ->
            (value.depth until value.depth + value.width).map { edge - 2f * it }
        }.orEmpty()
        val quarterTurns = move?.let { if (it.turns == 3) -1 else it.turns } ?: 0
        val angle = -turn.value * quarterTurns * PI.toFloat() / 2f

        fun isMoving(position: VP) =
            axis != null && movingLayers.any { abs(position.dot(axis) - it) < .01f }

        fun rotate(point: VP, position: VP): VP {
            if (axis == null || !isMoving(position)) return point
            return point * cos(angle) + axis.cross(point) * sin(angle) +
                axis * (axis.dot(point) * (1 - cos(angle)))
        }

        fun camera(point: VP): VP {
            val x = point.x * cos(yaw) + point.z * sin(yaw)
            val z = -point.x * sin(yaw) + point.z * cos(yaw)
            return VP(
                x,
                point.y * cos(pitch) - z * sin(pitch),
                point.y * sin(pitch) + z * cos(pitch),
            )
        }

        val quads = mutableListOf<VQuad>()
        val coordinates = (0 until cube.size).map { -edge + it * 2f }

        // Build actual cubies, just like the renderer used on Home. Drawing only six
        // sticker planes made adjacent faces overlap like loose cards at some angles.
        coordinates.forEach { x ->
            coordinates.forEach { y ->
                coordinates.forEach { z ->
                    if (abs(x) == edge || abs(y) == edge || abs(z) == edge) {
                        val position = VP(x, y, z)
                        Face.entries.forEach { face ->
                            val normal = Geometry.normals[face.ordinal].vp()
                            val right = Geometry.rights[face.ordinal].vp()
                            val down = Geometry.downs[face.ordinal].vp()
                            val center = position + normal * .97f
                            val points = listOf(
                                center + right * -.97f + down * -.97f,
                                center + right * .97f + down * -.97f,
                                center + right * .97f + down * .97f,
                                center + right * -.97f + down * .97f,
                            ).map { camera(rotate(it, position)) }
                            quads += VQuad(points, Color(0xFF151C1A), false)
                        }
                    }
                }
            }
        }

        VirtualCube.geometry(cube.size).forEachIndexed { index, geometry ->
            val position = geometry.position.vp()
            val normal = geometry.normal.vp()
            val right = geometry.right.vp()
            val down = geometry.down.vp()
            val center = position + normal * .985f
            val half = .80f
            val points = listOf(
                center + right * -half + down * -half,
                center + right * half + down * -half,
                center + right * half + down * half,
                center + right * -half + down * half,
            ).map { camera(rotate(it, position)) }
            quads += VQuad(points, Color(preferences.color(cube.stickers[index])), true)
        }

        val cameraDistance = max(8f, edge * 3.2f + 7f)
        fun perspective(point: VP): Pair<Float, Float> {
            val factor = cameraDistance / (cameraDistance - point.z)
            return point.x * factor to -point.y * factor
        }

        val projected = quads.flatMap { it.points }.map(::perspective)
        val minX = projected.minOf { it.first }
        val maxX = projected.maxOf { it.first }
        val minY = projected.minOf { it.second }
        val maxY = projected.maxOf { it.second }
        val fitScale = min(
            size.width * .82f / (maxX - minX).coerceAtLeast(1f),
            size.height * .72f / (maxY - minY).coerceAtLeast(1f),
        )
        val rawCenterX = (minX + maxX) / 2f
        val rawCenterY = (minY + maxY) / 2f
        fun project(point: VP): Offset {
            val raw = perspective(point)
            return Offset(
                size.width * .5f + (raw.first - rawCenterX) * fitScale,
                size.height * .48f + (raw.second - rawCenterY) * fitScale,
            )
        }

        quads
            .filter { quad ->
                val center = quad.points.reduce { a, b -> a + b } * .25f
                val inward = (quad.points[1] + quad.points[0] * -1f)
                    .cross(quad.points[3] + quad.points[0] * -1f)
                inward.dot(VP(0f, 0f, cameraDistance) + center * -1f) < -.0001f
            }
            .sortedBy { quad -> quad.points.map { it.z }.average() }
            .forEach { quad ->
                val points = quad.points.map(::project)
                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                    close()
                }
                drawPath(path, quad.color)
                if (quad.sticker) {
                    drawPath(
                        path,
                        brush = Brush.linearGradient(
                            listOf(Color.White.copy(alpha = .10f), Color.Transparent),
                            points[0],
                            points[2],
                        ),
                    )
                    drawPath(path, Color(0xFF07100E), style = Stroke(max(1.2f, 4.2f - cube.size * .32f)))
                }
            }
    }
}
