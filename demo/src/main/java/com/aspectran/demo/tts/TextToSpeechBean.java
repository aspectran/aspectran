/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.demo.tts;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Set;

/**
 * Synthesizes speech from text for immediate playback or
 * sends synthesized sound data to an output stream of bytes.
 *
 * <p>Created: 2018. 8. 29.</p>
 */
public class TextToSpeechBean implements InitializableBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(TextToSpeechBean.class);

    private String voicePackage;

    private String voiceName;

    private Voice voice;

    private float rate = 150.0f;

    private float pitch = 100.0f;

    private float pitchRange = 10.0f;

    public void setVoicePackage(String voicePackage) {
        this.voicePackage = voicePackage;
        System.setProperty("freetts.voices", voicePackage);
    }

    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setPitchRange(float pitchRange) {
        this.pitchRange = pitchRange;
    }

    public void setProperties(@NonNull Properties properties) {
        Set<String> keys = properties.stringPropertyNames();
        for (String key : keys) {
            System.setProperty(key, properties.getProperty(key));
        }
    }

    @Override
    public void initialize() {
        if (voicePackage == null) {
            setVoicePackage("com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        }

        VoiceManager voiceManager = VoiceManager.getInstance();

        if (logger.isDebugEnabled()) {
            Voice[] voices = voiceManager.getVoices();
            String[] arr = new String[voices.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = voices[i].getName() + "(" + voices[i].getDomain() + " domain)";
            }
            ToStringBuilder tsb = new ToStringBuilder("All voices available");
            tsb.append("voices", arr);
            logger.debug(tsb.toString());
        }

        if (voiceName != null) {
            voice = voiceManager.getVoice(voiceName);
        } else {
            Voice[] voices = voiceManager.getVoices();
            if (voices != null && voices.length > 0) {
                voice = voices[0];
            }
        }
        if (voice != null) {
            voice.setRate(rate);
            voice.setPitch(pitch);
            voice.setPitchRange(pitchRange);
            voice.allocate();
        }
    }

    @Override
    public void destroy() {
        if (voice != null) {
            voice.deallocate();
            voice = null;
        }
    }

    /**
     * Synthesizes speech of the given text and plays immediately.
     * @param text the text that will be transformed to speech
     */
    public void speak(String text) {
        if (voice == null) {
            throw new IllegalStateException("Cannot find a voice named " + voiceName);
        }
        voice.speak(text);
    }

    public void speak(@NonNull Translet translet) {
        String text = translet.getParameter("text");
        speak(text);
    }

    public static void main(String[] args) {
        TextToSpeechBean textToSpeechBean = new TextToSpeechBean();
        textToSpeechBean.setVoiceName("kevin16");
        textToSpeechBean.initialize();
        textToSpeechBean.speak("a h");
        textToSpeechBean.speak("Something there is that doesn't love a wall, " +
                "That sends the frozen-ground-swell under it, " +
                "And spills the upper boulders in the sun; " +
                "And makes gaps even two can pass abreast.");
        textToSpeechBean.destroy();
    }

}
