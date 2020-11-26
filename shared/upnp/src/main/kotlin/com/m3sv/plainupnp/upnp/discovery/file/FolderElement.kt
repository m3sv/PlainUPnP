package com.m3sv.plainupnp.upnp.discovery.file

private var index: Int = 0

abstract class FolderElement {
    val id: Int = index++
    abstract val name: String
    abstract val parent: FolderElement?

    val path: String
        get() {
            if (parent == null)
                return name

            if (parent is FolderRoot) {
                return "${parent!!.name}/$name"
            }

            if (parent is FolderChild) {
                return getPath(parent!!, name)
            }

            throw IllegalStateException("Parent is Leaf or doesn't exist")
        }


    private fun getPath(parent: FolderElement?, name: String): String {
        if (parent is FolderRoot) {
            return "${parent.name}/$name"
        }

        if (parent is FolderChild) {
            return getPath(parent.parent, "${parent.name}/$name")
        }

        throw IllegalStateException("Parent is Leaf or doesn't exist")
    }
}

abstract class FolderContainer : FolderElement() {
    abstract val children: MutableMap<String, FolderElement>
}

data class FolderRoot(
    override val name: String,
    override val children: MutableMap<String, FolderElement> = mutableMapOf(),
    override val parent: FolderElement? = null
) : FolderContainer() {
    override fun toString(): String = name
}

data class FolderChild(
    override val name: String,
    override val parent: FolderElement?,
    override val children: MutableMap<String, FolderElement> = mutableMapOf()
) : FolderContainer() {
    override fun toString(): String = name
}

data class FolderLeaf(
    override val name: String,
    override val parent: FolderElement
) : FolderElement()

