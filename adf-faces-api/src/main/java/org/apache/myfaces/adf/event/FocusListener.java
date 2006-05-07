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
package org.apache.myfaces.adf.event;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesListener;


/**
 * Listener for FocusEvents.
 * <p>
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-api/src/main/java/oracle/adf/view/faces/event/FocusListener.java#0 $) $Date: 10-nov-2005.19:09:02 $
 * @author The Oracle ADF Faces Team
 */
public interface FocusListener extends FacesListener
{
  public void processFocus(FocusEvent event)
    throws AbortProcessingException;
}
