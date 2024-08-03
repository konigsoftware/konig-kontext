package com.konigsoftware.kontext

import kotlin.reflect.KClass


internal object PersistentHashArrayMappedTrie {
    fun <K, V> get(root: Node<K, V>?, key: K): V? {
        return root?.get(key, key.hashCode(), 0)
    }

    fun <K, V> put(root: Node<K, V>?, key: K, value: V): Node<out K, out V> {
        return root?.put(
            key,
            value,
            key.hashCode(),
            0
        ) ?: Leaf(key, value)
    }

    internal interface Node<K, V> {
        fun get(var1: K, var2: Int, var3: Int): V?

        fun put(var1: K, var2: V, var3: Int, var4: Int): Node<out K, out V>

        fun size(): Int
    }

    internal class CompressedIndex<K, V> private constructor(
        val bitmap: Int,
        val values: Array<Node<K, V>>,
        private val size: Int
    ) : Node<K, V> {
        private val BITS = 5
        private val BITS_MASK = 31

        override fun size(): Int = this.size

        override fun get(key: K, hash: Int, bitsConsumed: Int): V? {
            val indexBit = indexBit(hash, bitsConsumed)
            if ((this.bitmap and indexBit) == 0) {
                return null
            } else {
                val compressedIndex = this.compressedIndex(indexBit)
                return values[compressedIndex]!!.get(key, hash, bitsConsumed + 5)
            }
        }

        override fun put(key: K, value: V, hash: Int, bitsConsumed: Int): Node<K, V> {
            val indexBit = indexBit(hash, bitsConsumed)
            val compressedIndex = this.compressedIndex(indexBit)
            if ((this.bitmap and indexBit) == 0) {
                val newBitmap = this.bitmap or indexBit
                val newValues = arrayOf<Node<K, V>>(
                    Leaf(key, value)
                )
                System.arraycopy(this.values, 0, newValues, 0, compressedIndex)
                newValues[compressedIndex] = Leaf(key, value)
                System.arraycopy(
                    this.values, compressedIndex, newValues, compressedIndex + 1,
                    values.size - compressedIndex
                )
                return CompressedIndex(newBitmap, newValues, this.size() + 1)
            } else {
                val newValues = values.copyOf(
                    values.size
                ) as? Array<Node<K, V>> ?: throw IllegalStateException("Unable to cast newValues as non-nullable array of nodes")
                newValues[compressedIndex] =
                    values[compressedIndex].put(key, value, hash, bitsConsumed + 5)
                var newSize = this.size()
                newSize += newValues[compressedIndex].size()
                newSize -= values[compressedIndex].size()
                return CompressedIndex(this.bitmap, newValues, newSize)
            }
        }

        override fun toString(): String {
            val valuesSb = StringBuilder()
            valuesSb.append("CompressedIndex(").append(
                String.format(
                    "bitmap=%s ", Integer.toBinaryString(
                        this.bitmap
                    )
                )
            )
            val var2 = this.values
            val var3 = var2.size

            for (var4 in 0 until var3) {
                val value: Node<K, V> = var2[var4]
                valuesSb.append(value).append(" ")
            }

            return valuesSb.append(")").toString()
        }

        private fun compressedIndex(indexBit: Int): Int {
            return Integer.bitCount(this.bitmap and (indexBit - 1))
        }

        companion object {
            fun <K, V> combine(
                node1: Node<K, V>,
                hash1: Int,
                node2: Node<K, V>,
                hash2: Int,
                bitsConsumed: Int
            ): Node<K, V> {
                var node1 = node1
                var node2 = node2
                assert(hash1 != hash2)

                val indexBit1 = indexBit(hash1, bitsConsumed)
                val indexBit2 = indexBit(hash2, bitsConsumed)
                val nodeCopy: Node<K, V>
                if (indexBit1 == indexBit2) {
                    nodeCopy = combine(node1, hash1, node2, hash2, bitsConsumed + 5)
                    val values = arrayOf(nodeCopy)
                    return CompressedIndex(indexBit1, values, nodeCopy.size())
                } else {
                    if (uncompressedIndex(hash1, bitsConsumed) > uncompressedIndex(hash2, bitsConsumed)) {
                        nodeCopy = node1
                        node1 = node2
                        node2 = nodeCopy
                    }

                    val values = arrayOf(node1, node2)
                    return CompressedIndex(indexBit1 or indexBit2, values, node1.size() + node2.size())
                }
            }

            private fun uncompressedIndex(hash: Int, bitsConsumed: Int): Int {
                return hash ushr bitsConsumed and 31
            }

            private fun indexBit(hash: Int, bitsConsumed: Int): Int {
                val uncompressedIndex = uncompressedIndex(hash, bitsConsumed)
                return 1 shl uncompressedIndex
            }
        }
    }

