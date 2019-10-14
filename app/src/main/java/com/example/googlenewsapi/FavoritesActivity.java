package com.example.googlenewsapi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.googlenewsapi.Adapter.NewsAdapter;
import com.example.googlenewsapi.Model.Article;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/*Favorites activity of project containing recyclerview displaying favorites articles.
 *Similar to the main screen, clicking a news article brings up a description with further links to
 *the web page.
 */
public class FavoritesActivity extends AppCompatActivity {

    ArrayList<Article> favoritesList;  //list for holding favorite Items
    DatabaseHandler dbHandler;  //handler for linking to SQLite database
    private static final String TAG = "FavoritesActivity";
    CompositeDisposable compositeDisposable;
    RecyclerView rcvFavorites;  //view displaying favorites
    NewsAdapter favoritesAdapter;  // adapter for recyclerview
    LinearLayoutManager favoritesManager;
    ImageView backbutton;
    TextView favoriteNotice;
    SelectableRoundedImageView dialogImage;
    TextView dialogTitle, dialogDate, dialogDescription, dialogUrlLink;
    AlertDialog alertDialog;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right_back, R.anim.right_to_left_back);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        //initialize views
        initViews();
        //initialize back button listener
        initListeners();

        //Handle repsonse of SQLite database on seperate thread.
        compositeDisposable.add(Observable.just(getFavoritesList())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Article>>() {

                    @Override
                    public void onNext(List<Article> list) {
                        if (list.size() == 0) {
                            favoriteNotice.setVisibility(View.VISIBLE);
                            rcvFavorites.setVisibility(View.GONE);
                            return;
                        }
                        favoritesAdapter = new NewsAdapter(getApplicationContext(), ((ArrayList<Article>) list));
                        favoritesManager = new LinearLayoutManager(getApplicationContext());
                        rcvFavorites.setLayoutManager(favoritesManager);
                        rcvFavorites.setAdapter(favoritesAdapter);
                        initializeClick();
                        favoriteNotice.setVisibility(View.GONE);
                        rcvFavorites.setVisibility(View.VISIBLE);
                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new Swipe2DeleteCallback(favoritesAdapter));
                        itemTouchHelper.attachToRecyclerView(rcvFavorites);
                    }

                    //
                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: ");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                })
        );
    }

    private void initListeners() {
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void initViews() {
        backbutton = findViewById(R.id.backButton);
        rcvFavorites = findViewById(R.id.recyclerFavorites);
        dbHandler = new DatabaseHandler(this);
        favoritesList = ((ArrayList<Article>) dbHandler.getAllArticles());
        favoriteNotice = findViewById(R.id.emptyFavoriteNotice);
        compositeDisposable = new CompositeDisposable();
    }

    private void initializeClick() {
        favoritesAdapter.setOnItemClickListener(new NewsAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                createDialog(position);
            }
        });
    }

    private void createDialog(final int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FavoritesActivity.this);
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
                String page_url = favoritesList.get(position).getUrl();
                in.putExtra("url", page_url);
                startActivity(in);
                overridePendingTransition(R.anim.slide_left_forward, R.anim.slide_right_forward);
            }
        });
    }

    private void setDialogDetails(int position) {

        Glide
                .with(this)
                .load(favoritesList.get(position).getUrlToImage())
                .into(dialogImage);
        String temp = favoritesList.get(position).getPublishedAt();
        int tempIndex = temp.indexOf('T');
        dialogTitle.setText(favoritesList.get(position).getTitle());
        dialogDate.setText(temp.substring(0, tempIndex) + "  " + temp.substring(tempIndex + 1, tempIndex + 6));
        dialogDescription.setText(favoritesList.get(position).getDescription());

    }

    private void bindDialogViews(View dialog_view) {

        dialogImage = dialog_view.findViewById(R.id.dialogImage);
        dialogTitle = dialog_view.findViewById(R.id.dialogTitle);
        dialogDate = dialog_view.findViewById(R.id.dialogDate);
        dialogDescription = dialog_view.findViewById(R.id.dialogDescription);
        dialogUrlLink = dialog_view.findViewById(R.id.dialogLink2Url);

    }

    List<Article> getFavoritesList() {
        return dbHandler.getAllArticles();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}