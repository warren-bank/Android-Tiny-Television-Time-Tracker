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

// -------------------------------------------------------------------------
// compatible with:
//   https://github.com/Omertron/api-themoviedb/blob/themoviedbapi-4.3/src/main/java/com/omertron/themoviedbapi/tools/HttpTools.java
// -------------------------------------------------------------------------
// based on:
//   https://stackoverflow.com/a/31357311
//   https://github.com/YAMJ/api-common/blob/master/src/main/java/org/yamj/api/common/http/DigestedResponse.java
//   https://github.com/YAMJ/api-common/blob/master/src/main/java/org/yamj/api/common/http/DigestedResponseReader.java
// -------------------------------------------------------------------------
// cross references:
//   https://github.com/apache/httpcomponents-core/blob/master/httpcore5/src/main/java/org/apache/hc/core5/http/HttpStatus.java
// -------------------------------------------------------------------------

package com.omertron.themoviedbapi.tools;

import com.omertron.themoviedbapi.MovieDbException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.yamj.api.common.exception.ApiExceptionType;

/**
 * HTTP tools to aid in processing web requests
 *
 * @author Stuart.Boston
 */
public class HttpTools {

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final String APPLICATION_JSON = "application/json";
    private static final int CONNECTION_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 15000;
    private static final long RETRY_DELAY = 1;
    private static final int RETRY_MAX = 5;
    private static final int SC_MULTIPLE_CHOICES = 300;
    private static final int SC_TOO_MANY_REQUESTS = 429;
    private static final int SC_INTERNAL_SERVER_ERROR = 500;
    private static final int SC_SERVICE_UNAVAILABLE = 503;
    private static final int SW_BUFFER_10K = 10240;

    public HttpTools() {
    }

    /**
     * GET data from the URL
     *
     * @param url URL to use in the request
     * @return String content
     * @throws MovieDbException exception
     */
    public String getRequest(final URL url) throws MovieDbException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", APPLICATION_JSON);

        DigestedResponse response = requestContent(url, "GET", headers, /* String body */ null, /* int retryCount */ 0);
        return validateResponse(response, url);
    }

    /**
     * Execute a DELETE on the URL
     *
     * @param url URL to use in the request
     * @return String content
     * @throws MovieDbException exception
     */
    public String deleteRequest(final URL url) throws MovieDbException {
        DigestedResponse response = requestContent(url, "DELETE", /* Map headers */ null, /* String body */ null, /* int retryCount */ 0);
        return validateResponse(response, url);
    }

    /**
     * POST content to the URL with the specified body
     *
     * @param url URL to use in the request
     * @param jsonBody Body to use in the request
     * @return String content
     * @throws MovieDbException exception
     */
    public String postRequest(final URL url, final String jsonBody) throws MovieDbException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", APPLICATION_JSON);
        headers.put("Accept",       APPLICATION_JSON);

        DigestedResponse response = requestContent(url, "POST", headers, jsonBody, 0);
        return validateResponse(response, url);
    }

    private class DigestedResponse {
        private int statusCode;
        private String content;

        public DigestedResponse(final int statusCode, final String content) {
            this.statusCode = statusCode;
            this.content    = content;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getContent() {
            return content;
        }
    }

    private DigestedResponse requestContent(final URL url, final String method, final Map<String, String> headers, final String body, int retryCount) {
      int statusCode = SC_SERVICE_UNAVAILABLE;
      String content = "";

      try {
          boolean doOutput = ((body != null) && !body.isEmpty());

          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setConnectTimeout(CONNECTION_TIMEOUT);
          conn.setReadTimeout(READ_TIMEOUT);
          conn.setRequestMethod(method);
          conn.setDoOutput(doOutput);
          conn.setDoInput(true);

          if (headers != null) {
            for(Map.Entry<String, String> entry : headers.entrySet()){
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
          }

          if (doOutput) {
              OutputStream os = conn.getOutputStream();
              OutputStreamWriter osw = new OutputStreamWriter(os, CHARSET);
              BufferedWriter writer = new BufferedWriter(osw);
              writer.write(body);
              writer.flush();
              writer.close();
              os.close();
          }

          statusCode = conn.getResponseCode();

          if (statusCode == SC_TOO_MANY_REQUESTS) {
              retryCount++;

              if (retryCount <= RETRY_MAX) {
                  delay(retryCount);
                  return requestContent(url, method, headers, body, retryCount);
              }
          }
          else if ((statusCode >= 200) && (statusCode < 300)) {
              StringWriter contentWriter = new StringWriter(SW_BUFFER_10K);
              InputStreamReader isr = new InputStreamReader(conn.getInputStream(), CHARSET);
              BufferedReader br = new BufferedReader(isr);
              String line = br.readLine();
              while (line != null) {
                  contentWriter.write(line);
                  line = br.readLine();
              }
              contentWriter.flush();
              content = contentWriter.toString();
          }
      }
      catch(Exception e) {
          statusCode = SC_SERVICE_UNAVAILABLE;
          content = "";
      }
      return new DigestedResponse(statusCode, content);
    }

    /**
     * Sleep for a period of time
     *
     * @param multiplier number of seconds to use for delay
     */
    private void delay(long multiplier) {
        try {
            // Wait for the timeout to finish
            Thread.sleep(TimeUnit.SECONDS.toMillis(RETRY_DELAY * multiplier));
        } catch (InterruptedException ex) {
            // Doesn't matter if we're interrupted
        }
    }

    /**
     * Check the status codes of the response and throw exceptions if needed
     *
     * @param response DigestedResponse to process
     * @param url URL for notification purposes
     * @return String content
     * @throws MovieDbException exception
     */
    private String validateResponse(final DigestedResponse response, final URL url) throws MovieDbException {
        if (response.getStatusCode() == 0) {
            throw new MovieDbException(ApiExceptionType.CONNECTION_ERROR, response.getContent(), response.getStatusCode(), url, null);
        } else if (response.getStatusCode() >= SC_INTERNAL_SERVER_ERROR) {
            throw new MovieDbException(ApiExceptionType.HTTP_503_ERROR, response.getContent(), response.getStatusCode(), url, null);
        } else if (response.getStatusCode() >= SC_MULTIPLE_CHOICES) {
            throw new MovieDbException(ApiExceptionType.HTTP_404_ERROR, response.getContent(), response.getStatusCode(), url, null);
        }

        return response.getContent();
    }

}
