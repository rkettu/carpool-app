package com.example.carpool_app;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

public class GetRideSpinner implements SpinnerAdapter, ListAdapter {

    private static final int getRideSpinnerExtra = 1;
    private SpinnerAdapter spinnerAdapter;
    protected Context context;
    private int nothingSelectedLayout;
    private int nothingSelectedDropdownLayout;
    private LayoutInflater layoutInflater;

    public GetRideSpinner(SpinnerAdapter spinnerAdapter, int nothingSelectedLayout, Context context)
    {
        this(spinnerAdapter, nothingSelectedLayout, -1, context);
    }

    public GetRideSpinner(SpinnerAdapter spinnerAdapter, int nothingSelectedLayout, int nothingSelectedDropdownLayout, Context context)
    {
        this.spinnerAdapter = spinnerAdapter;
        this.nothingSelectedLayout = nothingSelectedLayout;
        this.nothingSelectedDropdownLayout = nothingSelectedDropdownLayout;
        this.context = context;
        layoutInflater = layoutInflater.from(context);
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if(position == 0)
        {
            return getNothingSelectedView(parent);
        }
        return spinnerAdapter.getView(position - getRideSpinnerExtra, null, parent);
    }

    private View getNothingSelectedView(ViewGroup parent)
    {
        return layoutInflater.inflate(nothingSelectedLayout, parent, false);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(position == 0)
        {
            return nothingSelectedDropdownLayout == -1 ? new View(context) : getNothingSelectedDropdownView(parent);
        }
        return spinnerAdapter.getDropDownView(position - getRideSpinnerExtra, null, parent);
    }

    private View getNothingSelectedDropdownView(ViewGroup parent)
    {
        return layoutInflater.inflate(nothingSelectedDropdownLayout, parent, false);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        //doesn't allow you to pick item "nothing selected"
        return position != 0;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        spinnerAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        spinnerAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public int getCount() {
        int count = spinnerAdapter.getCount();
        return count == 0 ? 0 : count + getRideSpinnerExtra;
    }

    @Override
    public Object getItem(int position) {
        return position == 0 ? null : spinnerAdapter.getItem(position - getRideSpinnerExtra);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return spinnerAdapter.hasStableIds();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return spinnerAdapter.isEmpty();
    }
}
