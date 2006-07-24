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
package org.apache.myfaces.adfinternal.share.nls;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Context for locale-specific operations and properties.  All of the properties
 * should initially default to those of the base Locale, while allowing
 * the locale-specific properties to be overridden.
 * <p>
 * It is expected that additional properties will be added to this class
 * over time in order to support overriding the date and number formats.
 * <p>
 * Setting the DateFormatContext clones the parameter and wraps it in an
 * ImmutableDateFormatContext if necessary.  This will then be returned by
 * getDateFormatContext.  Clients are therefore guaranteed that this property
 * can not be modified after it is set on the LocaleContext, without a
 * subsequent call to setDateFormatContext.
 * <p>
 * @version $Name:  $ ($Revision: adfrt/faces/adf-faces-impl/src/main/java/oracle/adfinternal/view/faces/share/nls/MutableLocaleContext.java#0 $) $Date: 10-nov-2005.19:00:05 $
 * @author The Oracle ADF Faces Team
 */
public final class MutableLocaleContext extends LocaleContext
{
  /**
   * Creates a MutableLocaleContext based off of the default Locale.
   */
  public MutableLocaleContext()
  {
    super();
  }

  /**
   * Creates a MutableLocaleContext based off of the specified Locale.
   */
  public MutableLocaleContext(
    Locale baseLocale
    )
  {
    super(baseLocale);
  }

  /**
   * Creates a MutableLocaleContext based off of the specified Locale and using
   * a different Locale for translations.  Applications that only provide
   * translations for a subset of the Locales provided by subcomponents
   * can use the translation Locale to force subcomponents to only
   * use translations in a language supported by the application.
   * <p>
   * @param baseLocale Locale providing default behavior for the LocaleContext.
   *                   If not specified, the defualt Locale is used.
   * @param translationLocale Locale to use for translations.  If not
   *                          specified, the baseLocale is used.
   */
  public MutableLocaleContext(
    Locale baseLocale,
    Locale translationLocale
    )
  {
    super(baseLocale, translationLocale);
  }

  /**
   * Creates a MutableLocaleContext based on a pre-existing LocaleContext.
   */
  public MutableLocaleContext(LocaleContext context)
  {
    super(context.getLocale(), context.getTranslationLocale());

    setReadingDirection(context.getReadingDirection());
    setTimeZone(context.getTimeZone());
    setDateFormatContext(context.getDateFormatContext());
    setDecimalFormatContext(context.getDecimalFormatContext());
  }

  /**
   * Sets the new reading direction to be one of the reading directions
   * defined in <code>org.apache.myfaces.adfinternal.util.nls.LocaleUtils</code>.
   * <p>
   * If set to any value other than
   * <code>LocaleUtils.DIRECTION_DEFAULT</code>, the new value will override
   * the default determination of the reading direction based on the Locale.
   * <p>
   * @see #getReadingDirection
   * @see org.apache.myfaces.adfinternal.util.nls.LocaleUtils
   */
  public void setReadingDirection(
    int newReadingDirection
    )
  {
    super.setReadingDirection(newReadingDirection);
  }

  /**
   * Sets the TimeZone that the user is running in.  Setting this value
   * to null will set the TimeZone to the default TimeZone.
   */
  public void setTimeZone(
    TimeZone newTimeZone
    )
  {
    super.setTimeZone(newTimeZone);
  }

  /**
   * Sets the DateFormatContext containing all date format parameters.
   * If necessary, this dateFormatContext is cloned and wrapped inside
   * an ImmutableDateFormatContext which is returned by
   * <code>getDateFormatContext</code>.
   */
  public final void setDateFormatContext(
    DateFormatContext  dateFormatContext)
  {
    if (!ImmutableDateFormatContext.class.equals(dateFormatContext.getClass()))
      dateFormatContext = new ImmutableDateFormatContext(dateFormatContext);

    setDateFormatContextImpl(dateFormatContext);
  }


  /**
   * Sets the DecimalFormatContext containing all number format parameters.
   * If necessary, this decimalFormatContext is cloned and wrapped inside
   * an ImmutableDecimalFormatContext which is returned by
   * <code>getDecimalFormatContext</code>.
   */
  public final void setDecimalFormatContext(
    DecimalFormatContext  decimalFormatContext)
  {
    if (!ImmutableDecimalFormatContext.class.equals(
           decimalFormatContext.getClass()))
      decimalFormatContext =
         new ImmutableDecimalFormatContext(decimalFormatContext);

    setDecimalFormatContextImpl(decimalFormatContext);
  }


  /**
   * Returns the DateFormatContext containing all date format parameters.
   */
  protected DateFormatContext getDateFormatContextImpl()
  {
    return _dateFormatContext;
  }

  /**
   * Sets the DateFormatContext containing all date format parameters.
   */
  protected void setDateFormatContextImpl(
    DateFormatContext  dateFormatContext)
  {
    _dateFormatContext = dateFormatContext;
  }

  /**
   * Returns the DecimalFormatContext containing all number format parameters.
   */
  protected DecimalFormatContext getDecimalFormatContextImpl()
  {
    return _decimalFormatContext;
  }

  /**
   * Sets the DecimalFormatContext containing all number format parameters.
   */
  protected void setDecimalFormatContextImpl(
    DecimalFormatContext  decimalFormatContext)
  {
    _decimalFormatContext = decimalFormatContext;
  }

  private DateFormatContext   _dateFormatContext;
  private DecimalFormatContext _decimalFormatContext;
}

