package com.example.mobileproject.ui.library;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static com.example.mobileproject.App.CHECK_UPDATES_SERVICE_ID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mobileproject.ChaptersAdapter;
import com.example.mobileproject.R;
import com.example.mobileproject.VisitSourceActivity;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.CheckUpdateService;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.services.CheckNovelUpdatesService;

import java.util.ArrayList;

public class LibraryFragment extends Fragment {
    View root;
    AppCompatActivity ctx;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private NovelsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.library_fragment, container, false);
        ctx = (AppCompatActivity) this.requireActivity();

        Button mButton1 = root.findViewById(R.id.sort_novels);
        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(true){
                    return;
                }
                System.out.println("Apertou");
                Intent serviceIntent = new Intent(ctx, CheckUpdateService.class);
                ctx.startService(serviceIntent);
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.librarySwipeRefresh);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scheduleJob();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mRecyclerView = root.findViewById(R.id.library_recycle_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new GridLayoutManager(ctx, 2);
        mAdapter = new NovelsAdapter(new ArrayList<>(), ctx);
        mRecyclerView.addItemDecoration(new NovelsLayoutDecoration(10, ctx));

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        NovelsOnLibrary novelsOnLibrary = new NovelsOnLibrary();
        novelsOnLibrary.execute();

        this.root = root;

        return root;
    }

    private void scheduleJob(){
        ComponentName componentName = new ComponentName(ctx, CheckNovelUpdatesService.class);
        JobInfo info = new JobInfo.Builder(CHECK_UPDATES_SERVICE_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)
                .setPeriodic(3 * 60 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) ctx.getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.schedule(info);
    }

    public void UpdateNovels(){
        NovelsOnLibrary novelsOnLibrary = new NovelsOnLibrary();
        novelsOnLibrary.execute();
    }

    public class NovelsOnLibrary extends AsyncTask<Void, Void, ArrayList<NovelDetails>> {
        private LinearLayout isLoading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //isLoading = (LinearLayout) findViewById(R.id.isLoading);
        }

        @Override
        protected ArrayList<NovelDetails> doInBackground(Void... voids) {
            ArrayList<NovelDetails> novelDetailsArr;

            DBController db = new DBController(requireActivity().getApplicationContext());
            novelDetailsArr = db.selectAllNovels();

            return novelDetailsArr;
        }

        @Override
        protected void onPostExecute(ArrayList<NovelDetails> novelDetailsArr) {
            super.onPostExecute(novelDetailsArr);
            //isLoading.setVisibility(View.INVISIBLE);
            //isLoading.removeAllViews();

            mAdapter.updateNovelsList(novelDetailsArr);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

}