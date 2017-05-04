package libs;

import java.util.Map;

/**
 * ╱╲＿＿＿＿＿＿╱╲
 * ▏╭━━╮╭━━╮▕
 * ▏┃＿＿┃┃＿＿┃▕
 * ▏┃＿▉┃┃▉＿┃▕
 * ▏╰━━╯╰━━╯▕
 * ╲╰╰╯╲╱╰╯╯╱  Created by Silvanosky on 20/04/2017
 * ╱╰╯╰╯╰╯╰╯╲
 * ▏▕╰╯╰╯╰╯▏▕
 * ▏▕╯╰╯╰╯╰▏▕
 * ╲╱╲╯╰╯╰╱╲╱
 * ＿＿╱▕▔▔▏╲＿＿
 * ＿＿▔▔＿＿▔▔＿＿
 */
public class IEntry<K, V> implements Map.Entry<K, V> {

    private final K key;
    private V value;

    public IEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        this.value = value;
        return this.value;
    }
}
