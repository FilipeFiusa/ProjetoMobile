package com.example.mobileproject.ui.reader_normal_view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.SystemClock;
import android.text.Layout;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager2.widget.ViewPager2;

import com.example.mobileproject.R;
import com.example.mobileproject.ReaderActivity;
import com.example.mobileproject.model.Chapter;
import com.example.mobileproject.model.NovelReaderController;
import com.example.mobileproject.model.UserReaderPreferences;
import com.example.mobileproject.model.ViewPageItem;
import com.example.mobileproject.util.FontFactory;

import java.util.ArrayList;

public class PageViewController {
    final private FrameLayout container;
    final private ReaderActivity ctx;

    final private NovelReaderController nrc;

    private Chapter currentChapterOnSeparator;
    private Chapter currentChapter;

    private final FrameLayout loading;

    private final TextView textTempContainer;
    private final TextView textTempContainerWithTitle;
    private final TextView titleTempContainer;

    private ArrayList<ViewPageItem> separatedChapter = new ArrayList<>();
    private ArrayList<ViewPageItem> tempSeparatedChapter = new ArrayList<>();

    private final ArrayList<Chapter> chapters = new ArrayList<>();

    private int addType = 1; // 1- Add on end / 2- Add on start
    private int lastSelectedPage = -1;

    final private ViewPager2 viewPager2;
    private boolean firstLoad = true;
    private boolean currentOnSeparatorInvalid = false;
    private boolean menuChangedPage = false;

    private PageViewAdapter vpAdapter;

    final private UserReaderPreferences userReaderPreferences;

    public PageViewController(ReaderActivity ctx, NovelReaderController nrc, FrameLayout container, UserReaderPreferences userReaderPreferences, FrameLayout loading) {
        this.container = container;
        this.ctx = ctx;
        this.nrc = nrc;
        this.userReaderPreferences = userReaderPreferences;
        this.loading = loading;

        titleTempContainer = (TextView) container.findViewById(R.id.title_temp_container);
        textTempContainer = (TextView) container.findViewById(R.id.text_temp_container);
        textTempContainerWithTitle = (TextView) container.findViewById(R.id.text_temp_container2);

        userReaderPreferences.applyPreferences(null, titleTempContainer, textTempContainerWithTitle);
        userReaderPreferences.applyPreferences(null, null, textTempContainer);

        viewPager2 = container.findViewById(R.id.viewpager);
    }

    public void applyNewUserReaderPreferences(){
        userReaderPreferences.applyPreferences(null, titleTempContainer, textTempContainerWithTitle);
        userReaderPreferences.applyPreferences(null, null, textTempContainer);
    }

    public void onClicked(){
        ctx.toggleReaderMenu();
    }

    private void startSeparator(Chapter c){
        if( (c.getChapterIndex() == null) || (!c.exist() && c.getInvalidType() == 2) ){
            c.addPage();
            tempSeparatedChapter.add(new ViewPageItem(c));
            separatorFinished();

            return;
        }

        ViewTreeObserver vto = textTempContainerWithTitle.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(initialChapterSeparator);

        currentChapterOnSeparator = c;

        initiateFirstPage(c);
    }

    private void initiateFirstPage(Chapter chapter) {
        titleTempContainer.setText(chapter.getChapterContent().getChapterName());
        textTempContainerWithTitle.setText(chapter.getChapterContent().getChapterContent());
    }

    private void createNextPage(String chapter){
        textTempContainer.setText(chapter);
    }

