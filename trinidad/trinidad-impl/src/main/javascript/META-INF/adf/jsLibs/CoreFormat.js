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

function TrIntegerConverter(
  message,
  maxPrecision,
  maxScale,
  maxValue,
  minValue)
{
  this._message = message;
  this._maxPrecision = maxPrecision;
  this._maxScale = maxScale;
  this._maxValue = maxValue;
  this._minValue = minValue;

  // for debugging
  this._class = "TrIntegerConverter";
}

TrIntegerConverter.prototype = new TrConverter();


TrIntegerConverter.prototype.getAsString = function(
  number,
  label
  )
{
  return "" + number;
}

TrIntegerConverter.prototype.getAsObject = function(
  numberString,
  label
  )
{
  return _decimalParse(numberString, 
                       this._message,
                       "org.apache.myfaces.trinidad.convert.IntegerConverter",
                       this._maxPrecision,
                       this._maxScale,
                       this._maxValue,
                       this._minValue,
                       label);
}
function TrLongConverter(
  message,
  maxPrecision,
  maxScale,
  maxValue,
  minValue)
{
  this._message = message;
  this._maxPrecision = maxPrecision;
  this._maxScale = maxScale;
  this._maxValue = maxValue;
  this._minValue = minValue;

  // for debugging
  this._class = "TrLongConverter";
}

TrLongConverter.prototype = new TrConverter();


TrLongConverter.prototype.getAsString = function(
  number,
  label
  )
{
  return "" + number;
}

TrLongConverter.prototype.getAsObject = function(
  numberString,
  label
  )
{
  return _decimalParse(numberString, 
                       this._message,
                       "org.apache.myfaces.trinidad.convert.LongConverter",
                       this._maxPrecision,
                       this._maxScale,
                       this._maxValue,
                       this._minValue,
                       label);
}
function TrShortConverter(
  message,
  maxPrecision,
  maxScale,
  maxValue,
  minValue)
{
  this._message = message;
  this._maxPrecision = maxPrecision;
  this._maxScale = maxScale;
  this._maxValue = maxValue;
  this._minValue = minValue;

  // for debugging
  this._class = "TrShortConverter";
}

TrShortConverter.prototype = new TrConverter();


TrShortConverter.prototype.getAsString = function(
  number,
  label
  )
{
  return "" + number;
}

TrShortConverter.prototype.getAsObject = function(
  numberString,
  label
  )
{
  return _decimalParse(numberString, 
                       this._message,
                       "org.apache.myfaces.trinidad.convert.ShortConverter",
                       this._maxPrecision,
                       this._maxScale,
                       this._maxValue,
                       this._minValue,
                       label);
}
function TrByteConverter(
  message,
  maxPrecision,
  maxScale,
  maxValue,
  minValue)
{
  this._message = message;
  this._maxPrecision = maxPrecision;
  this._maxScale = maxScale;
  this._maxValue = maxValue;
  this._minValue = minValue;

  // for debugging
  this._class = "TrByteConverter";
}

TrByteConverter.prototype = new TrConverter();


TrByteConverter.prototype.getAsString = function(
  number,
  label
  )
{
  return "" + number;
}

TrByteConverter.prototype.getAsObject = function(
  numberString,
  label
  )
{
  return _decimalParse(numberString, 
                       this._message,
                       "org.apache.myfaces.trinidad.convert.ByteConverter",
                       this._maxPrecision,
                       this._maxScale,
                       this._maxValue,
                       this._minValue,
                       label);
}

function TrDoubleConverter(
  message,
  maxPrecision,
  maxScale,
  maxValue,
  minValue)
{
  this._message = message;
  this._maxPrecision = maxPrecision;
  this._maxScale = maxScale;
  this._maxValue = maxValue;
  this._minValue = minValue;

  // for debugging
  this._class = "TrDoubleConverter";
}

TrDoubleConverter.prototype = new TrConverter();


TrDoubleConverter.prototype.getAsString = function(
  number,
  label
  )
{
  return "" + number;
}

TrDoubleConverter.prototype.getAsObject = function(
  numberString,
  label
  )
{
  return _decimalParse(numberString, 
                       this._message,
                       "org.apache.myfaces.trinidad.convert.DoubleConverter",
                       this._maxPrecision,
                       this._maxScale,
                       this._maxValue,
                       this._minValue,
                       label);
}
function TrFloatConverter(
  message,
  maxPrecision,
  maxScale,
  maxValue,
  minValue)
{
  this._message = message;
  this._maxPrecision = maxPrecision;
  this._maxScale = maxScale;
  this._maxValue = maxValue;
  this._minValue = minValue;

  // for debugging
  this._class = "TrFloatConverter";
}

