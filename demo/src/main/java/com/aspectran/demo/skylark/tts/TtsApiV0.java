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
package com.aspectran.demo.skylark.tts;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

@Component
@Bean("ttsApiV0")
public class TtsApiV0 implements TtsApi {

    @Override
    public void tts(@NonNull Translet translet) throws IOException {
        TextToSpeechBean ttsBean = translet.getBean("voice-kevin16");
        ttsBean.speak(translet);
    }

    @Override
    public void download(@NonNull Translet translet) throws IOException {
        TextToSpeechBean ttsBean = translet.getBean("voice-kevin16");
        ttsBean.download(translet);
    }

}
