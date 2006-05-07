/*
 * Copyright  2005,2006 The Apache Software Foundation.
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
package org.apache.myfaces.adfinternal.renderkit.core.xhtml;

import java.io.IOException;

import java.text.Bidi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.adf.bean.FacesBean;
import org.apache.myfaces.adf.bean.PropertyKey;
import org.apache.myfaces.adf.component.UIXHierarchy;
import org.apache.myfaces.adf.component.core.nav.CoreCommandLink;
import org.apache.myfaces.adf.component.core.nav.CoreNavigationPath;

import org.apache.myfaces.adfinternal.renderkit.AdfRenderingContext;
import org.apache.myfaces.adfinternal.renderkit.core.CoreAdfRenderingContext;
import org.apache.myfaces.adfinternal.skin.icon.Icon;

public class NavigationPathRenderer extends XhtmlRenderer
{
  public NavigationPathRenderer()
  {
    super(CoreNavigationPath.TYPE);
  }
  
  protected void findTypeConstants(FacesBean.Type type)
  {
    super.findTypeConstants(type);
    _orientationKey = type.findKey("orientation");
  }


  public boolean getRendersChildren()
  {
    return true;
  }
  
  protected void encodeAll(
    FacesContext        context,
    AdfRenderingContext arc,
    UIComponent         component,
    FacesBean           bean) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    
    writer.startElement("span", component);
    renderAllAttributes(context, arc, bean);
    renderId(context, component);
        
    int renderedItemCount = _getItemCount((UIXHierarchy)component);
    int minItemCount = _getMinItemCount(arc);

    // no kids, no NavigationPath -- but you still get the span.
    if (renderedItemCount > minItemCount )
    {
      renderContent(context, arc, 
                    (UIXHierarchy)component, bean);
    }
    
    writer.endElement("span");
  }

  /**
   * Gets the stamp to use to render each link
   */
  protected UIComponent getStamp(
    FacesContext        context,
    AdfRenderingContext arc,
    UIXHierarchy        component,
    FacesBean           bean
    )
  {
    UIComponent stamp = component.getFacet("nodeStamp");   
    return stamp;
  }

  protected void renderContent(
    FacesContext        context,
    AdfRenderingContext arc,
    UIXHierarchy        component,
    FacesBean           bean
    ) throws IOException
  {

    boolean isVertical = _isVertical(bean);
    boolean shouldRenderLastChild = shouldRenderLastChild(arc);    
    boolean isLastChild   = false;
    boolean isFirstChild  = true;
    int renderedCount     = 0;
    int nextVisChildIndex = 1;
    UIComponent stamp = getStamp(context, arc, component, bean);
    Icon separatorIcon = arc.getIcon(
                            XhtmlConstants.AF_NAVIGATION_PATH_SEPARATOR_ICON_NAME); 
 
    // use the focusKey to stamp out path
    if(stamp != null)
    {
      // Save the current key
      Object oldPath = component.getRowKey();
      Object focusPath = _getFocusRowKey(component);
      if (focusPath == null)
        return;
      
      List paths = new ArrayList(component.getAllAncestorContainerRowKeys(focusPath));
      paths.add(focusPath);
      int size = paths.size();        

      for (int i = 0; i < size; i++)
      {

        // todo - I'm not calculating whether the item is visible or not
        if ( i + 1 == size)
          nextVisChildIndex=NO_CHILD_INDEX;
        else
          nextVisChildIndex = i + 1;
        
        isLastChild = (nextVisChildIndex == NO_CHILD_INDEX);  
        component.setRowKey(paths.get(i));

        renderNode(context, arc, separatorIcon, stamp, renderedCount,
                   shouldRenderLastChild, isFirstChild, isLastChild, isVertical);
        renderedCount++;
        isFirstChild = false;
        
      }

      // Restore the old path
      component.setRowKey(oldPath);
    }

    // now render children
    if (hasChildren(component))
    {
      List children = component.getChildren();
      nextVisChildIndex = getNextRenderedChildIndex(children, -1);
      while (nextVisChildIndex != NO_CHILD_INDEX)
      {
        UIComponent child = (UIComponent)children.get(nextVisChildIndex);        
        nextVisChildIndex = getNextRenderedChildIndex(children, 
                                                      nextVisChildIndex);
        isLastChild = (nextVisChildIndex == NO_CHILD_INDEX);           
        renderNode(context, arc, separatorIcon, child, renderedCount,
                   shouldRenderLastChild, isFirstChild, isLastChild, isVertical);
        renderedCount++;           
        isFirstChild = false;
      }
    }
    
  }

  protected boolean hasChildren(UIComponent component)
  {
    int childCount = component.getChildCount();
    return childCount > 0;
  }

  protected void renderNode(
    FacesContext        context,
    AdfRenderingContext arc,
    Icon                separatorIcon,
    UIComponent         child,
    int                 renderedCount,
    boolean             shouldRenderLastChild,
    boolean             isFirstChild,
    boolean             isLastChild,
    boolean             isVertical
    ) throws IOException
  {      
    
    if (!isLastChild || shouldRenderLastChild)
    {
      _renderStartOfLink(context, arc, isVertical, isLastChild);
      
      ResponseWriter writer = context.getResponseWriter();
      
      // if oriented vertically, then indent the levels
      if(!isFirstChild && isVertical)
      {
        char[] chars = new char[1];
        chars[0] = XhtmlConstants.NBSP_CHAR;

        for(int i = 0; i < renderedCount * _INDENT_SPACES; i++)
        {
          writer.writeText(chars, 0, 1);
        }
      }
      
      if (arc.isRightToLeft())
      {
        writer.startElement(XhtmlConstants.SPAN_ELEMENT, null);
        writer.writeAttribute(XhtmlConstants.DIR_ATTRIBUTE_VALUE, "rtl", null);
      }
      
      renderLink(context, arc, child, renderedCount, isLastChild);

      if (!isLastChild)
      {
        OutputUtils.renderIcon(context, arc, separatorIcon, "", null );
      }      
      
      if (arc.isRightToLeft())
      {
         writer.endElement(XhtmlConstants.SPAN_ELEMENT);
      }
      
      _renderEndOfLink(context, isVertical, isLastChild);                  
      
    }
  }

  protected void renderLink(
    FacesContext context,
    AdfRenderingContext arc,
    UIComponent child,
    int renderedCount,
    boolean isLastChild) throws IOException
  {
    if (isLastChild)
      ((CoreAdfRenderingContext) arc).setLinkDisabled(true);    
  
    boolean isBidi = false;
    String text = toString(child.getAttributes().get(CoreCommandLink.TEXT_KEY.getName()));
    if ((text != null) && (text.length() > 0))
    {
      char[] firstChar = new char[1];
      firstChar[0] = text.charAt(0);
      isBidi = Bidi.requiresBidi(firstChar, 0, 1);
    }
    
    Map originalResourceKeyMap = arc.getSkinResourceKeyMap();
    try
    {
      arc.setSkinResourceKeyMap(_RESOURCE_KEY_MAP);
      if (!isBidi)
      {
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement(XhtmlConstants.SPAN_ELEMENT, null);
        writer.writeAttribute(XhtmlConstants.DIR_ATTRIBUTE_VALUE, "ltr",null);
        encodeChild(context, child);
        writer.endElement(XhtmlConstants.SPAN_ELEMENT);
      }
      else
        encodeChild(context, child);      
      
      if (isLastChild)
        ((CoreAdfRenderingContext) arc).setLinkDisabled(false);       
    }
    finally
    {
      arc.setSkinResourceKeyMap(originalResourceKeyMap);
    }       
  }
 
  protected boolean shouldRenderLastChild(
    AdfRenderingContext arc
  )
  {
    Object propValue = arc.getSkin().getProperty(
                      XhtmlConstants.AF_NAVIGATIONPATH_SHOW_LAST_ITEM_PROPERTY_KEY);
                      
    return Boolean.TRUE.equals(propValue);                      
  }

  /** 
   * renderStyleAttributes - use the NavigationPath style class as the default
   * styleClass
   */
  protected void renderStyleAttributes(
    FacesContext        context,
    AdfRenderingContext arc,
    FacesBean           bean) throws IOException
  {
    renderStyleAttributes(context, arc, bean, 
                          XhtmlConstants.AF_NAVIGATION_PATH_STYLE_CLASS);
  }



  protected String getOrientation(FacesBean bean)
  {
    return toString(bean.getProperty(_orientationKey));
  }  


  //
  // Renders (almost) everything that goes before the link
  //
  /**
   * @todo - not rendering style elements - see breadcrumbsrenderer
   * for explanation, do we still need this code?
   */
  private void _renderStartOfLink(
    FacesContext        context,
    AdfRenderingContext arc,
    boolean             isVertical,
    boolean             isLastChild) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();

    if (isVertical)
    {
      writer.startElement("div", null);
    }

    // Breadcrumbs wrap within a page depending on the size of the
    // browser window. The wrap occurs to the right of the greater-than
    // symbol that follows after the breadcrumb text string,
    // and not within the text string itself.
    writer.startElement("nobr", null);
  }

  //
  // Renders everything that goes after the link
  //
  private void _renderEndOfLink(
    FacesContext        context,
    boolean             isVertical,
    boolean             isLastChild
    ) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();

    writer.endElement("nobr");
  
    if(isVertical)
    {
      writer.endElement("div");
    }
  }
  
  private Object _getFocusRowKey(
    UIXHierarchy    component
  )
  {  
    return component.getFocusRowKey();
  }

  private int _getMinItemCount(
    AdfRenderingContext arc
  )
  {
    int minChildCount = 0;
    boolean shouldRenderLastChild = shouldRenderLastChild(arc);
    if ( !shouldRenderLastChild)
      minChildCount = 1;
      
    return minChildCount;
  }

  private int _getItemCount(
    UIXHierarchy    component
    )
  {
    Object focusPath = _getFocusRowKey(component);
    int kids = getRenderedChildCount(component);

    if (focusPath == null)
      return kids;

    return kids + component.getDepth(focusPath) + 1;
  }

  /**
   * gets the orientation attribute from the link. If this attr is not set,
   * defaults to horizontal.
   * @return true if the orientation is vertical
   */
  private boolean _isVertical(FacesBean bean)
  {
    String orientation = getOrientation(bean);
    return XhtmlConstants.ORIENTATION_VERTICAL.equals(orientation);
  }


  private PropertyKey _orientationKey;
  

  // # of hard spaces to use in indenting vertical breadcrumbs
  private static final int _INDENT_SPACES = 10;  
  
  
  private static final Map _RESOURCE_KEY_MAP  =  new HashMap();
  static
  {
    _RESOURCE_KEY_MAP.put(
      XhtmlConstants.LINK_STYLE_CLASS,
      XhtmlConstants.AF_NAVIGATION_PATH_STEP_STYLE_CLASS);
    // the selected step is disabled, which is why we map these two styles
    _RESOURCE_KEY_MAP.put(
      XhtmlConstants.LINK_DISABLED_STYLE_CLASS,
      XhtmlConstants.AF_NAVIGATION_PATH_SELECTED_STEP_STYLE_CLASS); 
  }
}
