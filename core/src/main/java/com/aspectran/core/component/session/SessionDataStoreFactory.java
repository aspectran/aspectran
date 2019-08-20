package com.aspectran.core.component.session;

import com.aspectran.core.context.config.SessionFileStoreConfig;
import com.aspectran.core.util.StringUtils;

import java.io.File;

public class SessionDataStoreFactory {

    public static FileSessionDataStore createFileSessionDataStore(SessionFileStoreConfig fileStoreConfig) {
        FileSessionDataStore fileSessionDataStore = new FileSessionDataStore();
        if (fileStoreConfig != null) {
            String storeDir = fileStoreConfig.getStoreDir();
            if (StringUtils.hasText(storeDir)) {
                fileSessionDataStore.setStoreDir(new File(storeDir));
            }
            boolean deleteUnrestorableFiles = fileStoreConfig.isDeleteUnrestorableFiles();
            if (deleteUnrestorableFiles) {
                fileSessionDataStore.setDeleteUnrestorableFiles(true);
            }
        }
        return fileSessionDataStore;
    }

}
