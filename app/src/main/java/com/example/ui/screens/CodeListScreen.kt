package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.crypto.OtpType
import com.example.data.UserSettings
import com.example.model.Account
import com.example.ui.components.CodeDisplayCard
import com.example.ui.components.ClipboardCountdownToastBanner
import com.example.ui.components.EmptyVaultIllustration
import com.example.util.HapticAction
import com.example.util.HapticManager
import com.example.util.rememberClipboardCountdownState
import kotlinx.coroutines.launch

/**
 * State object for managing LazyColumn drag-and-drop reordering.
 */
class ReorderableLazyListState(
    val lazyListState: LazyListState,
    val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    val onSettle: () -> Unit,
    val onDragStartCallback: (() -> Unit)? = null,
    val onMoveCallback: (() -> Unit)? = null,
    val onDragEndCallback: (() -> Unit)? = null
) {
    var draggedIndex by mutableStateOf<Int?>(null)
        private set
    var draggedOffset by mutableFloatStateOf(0f)
        private set

    val isDragging: Boolean get() = draggedIndex != null

    fun onDragStart(index: Int) {
        draggedIndex = index
        draggedOffset = 0f
        onDragStartCallback?.invoke()
    }

    fun onDrag(delta: Float) {
        val currentIndex = draggedIndex ?: return
        draggedOffset += delta

        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
        val currentItemInfo = visibleItems.firstOrNull { it.index == currentIndex } ?: return
        val itemHeight = currentItemInfo.size.toFloat()
        if (itemHeight <= 0f) return

        val threshold = itemHeight * 0.5f

        if (draggedOffset > threshold && currentIndex < lazyListState.layoutInfo.totalItemsCount - 1) {
            val nextIndex = currentIndex + 1
            onMove(currentIndex, nextIndex)
            draggedIndex = nextIndex
            draggedOffset -= itemHeight
            onMoveCallback?.invoke()
        } else if (draggedOffset < -threshold && currentIndex > 0) {
            val prevIndex = currentIndex - 1
            onMove(currentIndex, prevIndex)
            draggedIndex = prevIndex
            draggedOffset += itemHeight
            onMoveCallback?.invoke()
        }
    }

    fun onDragEnd() {
        draggedIndex = null
        draggedOffset = 0f
        onDragEndCallback?.invoke()
        onSettle()
    }

    fun onDragCancel() {
        draggedIndex = null
        draggedOffset = 0f
    }
}

@Composable
fun rememberReorderableLazyListState(
    lazyListState: LazyListState = rememberLazyListState(),
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onSettle: () -> Unit,
    onDragStartCallback: (() -> Unit)? = null,
    onMoveCallback: (() -> Unit)? = null,
    onDragEndCallback: (() -> Unit)? = null
): ReorderableLazyListState {
    return remember(lazyListState, onMove, onSettle, onDragStartCallback, onMoveCallback, onDragEndCallback) {
        ReorderableLazyListState(
            lazyListState = lazyListState,
            onMove = onMove,
            onSettle = onSettle,
            onDragStartCallback = onDragStartCallback,
            onMoveCallback = onMoveCallback,
            onDragEndCallback = onDragEndCallback
        )
    }
}

/**
 * Modifier enabling long-press drag-and-drop reordering for a list item.
 */
fun Modifier.reorderable(
    state: ReorderableLazyListState,
    index: Int,
    enabled: Boolean = true
): Modifier = this.pointerInput(state, index, enabled) {
    if (!enabled) return@pointerInput
    detectDragGesturesAfterLongPress(
        onDragStart = { state.onDragStart(index) },
        onDrag = { change, dragAmount ->
            change.consume()
            state.onDrag(dragAmount.y)
        },
        onDragEnd = { state.onDragEnd() },
        onDragCancel = { state.onDragCancel() }
    )
}

/**
 * Modifier enabling immediate drag handle reordering for a specific icon or view.
 */
fun Modifier.reorderableDragHandle(
    state: ReorderableLazyListState,
    index: Int,
    enabled: Boolean = true
): Modifier = this.pointerInput(state, index, enabled) {
    if (!enabled) return@pointerInput
    detectDragGestures(
        onDragStart = { state.onDragStart(index) },
        onDrag = { change, dragAmount ->
            change.consume()
            state.onDrag(dragAmount.y)
        },
        onDragEnd = { state.onDragEnd() },
        onDragCancel = { state.onDragCancel() }
    )
}

