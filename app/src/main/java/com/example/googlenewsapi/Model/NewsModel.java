
package com.example.googlenewsapi.Model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//POJO class for Retrofit/Gson Functionality
public class NewsModel {

    @SerializedName("articles")
    @Expose
    private List<Article> articles = null;

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

}
