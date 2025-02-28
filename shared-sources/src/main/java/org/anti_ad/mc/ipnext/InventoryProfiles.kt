/*
 * Inventory Profiles Next
 *
 *   Copyright (c) 2019-2020 jsnimda <7615255+jsnimda@users.noreply.github.com>
 *   Copyright (c) 2021-2022 Plamen K. Kosseff <p.kosseff@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.anti_ad.mc.ipnext

import org.anti_ad.mc.ipnext.Log
import org.anti_ad.mc.common.config.options.ConfigKeyToggleBoolean
//import org.anti_ad.mc.common.gui.widgets.widgetsInitGlue
import org.anti_ad.mc.common.moreinfo.InfoManager
/*
import org.anti_ad.mc.common.vanilla.alias.aliasInitGlue
import org.anti_ad.mc.common.vanilla.render.renderInitTheGlue
import org.anti_ad.mc.common.vanilla.vanillaInitGlue

 */
import org.anti_ad.mc.ipnext.access.IPNImpl
import org.anti_ad.mc.ipnext.config.Debugs
import org.anti_ad.mc.ipnext.config.ModSettings
import org.anti_ad.mc.ipnext.config.SaveLoadManager
import org.anti_ad.mc.ipnext.event.ClientInitHandler
import org.anti_ad.mc.ipnext.gui.ConfigScreeHelper
import org.anti_ad.mc.ipnext.gui.inject.InsertWidgetHandler
import org.anti_ad.mc.ipnext.input.InputHandler
import org.anti_ad.mc.ipnext.specific.initInfoManager

var initGlueProc: (() -> Unit) = ::initGlues;

private fun initGlues() {

    ConfigKeyToggleBoolean.toggleNotificationHandler = ConfigScreeHelper::toggleBooleanSettingMessage
    ConfigKeyToggleBoolean.finish = ConfigScreeHelper::finish
    //renderInitTheGlue()
    //aliasInitGlue()
    //vanillaInitGlue()
    //widgetsInitGlue()
    IPNImpl.init()
    initGlueProc = ::nop

}

fun nop() { }

@Suppress("unused")
fun init() {

    initGlueProc()

    specificInit()

    ClientInitHandler.register {

        initInfoManager()
        Log.shouldDebug = { ModSettings.DEBUG.booleanValue }
        Log.shouldTrace = { ModSettings.DEBUG.booleanValue && Debugs.TRACE_LOGS.booleanValue }


        // Keybind register

        InputHandler.onClientInit()
        InsertWidgetHandler.onClientInit()

        SaveLoadManager.load()
        //CustomDataFileLoader.load()
        if (ModSettings.FIRST_RUN.booleanValue) {
            InfoManager.isEnabled = { false }
            ModSettings.FIRST_RUN. value = false
            SaveLoadManager.save()
        } else {
            InfoManager.isEnabled = { ModSettings.ENABLE_ANALYTICS.value }
        }
        InfoManager.event(lazy {"${InfoManager.loader}/${InfoManager.mcVersion}/${InfoManager.version}/started"})
        //var s: Sounds = Sounds.REFILL_STEP_NOTIFY
    }

}
