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
package com.omertron.themoviedbapi.model.person;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.omertron.themoviedbapi.enumeration.Gender;
import com.omertron.themoviedbapi.model.AbstractIdName;
import java.io.Serializable;

/**
 * @author stuart.boston
 */
public class PersonBasic extends AbstractIdName implements Serializable {

    private static final long serialVersionUID = 100L;

    @JsonProperty("profile_path")
    private String profilePath;
    @JsonProperty("gender")
    private Gender gender;

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }

    public Gender getGender() {
        return gender;
    }

    @JsonSetter("gender")
    public void setGender(int gender) {
        this.gender = Gender.fromInteger(gender);
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

}
