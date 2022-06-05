* [api-themoviedb](https://github.com/Omertron/api-themoviedb)
  - forked from:
    * commit: [34b337aecd058847da704be19cb40ab60f6ccf33](https://github.com/Omertron/api-themoviedb/tree/34b337aecd058847da704be19cb40ab60f6ccf33)
    * snapshot: [zip archive](https://github.com/Omertron/api-themoviedb/archive/34b337aecd058847da704be19cb40ab60f6ccf33.zip)
    * date: 2020-11-11
  - replaces:
    * the most recent stable build of the dependency:
      - 'com.omertron:themoviedbapi:4.3'
  - changes:
    * adds fix for: [issue 54](https://github.com/Omertron/api-themoviedb/issues/54)
    * removes unwanted transitive dependencies:
      - any variation of Apache HttpClient:
        * 'org.apache.http.legacy'
        * 'org.apache.httpcomponents:httpclient:4.5.13'
        * 'org.apache.httpcomponents:httpclient-android:4.3.5.1'
        * etc..
      - logging:
        * 'org.slf4j:slf4j-api:1.7.36'
        * 'org.slf4j:slf4j-nop:1.7.36'
* [org-yamj-api-common](https://github.com/YAMJ/api-common)
  - forked from:
    * tag: [api-common-2.1](https://github.com/YAMJ/api-common/tree/api-common-2.1)
    * commit: [5db1f370f7d1b9693499f6331798ff2b4bd6cb36](https://github.com/YAMJ/api-common/tree/5db1f370f7d1b9693499f6331798ff2b4bd6cb36)
    * snapshot: [zip archive](https://github.com/YAMJ/api-common/archive/5db1f370f7d1b9693499f6331798ff2b4bd6cb36.zip)
    * date: 2017-03-16
  - replaces:
    * the most recent stable build of the dependency:
      - 'org.yamj:api-common:2.1'
  - changes:
    * removes unwanted transitive dependencies:
      - any variation of Apache HttpClient:
        * 'org.apache.http.legacy'
        * 'org.apache.httpcomponents:httpclient:4.5.13'
        * 'org.apache.httpcomponents:httpclient-android:4.3.5.1'
        * etc..
      - logging:
        * 'org.slf4j:slf4j-api:1.7.36'
        * 'org.slf4j:slf4j-nop:1.7.36'
