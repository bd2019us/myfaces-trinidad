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
package org.apache.myfaces.adfinternal.renderkit.htmlBasic;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.myfaces.adfinternal.ui.AttributeKey;
import org.apache.myfaces.adfinternal.ui.MutableUINode;
import org.apache.myfaces.adfinternal.ui.Renderer;
import org.apache.myfaces.adfinternal.ui.RenderingContext;
import org.apache.myfaces.adfinternal.ui.UINode;
import org.apache.myfaces.adfinternal.ui.laf.base.PreAndPostRenderer;

import org.apache.myfaces.adfinternal.uinode.FacesRenderingContext;

/**
 * Base class for using a UINode to render a standard JSF component.
 * @todo Share common base class with UINodeRendererBase
 * @todo There's no pushing or popping of the UINode stack here.
 * <p>
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/renderkit/htmlBasic/UINodeRenderer.java#1 $) $Date: 11-nov-2005.14:59:42 $
 * @author The Oracle ADF Faces Team
 */
abstract public class UINodeRenderer extends javax.faces.render.Renderer
{
  public void encodeBegin(FacesContext context,
                          UIComponent component)
    throws IOException
  {
    if (!getRendersChildren())
    {
      RenderingContext rContext = getRenderingContext(context, component);
      UINode node = createUINode(context, component);
      Renderer renderer = _getRenderer(rContext, node);
      assert(renderer instanceof PreAndPostRenderer);
      ((PreAndPostRenderer) renderer).prerender(rContext, node);
    }
  }

  public void encodeChildren(FacesContext context,
                             UIComponent component)
  {
    // Children-encoding is always handled in getRendersChildren()
  }

  public void encodeEnd(FacesContext context,
                        UIComponent component)
    throws IOException
  {
    RenderingContext rContext = getRenderingContext(context, component);
    if (getRendersChildren())
    {
      createUINode(context, component).render(rContext);
    }
    else
    {
      // =-=AEW Should be able to retrieve cached UINode!
      UINode node = createUINode(context, component);
      Renderer renderer = _getRenderer(rContext, node);
      assert(renderer instanceof PreAndPostRenderer);
      ((PreAndPostRenderer) renderer).postrender(rContext, node);
    }
  }

  abstract protected UINode createUINode(
    FacesContext context,
    UIComponent  component);

  protected RenderingContext getRenderingContext(
    FacesContext context,
    UIComponent  component) throws IOException
  {
    return FacesRenderingContext.getRenderingContext(context, component);
  }

  /**
   * Sets an attribute if it has not already been set.
   * @todo doc why the heck anyone would call this!?!
   */
  protected void setAttribute(
    UIComponent   component,
    String        attrName,
    MutableUINode node,
    AttributeKey  attrKey)
  {
    Object value = component.getAttributes().get(attrName);
    if (value != null)
      node.setAttributeValue(attrKey, value);
  }

  public boolean getRendersChildren()
  {
    return true;
  }

  private Renderer _getRenderer(RenderingContext rContext, UINode node)
  {
    return rContext.getRendererManager().getRenderer(node);
  }
}
