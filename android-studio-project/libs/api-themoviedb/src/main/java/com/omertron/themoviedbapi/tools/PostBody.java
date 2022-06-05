/*
 *      Copyright (c) 2004-2016 Stuart Boston
 *
 *      This file is part of TheMovieDB API.
 *
 *      TheMovieDB API is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      TheMovieDB API is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with TheMovieDB API.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.omertron.themoviedbapi.tools;

/**
 * List of values to use for the POST requests
 *
 * @author Stuart.Boston
 */
public enum PostBody {

    MEDIA_ID("media_id"),
    MEDIA_TYPE("media_type"),
    FAVORITE("favorite"),
    WATCHLIST("watchlist"),
    NAME("name"),
    DESCRIPTION("description"),
    VALUE("value");

    private final String value;

    private PostBody(String value) {
        this.value = value;
    }

    /**
     * Get the URL parameter to use
     *
     * @return value
     */
    public String getValue() {
        return this.value;
    }
}
