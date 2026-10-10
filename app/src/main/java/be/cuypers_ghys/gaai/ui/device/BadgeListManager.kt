/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2024-2026, Frank HJ Cuypers
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package be.cuypers_ghys.gaai.ui.device

import android.util.Log
import be.cuypers_ghys.gaai.data.Badge

// Tag for logging
private const val TAG = "BadgeListManager"

/**
 * A listener that listens for a new [List] of [Badge]s.
 */
interface IBadgeListListener {
  /**
   * Informs the [IBadgeListListener] of a new [List] of [Badge]s.
   */
  fun badgeListChanged(badgeList: List<Badge>)
}

/**
 * Badge list manager, responsible for (un)registering [IBadgeListListener]s that want to be informed of updates.
 */
interface IBadgeListManager {
  /**
   * Register the [listener].
   * @param listener
   */
  fun register(listener: IBadgeListListener)

  /**
   * Unregister the [listener].
   * @param listener
   */
  fun unregister(listener: IBadgeListListener)
}

/**
 * Badge list manager, responsible for (un)registering [IBadgeListListener]s that want to be informed of updates and
 * for emitting new badge lists to the registered listeners.
 */
class BadgeListManager : IBadgeListManager {

  /**
   * The registered listeners.
   */
  private val listeners: MutableList<IBadgeListListener> = mutableListOf()

  /**
   * Register a new [listener].
   * @param listener
   */
  // TODO: must be thread safe
  override fun register(listener: IBadgeListListener) {
    listeners.add(listener)
  }

  /**
   * Unregister a [listener].
   * @param listener
   */
  // TODO: must be thread safe
  override fun unregister(listener: IBadgeListListener) {
    listeners.remove(listener)
  }

  /**
   * Inform all registered listeners of the new [badgeList].
   * @param badgeList
   */
  fun emitNewBadgeList(badgeList: List<Badge>) {
    Log.d(TAG, "emitNewBadgeList: $badgeList")

    listeners.forEach { it.badgeListChanged(badgeList) }
  }
}
