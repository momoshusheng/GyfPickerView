package com.example.gyfpickerview;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GyfPickerView.OnScrollChangedListener{
    private GyfPickerView pickerView;
    private GyfPickerView pickerview_day;
    private ArrayList<String> dataList=new ArrayList<>();
    private ArrayList<String> dataList_day=new ArrayList<>();
    private TextView text_result;
    private  String month,day;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickerView= (GyfPickerView) findViewById(R.id.pickerview);
        pickerview_day= (GyfPickerView) findViewById(R.id.pickerview_day);
        text_result= (TextView) findViewById(R.id.text_result);

        for (int i = 1; i <13 ; i++) {
            dataList.add(i+"");
        }
        for (int i = 1; i <31 ; i++) {
            dataList_day.add(i+"");
        }
        pickerView.setDataList(dataList);
        pickerview_day.setDataList(dataList_day);
        pickerview_day.setOnScrollChangedListener(this);
        pickerView.setOnScrollChangedListener(this);
        month=dataList.get(0);
        day=dataList_day.get(0);
        text_result.setText(month+" 月"+day+" 日");
    }

        @Override
        public void onScrollChanged(int curIndex,View view) {
                setTextResutl(curIndex,view);
        }

        @Override
        public void onScrollFinished(int curIndex,View view) {
            setTextResutl(curIndex,view);

        }
    public void setTextResutl(int curIndex,View view){
        if (view.getId()==R.id.pickerview)
            month=dataList.get(curIndex);
        else
            day=dataList_day.get(curIndex);

        text_result.setText(month+"  月"+day+"  日");
    }
}
