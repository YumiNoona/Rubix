package com.cubeguide.rendering

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cubeguide.core.CubeState
import com.cubeguide.core.Move
import com.cubeguide.play.VirtualCube
import com.cubeguide.play.VirtualMove
import com.cubeguide.ui.VirtualCubeView

/**
 * Shared 3x3 adapter for the solid virtual-cube renderer.
 *
 * Keeping one projection, lighting and cubie implementation prevents Home, scan
 * review and the physical guide from drifting visually from virtual play.
 */
@Composable
fun CubeView(
    cube: CubeState,
    modifier: Modifier = Modifier,
    move: Move? = null,
    replay: Int = 0,
    viewReset: Int = 0,
    showInitials: Boolean? = null,
    onAnimationProgress: (Float) -> Unit = {},
) {
    @Suppress("UNUSED_VARIABLE")
    val labelsAreIntentionallyEditorOnly = showInitials
    VirtualCubeView(
        cube = VirtualCube(3, cube.stickers),
        modifier = modifier,
        move = move?.let(VirtualMove::from),
        replay = replay,
        viewReset = viewReset,
        onAnimationProgress = onAnimationProgress,
    )
}
