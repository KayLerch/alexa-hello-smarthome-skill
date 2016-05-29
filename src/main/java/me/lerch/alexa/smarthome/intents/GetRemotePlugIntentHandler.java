package me.lerch.alexa.smarthome.intents;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.model.GetThingShadowRequest;
import com.amazonaws.services.iotdata.model.GetThingShadowResult;
import com.amazonaws.services.iotdata.model.ResourceNotFoundException;
import com.amazonaws.services.iotdata.model.UpdateThingShadowRequest;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.lerch.alexa.smarthome.utils.SkillConfig;
import me.lerch.alexa.smarthome.wrapper.AbstractIntentHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by Kay on 25.04.2016.
 */
public class GetRemotePlugIntentHandler extends AbstractIntentHandler {
    private final String SlotDeviceName = SkillConfig.getAlexaSlotDeviceName();
    private final String IOT_THING_NAME = SkillConfig.getAWSIoTthingPlugName();
    private final String IOT_THING_ATTR_REPORTED = "reported";
    private final String IOT_THING_ATTR_DESIRED = "desired";
    private final String IOT_THING_ATTR_STATE = "state";

    @Override
    public String getIntentName() {
        return SkillConfig.getAlexaIntentGetRemotePlug();
    }

    @Override
    public SpeechletResponse handleIntentRequest(Intent intent, Session session) {
        String deviceName = intent.getSlots().containsKey(SlotDeviceName) ?
                intent.getSlot(SlotDeviceName).getValue() : null;

        if (deviceName == null)
            return getErrorResponse("No device was provided.");

        AWSCredentials awsCredentials = SkillConfig.getAWSCredentials();
        AWSIotDataClient iotClient = awsCredentials != null ? new AWSIotDataClient(awsCredentials) : new AWSIotDataClient();

        try {
            GetThingShadowRequest iotRequest = new GetThingShadowRequest().withThingName(IOT_THING_NAME);
            GetThingShadowResult iotResult = iotClient.getThingShadow(iotRequest);
            String strShadowState = bb_to_str(iotResult.getPayload(), Charset.defaultCharset());

            JsonParser jsonShadow = new JsonFactory().createParser(strShadowState);
            JsonNode jsonShadowRoot = new ObjectMapper().readTree(jsonShadow);
            JsonNode jsonShadowState = jsonShadowRoot.path(IOT_THING_ATTR_STATE);

            // first look if the desired state was already desired for the attribute
            if (jsonShadowState != null && jsonShadowState.has(IOT_THING_ATTR_DESIRED)) {
                JsonNode jsonShadowStateDesired = jsonShadowState.path(IOT_THING_ATTR_DESIRED);
                if (jsonShadowStateDesired != null && jsonShadowStateDesired.has(deviceName)) {
                    String desiredState = jsonShadowStateDesired.path(deviceName).textValue();
                    return getResponseDesired(deviceName, desiredState);
                }
            }
            // first look if the desired state was already reported from the thing for the attribute
            if (jsonShadowState != null && jsonShadowState.has(IOT_THING_ATTR_REPORTED)) {
                JsonNode jsonShadowStateReported = jsonShadowState.path(IOT_THING_ATTR_REPORTED);
                if (jsonShadowStateReported != null && jsonShadowStateReported.has(deviceName)) {
                    String reportedState = jsonShadowStateReported.path(deviceName).textValue();
                    return getResponseReported(deviceName, reportedState);
                }
            }
            return getResponseNoState(deviceName);
        } catch (Exception ex) {
            return getErrorResponse();
        }
    }

    private SpeechletResponse getResponseNoState(String deviceName) {
        String strContent = "Last state of " + deviceName + " is unknown.";
        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml("<speak>" + strContent + "</speak>");

        SimpleCard card = new SimpleCard();
        card.setContent(strContent);

        SpeechletResponse response = SpeechletResponse.newTellResponse(outputSpeech, card);
        response.setShouldEndSession(true);
        return response;
    }

    private SpeechletResponse getResponseReported(String deviceName, String deviceState) {
        String strContent = "The last known state of " + deviceName + " is it was " + deviceState + ".";
        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml("<speak>" + strContent + "</speak>");

        SimpleCard card = new SimpleCard();
        card.setContent(strContent);

        SpeechletResponse response = SpeechletResponse.newTellResponse(outputSpeech, card);
        response.setShouldEndSession(true);
        return response;
    }

    private SpeechletResponse getResponseDesired(String deviceName, String deviceState) {
        String strContent = "I am not sure. What I can say is: the last desired state of the " + deviceName + " is it should turn " + deviceState + ".";
        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml("<speak>" + strContent + "</speak>");

        SimpleCard card = new SimpleCard();
        card.setContent(strContent);

        SpeechletResponse response = SpeechletResponse.newTellResponse(outputSpeech, card);
        response.setShouldEndSession(true);
        return response;
    }

    private static String bb_to_str(ByteBuffer buffer, Charset charset){
        byte[] bytes;
        if(buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
        return new String(bytes, charset);
    }
}
