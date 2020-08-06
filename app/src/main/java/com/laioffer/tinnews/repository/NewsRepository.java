package com.laioffer.tinnews.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.LinearLayout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.laioffer.tinnews.TinNewsApplication;
import com.laioffer.tinnews.database.TinNewsDatabase;
import com.laioffer.tinnews.model.Article;
import com.laioffer.tinnews.model.NewsResponse;
import com.laioffer.tinnews.network.NewsApi;
import com.laioffer.tinnews.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsRepository {

    private final NewsApi newsApi;
    private final TinNewsDatabase database;

    public NewsRepository(Context context) {
        this.newsApi = RetrofitClient.newInstance(context).create(NewsApi.class);
        this.database = ((TinNewsApplication) context.getApplicationContext()).getDatabase();
    }

    public LiveData<NewsResponse> getTopHeadlines(String country) {
        MutableLiveData<NewsResponse> topHeadLinesLiveData = new MutableLiveData<>();
        newsApi.getTopHeadlines(country).enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful()) {
                    topHeadLinesLiveData.setValue(response.body());
                } else {
                    topHeadLinesLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                topHeadLinesLiveData.setValue(null);
            }
        });
        return topHeadLinesLiveData;
    }

    public LiveData<NewsResponse> searchNews(String query) {
        MutableLiveData<NewsResponse> everythingLiveData = new MutableLiveData<>();
        newsApi.getEverything(query, 40).enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful()) {
                    everythingLiveData.setValue(response.body());
                } else {
                    everythingLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                everythingLiveData.setValue(null);
            }
        });
        return everythingLiveData;
    }

    public LiveData<Boolean> favoriteArticles(Article article) {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();

        new FavoriteAsyncTask(database, resultLiveData).execute(article);
        return resultLiveData;
    }

    private static class FavoriteAsyncTask extends AsyncTask<Article, Void, Boolean> {
        private final TinNewsDatabase database;
        private final MutableLiveData<Boolean> liveData;

        private FavoriteAsyncTask(TinNewsDatabase database, MutableLiveData<Boolean> liveData) {
            this.database = database;
            this.liveData = liveData;
        }

        @Override
        protected Boolean doInBackground(Article... articles) {
            Article article = articles[0];
            try {
                database.articleDao().saveArticle(article);
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            liveData.setValue(success);
        }
    }

    public LiveData<List<Article>> getAllSavedArticles() {
        return database.articleDao().getAllArticles();
    }

    public void deleteSavedArticle(Article article) {
        AsyncTask.execute(() -> database.articleDao().deleteArticle(article));
    }
}
