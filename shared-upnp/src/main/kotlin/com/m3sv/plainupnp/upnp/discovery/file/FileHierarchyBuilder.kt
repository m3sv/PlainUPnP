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

    private val registry = mutableMapOf<Int, BaseContainer>()

    fun populate(
        contentResolver: ContentResolver,
        parentContainer: BaseContainer,
        column: String,
        uri: Uri,
        containerBuilder: ContainerBuilder
    ): Map<Int, BaseContainer> {
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
                parentContainer = parentContainer,
                rootContainer = root,
                containerBuilder = containerBuilder
            )
        }

        return registry
    }

    private fun traverseTree(
        parentContainer: BaseContainer,
        rootContainer: FolderContainer,
        containerBuilder: ContainerBuilder
    ) {
        val id = "${rootContainer.name}${rootContainer.id}".hashCode()

        val newContainer = containerBuilder(
            id.toString(),
            null,
            rootContainer.name,
            rootContainer.path
        )

        parentContainer.addContainer(newContainer)

        registry[id] = newContainer

        rootContainer.children.forEach {
            when (val value = it.value) {
                is FolderContainer -> traverseTree(newContainer, value, containerBuilder)

                is FolderLeaf -> {
                    val leafId = "${value.name}${value.id}".hashCode()

                    val leafContainer =
                        containerBuilder(
                            leafId.toString(),
                            id.toString(),
                            it.key,
                            value.path
                        )

                    newContainer.addContainer(leafContainer)
                    registry[leafId] = leafContainer
                }
            }
        }
    }
}
