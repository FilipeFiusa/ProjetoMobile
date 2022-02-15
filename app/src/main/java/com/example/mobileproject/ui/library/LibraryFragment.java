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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mobileproject.CreateSourceWebViewActivity;
import com.example.mobileproject.MainActivity;
import com.example.mobileproject.NovelDetailsActivity;
import com.example.mobileproject.R;
import com.example.mobileproject.ReaderActivity;
import com.example.mobileproject.db.DBController;
import com.example.mobileproject.model.DownloadReceiver;
import com.example.mobileproject.model.DownloaderService;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.model.parser.Parser;
import com.example.mobileproject.model.parser.ParserFactory;
import com.example.mobileproject.services.CheckUpdateService;
import com.example.mobileproject.model.NovelDetails;
import com.example.mobileproject.services.CheckNovelUpdatesService;
import com.example.mobileproject.services.receivers.CheckUpdatesLFReceiver;

import java.net.MalformedURLException;
import java.net.URL;
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

    private URL currentUrl;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.library_fragment, container, false);
        ctx = (MainActivity) requireActivity();

/*        Button mButton1 = root.findViewById(R.id.sort_novels);
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
        });*/


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

        SetUpMenu();

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

    public void SetUpMenu(){
        FrameLayout popup = root.findViewById(R.id.pop_up_menu);
        LinearLayout selectTypeMenu = root.findViewById(R.id.select_type_menu);
        LinearLayout setNovelLinkMenu = root.findViewById(R.id.set_novel_link_menu);
        LinearLayout openWebViewMenu = root.findViewById(R.id.open_web_view_menu);

        ImageButton addNovel = root.findViewById(R.id.add_novel);
        addNovel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.setVisibility(View.VISIBLE);
                selectTypeMenu.setVisibility(View.VISIBLE);
            }
        });

        Button epubOption = root.findViewById(R.id.epub_option);
        epubOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ctx, "Não disponivel ainda", Toast.LENGTH_SHORT).show();
            }
        });

        Button novelLinkOption = root.findViewById(R.id.novel_link_option);
        novelLinkOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTypeMenu.setVisibility(View.GONE);
                setNovelLinkMenu.setVisibility(View.VISIBLE);
            }
        });

        Button cancelOption = root.findViewById(R.id.cancel_option);
        cancelOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTypeMenu.setVisibility(View.GONE);
                setNovelLinkMenu.setVisibility(View.GONE);
                popup.setVisibility(View.GONE);
            }
        });

        Button cancelOption2 = root.findViewById(R.id.cancel_option_2);
        cancelOption2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTypeMenu.setVisibility(View.GONE);
                setNovelLinkMenu.setVisibility(View.GONE);
                popup.setVisibility(View.GONE);
            }
        });

        Button searchOption = root.findViewById(R.id.search_option);
        searchOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText searchInput = root.findViewById(R.id.link_input);
                String searchLink = searchInput.getText().toString();

                try {
                    currentUrl = new URL(searchLink);
                    Parser currentParser = ParserFactory.checkIfSourceExistsWithLink(ctx, currentUrl);

                    if (currentParser != null){
                        System.out.println(currentParser.getSourceName());
                        System.out.println(currentUrl.getPath());

                        Intent intent = new Intent(ctx, NovelDetailsActivity.class);

                        intent.putExtra("novelLink", currentUrl.getPath());
                        intent.putExtra("novelName", "");
                        intent.putExtra("novelSource", currentParser.getSourceName());

                        ctx.startActivityForResult(intent, 1);

                        searchInput.getText().clear();
                    }else{
                        setNovelLinkMenu.setVisibility(View.GONE);
                        openWebViewMenu.setVisibility(View.VISIBLE);

                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        });

        Button cancelOption3 = root.findViewById(R.id.cancel_option_3);
        cancelOption3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTypeMenu.setVisibility(View.GONE);
                setNovelLinkMenu.setVisibility(View.GONE);
                popup.setVisibility(View.GONE);
                openWebViewMenu.setVisibility(View.GONE);
            }
        });

        Button openWebView = root.findViewById(R.id.open_web_view_option);
        openWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (true) return;

                Intent intent = new Intent(ctx, CreateSourceWebViewActivity.class);
                intent.putExtra("currentUrl", currentUrl);
                ctx.startActivityForResult(intent, 1);

                selectTypeMenu.setVisibility(View.GONE);
                setNovelLinkMenu.setVisibility(View.GONE);
                popup.setVisibility(View.GONE);
                openWebViewMenu.setVisibility(View.GONE);
            }
        });

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
}