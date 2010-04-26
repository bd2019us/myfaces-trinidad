/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.trinidaddemo.components.buttonsAndLinks.commandLink;

import org.apache.myfaces.trinidaddemo.support.impl.AbstractComponentDemo;
import org.apache.myfaces.trinidaddemo.support.ComponentDemoId;

import java.util.List;
import java.util.ArrayList;

/**
 *
 */
public class CommandLinkDemo extends AbstractComponentDemo {

    private static final long serialVersionUID = -1982061956883408710L;

	/**
	 * Constructor.
	 */
	public CommandLinkDemo() {
		super(ComponentDemoId.commandLink , "Command Link",
            new String[]{
                "/components/buttonsAndLinks/commandLink/commandLink.xhtml"
            });
	}

    public String getSummaryResourcePath() {
        return "/components/buttonsAndLinks/commandLink/summary.xhtml";
    }

    public String getBackingBeanResourcePath() {
		return "/org/apache/myfaces/trinidaddemo/components/buttonsAndLinks/commandLink/CommandLinkBean.java";
	}

    public String getSkinDocumentationLink(){
        return null;
    }
}
