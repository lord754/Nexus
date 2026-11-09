package com.tungsten.fcl.setting

import android.content.Context
import com.mio.data.Renderer
import com.tungsten.fclcore.util.platform.MemoryUtils

/**
 * 性能预设：一键套用「内存分配 + 渲染器 + 图形后端」的组合。
 *
 * 应用时始终写入显式内存并关闭自动分配（[VersionSetting.isAutoMemory]）：自动分配会在启动前
 * 把内存改写为设备推荐值，导致预设值不生效。内存取值以 [MemoryUtils.findBestRAMAllocation] 的
 * 设备推荐值为基准，保证在不同内存的机器上都落在合理区间。
 */
private const val BACKEND_DEFAULT = "default"
private const val BACKEND_OPENGL = "opengl"
private const val BACKEND_VULKAN = "vulkan"

/** 预设内存下限/MB，避免低内存设备被减到无法启动 */
private const val MIN_MEMORY = 512

enum class PerformancePreset(
    /** 渲染器 id，取值见 [Renderer] 的 ID_* 常量 */
    private val rendererId: String,
    /** 图形后端，取值同版本设置页（仅对 26.2+ 生效） */
    private val graphicsBackend: String,
    /** 由设备推荐内存与总内存算出该预设的最大堆大小/MB */
    private val memory: (recommended: Int, total: Int) -> Int
) {

    /** 性能优先：内存减半 + OpenGL 后端 + 最轻量的 GL4ES 包装器 */
    PERFORMANCE(
        Renderer.ID_GL4ES,
        BACKEND_OPENGL,
        { recommended, _ -> maxOf(MIN_MEMORY, recommended / 2) }
    ),

    /** 均衡：设备推荐内存 + 启动器默认渲染器与图形后端 */
    BALANCED(
        Renderer.ID_NGGL4ES,
        BACKEND_DEFAULT,
        { recommended, _ -> recommended }
    ),

    /** 画质优先：内存上调（不超过总内存一半）+ Vulkan 后端 + Mesa/Zink 渲染器 */
    QUALITY(
        Renderer.ID_ZINK,
        BACKEND_VULKAN,
        { recommended, total -> minOf(recommended * 3 / 2, maxOf(recommended, total / 2)) }
    );

    /** 把预设套用到目标版本设置 */
    fun applyTo(context: Context, setting: VersionSetting) {
        setting.isAutoMemory = false
        setting.maxMemory = memory(
            MemoryUtils.findBestRAMAllocation(context),
            MemoryUtils.getTotalDeviceMemory(context)
        )
        setting.renderer = rendererId
        setting.graphicsBackend = graphicsBackend
    }
}
