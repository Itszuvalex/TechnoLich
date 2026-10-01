package com.itszuvalex.technolich.core.frag

import com.itszuvalex.technolich.core.IBlockEntityFragment
import com.itszuvalex.technolich.core.IFragmentHost
import com.itszuvalex.technolich.core.IInternalBlockEntityFragment

/**
 * Base for fragments that need the owning block entity once attached, e.g. to mark it dirty.
 */
abstract class InternalBlockEntityFragment : IInternalBlockEntityFragment {
    protected var host: IFragmentHost? = null
        private set

    override fun onAttach(host: IFragmentHost) {
        this.host = host
    }

    /**
     * [IFragmentHost.markDirty]; does nothing before the fragment is attached.
     */
    protected fun markDirty() {
        host?.markDirty()
    }

    /**
     * [IFragmentHost.markDirtyAndSync]; does nothing before the fragment is attached.
     */
    protected fun markDirtyAndSync() {
        host?.markDirtyAndSync()
    }
}

abstract class BlockEntityFragment<T : Any> : InternalBlockEntityFragment(), IBlockEntityFragment<T>
