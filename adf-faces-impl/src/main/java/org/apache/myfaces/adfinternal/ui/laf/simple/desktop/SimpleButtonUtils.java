/*
 * Copyright  2004-2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myfaces.adfinternal.ui.laf.simple.desktop;

import java.awt.Color;

import java.io.IOException;

import javax.faces.context.FacesContext;
import org.apache.myfaces.adf.logging.ADFLogger;

import org.apache.myfaces.adfinternal.renderkit.AdfRenderingContext;
import org.apache.myfaces.adfinternal.share.io.InputStreamProvider;

import org.apache.myfaces.adfinternal.share.nls.LocaleContext;
import org.apache.myfaces.adfinternal.style.util.FontProxy;

import org.apache.myfaces.adfinternal.image.ImageProviderRequest;
import org.apache.myfaces.adfinternal.image.cache.CompositeButtonKey;

import org.apache.myfaces.adfinternal.ui.RenderingContext;
import org.apache.myfaces.adfinternal.skin.Skin;
import org.apache.myfaces.adfinternal.skin.icon.Icon;
import org.apache.myfaces.adfinternal.util.nls.LocaleUtils;

/**
 * Package-private utilities shared by SLAF button Renderers.
 *
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/ui/laf/simple/desktop/SimpleButtonUtils.java#0 $) $Date: 10-nov-2005.18:51:25 $
 * @author The Oracle ADF Faces Team
 */
class SimpleButtonUtils implements SimpleDesktopConstants
{
  // Returns the name of the named style for button styling.
  static public String getButtonStyleName(boolean disabled)
  {
    return disabled ? _DISABLED_STYLE_NAME : _STYLE_NAME;
  }

  // Tests whether button should be rendered as an image.
  // SLAF buttons are rendered using images if all three
  // button icons (start, end, background) specified.
  // Otherwise the button is rendered using the base.desktop
  // button implementation (browser buttons).
  static public boolean doRenderImageButton(
    RenderingContext context
    )
  {
    // First, check the _IMAGE_BUTTON_KEY (or _IMAGE_BUTTON_RTL_KEY if rtl)
    // property on the Skin.
    //   This will be Boolean.TRUE if
    // we have all three icons, Boolean.FALSE if we
    // don't, or null if we haven't checked yet.
    boolean rtl = _isRightToLeft(context);
    Boolean value;
    Skin skin = context.getSkin();
    if (rtl)
      value = (Boolean)skin.getProperty(_IMAGE_BUTTON_RTL_KEY);
    else
      value = (Boolean)skin.getProperty(_IMAGE_BUTTON_KEY);

    if (value != null)
      return (Boolean.TRUE == value);

    // we fetch different icons if we are in the
    // right-to-left reading direction. context.getIcon takes care of
    // this, by adding the :rtl suffix to the icon name if the
    // reading direction is rtl.
    Icon startIcon = context.getIcon(
                                  BUTTON_START_ICON_NAME);
    Icon endIcon = context.getIcon(
                                  BUTTON_END_ICON_NAME);
    Icon topBackgroundIcon = context.getIcon(
                                  BUTTON_TOP_BACKGROUND_ICON_NAME);
    Icon bottomBackgroundIcon = context.getIcon(
                                  BUTTON_BOTTOM_BACKGROUND_ICON_NAME);
    // List of missing icons
    String missing = null;

    if (startIcon == null)
      missing = _addMissingIcon(missing, BUTTON_START_ICON_NAME);
    if (endIcon == null)
      missing = _addMissingIcon(missing, BUTTON_END_ICON_NAME);
    if (topBackgroundIcon == null)
      missing = _addMissingIcon(missing, BUTTON_TOP_BACKGROUND_ICON_NAME);
    if (bottomBackgroundIcon == null)
      missing = _addMissingIcon(missing, BUTTON_BOTTOM_BACKGROUND_ICON_NAME);
    // If we are missing any of the icons, we don't render
    // the button image.
    if (missing != null)
    {
      // Only bother logging a message if one or more of the button
      // icons were actually specified.  If no button icons were
      // specified, than the user probably just wants to use
      // browser-based buttons.

      if ((startIcon != null)         ||
          (endIcon != null)           ||
          (topBackgroundIcon != null) ||
          (bottomBackgroundIcon != null))
      {
        if (_LOG.isWarning())
        {
          if (rtl)
            missing += "(Add :rtl to the icon names since locale is rtl)";
          _LOG.warning(_MISSING_ICON_ERROR + missing);
        }
      }

      if (rtl)
        skin.setProperty(_IMAGE_BUTTON_RTL_KEY, Boolean.FALSE);
      else
        skin.setProperty(_IMAGE_BUTTON_KEY, Boolean.FALSE);

      return false;
    }
    if (rtl)
      skin.setProperty(_IMAGE_BUTTON_RTL_KEY, Boolean.TRUE);
    else
      skin.setProperty(_IMAGE_BUTTON_KEY, Boolean.TRUE);

    return true;
  }

