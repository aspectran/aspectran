/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.demo.anatomy;

import com.aspectran.core.component.bean.annotation.Action;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Dispatch;
import com.aspectran.core.component.bean.annotation.Request;
import com.aspectran.core.component.bean.annotation.Transform;
import com.aspectran.core.context.rule.type.FormatType;

import java.util.Map;

/**
 * A controller that provides framework anatomy data for the viewer.
 */
@Component
@Bean("anatomyActivity")
public class AnatomyActivity {

    private final AnatomyService anatomyService;

    @Autowired
    public AnatomyActivity(AnatomyService anatomyService) {
        this.anatomyService = anatomyService;
    }

    /**
     * Dispatches to the anatomy viewer page within the default template.
     */
    @Request("/anatomy/viewer")
    @Dispatch("templates/default")
    @Action("page")
    public Map<String, String> viewer() {
        return Map.of(
                "include", "anatomy/viewer",
                "style", "plate solid compact",
                "headline", "Framework Anatomy"
        );
    }

    /**
     * Provides framework anatomy data as JSON.
     * @return a map containing the anatomy data, identified by "anatomyData"
     */
    @Request("/anatomy/data")
    @Action("anatomyData")
    @Transform(format = FormatType.JSON)
    public Map<String, Object> data() {
        return anatomyService.getAnatomyData();
    }

}
