/*
 * A schema manager for the workspace that works in either editing mode.
 */

package de.wwu.scdh.oxbytei.commons;

import java.util.List;

import ro.sync.contentcompletion.xml.WhatAttributesCanGoHereContext;
import ro.sync.contentcompletion.xml.CIAttribute;

/**
 * A schema manager for the workspace that works in either editing mode.
 *
 * @author Christian Lück
 */
public interface WSSchemaManager {

    public List<CIAttribute> whatAttributesCanGoHere(WhatAttributesCanGoHereContext context);

    public List<CIAttribute> whatAttributesCanGoHere(int offset);

    public List<CIAttribute> whatAttributesCanGoHere(String locationXPath);

    public List<CIAttribute> whatAttributesCanGoHere();

    public List<CIAttribute> whatAttributesAreHere(int offset);

    public List<CIAttribute> whatAttributesAreHere(String locationXPath);

    public List<CIAttribute> whatAttributesAreHere();

}
