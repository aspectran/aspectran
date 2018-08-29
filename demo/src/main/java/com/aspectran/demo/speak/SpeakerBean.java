/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.demo.speak;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

/**
 * <p>Created: 2018. 8. 29.</p>
 */
public class SpeakerBean implements InitializableBean, DisposableBean {

    private String voiceName;

    private Voice voice;

    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }

    @Override
    public void initialize() throws Exception {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");

        if (voiceName == null) {
            voiceName = "kevin16";
        }

        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(voiceName);
        if (voice != null) {
            voice.allocate();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (voice != null) {
            voice.deallocate();
        }
    }

    public void speak(String text) {
        if (voice == null) {
            throw new IllegalStateException("Cannot find a voice named " + voiceName);
        }
        voice.speak(text);
    }

    public static void listAllVoices() {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        System.out.println();
        System.out.println("All voices available:");
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice[] voices = voiceManager.getVoices();
        for (Voice voice1 : voices) {
            System.out.println("    " + voice1.getName() + " (" + voice1.getDomain() + " domain)");
        }
    }

    public static void main(String[] args) {
        listAllVoices();
    }

}
