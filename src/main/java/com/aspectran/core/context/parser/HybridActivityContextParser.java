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
package com.aspectran.core.context.parser;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.parser.apon.RootAponDisassembler;
import com.aspectran.core.context.parser.apon.params.AspectranParameters;
import com.aspectran.core.context.parser.importer.HybridImportHandler;
import com.aspectran.core.context.parser.importer.ImportHandler;
import com.aspectran.core.context.parser.importer.Importer;

/**
 * The Class HybridActivityContextParser.
 * 
 * <p>Created: 2015. 01. 27 PM 10:36:29</p>
 */
public class HybridActivityContextParser extends AbstractActivityContextParser {

    private final String encoding;

    public HybridActivityContextParser(ApplicationAdapter applicationAdapter) {
        this(applicationAdapter, null);
    }

    public HybridActivityContextParser(ApplicationAdapter applicationAdapter, String encoding) {
        super(applicationAdapter);
        this.encoding = encoding;
    }

    @Override
    public ActivityContext parse(String rootContext) throws ActivityContextParserException {
        try {
            if (rootContext == null) {
                throw new IllegalArgumentException("The rootContext argument must not be null.");
            }

            ImportHandler importHandler = new HybridImportHandler(this, encoding, isHybridLoad());
            getContextParserAssistant().setImportHandler(importHandler);

            Importer importer = resolveImporter(rootContext);
            importHandler.handle(importer);

            return createActivityContext();
        } catch (Exception e) {
            throw new ActivityContextParserException("Failed to parse an ActivityContext with " + rootContext, e);
        }
    }

    @Override
    public ActivityContext parse(AspectranParameters aspectranParameters) throws ActivityContextParserException {
        try {
            if (aspectranParameters == null) {
                throw new IllegalArgumentException("The aspectranParameters argument must not be null.");
            }

            ImportHandler importHandler = new HybridImportHandler(this, encoding, isHybridLoad());
            getContextParserAssistant().setImportHandler(importHandler);

            RootAponDisassembler rootAponDisassembler = new RootAponDisassembler(getContextParserAssistant());
            rootAponDisassembler.disassembleAspectran(aspectranParameters);

            importHandler.handle(null);

            return createActivityContext();
        } catch (Exception e) {
            throw new ActivityContextParserException("Failed to parse an ActivityContext with " + aspectranParameters, e);
        }
    }

}
