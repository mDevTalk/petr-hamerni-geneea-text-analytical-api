package com.geneea.mobile.demo.model;

import java.io.Serializable;

/**
 * Response for the <tt>/s2/sentiment</tt> method.
 */
public class SentimentResponse implements Serializable {

    private static final long serialVersionUID = 6860180090311275432L;

    public int sentiment;
    public String label;

}