/**
 * CodeListScreen renders a list of 2FA accounts with live codes and drag-and-drop reordering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeListScreen(
    accounts: List<Account>,
    timeSeconds: Long,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    blurCodesByDefault: Boolean = false,
    onCopyCode: ((String) -> Unit)? = null,
    onHotpIncrement: ((Account) -> Unit)? = null,
    onAccountClick: ((Account) -> Unit)? = null,
    onAccountLongClick: ((Account) -> Unit)? = null,
    onDeleteAccount: ((Account) -> Unit)? = null,
    onEditAccount: ((Account) -> Unit)? = null,
    onReorderAccounts: ((List<Account>) -> Unit)? = null,
    onNavigateToScanQr: (() -> Unit)? = null,
    onNavigateToManualEntry: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
    userSettings: UserSettings = UserSettings()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val clipboardCountdownState = rememberClipboardCountdownState(totalSeconds = 30)

    val localAccounts = remember { mutableStateListOf<Account>() }

    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { fromIndex, toIndex ->
            if (fromIndex in localAccounts.indices && toIndex in localAccounts.indices) {
                val movedItem = localAccounts.removeAt(fromIndex)
                localAccounts.add(toIndex, movedItem)
            }
        },
        onSettle = {
            onReorderAccounts?.invoke(localAccounts.toList())
        },
        onDragStartCallback = {
            HapticManager.performHaptic(context, HapticAction.DRAG_START, userSettings)
        },
        onMoveCallback = {
            HapticManager.performHaptic(context, HapticAction.KEYPRESS, userSettings)
        },
        onDragEndCallback = {
            HapticManager.performHaptic(context, HapticAction.DRAG_DROP, userSettings)
        }
    )

    LaunchedEffect(accounts) {
        if (!reorderState.isDragging) {
            localAccounts.clear()
            localAccounts.addAll(accounts)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("code_list_screen")
    ) {
        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyVaultIllustration(
                    searchQuery = searchQuery,
                    onNavigateToScanQr = onNavigateToScanQr,
                    onNavigateToManualEntry = onNavigateToManualEntry
                )
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(localAccounts, key = { _, account -> account.id }) { index, account ->
                    val isDraggingThis = reorderState.draggedIndex == index

                    // Subtle lift animation scale
                    val scale by animateFloatAsState(
                        targetValue = if (isDraggingThis) 1.05f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "dragScale"
                    )

                    // Card tilt on pickup for realistic tactile feedback
                    val tiltRotation by animateFloatAsState(
                        targetValue = if (isDraggingThis) -1.2f else 0.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "dragTilt"
                    )

                    // Card shadow elevation
                    val cardElevation by animateDpAsState(
                        targetValue = if (isDraggingThis) 16.dp else 1.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "dragElevation"
                    )

                    val offsetY = if (isDraggingThis) reorderState.draggedOffset else 0f
                    val zIndex = if (isDraggingThis) 15f else 0f

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            when (dismissValue) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    // Swipe Right -> Edit
                                    HapticManager.performHaptic(context, HapticAction.KEYPRESS, userSettings)
                                    onEditAccount?.invoke(account)
                                    false // Snap back into place
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    // Swipe Left -> Delete
                                    HapticManager.performHaptic(context, HapticAction.KEYPRESS, userSettings)
                                    onDeleteAccount?.invoke(account)
                                    true // Dismiss
                                }
                                SwipeToDismissBoxValue.Settled -> false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = searchQuery.isBlank() && !reorderState.isDragging,
                        enableDismissFromEndToStart = searchQuery.isBlank() && !reorderState.isDragging,
                        backgroundContent = {
                            val targetValue = dismissState.targetValue
                            val direction = dismissState.dismissDirection

                            val bgColor by animateColorAsState(
                                targetValue = when (targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f)
                                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                                },
                                label = "swipeBgColor"
                            )

                            val alignment = when (direction) {
                                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                else -> Alignment.CenterStart
                            }

                            val contentColor = when (direction) {
                                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onPrimaryContainer
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            val iconScale by animateFloatAsState(
                                targetValue = if (targetValue != SwipeToDismissBoxValue.Settled) 1.15f else 0.85f,
                                label = "swipeIconScale"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(bgColor)
                                    .padding(horizontal = 24.dp),
                                contentAlignment = alignment
                            ) {
                                if (direction != SwipeToDismissBoxValue.Settled) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        }
                                    ) {
                                        if (direction == SwipeToDismissBoxValue.StartToEnd) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Account",
                                                tint = contentColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Text(
                                                text = "Edit",
                                                color = contentColor,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                                            Text(
                                                text = "Delete",
                                                color = contentColor,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete Account",
                                                tint = contentColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        content = {
                            CodeDisplayCard(
                                account = account,
                                timeSeconds = timeSeconds,
                                isBlurred = blurCodesByDefault,
                                isDragging = isDraggingThis,
                                cardElevation = cardElevation,
                                modifier = Modifier
                                    .animateItem()
                                    .zIndex(zIndex)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        rotationZ = tiltRotation
                                        translationY = offsetY
                                    }
                                    .reorderable(
                                        state = reorderState,
                                        index = index,
                                        enabled = searchQuery.isBlank()
                                    ),
                                dragHandleModifier = Modifier.reorderableDragHandle(
                                    state = reorderState,
                                    index = index,
                                    enabled = searchQuery.isBlank()
                                ),
                                onCopyCode = { code ->
                                    clipboardCountdownState.copyToClipboard(code = code, label = "2FA Code", scope = coroutineScope)
                                    onCopyCode?.invoke(code)
                                    onAccountClick?.invoke(account)
                                },
                                onHotpIncrement = {
                                    onHotpIncrement?.invoke(account)
                                },
                                onLongClick = {
                                    onAccountLongClick?.invoke(account)
                                },
                                userSettings = userSettings
                            )
                        }
                    )
                }
            }
        }

        ClipboardCountdownToastBanner(
            state = clipboardCountdownState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
