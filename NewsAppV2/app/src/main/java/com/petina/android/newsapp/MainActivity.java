package com.petina.android.newsapp;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Loader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<NewsArticle>> {

    //define constants
    private static final String apiKey = BuildConfig.ApiKey;
    private static final int NEWSARTICLE_LOADER_ID = 1;
    private static final String LOG_TAG = MainActivity.class.getName();
    //private final String NEWS_URL = "http://content.guardianapis.com/search?q=[TOPIC]&show-tags=contributor&api-key=[API_KEY]";
    private final String NEWS_URL_ROOT = "https://content.guardianapis.com/search";


    //define private variable for internal class use
    private TextView _emptyStateTextView;
    private NewsArticleAdapter _newsArticleAdapter;
    private SwipeRefreshLayout _mySwiper;
    private View _loadingIndicator;
    private ListView _newsArticleListView;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    // This method initialize the contents of the Activity's options menu.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the Options Menu we specified in XML
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //link view objects to UI elements
        _mySwiper = findViewById(R.id.swiperefresh);
        _emptyStateTextView = (TextView) findViewById(R.id.empty_view);


        //check internet connection
        //if there's no connectivity, then display No internet connection msg, otherwise, create the instance of loader
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            //I have intenet connection
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWSARTICLE_LOADER_ID, null, this);

        } else {
            // First, hide loading indicator so error message will be visible
            _loadingIndicator = findViewById(R.id.indeterminateBar);
            _loadingIndicator.setVisibility(View.GONE);
            _emptyStateTextView.setText(R.string.no_internet_connection);
        }

        //Time to display list items
        _newsArticleListView = (ListView) findViewById(R.id.list);
        _newsArticleListView.setEmptyView(_emptyStateTextView);

        //setting up adapter
        _newsArticleAdapter = new NewsArticleAdapter(this, new ArrayList<NewsArticle>());
        _newsArticleListView.setAdapter(_newsArticleAdapter);

        //setup onclick listener to redirect clicker to actual news article web link
        _newsArticleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsArticle myItem = _newsArticleAdapter.getItem(position);
                String myURL = myItem.getURL();
                Intent urlIntent = new Intent(Intent.ACTION_VIEW);
                urlIntent.setData(Uri.parse(myURL));
                startActivity(urlIntent);
            }
        });

        //setup refresh listener
        _mySwiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "OnRefresh called");
                restartLoader(NEWSARTICLE_LOADER_ID);
                _mySwiper.setRefreshing(false);

            }
        });

    }

    /**
     * Takes in a topic and build guardian news api url
     *
     * @param myTopic -> the topic to search with
     * @return string url to the guardian news api
     */
   /*
    private String buildTopicURL(String myTopic) {

        String myRequestURL = NEWS_URL.replace("[API_KEY]", apiKey).replace("[TOPIC]", myTopic);
        Log.v(LOG_TAG, "building my API URL: " + myRequestURL);
        return myRequestURL;

    }
 */

    /**
     * Override the onCreateLoader for async url fetch
     */
    @Override
    public Loader<List<NewsArticle>> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "OnCreateLoader");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //collecting preference settings
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_orderby_key),
                getString(R.string.settings_orderby_default)
        );

        String section = sharedPrefs.getString(
                getString(R.string.settings_section_key),
                getString(R.string.settings_section_default)
        );

        String fromDate = sharedPrefs.getString(
                getString(R.string.settings_fromdate_key),
                getString(R.string.settings_fromdate_default));

        String toDate = sharedPrefs.getString(
                getString(R.string.settings_todate_key),
                getString(R.string.settings_todate_default));

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(NEWS_URL_ROOT);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Append query parameter and its value. For example, the `format=geojson`
        uriBuilder.appendQueryParameter("api-key", apiKey);
        uriBuilder.appendQueryParameter("show-tags", "contributor");

        String orderByNone = getString(R.string.settings_orderby_none_value);

        if (!orderBy.equalsIgnoreCase(orderByNone))
            uriBuilder.appendQueryParameter("order-by", orderBy);
        if (!fromDate.isEmpty())
            uriBuilder.appendQueryParameter("from-date", fromDate);
        if (!toDate.isEmpty())
            uriBuilder.appendQueryParameter("to-date", toDate);

        uriBuilder.appendQueryParameter("q", section);

        Log.v(LOG_TAG, uriBuilder.toString());
        return new NewsArticleLoader(this, uriBuilder.toString());

    }

    /**
     * Override the onLoadFinished for async url fetch
     */
    @Override
    public void onLoadFinished(Loader<List<NewsArticle>> loader, List<NewsArticle> data) {
        Log.v(LOG_TAG, "OnLoaderFinished");
        _emptyStateTextView.setText(R.string.no_article);

        View loadingIndicator = findViewById(R.id.indeterminateBar);
        loadingIndicator.setVisibility(View.GONE);
        _newsArticleAdapter.clear();
        if (data != null && !data.isEmpty()) {
            for (int i = 0; i < data.size(); i++) {
                Log.v(LOG_TAG, String.valueOf(i + 1) + ". " + data.get(i).getContributor());
            }
            _newsArticleAdapter.addAll(data);
        }
    }

    /**
     * Override the onLoaderReset for async url fetch
     */
    @Override
    public void onLoaderReset(Loader<List<NewsArticle>> loader) {
        Log.v(LOG_TAG, "OnLoaderReset");
        _newsArticleAdapter.clear();
    }

    /**
     * Restart loader action when swiper fresh's triggered
     *
     * @param loaderId int Id for the loader defined above.
     */
    private void restartLoader(int loaderId) {
        getLoaderManager().restartLoader(loaderId, null, this);
    }

}


