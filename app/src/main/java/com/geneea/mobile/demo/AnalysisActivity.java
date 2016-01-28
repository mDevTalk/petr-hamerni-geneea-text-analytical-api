package com.geneea.mobile.demo;

import com.geneea.mobile.demo.model.EntitiesResponse;
import com.geneea.mobile.demo.model.Request;
import com.geneea.mobile.demo.model.SentimentResponse;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import java.io.IOException;

/**
 * A main activity used for running the NLP analysis.
 */
public class AnalysisActivity extends AppCompatActivity {

    /**
     * Keep track of the analysis task to ensure we can cancel it if requested.
     */
    private AnalysisTask analysisTask = null;

    private GeneeaAPI geneeaAPI;
    private String authorization;

    // UI references
    private View mProgressView;
    private View mAnalysisFormView;
    private EditText mTextToAnalyze;
    private Spinner mAnalysisLanguage;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        mAnalysisFormView = findViewById(R.id.analysis_form);
        mProgressView = findViewById(R.id.analysis_progress);

        // setup the call-backs for running the analysis
        mTextToAnalyze = (EditText) findViewById(R.id.analysis_input);
        mTextToAnalyze.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView textView, final int id, final KeyEvent keyEvent) {
                if (id == R.id.analysis_input || id == EditorInfo.IME_NULL) {
                    runAnalysis();
                    return true;
                }
                return false;
            }
        });
        final Button mAnalyzeButton = (Button) findViewById(R.id.analyze_button);
        mAnalyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runAnalysis();
            }
        });

        // setup the languages spinner
        mAnalysisLanguage = (Spinner) findViewById(R.id.analysis_language);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.languages, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAnalysisLanguage.setAdapter(adapter);

        // create a REST API client using the Retrofit lib
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.rest_api_host))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geneeaAPI = retrofit.create(GeneeaAPI.class);
        authorization = getString(R.string.authorization);
    }

    /**
     * Shows the progress UI and hides the form.
     */
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mAnalysisFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mAnalysisFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnalysisFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mAnalysisFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Calls the analysis API.
     */
    private void runAnalysis() {
        if (analysisTask != null) {
            return;
        }

        mTextToAnalyze.setError(null);
        final String text = mTextToAnalyze.getText().toString();

        final Object selectedItem = mAnalysisLanguage.getSelectedItem();
        final String language = (selectedItem == null) ? null : selectedItem.toString();

        if (TextUtils.isEmpty(text)) {
            mTextToAnalyze.setError(getString(R.string.error_text_required));
            mAnalysisFormView.requestFocus();
        } else {
            // Show a progress spinner, and kick off an analysis task
            showProgress(true);
            analysisTask = new AnalysisTask();
            analysisTask.execute(text, language);
        }
    }

    /**
     * Shows the results using {@link ShowResultsActivity}.
     */
    private void showResults(final EntitiesResponse entities, final SentimentResponse sentiment) {
        // start the result visualization activity
        final Intent intent = new Intent(getBaseContext(), ShowResultsActivity.class);
        intent.putExtra(ShowResultsActivity.ENTITIES_PARAM_NAME, entities);
        intent.putExtra(ShowResultsActivity.SENTIMENT_PARAM_NAME, sentiment);
        startActivity(intent);
    }

    /**
     * Represents an asynchronous analysis task.
     * <p>{@code (text, language) -> success}</p>
     */
    private class AnalysisTask extends AsyncTask<String, Void, Boolean> {

        private Call<EntitiesResponse> entitiesCall;
        private Response<EntitiesResponse> entitiesResponse;

        private Call<SentimentResponse> sentimentCall;
        private Response<SentimentResponse> sentimentResponse;

        @Override
        protected Boolean doInBackground(final String... params) {
            // prepare the analysis request
            final String text = (params.length > 0) ? params[0] : null;
            final String language = (params.length > 1) ? params[1] : null;
            final Request request = new Request(text, language);

            try {
                // call the entity detection
                entitiesCall = geneeaAPI.findEntities(authorization, request);
                entitiesResponse = entitiesCall.execute();
                if (!entitiesResponse.isSuccess()) {
                    return false;
                }

                // call the sentiment analysis
                sentimentCall = geneeaAPI.detectSentiment(authorization, request);
                sentimentResponse = sentimentCall.execute();
                if (!sentimentResponse.isSuccess()) {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }

            // success
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                showResults(entitiesResponse.body(), sentimentResponse.body());
            } else {
                mTextToAnalyze.setError(getString(R.string.error_analysis_fail));
            }
            showProgress(false);
            analysisTask = null;
        }

        @Override
        protected void onCancelled() {
            if (entitiesCall != null) {
                entitiesCall.cancel();
            }
            if (sentimentCall != null) {
                sentimentCall.cancel();
            }
            showProgress(false);
            analysisTask = null;
        }
    }
}
