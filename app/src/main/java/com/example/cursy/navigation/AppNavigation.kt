package com.example.cursy.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dagger.hilt.android.EntryPointAccessors
import com.example.cursy.core.di.AuthSessionEntryPoint
import kotlinx.coroutines.flow.collectLatest
import com.example.cursy.features.course.presentation.screens.CourseDetailScreen
import com.example.cursy.features.course.presentation.screens.CreateCourseScreen
import com.example.cursy.features.course.presentation.viewmodels.CourseDetailViewModel
import com.example.cursy.features.course.presentation.viewmodels.CreateEditCourseViewModel
import com.example.cursy.features.feed.presentation.screens.FeedScreen
import com.example.cursy.features.feed.presentation.viewmodels.FeedViewModel
import com.example.cursy.features.login.presentation.screens.LoginScreen
import com.example.cursy.features.login.presentation.viewmodels.AuthViewModel
import com.example.cursy.features.notifications.presentation.screens.NotificationsScreen
import com.example.cursy.features.notifications.presentation.viewmodels.NotificationsViewModel
import com.example.cursy.features.profile.presentation.screens.ProfileScreen
import com.example.cursy.features.profile.presentation.viewmodels.EditProfileViewModel
import com.example.cursy.features.profile.presentation.viewmodels.ProfileViewModel
import com.example.cursy.features.Register.presenstation.screens.FormRegister
import com.example.cursy.features.settings.presentation.screens.SettingsScreen
import com.example.cursy.features.settings.presentation.viewmodels.SettingsViewModel
import com.example.cursy.features.explore.presentation.viewmodels.ExploreViewModel
import com.example.cursy.features.explore.presentation.screens.ExploreScreen
import com.example.cursy.features.chat.presentation.screens.ChatListScreen
import com.example.cursy.features.chat.presentation.screens.MessageScreen
import com.example.cursy.features.chat.presentation.screens.UserSearchScreen
import com.example.cursy.features.chat.presentation.viewmodels.ChatViewModel
import com.example.cursy.features.profile.presentation.screens.EditProfileScreen
import com.example.cursy.features.feed.presentation.screens.DownloadScreen
import com.example.cursy.features.feed.presentation.viewmodels.DownloadViewModel
import kotlinx.coroutines.launch

private val GreenPrimary = Color(0xFF2ECC71)

private val BOTTOM_BAR_ROUTES = listOf(
    Screen.Feed.route,
    Screen.Explore.route,
    Screen.ChatList.route,
    Screen.Profile.route
)

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Feed    : BottomNavItem(Screen.Feed.route,     Icons.Default.Home,              "Inicio")
    object Explore : BottomNavItem(Screen.Explore.route,  Icons.Default.Explore,           "Explorar")
    object Chats   : BottomNavItem(Screen.ChatList.route, Icons.Default.ChatBubbleOutline, "Chats")
    object Profile : BottomNavItem(Screen.Profile.route,  Icons.Default.Person,            "Mi Perfil")
}

