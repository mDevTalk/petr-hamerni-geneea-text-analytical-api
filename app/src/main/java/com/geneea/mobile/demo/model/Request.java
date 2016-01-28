package com.geneea.mobile.demo.model;

import java.io.Serializable;

/**
 * Request for the Geneea NLP REST API.
 */
public class Request implements Serializable {

    private static final long serialVersionUID = -5391408619294322350L;

    public String title = null;
    public String text = null;
    public String language = null;
    public boolean returnTextInfo = false;

    public Request() {}

    public Request(final String text, final String language) {
        this.text = text;
        this.language = language;
    }
}
