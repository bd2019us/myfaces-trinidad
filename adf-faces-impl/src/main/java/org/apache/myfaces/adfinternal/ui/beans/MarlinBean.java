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
package org.apache.myfaces.adfinternal.ui.beans;

import org.apache.myfaces.adfinternal.ui.UIConstants;
import org.apache.myfaces.adfinternal.ui.UINode;


/**
 * Subclass of BaseWebBean that places its subclasses inside
 * the UIX namespace.  Clients should <b>never</b> subclass
 * this class.  Instead, subclass BaseWebBean and place
 * components in your group's own namespaces.
 * <p>
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/ui/beans/MarlinBean.java#0 $) $Date: 10-nov-2005.18:57:39 $
 * @author The Oracle ADF Faces Team
 */
public class MarlinBean extends BaseWebBean
{
  /**
   * Creates a WebBean with the specified local name in the UIX
   * namespace.
   */
  public MarlinBean(
    String localName
    )
  {
    super(UIConstants.MARLIN_NAMESPACE, localName);
  }
  
  
  /**
   * Returns true if the specified node has the same UIX name as the
   * name passed in.
   */
  protected static boolean isEqualMarlinName(
    UINode node,
    String localName
    )
  {
    return (node.getNamespaceURI() == MARLIN_NAMESPACE) &&
           (localName.equals(node.getLocalName()));
  }
}
