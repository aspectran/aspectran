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
                "style", "plate compact",
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
