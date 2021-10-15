package com.example.mobileproject;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class VisitAllSourcesActivity  extends AppCompatActivity {

    Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_in_all_sources);

        int[] lista = new int[]{R.layout.search_in_source_grid,R.layout.search_in_source_grid};
        GridView gv = (GridView) findViewById(R.id.novelsGrid);
        gv.setAdapter(new SearchNovelGridAdapter((Context) this, lista));

        Button simpleButton1 = (Button) findViewById(R.id.return_activity);
        simpleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ctx, "Funcionou o que eu queria", Toast.LENGTH_LONG).show();//display the text of button1
                finish();
            }
        });
    }
}