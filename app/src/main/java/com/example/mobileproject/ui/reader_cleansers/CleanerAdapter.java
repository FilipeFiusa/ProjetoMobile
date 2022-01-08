package com.example.mobileproject.ui.reader_cleansers;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.NovelDetailsActivity;
import com.example.mobileproject.NovelsGridAdaptor;
import com.example.mobileproject.R;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.NovelCleaner;
import com.example.mobileproject.model.NovelDetailsMinimum;

import org.jsoup.safety.Cleaner;

import java.util.ArrayList;

public class CleanerAdapter extends RecyclerView.Adapter<CleanerAdapter.CleanerViewHolder> {
    private ArrayList<NovelCleaner> mCleanerList = new ArrayList<>();
    private ArrayList<NovelCleaner> rawCleanerList;
    private ReaderCleaner cleanerController;
    private int type;
    private AppCompatActivity ctx;


    public static class CleanerViewHolder extends RecyclerView.ViewHolder {
        public SwitchCompat mSwitch;
        public TextView mTextView1;
        public ImageButton mEditButton;
        public ImageButton mDeleteButton;
        

        public CleanerViewHolder(@NonNull View itemView) {
            super(itemView);

            mSwitch = itemView.findViewById(R.id.isActive);
            mTextView1 = itemView.findViewById(R.id.cleaner_name);
            mEditButton = itemView.findViewById(R.id.edit_cleaner);
            mDeleteButton = itemView.findViewById(R.id.delete_cleaner);
        }
    }

    public CleanerAdapter(ArrayList<NovelCleaner> cleanerList, AppCompatActivity ctx, int type, ReaderCleaner cleanerController) {
        this.type = type;
        for(NovelCleaner cleaner : cleanerList){
            if(cleaner.getType() == type){
                this.mCleanerList.add(cleaner);
            }
        }
        this.ctx = ctx;
        this.rawCleanerList = cleanerList;
        this.cleanerController = cleanerController;
    }

    public void addCleaner(NovelCleaner cleaner){
        mCleanerList.add(cleaner);
        notifyItemInserted(mCleanerList.size() - 1);
    }

    @NonNull
    @Override
    public CleanerAdapter.CleanerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reader_user_cleaners_select_item, parent, false);
        return new CleanerAdapter.CleanerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CleanerAdapter.CleanerViewHolder holder, int position) {
        NovelCleaner currentItem = mCleanerList.get(position);

        currentItem.position = holder.getAdapterPosition();

        holder.mTextView1.setText(currentItem.getName());
        holder.mSwitch.setChecked(currentItem.isActive());

        holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                currentItem.setActive(b);

                ChangeIsActiveTask changeIsActiveTask = new ChangeIsActiveTask();
                changeIsActiveTask.execute(currentItem);
            }
        });

        holder.mEditButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cleanerController.EditCleaner(currentItem);
            }
        });
        holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RemoveCleanerTask removeCleanerTask = new RemoveCleanerTask();
                removeCleanerTask.execute(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCleanerList.size();
    }

    private class RemoveCleanerTask extends AsyncTask<NovelCleaner, Void, NovelCleaner> {
        @Override
        protected NovelCleaner doInBackground(NovelCleaner... novelCleaners) {
            DBController db = new DBController(ctx);
            NovelCleaner cleaner = novelCleaners[0];

            db.deleteCleaner(cleaner.getCleanerId());

            return cleaner;
        }

        @Override
        protected void onPostExecute(NovelCleaner cleaner) {
            super.onPostExecute(cleaner);
            int removedPosition = 0;

            for (int i = 0; i < mCleanerList.size(); i++) {
                NovelCleaner currentItem = mCleanerList.get(i);
                if(currentItem.getConnectionId() == cleaner.getConnectionId()){
                    removedPosition = i;
                    break;
                }
            }

            mCleanerList.remove(cleaner);
            rawCleanerList.remove(cleaner);
            notifyItemRangeRemoved(removedPosition, 1);
        }
    }

    private class ChangeIsActiveTask extends AsyncTask<NovelCleaner, Void, NovelCleaner> {
        @Override
        protected NovelCleaner doInBackground(NovelCleaner... novelCleaners) {
            DBController db = new DBController(ctx);
            NovelCleaner cleaner = novelCleaners[0];

            db.ChangeIsActiveConnection(cleaner.getConnectionId(), cleaner.isActive());

            return cleaner;
        }
    }
}