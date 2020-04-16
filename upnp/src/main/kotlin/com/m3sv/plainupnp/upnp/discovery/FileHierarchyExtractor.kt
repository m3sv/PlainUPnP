package com.m3sv.plainupnp.upnp.discovery

class FileHierarchyExtractor {
    fun extract(
        roots: MutableMap<String, FolderRoot>,
        directories: List<String>,
        parent: FolderContainer?
    ) {
        // if null then we're in the root
        if (directories.isEmpty()) {
            return
        }

        if (parent == null) {
            val directory = directories[0]
            if (roots[directory] == null) {
                roots[directory] = FolderRoot(directory, mutableMapOf())
            }

            if (directories.size > 1) {
                extract(roots, directories.subList(1, directories.size), roots[directory])
            }

            return
        }

        val element = directories[0]

        if (directories.size > 1) {
            if (parent.children[element] == null) {
                parent.children[element] = FolderChild(element, parent)
            }

            extract(
                roots,
                directories.subList(1, directories.size),
                parent.children[element] as FolderChild
            )
        } else {
            if (parent.children[element] == null) {
                parent.children[element] = FolderLeaf(element, parent)
            }
        }
    }
}
