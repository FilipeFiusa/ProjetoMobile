package com.example.mobileproject.model.epub;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.example.mobileproject.model.ChapterIndex;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.*;

public class EpubParser {
    private Context ctx;

    private BookDetails bookDetails;
    private ArrayList<File> htmlList = new ArrayList<>();
    private ArrayList<EpubChapter> chapterList = new ArrayList<>();

    public EpubParser(Context ctx){
        this.ctx = ctx;
    }

    public BookDetails parse(Uri epubUri) throws IOException{
        File epubFile = loadFile(epubUri);
        String unzippedPath = unzipEpub(epubFile);
        File tempPath = new File(unzippedPath);

        listFilesForFolder(tempPath);

        loadChapters();

        loadBookCover();

        saveImages(tempPath.getPath());

        boolean epubFileDeleted = epubFile.delete();
        if(!epubFileDeleted){
            System.out.println("Ocorreu algum erro ao deletar epubFile");
        }

        FileUtils.deleteDirectory(tempPath);

        bookDetails.setChapterList(chapterList);

        return bookDetails;
    }

    public File loadFile(Uri uri) throws IOException {
        File epubFile = new File(ctx.getFilesDir().getPath() + "/temp.epub");

        InputStream in =  ctx.getContentResolver().openInputStream(uri);
        OutputStream out = new FileOutputStream(epubFile);
        byte[] buf = new byte[1024];
        int len;
        while((len=in.read(buf))>0){
            out.write(buf,0,len);
        }
        out.close();
        in.close();

        return epubFile;
    }

    public String unzipEpub(File epubFile){
        //Open the file
        try(ZipFile file = new ZipFile(epubFile)) {
            //Get file entries
            Enumeration<? extends ZipEntry> entries = file.entries();

            //We will unzip files in this folder
            File tempDir = new File(ctx.getFilesDir().getPath() + "/temp");
            tempDir.mkdir();

            //Iterate over entries
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                //If directory then create a new directory in uncompressed folder
                if (entry.isDirectory())
                {
                    new File(tempDir.getPath() +  "/" + entry.getName()).mkdir();
                }
                //Else create the file
                else
                {
                    InputStream is = file.getInputStream(entry);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    String uncompressedFileName = tempDir.getPath() + "/" + entry.getName();
                    FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);
                    while (bis.available() > 0){
                        fileOutput.write(bis.read());
                    }
                    fileOutput.close();
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return ctx.getFilesDir().getPath() + "/temp";
    }

    public void listFilesForFolder(final File folder) {
        XmlParser xmlParser = new XmlParser();

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                String fileName = fileEntry.getName();

                if(fileName.endsWith(".opf")){
                    bookDetails = xmlParser.parseEpubDetails(fileEntry);
                }

                if(fileName.endsWith(".ncx")){
                    chapterList = xmlParser.parseChapterList(fileEntry);
                }

                if(fileName.endsWith(".xhtml") || fileName.endsWith(".html") || fileName.endsWith(".htm")){
                    htmlList.add(fileEntry);
                }
            }
        }
    }

    public void loadChapters() throws IOException{
        for(File currentFile : htmlList){
            for(int i = 0; i < chapterList.size(); i++){
                EpubChapter currentChapter = chapterList.get(i);
                if(currentFile.getName().endsWith(currentChapter.getChapterLink())){
                    org.jsoup.nodes.Document doc = Jsoup.parse(currentFile, "UTF-8");
                    currentChapter.setRawChapterContent( doc.html() );;
                    break;
                }
            }
        }
    }

    public void loadBookCover (){
        String filePath = "temp/" + bookDetails.getBookCoverLink();
        File mSaveBit = new File(ctx.getFilesDir(), filePath);;
        String imagePath = mSaveBit.getPath();
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        bookDetails.setBookCover(bitmap);
    }

    public void saveImages(String tempPath) throws IOException {
        File tempImagesFolder = new File(tempPath + "/images");
        File epubImagesFolder = new File(ctx.getFilesDir().getPath() + "/" + bookDetails.getBookName().replace(" ", "_"));
        epubImagesFolder.mkdir();
        epubImagesFolder = new File(epubImagesFolder.getPath() + "/images");
        epubImagesFolder.mkdir();

        FileUtils.copyDirectory(tempImagesFolder, epubImagesFolder);
    }
}
