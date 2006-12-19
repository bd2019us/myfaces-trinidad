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
package org.apache.myfaces.trinidadinternal.validator;

import java.util.Collection;
import java.util.Collections;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.myfaces.trinidad.util.MessageFactory;
import org.apache.myfaces.trinidad.validator.ClientValidator;
import org.apache.myfaces.trinidadinternal.renderkit.core.xhtml.XhtmlUtils;

public class RegExpValidator
                       extends org.apache.myfaces.trinidad.validator.RegExpValidator
                         implements ClientValidator
{
  public RegExpValidator()
  {
    super();
  }

  /**
   * Opportunity for the ClientValidator to return script content.
   * For HTML, this will be javascript that will be embedded in a
   * script tag. For HTML this method is expected to return an
   * implementation of the javascript Validator object.
   * <p>This method will be called once per validator instance.
   * Content that should only be written once per request
   * should only be returned once.
   */
  public String getClientScript(FacesContext context, UIComponent component)
  {
    return null;
  }

  /**
   * Called to retrieve the appropriate client
   * validation code for the node and context.
   * For HTML, this will be javascript that will be embedded in a
   * script tag. For HTML this method is expected to return a
   * constructor of the javascript Validator object
   * returned by getClientScript().
   *
   */
  public String getClientValidation(FacesContext context, UIComponent component)
  {
   
    String jsPattern = XhtmlUtils.escapeJS(getPattern());
    
    StringBuilder outBuffer = new StringBuilder(22
                                              + jsPattern.length());

    outBuffer.append("new TrRegExpValidator('"); // 21
    outBuffer.append(jsPattern);
    _applyCustomMessage(context, outBuffer);
    outBuffer.append("')");                // 1

    return outBuffer.toString();
  }
  
  private void _applyCustomMessage(FacesContext context, StringBuilder outBuffer)
  {
    String noMatchMsg = getMessageDetailNoMatch();
    if(noMatchMsg != null)
    {
      Object[] params = new Object[] {"{0}", "{1}", "{2}"};

      FacesMessage message = MessageFactory.getMessage(context,
                                  RegExpValidator.NO_MATCH_MESSAGE_ID,
                                  noMatchMsg,
                                  params);
      String esNoMatchMsgPattern = XhtmlUtils.escapeJS(message.getDetail());
      outBuffer.append("','");
      outBuffer.append(esNoMatchMsgPattern);
    }    

  }
  
  public Collection<String> getClientImportNames()
  {
    return _IMPORT_NAMES;
  }
  
  public String getClientLibrarySource(
   FacesContext context)
  {
    return null;
  }

  private static final Collection<String> _IMPORT_NAMES = Collections.singletonList( "TrRegExpValidator()" );     
}