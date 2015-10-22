package com.example.bookstore;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookstore.adapter.BookAdapter;
import com.example.bookstore.datahandling.DataParser;
import com.example.bookstore.loader.AbstractLoader;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.BookStorage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BookstoreActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<Book>> {

    private ListView mBookListView;

    @Bind(R.id.book_view_code)
    TextView mCode;

    @Bind(R.id.book_view_name)
    TextView mName;

    @Bind(R.id.book_view_author)
    TextView mAuthor;

    @Bind(R.id.book_view_language)
    TextView mLanguage;

    @Bind(R.id.book_view_pages)
    TextView mPages;

    @Bind(R.id.book_view_price)
    TextView mPrice;

    @Bind(R.id.book_view_link)
    TextView mLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bookstore);

        ButterKnife.bind(this);

        BookstoreFragment mBookstoreFragment = (BookstoreFragment)
                getFragmentManager().findFragmentById(R.id.bookstore_fragment_id_in_activity);
        View mFragment = mBookstoreFragment.getView();

        mBookListView = (ListView) mFragment;
        try {
            mBookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String mString = BookstoreActivity.this.getString(R.string.screen_type);

                    if (mString.equals("tablet")) {
                        fillingFieldsInViewBookFragment(BookStorage.getInstance().getBook(position));
                    } else if (mString.equals("phone")) {
                        Intent intent = new Intent(BookstoreActivity.this, ViewBookActivity.class);
                        intent.putExtra(Constants.SELECTED_BOOK, position);
                        startActivity(intent);
                    }
                }
            });
        } catch (NullPointerException e) {
            Log.d(Constants.LOG_TAG, "setOnItemClickListener.NullPointerException = " + e);
        }
        try {
            mBookListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Book book = BookStorage.getInstance().getBook(position);
                    if (book.isFavorite()) {
                        Log.d(Constants.LOG_TAG, "Book \"" + book.getName() + "\" remove from favorite");
                        BookStorage.getInstance().removeFavoriteBook(book);
                    } else {
                        Log.d(Constants.LOG_TAG, "Book \"" + book.getName() + "\" added to favorite");
                        BookStorage.getInstance().addFavoriteBook(book);
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            Log.d(Constants.LOG_TAG, "setOnItemLongClickListener.Exception = " + e);
        }
        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bookstore_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorite_action:
                Log.d(Constants.LOG_TAG, "Favorite action button pressed");
                mBookListView.setAdapter(new BookAdapter(this, BookStorage.getInstance().getFavoritesBooks()));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        return new BookLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> data) {
        mBookListView.setAdapter(new BookAdapter(this, data));
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
    }

    public void fillingFieldsInViewBookFragment(Book book) {
        mCode.setText(book.getCode());
        mName.setText(book.getName());
        mAuthor.setText(book.getAuthor());
        mLanguage.setText(book.getLanguage());
        mPages.setText(book.getPages());
        mPrice.setText(book.getPrice());
        mLink.setText(book.getLink());
    }

    private static class BookLoader extends AbstractLoader<List<Book>> {

        BookLoader(Context context) {
            super(context);
        }

        @Override
        public List<Book> loadInBackground() {
            DataParser mParser = new DataParser();
            URL mResource = null;
            try {
                mResource = new URL(Constants.URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return mParser.parseBookList(mResource);
        }

        @Override
        protected void freeResource(List<Book> data) {

        }

    }
}
