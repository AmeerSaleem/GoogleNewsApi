package com.example.googlenewsapi;

import com.example.googlenewsapi.Adapter.NewsAdapter;
import com.example.googlenewsapi.Model.NewsModel;

import javax.inject.Inject;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient{

    private static RetrofitClient instance;
    private static final String BASE_URL = "https://newsapi.org/v2/";
    GoogleNewsService service;

    @Inject
    public RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        service = retrofit.create(GoogleNewsService.class);
    }

    public static RetrofitClient getInstance(){
        if(instance == null){
            instance = new RetrofitClient();
        }
        return instance;
    }

    public Observable<NewsModel> getNewsModels(String sources, String key){ return service.getNewsModel(sources,key);}

}