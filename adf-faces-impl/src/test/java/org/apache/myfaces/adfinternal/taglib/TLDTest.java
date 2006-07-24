/*
* Copyright 2006 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.myfaces.adfinternal.taglib;

import java.net.URL;

import org.apache.myfaces.adfinternal.XMLValidityTestCase;

public class TLDTest extends XMLValidityTestCase
{
  public TLDTest(
    String testName)
  {
    super(testName);
  }

  public void testAdfFacesCoreValidity() throws Throwable
  {
    URL dtdSource = getClass().getResource("/javax/servlet/jsp/resources/web-jsptaglibrary_1_2.dtd");
    String publicID = 
      "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN";
    URL tldSource = getClass().getResource("/META-INF/af.tld");
    executeValidityTest(dtdSource,
                        publicID,
                        tldSource);
  }

  public void testAdfFacesHtmlValidity() throws Throwable
  {
    URL dtdSource = getClass().getResource("/javax/servlet/jsp/resources/web-jsptaglibrary_1_2.dtd");
    String publicID = 
      "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN";
    URL tldSource = getClass().getResource("/META-INF/afh.tld");
    executeValidityTest(dtdSource,
                        publicID,
                        tldSource);
  }
}
