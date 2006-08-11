/*
 * Copyright 2004,2006 The Apache Software Foundation.
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
package org.apache.myfaces.trinidad.component;

import javax.faces.context.FacesContext;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.myfaces.trinidad.component.UIComponentTestCase;
import org.apache.myfaces.trinidad.component.UIXTableTest.DoNotCallBinding;

/**
 * Unit tests for UIXTree
 *
 * @author Arjuna Wijeyekoon
 */
public class UIXTreeTest extends UIComponentTestCase
{
  /**
   * @param testName  the unit test name
   */
  public UIXTreeTest(
    String testName)
  {
    super(testName);
  }
  
  @Override
  public void setUp()
  {
    super.setUp();
  }
  
  @Override
  public void tearDown()
  {
    super.tearDown();
  }
  
  public static Test suite()
  {
    return new TestSuite(UIXTreeTest.class);
  }


  /**
   * make sure that the model is not executed at invalid or unnecessary times.
   * valueBindings cannot be called during restoreState.
   * Also table model must not be executed if rendered="false".
   * However, saveState is called even if rendered="false" on a component.
   * Therefore, saveState should not call getValue() on the table.
   */
  public void testSaveRestoreStateGetValue()
  {
    // make sure that getValue() is not called during restoreState:
    DoNotCallBinding doNotCall = new DoNotCallBinding();
    doNotCall.doNotCall = true;
    final Object state;
    {
      UIXTree tree = _createTree();
      tree.setValueBinding("value", doNotCall);
      state = tree.processSaveState(facesContext);
    }

    UIXTree tree = _createTree();
    // this value binding should be restored during processRestoreState;
    // however, set it anyway just to catch any getValue() calls prior to
    // that.
    tree.setValueBinding("value", doNotCall);
    tree.processRestoreState(facesContext, state);

    assertTrue(tree.getValueBinding("value") instanceof DoNotCallBinding);

  }

  @Override
  public void setCurrentContext(FacesContext fc)
  {
    // prevent removal of facesContext before we are done testing:
    if (fc != null)
      super.setCurrentContext(fc);
  }


  public static void main(String[] args)
  {
    TestRunner.run(UIXTreeTest.class);
  }

  @Override
  protected boolean isRendererUsed()
  {
    // we use our own MockRenderer; not the one created by our super class:
    return false;
  }

  private UIXTree _createTree()
  {
    UIXTree tree = new UIXTree();
    return tree;
  }

}
