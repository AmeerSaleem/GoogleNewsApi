package com.example.googlenewsapi;

import dagger.Component;

@Component
public interface NewsComponent {

    void inject(MainActivity mainActivity);

}
