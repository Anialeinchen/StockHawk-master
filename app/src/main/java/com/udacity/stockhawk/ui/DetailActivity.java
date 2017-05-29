package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Anna Morgiel on 23.05.2017.
 */

public class DetailActivity extends AppCompatActivity {

    public static final String STOCK_DETAILS = "stockDetails";

    @BindView(R.id.chart)
    LineChart chart;

    @BindView(R.id.tv)
    TextView tv;
    String details = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        Intent intent = getIntent();

        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(STOCK_DETAILS)) {
                details = extras.getString(STOCK_DETAILS);
            }
        }

        getHistory(details);
        tv.setText(details);
    }

    private void getHistory(String s) {
        String history = getHistoryString(s);

        List<String[]> lines = getLines(history);

        List<Entry> entries = new ArrayList<>(lines.size());

        final ArrayList<Long> xAxisValues = new ArrayList<>();
        int xAxisPosition = 0;

        for (int i = lines.size() -1; i >= 0; i--) {
            String[] line = lines.get(i);

            xAxisValues.add(Long.valueOf(line[0]));
            xAxisPosition++;

            Entry entry =
                    new Entry(
                            xAxisPosition,
                            Float.valueOf(line[1]));

            entries.add(entry);
        }

        setupChart(s, entries, xAxisValues);
        tv.setText(s);
    }

    private void setupChart(String symbol, List<Entry> entries, final List<Long> xAxisValues) {
        LineData lineData = new LineData(new LineDataSet(entries, symbol));
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date(xAxisValues.get(xAxisValues.size()-(int) value - 1));
                return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                        .format(date);
            }
        });
    }

    @Nullable
    private List<String[]> getLines(String history) {
        List<String[]> lines = null;
        CSVReader reader = new CSVReader(new StringReader(history));
        try {
            lines = reader.readAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private String getHistoryString(String s) {
        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(s),
                null,
                null,
                null,
                null);
        String history = "";
        if (cursor.moveToNext()) {
            history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            cursor.close();
        }
        return history;
    }
}
