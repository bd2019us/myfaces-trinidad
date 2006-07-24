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
package org.apache.myfaces.adf.component;

import java.io.IOException;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;

import org.apache.myfaces.adf.logging.ADFLogger;
import org.apache.myfaces.adf.event.SelectionEvent;
import org.apache.myfaces.adf.model.CollectionModel;

/**
 * Base class for components that do stamping.
 * This class set the EL 'var' variable correctly when the rowData changes.
 * And it wraps events that are queued, so that the correct rowData can be
 * restored on this component when the event is broadcast.
 * @author The Oracle ADF Faces Team
 */
public abstract class UIXCollection extends UIXComponentBase
  implements NamingContainer
{
  protected UIXCollection(String rendererType)
  {
    super(rendererType);
  }

  protected UIXCollection()
  {
    this(null);
  }


  /**
   * Queues an event. If there is a currency set on this table, then
   * the event will be wrapped so that when it is finally delivered, the correct
   * currency will be restored.
   * @param event a FacesEvent
   */
  public void queueEvent(FacesEvent event)
  {
    if (event.getSource() == this)
    {
      // Remember non-SelectionEvents on ourselves.  This
      // is a hack to support validation in tableSelectXyz.
      if (!(event instanceof SelectionEvent))
      {
        InternalState iState = _getInternalState(true);
        iState._hasEvent = true;
      }
    }

    // we want to wrap up
    // the event so we can execute it in the correct context (with the correct
    // rowKey/rowData):
    Object currencyKey = getRowKey();
    event = new TableRowEvent(this, event, currencyKey);
    super.queueEvent(event);
  }

  /**
   * Delivers a wrapped event to the appropriate component.
   * If the event is a special wrapped event, it is unwrapped.
   * @param event a FacesEvent
   * @throws javax.faces.event.AbortProcessingException
   */
  public void broadcast(FacesEvent event)
    throws AbortProcessingException
  {
    // For "TableRowEvents", set up the data before firing the
    // event to the actual component.
    if (event instanceof TableRowEvent)
    {
      TableRowEvent rowEvent = (TableRowEvent) event;
      Object old = getRowKey();
      setRowKey(rowEvent.getCurrencyKey());
      FacesEvent wrapped = rowEvent.getEvent();
      wrapped.getComponent().broadcast(wrapped);
      setRowKey(old);
    }
    else
    {
      super.broadcast(event);
    }
  }

  /**
   * Decodes this component before decoding the facets.
   * Decodes the children as many times as they are stamped.
   * @param context the FacesContext
   */
  public final void processDecodes(FacesContext context)
  {
    if (context == null)
      throw new NullPointerException();

    _init();

    InternalState iState = _getInternalState(true);
    iState._isFirstRender = false;

    if (!isRendered())
      return;

    _flushCachedModel();

    // Make sure _hasEvent is false.
    iState._hasEvent = false;

    // =-=AEW Because I'm getting the state in decode(), I need
    // to do it before iterating over the children - otherwise,
    // they'll be working off the wrong startIndex.  When state
    // management is integrated, I can likely put this back in the
    // usual order

    // Process this component itself
    decode(context);

    // Process all facets and children of this component
    decodeChildren(context);
  }

  protected void decodeChildrenImpl(FacesContext context)
  {
    processFacetsAndChildren(context, PhaseId.APPLY_REQUEST_VALUES);
  }

  protected void validateChildrenImpl(FacesContext context)
  {
    processFacetsAndChildren(context, PhaseId.PROCESS_VALIDATIONS);
  }

  protected void updateChildrenImpl(FacesContext context)
  {
    processFacetsAndChildren(context, PhaseId.UPDATE_MODEL_VALUES);
  }

  /**
   * Resets this component's stamps to their
   * uninitialized state. This is useful when the user wants to
   * undo any edits made to an editable table.
   */
  public void resetStampState()
  {
    InternalState iState = _getInternalState(true);
    // TODO: this is over kill. for eg, It clears out any toggled showDetails.
    Object initKey = _getCurrencyKeyForInitialStampState();
    // do not clear the initial stamp state: bug 4862103:
    iState._stampState.clear(initKey);
  }

  public Object processSaveState(FacesContext context)
  {
    // If we saved state in the middle of processing a row,
    // then make sure that we revert to a "null" rowKey while
    // saving state;  this is necessary to ensure that the
    // current row's state is properly preserved, and that
    // the children are reset to their default state.
    Object currencyKey = _getCurrencyKey();
    Object initKey = _getCurrencyKeyForInitialStampState();
    if (currencyKey != initKey) // beware of null currencyKeys if equals() is used
    {
      setRowKey(initKey);
    }

    Object savedState = super.processSaveState(context);

    if (currencyKey != initKey) // beware of null currencyKeys if equals() is used
    {
      setRowKey(currencyKey);
    }

    return savedState;
  }

  public Object saveState(FacesContext context)
  {
    // _stampState is stored as an instance variable, so it isn't
    // automatically saved
    Object superState = super.saveState(context);
    Object stampState;
    ValueMap currencyCache;

    // becareful not to create the internal state too early:
    // otherwise, the internal state will be shared between
    // nested table stamps:
    InternalState iState = _getInternalState(false);
    if (iState != null)
    {
      stampState = iState._stampState;
      currencyCache = iState._currencyCache;
      if ((currencyCache != null) && currencyCache.isEmpty())
        currencyCache = null;
    }
    else
    {
      stampState = null;
      currencyCache = null;
    }

    if ((superState != null) || (stampState != null) || (currencyCache != null))
      return new Object[]{superState, stampState, currencyCache};
    return null;
  }


  public void restoreState(FacesContext context, Object state)
  {
    final Object superState, stampState, currencyCache;
    Object[] array = (Object[]) state;
    if (array != null)
    {
      superState = array[0];
      stampState = array[1];
      currencyCache = array[2];
    }
    else
    {
      superState = stampState = currencyCache = null;
    }
    super.restoreState(context, superState);

    if ((stampState != null) || (currencyCache != null))
    {
      InternalState iState = _getInternalState(true);
      iState._stampState = (StampState) stampState;
      iState._currencyCache = (ValueMap) currencyCache;
    }
    else
    {
      // becareful not to force creation of the internal state
      // too early:
      InternalState iState = _getInternalState(false);
      if (iState != null)
      {
        iState._stampState = null;
        iState._currencyCache = null;
      }
    }
  }

  /**
   * Checks to see if the current row is available. This is useful when the
   * total number of rows is not known.
   * @see CollectionModel#isRowAvailable
   * @return true iff the current row is available.
   */
  public final boolean isRowAvailable()
  {
    return getCollectionModel().isRowAvailable();
  }

  /**
   * Gets the total number of rows in this table.
   * @see CollectionModel#getRowCount
   * @return -1 if the total number is not known.
   */
  public final int getRowCount()
  {
    return getCollectionModel().getRowCount();
  }

  /**
   * Gets the index of the current row.
   * @see CollectionModel#getRowIndex
   * @return -1 if the current row is unavailable.
   */
  public final int getRowIndex()
  {
    return getCollectionModel().getRowIndex();
  }

  /**
   * Gets the rowKey of the current row.
   * @see CollectionModel#getRowKey
   * @return null if the current row is unavailable.
   */
  public final Object getRowKey()
  {
    InternalState iState = _getInternalState(true);
    if (iState._currentRowKey == _NULL)
    {
      // See bug 4534104.
      // Sometimes the rowKey for a particular row changes during update model
      // (this happens in ADFM if you edit the primary key of a row).
      // It is bad if the rowKey changes after _restoreStampState() and
      // before _saveStampState(). Therefore, we cache it:
      iState._currentRowKey = getCollectionModel().getRowKey();
    }

    return iState._currentRowKey;
  }

  /**
   * Gets the data for the current row.
   * @see CollectionModel#getRowData
   * @return null if the current row is unavailable
   */
  public final Object getRowData()
  {
    CollectionModel model = getCollectionModel();
    // we need to call isRowAvailable() here because the 1.0 sun RI was
    // throwing exceptions when getRowData() was called with rowIndex=-1
    return model.isRowAvailable() ? model.getRowData() : null;
  }

  /**
   * Checks to see if the row at the given index is available.
   * @see CollectionModel#isRowAvailable(int)
   * @param rowIndex the index of the row to check.
   * @return true if data for the row exists.
   */
  public boolean isRowAvailable(int rowIndex)
  {
    return getCollectionModel().isRowAvailable(rowIndex);
  }

  /**
   * Gets the rowData at the given index.
   * @see CollectionModel#getRowData(int)
   * @param rowIndex the index of the row to get data from.
   * @return the data for the given row.
   */
  public Object getRowData(int rowIndex)
  {
    return getCollectionModel().getRowData(rowIndex);
  }

  /**
   * Gets the EL variable name to use when iterating through the collection's data.
   */
  public abstract String getVar();

  /**
   * Gets the EL variable name to use to expose the varStatusMap.
   * @see #createVarStatusMap()
   */
  public abstract String getVarStatus();

  /**
   * Makes a row current.
   * This method calls {@link #preRowDataChange} and
   * {@link #postRowDataChange} as appropriate.
   * @see CollectionModel#setRowKey
   * @param rowKey The rowKey of the row that should be made current. Use null
   * to clear the current row.
   */
  public void setRowKey(Object rowKey)
  {
    preRowDataChange();
    getCollectionModel().setRowKey(rowKey);
    postRowDataChange();
    if (_LOG.isFine() && (rowKey != null) && (!isRowAvailable()))
      _LOG.fine("no row available for rowKey:"+rowKey);
  }

  /**
   * Makes a row current.
   * This method calls {@link #preRowDataChange} and
   * {@link #postRowDataChange} as appropriate.
   * @see CollectionModel#setRowIndex
   * @param rowIndex The rowIndex of the row that should be made current. Use -1
   * to clear the current row.
   */
  public void setRowIndex(int rowIndex)
  {
    preRowDataChange();
    getCollectionModel().setRowIndex(rowIndex);
    postRowDataChange();
    if (_LOG.isFine() && (rowIndex != -1) && (!isRowAvailable()))
      _LOG.fine("no row available for rowIndex:"+rowIndex);
  }

  /**
   * @param property a property name in the model
   * @return  true if the model is sortable by the given property.
   * @see CollectionModel#isSortable
   */
  public final boolean isSortable(String property)
  {
    return getCollectionModel().isSortable(property);
  }

  /**
   * Sorts this collection by the given criteria.
   * @param criteria Each element in this List must be of type SortCriterion.
   * @see org.apache.myfaces.adf.model.SortCriterion
   * @see CollectionModel#setSortCriteria
   */
  public void setSortCriteria(List criteria)
  {
    getCollectionModel().setSortCriteria(criteria);
  }

  /**
   * Gets the criteria that this collection is sorted by.
   * @return each element in this List is of type SortCriterion.
   * An empty list is returned if this collection is not sorted.
   * @see org.apache.myfaces.adf.model.SortCriterion
   * @see CollectionModel#getSortCriteria
   */
  public final List getSortCriteria()
  {
    return getCollectionModel().getSortCriteria();
  }

  /**
   * Clears all the currency strings.
   */
  public final void encodeBegin(FacesContext context) throws IOException
  {
    _init();

    _getCurrencyCache().clear();
    _flushCachedModel();

    Object assertKey = null;
    assert ((assertKey = getRowKey()) != null) || true;
    __encodeBegin(context);
    // make sure that the rendering code preserves the currency:
    assert _assertKeyPreserved(assertKey) : "CurrencyKey not preserved";
  }

  public void encodeEnd(FacesContext context) throws IOException
  {
    Object assertKey = null;
    assert ((assertKey = getRowKey()) != null) || true;
    super.encodeEnd(context);
    // make sure that the rendering code preserves the currency:
    assert _assertKeyPreserved(assertKey) : "CurrencyKey not preserved";
  }

  private boolean _assertKeyPreserved(Object oldKey)
  {
    Object newKey = getRowKey();
    return (oldKey != null) ? oldKey.equals(newKey) : (newKey == null);
  }

  void __encodeBegin(FacesContext context) throws IOException
  {
    super.encodeBegin(context);
  }

  /**
   * Checks to see if processDecodes was called. If this returns true
   * then this is the initial request, and processDecodes has not been called.
   */
  boolean __isFirstRender()
  {
    InternalState iState = _getInternalState(true);
    return iState._isFirstRender;
  }

  /**
   * Gets a short currency string that can be used to setup this component later.
   * All the information needed to restore the rowData on this component
   * (for example: rowkey) is encoded on this currency string.
   * However, this currency string is very short. As a result, its lifetime is
   * also very short. Currency strings are valid across one request only.
   * They come into existence during the encode phase, and they are cleared before
   * the start of the encode phase on the next request.
   * @see #setCurrencyString
   */
  public String getCurrencyString()
  {
    // only call getCurrencyKey if we already have a dataModel.
    // otherwise behave as though no currency was set.
    // we need to do this because we don't want dataModel created for components
    // that are not rendered. The faces RI calls getClientId even on components
    // that are not rendered and this in turn was calling this method:
    Object currencyObject = _getCurrencyKey();
    if (currencyObject == null)
      return null;

    Object initKey = _getCurrencyKeyForInitialStampState();
    if (_equals(currencyObject, initKey))
      return null;

    String key = _getToken(currencyObject);
    return key;
  }

  private String _getToken(Object rowKey)
  {
    assert rowKey != null;

    ValueMap currencyCache = _getCurrencyCache();
    String key = (String) currencyCache.get(rowKey);
    if (key == null)
    {
      if (rowKey instanceof String)
      {
        // TODO: make sure that this string is suitable for use as
        // NamingContainer ids:
        key = rowKey.toString();
        if (_isOptimizedKey(key))
        {
          // no need to add to the token map:
          return key;
        }
      }

      key = _createToken(currencyCache);

      if (_LOG.isFiner())
        _LOG.finer("Storing token:"+key+
                   " for rowKey:"+rowKey);

      currencyCache.put(rowKey, key);
    }
    return key;
  }

  private boolean _isOptimizedKey(String key)
  {
    // if a key could be a number, then it might conflict with our
    // internal representation of tokens. Therefore, if a key could be
    // a number, then use the token cache.
    // if there is no way this key can be a number, then it can
    // be treated as an optimized key and can bypass the token cache
    // system:
    return ((key.length() > 0) && (!Character.isDigit(key.charAt(0))));
  }

  private String _createToken(ValueMap currencyCache)
  {
    String key = String.valueOf(currencyCache.size());
    return key;
  }

  /**
   * This is a safe way of getting currency keys and not accidentally forcing
   * the model to execute. When rendered="false" we should never execute the model.
   * However, the JSF engine calls certain methods when rendered="false" such as
   * processSaveState and getClientId.
   * Those methods, in turn, get the CurrencyKey.
   */
  private Object _getCurrencyKey()
  {
    // use false so that we don't create an internal state.
    // if the internal state is created too soon, then the same internal
    // state will get shared across all nested table instances.
    // this was causing bug 4616844:
    InternalState iState = _getInternalState(false);
    if (iState == null)
      return null;

    return (iState._model != null)
      ? getRowKey()
      : _getCurrencyKeyForInitialStampState();
  }

  /**
   * Restores this component's rowData to be what it was when the given
   * currency string was created.
   * <P>
   * All the information needed to restore the rowData on this component
   * (for example: rowkey) is encoded on the given currency string.
   * However, this currency string is very short. As a result, its lifetime is
   * also very short. Currency strings are valid across one request only.
   * They come into existence during the encode phase, and they are cleared before
   * the start of the encode phase on the next request.
   * <P>
   * This method gets the corresponding currencyKey and passes it to
   * {@link #setCurrencyKey}
   * @see #getCurrencyString
   */
  public void setCurrencyString(String currency)
  {
    if (currency == null)
    {
      setRowKey(_getCurrencyKeyForInitialStampState());
      return;
    }

    Object currencyObject = _isOptimizedKey(currency)
      ? currency
      : _getCurrencyCache().getKey(currency);
    if (currencyObject == null)
    {
      _LOG.severe("Could not restore currency for currencyString:"+currency);
    }
    else
      setRowKey(currencyObject);
  }

  /**
   * Gets the client-id of this component, without any NamingContainers.
   * This id changes depending on the currency Object.
   * Because this implementation uses currency strings, the local client ID is
   * not stable for very long. Its lifetime is the same as that of a
   * currency string.
   * @see #getCurrencyString
   * @return the local clientId
   */
  protected final String getLocalClientId()
  {
    String id = super.getLocalClientId();
    String key = getCurrencyString();
    if (key != null)
    {
      id += NamingContainer.SEPARATOR_CHAR + key;
    }

    return id;
  }

  /**
   * Prepares this component for a change in the rowData.
   * This method should be called right before the rowData changes.
   * It saves the internal states of all the stamps of this component
   * so that they can be restored when the rowData is reverted.
   */
  protected final void preRowDataChange()
  {
    _saveStampState();
    InternalState iState = _getInternalState(true);
    // mark the cached rowKey as invalid:
    iState._currentRowKey = _NULL;
  }

  /**
   * Sets up this component to use the new rowData.
   * This method should be called right after the rowData changes.
   * It sets up the var EL variable to be the current rowData.
   * It also sets up the internal states of all the stamps of this component
   * to match this new rowData.
   */
  protected final void postRowDataChange()
  {
    Object rowData = getRowData();
    if (_LOG.isFinest() && (rowData == null))
    {
      _LOG.finest("rowData is null at rowIndex:"+getRowIndex()+
                  " and currencyKey:"+getRowKey());
    }

    InternalState iState = _getInternalState(true);
    if (rowData == null)
    {
      // if the rowData is null, then we will restore the EL 'var' variable
      // to be whatever the value was, before this component started rendering:
      if (iState._prevVarValue != _NULL)
      {
        _setELVar(iState._var, iState._prevVarValue);
        iState._prevVarValue = _NULL;
      }
      if (iState._prevVarStatus != _NULL)
      {
        _setELVar(iState._varStatus, iState._prevVarStatus);
        iState._prevVarStatus = _NULL;
      }
    }
    else
    {
      if (iState._var != null)
      {
        Object oldData = _setELVar(iState._var, rowData);
        if (iState._prevVarValue == _NULL)
          iState._prevVarValue = oldData;
      }

      // varStatus is not set per row. It is only set once.
      // if _PrevVarStatus has not been assigned, then we have not set the
      // varStatus yet:
      if ((iState._varStatus != null) && (iState._prevVarStatus == _NULL))
      {
        Map varStatusMap = createVarStatusMap();
        iState._prevVarStatus = _setELVar(iState._varStatus, varStatusMap);
      }
    }

    _restoreStampState();
  }

  /**
   * Gets the UIComponents that are considered stamps.
   * This implementation simply returns the children of this component.
   * @return each element must be of type UIComponent.
   */
  protected List getStamps()
  {
    return getChildren();
  }

  /**
   * Gets the currencyObject to setup the rowData to use to build initial
   * stamp state.
   */
  private Object _getCurrencyKeyForInitialStampState()
  {
    InternalState iState = _getInternalState(false);
    if (iState == null)
      return null;

    Object rowKey = iState._initialStampStateKey;
    return (rowKey == _NULL) ? null : rowKey;
  }

  /**
   * Saves the state of a stamp. This method is called when the currency of this
   * component is changed so that the state of this stamp can be preserved, before
   * the stamp is updated with the state corresponding to the new currency.
   * This method recurses for the children and facets of the stamp.
   * @return this object must be Serializable if client-side state saving is
   * used.
   */
  protected Object saveStampState(FacesContext context, UIComponent stamp)
  {
    Object[] state = new Object[3];
    state[0] = StampState.saveStampState(context, stamp);

    int facetCount = _getFacetCount(stamp);
    Object[] facetState;
    if (facetCount == 0)
      facetState = _EMPTY_ARRAY;
    else
    {
      facetState = new Object[facetCount * 2];
      Map facetMap = stamp.getFacets();
      Iterator facets = facetMap.entrySet().iterator();
      for (int i=0; facets.hasNext(); i++)
      {
        Map.Entry entry = (Map.Entry) facets.next();
        facetState[i * 2] = entry.getKey();
        facetState[(i * 2) + 1] = saveStampState(context,
                                                 (UIComponent) entry.getValue());
      }
    }

    state[1] = facetState;

    int childCount = stamp.getChildCount();
    Object[] childState;
    if (childCount == 0)
    {
      childState = _EMPTY_ARRAY;
    }
    else
    {
      List children = stamp.getChildren();
      childState = new Object[childCount];
      for(int i=0; i < childCount; i++)
      {
        UIComponent child = (UIComponent) children.get(i);
        childState[i] = saveStampState(context, child);
      }
    }

    state[2] = childState;

    return state;
  }

  /**
   * Restores the state of a stamp. This method is called after the currency of this
   * component is changed so that the state of this stamp can be changed
   * to match the new currency.
   * This method recurses for the children and facets of the stamp.
   */
  protected void restoreStampState(FacesContext context, UIComponent stamp,
                                   Object stampState)
  {
    Object[] state = (Object[]) stampState;
    StampState.restoreStampState(context, stamp, state[0]);

    Object[] facetState = (Object[]) state[1];
    for(int i=0; i<facetState.length; i+=2)
    {
      String facetName = (String) facetState[i];
      restoreStampState(context, stamp.getFacet(facetName), facetState[i + 1]);
    }

    List children = stamp.getChildren();
    Object[] childState = (Object[]) state[2];
    for(int i=0; i<childState.length; i++)
    {
      UIComponent child = (UIComponent) children.get(i);
      restoreStampState(context, child, childState[i]);
    }
  }

  /**
   * Process a component.
   * This method calls {@link #processDecodes},
   * {@link #processValidators} or
   * {@link #processUpdates}
   * depending on the {#link PhaseId}.
   */
  protected final void processComponent(
    FacesContext context,
    UIComponent  component,
    PhaseId      phaseId)
  {
    if (component != null)
    {
      if (phaseId == PhaseId.APPLY_REQUEST_VALUES)
        component.processDecodes(context);
      else if (phaseId == PhaseId.PROCESS_VALIDATIONS)
        component.processValidators(context);
      else if (phaseId == PhaseId.UPDATE_MODEL_VALUES)
        component.processUpdates(context);
      else
        throw new IllegalArgumentException("Bad PhaseId:"+phaseId);
    }
  }

  /**
   * Process this component's facets and children.
   * This method should call {@link #processComponent}
   * as many times as necessary for each facet and child.
   * {@link #processComponent}
   * may be called repeatedly for the same child if that child is
   * being stamped.
   */
  protected abstract void processFacetsAndChildren(
    FacesContext context,
    PhaseId phaseId);

  /**
   * Gets the CollectionModel to use with this component.
   */
  protected final CollectionModel getCollectionModel()
  {
    return getCollectionModel(true);
  }

  /**
   * Gets the CollectionModel to use with this component.
   *
   * @param createIfNull  creates the collection model if necessary
   */
  protected final CollectionModel getCollectionModel(
    boolean createIfNull)
  {
    InternalState iState = _getInternalState(true);
    if (iState._model == null && createIfNull)
    {
      //  _init() is usually called from either processDecodes or encodeBegin.
      //  Sometimes both processDecodes and encodeBegin may not be called,
      //  but processSaveState is called (this happens when
      //  component's rendered attr is set to false). We need to make sure that
      //  _init() is called in that case as well. Otherwise we get nasty NPEs.
      //  safest place is to call it here:
      _init();

      iState._value = getValue();
      iState._model = createCollectionModel(null, iState._value);
      assert iState._model != null;
    }
    // model might not have been created if createIfNull is false:
    if ((iState._initialStampStateKey == _NULL) &&
        (iState._model != null))
    {
      // if we have not already initialized the initialStampStateKey
      // that means that we don't have any stamp-state to use as the default
      // state for rows that we have not seen yet. So...
      // we will use any stamp-state for the initial rowKey on the model
      // as the default stamp-state for all rows:
      iState._initialStampStateKey = iState._model.getRowKey();
    }
    return iState._model;
  }

  /**
   * Creates the CollectionModel to use with this component.
   * @param current the current CollectionModel, or null if there is none.
   * @param value this is the value returned from {@link #getValue()}
   */
  protected abstract CollectionModel createCollectionModel(
    CollectionModel current,
    Object value);

  /**
   * Gets the value that must be converted into a CollectionModel
   */
  protected abstract Object getValue();

  /**
   * Gets the Map to use as the "varStatus".
   * This implementation supports the following keys:<ul>
   * <li>model - returns the CollectionModel
   * <li>index - returns the current rowIndex
   * <li>rowKey - returns the current rowKey
   * <li>current - returns the current rowData
   * </ul>
   */
  protected Map createVarStatusMap()
  {
    return new AbstractMap()
    {
      public Object get(Object key)
      {
        // some of these keys are from <c:forEach>, ie:
        // javax.servlet.jsp.jstl.core.LoopTagStatus
        if ("model".equals(key))
          return getCollectionModel();
        if ("rowKey".equals(key))
          return getRowKey();
        if ("index".equals(key)) // from jstl
          return new Integer(getRowIndex());
        if ("current".equals(key)) // from jstl
          return getRowData();
        return null;
      }

      public Set entrySet()
      {
        return Collections.EMPTY_SET;
      }
    };
  }

  /**
   * override this method to place initialization code that must run
   * once this component is created and the jsp engine has finished setting
   * attributes on it.
   */
  void __init()
  {
    InternalState iState = _getInternalState(true);
    iState._var = getVar();
    if (_LOG.isFine() && (iState._var == null))
    {
      _LOG.fine("'var' attribute is null.");
    }
    iState._varStatus = getVarStatus();
    if (_LOG.isFinest() && (iState._varStatus == null))
    {
      _LOG.finest("'varStatus' attribute is null.");
    }
 }

  private void _init()
  {
    InternalState iState = _getInternalState(true);
    if (!iState._isInitialized)
    {
      assert iState._model == null;
      iState._isInitialized = true;
      __init();
    }
  }

  private void _flushCachedModel()
  {
    InternalState iState = _getInternalState(true);
    Object value = getValue();
    if (iState._value != value)
    {
      iState._value = value;
      iState._model = createCollectionModel(iState._model, value);
    }
  }

  /**
   * Gets the internal state of this component.
   * This is to support table within table.
   */
  Object __getMyStampState()
  {
    return _state;
  }

  /**
   * Sets the internal state of this component.
   * This is to support table within table.
   * @param stampState the internal state is obtained from this object.
   */
  void __setMyStampState(Object stampState)
  {
    InternalState iState = (InternalState) stampState;
    _state = iState;
  }

  /**
   * Returns true if an event (other than a selection event)
   * has been queued for this component.  This is a hack
   * to support validation in the tableSelectXyz components.
   */
  boolean __hasEvent()
  {
    InternalState iState = _getInternalState(true);
    return iState._hasEvent;
  }

  /**
   * Saves the state of all the stamps of this component.
   * This method should be called before the rowData of this component
   * changes. This method gets all the stamps using {@link #getStamps} and
   * saves their states by calling {@link #saveStampState}.
   */
  private void _saveStampState()
  {
    InternalState iState = _getInternalState(true);
    StampState stampState = _getStampState();
    FacesContext context = getFacesContext();
    Object currencyObj = getRowKey();
    Iterator stamps = getStamps().iterator();
    int position = 0;
    while(stamps.hasNext())
    {
      UIComponent stamp = (UIComponent) stamps.next();
      Object state = saveStampState(context, stamp);
//      String stampId = stamp.getId();
      // TODO
      // temporarily use position. later we need to use ID's to access
      // stamp state everywhere, and special case NamingContainers:
      String stampId = String.valueOf(position++);
      stampState.put(currencyObj, stampId, state);
      if (_LOG.isFinest())
        _LOG.finest("saving stamp state for currencyObject:"+currencyObj+
          " and stampId:"+stampId);
    }
  }

  /**
   * Restores the state of all the stamps of this component.
   * This method should be called after the currency of this component
   * changes. This method gets all the stamps using {@link #getStamps} and
   * restores their states by calling
   * {@link #restoreStampState}.
   */
  private void _restoreStampState()
  {
    StampState stampState = _getStampState();
    FacesContext context = getFacesContext();
    Object currencyObj = getRowKey();
    Iterator stamps = getStamps().iterator();
    int position = 0;
    while(stamps.hasNext())
    {
      UIComponent stamp = (UIComponent) stamps.next();
//      String stampId = stamp.getId();
      // TODO
      // temporarily use position. later we need to use ID's to access
      // stamp state everywhere, and special case NamingContainers:
      String stampId = String.valueOf(position++);
      Object state = stampState.get(currencyObj, stampId);
      if (state == null)
      {
        Object iniStateObj = _getCurrencyKeyForInitialStampState();
        state = stampState.get(iniStateObj, stampId);
        if (state==null)
        {
          _LOG.severe("There was no initial stamp state for currencyKey:"+
                      currencyObj+" and currencyKeyForInitialStampState:"+
                      iniStateObj+" and stampId:"+stampId);
          continue;
        }
      }
      restoreStampState(context, stamp, state);
    }
  }

  private InternalState _getInternalState(boolean create)
  {
    if ((_state == null) && create)
    {
      _state = new InternalState();
    }
    return _state;
  }

  private StampState _getStampState()
  {
    InternalState iState = _getInternalState(true);
    if (iState._stampState == null)
      iState._stampState = new StampState();

    return iState._stampState;
  }

  private ValueMap _getCurrencyCache()
  {
    InternalState iState = _getInternalState(true);
    if (iState._currencyCache == null)
      iState._currencyCache = new ValueMap();
    return iState._currencyCache;
  }

  /**
   * sets an EL variable.
   * @param varName the name of the variable
   * @param newData the value of the variable
   * @return the old value of the variable, or null if there was no old value.
   */
  private Object _setELVar(String varName, Object newData)
  {
    assert varName != null;
    // we need to place each row at an EL reachable place so that it
    // can be accessed via the 'var' variable. Let's place it on the
    // requestMap:
    return TableUtils.setupELVariable(getFacesContext(), varName, newData);
  }

  private static boolean _equals(Object a, Object b)
  {
    if (b == null)
      return (a == null);

    return b.equals(a);
  }

  //
  // Optimized path that avoids creating the Facet map
  // unless necessary
  //
  private int _getFacetCount(UIComponent component)
  {
    if (component instanceof UIXComponent)
      return ((UIXComponent) component).getFacetCount();

    return component.getFacets().size();
  }

  private static final class InternalState implements Serializable
  {
    private transient boolean _hasEvent = false;
    private transient Object _prevVarValue = _NULL;
    private transient Object _prevVarStatus = _NULL;
    private transient String _var = null;
    private transient String _varStatus = null;
    private transient Object _value = null;
    private transient CollectionModel _model = null;
    private transient Object _currentRowKey = _NULL;
    // this is true if this is the first request for this viewID and processDecodes
    // was not called:
    private transient boolean _isFirstRender = true;
    private transient boolean _isInitialized = false;
    // this is the rowKey used to retrieve the default stamp-state for all rows:
    private transient Object _initialStampStateKey = _NULL;

    private StampState _stampState = null;
    private ValueMap _currencyCache = null;
  }

  // do not assign a non-null value. values should be assigned lazily. this is
  // because this value is preserved by stampStateSaving, when table-within-table
  // is used. And if a non-null value is used, then all nested tables will
  // end up sharing this stampState. see bug 4279735:
  private InternalState _state = null;

  // use this key to indicate uninitialized state.
  // all the variables that use this are transient so this object need not
  // be Serializable:
  private static final Object _NULL = new Object();
  private static final Object[] _EMPTY_ARRAY = new Object[0];
  private static final ADFLogger _LOG = ADFLogger.createADFLogger(UIXCollection.class);
}
