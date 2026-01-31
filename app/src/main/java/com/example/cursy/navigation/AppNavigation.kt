package com.example.cursy.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cursy.core.di.AppContainer
import com.example.cursy.features.course.domain.repository.BlockInput
import com.example.cursy.features.course.presentation.screens.CourseDetailScreen
import com.example.cursy.features.course.presentation.screens.CreateCourseScreen
import com.example.cursy.features.course.presentation.screens.EditableBlock
import com.example.cursy.features.course.presentation.viewmodels.CourseDetailViewModel
import com.example.cursy.features.feed.presentation.screens.FeedScreen
import com.example.cursy.features.feed.presentation.viewmodels.FeedViewModel
import com.example.cursy.features.profile.presentation.screens.ProfileScreen
import com.example.cursy.features.profile.presentation.viewmodels.ProfileViewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import com.example.cursy.features.settings.presentation.screens.SettingsScreen
import kotlinx.coroutines.launch

private val GreenPrimary = Color(0xFF2ECC71)

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Feed : BottomNavItem(Screen.Feed.route, Icons.Default.Home, "Inicio")
    object Profile : BottomNavItem(Screen.Profile.route, Icons.Default.Person, "Mi Perfil")
}

@Composable
fun AppNavigation(
    appContainer: AppContainer,
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    var userProfileImage by remember { mutableStateOf("") }
    var hasPublishedCourse by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Feed.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Feed
            composable(Screen.Feed.route) {
                val viewModel: FeedViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return FeedViewModel(appContainer.getFeedUseCase) as T
                        }
                    }
                )

                val profileViewModel: ProfileViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ProfileViewModel(
                                appContainer.getMyProfileUseCase,
                                appContainer.getMyCoursesUseCase,
                                appContainer.getSavedCoursesUseCase
                            ) as T
                        }
                    }
                )

                RefreshOnResume {
                    viewModel.refresh()
                    profileViewModel.refresh()
                }
                
                val profileUiState by profileViewModel.uiState.collectAsState()
                
                LaunchedEffect(profileUiState.profile) {
                    profileUiState.profile?.let {
                        userProfileImage = it.profileImage
                    }
                }
                
                LaunchedEffect(profileUiState.publishedCourses.size) {
                    if (profileUiState.profile != null) {
                        hasPublishedCourse = profileUiState.publishedCourses.isNotEmpty()
                    }
                }

                FeedScreen(
                    viewModel = viewModel,
                    onCourseClick = { courseId ->
                        navController.navigate(Screen.CourseDetail.createRoute(courseId))
                    },
                    onCreateCourse = {
                        navController.navigate(Screen.CreateCourse.route)
                    },
                    userProfileImage = userProfileImage,
                    hasPublishedCourse = hasPublishedCourse
                )
            }

            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ProfileViewModel(
                                appContainer.getMyProfileUseCase,
                                appContainer.getMyCoursesUseCase,
                                appContainer.getSavedCoursesUseCase
                            ) as T
                        }
                    }
                )
                
                RefreshOnResume {
                    viewModel.refresh()
                }

                val uiState by viewModel.uiState.collectAsState()
                
                // Actualizar hasPublishedCourse solo cuando cambie
                LaunchedEffect(uiState.publishedCourses.size) {
                    hasPublishedCourse = uiState.publishedCourses.isNotEmpty()
                }
                
                LaunchedEffect(uiState.profile?.profileImage) {
                    uiState.profile?.profileImage?.let {
                        userProfileImage = it
                    }
                }
                
                ProfileScreen(
                    viewModel = viewModel,
                    onCourseClick = { courseId, isDraft ->
                        if (isDraft) {
                            navController.navigate(Screen.EditCourse.createRoute(courseId))
                        } else {
                            navController.navigate(Screen.CourseDetail.createRoute(courseId))
                        }
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onEditProfileClick = {
                        navController.navigate(Screen.EditProfile.route)
                    }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onDeleteAccount = {
                        // TODO: Delete account API call
                    },
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode
                )
            }

            // Edit Profile
            composable(Screen.EditProfile.route) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ProfileViewModel(
                                appContainer.getMyProfileUseCase,
                                appContainer.getMyCoursesUseCase,
                                appContainer.getSavedCoursesUseCase
                            ) as T
                        }
                    }
                )

                val uiState by profileViewModel.uiState.collectAsState()
                val profile = uiState.profile

                if (profile != null) {
                    com.example.cursy.features.profile.presentation.screens.EditProfileScreen(
                        initialName = profile.name,
                        initialBio = profile.bio,
                        initialUniversity = profile.university,
                        initialProfileImage = profile.profileImage,
                        onNavigateBack = { navController.popBackStack() },
                        onSave = { name, profileImage, bio, university ->
                            appContainer.updateProfileUseCase(
                                name = name,
                                profileImage = profileImage,
                                bio = bio,
                                university = university
                            )
                        },
                        onUploadImage = { file ->
                            appContainer.uploadImageUseCase(file)
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
            }

            // Create Course
            composable(Screen.CreateCourse.route) {
                var isLoading by remember { mutableStateOf(false) }
                
                CreateCourseScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onPublish = { title, description, coverImage, blocks ->
                        isLoading = true
                        scope.launch {
                            try {
                                val blockInputs = blocks.mapIndexed { index, block ->
                                    BlockInput(
                                        type = block.type.lowercase(),
                                        content = block.content,
                                        order = index + 1
                                    )
                                }
                                
                                val result = appContainer.createCourseUseCase(
                                    title = title,
                                    description = description,
                                    coverImage = coverImage.takeIf { it.isNotEmpty() },
                                    blocks = blockInputs,
                                    publish = true
                                )
                                
                                result.fold(
                                    onSuccess = { courseId ->
                                        Log.d("CreateCourse", "Curso creado: $courseId")
                                        hasPublishedCourse = true
                                        navController.popBackStack()
                                    },
                                    onFailure = { error ->
                                        Log.e("CreateCourse", "Error: ${error.message}")
                                    }
                                )
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    onUploadImage = { file -> appContainer.uploadImageUseCase(file) },
                    onSaveDraft = { title, description, coverImage, blocks ->
                        scope.launch {
                            val blockInputs = blocks.mapIndexed { index, block ->
                                BlockInput(
                                    type = block.type.lowercase(),
                                    content = block.content,
                                    order = index + 1
                                )
                            }
                            
                            val result = appContainer.createCourseUseCase(
                                title = title,
                                description = description,
                                coverImage = coverImage.takeIf { it.isNotEmpty() },
                                blocks = blockInputs,
                                publish = false
                            )
                            
                            result.fold(
                                onSuccess = { navController.popBackStack() },
                                onFailure = { Log.e("CreateCourse", "Error: ${it.message}") }
                            )
                        }
                    }
                )
            }

            // Edit Course
            composable(
                route = Screen.EditCourse.route,
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
                val context = androidx.compose.ui.platform.LocalContext.current

                var initialTitle by remember { mutableStateOf("") }
                var initialDescription by remember { mutableStateOf("") }
                var initialCoverImage by remember { mutableStateOf("") }
                var initialBlocks by remember { mutableStateOf<List<EditableBlock>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }
                
                LaunchedEffect(courseId) {
                    android.util.Log.d("EditCourse", "Fetching course detail for ID: $courseId")
                    val result = appContainer.getCourseDetailUseCase(courseId)
                    result.fold(
                        onSuccess = { (course, _, _) ->
                            android.util.Log.d("EditCourse", "Successfully loaded course: ${course.title}")
                            initialTitle = course.title
                            initialDescription = course.description
                            initialCoverImage = course.coverImage
                            initialBlocks = course.blocks.map { 
                                EditableBlock(
                                    type = it.type.name.lowercase(),
                                    content = it.content
                                )
                            }
                            isLoading = false
                        },
                        onFailure = {
                            android.util.Log.e("EditCourse", "Failed to load course: ${it.message}")
                            android.widget.Toast.makeText(context, "Error al cargar curso: ${it.message}", android.widget.Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }
                    )
                }

                if (isLoading) {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                } else {
                    CreateCourseScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onPublish = { title, description, coverImage, blocks ->
                             scope.launch {
                                val blockInputs = blocks.mapIndexed { index, block ->
                                    BlockInput(
                                        type = block.type.lowercase(),
                                        content = block.content,
                                        order = index + 1
                                    )
                                }
                                val result = appContainer.updateCourseUseCase(
                                    courseId = courseId,
                                    title = title,
                                    description = description,
                                    coverImage = coverImage,
                                    blocks = blockInputs,
                                    publish = true
                                )
                                result.onSuccess {
                                    navController.popBackStack()
                                }
                                result.onFailure {
                                    Log.e("EditCourse", "Error updating course: ${it.message}")
                                }
                             }
                        },
                        onUploadImage = { file -> appContainer.uploadImageUseCase(file) },
                        onSaveDraft = { title, description, coverImage, blocks ->
                            scope.launch {
                                val blockInputs = blocks.mapIndexed { index, block ->
                                    BlockInput(
                                        type = block.type.lowercase(),
                                        content = block.content,
                                        order = index + 1
                                    )
                                }
                                appContainer.updateCourseUseCase(
                                    courseId = courseId,
                                    title = title,
                                    description = description,
                                    coverImage = coverImage,
                                    blocks = blockInputs,
                                    publish = false
                                )
                                navController.popBackStack()
                            }
                        },
                        initialTitle = initialTitle,
                        initialDescription = initialDescription,
                        initialCoverImage = initialCoverImage,
                        initialBlocks = initialBlocks,
                        isEditing = true
                    )
                }
            }

            composable(
                route = Screen.CourseDetail.route,
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
                val viewModel: CourseDetailViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return CourseDetailViewModel(
                                appContainer.getCourseDetailUseCase,
                                appContainer.deleteCourseUseCase,
                                appContainer.saveCourseUseCase
                            ) as T
                        }
                    }
                )
                CourseDetailScreen(
                    courseId = courseId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onEditCourse = {
                        navController.navigate(Screen.EditCourse.createRoute(courseId))
                    },
                    onCreateCourse = {
                        navController.navigate(Screen.CreateCourse.route)
                    },
                    hasPublishedCourse = hasPublishedCourse
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Feed,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in items.map { it.route }

    if (showBottomBar) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GreenPrimary
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GreenPrimary,
                        selectedTextColor = GreenPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

@Composable
fun RefreshOnResume(onRefresh: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}