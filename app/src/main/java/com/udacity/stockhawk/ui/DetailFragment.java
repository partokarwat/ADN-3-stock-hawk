package com.udacity.stockhawk.ui;


import android.database.Cursor;
import android.graphics.Color;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract.Quote;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    private LineChart mChart;

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

        mChart = (LineChart) rootView.findViewById(R.id.sparkView);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        mChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setViewPortOffsets(0f, 0f, 0f, 0f);

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

            String history = data.getString(COL_QUOTE_HISTORY);
            String[] points = history.split("\n");

            ArrayList<Entry> values = new ArrayList<>();

            for (String point : points) {
                String[] axis = point.split(",");
                float x = TimeUnit.MILLISECONDS.toHours(Long.valueOf(axis[0]));
                float y = Float.valueOf(axis[1]);
                values.add(new Entry(x, y));
            }

            // always sort entries, unsorted nothing will be drawn!
            Collections.sort(values, new EntryXComparator());

            // create a dataset and give it a type
            LineDataSet set1 = new LineDataSet(values, getString(R.string.spark_line_label));
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(ColorTemplate.getHoloBlue());
            set1.setValueTextColor(ColorTemplate.getHoloBlue());
            set1.setLineWidth(1.5f);
            set1.setDrawCircles(false);
            set1.setDrawValues(false);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);
            set1.setDrawFilled(true);

            // create a data object with the datasets
            LineData chartData = new LineData(set1);
            chartData.setValueTextColor(Color.RED);
            chartData.setValueTextSize(9f);

            // set data
            mChart.setData(chartData);

            // dont forget to refresh the drawing
            mChart.invalidate();

            // get the legend (only possible after setting data)
            Legend l = mChart.getLegend();
            l.setEnabled(false);

            XAxis xAxis = mChart.getXAxis();
            xAxis.enableGridDashedLine(10f, 10f, 0f);
            xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
            xAxis.setTextSize(10f);
            xAxis.setTextColor(Color.WHITE);
            xAxis.setDrawAxisLine(false);
            xAxis.setDrawGridLines(true);
            xAxis.setTextColor(getResources().getColor(R.color.textColorChartAxis));
            xAxis.setCenterAxisLabels(true);
            xAxis.setGranularity(1f); // one hour
            xAxis.setAxisMaximum(Collections.max(values, new EntryXComparator()).getX() + 2000);
            xAxis.setValueFormatter(new IAxisValueFormatter() {

                private SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM yy");

                @Override
                public String getFormattedValue(float value, AxisBase axis) {

                    long millis = TimeUnit.HOURS.toMillis((long) value);
                    return mFormat.format(new Date(millis));
                }
            });

            YAxis leftAxis = mChart.getAxisLeft();
            leftAxis.enableGridDashedLine(10f, 10f, 0f);
            leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
            leftAxis.setTextColor(ColorTemplate.getHoloBlue());
            leftAxis.setDrawGridLines(true);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setAxisMaximum(Collections.max(values, new EntryXComparator()).getY() + Collections.min(values, new EntryXComparator()).getY());
            leftAxis.setYOffset(-9f);
            leftAxis.setTextColor(getResources().getColor(R.color.textColorChartAxis));

            // limit lines are drawn behind data (and not on top)
            leftAxis.setDrawLimitLinesBehindData(true);

            YAxis rightAxis = mChart.getAxisRight();
            rightAxis.setEnabled(false);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
