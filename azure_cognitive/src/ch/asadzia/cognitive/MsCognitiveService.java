package ch.asadzia.cognitive;

import java.io.File;

/**
 * Copyright (c) Asad Zia
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
public abstract class MsCognitiveService {

    protected File imageData;
    protected String apikey;

    public MsCognitiveService(File data){
        this.imageData = data;
        String envVar = this.getClass().getSimpleName().toUpperCase() + "_KEY";

        apikey = System.getenv(envVar);

        if(apikey==null)
            throw new IllegalArgumentException("Please define environment variable " + envVar);
    }

    public abstract ServiceResult process();
}
