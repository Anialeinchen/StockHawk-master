package com.udacity.stockhawk.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Anna Morgiel on 28.05.2017.
 */

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockWidgetViewsFactory(getApplicationContext());
    }

    private class StockWidgetViewsFactory implements RemoteViewsFactory {
        private final Context mContext;
        DecimalFormat dollarFormat;
        DecimalFormat dollarFormatWithPlus;
        DecimalFormat percentageFormat;

        List<ContentValues> contentValuesList = new ArrayList<>();

        public StockWidgetViewsFactory(Context applicationContext) {
            mContext = applicationContext;
            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");
        }

        @Override
        public void onCreate() {
            getData();
        }

        private void getData() {
            contentValuesList.clear();
            ContentResolver contentResolver = mContext.getContentResolver();

            Cursor cursor = contentResolver.query(
                    Contract.Quote.URI,
                    null,
                    null,
                    null,
                    null);

            while (cursor.moveToNext()) {
                String symbol = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                float price = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                float absChange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
                float percentChange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));

                ContentValues cv = new ContentValues();

                cv.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                cv.put(Contract.Quote.COLUMN_PRICE, price);
                cv.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, absChange);
                cv.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);

                contentValuesList.add(cv);
            }
            cursor.close();
        }


        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return contentValuesList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            ContentValues cv = contentValuesList.get(position);
            RemoteViews remoteViews = new RemoteViews(
                    mContext.getPackageName(),
                    R.layout.list_item_quote);
            remoteViews.setTextViewText(R.id.symbol, cv.getAsString(Contract.Quote.COLUMN_SYMBOL));
            remoteViews.setTextViewText(R.id.price, dollarFormat.format(cv.getAsFloat(Contract.Quote.COLUMN_PRICE)));

            float absChange = cv.getAsFloat(Contract.Quote.COLUMN_ABSOLUTE_CHANGE);
            float percChange = cv.getAsFloat(Contract.Quote.COLUMN_PERCENTAGE_CHANGE);
            if (absChange> 0){
                remoteViews.setInt(
                        R.id.change, "setBackgroundResource",
                        R.drawable.percent_change_pill_green );
            }else{
                remoteViews.setInt(
                        R.id.change, "setBackgroundResource",
                        R.drawable.percent_change_pill_red );
            }
            remoteViews.setTextViewText(R.id.change, percentageFormat.format(percChange / 100));

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