    internal class CollisionLeaf<K, V>(private val keys: Array<K>, private val values: Array<V>, private val keyClass: Class<K>, private val valueClass: Class<V>) : Node<K, V> {
        override fun size(): Int {
            return values.size
        }

        override fun get(key: K, hash: Int, bitsConsumed: Int): V? {
            for (i in keys.indices) {
                if (keys[i] === key) {
                    return values[i]
                }
            }

            return null
        }

        override fun put(key: K, value: V, hash: Int, bitsConsumed: Int): Node<K, V> {
            val thisHash = keys[0].hashCode()
            if (thisHash != hash) {
                return CompressedIndex.combine(
                    Leaf(key, value), hash,
                    this, thisHash, bitsConsumed
                )
            } else {
                var keyIndex: Int
                val newKeys: Array<K>
                val newValues: Array<V>
                if ((indexOfKey(key).also { keyIndex = it }) != -1) {
                    newKeys = keys.copyOf(keys.size) as? Array<K> ?: throw IllegalStateException("Unable to cast new keys as non-nullable array")
                    newValues = values.copyOf(keys.size) as? Array<V> ?: throw IllegalStateException("Unable to cast new values as non-nullable array")
                    newKeys[keyIndex] = key
                    newValues[keyIndex] = value
                    return CollisionLeaf(newKeys, newValues)
                } else {
                    newKeys = keys.copyOf(keys.size + 1) as? Array<K> ?: throw IllegalStateException("Unable to cast new keys as non-nullable array")
                    newValues = values.copyOf(keys.size + 1) as? Array<V> ?: throw IllegalStateException("Unable to cast new values as non-nullable array")
                    newKeys[keys.size] = key
                    newValues[keys.size] = value
                    return CollisionLeaf(newKeys, newValues)
                }
            }
        }

        private fun indexOfKey(key: K): Int {
            for (i in keys.indices) {
                if (keys[i] === key) {
                    return i
                }
            }

            return -1
        }

        override fun toString(): String {
            val valuesSb = StringBuilder()
            valuesSb.append("CollisionLeaf(")

            for (i in values.indices) {
                valuesSb.append("(key=").append(keys[i]).append(" value=").append(values[i]).append(") ")
            }

            return valuesSb.append(")").toString()
        }
    }

    internal class Leaf<K, V>(private val key: K, private val value: V) : Node<K, V> {
        override fun size(): Int {
            return 1
        }

        override fun get(key: K, hash: Int, bitsConsumed: Int): V? {
            return if (this.key === key) this.value else null
        }

        override fun put(key: K, value: V, hash: Int, bitsConsumed: Int): Node<out K, out V> {
            val thisHash = this.key.hashCode()
            return if (thisHash != hash) {
                CompressedIndex.combine(
                    Leaf(key, value), hash,
                    this, thisHash, bitsConsumed
                )
            } else {
                (if (this.key === key) Leaf(key, value) else CollisionLeaf(
                    arrayOf(this.key, key),
                    arrayOf(this.value, value),
                    this.key::class.java,
                    this.value::class.java
                ))
            }
        }

        override fun toString(): String {
            return String.format("Leaf(key=%s value=%s)", this.key, this.value)
        }
    }
}
