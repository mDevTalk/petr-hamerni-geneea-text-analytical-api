package com.geneea.mobile.demo;

import com.geneea.mobile.demo.model.EntitiesResponse;
import com.geneea.mobile.demo.model.SentimentResponse;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * An activity for showing the NLP analysis results.
 */
public class ShowResultsActivity extends AppCompatActivity {

    public static final String ENTITIES_PARAM_NAME = "com.geneea.mobile.demo.entities_response";
    public static final String SENTIMENT_PARAM_NAME = "com.geneea.mobile.demo.sentiment_response";

    private static final String POSITIVE = new String(Character.toChars(0x1F603)) + " positive";
    private static final String NEUTRAL = new String(Character.toChars(0x1F612)) + " neutral";
    private static final String NEGATIVE = new String(Character.toChars(0x1F620)) + " negative";

    private LayoutInflater mInflater;
    private TextView mSentiment;
    private LinearLayout mEntityList;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        mInflater = LayoutInflater.from(getBaseContext());
        mSentiment = (TextView) findViewById(R.id.sentiment);
        mEntityList = (LinearLayout) findViewById(R.id.entity_list);

        showResults();
    }

    private void showResults() {
        final EntitiesResponse entities = (EntitiesResponse) getIntent().getSerializableExtra(ENTITIES_PARAM_NAME);
        final SentimentResponse sentiment = (SentimentResponse) getIntent().getSerializableExtra(SENTIMENT_PARAM_NAME);

        for (EntitiesResponse.Entity entity : entities.entities) {
            addEntityRow(entity.type, entity.name);
        }
        final int val = sentiment.sentiment;
        mSentiment.setText(val > 0 ? POSITIVE : val < 0 ? NEGATIVE : NEUTRAL);
    }

    private void addEntityRow(final String type, final String name) {
        final View mView = mInflater.inflate(R.layout.list_entity_result, mEntityList, false);
        mEntityList.addView(mView);

        final TextView mType = (TextView) mView.findViewById(R.id.entity_type);
        final TextView mName = (TextView) mView.findViewById(R.id.entity_name);
        mType.setText(type);
        mName.setText(name);

        final ImageButton mMapBtn = (ImageButton) mView.findViewById(R.id.button_map);
        final ImageButton mPhoneBtn = (ImageButton) mView.findViewById(R.id.button_phone);
        mMapBtn.setVisibility("location".equals(type) ? View.VISIBLE : View.GONE);
        mPhoneBtn.setVisibility("person".equals(type) ? View.VISIBLE : View.GONE);

        mMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showMap(mName.getText().toString());
            }
        });
        mPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showPhone(mName.getText().toString());
            }
        });
    }

    private void showMap(final String location) {
        final Uri geoUri = Uri.parse("geo:0,0?q=" + location.replaceAll("\\s+", "+"));
        final Intent intent = new Intent(Intent.ACTION_VIEW, geoUri);
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    private void showPhone(final String person) {
        // TODO find a phone number for the person
        final String phoneNumber = person.substring(0, person.length() < 9 ? person.length() : 9);
        final Uri dialUri = Uri.parse("tel:" + phoneNumber);
        final Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(dialUri);
        startActivity(intent);
    }
}
