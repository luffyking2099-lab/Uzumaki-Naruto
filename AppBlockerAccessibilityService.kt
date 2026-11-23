package com.family.guardian.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.family.guardian.ui.BlockedAppActivity

/**
 * Watches which app is in the foreground (package name only — canRetrieveWindowContent
 * is false in accessibility_service_config.xml, so this service never reads on-screen
 * text, messages, or any app content). When the foreground app is on the current
 * block/allow list, it launches a full-screen "blocked" screen instead.
 *
 * Rules (blockedPackages / allowMode / allowedPackages) should be synced down from the
 * parent dashboard via RulesRepository, not hardcoded — wire that up once the backend exists.
 */
class AppBlockerAccessibilityService : AccessibilityService() {

    private val blockedPackages = mutableSetOf<String>()
    private var allowMode = false
    private val allowedPackages = mutableSetOf<String>()

    // Apps the service itself needs to run; never block these regardless of rules.
    private val alwaysAllowed = setOf(
        packageName, "com.android.systemui", "com.android.settings"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg in alwaysAllowed) return

        val isBlocked = if (allowMode) pkg !in allowedPackages else pkg in blockedPackages
        if (isBlocked) {
            val intent = Intent(this, BlockedAppActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("blocked_package", pkg)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {}

    fun updateRules(blockMode: Boolean, blocked: List<String>, allowed: List<String>) {
        allowMode = !blockMode
        blockedPackages.clear(); blockedPackages.addAll(blocked)
        allowedPackages.clear(); allowedPackages.addAll(allowed)
    }
}
