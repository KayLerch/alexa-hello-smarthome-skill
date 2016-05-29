package me.lerch.alexa.smarthome.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Encapsulates access to application-wide property values
 */
public class SkillConfig {
    private static Properties properties = new Properties();
    private static final String propertiesFile = "app.properties";

    /**
     * Static block does the bootstrapping of all configuration properties with
     * reading out values from different resource files
     */
    static {
        InputStream propertiesStream = SkillConfig.class.getClassLoader().getResourceAsStream(propertiesFile);
        try {
            properties.load(propertiesStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (propertiesStream != null) {
                try {
                    propertiesStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Application-id which should be supported by this skill implementation
     */
    public static String getAlexaAppId() {
        return properties.getProperty("AlexaAppId");
    }

    public static String getAlexaIntentGetAirCondition() {
        return properties.getProperty("AlexaIntentGetAirCondition");
    }

    public static String getAWSIoTthingTempName() {
        return properties.getProperty("AWSIoTthingTempName");
    }

    public static String getAWSIoTthingPlugName() {
        return properties.getProperty("AWSIoTthingPlugName");
    }

    public static String getAlexaIntentGetRemotePlug() {
        return properties.getProperty("AlexaIntentGetRemotePlug");
    }

    public static String getAlexaSlotAirCondition() {
        return properties.getProperty("AlexaSlotAirCondition");
    }

    public static String getAlexaSlotDeviceName() {
        return properties.getProperty("AlexaSlotDeviceName");
    }

    public static String getAlexaIntentSetRemotePlug() {
        return properties.getProperty("AlexaIntentSetRemotePlug");
    }

    public static String getAlexaSlotDeviceState() {
        return properties.getProperty("AlexaSlotDeviceState");
    }

    public static AWSCredentials getAWSCredentials() {
        String awsKey = getAWSAccessKey();
        String awsSecret = getAWSAccessSecret();

        if (awsKey != null && !awsKey.isEmpty() && awsSecret != null && !awsSecret.isEmpty()) {
            return new BasicAWSCredentials(awsKey, awsSecret);
        }
        return null;
    }

    private static String getAWSAccessKey() {
        return properties.getProperty("AWSAccessKey");
    }

    private static String getAWSAccessSecret() {
        return properties.getProperty("AWSAccessSecret");
    }
}
