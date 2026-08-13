package dev.andrewohara.utils.jdk

fun <T: Any> reconcileCollection(
    existing: Collection<T>,
    new: Collection<T>,
    idFn: (T) -> String,
    equalsFn: (T, T) -> Boolean = { a, b -> a == b },
    createFn: (Collection<T>) -> Unit,
    updateFn: (Collection<T>) -> Unit,
    deleteFn: (Collection<T>) -> Unit
) {
    val existingById = existing.associateBy(idFn)
    val newById = new.associateBy(idFn)
    
    new.filter { idFn(it) !in existingById }.also(createFn)
    existing.filter { idFn(it) !in newById }.also(deleteFn)
    new.filter {
        val existing = existingById[idFn(it)]
        existing != null && !equalsFn(existing, it)
    }.also(updateFn)
}

fun <T: Any> reconcileCollectionIndividual(
    existing: Collection<T>,
    new: Collection<T>,
    idFn: (T) -> String,
    equalsFn: (T, T) -> Boolean = { a, b -> a == b },
    createFn: (T) -> Unit,
    deleteFn: (T) -> Unit,
    updateFn: (T) -> Unit = { deleteFn(it); createFn(it) }
) = reconcileCollection(
    existing = existing,
    new = new,
    idFn = idFn,
    equalsFn = equalsFn,
    createFn = { it.forEach(createFn) },
    updateFn = { it.forEach(updateFn) },
    deleteFn = { it.forEach(deleteFn) }
)

/**
 * Updates an existing collection with new data.
 *
 * The modelKeyFn and dataKeyFn determine if an existing model should be updated or a new model created.
 * With the appropriate equalsFn and updateFn, metadata such as a created timestamp can be preserved.
 *
 * The output order matches the other of the data
 */
fun <Model: Any, Data: Any> updateCollection(
    existing: Collection<Model>,
    data: List<Data>,
    modelKeyFn: (Model) -> String,
    dataKeyFn: (Data) -> String,
    equalsFn: (Model, Data) -> Boolean,
    newFn: (Data) -> Model,
    updateFn: (Model, Data) -> Model
): List<Model> {
    val existingByKey = existing.associateBy(modelKeyFn)

    return data.map { item ->
        val key = dataKeyFn(item)
        val existing = existingByKey[key]

        when {
            existing == null -> newFn(item)
            equalsFn(existing, item) -> existing
            else -> updateFn(existing, item)
        }
    }
}
