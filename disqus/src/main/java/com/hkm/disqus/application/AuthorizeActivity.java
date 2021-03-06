package com.hkm.disqus.application;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.hkm.disqus.BuildConfig;
import com.hkm.disqus.R;
import com.hkm.disqus.api.AuthTokenServiceManager;
import com.hkm.disqus.DisqusClient;
import com.hkm.disqus.api.model.oauth2.AccessToken;

/**
 * Created by hesk on 21/5/15.
 */
public class AuthorizeActivity extends AppCompatActivity implements AuthTokenServiceManager.AuthenticationListener {
    public static final String TAG = "authorization act";
    /**
     * Extras that should be passed in the {@link Intent}
     */
    public static final String EXTRA_API_KEY = "api_key";
    public static final String EXTRA_SCOPES = "scope";
    public static final String EXTRA_REDIRECT_URI = "redirect_uri";

    /**
     * Extras that are passed in the result
     */
    public static final String EXTRA_ACCESS_TOKEN = "access_token";
    public static final String EXTRA_SECRET = "secret";
    public static final String EXTRA_DEFAULT_ACCESS = "default";
    public static final String EXTRA_FORUM_NAME = ".forumname";
    public static final String EXTRA_STATUS_BAR_TOP = ".top_bar";


    @LayoutRes
    protected int authorize_layout() {
        return R.layout.disqusloginactivityframelayout;
    }

    protected DisqusClient getClient() {
        return DisqusClient.getInstance(this, AuthorizationFragment.genConfig(getIntent().getExtras()));
    }

    protected void statFragmentLogin(Bundle fragmentextras) {
        getFragmentManager().beginTransaction().add(
                R.id.disqus_fragment_id_authorize,
                AuthorizationFragment.B(fragmentextras)
        ).commit();
    }

    protected void check_status_bar_height() {
        if (!getIntent().getExtras().getBoolean(EXTRA_STATUS_BAR_TOP, true)) {
            View statusbar = (View) findViewById(R.id.disqus_status_bar_login);
            statusbar.setVisibility(View.GONE);
        }
    }

    //  private Binder binder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(authorize_layout());
       /* if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View statusbar = (View) findViewById(R.id.disqus_status_bar_login);
            statusbar.setVisibility(View.GONE);
        }*/
        check_status_bar_height();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Get extras
            statFragmentLogin(extras);
        } else {
            // Can't do anything without the right extras so finish
            // TODO Add some sort of error handling?
            finish();
        }
        // binder = ((BinderProvider) getParentFragment()).createBinder(this);
    }

    /*  @Override
      public void onSuccess(AccessToken accessToken) {
          // Create a result intent
          Intent data = new Intent();
          data.putExtra(EXTRA_ACCESS_TOKEN, accessToken);
          setResult(RESULT_OK, data);
          finish();
      }

      @Override
      public void onFailure() {
          Toast.makeText(this, getResources().getString(R.string.failurelogin), Toast.LENGTH_LONG);
          setResult(RESULT_CANCELED);
          finish();
      }
  */
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
        getClient().getAuthManager().addListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getClient().getAuthManager().removeListener(this);
    }

    @Override
    public void onLogin(AccessToken accessToken) {
        //   binder.onUserAuthenticated(true);
        Intent data = new Intent();
        data.putExtra(EXTRA_ACCESS_TOKEN, accessToken);
        setResult(RESULT_OK, data);
        getClient().getConfiguration().setAccessToken(accessToken.accessToken);
        finish();
    }

    @Override
    public void onLoginFailed(String error) {
        //  binder.onUserAuthenticated(false);
        final String failure = "Acquire token failure:" + error;
        final MessageD m = new MessageD(failure);
        Log.d(TAG, failure);
        m.show(getFragmentManager(), "NoticeDialogFragment");
    }

    @Override
    public void onLogout() {
        /** Not used**/
        Log.d(TAG, "onLogout");
    }

    @SuppressLint("ValidFragment")
    public class MessageD extends DialogFragment {
        private final String m;

        public MessageD(String message) {
            m = message;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(this.m)
                    .setPositiveButton(R.string.donesuccess, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //    onFailure();
                            onLoginFailed(m);
                        }
                    });
            return builder.create();
        }
    }
}