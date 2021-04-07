package com.example.awsamplify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class historyActivity extends AppCompatActivity {

    ArrayList<String> asHistory;

    ListView listView;
    ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Intent intent = getIntent();
        asHistory = intent.getStringArrayListExtra("history");

        listView = findViewById(R.id.listHistory);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,asHistory);
        listView.setAdapter(listAdapter);

    }

    public void goBack(View view)
    {
        finish();
    }
}