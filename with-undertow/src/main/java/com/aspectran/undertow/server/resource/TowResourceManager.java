package com.aspectran.undertow.server.resource;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import io.undertow.server.handlers.resource.FileResourceManager;

import java.io.File;
import java.io.IOException;

public class TowResourceManager extends FileResourceManager implements ApplicationAdapterAware {

    private ApplicationAdapter applicationAdapter;

    public TowResourceManager() {
        super(1024, true, false, (String[])null);
    }

    public TowResourceManager(File base) {
        super(base);
    }

    public TowResourceManager(File base, long transferMinSize) {
        super(base, transferMinSize);
    }

    public TowResourceManager(File base, long transferMinSize, boolean caseSensitive) {
        super(base, transferMinSize, caseSensitive);
    }

    public TowResourceManager(File base, long transferMinSize, boolean followLinks, String... safePaths) {
        super(base, transferMinSize, followLinks, safePaths);
    }

    protected TowResourceManager(long transferMinSize, boolean caseSensitive, boolean followLinks, String... safePaths) {
        super(transferMinSize, caseSensitive, followLinks, safePaths);
    }

    public TowResourceManager(File base, long transferMinSize, boolean caseSensitive, boolean followLinks, String... safePaths) {
        super(base, transferMinSize, caseSensitive, followLinks, safePaths);
    }

    public void setBase(String base) throws IOException {
        if (applicationAdapter != null) {
            setBase(applicationAdapter.toRealPathAsFile(base));
        } else {
            setBase(new File(base));
        }
    }

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

}
