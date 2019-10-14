package com.example.googlenewsapi;

import com.example.googlenewsapi.Model.NewsModel;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleNewsService {

    @GET("top-headlines")
    Observable<NewsModel> getNewsModel(
            @Query("sources") String google_news,
            @Query("apiKey") String api_key
            );

}
