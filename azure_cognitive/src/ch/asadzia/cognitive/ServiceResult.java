package ch.asadzia.cognitive;

/**
 * Copyright (c) Asad Zia
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
public class ServiceResult {

    public enum Sentiment{
        POSITIVE, NEGATIVE, NEUTRAL
    }

    public Sentiment sentiment;
    public String message;

    /* package */ ServiceResult(Sentiment sentiment, String msg){
        message = msg;
        this.sentiment = sentiment;
    }
}
