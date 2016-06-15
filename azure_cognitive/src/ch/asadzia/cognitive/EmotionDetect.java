package ch.asadzia.cognitive;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Copyright (c) Asad Zia
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
public class EmotionDetect extends MsCognitiveService {

    public EmotionDetect(File data) {
        super(data);
    }

    public ServiceResult process()
    {

        HttpClient httpclient = HttpClients.createDefault();

        try
        {
            URIBuilder builder = new URIBuilder("https://api.projectoxford.ai/emotion/v1.0/recognize");


            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", apikey);


            // Request body
            FileEntity reqEntity = new FileEntity(imageData);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                String responseStr = EntityUtils.toString(entity);

                if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                    System.err.println(responseStr);
                    return null;
                }

                JSONArray jsonArray = (JSONArray)new JSONParser().parse(responseStr);
                JSONObject jsonObject = (JSONObject) jsonArray.get(0);

                HashMap<char[], Double> scores = (HashMap)  jsonObject.get("scores");

                Map.Entry<char[], Double> maxEntry = null;

                for (Map.Entry<char[], Double> entry : scores.entrySet()) {
                    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());

                    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                    {
                        maxEntry = entry;
                    }
                }
                Object key = maxEntry.getKey();

                String winningEmotionName = (String) key;

                ServiceResult result = new ServiceResult(translateEmotion(winningEmotionName), winningEmotionName);

                System.out.println(responseStr);

                return result;
            }
        }
        catch (Exception e)
        {
            System.err.println(e.toString());
        }
        return null;
    }

    private ServiceResult.Sentiment translateEmotion(String string){

        ServiceResult.Sentiment sentiment;

        switch (string.toLowerCase()) {
            case "anger":
            case "contempt":
            case "disgust":
            case "fear":
            case "sadness":
                sentiment = ServiceResult.Sentiment.NEGATIVE;
                break;
            case "happiness":
            case "surprise":
                sentiment = ServiceResult.Sentiment.POSITIVE;
                break;
            case "neutral":
            default:
                sentiment = ServiceResult.Sentiment.NEUTRAL;
                break;
        }
        return sentiment;
    }
}
