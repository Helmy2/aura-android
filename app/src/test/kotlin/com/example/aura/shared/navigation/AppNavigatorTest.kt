package com.example.aura.shared.navigation

import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AppNavigatorTest {

    private lateinit var navigator: AppNavigator

    @Before
    fun setup() {
        navigator = AppNavigator(Destination.Home)
    }

    @Test
    fun `initial state should contain start destination`() {
        assertEquals(1, navigator.backStack.size)
        assertEquals(Destination.Home, navigator.backStack.last())
    }

    @Test
    fun `navigate should add destination to back stack`() {
        val newDestination = Destination.VideoList
        navigator.navigate(newDestination)

        assertEquals(2, navigator.backStack.size)
        assertEquals(newDestination, navigator.backStack.last())
    }

    @Test
    fun `navigate singleTop should replace if same class`() {
        navigator.navigate(Destination.Home) // Already at Home, should replace

        assertEquals(1, navigator.backStack.size)
        assertEquals(Destination.Home, navigator.backStack.last())
    }

    @Test
    fun `navigate singleTop should add if different class`() {
        navigator.navigate(Destination.VideoList)

        assertEquals(2, navigator.backStack.size)
        assertEquals(Destination.VideoList, navigator.backStack.last())
    }

    @Test
    fun `back should remove last destination`() {
        navigator.navigate(Destination.VideoList)
        assertEquals(2, navigator.backStack.size)

        navigator.back()
        assertEquals(1, navigator.backStack.size)
        assertEquals(Destination.Home, navigator.backStack.last())
    }

    @Test
    fun `back should do nothing if only one destination`() {
        navigator.back()
        assertEquals(1, navigator.backStack.size)
        assertEquals(Destination.Home, navigator.backStack.last())
    }

    @Test
    fun `navigateAsStart should clear stack and set new root`() {
        navigator.navigate(Destination.VideoList)
        navigator.navigateAsStart(Destination.Settings)

        assertEquals(1, navigator.backStack.size)
        assertEquals(Destination.Settings, navigator.backStack.last())
    }

    @Test
    fun `navigateToTopLevel should pop to root if already selected`() {
        navigator.navigate(Destination.VideoList) // Stack: Home -> VideoList
        
        navigator.navigateToTopLevel(Destination.Home)

        // Should pop VideoList and return to Home
        assertEquals(1, navigator.backStack.size)
        assertEquals(Destination.Home, navigator.backStack.last())
    }

    @Test
    fun `navigateToTopLevel should clear and add if different`() {
        navigator.navigateToTopLevel(Destination.Favorites)

        assertEquals(1, navigator.backStack.size)
        assertEquals(Destination.Favorites, navigator.backStack.last())
    }

    @Test
    fun `attachToRegistry should register provider`() {
        val owner = mockk<SavedStateRegistryOwner>(relaxed = true)
        val registry = mockk<SavedStateRegistry>(relaxed = true)
        every { owner.savedStateRegistry } returns registry
        every { registry.consumeRestoredStateForKey(any()) } returns null

        navigator.attachToRegistry(owner)

        verify { registry.registerSavedStateProvider("key_nav_back_stack", navigator) }
    }
}
