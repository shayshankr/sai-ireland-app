package org.sathyasaieire.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.sathyasaieire.app.navigation.Route

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val navItems = listOf(
    NavItem(Route.Home.path, "Home", Icons.Outlined.Home),
    NavItem(Route.Events.path, "Events", Icons.Outlined.CalendarMonth),
    NavItem(Route.Polls.path, "Polls", Icons.Outlined.HowToVote),
    NavItem(Route.News.path, "News", Icons.Outlined.Newspaper),
    NavItem(Route.More.path, "More", Icons.Outlined.MoreHoriz),
)

val bottomBarRoutes: Set<String> = navItems.map { it.route }.toSet()

@Composable
fun SaiBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
