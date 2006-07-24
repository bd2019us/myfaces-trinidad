/*
 * Copyright  2003-2006 The Apache Software Foundation.
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

package org.apache.myfaces.adfinternal.renderkit.core.skin;



import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;
import org.apache.myfaces.adfinternal.agent.AdfFacesAgent;

import org.apache.myfaces.adfinternal.renderkit.AdfRenderingContext;
import org.apache.myfaces.adfinternal.skin.icon.Icon;

/**
 * An Icon implementation which switches between a Mac OS-specific
 * Icon and a default Icon.
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/renderkit/core/skin/MacOSSwitcherIcon.java#0 $) $Date: 10-nov-2005.19:02:51 $
 * @author The Oracle ADF Faces Team
 */
class MacOSSwitcherIcon extends Icon
{
  public MacOSSwitcherIcon(
    Icon icon,
    Icon macOSIcon
    )
  {
    if ((icon == null)||(macOSIcon == null))
    {
      throw new NullPointerException("Null argument");
    }

    _icon = icon;
    _macOSIcon = macOSIcon;
  }

  /**
   * Override of Icon.renderIcon().
   */
  public void renderIcon(
    FacesContext        context,
    AdfRenderingContext arc,
    Map              attrs
    ) throws IOException
  {
    Icon icon = _getIcon(arc);

    icon.renderIcon(context, arc, attrs);
  }

  /**
   * Override of Icon.getImageURI().
   */
  public Object getImageURI(
    FacesContext        context,
    AdfRenderingContext arc)
  {
    Icon icon = _getIcon(arc);

    return icon.getImageURI(context, arc);
  }

  /**
   * Override of Icon.getImageWidth().
   */
  public Integer getImageWidth(AdfRenderingContext arc)
  {
    Icon icon = _getIcon(arc);

    return icon.getImageWidth(arc);
  }

  /**
   * Override of Icon.getImageHeight().
   */
  public Integer getImageHeight(AdfRenderingContext arc)
  {
    Icon icon = _getIcon(arc);

    return icon.getImageHeight(arc);
  }

  // Returns the Icon to use
  private Icon _getIcon(AdfRenderingContext arc)
  {
    return (arc.getAgent().getAgentOS() == AdfFacesAgent.OS_MACOS) ?
             _macOSIcon :
             _icon;
  }

  private Icon _icon;
  private Icon _macOSIcon;
}