  // Creates the ImageProviderRequest for the composite button lookup
  static public ImageProviderRequest createButtonRequest(
    RenderingContext context,
    Object       name,
    Object       text,
    Color        foreground,
    Color        background,
    Color        surroundingColor,
    FontProxy    font,
    boolean      disabled,
    boolean      textAntialias,
    char         accessKey
    )
  {
    AdfRenderingContext arc = AdfRenderingContext.getCurrentInstance();
    FacesContext fContext = context.getFacesContext();
    return new SimpleButtonUtils.Key(
                        fContext,
                        arc,
                        context,
                        context.getSkin().getId(),
                        (name != null)
                          ? name.toString()
                          : null,
                        (text != null)
                          ? text.toString()
                          : null,
                        foreground,
                        background,
                        surroundingColor,
                        font,
                        disabled,
                        textAntialias,
                        accessKey);
  }

  // Add an icon name to the list of missing icons
  private static String _addMissingIcon(
    String missing,
    String iconName
    )
  {
    if (missing == null)
      return iconName;

    return missing + ", " + iconName;
  }

  // Our private ButtonKey implementation.  It provides public accessor
  // methods to set the textAntialias and accessKey values.
  private static class Key extends CompositeButtonKey
  {
    public Key(
      FacesContext fContext,
      AdfRenderingContext arc,
      RenderingContext context,
      String       skinId,
      String       name,
      String       text,
      Color        foreground,
      Color        background,
      Color        surroundingColor,
      FontProxy    font,
      boolean      disabled,
      boolean      textAntialias,
      char         accessKey
      )
    {
      super(context.getImageContext(),
            skinId,
            name,
            text,
            foreground,
            background,
            surroundingColor,
            font,
            disabled,
            textAntialias,
            accessKey,
            null,
            null,
            null,
            null);

      _fContext = fContext;
      _arc = arc;
      _context = context;

    }

    // Override of getStartIcon() which retrieves
    // the InputStreamProvider from the Icon.
    protected InputStreamProvider getStartIcon()
    {
      return _getIconData(BUTTON_START_ICON_NAME,
                          BUTTON_DISABLED_START_ICON_NAME);
    }

    // Override of getEndIcon() which retrieves
    // the InputStreamProvider from the Icon.
    protected InputStreamProvider getEndIcon()
    {
      return _getIconData(BUTTON_END_ICON_NAME,
                          BUTTON_DISABLED_END_ICON_NAME);
    }

    // Override of getTopBackgroundIcon() which retrieves
    // the InputStreamProvider from the Icon.
    protected InputStreamProvider getTopBackgroundIcon()
    {
      return _getIconData(BUTTON_TOP_BACKGROUND_ICON_NAME,
                          BUTTON_DISABLED_TOP_BACKGROUND_ICON_NAME);
    }

    // Override of getBottomBackgroundIcon() which retrieves
    // the InputStreamProvider from the Icon.
    protected InputStreamProvider getBottomBackgroundIcon()
    {
      return _getIconData(BUTTON_BOTTOM_BACKGROUND_ICON_NAME,
                          BUTTON_DISABLED_BOTTOM_BACKGROUND_ICON_NAME);
    }


    private InputStreamProvider _getIconData(
      String iconName,
      String disabledIconName
      )
    {

      Icon icon = null;

      // If we're disabled, try looking for the disabled version
      // of the Icon
      if (isDisabled())
        icon = _context.getIcon(disabledIconName);

      // If we're not disabled, or if we don't have a disabled
      // version of the Icon, just use the enabled version
      if (icon == null)
        icon = _context.getIcon(iconName);

      if (icon != null)
      {
        try
        {
          return icon.getImageData(_fContext, _arc);
        }
        catch (IOException e)
        {
          _LOG.severe(e);
        }
      }

      return null;
    }

    private RenderingContext _context;
    private AdfRenderingContext _arc;
    private FacesContext _fContext;

  }


  private static boolean _isRightToLeft(RenderingContext context)
  {
    LocaleContext lContext = context.getLocaleContext();

    return (LocaleUtils.DIRECTION_RIGHTTOLEFT ==
            lContext.getReadingDirection());
  }

  // Key for Boolean Skin property which we use
  // to track whether buttons should be rendered as images.
  private static Object _IMAGE_BUTTON_KEY = new Object();
  private static Object _IMAGE_BUTTON_RTL_KEY = new Object();

  // Server style class names
  private static final String _STYLE_NAME =
    "AFButtonServerText";
  private static final String _DISABLED_STYLE_NAME =
    "AFButtonServerTextDisabled";

  // Error messasges
  private static final String _MISSING_ICON_ERROR =
    "Unable to generate composite button images.  Missing button icons for: ";
  private static final ADFLogger _LOG = ADFLogger.createADFLogger(SimpleButtonUtils.class);
}

