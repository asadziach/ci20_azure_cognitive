package ch.asadzia.cognitive;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


import java.io.File;
import java.net.URI;
import ch.asadzia.cognitive.ServiceResult.Sentiment;

/**
 * Copyright (c) Asad Zia
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
public class SituationAnalysis extends MsCognitiveService{
    public SituationAnalysis(File data) {
        super(data);
    }

    //An example of bad situations to look after image analysis, tagging and description operation.
    private static String badSituation[] = {"gun","violence","T-1000", "threat", "weapon"};

    //An example of good situations to look after image analysis, tagging and description operation.
    private static String goodSituation[] = {"baby","flower","happy", "friend", "fun"};

    public ServiceResult process()
    {

        HttpClient httpclient = HttpClients.createDefault();

        try
        {
            URIBuilder builder = new URIBuilder("https://api.projectoxford.ai/vision/v1.0/analyze");

            builder.setParameter("visualFeatures", "Categories,Tags,Description,Faces,Adult");

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

                ServiceResult result = translateSituation(responseStr);

                System.out.println(responseStr);

                return result;
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        return null;
    }

    private ServiceResult translateSituation(String string){

        for (int i = 0; i < badSituation.length; i++) {
            if(string.contains(badSituation[i])){
                return new ServiceResult(Sentiment.NEGATIVE, badSituation[i]);
            }

        }

        for (int i = 0; i < goodSituation.length; i++) {
            if(string.contains(goodSituation[i])){
                return new ServiceResult(Sentiment.POSITIVE, goodSituation[i]);
            }

        }

        return new ServiceResult(Sentiment.NEUTRAL, "Not interesting");
    }

}