TrFloatConverter.prototype = new TrConverter();

TrFloatConverter.prototype.getAsString = function(
  number,
  label
  )
{
  return "" + number;
}

TrFloatConverter.prototype.getAsObject = function(
  numberString,
  label
  )
{
  return _decimalParse(numberString, 
                       this._message,
                       "org.apache.myfaces.trinidad.convert.FloatConverter",
                       this._maxPrecision,
                       this._maxScale,
                       this._maxValue,
                       this._minValue,
                       label);
}


function TrRangeValidator(
  maxValue,
  minValue)
{
  this._maxValue = maxValue;
  this._minValue = minValue;

  // for debugging
  this._class = "TrRangeValidator";
}

TrRangeValidator.prototype = new TrValidator();
TrRangeValidator.prototype.validate  = function(
  value,
  label
)
{
  string = "" + value;
  numberValue = parseFloat(string);
  
  if(numberValue >= this._minValue && numberValue <= this._maxValue)
  {
    return string;
  }
  else
  {
    if(numberValue>this._maxValue)
    {
      facesMessage = _createFacesMessage("org.apache.myfaces.trinidad.validator.LongRangeValidator.MAXIMUM",
                                        label,
                                        string,
                                        ""+this._maxValue);
    }
    else
    {
      facesMessage = _createFacesMessage("org.apache.myfaces.trinidad.validator.LongRangeValidator.MINIMUM",
                                        label,
                                        string,
                                        ""+this._minValue);
    }
    throw new TrConverterException(facesMessage);
  }
}

function TrLengthValidator(
  maxValue,
  minValue)
{
 
  this._maxValue = maxValue;
  this._minValue = minValue;

  // for debugging
  this._class = "TrLengthValidator";
}

TrLengthValidator.prototype = new TrValidator();
TrLengthValidator.prototype.validate  = function(
  value,
  label
)
{

  string = "" + value;
  length = string.length;
  
  if(length >= this._minValue && length <= this._maxValue)
  {
    return string;
  }
  else
  {
    facesMessage = _createFacesMessage("org.apache.myfaces.trinidad.validator.LengthValidator.NOT_IN_RANGE",
                                        label,
                                        string,
                                        ""+this._minValue,
                                        ""+this._maxValue);
    throw new TrConverterException(facesMessage);
  }
}

function TrDateTimeRangeValidator(
  maxValue,
  minValue)
{
 
  this._maxValue = maxValue;
  this._minValue = minValue;

  // for debugging
  this._class = "TrDateTimeRangeValidator";
}

TrDateTimeRangeValidator.prototype = new TrValidator();
TrDateTimeRangeValidator.prototype.validate  = function(
  value,
  label
)
{
  dateTime = value.getTime();
  minDate = parseInt(this._minValue);
  maxDate = parseInt(this._maxValue);
  
  if(dateTime >= minDate && dateTime <= maxDate)
  {
    return value;
  }
  else
  {
    facesMessage = _createFacesMessage("org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.NOT_IN_RANGE",
                                        label,
                                        ""+value,
                                        ""+new Date(this._minValue),
                                        ""+new Date(this._maxValue));
    throw new TrConverterException(facesMessage);
  }
  
}

function TrDateRestrictionValidator(
  weekdaysValue,
  monthValue,
  weekdaysMap,
  monthMap)
{
  this._weekdaysValue = weekdaysValue;
  this._monthValue = monthValue;
  this._weekdaysMap = weekdaysMap;
  this._monthMap = monthMap;

  // for debugging
  this._class = "TrDateRestrictionValidator";
}

TrDateRestrictionValidator.prototype = new TrValidator();
TrDateRestrictionValidator.prototype.validate  = function(
  value,
  label
)
{
  submittedDay = value.getDay();
  weekDaysArray = eval(this._weekdaysValue);
  if(weekDaysArray)
  {
  	var dayString = this._weekdaysMap[submittedDay];
  	for(var i = 0; i < weekDaysArray.length; ++i)
  	{
  		if(weekDaysArray[i].toLowerCase() == dayString)
  		{
        facesMessage = _createFacesMessage("org.apache.myfaces.trinidad.validator.DateRestrictionValidator.WEEKDAY",
                                        label,
                                        ""+value,
                                        dayString);
        throw new TrConverterException(facesMessage);
  		}
  	}
  }
  
  submittedMonth = value.getMonth();
  monthArray = eval(this._monthValue);
  if(monthArray)
  {
  	var monthString = this._monthMap[submittedMonth];
  	for(var i = 0; i < monthArray.length; ++i)
  	{
  		if(monthArray[i].toLowerCase() == monthString)
  		{
        facesMessage = _createFacesMessage("org.apache.myfaces.trinidad.validator.DateRestrictionValidator.MONTH",
                                        label,
                                        ""+value,
                                        monthString);
        throw new TrConverterException(facesMessage);
  		}
  	}
  }
	return value;
}

