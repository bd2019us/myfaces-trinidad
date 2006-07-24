/*
 * Copyright  2006,2006 The Apache Software Foundation.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.adf.bean.FacesBean;
import org.apache.myfaces.adf.bean.PropertyKey;
import org.apache.myfaces.adf.component.UIXCommand;
import org.apache.myfaces.adf.component.UIXHierarchy;
import org.apache.myfaces.adf.component.core.nav.CoreNavigationLevel;
import org.apache.myfaces.adf.context.Agent;
import org.apache.myfaces.adf.logging.ADFLogger;

import org.apache.myfaces.adfinternal.agent.AdfFacesAgent;
import org.apache.myfaces.adfinternal.renderkit.AdfRenderingContext;

public class NavigationLevelRenderer extends XhtmlRenderer
{
  public NavigationLevelRenderer()
  {
    super(CoreNavigationLevel.TYPE);
  }

  protected void findTypeConstants(FacesBean.Type type)
  {
    super.findTypeConstants(type);
    _hintKey = type.findKey("hint");
    _shortDescKey = type.findKey("shortDesc");
    _titleKey = type.findKey("title");
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

    writer.startElement("div", component);
    renderAllAttributes(context, arc, bean);
    _renderStyleAttributes(context, arc, bean);
    renderId(context, component);

    int renderedItemCount = _getItemCount((UIXHierarchy)component);

    // no kids, no NavigationLevel -- but you still get the span.
    if (renderedItemCount > 0)
    {
      renderContent(context, arc,
                    (UIXHierarchy)component, bean);
    }

    writer.endElement("div");
  }

  /**
   * Gets the stamp to use to render each link
   */
  private UIComponent _getStamp(
    UIXHierarchy        component
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
    NavItemData navItemData = new NavItemData();
    String renderingHint = _getHint(bean);

    UIComponent nodeStamp = _getStamp(component);
    if (nodeStamp == null)
    {
      // we aren't stamping, but rather have explicitly defined children:
      List<UIComponent> children = component.getChildren();
      int childrenLength = children.size();
      for (int i=0; i<childrenLength; i++)
      {
        try
        {
          UIXCommand navItem = (UIXCommand)children.get(i);
          if (navItem.isRendered())
          {
            // collect the information needed to render this nav item:
            _collectNavItemData(navItemData, navItem);
          }
        }
        catch (ClassCastException cce)
        {
          _LOG.severe(
            "Warning: illegal component hierarchy detected, expected UIXCommand but found another type of component instead.",
            cce);
        }
      }
    }
    else
    {
      // we are stamping the children:

      // =-= mcc TODO need to add support for stamped children
      _LOG.severe("Stamped children in Core NavigationLevel are not currently supported.");
    }

    // We must loop this second time because we support overlapping items,
    // arbitrary child wrappers; we need to know information like being the
    // first, the last, or whether nearby another active item.
    int visibleItemCount = navItemData.getItemCount();
    if (visibleItemCount != 0)
    {
      // starting outer markup:
      ResponseWriter rw = context.getResponseWriter();
      boolean isRtl = arc.getLocaleContext().isRightToLeft();
      boolean isChoiceHint =
        (NavigationLevelRenderer._HINT_CHOICE.equals(renderingHint));
      String choiceSelectId = null;
      if (isChoiceHint)
      {
        choiceSelectId = getClientId(context, component) + _CHOICE_SELECT_ID_SUFFIX;
        if (isRtl)
        {
          _renderChoiceButton(context, arc, rw, isRtl, choiceSelectId);
        }
        else
        {
          _renderChoiceLabel(context, arc, rw, isRtl, bean);
        }

        rw.startElement("select", null);
        rw.writeAttribute("id", choiceSelectId, null);
        renderStyleClass(context, arc,
          XhtmlConstants.AF_NAVIGATION_LEVEL_CHOICE_OPTIONS_STYLE_CLASS);
      }

      int lastRowIndex = visibleItemCount - 1;
      boolean previousActive = false;
      int nextActiveIndex = navItemData.getEffectiveActiveIndex() - 1;
      for (int i=0; i<visibleItemCount; i++)
      {
        Map currentItemData = navItemData.getItemData(i);
        currentItemData.put("isFirst", (i == 0));
        currentItemData.put("isLast", (i == lastRowIndex));
        currentItemData.put("previousActive", previousActive);
        currentItemData.put("nextActive", (i == nextActiveIndex));
        _renderNavigationItem(
          context,
          arc,
          rw,
          currentItemData,
          renderingHint,
          isRtl);
        previousActive = _getBooleanFromProperty(currentItemData.get("isActive"));
      }

      // Close up any opened choice tags:
      if (isChoiceHint)
      {
        rw.endElement("select");
        if (isRtl)
        {
          _renderChoiceLabel(context, arc, rw, isRtl, bean);
        }
        {
          _renderChoiceButton(context, arc, rw, isRtl, choiceSelectId);
        }
      }
    }
  }

  protected boolean hasChildren(UIComponent component)
  {
    int childCount = component.getChildCount();
    return childCount > 0;
  }

  /**
   * renderStyleAttributes - use the NavigationLevel style class as the default
   * styleClass
   */
  protected void _renderStyleAttributes(
    FacesContext        context,
    AdfRenderingContext arc,
    FacesBean           bean) throws IOException
  {
    String renderingHint = _getHint(bean);
    if (NavigationLevelRenderer._HINT_TABS.equals(renderingHint))
    {
      renderStyleAttributes(context, arc, bean, 
                            XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_STYLE_CLASS);
    }
    else if (NavigationLevelRenderer._HINT_BAR.equals(renderingHint))
    {
      renderStyleAttributes(context, arc, bean, 
                            XhtmlConstants.AF_NAVIGATION_LEVEL_BAR_STYLE_CLASS);
    }
    else
    {
      renderStyleAttributes(context, arc, bean, 
                            XhtmlConstants.AF_NAVIGATION_LEVEL_STYLE_CLASS);
    }
  }

  private String _getHint(FacesBean bean)
  {
    String renderingHint = toString(bean.getProperty(_hintKey));
    if (renderingHint == null)
    {
      // automated hints are only used if the hint attribute was not set
      // =-= mcc TODO pull from arc, e.g. when placed by Page, PanelPage
    }
    if (renderingHint == null)
    {
      renderingHint = _HINT_DEFAULT;
    }
    return renderingHint;
  }

  protected String getShortDesc(FacesBean bean)
  {
    return toString(bean.getProperty(_shortDescKey));
  }

  protected String getTitle(FacesBean bean)
  {
    return toString(bean.getProperty(_titleKey));
  }

  /**
   * Renders the client ID as both "id" and "name"
   */
  protected void _renderCommandChildId(
    FacesContext context,
    UIXCommand   component
    ) throws IOException
  {
    String clientId = getClientId(context, component);
    // For links, these are actually URI attributes
    context.getResponseWriter().writeURIAttribute("id", clientId, "id");
    context.getResponseWriter().writeURIAttribute("name", clientId, "id");
  }

  private Object _getFocusRowKey(
    UIXHierarchy component
    )
  {  
    return component.getFocusRowKey();
  }

  private int _getItemCount(
    UIXHierarchy component
    )
  {
    Object focusPath = _getFocusRowKey(component);
    int kids = getRenderedChildCount(component);

    if (focusPath == null)
      return kids;

    // =-= mcc TODO level is different than path, so the following is wrong:
    return kids + component.getDepth(focusPath) + 1;
  }

  private void _collectNavItemData(
    NavItemData navItemData,
    UIXCommand commandChild)
  {
    int itemDataIndex = navItemData.getItemCount();
    boolean isActive = _getBooleanFromProperty(_getCommandChildProperty(commandChild, "selected"));
    if (isActive)
    {
      if (navItemData.getEffectiveActiveIndex() == -1)
      {
        navItemData.setEffectiveActiveIndex(itemDataIndex);
      }
      else
      {
        isActive = false; // there can only be 1 active item
      }
    }
    HashMap itemDataMap = new HashMap();
    itemDataMap.put("accessKey", _getCommandChildProperty(commandChild, "accessKey"));
    itemDataMap.put("component", commandChild);
    itemDataMap.put("dataIndex", itemDataIndex);
    itemDataMap.put("destination", _getCommandChildProperty(commandChild, "destination"));
    itemDataMap.put("icon", _getCommandChildProperty(commandChild, "icon"));
    itemDataMap.put("immediate", _getCommandChildProperty(commandChild, "immediate"));
    itemDataMap.put("inlineStyle", _getCommandChildProperty(commandChild, "inlineStyle"));
    itemDataMap.put("isActive", isActive);
    itemDataMap.put("isDisabled", _getCommandChildProperty(commandChild, "disabled"));
    itemDataMap.put("partialSubmit", _getCommandChildProperty(commandChild, "partialSubmit"));
    itemDataMap.put("shortDesc", _getCommandChildProperty(commandChild, "shortDesc"));
    itemDataMap.put("styleClass", _getCommandChildProperty(commandChild, "styleClass"));
    itemDataMap.put("targetFrame", _getCommandChildProperty(commandChild, "targetFrame"));
    itemDataMap.put("text", _getCommandChildProperty(commandChild, "text"));
    navItemData.addItemData(itemDataMap);
  }

  private boolean _getBooleanFromProperty(Object value)
  {
    if (value == null)
    {
      return false;
    }
    return ("true".equals(value.toString()));
  }

  private String _getPossiblyNullString(Object value)
  {
    if (value == null)
    {
      return null;
    }
    return value.toString();
  }

  private Object _getCommandChildProperty(
    UIXCommand commandChild,
    String propertyName)
  {
    FacesBean childFacesBean = commandChild.getFacesBean();
    FacesBean.Type type = childFacesBean.getType();
    PropertyKey propertyKey = type.findKey(propertyName);
    if (propertyKey == null)
    {
      if (_LOG.isSevere())
      {
        _LOG.severe(
          "Warning: NavigationLevelRenderer was looking for child property \"" +
          propertyName +
          "\" but none was found, it is likely that an unexpected child component was found (expected CommandNavigationItem).");
      }
      return null;
    }
    else
    {
      return childFacesBean.getProperty(propertyKey);
    }
  }

  private void _renderNavigationItem(
    FacesContext context,
    AdfRenderingContext arc,
    ResponseWriter rw,
    Map itemData,
    String renderingHint,
    boolean isRtl
    ) throws IOException
  {
    if (_HINT_BAR.equals(renderingHint))
    {
      _renderNonOverlappingItem(context, arc, rw, itemData, isRtl, true, false);
    }
    else if (_HINT_BUTTONS.equals(renderingHint))
    {
      _renderNonOverlappingItem(context, arc, rw, itemData, isRtl, false, false);
    }
    else if (_HINT_CHOICE.equals(renderingHint))
    {
      _renderChoiceItem(context, arc, rw, itemData);
    }
    else if (_HINT_LIST.equals(renderingHint))
    {
      _renderNonOverlappingItem(context, arc, rw, itemData, isRtl, false, true);
    }
    else // _HINT_TABS
    {
      _renderTabItem(context, arc, rw, itemData, isRtl);
    }
  }

  private void _writeInlineStyles(
    ResponseWriter rw,
    Object userInlineStyle,
    String appendedInlineStyle
    ) throws IOException
  {
    if (userInlineStyle == null)
    {
      rw.writeAttribute("style", appendedInlineStyle, null);
    }
    else
    {
      String userInlineStyleString = _getPossiblyNullString(userInlineStyle).trim();
      boolean needSemicolon = false;
      int lastIndex = userInlineStyleString.length()-1;
      if ( (lastIndex > 0) && (userInlineStyleString.charAt(lastIndex) != ';') )
      {
        needSemicolon = true;
      }
      StringBuilder itemInlineStyle = new StringBuilder();
      itemInlineStyle.append(userInlineStyleString);
      if (needSemicolon)
      {
        itemInlineStyle.append(";");
      }
      if (appendedInlineStyle != null)
      {
        itemInlineStyle.append(appendedInlineStyle);
      }
      rw.writeAttribute("style", itemInlineStyle.toString(), null);
    }
  }

  private void _writeInlineTbodyStyles(
    AdfRenderingContext arc,
    ResponseWriter rw
    ) throws IOException
  {
    // IE and Firefox require the TABLE to be "inline".
    if (arc.getAgent().getAgentName() == Agent.AGENT_GECKO)
    {
      // Firefox 1.5 also requires the TBODY to be "inline":
      rw.writeAttribute("style", "display: inline;", null);
    }
  }

  private void _appendIconAndText(
    FacesContext context,
    AdfRenderingContext arc,
    ResponseWriter rw,
    String iconUri,
    Map itemData,
    boolean isDisabled,
    boolean isRtl
    ) throws IOException
  {
    if ( (iconUri != null) && !isRtl )
    {
      _appendIcon(context, rw, iconUri, isRtl);
    }
    _writeItemLink(context, arc, rw, itemData, isDisabled);
    if ( (iconUri != null) && isRtl )
    {
      _appendIcon(context, rw, iconUri, isRtl);
    }
  }

  private void _appendIcon(
    FacesContext context,
    ResponseWriter rw,
    String iconUri,
    boolean isRtl
    ) throws IOException
  {
    rw.startElement("img", null);
    rw.writeAttribute("border", "0", null);
    rw.writeAttribute("align", "absmiddle", null);
    if (isRtl)
    {
      rw.writeAttribute("style", "padding-left: 5px; float: right;", null);
    }
    else
    {
      rw.writeAttribute("style", "padding-right: 5px; float: left;", null);
    }
    Application application = context.getApplication();
    ViewHandler handler = application.getViewHandler();
    String resolvedIconUri = handler.getResourceURL(context, iconUri);
    rw.writeAttribute("src", resolvedIconUri, null);
    rw.endElement("img");
  }

  private void _writeItemLink(
    FacesContext context,
    AdfRenderingContext arc,
    ResponseWriter rw,
    Map itemData,
    boolean isDisabled
    ) throws IOException
  {
    if (isDisabled)
    {
      rw.write(_getPossiblyNullString(itemData.get("text")));
      return;
    }

    UIXCommand commandChild = (UIXCommand)itemData.get("component");
    String destination = _getPossiblyNullString(itemData.get("destination"));
    boolean immediate = false;
    boolean partialSubmit = false;
    Object old = null;
    if (destination == null)
    {
      immediate = _getBooleanFromProperty(itemData.get("immediate"));
      partialSubmit = _getBooleanFromProperty(itemData.get("partialSubmit"));
      if (partialSubmit)
      {
        AutoSubmitUtils.writeDependencies(context, arc);
      } 
      String clientId = commandChild.getClientId(context);
      // Make sure we don't have anything to save
      assert(arc.getCurrentClientId() == null);
      arc.setCurrentClientId(clientId);

      // Find the params up front, and save them off - 
      // getOnClick() doesn't have access to the UIComponent
      String extraParams = AutoSubmitUtils.getParameters(commandChild);
      old = arc.getProperties().put(_EXTRA_SUBMIT_PARAMS_KEY, extraParams);
    }

    rw.startElement("a", commandChild); // linkElement
    _renderCommandChildId(context, commandChild);

    if (destination == null)
    {
      rw.writeAttribute("href", "#", null); // required for IE to support ":hover" styles
    }
    else
    {
      renderEncodedActionURI(context, "href", destination);
      String targetFrame = _getPossiblyNullString(itemData.get("targetFrame"));
      if ( (targetFrame != null) && !Boolean.FALSE.equals(
        arc.getAgent().getCapability(AdfFacesAgent.CAP_TARGET)) )
      {
        rw.writeAttribute("target", targetFrame, null);
      }
    }

    // Cannot use super.renderEventHandlers(context, bean); because the wrong
    // property keys would be in use so must do it this way:
    _writeOnclickProperty(
      arc,
      rw,
      commandChild,
      (destination == null),
      immediate,
      partialSubmit); // special for actions!
    _writeCommandChildProperty(rw, commandChild, "ondblclick");
    _writeCommandChildProperty(rw, commandChild, "onkeydown");
    _writeCommandChildProperty(rw, commandChild, "onkeyup");
    _writeCommandChildProperty(rw, commandChild, "onkeypress");
    _writeCommandChildProperty(rw, commandChild, "onmousedown");
    _writeCommandChildProperty(rw, commandChild, "onmousemove");
    _writeCommandChildProperty(rw, commandChild, "onmouseout");
    _writeCommandChildProperty(rw, commandChild, "onmouseover");
    _writeCommandChildProperty(rw, commandChild, "onmouseup");

    String accessKey = _getPossiblyNullString(itemData.get("accessKey"));
    if ( !isDisabled && (accessKey != null) )
    {
      rw.writeAttribute("accessKey", accessKey, null);
    }
    rw.write(_getPossiblyNullString(itemData.get("text")));
    rw.endElement("a"); // linkElement

    if (destination == null)
    {
      // Restore any old params, though really, how could that happen??
      arc.getProperties().put(_EXTRA_SUBMIT_PARAMS_KEY, old);

      arc.setCurrentClientId(null);
    }
  }

  private void _writeCommandChildProperty(
    ResponseWriter rw,
    UIXCommand commandChild,
    String propertyName
    ) throws IOException
  {
    rw.writeAttribute(
      propertyName,
      _getCommandChildProperty(commandChild, propertyName),
      propertyName);
  }

  private void _writeOnclickProperty(
    AdfRenderingContext arc,
    ResponseWriter rw,
    UIXCommand commandChild,
    boolean actionSpecialCase,
    boolean immediate,
    boolean partialSubmit
    ) throws IOException
  {
    if (actionSpecialCase)
    {
      String onclick = (String)_getCommandChildProperty(commandChild, "onclick");
      String script = _getAutoSubmitScript(arc, immediate, partialSubmit);
      rw.writeAttribute(
        "onclick", XhtmlUtils.getChainedJS(onclick, script, true), "onclick");
    }
    else // simple case, e.g. (destination != null)
    {
      _writeCommandChildProperty(rw, commandChild, "onclick");
    }
  }

  private String _getAutoSubmitScript(
    AdfRenderingContext arc,
    boolean immediate,
    boolean partialSubmit
    )
  {
    String id = arc.getCurrentClientId();

    String extraParams = (String)
      arc.getProperties().get(_EXTRA_SUBMIT_PARAMS_KEY);

    String script;
    if (partialSubmit)
    {
      script = AutoSubmitUtils.getSubmitScript(
        arc, id, immediate, false,
        null/* no event*/,
        extraParams,
        false);
    }
    else
    {
      script = AutoSubmitUtils.getFullPageSubmitScript(
        arc, id, immediate,
        null/*no event*/,
        extraParams,
        false/* return false*/);
    }
    return script;
  }

  private void _renderNonOverlappingItem(
    FacesContext context,
    AdfRenderingContext arc,
    ResponseWriter rw,
    Map itemData,
    boolean isRtl,
    boolean isBar,
    boolean isList
    ) throws IOException
  {
    rw.startElement("table", null);
    OutputUtils.renderLayoutTableAttributes(context, arc, "0", null);
    String appendedStyle = null;
    if (!isList)
    {
      appendedStyle = "display: inline;"; // style to make the table inline
    }
    _writeInlineStyles(rw, _getPossiblyNullString(itemData.get("inlineStyle")),
      appendedStyle); // user's style + what we must have on top of it
    rw.writeAttribute("title", itemData.get("shortDesc"), null);
    StringBuilder itemStyleClass = new StringBuilder();
    String userStyleClass = _getPossiblyNullString(itemData.get("styleClass"));
    if (userStyleClass != null)
    {
      itemStyleClass.append(userStyleClass);
      itemStyleClass.append(" "); // more style classes are appended below
    }

    // Assign the event handlers:
    boolean isDisabled = _getBooleanFromProperty(itemData.get("isDisabled"));
    boolean isActive = _getBooleanFromProperty(itemData.get("isActive"));
    if (isActive)
    {
      if (isDisabled)
      {
        if (isList)
        {
          itemStyleClass.append(
            XhtmlConstants.AF_NAVIGATION_LEVEL_LIST_ACTIVE_DISABLED_STYLE_CLASS);
        }
        else if (isBar)
        {
          itemStyleClass.append(
            XhtmlConstants.AF_NAVIGATION_LEVEL_BAR_ACTIVE_DISABLED_STYLE_CLASS);
        }
        else
        {
          itemStyleClass.append(
            XhtmlConstants.AF_NAVIGATION_LEVEL_BUTTONS_ACTIVE_DISABLED_STYLE_CLASS);
        }
      }
      else
      {
        if (isList)
        {
          itemStyleClass.append(
            XhtmlConstants.AF_NAVIGATION_LEVEL_LIST_ACTIVE_ENABLED_STYLE_CLASS);
        }
        else if (isBar)
        {
          itemStyleClass.append(
            XhtmlConstants.AF_NAVIGATION_LEVEL_BAR_ACTIVE_ENABLED_STYLE_CLASS);
        }
        else
        {
          itemStyleClass.append(
            XhtmlConstants.AF_NAVIGATION_LEVEL_BUTTONS_ACTIVE_ENABLED_STYLE_CLASS);
        }
      }
    }
    else
    {
      if (isDisabled)
      {
        if (isList)
        {
          itemStyleClass.append(
            XhtmlConstants.AF_NAVIGATION_LEVEL_LIST_INACTIVE_DISABLED_STYLE_CLASS);
        }
        else if (isBar)
        {
          itemStyleClass.append(
            XhtmlConstants.AF_NAVIGATION_LEVEL_BAR_INACTIVE_DISABLED_STYLE_CLASS);
        }
        else
        {
          itemStyleClass.append(
            XhtmlConstants.AF_NAVIGATION_LEVEL_BUTTONS_INACTIVE_DISABLED_STYLE_CLASS);
        }
      }
      else
      {
        if (isList)
        {
          itemStyleClass.append(
            XhtmlConstants.AF_NAVIGATION_LEVEL_LIST_INACTIVE_ENABLED_STYLE_CLASS);
        }
        else if (isBar)
        {
          itemStyleClass.append(
            XhtmlConstants.AF_NAVIGATION_LEVEL_BAR_INACTIVE_ENABLED_STYLE_CLASS);
        }
        else
        {
          itemStyleClass.append(
            XhtmlConstants.AF_NAVIGATION_LEVEL_BUTTONS_INACTIVE_ENABLED_STYLE_CLASS);
        }
      }
    }
    renderStyleClass(context, arc, itemStyleClass.toString());
    String rowKey = _getPossiblyNullString(itemData.get("dataIndex"));
    if (rowKey != null)
    {
      // =-= mcc TODO save off stamping currency, e.g. rw.writeAttribute(_ROWKEY_ATTRIBUTE, rowKey);
    }
    rw.startElement("tbody", null);
    if (!isList)
    {
      _writeInlineTbodyStyles(arc, rw);
    }
    rw.startElement("tr", null);

    if (isList)
    {
      rw.startElement("td", null); // bulletCell
      renderStyleClass(
        context,
        arc,
        XhtmlConstants.AF_NAVIGATION_LEVEL_LIST_BULLET_STYLE_CLASS);
      rw.startElement("div", null); // bulletContent
      rw.write(" ");
      rw.endElement("div"); // bulletContent
      rw.endElement("td"); // bulletCell
    }

    rw.startElement("td", null); // centerCell
    rw.startElement("div", null); // centerContent
    if (isList)
    {
      renderStyleClass(context, arc,
        XhtmlConstants.AF_NAVIGATION_LEVEL_LIST_CONTENT_STYLE_CLASS);
    }
    else if (isBar)
    {
      renderStyleClass(context, arc,
        XhtmlConstants.AF_NAVIGATION_LEVEL_BAR_CONTENT_STYLE_CLASS);
    }
    else
    {
      renderStyleClass(context, arc,
        XhtmlConstants.AF_NAVIGATION_LEVEL_BUTTONS_CONTENT_STYLE_CLASS);
    }
    _appendIconAndText(
      context,
      arc,
      rw,
      _getPossiblyNullString(itemData.get("icon")),
      itemData,
      isDisabled,
      isRtl);
    rw.endElement("div"); // centerContent
    rw.endElement("td"); // centerCell

    if ( !isList && !_getBooleanFromProperty(itemData.get("isLast")) )
    {
      rw.startElement("td", null); // rightCell
      rw.startElement("div", null); // rightContent
      if (isBar)
      {
        renderStyleClass(context, arc,
          XhtmlConstants.AF_NAVIGATION_LEVEL_BAR_SEPARATOR_STYLE_CLASS);
      }
      else
      {
        renderStyleClass(context, arc,
          XhtmlConstants.AF_NAVIGATION_LEVEL_BUTTONS_SEPARATOR_STYLE_CLASS);
      }
      rw.write("|");
      rw.endElement("div"); // rightContent
      rw.endElement("td"); // rightCell
    }

    rw.endElement("tr");
    rw.endElement("tbody");
    rw.endElement("table");
  }

  private void _renderChoiceItem(
    FacesContext context,
    AdfRenderingContext arc,
    ResponseWriter rw,
    Map itemData
    ) throws IOException
  {
    // Choice items do not support icons at this time.
    boolean isDisabled = _getBooleanFromProperty(itemData.get("isDisabled"));
    // IE doesn't support disabled options so we won't add them in that case.
    if ( !isDisabled || (arc.getAgent().getAgentName() != Agent.AGENT_IE) )
    {
      boolean isActive = _getBooleanFromProperty(itemData.get("isActive"));
      String rowKey = _getPossiblyNullString(itemData.get("dataIndex"));
      rw.startElement("option", null);
      if (isActive)
      {
        rw.writeAttribute("selected", Boolean.TRUE, null);
      }
      rw.writeAttribute("disabled", isDisabled, null);
      rw.writeAttribute("title", itemData.get("shortDesc"), null);
      if (rowKey != null)
      {
        // =-= mcc TODO save off stamping currency, e.g. rw.writeAttribute(_ROWKEY_ATTRIBUTE, rowKey);
      }

      if (!isDisabled)
      {
        // Write the script to evaluate once the item is selected
        UIXCommand commandChild = (UIXCommand)itemData.get("component");
        String destination = _getPossiblyNullString(itemData.get("destination"));
        boolean immediate = false;
        boolean partialSubmit = false;
        Object old = null;
        if (destination == null)
        {
          immediate = _getBooleanFromProperty(itemData.get("immediate"));
          partialSubmit = _getBooleanFromProperty(itemData.get("partialSubmit"));
          if (partialSubmit)
          {
            AutoSubmitUtils.writeDependencies(context, arc);
          } 
          String clientId = commandChild.getClientId(context);
          // Make sure we don't have anything to save
          assert(arc.getCurrentClientId() == null);
          arc.setCurrentClientId(clientId);

          // Find the params up front, and save them off - 
          // getOnClick() doesn't have access to the UIComponent
          String extraParams = AutoSubmitUtils.getParameters(commandChild);
          old = arc.getProperties().put(_EXTRA_SUBMIT_PARAMS_KEY, extraParams);
        }
        _renderCommandChildId(context, commandChild);
        String selectionScript;
        if (destination == null)
        {
          selectionScript = _getAutoSubmitScript(arc, immediate, partialSubmit);

          // Trim off the "return false;" because we will be performing an
          // "eval" on the script and a "return" will yield a runtime error:
          selectionScript =
            selectionScript.substring(0, selectionScript.length() - 13);
        }
        else
        {
          String encodedDestination =
            context.getExternalContext().encodeActionURL(destination.toString());
          String targetFrame = _getPossiblyNullString(itemData.get("targetFrame"));
          StringBuilder sb = new StringBuilder();
          if ( (targetFrame != null) && !Boolean.FALSE.equals(
            arc.getAgent().getCapability(AdfFacesAgent.CAP_TARGET)) )
          {
            sb.append("window.open('");
            sb.append(encodedDestination);
            sb.append("','");
            sb.append(targetFrame);
            sb.append("');");
          }
          else
          {
            sb.append("self.location='");
            sb.append(encodedDestination);
            sb.append("';");
          }
          selectionScript = sb.toString();
        }
        rw.writeAttribute("value", selectionScript, null);
      }

      rw.write(_getPossiblyNullString(itemData.get("text")));
      rw.endElement("option");
    }
  }

  private void _renderChoiceLabel(
    FacesContext context,
    AdfRenderingContext arc,
    ResponseWriter rw,
    boolean isRtl,
    FacesBean bean
    ) throws IOException
  {
    String chooseText = getTitle(bean);
    if ( (chooseText == null) || (chooseText.length() == 0) )
    {
      chooseText = getShortDesc(bean);
    }

    if ( (chooseText != null) && (chooseText.length() != 0) )
    {
      if (isRtl)
      {
        _renderSpace(rw);
      }
      rw.startElement("span", null);
      renderStyleClass(context, arc,
        XhtmlConstants.AF_NAVIGATION_LEVEL_CHOICE_LABEL_STYLE_CLASS);
      rw.write(chooseText);
      rw.endElement("span");
      if (!isRtl)
      {
        _renderSpace(rw);
      }
    }
  }

  private void _renderChoiceButton(
    FacesContext context,
    AdfRenderingContext arc,
    ResponseWriter rw,
    boolean isRtl,
    String choiceSelectId
    ) throws IOException
  {
    if (!isRtl)
    {
      _renderSpace(rw);
    }
    rw.startElement("button", null);
    renderStyleClass(context, arc,
      XhtmlConstants.AF_NAVIGATION_LEVEL_CHOICE_BUTTON_STYLE_CLASS);
    String goText = arc.getSkin().getTranslatedString(
      arc.getLocaleContext(),
      _GO_BUTTON_LABEL_KEY);

    // The onclick handler will evaluate the value of the selected option:
    rw.writeAttribute(
      "onclick",
      "var navLevelSelect = document.getElementById('" +
        choiceSelectId +
        "'); eval(navLevelSelect.options[navLevelSelect.selectedIndex].value); return false;",
      null);

    rw.write(goText);
    rw.endElement("button");
    if (isRtl)
    {
      _renderSpace(rw);
    }
  }

  private void _renderSpace(
    ResponseWriter rw
    ) throws IOException
  {
    rw.startElement("span", null);
    rw.writeAttribute("style", "width: 5px;", null);
    rw.write(" ");
    rw.endElement("span");
  }

  private void _renderTabItem(
    FacesContext context,
    AdfRenderingContext arc,
    ResponseWriter rw,
    Map itemData,
    boolean isRtl
    ) throws IOException
  {
    rw.startElement("table", null);
    OutputUtils.renderLayoutTableAttributes(context, arc, "0", null);
    _writeInlineStyles(rw, _getPossiblyNullString(itemData.get("inlineStyle")),
      "display: inline;"); // user's style + what we must have on top of it
    rw.writeAttribute("title", itemData.get("shortDesc"), null);
    StringBuilder itemStyleClass = new StringBuilder();
    String userStyleClass = _getPossiblyNullString(itemData.get("styleClass"));
    if (userStyleClass != null)
    {
      itemStyleClass.append(userStyleClass);
      itemStyleClass.append(" "); // more style classes are appended below
    }

    // Assign the event handlers:
    boolean isDisabled = _getBooleanFromProperty(itemData.get("isDisabled"));
    boolean isActive = _getBooleanFromProperty(itemData.get("isActive"));
    String sectionStyleClass1;
    String sectionStyleClass2 = null;
    if (isActive)
    {
      if (isDisabled)
      {
        sectionStyleClass2 = XhtmlConstants.P_AF_DISABLED;
      }
      sectionStyleClass1 =
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_ACTIVE_STYLE_CLASS;
    }
    else
    {
      if (isDisabled)
      {
        sectionStyleClass2 = XhtmlConstants.P_AF_DISABLED;
      }
      sectionStyleClass1 =
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_INACTIVE_STYLE_CLASS;
    }
    renderStyleClass(context, arc, itemStyleClass.toString());
    String rowKey = _getPossiblyNullString(itemData.get("dataIndex"));
    if (rowKey != null)
    {
      // =-= mcc TODO save off stamping currency, e.g. rw.writeAttribute(_ROWKEY_ATTRIBUTE, rowKey);
    }
    rw.startElement("tbody", null);
    _writeInlineTbodyStyles(arc, rw);
    rw.startElement("tr", null);

    boolean isFirst = _getBooleanFromProperty(itemData.get("isFirst"));
    boolean isLast = _getBooleanFromProperty(itemData.get("isLast"));
    boolean previousActive = _getBooleanFromProperty(itemData.get("previousActive"));

    // start portion of tab:
    if (isFirst)
    {
      // first start
      rw.startElement("td", null);
      _renderTabSection(
        context,
        arc,
        rw,
        sectionStyleClass1,
        sectionStyleClass2,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_START_STYLE_CLASS,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_START_STYLE_CLASS,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_START_CONTENT_STYLE_CLASS,
        null,
        isDisabled,
        isRtl);
      rw.endElement("td");
    }
    else if (previousActive)
    {
      // start-join-selected-to-deselected
      rw.startElement("td", null);
      _renderTabSection(
        context,
        arc,
        rw,
        sectionStyleClass1,
        sectionStyleClass2,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_START_JOIN_FROM_ACTIVE_STYLE_CLASS,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_END_STYLE_CLASS,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_MID_CONTENT_STYLE_CLASS,
        null,
        isDisabled,
        isRtl);
      rw.endElement("td");
    }
    else if (isActive)
    {
      // start-join-deselected-to-selected
      rw.startElement("td", null);
      _renderTabSection(
        context,
        arc,
        rw,
        sectionStyleClass1,
        sectionStyleClass2,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_START_JOIN_STYLE_CLASS,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_START_STYLE_CLASS,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_START_CONTENT_STYLE_CLASS,
        null,
        isDisabled,
        isRtl);
      rw.endElement("td");
    }
    else
    {
      // start-join-deselected-to-deselected
      rw.startElement("td", null);
      _renderTabSection(
        context,
        arc,
        rw,
        sectionStyleClass1,
        sectionStyleClass2,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_START_JOIN_FROM_INACTIVE_STYLE_CLASS,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_END_STYLE_CLASS,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_MID_CONTENT_STYLE_CLASS,
        null,
        isDisabled,
        isRtl);
      rw.endElement("td");
    }

    // mid portion of tab:
    rw.startElement("td", null);
    _renderTabSection(
      context,
      arc,
      rw,
      sectionStyleClass1,
      sectionStyleClass2,
      XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_MID_STYLE_CLASS,
      XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_MID_STYLE_CLASS,
      XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_MID_CONTENT_STYLE_CLASS,
      itemData,
      isDisabled,
      isRtl);
    rw.endElement("td");

    if (isLast)
    {
      // last end
      rw.startElement("td", null);
      _renderTabSection(
        context,
        arc,
        rw,
        sectionStyleClass1,
        sectionStyleClass2,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_END_STYLE_CLASS,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_END_STYLE_CLASS,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_END_CONTENT_STYLE_CLASS,
        null,
        isDisabled,
        isRtl);
      rw.endElement("td");
    }
    else if ( isActive ||  (!_getBooleanFromProperty(itemData.get("nextActive"))) )
    {
      // end-join-selected-to-deselected or end-join-deselected-to-deselected
      rw.startElement("td", null);
      _renderTabSection(
        context,
        arc,
        rw,
        sectionStyleClass1,
        sectionStyleClass2,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_END_JOIN_TO_INACTIVE_STYLE_CLASS,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_END_JOIN_STYLE_CLASS,
        XhtmlConstants.AF_NAVIGATION_LEVEL_TABS_BOTTOM_END_CONTENT_STYLE_CLASS,
        null,
        isDisabled,
        isRtl);
      rw.endElement("td");
    }

    rw.endElement("tr");
    rw.endElement("tbody");
    rw.endElement("table");
  }

  private void _renderTabSection(
    FacesContext context,
    AdfRenderingContext arc,
    ResponseWriter rw,
    String sectionStyleClass1,
    String sectionStyleClass2,
    String topStyleClass,
    String bottomStyleClass,
    String bottomContentStyleClass,
    Map itemData,
    boolean isDisabled,
    boolean isRtl
    ) throws IOException
  {
    rw.startElement("table", null);
    OutputUtils.renderLayoutTableAttributes(context, arc, "0", null);
    if (sectionStyleClass2 == null)
    {
      renderStyleClass(context, arc, sectionStyleClass1);
    }
    else
    {
      String[] sectionStyleClasses = { sectionStyleClass1, sectionStyleClass2 };
      renderStyleClasses(context, arc, sectionStyleClasses);
    }
    rw.startElement("tbody", null);
    rw.startElement("tr", null);
    rw.startElement("td", null);
    renderStyleClass(context, arc, topStyleClass);
    if (itemData != null)
    {
      _appendIconAndText(
        context,
        arc,
        rw,
        _getPossiblyNullString(itemData.get("icon")),
        itemData,
        isDisabled,
        isRtl);
    }
    rw.endElement("td");
    rw.endElement("tr");
    rw.startElement("tr", null);
    rw.startElement("td", null);
    renderStyleClass(context, arc, bottomStyleClass);
    if (bottomContentStyleClass != null)
    {
      rw.startElement("div", null);
      renderStyleClass(context, arc, bottomContentStyleClass);
      rw.endElement("div");
    }
    rw.endElement("td");
    rw.endElement("tr");
    rw.endElement("tbody");
    rw.endElement("table");
  }

  private class NavItemData
  {
    NavItemData()
    {
      _list = new ArrayList<Map>();
      _effectiveActiveIndex = -1;
    }

    int getEffectiveActiveIndex()
    {
      return _effectiveActiveIndex;
    }

    void setEffectiveActiveIndex(int effectiveActiveIndex)
    {
      _effectiveActiveIndex = effectiveActiveIndex;
    }

    int getItemCount()
    {
      return _list.size();
    }

    void addItemData(Map itemData)
    {
      _list.add(itemData);
    }

    Map getItemData(int index)
    {
      return _list.get(index);
    }

    private List<Map> _list;
    private int _effectiveActiveIndex;
  }

  private PropertyKey _hintKey;
  private PropertyKey _shortDescKey;
  private PropertyKey _titleKey;

  static private final String _HINT_TABS = "tabs";
  static private final String _HINT_BAR = "bar";
  static private final String _HINT_LIST = "list";
  static private final String _HINT_BUTTONS = "buttons";
  static private final String _HINT_CHOICE = "choice";
  static private final String _HINT_DEFAULT = NavigationLevelRenderer._HINT_TABS;

  private static final String _CHOICE_SELECT_ID_SUFFIX =
    "_af_choice_select";
  private static final String _GO_BUTTON_LABEL_KEY =
    "af_menuChoice.GO";

  static private final Object _EXTRA_SUBMIT_PARAMS_KEY = new Object();

  private static final ADFLogger _LOG =
    ADFLogger.createADFLogger(NavigationLevelRenderer.class);
}
