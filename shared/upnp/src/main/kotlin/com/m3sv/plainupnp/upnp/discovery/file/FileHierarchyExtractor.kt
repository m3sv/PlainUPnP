package com.m3sv.plainupnp.upnp.discovery.file

import java.util.*

class FileHierarchyExtractor {
    fun extract(
        roots: MutableMap<String, FolderRoot>,
        directories: Queue<String>
    ) {
        if (directories.isEmpty())
            return

        val rootName = requireNotNull(directories.poll())

        if (roots[rootName] == null) {
            roots[rootName] = FolderRoot(rootName)
        }

        var root: FolderContainer = requireNotNull(roots[rootName])

        while (directories.isNotEmpty()) {
            val directory = requireNotNull(directories.poll())

            val element = root.children[directory]
            val isElementExists = element != null
            val isQueueEmpty = directories.isEmpty()

            when {
                !isElementExists && isQueueEmpty -> root.children[directory] =
                    FolderLeaf(directory, root)
                !isElementExists && !isQueueEmpty -> {
                    val child = FolderChild(directory, root)
                    root.children[directory] = child
                    root = child
                }
                isElementExists && isQueueEmpty -> Unit
                isElementExists && !isQueueEmpty -> if (element is FolderLeaf) {
                    val child = FolderChild(directory, root)
                    root.children[directory] = child
                    root = child
                } else {
                    root = element as FolderContainer
                }
            }
        }
    }
}
