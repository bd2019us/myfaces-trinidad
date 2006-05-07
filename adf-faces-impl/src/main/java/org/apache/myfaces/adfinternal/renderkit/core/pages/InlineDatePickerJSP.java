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

package org.apache.myfaces.adfinternal.renderkit.core.pages;

import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.myfaces.adf.component.html.HtmlBody;
import org.apache.myfaces.adf.component.html.HtmlHtml;

import org.apache.myfaces.adfinternal.renderkit.AdfRenderingContext;
import org.apache.myfaces.adfinternal.renderkit.core.xhtml.XhtmlConstants;


/**
 * Entry point for the embedded calendar functionality.
 * <p>
 * Parameters:
 * <ul>
 * <li>Java locale to use
 * <li>tzOffset: the time zone offset in hours
 * <li>minValue, maxValue: min and max dates
 * <li>value: current value
 * <li>scrolledValue: first visible value
 * </ul>
 * <p>
 *
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/renderkit/core/pages/InlineDatePickerJSP.java#0 $) $Date: 10-nov-2005.19:03:35 $
 * @author The Oracle ADF Faces Team
 */
class InlineDatePickerJSP
{
  static public void service(FacesContext context)
    throws IOException
  {
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    AdfRenderingContext arc = AdfRenderingContext.getCurrentInstance();
    arc.getPartialPageContext().addPartialTarget(
              (String) requestParams.get(XhtmlConstants.SOURCE_PARAM));

    // Use Html and Body to avoid the cost of the stylesheet
    HtmlHtml html = new HtmlHtml();
    context.getViewRoot().getChildren().add(html);

    HtmlBody body = new HtmlBody();
    html.getChildren().add(body);

    body.getChildren().add(CalendarUtils.createChooseDate(context));
  }
}
