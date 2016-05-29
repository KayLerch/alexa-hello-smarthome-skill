package me.lerch.alexa.smarthome.intents;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.model.UpdateThingShadowRequest;
import me.lerch.alexa.smarthome.utils.SkillConfig;
import me.lerch.alexa.smarthome.wrapper.AbstractIntentHandler;

import java.nio.ByteBuffer;

/**
 * Created by Kay on 25.04.2016.
 */
public class SetRemotePlugIntentHandler extends AbstractIntentHandler {
    private final String SlotDeviceName = SkillConfig.getAlexaSlotDeviceName();
    private final String SlotDeviceState = SkillConfig.getAlexaSlotDeviceState();
    private final String IOT_THING_NAME = SkillConfig.getAWSIoTthingPlugName();
    private final String IOT_THING_ATTR_DESIRED = "desired";
    private final String IOT_THING_ATTR_STATE = "state";

    @Override
    public String getIntentName() {
        return SkillConfig.getAlexaIntentSetRemotePlug();
    }

    @Override
    public SpeechletResponse handleIntentRequest(Intent intent, Session session) {
        String deviceName = intent.getSlots().containsKey(SlotDeviceName) ?
                intent.getSlot(SlotDeviceName).getValue() : null;

        String deviceState = intent.getSlots().containsKey(SlotDeviceState) ?
                intent.getSlot(SlotDeviceState).getValue() : null;

        if (deviceName == null || deviceState == null)
            return getErrorResponse("No device was provided.");

        try {
            AWSCredentials awsCredentials = SkillConfig.getAWSCredentials();
            AWSIotDataClient iotClient = awsCredentials != null ? new AWSIotDataClient(awsCredentials) : new AWSIotDataClient();

            String strPayload = "{\"" + IOT_THING_ATTR_STATE + "\":{\"" + IOT_THING_ATTR_DESIRED + "\":{\"" + deviceName + "\":\"" + deviceState + "\"}}}";
            ByteBuffer buffer = null;
            try {
                buffer = ByteBuffer.wrap(strPayload.getBytes("UTF-8"));
                UpdateThingShadowRequest iotRequest = new UpdateThingShadowRequest().withThingName(IOT_THING_NAME).withPayload(buffer);
                iotClient.updateThingShadow(iotRequest);
            } catch (Exception e) {
                e.printStackTrace();
                return getErrorResponse();
            }
            return getResponse(deviceName, deviceState);
        } catch (Exception e) {
            return getErrorResponse();
        }
    }

    private SpeechletResponse getResponse(String deviceName, String deviceState) {
        String strContent = deviceName + " is asked to turn " + deviceState + ".";
        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml("<speak>" + strContent + "</speak>");

        SimpleCard card = new SimpleCard();
        card.setContent(strContent);

        SpeechletResponse response = SpeechletResponse.newTellResponse(outputSpeech, card);
        response.setShouldEndSession(true);
        return response;
    }
}
