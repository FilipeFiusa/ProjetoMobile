package com.example.mobileproject.ui.library;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.mobileproject.model.NovelDetails;

import java.util.List;

public class NovelListDiffCallback extends DiffUtil.Callback {

    private final List<NovelDetails> mOldNovelDetailsList;
    private final List<NovelDetails> mNewNovelDetailsList;

    public NovelListDiffCallback(List<NovelDetails> mOldNovelDetailsList, List<NovelDetails> mNewNovelDetailsList) {
        this.mOldNovelDetailsList = mOldNovelDetailsList;
        this.mNewNovelDetailsList = mNewNovelDetailsList;
    }

    @Override
    public int getOldListSize() {
        return mOldNovelDetailsList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewNovelDetailsList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldNovelDetailsList.get(oldItemPosition).getDb_id() == mNewNovelDetailsList.get(
                newItemPosition).getDb_id();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final NovelDetails oldNovelDetails = mOldNovelDetailsList.get(oldItemPosition);
        final NovelDetails newNovelDetails = mNewNovelDetailsList.get(newItemPosition);

        return oldNovelDetails.getChapterToReadQuantity() == newNovelDetails.getChapterToReadQuantity()
                && oldNovelDetails.getNovelName().equals(newNovelDetails.getNovelName());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}