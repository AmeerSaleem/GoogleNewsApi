package com.example.googlenewsapi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.googlenewsapi.Adapter.NewsAdapter;
import com.example.googlenewsapi.Model.Article;
import com.example.googlenewsapi.Model.NewsModel;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/*Main screen of project containing recyclerview displaying news articles.
  *The web service used here is a google news api.
  *Clicking a news article brings up a description with further links to
  *the web page. Long clicking allows for story to be stored in favorites.
 */

public class MainActivity extends AppCompatActivity {

    RecyclerView rcvNews;
    NewsAdapter adapter;
    @Inject
    RetrofitClient client;
    ArrayList<Article> articleList;
    GoogleNewsService service;
    CompositeDisposable compositeDisposable;
    LinearLayoutManager manager;
    AlertDialog alertDialog;
    SelectableRoundedImageView dialogImage;
    TextView dialogTitle, dialogDate, dialogDescription, dialogUrlLink;
    ImageView refreshButton;
    ImageView favoritesButton;
    DatabaseHandler dbHandler;

    private static final String GOOGLE_NEWS_KEY = "7175e376b39e42fb89483b776e7af64a";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initializze views
        initView();
        //initialize listeners of icons i.e. refresh and favorites buttons
        initIconListeners();
        //fetch news data
        loadNewsData();

    }

    private void initIconListeners() {
        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_left_forward,R.anim.slide_right_forward);
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animation = new RotateAnimation(0.0f, 360.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                        0.5f);
                animation.setRepeatCount(-1);
                animation.setDuration(2000);
                refreshButton.startAnimation(animation);
                loadNewsData();
            }
        });
    }

    private void initView() {
        rcvNews = findViewById(R.id.recyclerNews);
        favoritesButton = findViewById(R.id.favoriteList);
        refreshButton = findViewById(R.id.image_refresh);
        dbHandler = new DatabaseHandler(this);
        rcvNews.setHasFixedSize(true);
        compositeDisposable = new CompositeDisposable();
        NewsComponent newsComponent = DaggerNewsComponent.create();
        newsComponent.inject(this);
    }

    private void loadNewsData() {
        //API response handled on seperate thread
        Observable<NewsModel> newsObservable = client.getNewsModels("google-news",GOOGLE_NEWS_KEY);
        compositeDisposable.add(newsObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<NewsModel>() {

                    @Override
                    public void onNext(NewsModel newsModel) {
                        articleList = (ArrayList<Article>) newsModel.getArticles();
                        adapter = new NewsAdapter(getApplicationContext(),articleList);
                        manager = new LinearLayoutManager(getApplicationContext());
                        rcvNews.setAdapter(adapter);
                        rcvNews.setLayoutManager(manager);
                        refreshButton.clearAnimation();
                        Toast.makeText(MainActivity.this, "News Items Loaded", Toast.LENGTH_SHORT).show();
                        initializeClick();
                        initializeLongClick();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: ");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                }));
    }

    //Long click on item allows for storage in favorites
    private void initializeLongClick() {
        adapter.setOnItemLongClickListener(new NewsAdapter.onItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                dbHandler.addArticle(articleList.get(position));
                Toast.makeText(MainActivity.this, "Article added to favorites", Toast.LENGTH_SHORT).show();
                Animation expand = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.expand_out);
                favoritesButton.startAnimation(expand);
            }
        });
    }

    //click displays dialog for article presenting further detail including URL link to article
    private void initializeClick() {
        adapter.setOnItemClickListener(new NewsAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                createDialog(position);
            }
        });
    }

    private void createDialog(final int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialog = dialogBuilder.create();
        View dialog_view = getLayoutInflater().inflate(R.layout.dialog_view, null);
        alertDialog.setView(dialog_view);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        bindDialogViews(dialog_view);
        alertDialog.show();
        setDialogDetails(position);
        dialogUrlLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(getApplicationContext(), WebActivity.class);
                String page_url = articleList.get(position).getUrl();
                in.putExtra("url", page_url);
                startActivity(in);
                overridePendingTransition(R.anim.slide_left_forward,R.anim.slide_right_forward);
            }
        });
    }

    private void setDialogDetails(int position) {

        Glide
                .with(this)
                .load(articleList.get(position).getUrlToImage())
                .into(dialogImage);

        String temp = articleList.get(position).getPublishedAt();
        int tempIndex = temp.indexOf('T');
        dialogTitle.setText(articleList.get(position).getTitle());
        dialogDate.setText(temp.substring(0,tempIndex) + "  " + temp.substring(tempIndex+1,tempIndex+6));
        dialogDescription.setText(articleList.get(position).getDescription());

    }

    private void bindDialogViews(View dialog_view) {

        dialogImage = dialog_view.findViewById(R.id.dialogImage);
        dialogTitle = dialog_view.findViewById(R.id.dialogTitle);
        dialogDate = dialog_view.findViewById(R.id.dialogDate);
        dialogDescription = dialog_view.findViewById(R.id.dialogDescription);
        dialogUrlLink = dialog_view.findViewById(R.id.dialogLink2Url);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }
}