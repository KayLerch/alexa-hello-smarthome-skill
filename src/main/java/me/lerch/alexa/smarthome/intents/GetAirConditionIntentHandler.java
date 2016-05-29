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
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.lerch.alexa.smarthome.utils.SkillConfig;
import me.lerch.alexa.smarthome.wrapper.AbstractIntentHandler;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by Kay on 25.04.2016.
 */
public class GetAirConditionIntentHandler extends AbstractIntentHandler {
    private final String SlotAirCondition = SkillConfig.getAlexaSlotAirCondition();
    private final String IOT_THING_NAME = SkillConfig.getAWSIoTthingTempName();
    private final String SlotAirConditionValAll = "condition";
    private final String SlotAirConditionValHum = "humidity";
    private final String SlotAirConditionValTemp = "temperature";
    private final String IOT_THING_ATTR_REPORTED = "reported";
    private final String IOT_THING_ATTR_STATE = "state";
    private final String IOT_THING_ATTR_TEMP = "temperature";
    private final String IOT_THING_ATTR_HUMIDITY = "humidity";

    @Override
    public String getIntentName() {
        return SkillConfig.getAlexaIntentGetAirCondition();
    }

    @Override
    public SpeechletResponse handleIntentRequest(Intent intent, Session session) {
        String airCondition = intent.getSlots().containsKey(SlotAirCondition) ?
                intent.getSlot(SlotAirCondition).getValue() : SlotAirConditionValAll;

        AWSCredentials awsCredentials = SkillConfig.getAWSCredentials();
        AWSIotDataClient iotClient = awsCredentials != null ? new AWSIotDataClient(awsCredentials) : new AWSIotDataClient();

        try {
            GetThingShadowRequest iotRequest = new GetThingShadowRequest().withThingName(IOT_THING_NAME);
            GetThingShadowResult iotResult = iotClient.getThingShadow(iotRequest);
            String strShadowState = bb_to_str(iotResult.getPayload(), Charset.defaultCharset());

            JsonParser jsonShadow = new JsonFactory().createParser(strShadowState);
            JsonNode jsonShadowRoot = new ObjectMapper().readTree(jsonShadow);
            JsonNode jsonShadowState = jsonShadowRoot.path(IOT_THING_ATTR_STATE);

            if (jsonShadowState != null && jsonShadowState.has(IOT_THING_ATTR_REPORTED)) {
                JsonNode jsonShadowStateReported = jsonShadowState.path(IOT_THING_ATTR_REPORTED);
                if (jsonShadowStateReported != null && jsonShadowStateReported.has(IOT_THING_ATTR_TEMP) && jsonShadowStateReported.has(IOT_THING_ATTR_HUMIDITY)) {
                    Double reportedStateTemp = jsonShadowStateReported.path(IOT_THING_ATTR_TEMP).asDouble();
                    Double reportedStateHumidity = jsonShadowStateReported.path(IOT_THING_ATTR_HUMIDITY).asDouble();
                    return getResponse(reportedStateTemp, reportedStateHumidity, airCondition);
                }
            }
            return getErrorResponse("No air condition was reported so far.");
        } catch (Exception ex) {
            return getErrorResponse();
        }
    }

    private SpeechletResponse getResponse(Double reportedStateTemp, Double reportedStateHumidity, String airCondition) {
        StringBuilder sb = new StringBuilder("This room has ");
        if (!SlotAirConditionValTemp.equals(airCondition)) {
            sb.append("a humidity of " + reportedStateHumidity + " percent");
        }
        if (SlotAirConditionValAll.equals(airCondition)) {
            sb.append(" and ");
        }
        if (!SlotAirConditionValHum.equals(airCondition)) {
            sb.append("a temperature of " + reportedStateTemp + " degrees");
        }

        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml("<speak>" + sb.toString() + "</speak>");

        SimpleCard card = new SimpleCard();
        card.setContent(sb.toString());

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
