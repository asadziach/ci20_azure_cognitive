package ch.asadzia;

import ch.asadzia.cognitive.Emotion;
import ch.asadzia.cognitive.ServiceResult;
import ch.asadzia.cognitive.ServiceResult.Sentiment;

import java.io.*;

/**
 * Copyright (c) Asad Zia
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
public class Main {

    private static final String imageCaptureFile = "/tmp/capture.jpeg";
    private static final String streamerExe = "/usr/bin/streamer";
    private static final String cameraDeviceFile = "/dev/video0";

    private static File led1 = new File("/sys/class/leds/led1/brightness");
    private static File led2 = new File("/sys/class/leds/led2/brightness");
    private static File led3 = new File("/sys/class/leds/led3/brightness");

    public static void main(String[] args) {
        if(!System.getProperty("os.name").equals("Linux")){
            System.err.println("Only Linux is supported");
            System.exit(-1);
        }
        File videoDevice = new File(cameraDeviceFile);
        if(!videoDevice.exists() || !videoDevice.canRead()){
            System.err.println("Camera not found. Make sure you have appropriate kernel and camera is plugged in.");
            System.exit(-1);
        }
        File streamer = new File(streamerExe);
        if(!streamer.exists() || !streamer.canExecute()) {
            System.err.println("Please install streamer by command \"sudo apt-get install streamer\".");
            System.exit(-1);
        }
        if(!led1.canWrite() || !led2.canWrite() || !led3.canWrite()){
            System.err.println("Cannot control LED brightness. Make sure you have required permissions or run the program as sudo");
            return;
        }

        try {
            Process process = new ProcessBuilder(streamerExe,"-c",cameraDeviceFile, "-b", "16", "-o", imageCaptureFile).start();

            BufferedReader bre = new BufferedReader
                    (new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = bre.readLine()) != null) {
                System.out.println(line);
            }
            bre.close();
            process.waitFor(); // Wait for the snapshot to be taken.

            File image = new File(imageCaptureFile);
            Emotion emotion = new Emotion(image);
            ServiceResult result = emotion.process();
            if(result == null){
                System.err.println("Error during emotion.process()");
                expressSentiment(null);
                return;
            }
            System.out.println("Emotion is " + result.message + ", Sentiment is " + result.sentiment);
            expressSentiment(result.sentiment);

        System.out.println("Thats all folks");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static void expressSentiment(Sentiment sentiment) throws IOException, InterruptedException {

        FileOutputStream led1Brightness = new FileOutputStream(led1);
        FileOutputStream led2Brightness = new FileOutputStream(led2);
        FileOutputStream led3Brightness = new FileOutputStream(led3);

        byte[] bytes = new byte[2];
        bytes[1] = '\n';

        if(sentiment == null){ // error encountered during processing
            bytes[0] = '1';
            led1Brightness.write(bytes);
            led2Brightness.write(bytes);
            led3Brightness.write(bytes);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            bytes[0] = '0';
            led1Brightness.write(bytes);
            led2Brightness.write(bytes);
            led3Brightness.write(bytes);

            return;
        }
        switch(sentiment){
            case POSITIVE:
                bytes[0] = '1';
                led1Brightness.write(bytes);
                bytes[0] = '0';
                led2Brightness.write(bytes);
                led3Brightness.write(bytes);
                break;
            case NEGATIVE:
                bytes[0] = '1';
                led2Brightness.write(bytes);
                bytes[0] = '0';
                led1Brightness.write(bytes);
                led3Brightness.write(bytes);
                break;
            case NEUTRAL:
                bytes[0] = '1';
                led3Brightness.write(bytes);
                bytes[0] = '0';
                led2Brightness.write(bytes);
                led1Brightness.write(bytes);
                break;
        }

        led1Brightness.close();
        led2Brightness.close();
        led3Brightness.close();

        Thread.sleep(1000);
    }
}
