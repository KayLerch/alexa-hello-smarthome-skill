This Alexa skill was part of a demo to show custom smarthome integration. The implemented speechlet has to be deployed as a AWS Lambda function which requires AWS IoT things set up in the app.properties. Also the whole hardware setup is not part of this repo. For me there is a Raspberry Pi with two Python scripts which are the actual "things" refered to in app.properties. The Python scripts consume messages resulting from AWS IoT shadow updates and control remote plugs connected to some home appliances.

You have to register two "things" in AWS IoT according to the app.properties. To get into the basics see:
https://www.linkedin.com/pulse/understanding-internet-things-aws-iot-kay-lerch

To learn more on setting up a Raspberry Pi as the runtime environment for the Python scripts in the "things" folder see:
https://www.linkedin.com/pulse/prepare-your-raspberry-pi-work-aws-iot-kay-lerch

To learn more on the demos see:
https://www.linkedin.com/pulse/home-smart-getting-started-iot-4-kay-lerch
https://www.linkedin.com/pulse/cloudwatch-your-homes-air-condition-getting-started-iot-kay-lerch