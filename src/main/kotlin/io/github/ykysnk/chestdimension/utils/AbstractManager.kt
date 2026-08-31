package io.github.ykysnk.chestdimension.utils

import io.github.ykysnk.chestdimension.Constants
import kotlinx.coroutines.*
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import java.nio.file.Path
import kotlin.time.Duration.Companion.minutes

abstract class AbstractManager<T>(fileName: String, newData: T) {
    protected open val dataPath: Path = Constants.ConfigDir.resolve(fileName)
    protected open var scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    protected open var data: T = newData

    protected abstract fun load()

    abstract fun save()

    @Suppress("MemberVisibilityCanBePrivate")
    fun saveNow() = saveNow(data)

    protected abstract fun saveNow(saveData: T)

    init {
        load()
        ServerLifecycleEvents.SERVER_STARTING.register {
            scope.launch {
                while (isActive) {
                    delay(5.minutes)
                    save()
                }
            }
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            saveNow()
            scope.cancel()
        }
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register { _, _ ->
            save()
        }
    }
}