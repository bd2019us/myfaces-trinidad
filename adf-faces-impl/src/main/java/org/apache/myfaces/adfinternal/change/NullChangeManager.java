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

package org.apache.myfaces.adfinternal.change;

import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.myfaces.adf.change.ChangeManager;
import org.apache.myfaces.adf.change.ComponentChange;

/**
 * An ChangeManager implementation that is all a no-op.
 *
 * @author The Oracle ADF Faces Team
 */
public class NullChangeManager extends ChangeManager
{
  /**
   * {@inheritDoc}
   */
  public void addComponentChange(
    FacesContext facesContext,
    UIComponent uiComponent,
    ComponentChange change)
  {
    // do nothing
  }
  
  /**
   * {@inheritDoc}
   */
  public Iterator getComponentChanges(FacesContext facesContext,
                             UIComponent uiComponent)
  {
    return null;
  }
  
  /**
   * {@inheritDoc}
   */
  public Iterator getComponentIdsWithChanges(FacesContext facesContext)
  {
    return null;
  }
}
