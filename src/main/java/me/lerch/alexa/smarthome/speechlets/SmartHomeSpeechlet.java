package me.lerch.alexa.smarthome.speechlets;

import com.amazon.speech.ui.SsmlOutputSpeech;
import me.lerch.alexa.smarthome.intents.GetAirConditionIntentHandler;
import me.lerch.alexa.smarthome.intents.GetRemotePlugIntentHandler;
import me.lerch.alexa.smarthome.intents.SetRemotePlugIntentHandler;
import me.lerch.alexa.smarthome.wrapper.AbstractSpeechlet;
import me.lerch.alexa.smarthome.wrapper.IIntentHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kay on 25.04.2016.
 */
public class SmartHomeSpeechlet extends AbstractSpeechlet {

    private final List<IIntentHandler> intentHandlers;

    public SmartHomeSpeechlet() {
        intentHandlers = new ArrayList<>();
        intentHandlers.add(new SetRemotePlugIntentHandler());
        intentHandlers.add(new GetRemotePlugIntentHandler());
        intentHandlers.add(new GetAirConditionIntentHandler());
    }

    @Override
    public String getSampleSpeech() {
        return "Get air condition.";
    }

    @Override
    public List<IIntentHandler> getIntentHandlers() {
        return intentHandlers;
    }

    @Override
    public SsmlOutputSpeech getWelcomeSpeech() {
        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml("<speak>Welcome to smart home demo. What can I do for you?</speak>");
        return outputSpeech;
    }
}
