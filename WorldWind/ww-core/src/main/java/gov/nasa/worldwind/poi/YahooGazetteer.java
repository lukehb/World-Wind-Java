/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.poi;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;

/**
 * A gazetteer that uses Yahoo's geocoding service to find locations for requested places.
 *
 * @author tag
 * @version $Id$
 */
public class YahooGazetteer implements Gazetteer, ReverseLookupGazetteer
{
    protected static final String GEOCODE_SERVICE =
        "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.placefinder%20where%20text%3D";

    @Override
    public List<PointOfInterest> findPlaces(String lookupString) throws NoItemException, ServiceException
    {
        if (lookupString == null || lookupString.length() < 1)
        {
            return null;
        }

        String urlString;
        try
        {
            urlString = GEOCODE_SERVICE + "%22" + URLEncoder.encode(lookupString, "UTF-8") + "%22";
        }
        catch (UnsupportedEncodingException e)
        {
            urlString = GEOCODE_SERVICE + "%22" + lookupString.replaceAll(" ", "+") + "%22";
        }

        if (isNumber(lookupString))
            lookupString += "%20and%20gflags%3D%22R%22";

        String locationString = POIUtils.callService(urlString);

        if (locationString == null || locationString.length() < 1)
        {
            return null;
        }

        return this.parseLocationString(locationString);
    }

    @Override
    public List<PointOfInterest> findPlaces(LatLon latlon) throws NoItemException, ServiceException
    {
        if (latlon == null)
        {
            return null;
        }
        
        String latStr = String.format("%.6f", latlon.getLatitude().getDegrees());
        String lonStr = String.format("%.6f", latlon.getLongitude().getDegrees());
        String urlString = GEOCODE_SERVICE + "%22" + latStr + "%2C%20" + lonStr + "%22" + "%20and%20gflags%3D%22R%22";
        
        String locationString = POIUtils.callService(urlString);
        
        if (locationString == null || locationString.length() < 1)
        {
            return null;
        }
        
        return this.parseLocationString(locationString);
    }

    protected boolean isNumber(String lookupString)
    {
        lookupString = lookupString.trim();

        return lookupString.startsWith("-") || lookupString.startsWith("+") || Character.isDigit(lookupString.charAt(0));
    }

    protected List<PointOfInterest> parseLocationString(String locationString) throws WWRuntimeException
    {
        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(false);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.parse(new ByteArrayInputStream(locationString.getBytes("UTF-8")));

            XPathFactory xpFactory = XPathFactory.newInstance();
            XPath xpath = xpFactory.newXPath();

            org.w3c.dom.NodeList resultNodes =
                (org.w3c.dom.NodeList) xpath.evaluate("/query/results/Result", doc, XPathConstants.NODESET);

            List<PointOfInterest> positions = new ArrayList<PointOfInterest>(resultNodes.getLength());

            for (int i = 0; i < resultNodes.getLength(); i++)
            {
                org.w3c.dom.Node location = resultNodes.item(i);
                String lat = xpath.evaluate("latitude", location);
                String lon = xpath.evaluate("longitude", location);
                StringBuilder displayName = new StringBuilder();
                
                String name = xpath.evaluate("name", location);
                if (name != null && !name.equals("") && !name.equals(lat + ", " + lon))
                {
                    displayName.append(name);
                    displayName.append(", ");
                }
                
                String house = xpath.evaluate("house", location);
                if (house != null && !house.equals(""))
                {
                    displayName.append(house);
                    displayName.append(" ");
                }

                String street = xpath.evaluate("street", location);
                if (street != null && !street.equals(""))
                {
                    displayName.append(street);
                    displayName.append(", ");
                }
                
                String neighborhood = xpath.evaluate("neighborhood", location);
                if (neighborhood != null && !neighborhood.equals(""))
                {
                    displayName.append(neighborhood);
                    displayName.append(", ");
                }

                String city = xpath.evaluate("city", location);
                if (city != null && !city.equals(""))
                {
                    displayName.append(city);
                    displayName.append(", ");
                }
                
                String county = xpath.evaluate("county", location);
                if (county != null && !county.equals(""))
                {
                    displayName.append(county);
                    displayName.append(", ");
                }
                
                String state = xpath.evaluate("state", location);
                if (state != null && !state.equals(""))
                {
                    displayName.append(state);
                    displayName.append(", ");
                }
                
                String country = xpath.evaluate("country", location);
                displayName.append(country);

                if (lat != null && lon != null)
                {
                    LatLon latlon = LatLon.fromDegrees(Double.parseDouble(lat), Double.parseDouble(lon));
                    PointOfInterest loc = new BasicPointOfInterest(latlon);
                    loc.setValue(AVKey.DISPLAY_NAME, displayName.toString());
                    positions.add(loc);
                }
            }

            return positions;
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("Gazetteer.URLException", locationString);
            Logging.logger().log(Level.SEVERE, msg);
            throw new WWRuntimeException(msg);
        }
    }
}
