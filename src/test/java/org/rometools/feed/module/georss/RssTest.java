/*
 * Copyright 2006 Marc Wick, geonames.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.rometools.feed.module.georss;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.rometools.feed.module.georss.geometries.LineString;
import org.rometools.feed.module.georss.geometries.Position;
import org.rometools.feed.module.georss.geometries.PositionList;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

/**
 * tests for geo-rss module.
 * 
 */
public class RssTest extends TestCase {

    private static final double DELTA = 0.00001;

    /**
     * compares expected lat/lng values with acutal values read from feed.
     * 
     * @param fileName a file in the src/data directory
     * @param expectedLat array of expected latitude values
     * @param expectedLng array of expected longitude values
     * @throws Exception if file not found or not accessible
     */
    private void assertTestFile(final String fileName, final Double[] expectedLat, final Double[] expectedLng) throws Exception {
        assertTestInputStream(new FileInputStream(new File("src/test/resources/data/", fileName)), expectedLat, expectedLng);
    }

    /**
     * test expected latitude and longitude values in items of test file.
     * 
     * @param in testfeed
     * @param expectedLat expected latitude values
     * @param expectedLng expected longitude values
     * @throws Exception if file not found or not accessible
     */
    private void assertTestInputStream(final InputStream in, final Double[] expectedLat, final Double[] expectedLng) throws Exception {
        final SyndFeedInput input = new SyndFeedInput();

        final SyndFeed feed = input.build(new XmlReader(in));

        final List<SyndEntry> entries = feed.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            final SyndEntry entry = entries.get(i);
            final GeoRSSModule geoRSSModule = GeoRSSUtils.getGeoRSS(entry);
            final Position position = geoRSSModule.getPosition();
            if (expectedLat[i] == null || expectedLng[i] == null) {
                assertNull("position " + i, position);
            } else {
                assertEquals("lat " + i, expectedLat[i], position.getLatitude(), DELTA);
                assertEquals("lng " + i, expectedLng[i], position.getLongitude(), DELTA);
            }
        }
    }

    /**
     * @return implementation of SyndFeed with basic setup
     */
    private SyndFeed createFeed() {
        final SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");

        feed.setTitle("Sample Feed (created with ROME)");
        feed.setLink("http://rome.dev.java.net");
        feed.setDescription("This feed has been created using ROME (Java syndication utilities");

        final List<SyndEntry> entries = new ArrayList<SyndEntry>();
        SyndEntry entry;
        SyndContent description;

        entry = new SyndEntryImpl();
        entry.setTitle("ROME v1.0");
        entry.setLink("http://wiki.java.net/bin/view/Javawsxml/Rome01");
        entry.setPublishedDate(new Date());
        description = new SyndContentImpl();
        description.setType("text/plain");
        description.setValue("Initial release of ROME");
        entry.setDescription(description);
        entries.add(entry);

        feed.setEntries(entries);
        return feed;
    }

    /**
     * @throws Exception if file not found or not accessible
     */
    public void testReadRss2Simple() throws Exception {
        final Double[] lat = { 31.7666667, 31.7666667, 31.7666667 };
        final Double[] lng = { 35.2333333, 35.2333333, 35.2333333 };
        assertTestFile("rss2.0-simple.xml", lat, lng);
    }

    /**
     * @throws Exception if file not found or not accessible
     */
    public void testReadRss2W3CGeo() throws Exception {
        final Double[] lat = { 42.986284, 41.492491, 39.901893, 40.750598 };
        final Double[] lng = { -71.45156, -72.095783, -75.171998, -73.993392 };
        assertTestFile("rss2.0-w3cgeo.xml", lat, lng);
    }

    /**
     * @throws Exception if file not found or not accessible
     */
    public void xtestReadRss1W3CGeo() throws Exception {
        final Double[] lat = { 45.9774, 45.9707, 46.0087 };
        final Double[] lng = { 8.9781, 8.97055, 8.98579 };
        assertTestFile("rss1.0-w3cgeo.xml", lat, lng);
    }

    /**
     * @throws Exception if file not found or not accessible
     */
    public void testReadGML() throws Exception {
        final Double[] lat = { 45.256, 45.256, 45.256, 45.256 };
        final Double[] lng = { -71.92, -71.92, -71.92, -71.92 };
        assertTestFile("rss2.0-gml.xml", lat, lng);
    }

    /**
     * @throws Exception if file not found or not accessible
     */
    public void testNpeOnEmptyPoint() throws Exception {
        final Double[] lat = { null, 0.0, 0.0, 0.0, 43.653226, 0.0, 0.0, 0.0, 0.0, 0.0 };
        final Double[] lng = { null, 0.0, 0.0, 0.0, -79.383184, 0.0, 0.0, 0.0, 0.0, 0.0 };
        assertTestFile("geo-rss-issue-27.rss", lat, lng);
    }

    /**
     * @throws Exception if file not found or not accessible
     */
    public void xtestProduceAndReadGML() throws Exception {
        final SyndFeed feed = createFeed();
        final GeoRSSModule geoRSSModule = new GMLModuleImpl();
        final double latitude = 47.0;
        final double longitude = 9.0;
        final Position position = new Position();
        position.setLatitude(latitude);
        position.setLongitude(longitude);
        geoRSSModule.setPosition(position);

        final SyndEntry entry = feed.getEntries().get(0);
        entry.getModules().add(geoRSSModule);

        final SyndFeedOutput output = new SyndFeedOutput();

        final StringWriter stringWriter = new StringWriter();
        output.output(feed, stringWriter);

        final InputStream in = new ByteArrayInputStream(stringWriter.toString().getBytes("UTF8"));
        final Double[] lat = { latitude };
        final Double[] lng = { longitude };
        assertTestInputStream(in, lat, lng);
    }

    /**
     * @throws Exception if file not found or not accessible
     */
    public void testProduceAndReadSimple() throws Exception {
        final SyndFeed feed = createFeed();
        final GeoRSSModule geoRSSModule = new SimpleModuleImpl();
        final double latitude = 47.0;
        final double longitude = 9.0;
        final Position position = new Position();
        position.setLatitude(latitude);
        position.setLongitude(longitude);
        geoRSSModule.setPosition(position);

        final SyndEntry entry = feed.getEntries().get(0);
        entry.getModules().add(geoRSSModule);

        final SyndFeedOutput output = new SyndFeedOutput();

        final StringWriter stringWriter = new StringWriter();
        output.output(feed, stringWriter);

        final InputStream in = new ByteArrayInputStream(stringWriter.toString().getBytes("UTF8"));
        final Double[] lat = { latitude };
        final Double[] lng = { longitude };
        assertTestInputStream(in, lat, lng);
    }

    /**
     * @throws Exception if file not found or not accessible
     */
    public void testProduceAndReadSimpleLine() throws Exception {
        SyndFeed feed = createFeed();

        final double[] latitudes = new double[] { 45.256, 46.46, 43.84 };
        final double[] longitudes = new double[] { -110.45, -109.48, -109.86 };
        // 45.256 -110.45 46.46 -109.48 43.84 -109.86
        PositionList positionList = new PositionList();
        positionList.add(latitudes[0], longitudes[0]);
        positionList.add(latitudes[1], longitudes[1]);
        positionList.add(latitudes[2], longitudes[2]);

        SimpleModuleImpl geoRSSModule = new SimpleModuleImpl();
        geoRSSModule.setGeometry(new LineString(positionList));

        SyndEntry entry = feed.getEntries().get(0);
        entry.getModules().add(geoRSSModule);

        final SyndFeedOutput output = new SyndFeedOutput();

        final StringWriter stringWriter = new StringWriter();
        output.output(feed, stringWriter);

        final InputStream in = new ByteArrayInputStream(stringWriter.toString().getBytes("UTF8"));
        final SyndFeedInput input = new SyndFeedInput();

        feed = input.build(new XmlReader(in));

        final List<SyndEntry> entries = feed.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            entry = entries.get(i);
            geoRSSModule = (SimpleModuleImpl) GeoRSSUtils.getGeoRSS(entry);
            final LineString lineString = (LineString) geoRSSModule.getGeometry();
            positionList = lineString.getPositionList();

            assertEquals(latitudes[i], positionList.getLatitude(i), DELTA);
            assertEquals(longitudes[i], positionList.getLongitude(i), DELTA);
        }
    }

}
