package com.vezhny.eventtracker.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.vezhny.eventtracker.R
import com.vezhny.eventtracker.ui.icons.EventIcon

/** "No icon" followed by every [EventIcon]; tapping the selected icon again clears it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconPicker(selected: EventIcon?, onSelect: (EventIcon?) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconChoice(Icons.Filled.Clear, stringResource(R.string.icon_none), selected == null) { onSelect(null) }
        EventIcon.entries.forEach { icon ->
            IconChoice(icon.vector, stringResource(icon.label), selected == icon) {
                onSelect(if (selected == icon) null else icon)
            }
        }
    }
}

@Composable
private fun IconChoice(vector: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(if (selected) colors.primaryContainer else colors.surfaceContainerHigh, shape)
            .border(2.dp, if (selected) colors.primary else Color.Transparent, shape)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            vector,
            contentDescription = label,
            tint = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
    }
}
