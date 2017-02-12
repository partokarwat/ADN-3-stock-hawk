package com.udacity.stockhawk.ui;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract.Quote;

/**
 * A fragment containing the history of a stock.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String DETAIL_URI = "URI";

    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            Quote.TABLE_NAME + "." + Quote._ID,
            Quote.COLUMN_SYMBOL,
            Quote.COLUMN_PRICE,
            Quote.COLUMN_ABSOLUTE_CHANGE,
            Quote.COLUMN_PERCENTAGE_CHANGE,
            Quote.COLUMN_HISTORY
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_QUOTE_ID = 0;
    public static final int COL_QUOTE_SYMBOL = 1;
    public static final int COL_QUOTE_PRICE = 2;
    public static final int COL_QUOTE_ABSOLUTE_CHANGE = 3;
    public static final int COL_QUOTE_PERCENTAGE_CHANGE = 4;
    public static final int COL_QUOTE_HISTORY = 5;

    private TextView testView;

    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        testView = (TextView) rootView.findViewById(R.id.symbol);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            String symbol = data.getString(COL_QUOTE_SYMBOL);
            testView.setText(symbol);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
