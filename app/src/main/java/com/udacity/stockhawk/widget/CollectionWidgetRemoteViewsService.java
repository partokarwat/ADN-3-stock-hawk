package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by partokarwat on 2/14/17.
 * RemoteViewsService controlling the data being shown in the scrollable stock collection widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CollectionWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = CollectionWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] DETAIL_COLUMNS = {
            Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
            Contract.Quote.COLUMN_HISTORY
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int INDEX_QUOTE_ID = 0;
    public static final int INDEX_QUOTE_SYMBOL = 1;
    public static final int INDEX_QUOTE_PRICE = 2;
    public static final int INDEX_QUOTE_ABSOLUTE_CHANGE = 3;
    public static final int INDEX_QUOTE_PERCENTAGE_CHANGE = 4;
    public static final int INDEX_QUOTE_HISTORY = 5;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Uri stockUri = Contract.Quote.URI;
                data = getContentResolver().query(stockUri,
                        DETAIL_COLUMNS,
                        null,
                        null,
                        null
                );

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_collection_list_item);

                String symbol = data.getString(INDEX_QUOTE_SYMBOL);
                float price = data.getFloat(INDEX_QUOTE_PRICE);
                float change = data.getFloat(INDEX_QUOTE_PERCENTAGE_CHANGE);

                if (change > 0) {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

                DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");

                views.setTextViewText(R.id.symbol, symbol);
                views.setTextViewText(R.id.price, dollarFormat.format(price));
                views.setTextViewText(R.id.change, percentageFormat.format(change / 100));

                final Intent fillInIntent = new Intent();
                Uri stockUri = Contract.Quote.makeUriForStock(data.getString(INDEX_QUOTE_SYMBOL));
                fillInIntent.setData(stockUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_collection_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_QUOTE_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

