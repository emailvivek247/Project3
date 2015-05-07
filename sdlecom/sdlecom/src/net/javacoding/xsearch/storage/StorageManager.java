/*
 * Created on 2006-1-22
 */
package net.javacoding.xsearch.storage;

import net.javacoding.xsearch.config.StorageConfiguration;

public class StorageManager {
    Storage getStorageInstance(StorageConfiguration stc) {
        return new LocalStorage(stc);
    }
}
