/*
 * Copyright  2000-2006 The Apache Software Foundation.
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
package org.apache.myfaces.adfinternal.ui.composite;

import org.apache.myfaces.adfinternal.ui.RenderingContext;
import org.apache.myfaces.adfinternal.ui.UINode;

import org.apache.myfaces.adfinternal.ui.collection.UINodeAttributeMap;



/**
 * AttributeMap that delegates to the ParentContext's current UINode.  This
 * class is typically used inside of composite widgets to delegate attributes
 * to the root of the composite.
 * <p>
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/ui/composite/RootAttributeMap.java#0 $) $Date: 10-nov-2005.18:56:53 $
 * @author The Oracle ADF Faces Team
 */
public class RootAttributeMap extends UINodeAttributeMap
{
  public static RootAttributeMap getAttributeMap()
  {
    return _INSTANCE;
  }
  
  protected RootAttributeMap()
  {
  }
  
  protected UINode getUINode(
    RenderingContext context
    )
  {
    if (context != null)
    {
      RenderingContext parentContext = context.getParentContext();
      
      if (parentContext != null)
      {
        return parentContext.getAncestorNode(0);
      }
    }

    return null;
  }
  
  protected RenderingContext getRenderingContext(RenderingContext context)
  {
    if (context == null)
      return null;

    return context.getParentContext();
  }

  private static final RootAttributeMap _INSTANCE = new RootAttributeMap();
}
