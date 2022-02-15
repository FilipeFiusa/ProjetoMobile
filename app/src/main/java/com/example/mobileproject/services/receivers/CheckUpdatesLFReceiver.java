package com.example.mobileproject.services.receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.example.mobileproject.ChaptersAdapter;
import com.example.mobileproject.ui.library.LibraryFragment;

public class CheckUpdatesLFReceiver extends ResultReceiver {
    private final LibraryFragment libraryFragment;

    public CheckUpdatesLFReceiver(Handler handler, LibraryFragment libraryFragment) {
        super(handler);
        this.libraryFragment = libraryFragment;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);

        if(libraryFragment != null){
            libraryFragment.checkUpdatesFinished();
        }

    }
}
