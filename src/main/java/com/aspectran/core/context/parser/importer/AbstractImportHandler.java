/*
 * Copyright 2008-2017 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.parser.importer;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.context.env.Environment;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AbstractImportHandler.
 */
abstract class AbstractImportHandler implements ImportHandler {

    protected final Log log = LogFactory.getLog(getClass());

    private Environment environment;

    private List<Importer> pendingList;

    AbstractImportHandler(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void pending(Importer importer) {
        if (pendingList == null) {
            pendingList = new ArrayList<>();
        }

        pendingList.add(importer);

        if (log.isDebugEnabled()) {
            log.debug("Pending import " + importer);
        }
    }

    protected void handle() throws Exception {
        if (pendingList != null) {
            List<Importer> pendedList = pendingList;
            pendingList = null;

            if (environment != null) {
                for (Importer importer : pendedList) {
                    if (environment.acceptsProfiles(importer.getProfiles())) {
                        if (log.isDebugEnabled()) {
                            log.debug("Import " + importer);
                        }
                        handle(importer);
                    }
                }
            } else {
                for (Importer importer : pendedList) {
                    if (log.isDebugEnabled()) {
                        log.debug("Import " + importer);
                    }
                    handle(importer);
                }
            }
        }
    }

    @Override
    public List<Importer> getPendingList() {
        return pendingList;
    }

}
