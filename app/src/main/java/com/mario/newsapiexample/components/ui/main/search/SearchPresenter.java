package com.mario.newsapiexample.components.ui.main.search;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.mario.newsapiexample.data.model.news.News;
import com.mario.newsapiexample.data.source.news.NewsRepository;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by mario on 14/06/18.
 */

public class SearchPresenter implements SearchContract.Presenter {

    private SearchContract.View view;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    NewsRepository newsRepository;

    @Inject
    SearchPresenter() {
    }

    @Override
    public void onDestroy() {
        view = null;
        compositeDisposable.clear();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void setView(SearchContract.View view) {
        this.view = view;
    }

    @Override
    public void searchNews(String keyword, int page) {
        compositeDisposable.add(newsRepository.getNewsRemoteDataSource().searchNews(keyword, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(News::getArticles)
                .subscribe(articles -> {
                            if (view != null) {
                                if (articles.isEmpty()) {
                                    view.showNoResults();
                                } else {
                                    if (page > 1) {
                                        view.showNextPageResults(articles);
                                    } else {
                                        view.showFirstPageResults(articles);
                                    }
                                }
                            }
                        }, throwable -> {
                            if (((HttpException) throwable).code() == 429) {
                                view.showNoMorePages();
                            } else {
                                view.toast(throwable.getMessage());
                            }
                        }
                ));
    }

    @Override
    public void onSearchCloseClicked() {
        if (view != null) {
            view.replaceFragment();
        }
    }

}
