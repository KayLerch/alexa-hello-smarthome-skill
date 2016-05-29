package me.lerch.alexa.smarthome;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import me.lerch.alexa.smarthome.speechlets.SmartHomeSpeechlet;
import me.lerch.alexa.smarthome.utils.SkillConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kay on 27.04.2016.
 */
public class SmartHomeSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds = new HashSet<String>();

    static {
        supportedApplicationIds.add(SkillConfig.getAlexaAppId());
    }

    public SmartHomeSpeechletRequestStreamHandler() {
        super(new SmartHomeSpeechlet(), supportedApplicationIds);
    }
}
