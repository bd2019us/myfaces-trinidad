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
function _decimalGetAsString(
  number
  )
{
  return "" + number;
}

function _decimalFacesMessage(
  messages,
  messageKey
)
{
  return new FacesMessage(messages[(messageKey+ '_S')], 
                          messages[messageKey], 
                          FacesMessage.SEVERITY_ERROR);  
}

function _decimalParse(
  numberString,
  messages,
  maxPrecision,
  maxScale,
  maxValue,
  minValue
  )
{
  var facesMessage = null; 
  if (!numberString)
  { 
    facesMessage = _decimalFacesMessage(messages, DecimalFormat.D);
    throw new ConverterException(null, facesMessage);
  }
       

  // Get LocaleSymbols (from Locale.js)
  var symbols = getLocaleSymbols();
  if (symbols)
  {
    // We don't want leading or trailing grouping separators
    var grouping = symbols.getGroupingSeparator();
    if ((numberString.indexOf(grouping) == 0) ||
        (numberString.lastIndexOf(grouping) ==  (numberString.length - 1)))
    {
      facesMessage = _decimalFacesMessage(messages, DecimalFormat.D);
      throw new ConverterException(null, facesMessage);
    }

    // Remove the thousands separator - which Javascript doesn't want to see
    var thousands = new RegExp("\\" + grouping, "g");
    numberString = numberString.replace(thousands, "");
    // Then change the decimal separator into a period, the only
    // decimal separator allowed by JS
    var decimal = new RegExp("\\" + symbols.getDecimalSeparator(),  "g");
    numberString = numberString.replace(decimal, ".");
  }

  // First, reject anything that's all spaces
  var i = numberString.length - 1;
  while (i >= 0)
  {
    if (numberString.charAt(i) != ' ')
      break;
    i--;
  }

  if (i >= 0)
  {
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
          fractionDigits = numberString.length - sepIndex -1;
        }
        
        var messageKey;
        
        if ((maxValue != (void 0)) &&
            (result  > maxValue))
        {
          messageKey = DecimalFormat.LV;
        }
        else if ((minValue != (void 0)) &&
                 (result  < minValue))
        {
          messageKey = DecimalFormat.MV;
        }
        else if ((maxPrecision != (void 0)) &&
                 (integerDigits  > maxPrecision))
        {
          messageKey = DecimalFormat.LID;
        }
        else if ((maxScale != (void 0)) &&
                 (fractionDigits  > maxScale))
        {
          messageKey = DecimalFormat.LFD;
        }

        if (messageKey != (void 0))
        {
          var messages = messages;
          
          if ((messages == (void 0)) ||
              (messages[messageKey] == (void 0)))
            throw  new ConverterException("Conversion failed, but no appropriate message found");  // default error format
          else
          {
            facesMessage = _decimalFacesMessage(messages, messageKey);
            throw new ConverterException(null, facesMessage);
          }
        }
        
        return result;
      }
    }
  }

  facesMessage = _decimalFacesMessage(messages, DecimalFormat.D);
  throw new ConverterException(null, facesMessage);
}

function _decimalGetAsObject(
  numberString
  )
{
  return _decimalParse(numberString, 
                       this._messages,
                       this._maxPrecision,
                       this._maxScale,
                       this._maxValue,
                       this._minValue);
}

function DecimalFormat(
  messages,
  maxPrecision,
  maxScale,
  maxValue,
  minValue)
{
  this._messages = messages;
  this._maxPrecision = maxPrecision;
  this._maxScale = maxScale;
  this._maxValue = maxValue;
  this._minValue = minValue;

  // for debugging
  this._class = "DecimalFormat";
}

DecimalFormat.prototype = new Converter();
DecimalFormat.prototype.getAsString  = _decimalGetAsString;
DecimalFormat.prototype.getAsObject  = _decimalGetAsObject;

// Less fraction digits
DecimalFormat.LFD = 'LFD';
// Less integer digits
DecimalFormat.LID = 'LID';
// Less value
DecimalFormat.LV  = 'LV';
// More value
DecimalFormat.MV  = 'MV';
// default
DecimalFormat.D   = 'D';


function _decimalValidate(
  value
)
{
  // This should probably do more than call decimalParse!
  // the following line is needed because what's being passed
  // into the validator is a number, and _decimalParse expects a string.
  numberString = "" + value;
  
  try
  {
    return _decimalParse(numberString, 
                       this._messages,
                       this._maxPrecision,
                       this._maxScale,
                       this._maxValue,
                       this._minValue);
  }
  catch (e)
  {
    throw new ValidatorException((void 0), e.getFacesMessage());
  }
}

function DecimalValidator(
  messages,
  maxPrecision,
  maxScale,
  maxValue,
  minValue)
{
  this._messages = messages;
  this._maxPrecision = maxPrecision;
  this._maxScale = maxScale;
  this._maxValue = maxValue;
  this._minValue = minValue;

  // for debugging
  this._class = "DecimalValidator";
}

DecimalValidator.prototype = new Validator();
DecimalValidator.prototype.validate  = _decimalValidate;


function _regExpParse(
  parseString
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
    // format the error string
    //   {2}    a legal example
    if (this._messages[RegExpFormat.NM] != null)
    {
      this._messages[RegExpFormat.NM] = 
                  _formatErrorString(this._messages[RegExpFormat.NM],
                                     { 
                                       "2":this._pattern
                                     });
    }

    var facesMessage = new FacesMessage(this._messages[RegExpFormat.NMS], 
                                        this._messages[RegExpFormat.NM], 
                                        FacesMessage.SEVERITY_ERROR);
    throw new ValidatorException(null, facesMessage); 
  }
}


function RegExpFormat(
  pattern,
  messages
  )
{  
  this._pattern  = pattern;
  this._messages = messages;
  this._class = "RegExpFormat";
}

// no match pattern
RegExpFormat.NM = 'NM';
// no match pattern summary
RegExpFormat.NMS = 'NMS';

RegExpFormat.prototype = new Validator();
RegExpFormat.prototype.validate  = _regExpParse;
