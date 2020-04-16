package com.m3sv.plainupnp.upnp.discovery


class FileTree(private val extractor: FileHierarchyExtractor) {

    private val roots: MutableMap<String, FolderRoot> = mutableMapOf()

    val fileFolderRoots: Collection<FolderRoot> = roots.values

    private val pathCache = mutableSetOf<String>()

    /**
     * We assume that first inserted path defines root
     */
    fun insertPath(path: String) {
        val vettedPath = path.substring(0, path.indexOfLast { it == '/' })

        if (pathCache.contains(vettedPath))
            return

        val splitPath = vettedPath.split("/")
        extractor.extract(roots, splitPath, null)
        pathCache.add(vettedPath)
    }
}
