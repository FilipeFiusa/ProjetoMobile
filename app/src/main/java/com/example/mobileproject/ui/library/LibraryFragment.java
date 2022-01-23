package com.example.mobileproject.ui.library;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static com.example.mobileproject.App.CHECK_UPDATES_SERVICE_ID;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mobileproject.MainActivity;
import com.example.mobileproject.R;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.DownloaderService;
import com.example.mobileproject.services.CheckUpdateService;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.services.CheckNovelUpdatesService;
import com.example.mobileproject.services.receivers.CheckUpdatesLFReceiver;

import java.util.ArrayList;

public class LibraryFragment extends Fragment {
    public MainActivity ctx;

    private CheckUpdatesLFReceiver updatesReceiver;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private NovelsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private View root;

    private ArrayList<NovelDetails> selectedNovelsReference;

    private Thread checkUpdatesThread;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.library_fragment, container, false);
        ctx = (MainActivity) requireActivity();

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
        mAdapter = new NovelsAdapter(new ArrayList<>(), ctx, this);
        mRecyclerView.addItemDecoration(new NovelsLayoutDecoration(5, ctx));

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        NovelsOnLibrary novelsOnLibrary = new NovelsOnLibrary();
        novelsOnLibrary.execute();

        updatesReceiver = new CheckUpdatesLFReceiver(new Handler(), this);

        checkUpdatesThread = new Thread(new CheckForUpdateService());
        checkUpdatesThread.start();

        return root;
    }

    private void scheduleJob(){
        JobScheduler scheduler = (JobScheduler) ctx.getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(CHECK_UPDATES_SERVICE_ID);

        ComponentName componentName = new ComponentName(ctx, CheckNovelUpdatesService.class);
        JobInfo info = new JobInfo.Builder(CHECK_UPDATES_SERVICE_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)
                .setPeriodic(3 * 60 * 60 * 1000)
                .build();

        scheduler.schedule(info);
    }

    public void UpdateNovels(){
        NovelsOnLibrary novelsOnLibrary = new NovelsOnLibrary();
        novelsOnLibrary.execute();
    }

    public void openMenu(ArrayList<NovelDetails> reference){
        selectedNovelsReference = reference;

        showSelectMenu();

        ImageButton cancelSelection = (ImageButton) root.findViewById(R.id.cancel_selection);
        cancelSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSelectMenu();

                mAdapter.resetSelectedList();
                selectedNovelsReference = null;
            }
        });

        ImageButton selectAll = (ImageButton) root.findViewById(R.id.select_all);
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.selectAll();
            }
        });

        ImageButton invertSelection = (ImageButton) root.findViewById(R.id.invert_selection);
        invertSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.invertSelection();
            }
        });

        ImageButton bookmarkSelected = (ImageButton) root.findViewById(R.id.bookmark_selected);
        bookmarkSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ctx, "Não Disponivel ainda", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton readSelected = (ImageButton) root.findViewById(R.id.read_selected);
        readSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetNovelsAsReadiedTask readiedTask = new SetNovelsAsReadiedTask();
                readiedTask.execute(new ArrayList<>(selectedNovelsReference));

                mAdapter.setAsReadied();

                hideSelectMenu();
                selectedNovelsReference = null;
            }
        });

        ImageButton downloadSelected = (ImageButton) root.findViewById(R.id.download_selected);
        downloadSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ctx, "Não Disponivel ainda", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton deleteSelected = (ImageButton) root.findViewById(R.id.delete_selected);
        deleteSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteNovelTask deleteTask = new DeleteNovelTask();
                deleteTask.execute(new ArrayList<>(selectedNovelsReference));

                mAdapter.removeSelected();

                hideSelectMenu();
                selectedNovelsReference = null;
            }
        });

        update();
    }

    public void update(){
        if(selectedNovelsReference != null && selectedNovelsReference.isEmpty()){
            hideSelectMenu();
        }
    }

    private void showSelectMenu(){
        ctx.hideBottomNav();

        View topView1 = (LinearLayout) root.findViewById(R.id.normal_menu);
        View topView2 = (LinearLayout) root.findViewById(R.id.select_menu);
        topView1.setVisibility(View.GONE);
        topView2.setVisibility(View.VISIBLE);

        FrameLayout mMenu = (FrameLayout) root.findViewById(R.id.bottom_select_menu);
        mMenu.setVisibility(View.VISIBLE);
    }

    private void hideSelectMenu(){
        View topView1 = (LinearLayout) root.findViewById(R.id.normal_menu);
        View topView2 = (LinearLayout) root.findViewById(R.id.select_menu);

        topView1.setVisibility(View.VISIBLE);
        topView2.setVisibility(View.GONE);

        FrameLayout mMenu = (FrameLayout) root.findViewById(R.id.bottom_select_menu);
        mMenu.setVisibility(View.GONE);

        ctx.showBottomNav();
    }

    public void checkUpdatesFinished(){
        NovelsOnLibrary novelsOnLibrary = new NovelsOnLibrary();
        novelsOnLibrary.execute();
    }

    private class NovelsOnLibrary extends AsyncTask<Void, Void, ArrayList<NovelDetails>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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

            mAdapter.updateNovelsList(novelDetailsArr);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class DeleteNovelTask extends AsyncTask<ArrayList<NovelDetails>, Void, Void> {

        @Override
        protected Void doInBackground(ArrayList<NovelDetails>... arrayLists) {
            DBController db = new DBController(ctx);
            ArrayList<NovelDetails> arrayList = new ArrayList<>(arrayLists[0]);

            for(NovelDetails n : arrayList){
                db.deleteNovel(n.getNovelName(), n.getSource());
            }

            return null;
        }

    }

    private class SetNovelsAsReadiedTask extends  AsyncTask<ArrayList<NovelDetails>, Void, Void> {

        @Override
        protected Void doInBackground(ArrayList<NovelDetails>... arrayLists) {
            DBController db = new DBController(ctx);
            ArrayList<NovelDetails> arrayList = new ArrayList<>(arrayLists[0]);

            for(NovelDetails n : arrayList){
                db.setChaptersReadied(n.getNovelName(), n.getSource(), "");
            }

            return null;
        }
    }

    private class CheckForUpdateService implements Runnable {

        @Override
        public void run() {
            boolean isServiceRunning = false;
            while (!isServiceRunning){
                isServiceRunning = isMyServiceRunning(CheckUpdateService.class);

                if(isServiceRunning){
                    Intent serviceIntent = new Intent(ctx, CheckUpdateService.class);
                    serviceIntent.putExtra("receiver", (Parcelable) updatesReceiver);
                    ctx.startService(serviceIntent);
                }

                SystemClock.sleep(1000);
            }
        }

        private boolean isMyServiceRunning(Class<?> serviceClass) {
            ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
            return false;
        }
    }
}