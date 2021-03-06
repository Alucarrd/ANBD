package com.petina.android.newsapp;

import android.app.Activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;


import java.util.List;

public class NewsArticleAdapter extends ArrayAdapter<NewsArticle> {
    private Activity _context;
    //constructor for adapter
    public NewsArticleAdapter(Activity context, List<NewsArticle> newsArticles) {
        super(context, 0, newsArticles);
        _context = context;

    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The position in the list of data that should be displayed in the
     *                    list item view.
     * @param convertView The recycled view to populate.
     * @param parent      The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item_constraintlayout, parent, false);
        }

        NewsArticle myItem = getItem(position);
        TextView sectionName = (TextView) listItemView.findViewById(R.id.item_section_name);
        sectionName.setText(myItem.getSectionName());

        TextView title = (TextView) listItemView.findViewById(R.id.item_title);
        title.setText(myItem.getTitle());
        Log.v("Adapter", myItem.getContributor());
        TextView contributor = (TextView) listItemView.findViewById(R.id.item_contributor);

        if (myItem.getContributor().equalsIgnoreCase("")) {
            contributor.setVisibility(View.GONE);

        } else {
            String contributorTemplate = _context.getString(R.string.label_contributor);
            contributor.setVisibility(View.VISIBLE);
            contributor.setText(String.format(contributorTemplate, myItem.getContributor()));
        }

        TextView publication = (TextView) listItemView.findViewById(R.id.item_publication);
        TextView publication_label = (TextView) listItemView.findViewById(R.id.item_publication_label);

        if (myItem.getPublicationDate().equalsIgnoreCase("")) {
            publication.setVisibility(View.GONE);
            publication_label.setVisibility(View.GONE);
        } else {
            publication.setVisibility(View.VISIBLE);
            publication_label.setVisibility(View.VISIBLE);
            publication.setText(getMyDate(myItem.getPublicationDate()));
        }

        return listItemView;


    }

    /**
     * Takes in the ISO 8601 format date and convert to MM/dd/yyyy format
     *
     * @param myDate String format of publication date returned from the guardian news api
     * @return String of MM/dd/yyyy
     */
    public String getMyDate(String myDate) {

        if (!myDate.isEmpty()) {
            Log.v("adapter", myDate);
            try {
                Date date = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSZ")).parse(myDate.replaceAll("Z$", "+0000"));
                return new SimpleDateFormat("MM/dd/yyyy").format(date);
            } catch (ParseException e) {
                return "";
            }

        } else
            return "";

    }


}






