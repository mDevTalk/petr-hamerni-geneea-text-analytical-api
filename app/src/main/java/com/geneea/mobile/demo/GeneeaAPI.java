package com.geneea.mobile.demo;

import com.geneea.mobile.demo.model.EntitiesResponse;
import com.geneea.mobile.demo.model.Request;
import com.geneea.mobile.demo.model.SentimentResponse;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.POST;

/**
 * An interface for Geneea NLP REST API version S2.
 * It's use by the Retrofit library.
 */
public interface GeneeaAPI {

    @POST("/s2/entities")
    Call<EntitiesResponse> findEntities(
            @Header("Authorization") String authorization,
            @Body Request request
    );

    @POST("/s2/sentiment")
    Call<SentimentResponse> detectSentiment(
            @Header("Authorization") String authorization,
            @Body Request request
    );

}