function _decimalParse(
  numberString,
  message,
  standardKey,
  maxValue,
  minValue,
  label
  )
{

  // The following are from the javadoc for TrNumberConverter
  // If the specified String is null, return a null. Otherwise, trim leading and trailing whitespace before proceeding.
  // If the specified String - after trimming - has a zero length, return null.
  if (numberString == null)
    return null;
    
  numberString = TrUIUtils.trim(numberString);
  if (numberString.length == 0)
    return null
    
  var facesMessage = null;        

  // Get LocaleSymbols (from Locale.js)
  var symbols = getLocaleSymbols();
  if (symbols)
  {
    // We don't want leading or trailing grouping separators
    var grouping = symbols.getGroupingSeparator();
    if ((numberString.indexOf(grouping) == 0) ||
        (numberString.lastIndexOf(grouping) ==  (numberString.length - 1)))
    {
      if(message == null)
      {
        facesMessage =  _createFacesMessage( standardKey+".CONVERT",
                                          label,
                                          numberString);
      }
      else
      { 
        facesMessage =  _createFacesMessage( standardKey+".CONVERT",
                                          label,
                                          numberString);
        //var summary = "";
        //var detail = "";
        //facesMessage =  _createCustomMessage( summary,
                                          //detail,
                                          //label,
                                          //numberString);
      }
      throw new TrConverterException(facesMessage);
    }

    // Remove the thousands separator - which Javascript doesn't want to see
    var thousands = new RegExp("\\" + grouping, "g");
    numberString = numberString.replace(thousands, "");
    // Then change the decimal separator into a period, the only
    // decimal separator allowed by JS
    var decimal = new RegExp("\\" + symbols.getDecimalSeparator(),  "g");
    numberString = numberString.replace(decimal, ".");
  }


  // OK; it's non-empty.  Now, disallow exponential
  // notation, and then use some JS magic to exclude
  // non-numbers
  if ((numberString.indexOf('e') < 0) &&
      (numberString.indexOf('E') < 0) &&
      (((numberString * numberString) == 0) ||
       ((numberString / numberString) == 1)))
  {
    var result = parseFloat(numberString);
    if (!isNaN(result))
    {
      var integerDigits = numberString.length;
      var fractionDigits = 0;

      var sepIndex = numberString.lastIndexOf('.');
      if (sepIndex != -1)
      {
        integerDigits = sepIndex;
        fractionDigits = parseInt(numberString.length - parseInt(sepIndex -1));
      }
      
      var messageKey;
      //not true for float/double converter
      if ((maxValue != null) &&
          (result  > maxValue))
      {
        messageKey = standardKey+".MAXIMUM";
      }
      else if ((minValue != null) &&
               (result  < minValue))
      {
        messageKey = standardKey+".MINIMUM";
      }

      if (messageKey != null)
      {
        var messages = messages;
        if ((messages == null) ||
            (messages[messageKey] == null))
          throw  new TrConverterException(null, null, "Conversion failed, but no appropriate message found");  // default error format
        else
        {
          facesMessage =  _createFacesMessage( messageKey,
                                      label,
                                      numberString);
          throw new TrConverterException(facesMessage);
        }
       }
      return result;
    }
  }

  facesMessage = _createFacesMessage( standardKey+".CONVERT",
                                        label,
                                        numberString);
  throw new TrConverterException(facesMessage);
}

function TrRegExpValidator(
  pattern,
  summary,
  detail
  )
{  
  this._pattern  = pattern;
  this._summary = summary;
  this._detail = detail;
  this._class = "TrRegExpValidator";
}

TrRegExpValidator.prototype = new TrValidator();
TrRegExpValidator.prototype.validate  = function(
  parseString,
  label
  )
{
  //For some reason when using digits as input values 
  // parseString becomes a integer type, so get away with it.  
  parseString = parseString + '';
  
  var matchArr = parseString.match(this._pattern); 
        
  if ((matchArr != (void 0)) && (matchArr[0] == parseString))
  {
    return parseString;
  }
  else
  {
    var facesMessage;
    if(this._summary == undefined)
    {
      facesMessage = _createFacesMessage( "org.apache.myfaces.trinidad.validator.RegExpValidator.NO_MATCH",
                                              label,
                                              parseString,
                                              this._pattern);                                          
    }
    else
    {
      facesMessage = _createCustomFacesMessage(this._summary,
                                          this._detail,
                                          label,
                                          parseString,
                                          this._pattern);                                          
    }
    throw new TrValidatorException(facesMessage); 
  }
}