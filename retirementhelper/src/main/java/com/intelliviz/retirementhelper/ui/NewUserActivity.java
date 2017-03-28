package com.intelliviz.retirementhelper.ui;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.intelliviz.retirementhelper.R;
import com.intelliviz.retirementhelper.db.RetirementContract;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Class to manager the registration of a new user.
 * CLass gathers user name (email) and password information.
 * @author Ed Muhlestein
 */
public class NewUserActivity extends AppCompatActivity implements UserInfoListener {
    private int mEmailStatus;
    private int mPasswordStatus;
    @Bind(R.id.pasword_edit_text) EditText mPasswordEditText;
    @Bind(R.id.password2_edit_text) EditText mPassword2EditText;
    @Bind(R.id.email_edit_text) EditText mEmailEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        ButterKnife.bind(this);

        ActionBar ab = getSupportActionBar();
        ab.setSubtitle("New User");
    }

    public void registerUser(View view) {
        mEmailStatus = UserInfoConstants.EMAIL_UNKNOWN;
        mPasswordStatus = UserInfoConstants.PASSWORD_UNKNOWN;

        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String password2 = mPassword2EditText.getText().toString();
        if(!validateEmail(email)) {
            // TODO pop up toast saying that email is invalid
            return;
        }

        if(!validatePassword(password, password2)) {
            // TODO pop up toast saying password is not valid
            return;
        }

        // now check password in db.
        validateUser(email, password);

        //Intent intent = new Intent(this, SummaryActivity.class);
        //startActivity(intent);
    }

    /**
     * Passwords must match and must be valid.
     * TODO valid password rules need to be defined.
     */
    private boolean validatePassword(String password1, String password2) {
        return true;
    }

    /**
     * Passwords must match and must be valid.
     * TODO valid password rules need to be defined.
     */
    private boolean validateEmail(String email) {
        return true;
    }

    private void validateUser(String email, String password) {
        UserInfoQueryHandler userInfoQueryHandler =
                new UserInfoQueryHandler(getContentResolver(), this);
        String selection = RetirementContract.PeronsalInfoEntry.TABLE_NAME + "." +
                RetirementContract.PeronsalInfoEntry.COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Uri uri = RetirementContract.PeronsalInfoEntry.CONTENT_URI.buildUpon().appendPath(email).build();
        userInfoQueryHandler.startQuery(1, password, uri, null, selection, selectionArgs, null);
    }

    @Override
    public void onQueryUserInfo(Cursor cursor, Object cookie) {
        if(cursor == null || !cursor.moveToFirst()) {
            // email is valid and does not exist in db; can add it
            return;
        } else {
            // email already exists; don't add it.
        }

        String dbEmail = "";
        int emailIndex = cursor.getColumnIndex(RetirementContract.PeronsalInfoEntry.COLUMN_EMAIL);
        if(emailIndex != -1) {
            dbEmail = cursor.getString(emailIndex);
            /*
            if(dbEmail.equals(email)) {
                // email already in use
                // TODO pop up toast that says email already in use
                mEmailStatus = UserInfoConstants.EMAIL_IN_USE;
            } else {
                // email not in use; add it
                UserInfoQueryHandler emailQueryHandler =
                        new UserInfoQueryHandler(getContentResolver(), this);
                ContentValues values = new ContentValues();
                values.put(RetirementContract.PeronsalInfoEntry.COLUMN_PASSWORD, "x");
                values.put(RetirementContract.PeronsalInfoEntry.COLUMN_EMAIL, email);


                Uri uri = RetirementContract.PeronsalInfoEntry.CONTENT_URI.buildUpon().appendPath(email).build();
                emailQueryHandler.startInsert(1, null, uri, values);
            }
            */
        }
    }

    @Override
    public void onInsertEmail(Uri insrtedUri, Object cookie) {

    }

    private class UserInfoQueryHandler extends AsyncQueryHandler {

        private WeakReference<UserInfoListener> mListener;

        public UserInfoQueryHandler(ContentResolver cr, UserInfoListener listener) {
            super(cr);
            mListener = new WeakReference<>(listener);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            final UserInfoListener listener = mListener.get();
            if(listener != null) {
                listener.onQueryUserInfo(cursor, cookie);
            }
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            final UserInfoListener listener = mListener.get();
            if(listener != null) {
                listener.onInsertEmail(uri, cookie);
            }
        }
    }
}
