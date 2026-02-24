package me.kaylunasa.tahanapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import me.kaylunasa.tahanapp.R;

public class FontArrayAdapter<T> extends ArrayAdapter<T> {
    private Typeface typeface;

    public FontArrayAdapter(@NonNull Context context, int resource, @NonNull T[] objects, Typeface font) {
        super(context, resource, objects);
        this.typeface = font;
    }

    @NonNull
    @Override
    public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(pos, convertView, parent);
        view.setTypeface(typeface);
        view.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        int pxPadding = Math.round(15 * getContext().getResources().getDisplayMetrics().density);
        view.setPadding(pxPadding, view.getPaddingTop(), pxPadding, view.getPaddingBottom());
        return view;
    }

    @Override
    public View getDropDownView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(pos, convertView, parent);
        view.setTypeface(typeface);
        int pxPadding = Math.round(10 * getContext().getResources().getDisplayMetrics().density);
        view.setPadding(pxPadding, view.getPaddingTop(), pxPadding, view.getPaddingBottom());
        return view;
    }
}
