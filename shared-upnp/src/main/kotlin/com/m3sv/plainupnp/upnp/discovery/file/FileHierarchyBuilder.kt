package com.m3sv.plainupnp.upnp.discovery.file

import android.content.ContentResolver
import android.net.Uri
import com.m3sv.plainupnp.upnp.mediacontainers.BaseContainer

typealias ContainerBuilder = (
    id: String,
    parentId: String?,
    containerName: String,
    path: String
) -> BaseContainer

class FileHierarchyBuilder {

    private val fileTree = FileTree(FileHierarchyExtractor())

    fun populate(
        contentResolver: ContentResolver,
        baseContainer: BaseContainer,
        containerRegistry: MutableMap<Int, BaseContainer?>,
        column: String,
        uri: Uri,
        containerBuilder: ContainerBuilder
    ) {
        contentResolver.query(
            uri,
            arrayOf(column),
            null,
            null,
            null
        )?.use { cursor ->
            val pathColumn = cursor.getColumnIndexOrThrow(column)
            while (cursor.moveToNext()) {
                var path = cursor.getString(pathColumn)
                if (path.startsWith("/")) {
                    path = path.drop(1)
                }

                fileTree.insertPath(path)
            }
        }

        fileTree.fileFolderRoots.forEach { root ->
            traverseTree(
                baseContainer = baseContainer,
                container = root,
                containerRegistry = containerRegistry,
                containerBuilder = containerBuilder
            )
        }
    }

    private fun traverseTree(
        baseContainer: BaseContainer,
        container: FolderContainer,
        containerRegistry: MutableMap<Int, BaseContainer?>,
        containerBuilder: ContainerBuilder
    ) {
        val id = "${container.name}${container.id}".hashCode()

        val newContainer = containerBuilder(
            id.toString(),
            null,
            container.name,
            container.path
        )

        baseContainer.addContainer(newContainer)
        containerRegistry[id] = newContainer

        container.children.forEach {
            val value = it.value

            if (value is FolderContainer)
                traverseTree(newContainer, value, containerRegistry, containerBuilder)

            if (value is FolderLeaf) {
                val leafId = "${value.name}${value.id}".hashCode()

                val leafContainer =
                    containerBuilder(
                        leafId.toString(),
                        id.toString(),
                        it.key,
                        value.path
                    )

                newContainer.addContainer(leafContainer)
                containerRegistry[leafId] = leafContainer
            }
        }
    }
}
