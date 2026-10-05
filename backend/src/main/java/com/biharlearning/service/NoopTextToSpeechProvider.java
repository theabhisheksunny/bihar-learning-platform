package com.biharlearning.service;

import org.springframework.stereotype.Component;

/** Default no-op TTS so the interface has a binding in v1. Replace with a real provider later. */
@Component
public class NoopTextToSpeechProvider implements TextToSpeechProvider {

    @Override
    public boolean isAvailable() { return false; }

    @Override
    public byte[] synthesize(String text, String language) { return null; }
}