@Composable
fun AppNavigation(
    startDestination: String = Screen.Login.route,
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    val navController = rememberNavController()
    var userProfileImage by remember { mutableStateOf("") }
    var hasPublishedCourse by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val authSessionManager = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AuthSessionEntryPoint::class.java
        ).authSessionManager()
    }

    LaunchedEffect(navController) {
        authSessionManager.sessionExpiredEvents.collectLatest {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Pantalla de Login
            composable(Screen.Login.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                LoginScreen(
                    onLoginSuccess = { token, userId ->
                        authViewModel.setAuthToken(token)
                        authViewModel.setCurrentUserId(userId)
                        onLoginSuccess() // Sincronizar FCM token
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onClose = {}
                )
            }

            composable(Screen.Register.route) {
                FormRegister(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            // Pantalla de Feed
            composable(Screen.Feed.route) {
                val viewModel: FeedViewModel = hiltViewModel()
                val profileViewModel: ProfileViewModel = hiltViewModel()

                RefreshOnResume {
                    viewModel.refresh()
                    profileViewModel.refresh()
                }

                val profileUiState by profileViewModel.uiState.collectAsState()

                LaunchedEffect(profileUiState.profile) {
                    profileUiState.profile?.let { userProfileImage = it.profileImage }
                }
                LaunchedEffect(profileUiState.publishedCourses.size) {
                    if (profileUiState.profile != null)
                        hasPublishedCourse = profileUiState.publishedCourses.isNotEmpty()
                }

                FeedScreen(
                    viewModel = viewModel,
                    onCourseClick = { courseId ->
                        navController.navigate(Screen.CourseDetail.createRoute(courseId))
                    },
                    onCreateCourse     = { navController.navigate(Screen.CreateCourse.route) },
                    onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                    userProfileImage   = userProfileImage,
                    hasPublishedCourse = hasPublishedCourse
                )
            }

            // Notificaciones
            composable(Screen.Notifications.route) {
                val viewModel: NotificationsViewModel = hiltViewModel()
                NotificationsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Pantalla de Explorar
            composable(Screen.Explore.route) {
                val exploreViewModel: ExploreViewModel = hiltViewModel()
                val chatViewModel: ChatViewModel = hiltViewModel()

                // Escucha el evento de navegación que emite createConversation()
                LaunchedEffect(Unit) {
                    chatViewModel.navigationEvent.collect { conversationId ->
                        navController.navigate(Screen.Message.createRoute(conversationId))
                    }
                }

                ExploreScreen(
                    viewModel = exploreViewModel,
                    onMessageClick = { userId ->
                        chatViewModel.createConversation(userId)
                    }
                )
            }

            composable(Screen.ChatList.route) {
                val chatViewModel: ChatViewModel = hiltViewModel()
                ChatListScreen(
                    viewModel    = chatViewModel,
                    onChatClick  = { conversationId ->
                        navController.navigate(Screen.Message.createRoute(conversationId))
                    },
                    onNewChatClick = { navController.navigate(Screen.UserSearch.route) },
                    onBackClick    = { navController.popBackStack() }
                )
            }

            // Búsqueda de usuarios
            composable(Screen.UserSearch.route) {
                val chatViewModel: ChatViewModel = hiltViewModel()
                UserSearchScreen(
                    viewModel   = chatViewModel,
                    onUserClick = { conversationId ->
                        navController.navigate(Screen.Message.createRoute(conversationId)) {
                            popUpTo(Screen.ChatList.route)
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            //Mensajes
            composable(
                route = Screen.Message.route,
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId")
                    ?: return@composable
                val chatViewModel: ChatViewModel = hiltViewModel()
                MessageScreen(
                    viewModel      = chatViewModel,
                    conversationId = conversationId,
                    onBackClick    = { navController.popBackStack() }
                )
            }

            // Perfil
            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel()
                RefreshOnResume { viewModel.refresh() }
                val uiState by viewModel.uiState.collectAsState()

                LaunchedEffect(uiState.publishedCourses.size) {
                    hasPublishedCourse = uiState.publishedCourses.isNotEmpty()
                }
                LaunchedEffect(uiState.profile?.profileImage) {
                    uiState.profile?.profileImage?.let { userProfileImage = it }
                }

                ProfileScreen(
                    viewModel          = viewModel,
                    onCourseClick      = { courseId, isDraft ->
                        if (isDraft) navController.navigate(Screen.EditCourse.createRoute(courseId))
                        else         navController.navigate(Screen.CourseDetail.createRoute(courseId))
                    },
                    onSettingsClick    = { navController.navigate(Screen.Settings.route) },
                    onEditProfileClick = { navController.navigate(Screen.EditProfile.route) }
                )
            }

            // david: Pantalla de mis descargas (offline)
            composable(Screen.Downloads.route) {
                val viewModel: DownloadViewModel = hiltViewModel()
                DownloadScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPlayOffline = { localPath ->
                        // david: Navegar a un reproductor o manejar la reproducción local
                    }
                )
            }

            //Configuración
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                val settingsUiState by settingsViewModel.uiState.collectAsState()

                LaunchedEffect(settingsUiState.navigateToLogin) {
                    if (settingsUiState.navigateToLogin) {
                        settingsViewModel.resetNavigation()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                SettingsScreen(
                    viewModel        = settingsViewModel,
                    onNavigateBack   = { navController.popBackStack() },
                    onLogout         = { settingsViewModel.logout() },
                    onDeleteAccount  = { settingsViewModel.deleteAccount() },
                    isDarkMode       = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode
                )
            }

            //Editar Perfil
            composable(Screen.EditProfile.route) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                val editProfileViewModel: EditProfileViewModel = hiltViewModel()
                val uiState by profileViewModel.uiState.collectAsState()
                val profile = uiState.profile

                if (profile != null) {
                    LaunchedEffect(profile) {
                        editProfileViewModel.initWith(
                            name = profile.name,
                            bio = profile.bio,
                            university = profile.university,
                            profileImage = profile.profileImage
                        )
                    }

                    EditProfileScreen(
                        initialProfileImage = profile.profileImage,
                        onNavigateBack = { navController.popBackStack() },
                        viewModel = editProfileViewModel
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
            }

            //Crear Curso
            composable(Screen.CreateCourse.route) {
                val createEditViewModel: CreateEditCourseViewModel = hiltViewModel()
                val courseUiState by createEditViewModel.uiState.collectAsState()

                LaunchedEffect(courseUiState.navigateBack) {
                    if (courseUiState.navigateBack) {
                        if (courseUiState.coursePublished) hasPublishedCourse = true
                        createEditViewModel.resetNavigateBack()
                        navController.popBackStack()
                    }
                }

                CreateCourseScreen(
                    viewModel      = createEditViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            //Editar Curso
            composable(
                route = Screen.EditCourse.route,
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
                val vm: CreateEditCourseViewModel = hiltViewModel()
                val editUiState by vm.uiState.collectAsState()

                LaunchedEffect(courseId) { vm.loadCourseForEdit(courseId) }
                LaunchedEffect(editUiState.navigateBack) {
                    if (editUiState.navigateBack) { vm.resetNavigateBack(); navController.popBackStack() }
                }

                if (editUiState.isLoading && !editUiState.courseLoaded) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                } else if (editUiState.courseLoaded) {
                    CreateCourseScreen(
                        viewModel      = vm,
                        onNavigateBack = { navController.popBackStack() },
                        isEditing      = true,
                        courseId        = courseId
                    )
                }
            }

            //Detalle de Curso
            composable(
                route = Screen.CourseDetail.route,
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
                val viewModel: CourseDetailViewModel = hiltViewModel()

                CourseDetailScreen(
                    courseId       = courseId,
                    viewModel      = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onEditCourse   = { navController.navigate(Screen.EditCourse.createRoute(courseId)) },
                    onCreateCourse = { navController.navigate(Screen.CreateCourse.route) }
                )
            }
        }
    }
}

// Barra de navegación inferior
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Feed,
        BottomNavItem.Explore,
        BottomNavItem.Chats,
        BottomNavItem.Profile
    )

    val navBackStackEntry  by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in BOTTOM_BAR_ROUTES

    if (showBottomBar) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor   = GreenPrimary
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    icon     = { Icon(item.icon, contentDescription = item.label) },
                    label    = { Text(item.label) },
                    selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                    onClick  = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = GreenPrimary,
                        selectedTextColor   = GreenPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor      = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

// Utilidad para refrescar al volver a la pantalla
@Composable
fun RefreshOnResume(onRefresh: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
