package com.example.tribecovidmonitor.ui.CollectData;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CollectDataViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CollectDataViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}