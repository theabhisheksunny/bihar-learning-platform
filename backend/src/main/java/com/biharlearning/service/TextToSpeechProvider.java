package com.biharlearning.service;

/**
 * Pluggable read-aloud provider (spec section 21). No external TTS is a hard dependency in v1;
 * a provider can be wired later (cloud TTS, on-device, etc.) without touching callers.
 */
public interface TextToSpeechProvider {

    boolean isAvailable();

    /**
     * Synthesize speech for the given text in the given language.
     * @return audio bytes (e.g. mp3), or null if unavailable.
     */
    byte[] synthesize(String text, String language);
}
