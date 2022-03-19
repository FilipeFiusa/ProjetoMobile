package com.example.mobileproject.ui.reader_settings;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import com.example.mobileproject.R;
import com.example.mobileproject.ReaderActivity;
import com.example.mobileproject.util.FontFactory;

public class ReaderSettings {
    private FrameLayout ReaderSettings;
    private ReaderActivity ctx;

    private float size = 0;
    private String font;
    private String font_color;
    private String background_color;

    public ReaderSettings(@NonNull FrameLayout frameLayout, ReaderActivity ctx) {
        this.ReaderSettings = frameLayout;
        this.ctx = ctx;

        SetUpReaderConfigMenu();
    }

    private void SetUpReaderConfigMenu(){
        TextView textPreview = (TextView) ReaderSettings.findViewById(R.id.text_preview);
        LinearLayout bgPreview = (LinearLayout) ReaderSettings.findViewById(R.id.background_preview);

        //reader_font_size_selector
        Spinner dropdown = ReaderSettings.findViewById(R.id.reader_font_size_selector);
        String[] items = new String[]{"16", "17","18","19","20","21","22","23","24","25","26","27","28","29","30"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                size = (Float) Float.parseFloat((String) parent.getItemAtPosition(position));
                Log.v("item", (String) parent.getItemAtPosition(position));
                textPreview.setTextSize(size);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        Spinner dropdown2 = ReaderSettings.findViewById(R.id.reader_font_family_selector);
        String[] items2 = new FontFactory().getAvailableFontsInString();
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, items2);
        dropdown2.setAdapter(adapter2);
        dropdown2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                Log.i("item: ", (String) parent.getItemAtPosition(position));

                font = (String) parent.getItemAtPosition(position);

                textPreview.setTypeface(new FontFactory().getFont(font, ctx));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        LinearLayout mColorChanger = ReaderSettings.findViewById(R.id.reader_font_color_selector);
        mColorChanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialog colorPickerDialog= ColorPickerDialog.createColorPickerDialog(ctx,ColorPickerDialog.DARK_THEME);
                colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(int color, String hexVal) {
                        font_color = hexVal;

                        textPreview.setTextColor(Color.parseColor(font_color));
                    }
                });
                colorPickerDialog.show();
            }
        });

        LinearLayout mBgChanger = ReaderSettings.findViewById(R.id.reader_bg_color_selector);
        bgPreview.setBackgroundColor(ctx.getResources().getColor(R.color.main_dark_theme));
        mBgChanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialog colorPickerDialog= ColorPickerDialog.createColorPickerDialog(ctx,ColorPickerDialog.DARK_THEME);
                colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(int color, String hexVal) {
                        background_color = hexVal;

                        bgPreview.setBackgroundColor(Color.parseColor(background_color));
                        mBgChanger.setBackgroundColor(Color.parseColor(background_color));
                    }
                });
                colorPickerDialog.show();
            }
        });

        Button saveChangesButton = (Button) ReaderSettings.findViewById(R.id.save_changes_button);
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.changeReaderSettings(size, font, font_color, background_color);
            }
        });

        ImageButton imageButton = (ImageButton) ReaderSettings.findViewById(R.id.close_reader_settings);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout container = (RelativeLayout) ctx.findViewById(R.id.reader_activity);
                container.removeView(ReaderSettings);
                ctx.readerSettings = null;
            }
        });
    }

    public FrameLayout getFrameLayout(){
        return ReaderSettings;
    }

}
