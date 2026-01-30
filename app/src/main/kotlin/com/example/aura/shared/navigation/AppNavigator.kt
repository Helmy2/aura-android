package com.example.aura.shared.navigation

import android.os.Bundle
import androidx.compose.runtime.mutableStateListOf
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.serialization.json.Json

/**
 * Manages the app's back stack and top-level navigation state using an observable list.
 */
class AppNavigator(
    startDestination: Destination = Destination.Home,
) : SavedStateRegistry.SavedStateProvider {
    /**
     * Observable history of visited destinations. The last item is the active screen.
     * Exposed as [mutableStateListOf] for Compose reactivity.
     */
    val backStack = mutableStateListOf(startDestination)

    private val backStackStateKey = "key_nav_back_stack"

    /**
     * Connects this Navigator to the Activity's SavedStateRegistry.
     * MUST be called in MainActivity.onCreate().
     */
    fun attachToRegistry(owner: SavedStateRegistryOwner) {
        val registry = owner.savedStateRegistry

        // Unregister any previous old registry to prevent leaks
        try {
            registry.unregisterSavedStateProvider(backStackStateKey)
        } catch (e: IllegalArgumentException) {
            // Ignore if wasn't registered
        }

        registry.registerSavedStateProvider(backStackStateKey, this)

        val savedBundle = registry.consumeRestoredStateForKey(backStackStateKey)
        if (savedBundle != null) {
            val jsonList = savedBundle.getStringArrayList(backStackStateKey)
            if (!jsonList.isNullOrEmpty()) {
                backStack.clear()
                val restoredStack = jsonList.map { Json.decodeFromString<Destination>(it) }
                backStack.addAll(restoredStack)
            }
        }
    }

    fun detachFromRegistry(owner: SavedStateRegistryOwner) {
        owner.savedStateRegistry.unregisterSavedStateProvider(backStackStateKey)
    }

    /**
     * Called by Android when the process is being killed or configuration changes.
     * We serialize the current backStack to a list of JSON strings.
     */
    override fun saveState(): Bundle =
        Bundle().apply {
            val jsonList = ArrayList(backStack.map { Json.encodeToString(it) })
            putStringArrayList(backStackStateKey, jsonList)
        }

    /**
     * Pushes [destination] to [backStack].
     *
     * @param destination The destination to navigate to.
     * @param singleTop If true (default), replaces the current screen if it's the same class
     *                  to prevent duplicate stacking (e.g., double-tap protection).
     */
    fun navigate(
        destination: Destination,
        singleTop: Boolean = true,
    ) {
        val current = backStack.lastOrNull()

        if (singleTop && current != null && current::class == destination::class) {
            backStack[backStack.lastIndex] = destination
        } else {
            backStack.add(destination)
        }
    }

    /**
     * Clears the entire back stack and sets [destination] as the new root.
     *
     * **Use Case:** Logging out, completing a checkout flow, or resetting the app state.
     */
    fun navigateAsStart(destination: Destination) {
        backStack.clear()
        backStack.add(destination)
    }

    /** Safe pop of the last destination. Does nothing if only one screen remains. */
    fun back() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }


    /**
     * Switches to a top-level root (Bottom Nav Tab).
     * - If re-selecting the current tab: Pops to root.
     * - If switching tabs: Resets stack to the new root.
     */
    fun navigateToTopLevel(destination: Destination) {
        if (backStack.firstOrNull() == destination) {
            if (backStack.size > 1) {
                backStack.removeRange(1, backStack.size)
            }
        } else {
            backStack.clear()
            backStack.add(destination)
        }
    }
}
