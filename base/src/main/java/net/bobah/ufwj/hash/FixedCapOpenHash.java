/*
 * Copyright (c) 2018 Vladimir Lysyy (mrbald@github)
 * ALv2 (http://www.apache.org/licenses/LICENSE-2.0)
 */

package net.bobah.ufwj.hash;

import java.util.stream.IntStream;

/**
 * Fixed capacity open addressing hash table.
 * <p>
 * No memory allocation after construction and good data locality.
 * </p>
 * @param <K> key data type
 * @param <V> value data type
 *
 * @author Vladimir Lysyy (mrbald@github)
 */
public final class FixedCapOpenHash<K, V> {
    private int modOp;

    private final Object[] keys;
    private final Object[] values;

    /**
     * An instance of {@link FixedCapOpenHash} with capacity at least equal to the requested.
     * @param requiredCap required capacity
     */
    public FixedCapOpenHash(int requiredCap) {
        assert requiredCap > 1;

        int cap = 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(requiredCap - 1));

        this.modOp = cap - 1;
        this.keys = new Object[cap];
        this.values = new Object[cap];
        assert keys.length == values.length;
    }

    /**
     * Looks up the slot for the key for read or update operation.
     *
     * @return the position of the slot or -1 if key is not in the table
     */
    public int find(K key) {
        return lookup(key, false);
    }

    /**
     * Looks up the slot for the key for insert or update operation.
     *
     * @return the position if the slot or -1 if the key is not in the table and the table is full
     */
    public int write(K key) {
        final int pos = lookup(key, true);

        if (pos != -1) {
            keys[pos] = key;
        }

        return pos;
    }

    /**
     * @return key of the slot at the specified position
     */
    @SuppressWarnings("unchecked")
    public K getKey(int pos) {
        assert 0 <= pos && pos < keys.length;
        return (K)keys[pos];
    }

    /**
     * @return value of the slot at the specified position
     */
    @SuppressWarnings("unchecked")
    public V getValue(int pos) {
        assert 0 <= pos && pos < keys.length;
        return (V)values[pos];
    }

    public void drop(int pos) {
        assert 0 <= pos && pos < keys.length;
        keys[pos] = null;
        setValue(pos, null);
    }

    public int capacity() {
        return keys.length;
    }

    public IntStream stream() {
        return IntStream.range(0, keys.length).filter(pos -> keys[pos] != null);
    }

    public void setValue(int pos, V value) {
        assert 0 <= pos && pos < keys.length;
        values[pos] = value;
    }

    private int cycled(int pos) {
        return pos & modOp;
    }

    private int lookup(K key, boolean forWrite) {
        final int base = cycled(key.hashCode());

        int pos = base;
        do {
            final Object keyAtPos = keys[pos];
            if (keyAtPos == null) {
                return forWrite ? pos : -1;
            } else if (key.equals(keyAtPos)) {
                return pos;
            }
            pos = cycled(pos + 1);
        } while (pos != base);

        return -1;
    }
}
