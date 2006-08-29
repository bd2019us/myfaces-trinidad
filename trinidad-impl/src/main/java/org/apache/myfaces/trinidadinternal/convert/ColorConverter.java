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
package org.apache.myfaces.trinidadinternal.convert;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.myfaces.trinidad.convert.ClientConverter;
import org.apache.myfaces.trinidad.logging.TrinidadLogger;
import org.apache.myfaces.trinidad.util.MessageFactory;

import org.apache.myfaces.trinidadinternal.renderkit.core.xhtml.XhtmlUtils;
import org.apache.myfaces.trinidadinternal.share.text.RGBColorFormat;
import org.apache.myfaces.trinidadinternal.ui.laf.base.xhtml.XhtmlLafUtils;


/**
 * @author The Oracle ADF Faces Team
 */
public class ColorConverter extends org.apache.myfaces.trinidad.convert.ColorConverter
                            implements ClientConverter
{

  public String getClientLibrarySource(
   FacesContext context)
  {
    return null;
  }
  
  public Collection<String> getClientImportNames()
  {
    return _IMPORT_NAMES;
  }

  @SuppressWarnings("unchecked")
  public String getClientScript(FacesContext context, UIComponent component)
  {

    if ( component == null)
    {
      _LOG.severe("The component is null, but it is needed for the client id, so no script written");
      return null;
    }

    // Add a JavaScript Object to store the colorfield formats
    // on the client-side.  We currently store the format string
    // for each and every field.  It'd be more efficient to have
    // an array of formats, then store for each field the
    // index of the format, especially if we could delay outputting
    // these objects to when the <form> closes.

    String clientId = component.getClientId(context);

    if (clientId != null)
    {
      // FIX - figure out size!!!
      StringBuffer buff = new StringBuffer();

      Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
      // =-=JRF Only if Javascript...
      if (requestMap.get(_PATTERN_WRITTEN_KEY) == null)
      {
        requestMap.put( _PATTERN_WRITTEN_KEY, Boolean.TRUE);

        buff.append("_cfTrans=\"");
        buff.append(XhtmlUtils.escapeJS(getTransparentString(context)));
        buff.append("\";");

        buff.append("var _cfs=new Object();");
        buff.append("var _cfts=new Object();");
      }

      String[] patterns = getPatterns();
      buff.append("_cfs[\"");
      buff.append(clientId);
      buff.append("\"]=");

      if (patterns.length == 1)
      {
        buff.append("\"");
        buff.append(patterns[0]);
        buff.append("\"");
      }
      else
      {
        buff.append("new Array(");

        for (int i = 0; i < patterns.length; i++)
        {
          buff.append("\"");
          buff.append(patterns[i]);
          buff.append("\"");

          if (i < (patterns.length - 1))
            buff.append(",");
        }

        buff.append(")");
      }

      buff.append(";");

      if (isTransparentAllowed())
      {
        buff.append("_cfts[\"");
        buff.append(clientId);
        buff.append("\"]=true;");
      }

      return buff.toString();
    }
    else
    {
      _LOG.severe("Client id is null, no script rendered");
    }

    return null;

  }

  /**
   * @todo add message detail as part of RGBColorFormat constructor.
   */
  public String getClientConversion(FacesContext context, UIComponent component)
  {
    int patternsArgSize = _getPatternsArgSize();

    int size = 19 + patternsArgSize + 19;
    StringBuffer sb = new StringBuffer(size);

    StringBuffer patterns = new StringBuffer();
    String[] setPatterns = getPatterns();
    for (int i = 0; i < setPatterns.length ; i++)
    {
      patterns.append(setPatterns[i]);
      patterns.append(' ');
    }
    String patternsString = patterns.toString();

    sb.append("new RGBColorFormat(");

    _appendPatternsArg(sb);

    if (isTransparentAllowed())
    {
      sb.append(",true,'");
    }
    else
    {
      sb.append(",false,'");
    }
    
    FacesMessage msg = _getConvertFacesMessage(context);
    sb.append(XhtmlLafUtils.escapeJS(msg.getSummary())); 
    sb.append("','");
    sb.append(XhtmlLafUtils.escapeJS(msg.getDetail())); 
    sb.append("','");
    sb.append(XhtmlLafUtils.escapeJS(patternsString));
    sb.append("')");

    return sb.toString();
  }


  public int getColumns(
    FacesContext context)
  {
    int columns = 0;

    if (isTransparentAllowed())
      columns = getTransparentString(context).length();

    String[] patterns = getPatterns();
    for (int i=0; i < patterns.length; i++)
      columns = Math.max(columns, new RGBColorFormat(patterns[i]).length());

    return columns;
  }

  // Returns the length of the patterns argument
  private int _getPatternsArgSize()
  {
    String[] patterns = this.getPatterns();
    int count = patterns.length;

    if (count == 1)
      return patterns[0].length();

    int size = 11; // Leave room for "new Array()"

    for (int i = 0; i < count; i++)
    {
      // Include room for the pattern, comma, and quotes
      size += (patterns[i].length() + 3);
    }

    return size;
  }

  // Appends the patterns argument to the StringBuffer
  private void _appendPatternsArg(StringBuffer buffer)
  {
    String[] patterns = getPatterns();
    int count = patterns.length;

    if (count == 1)
    {
      buffer.append("'");
      buffer.append(patterns[0]);
      buffer.append("'");
    }
    else
    {
      buffer.append("new Array(");

      for (int i = 0; i < count; i++)
      {
        buffer.append("'");
        buffer.append(patterns[i]);
        buffer.append("'");

        if (i < (count - 1))
          buffer.append(",");
      }

      buffer.append(")");
    }
  }

  private FacesMessage _getConvertFacesMessage(FacesContext context)
  {
    String convMsgDet = getConvertMessageDetail();

   
    // will get replaced in javascript
    Object[] params = new Object[] {"{0}", "{1}", "{2}"};

    return MessageFactory.getMessage(context, CONVERT_MESSAGE_ID,
                                           convMsgDet, params);
  }
  
  private static final Collection<String> _IMPORT_NAMES = Collections.singletonList( "ColorFormat()" );
  private static final TrinidadLogger _LOG = TrinidadLogger.createTrinidadLogger(ColorConverter.class);
  private static final String _PATTERN_WRITTEN_KEY = "org.apache.myfaces.trinidadinternal.convert.ColorConverter._PATTERN_WRITTEN";
}