    private void separatorFinished(){
        int itemsAdded = tempSeparatedChapter.size();
        int position = viewPager2.getCurrentItem();

        tempSeparatedChapter.get(tempSeparatedChapter.size() - 1).setLastPage(true);

        if(addType == 1){
            separatedChapter.addAll(tempSeparatedChapter);
        }else if (addType == 2){
            separatedChapter.addAll(0, tempSeparatedChapter);
        }

        tempSeparatedChapter = new ArrayList<>();

        if(chapters.size() > 0){
            startSeparator(chapters.remove(0));
            return;
        }

        if(firstLoad){
            vpAdapter = new PageViewAdapter(separatedChapter, ctx, this, userReaderPreferences);
            viewPager2.setAdapter(vpAdapter);
            int page = nrc.getCurrentPreviousChapter().getTotalPages();
            viewPager2.setCurrentItem(page, false);
            firstLoad = false;

            currentChapter = nrc.getCurrentChapter();

            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    Chapter chapter = separatedChapter.get(position).getChapter();

                    if(chapter != null  && chapter.getChapterIndex() != null &&  !currentChapter.equals(chapter)){
                        System.out.println("---");
                        ctx.chapterNameBottom.setText(chapter.getChapterIndex().getChapterName());

                        if(chapter.equals(nrc.getCurrentPreviousChapter())){
                            addType = 2;
                            ctx.getPreviousChapter();
                        }else if(chapter.equals(nrc.getCurrentNextChapter())){
                            addType = 1;
                            ctx.getNextChapter();
                        }

                        currentChapter = chapter;
                    }

                    if(separatedChapter.get(position).getType() == 3 || separatedChapter.get(position).getType() == 4){
                        if (lastSelectedPage < position){
                            ctx.lastChapterFinished();
                        }
                    }

                    if (lastSelectedPage == -1){
                        lastSelectedPage = position;
                    }
                }
            });

            viewPager2.setClipToPadding(false);
            viewPager2.setClipChildren(false);
            viewPager2.setOffscreenPageLimit(2);
            viewPager2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        }else{
            if(addType == 1){
                //vpAdapter.notifyItemInserted(itemsAdded);
                vpAdapter.notifyDataSetChanged();

            }else if (addType == 2){
                position += nrc.getCurrentPreviousChapter().getTotalPages();

                if (currentOnSeparatorInvalid){
                    //vpAdapter.notifyItemRangeInserted(0, nrc.getCurrentPreviousChapter().getTotalPages());
                    currentOnSeparatorInvalid = false;

                    int finalPosition = position;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SystemClock.sleep(500);
                            viewPager2.setCurrentItem(finalPosition, false);
                        }
                    });
                }else {
                    //vpAdapter.notifyItemRangeInserted(0, nrc.getCurrentPreviousChapter().getTotalPages());
                    if(!menuChangedPage){
                        viewPager2.setCurrentItem(position, false);
                    }
                }
            }

            if(menuChangedPage){
                moveToCurrentChapterFromMenu();
            }
        }

        loading.setVisibility(View.GONE);
    }

    public void addChapter(int direction){
        addType = direction;
        tempSeparatedChapter = new ArrayList<>();
        
        if(addType == 1){
            if (chapterAlreadyExist(nrc.getCurrentNextChapter())){
                if(menuChangedPage){
                    moveToCurrentChapterFromMenu();
                }
                return;
            }

            chapters.add(nrc.getCurrentNextChapter());
            startSeparator(chapters.remove(0));
        }else if(addType == 2){
            if (chapterAlreadyExist(nrc.getCurrentPreviousChapter())){
                if(menuChangedPage){
                    moveToCurrentChapterFromMenu();
                }
                return;
            }

            chapters.add(nrc.getCurrentPreviousChapter());
            startSeparator(chapters.remove(0));
        }
    }

    public void moveToCurrentChapterFromMenu(){
        int totalPages = 0;
        menuChangedPage = false;

        for (ViewPageItem item : separatedChapter){
            if(item.getChapter().equals(nrc.getCurrentChapter())){
                break;
            }
            totalPages++;
        }

        viewPager2.setCurrentItem(totalPages, false);
    }

    public void addInvalidPage(int direction, int invalidType){
        currentOnSeparatorInvalid = true;

        addType = direction;
        Chapter c = new Chapter(invalidType);

        if(chapterAlreadyExist(c)){
            return;
        }

        c.addPage();
        tempSeparatedChapter.add(new ViewPageItem(c));
        separatorFinished();

    }

    public void addInvalidPage(int direction){
        addInvalidPage(direction, 1);
    }

    public void updateChapters(){
        firstLoad = true;
        separatedChapter = new ArrayList<>();
        tempSeparatedChapter = new ArrayList<>();

        chapters.add(nrc.getCurrentPreviousChapter());
        chapters.add(nrc.getCurrentChapter());
        chapters.add(nrc.getCurrentNextChapter());

        startSeparator(chapters.remove(0));
    }

    private boolean chapterAlreadyExist(Chapter c){
        System.out.println(separatedChapter.get(separatedChapter.size() - 1).getType());
        System.out.println(separatedChapter.get(0).getType());

        if(!c.exist() && (separatedChapter.get(separatedChapter.size() - 1).getType() == 3 ||
                separatedChapter.get(0).getType() == 3)){
            Toast.makeText(ctx, "No chapter", Toast.LENGTH_SHORT).show();
            return true;
        }


        for(ViewPageItem item : separatedChapter){
            if(item.getChapter().equals(c)){
                return true;
            }
        }

        return false;
    }

    public void menuChangePage(){
        menuChangedPage = true;
    }
    
    private ViewTreeObserver.OnGlobalLayoutListener initialChapterSeparator = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            try {
                Layout l = textTempContainerWithTitle.getLayout();

                int height = textTempContainerWithTitle.getHeight();
                int scrollY = textTempContainerWithTitle.getScrollY();

                /*
                 * Key part
                 */

                int lineCount = l.getLineForVertical(height + scrollY);

                int start = l.getLineStart(0);
                int end = l.getLineEnd(lineCount);

                /*
                 * Cut string
                 */


                String cutString = textTempContainerWithTitle.getText().toString().substring(start, end);
                String tempText = textTempContainerWithTitle.getText().toString().substring(start, end);
                for(int i = end - 1; i > 0; i--){
                    char c = tempText.charAt(i);
                    if(tempText.charAt(i) == ' ' || tempText.charAt(i) == '\n'){
                        end = i;
                        break;
                    }
                }

                String restOfString;
                try {
                    restOfString = textTempContainerWithTitle.getText().toString().substring(end + 1, textTempContainerWithTitle.getText().length());
                }catch (StringIndexOutOfBoundsException e){
                    System.out.println("CABOU \n\n");

                    textTempContainerWithTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    //finalizou();
                    return;
                }
                textTempContainerWithTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                currentChapterOnSeparator.addPage();
                tempSeparatedChapter.add(new ViewPageItem(currentChapterOnSeparator.getChapterContent().getChapterName(), cutString, currentChapterOnSeparator));

                ViewTreeObserver vto = textTempContainer.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(chapterSeparator);

                createNextPage(restOfString);
            }catch (NullPointerException e){
                System.out.println("Finalizou");
                e.printStackTrace();
                //finalizou();
            }
        }
    };

    private ViewTreeObserver.OnGlobalLayoutListener chapterSeparator = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            try {
                Layout l = textTempContainer.getLayout();

                int height = textTempContainer.getHeight();
                int scrollY = textTempContainer.getScrollY();

                /*
                 * Key part
                 */

                int lineCount = l.getLineForVertical(height + scrollY);

                int start = l.getLineStart(0);
                int end = l.getLineEnd(lineCount);

                /*
                 * Cut string
                 */

                String s = textTempContainer.getText().toString().substring(start, end);
                String tempText = textTempContainer.getText().toString().substring(start, end);
                for(int i = end - 1; i > 0; i--){
                    char c = tempText.charAt(i);
                    if(tempText.charAt(i) == ' ' || tempText.charAt(i) == '\n'){
                        end = i;
                        break;
                    }
                }

                String restOfString;
                try {
                    restOfString = textTempContainer.getText().toString().substring(end + 1, textTempContainer.getText().length());
                }catch (StringIndexOutOfBoundsException e){
                    System.out.println("CABOU \n\n");

                    textTempContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    separatorFinished();
                    return;
                }

                currentChapterOnSeparator.addPage();
                tempSeparatedChapter.add(new ViewPageItem(s, currentChapterOnSeparator));

                createNextPage(restOfString);
            }catch (NullPointerException e){
                separatorFinished();
            }
        }
    };

}
