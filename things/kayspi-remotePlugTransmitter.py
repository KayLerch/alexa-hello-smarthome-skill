import ssl
import json
import sys
import paho.mqtt.client as mqtt
from subprocess import call

mqttClientConnCount = 0
mqttCert_Protocol = ssl.PROTOCOL_TLSv1_2
mqttTopic_get = "$aws/things/kayspi-remotePlugTransmitter/shadow/get"
mqttTopic_getAcc = "$aws/things/kayspi-remotePlugTransmitter/shadow/get/accepted"
mqttTopic_pub = "$aws/things/kayspi-remotePlugTransmitter/shadow/update"
mqttTopic_sub = "$aws/things/kayspi-remotePlugTransmitter/shadow/update/delta"
mqttCert_ca = "./cert/VeriSign-Class-3-Public-Primary-Certification-Authority-G5.pem"
mqttCert = "./cert/kayspi-remotePlugTransmitter/b5fb3ddcac-certificate.pem.crt"
mqttCert_priv = "./cert/kayspi-remotePlugTransmitter/b5fb3ddcac-private.pem.key"
mqttClientId = "kayspi-remotePlugTransmitter"
mqttEndpoint = "A1B71MLXKNXXXX.iot.us-east-1.amazonaws.com"
mqttPort = 8883

# the dipcode set on your remote control for the supplies
dipCode = "10110"
# associate the id set to the physical supplies with a name coming from the shadow
supplies = \
    {
        "light": 1,
        "kettle": 2,
        "music": 3
    }
supplyCommandOn = "on"
supplyCommandOff = "off"
supplyControlScriptPath = "./RPi_utils/send"

# called while client tries to establish connection with the server
def on_connect(mqttc, obj, flags, rc):
    if rc == 0:
        global mqttClientConnCount
        print("Client conntected : " + str(rc) + " | Connection status: successful.")
        # subscribe to topic
        mqttClientConnCount += 1
        mqttClient.subscribe([(mqttTopic_getAcc, 1),(mqttTopic_sub, 1)])


def on_disconnect(client, userdata, rc):
    print("Client connection closed.")


def on_log(pahoClient, obj, level, string):
    print("---------------")
    print(string)


def on_subscribe(mqttc, obj, mid, granted_qos):
    global mqttClientConnCount
    print("Topic subscribed : " + str(mid) + " " + str(granted_qos) + "data" + str(obj))
    # only on initial connect subscribe to topics
    if mqttClientConnCount == 1:
        # request shadow state
        mqttClient.publish(mqttTopic_get, "", 0, False)


def teardown():
    mqttClient.unsubscribe(mqttTopic_sub)
    mqttClient.disconnect()
    mqttClient.loop_stop()
    sys.exit()


def on_message(mqttc, obj, msg):
    print("Message received : " + msg.topic + " | QoS: " + str(msg.qos) + " | Data Received: " + str(msg.payload))
    payload = str(msg.payload).replace("b'", "", 1).replace("'", "")
    # extract progress from json payload
    payloadJson = json.loads(payload)
    if "state" not in payloadJson:
        print("Payload does not contain state-object.");
        return
    payloadState = payloadJson["state"]

    # if this message is a get-response, desired state resides in a sub-node
    if msg.topic == mqttTopic_getAcc:
        if "desired" in payloadState: payloadState = payloadState["desired"]

    reportedState = ""

    # for each supply controlled by this script
    for supplyName in supplies:
        # check if supply is within the desired state information of the shadow
        if supplyName in payloadState:
            supplyState = payloadState[supplyName]
            supplyId = supplies[supplyName]
            # now check if the desired state is associated with a supply command
            if supplyState == supplyCommandOn:
                onOff = 1
            elif supplyState == supplyCommandOff:
                onOff = 0
            else:
                print(supplyName + " has a command " + supplyState + " which is not expected.")
                return
            # fire command as it is desired from the shadow
            call(["sudo", supplyControlScriptPath, dipCode, str(supplyId), str(onOff)])
            # append comma in case this is not the first state to report
            if len(reportedState) > 0: reportedState += ","
            # append fulfilled state to the to be reported state
            reportedState += "{\"" + supplyName + "\":\"" + supplyState + "\"}"

    if len(reportedState) > 0:
        # envelope the report-state(s) in a json message as aws expects it
        payload = "{\"state\":{\"reported\":" + reportedState + ",\"desired\":null}}"
        print("Publish data:" + payload)
        # now report state to the topic
        mqttClient.publish(mqttTopic_pub, payload, 0, True)
        #mqttClient.publish(mqttTopic_pub, payload, 0, False)


mqttClient = mqtt.Client(client_id=mqttClientId, clean_session=True)
# subsribe callback methods to mqtt-events
mqttClient.on_connect = on_connect
mqttClient.on_subscribe = on_subscribe
mqttClient.on_message = on_message
mqttClient.on_disconnect = on_disconnect
mqttClient.on_log = on_log

# Configure network encryption and authentication options. Enables SSL/TLS support.
# adding client-side certificates and enabling tlsv1.2 support as required by aws-iot service
mqttClient.tls_set(mqttCert_ca, certfile=mqttCert, keyfile=mqttCert_priv, tls_version=mqttCert_Protocol, ciphers=None)

print("Start connecting to " + mqttEndpoint + ":" + str(mqttPort) + " ...")

try:
    # connecting to aws-account-specific iot-endpoint
    mqttClient.connect(mqttEndpoint, port=mqttPort)
    mqttClient.loop_forever()
except (KeyboardInterrupt, SystemExit):
    teardown()
