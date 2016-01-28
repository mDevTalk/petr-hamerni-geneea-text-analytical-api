package com.geneea.mobile.demo.model;

import java.io.Serializable;
import java.util.List;

/**
 * Response for the <tt>/s2/entities</tt> method.
 */
public class EntitiesResponse implements Serializable {

    private static final long serialVersionUID = -444186697228683501L;

    public List<Entity> entities;

    public static class Entity implements Serializable {

        private static final long serialVersionUID = 6860982992045561108L;

        public String name;
        public String type;
    }
}
