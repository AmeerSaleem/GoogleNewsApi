package com.example.googlenewsapi.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.googlenewsapi.DatabaseHandler;
import com.example.googlenewsapi.FavoritesActivity;
import com.example.googlenewsapi.Model.Article;
import com.example.googlenewsapi.Model.NewsModel;
import com.example.googlenewsapi.R;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.newsViewHolder> {

    public Context context;
    ArrayList<Article> newsItems;
    onItemClickListener mListener;
    onItemLongClickListener mLongListener;

    public interface onItemClickListener{
        public void onItemClick(int position);
    }

    public interface onItemLongClickListener{
        public void onItemLongClick(int position);
    }

    public void setOnItemClickListener(NewsAdapter.onItemClickListener listener){mListener = listener;}

    public void setOnItemLongClickListener(NewsAdapter.onItemLongClickListener listener){mLongListener = listener;}

    public NewsAdapter(Context context, ArrayList<Article> newsItems) {
        this.context = context;
        this.newsItems = newsItems;
    }

    class newsViewHolder extends RecyclerView.ViewHolder {

        SelectableRoundedImageView image;
        TextView newsTitle;
        TextView newsDate;
        LinearLayout newsCard;

        public newsViewHolder(@NonNull View itemView, final onItemClickListener listener, final onItemLongClickListener longListener) {

            super(itemView);
            image = itemView.findViewById(R.id.newsImage);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsDate = itemView.findViewById(R.id.newsDate);
            newsCard = itemView.findViewById(R.id.newsCard);

            newsCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                    ;
                }
            });

            newsCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (longListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            longListener.onItemLongClick(position);
                        }
                    }
                    return true;
                }
            });

        }
    }

    @NonNull
    @Override
    public newsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_layout_news, parent, false);
        newsViewHolder nvh = new newsViewHolder(v,mListener, mLongListener);
        return nvh;
    }

    @Override
    public void onBindViewHolder(@NonNull final newsViewHolder holder, final int position) {
        final Article nModel = newsItems.get(position);

        Glide
                .with(context)
                .load(nModel.getUrlToImage())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        String temp = nModel.getPublishedAt();
                        int tempIndex = temp.indexOf('T');
                        holder.newsTitle.setText(nModel.getTitle());
                        holder.newsDate.setText(temp.substring(0,tempIndex) + "  " + temp.substring(tempIndex+1,tempIndex+6));
                        holder.newsTitle.setVisibility(View.VISIBLE);
                        holder.newsDate.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(holder.image);

        Animation fadeIn = AnimationUtils.loadAnimation(context,R.anim.fade_in);
        holder.image.startAnimation(fadeIn);
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    public void deleteItem(int position){
        Article recentlyDeleted = newsItems.get(position);
        int recentlyDeletedPosition = position;
        newsItems.remove(position);
        new DatabaseHandler(context).deleteArticle(recentlyDeleted);
        notifyItemRemoved(position);
        Toast.makeText(context, "Article removed from favorites", Toast.LENGTH_SHORT).show();

    }

}