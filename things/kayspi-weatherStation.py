import Adafruit_DHT
import ssl
import sys
import paho.mqtt.client as mqtt
import time

sensor = Adafruit_DHT.DHT22
pin = 21 # GPIO 21
delaySecondsBetweenPublish = 1

mqttCert_Protocol = ssl.PROTOCOL_TLSv1_2
mqttTopic_pub = "$aws/things/kayspi-weatherStation/shadow/update"
mqttTopic_sub = "$aws/things/kayspi-weatherStation/shadow/update/rejected"
mqttCert_ca = "./cert/VeriSign-Class-3-Public-Primary-Certification-Authority-G5.pem"
mqttCert = "./cert/kayspi-weatherStation/de6f9196d1-certificate.pem.crt"
mqttCert_priv = "./cert/kayspi-weatherStation/de6f9196d1-private.pem.key"
mqttClientId = "kayspi-weatherStation"
mqttEndpoint = "A1B71MLXKNXXXX.iot.us-east-1.amazonaws.com"
mqttPort = 8883

def on_connect(mqttc, obj, flags, rc):
    if rc == 0:
        print("Client conntected : " + str(rc) + " | Connection status: successful.")
        mqttClient.subscribe(mqttTopic_sub, qos=0)
        publish_data()

def publish_data():
    #time.sleep(delaySecondsBetweenPublish)
    humidity, temperature = Adafruit_DHT.read_retry(sensor, pin)
    if humidity is not None and temperature is not None:
        payload = '{{"state":{{"reported":{{"humidity":{0:0.1f},"temperature":{1:0.1f}}}}}}}' \
            .format(humidity,temperature)
        print("Publish {0}".format(payload))
        mqttClient.publish(mqttTopic_pub, payload, 0, False)

def on_disconnect(client, userdata, rc):
    print("Client connection closed.")

def on_log(pahoClient, obj, level, string):
    print("---------------")
    print(string)

def on_publish(mosq, obj, mid):
    print("mid: " + str(mid))
    publish_data()

def teardown():
    mqttClient.disconnect()
    mqttClient.loop_stop()
    sys.exit()

mqttClient = mqtt.Client(client_id=mqttClientId)
mqttClient.on_connect = on_connect
mqttClient.on_disconnect = on_disconnect
mqttClient.on_publish = on_publish
mqttClient.on_log = on_log

mqttClient.tls_set(mqttCert_ca, certfile=mqttCert, keyfile=mqttCert_priv, tls_version=mqttCert_Protocol, ciphers=None)

print("Start connecting to " + mqttEndpoint + ":" + str(mqttPort) + " ...")

try:
    mqttClient.connect(mqttEndpoint, port=mqttPort)
    mqttClient.loop_forever()
except (KeyboardInterrupt, SystemExit):
    teardown()